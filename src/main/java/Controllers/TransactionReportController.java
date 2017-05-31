package Controllers;

import Constants.Constant;
import PDF.ReportGenerator;
import com.itextpdf.text.DocumentException;
import db.DBExecuteCustomer;
import db.DBExecuteStaff;
import db.DBExecuteTransaction;
import db.DBQueries;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.*;
import org.apache.log4j.Logger;
import util.AlertBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by Tjin on 5/27/2017.
 */
public class TransactionReportController {
    public static Logger logger= Logger.getLogger(TransactionReportController.class);
    private Stage dialogStage;
    private Executor executor;
    private List<Transaction> finalizedOutTransactions;
    private List<Transaction> pendingOutTransactions;
    private List<Transaction> brokenInTransactions;
    private List<TransactionReport> transactionReports;
    private List<Staff> staffs;
    private List<Customer> customers;
    private ObservableList<TransactionReport> transactionReportObservableList;
    private ObservableList<TransactionReport> subTransactionReportObservableList;
    private DBExecuteTransaction dbExecuteTransaction;
    private DBExecuteStaff dbExecuteStaff;
    private DBExecuteCustomer dbExecuteCustomer;
    private StringProperty reportTile;
    private DoubleProperty total;
    private LocalDate fromDate;
    private LocalDate toDate;


    @FXML
    private ComboBox<TransactionReport.ReportType> reportTypeComboBox;
    @FXML
    private TableView<TransactionReport> transactionReportTable;
    @FXML
    private DatePicker fromDatePicker;
    @FXML
    private DatePicker toDatePicker;
    @FXML
    private TableColumn<TransactionReport, String> dateCol;
    @FXML
    private TableColumn<TransactionReport, Integer> transactionIdCol;
    @FXML
    private TableColumn<TransactionReport, String> staffCol;
    @FXML
    private TableColumn<TransactionReport, String> infoCol;
    @FXML
    private TableColumn<TransactionReport, Double> transactionTotalCol;
    @FXML
    private TableColumn<TransactionReport, String> transactionTypeCol;
    @FXML
    private TableColumn<TransactionReport, String> reportTypeCol;
    @FXML
    private TableColumn<TransactionReport, Double> amountCol;
    @FXML
    private Button generateButton;
    @FXML
    private Label reportTitleLabel;
    @FXML
    private Label totalLabel;


    @FXML
    private void initialize(){
        dateCol.setCellValueFactory(new PropertyValueFactory<>("paymentDate"));
        transactionIdCol.setCellValueFactory(new PropertyValueFactory<>("transactionId"));
        staffCol.setCellValueFactory(new PropertyValueFactory<>("staffName"));
        infoCol.setCellValueFactory(new PropertyValueFactory<>("info"));
        transactionTotalCol.setCellValueFactory(new PropertyValueFactory<>("transactionTotal"));
        transactionTypeCol.setCellValueFactory(new PropertyValueFactory<>("transactionType"));
        reportTypeCol.setCellValueFactory(new PropertyValueFactory<>("reportType"));
        amountCol.setCellValueFactory(new PropertyValueFactory<>("reportValue"));
        fromDatePicker.setOnAction(event -> {
            this.fromDate = fromDatePicker.getValue();
            ObservableList<TransactionReport> subList = FXCollections.observableArrayList();
            for(TransactionReport report: transactionReportObservableList){
                LocalDate date = LocalDate.parse(report.getPaymentDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                if(this.fromDate.isBefore(date)){
                    if(this.toDate != null){
                        if(this.toDate.isAfter(date)){
                            subList.add(report);
                        }
                    }else{
                        subList.add(report);
                    }
                }
            }
            transactionReportTable.setItems(subList);
            getTotalValue(subList);

        });
        toDatePicker.setOnAction(event -> {
            this.toDate = toDatePicker.getValue();
            ObservableList<TransactionReport> subList = FXCollections.observableArrayList();
            for(TransactionReport report: transactionReportObservableList){
                LocalDate date = LocalDate.parse(report.getPaymentDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                if(this.toDate.isAfter(date)){
                    if(this.fromDate != null){
                        if(this.fromDate.isBefore(date)){
                            subList.add(report);
                        }
                    }else{
                        subList.add(report);
                    }
                }
            }
            transactionReportTable.setItems(subList);
            getTotalValue(subList);
        });
        reportTypeComboBox.setItems(FXCollections.observableArrayList(TransactionReport.ReportType.values()));
        reportTypeComboBox.setConverter(new StringConverter<TransactionReport.ReportType>() {
            @Override
            public String toString(TransactionReport.ReportType object) {
                if(object == null){
                    return null;
                }
                return object.toString();
            }

            @Override
            public TransactionReport.ReportType fromString(String string) {
                if(string == null){
                    return null;
                }
                return TransactionReport.ReportType.valueOf(string);
            }
        });
        totalLabel.textProperty().bind(total.asString());
        reportTitleLabel.textProperty().bind(reportTile);
        generateButton.disableProperty().bind(reportTypeComboBox.valueProperty().isNull());

        executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });

    }

    @FXML
    private void handleGenerateButton(){
        transactionReports = new ArrayList<>();
        TransactionReport.ReportType type = reportTypeComboBox.getValue();
        switch(type){
            case Deposit:
                for(Transaction transaction: pendingOutTransactions){
                    List<TransactionReport> reports = buildReportFromTransaction(transaction, type);
                    transactionReports.addAll(reports);
                }
                break;
            case Broken:
                for(Transaction transaction: brokenInTransactions){
                    List<TransactionReport> reports = buildReportFromTransaction(transaction, type);
                    transactionReports.addAll(reports);
                }
                break;
            default:
                for(Transaction transaction: finalizedOutTransactions){
                    List<TransactionReport> reports = buildReportFromTransaction(transaction, type);
                    transactionReports.addAll(reports);
                }
                break;
        }
        transactionReportObservableList = FXCollections.observableList(transactionReports);
        transactionReportTable.setItems(transactionReportObservableList);
        getTotalValue(transactionReportObservableList);
        reportTile.setValue("Milan Tile " + type + " Transaction Report");
    }

    @FXML
    private void handleCancelButton(){
        this.dialogStage.close();
    }

    @FXML
    private void handleSaveButton(){
        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File file = fileChooser.showSaveDialog(this.dialogStage);
        if(file != null){
            ReportGenerator reportGenerator = new ReportGenerator(transactionReportTable.getItems(), file.getAbsolutePath(), reportTile.get(), total.get());
            try {
                reportGenerator.generate();
            } catch (DocumentException | FileNotFoundException e) {
                logger.info(e.getMessage(), e);
            }
        }

    }

    public TransactionReportController(){
        dbExecuteTransaction = new DBExecuteTransaction();
        dbExecuteStaff = new DBExecuteStaff();
        dbExecuteCustomer = new DBExecuteCustomer();
        brokenInTransactions = new ArrayList<>();
        finalizedOutTransactions = new ArrayList<>();
        pendingOutTransactions = new ArrayList<>();
        transactionReports = new ArrayList<>();
        reportTile = new SimpleStringProperty("Milan Tile Transaction Report");
        total = new SimpleDoubleProperty(0);
    }

    public void setDialogStage(Stage dialogStage){
        this.dialogStage = dialogStage;
    }

    public void loadDataFromDB(){
        Task<List<Staff>> staffTask = new Task<List<Staff>>() {
            @Override
            protected List<Staff> call() throws Exception {
                logger.info("Retrieve all staffs from the database");
                return dbExecuteStaff.selectFromDatabase(DBQueries.SelectQueries.Staff.SELECT_ALL_STAFF);
            }
        };
        Task<List<Transaction>> transactionTask = new Task<List<Transaction>>() {
            @Override
            protected List<Transaction> call() throws Exception {
                logger.info("Retrieve all transactions from the database");
                return dbExecuteTransaction.selectFromDatabase(DBQueries.SelectQueries.Transaction.SELECT_ALL_TRANSACTION);
            }
        };
        Task<List<Customer>> customerTask = new Task<List<Customer>>() {
            @Override
            protected List<Customer> call() throws Exception {
                logger.info("Retrieve all customers from the database");
                return dbExecuteCustomer.selectFromDatabase(DBQueries.SelectQueries.Customer.SELECT_ALL_CUSTOMER);
            }
        };
        transactionTask.setOnSucceeded(event -> {
            List<Transaction> tmpTransactions = transactionTask.getValue();
            tmpTransactions.forEach(transaction -> {
                if(transaction.getType().equals(Transaction.TransactionType.IN) && transaction.getTotal() < 0){
                    brokenInTransactions.add(transaction);
                }else if(transaction.getType().equals(Transaction.TransactionType.OUT)){
                    if(transaction.getTotal() == transaction.getPayment()){
                        finalizedOutTransactions.add(transaction);
                    }else{
                        pendingOutTransactions.add(transaction);
                    }
                }
            });
        });

        transactionTask.setOnFailed(event -> new AlertBuilder()
                .alertType(Alert.AlertType.ERROR)
                .alertContentText(Constant.DatabaseError.databaseUpdateError + event.getSource().exceptionProperty().getValue())
                .alertTitle(Constant.DatabaseError.databaseErrorAlertTitle)
                .build()
                .showAndWait());

        staffTask.setOnSucceeded(event -> staffs = staffTask.getValue());
        staffTask.setOnFailed(event -> new AlertBuilder()
                .alertType(Alert.AlertType.ERROR)
                .alertContentText(Constant.DatabaseError.databaseUpdateError + event.getSource().exceptionProperty().getValue())
                .alertTitle(Constant.DatabaseError.databaseErrorAlertTitle)
                .build()
                .showAndWait());
        customerTask.setOnSucceeded(event -> customers = customerTask.getValue());
        customerTask.setOnFailed(event -> new AlertBuilder()
                .alertType(Alert.AlertType.ERROR)
                .alertContentText(Constant.DatabaseError.databaseUpdateError + event.getSource().exceptionProperty().getValue())
                .alertTitle(Constant.DatabaseError.databaseErrorAlertTitle)
                .build()
                .showAndWait());
        executor.execute(staffTask);
        executor.execute(transactionTask);
        executor.execute(customerTask);
    }

    private List<TransactionReport> buildReportFromTransaction(Transaction transaction, TransactionReport.ReportType type){
        List<TransactionReport> tmpList = new ArrayList<>();
        TransactionReport transactionReport;
        switch(type){
            case GST:
                transactionReport = buildDefaultTransactionReport(transaction, type);
                transactionReport.setReportValue(transaction.getGstTax());
                transactionReport.setPaymentDate(transaction.getPayinfo().get(transaction.getPayinfo().size() - 1).getDate());
                tmpList.add(transactionReport);
                break;
            case PST:
                transactionReport = buildDefaultTransactionReport(transaction, type);
                transactionReport.setReportValue(transaction.getPstTax());
                transactionReport.setPaymentDate(transaction.getPayinfo().get(transaction.getPayinfo().size() - 1).getDate());
                tmpList.add(transactionReport);
                break;
            case Broken:
                transactionReport = buildDefaultTransactionReport(transaction, type);
                transactionReport.setReportValue(transaction.getTotal());
                transactionReport.setPaymentDate(transaction.getPayinfo().get(transaction.getPayinfo().size() - 1).getDate());
                tmpList.add(transactionReport);
            case Cash:
                for(PaymentRecord paymentRecord: transaction.getPayinfo()){
                    transactionReport = buildDefaultTransactionReport(transaction, type);
                    if(paymentRecord.getPaymentType().equals("Cash") && paymentRecord.getPaid() != 0){
                        transactionReport.setReportValue(paymentRecord.getPaid());
                        transactionReport.setPaymentDate(paymentRecord.getDate());
                        tmpList.add(transactionReport);
                    }
                }
                break;
            case Debit:
                for(PaymentRecord paymentRecord: transaction.getPayinfo()){
                    transactionReport = buildDefaultTransactionReport(transaction, type);
                    if(paymentRecord.getPaymentType().equals("Debit") && paymentRecord.getPaid() != 0){
                        transactionReport.setReportValue(paymentRecord.getPaid());
                        transactionReport.setPaymentDate(paymentRecord.getDate());
                        tmpList.add(transactionReport);
                    }
                }
                break;
            case Cheque:
                for(PaymentRecord paymentRecord: transaction.getPayinfo()){
                    transactionReport = buildDefaultTransactionReport(transaction, type);
                    if(paymentRecord.getPaymentType().equals("Cheque") && paymentRecord.getPaid() != 0){
                        transactionReport.setReportValue(paymentRecord.getPaid());
                        transactionReport.setPaymentDate(paymentRecord.getDate());
                        tmpList.add(transactionReport);
                    }
                }
                break;
            case Credit:
                for(PaymentRecord paymentRecord: transaction.getPayinfo()){
                    transactionReport = buildDefaultTransactionReport(transaction, type);
                    if(paymentRecord.getPaymentType().equals("Credit") && paymentRecord.getPaid() != 0){
                        transactionReport.setReportValue(paymentRecord.getPaid());
                        transactionReport.setPaymentDate(paymentRecord.getDate());
                        tmpList.add(transactionReport);
                    }
                }
                break;
            case Deposit:
                for(PaymentRecord paymentRecord: transaction.getPayinfo()){
                    transactionReport = buildDefaultTransactionReport(transaction, type);
                    if(paymentRecord.isDeposit() && paymentRecord.getPaid() != 0){
                        transactionReport.setReportValue(paymentRecord.getPaid());
                        transactionReport.setPaymentDate(paymentRecord.getDate());
                        tmpList.add(transactionReport);
                    }
                }
                break;
            default:
                break;
        }
        return tmpList;
    }

    private TransactionReport buildDefaultTransactionReport(Transaction transaction, TransactionReport.ReportType type){
        String staffName = null;
        for(Staff staff: staffs){
            if(staff.getStaffId() == transaction.getStaffId()){
                staffName = staff.getFullName();
                break;
            }
        }
        String infoName = null;
        for(Customer customer: customers){
            if(!transaction.getType().equals(Transaction.TransactionType.IN) &&
                    customer.getUserName().equals(transaction.getInfo())){
                infoName = customer.getFirstName() + " " + customer.getLastName();
                break;
            }else if(transaction.getType().equals(Transaction.TransactionType.IN)){
                infoName = transaction.getInfo();
                break;
            }
        }
        if(infoName == null){
            infoName = "<NOT FOUND>";
        }
        return new TransactionReport.TransactionReportBuilder()
                .transactionId(transaction.getTransactionId())
                .info(infoName)
                .transactionTotal(transaction.getTotal())
                .transactionType(transaction.getType())
                .reportType(type)
                .staffName(staffName)
                .build();
    }

    private void getTotalValue(ObservableList<TransactionReport> reports){
        BigDecimal totalBigDecimal = new BigDecimal(0);
        for(TransactionReport report: reports){
            totalBigDecimal = totalBigDecimal.add(new BigDecimal(report.getReportValue()));
        }
        this.total.setValue(totalBigDecimal.setScale(2, BigDecimal.ROUND_HALF_EVEN).doubleValue());
    }

}
