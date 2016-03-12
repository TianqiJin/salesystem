package Controllers;


import Constants.Constant;
import MainClass.SaleSystem;
import db.DBExecuteStaff;
import db.DBQueries;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Staff;
import util.AlertBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class StaffOverviewController implements OverviewController{

    private SaleSystem saleSystem;
    private ObservableList<Staff> staffList;
    private Executor executor;
    private Stage dialogStage;

    @FXML
    private TableView<Staff> staffTable;
    @FXML
    private TextField filterField;
    @FXML
    private TableColumn<Staff, String> nameCol;
    @FXML
    private TableColumn<Staff, String> positionCol;
    @FXML
    private Label staffIdLabel;
    @FXML
    private Label nameLabel;
    @FXML
    private Label positionLabel;
    @FXML
    private Label streetLabel;
    @FXML
    private Label cityLabel;
    @FXML
    private Label postalCodeLabel;
    @FXML
    private ProgressBar progressBar;

    @FXML
    private void initialize(){
        nameCol.setCellValueFactory(new PropertyValueFactory<>("FullName"));
        positionCol.setCellValueFactory(new PropertyValueFactory<>("Position"));
        showStaffDetail(null);
        staffTable.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<Staff>() {
                    @Override
                    public void changed(ObservableValue<? extends Staff> observable, Staff oldValue, Staff newValue) {
                        showStaffDetail(newValue);
                    }
                }
        );
        executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
    }

    @FXML
    private void handleDeleteStaff(){
        int selectedIndex = staffTable.getSelectionModel().getSelectedIndex();
        if(selectedIndex >= 0){
            String tempName = staffTable.getItems().get(selectedIndex).getFullName();
            String tempPosition = staffTable.getItems().get(selectedIndex).getPosition().name();
            Alert alertConfirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this Staff?");
            Optional<ButtonType> result =  alertConfirm.showAndWait();
            boolean flag = true;
            if(result.isPresent() && result.get() == ButtonType.OK){
                try{
                    dbExecute.deleteDatabase(DBQueries.DeleteQueries.Staff.DELETE_FROM_STAFF,
                            staffTable.getItems().get(selectedIndex).getUserName());
                }catch(SQLException e){
                    e.printStackTrace();
                    flag = false;
                    new AlertBuilder()
                            .alertType(Alert.AlertType.ERROR)
                            .alertTitle(Constant.DatabaseError.databaseErrorAlertTitle)
                            .alertContentText(Constant.DatabaseError.databaseDeleteError + e.toString())
                            .build()
                            .showAndWait();
                }finally{
                    if(flag){
                        staffTable.getItems().remove(selectedIndex);
                        new AlertBuilder()
                                .alertTitle("Delete Staff Successfully")
                                .alertContentText("Successfully deleted staff "+tempName+" "+tempPosition)
                                .build()
                                .showAndWait();
                    }
                }
            }
        }
        else{
            new AlertBuilder()
                    .alertType(Alert.AlertType.WARNING)
                    .alertTitle("No Staff Selected")
                    .alertContentText("Please select a person in the table.")
                    .build()
                    .showAndWait();
        }

    }

    @FXML
    private void handleAddStaff(){
        Staff newStaff = new Staff(new Staff.StaffBuilder());
        newStaff.setStaffId(dbExecute.getMaxNum(DBQueries.SelectQueries.Staff.SELECT_STAFF_MAX_NUM));
        boolean okClicked = saleSystem.showStaffEditDialog(newStaff);
        if(okClicked){
            try{
                dbExecute.insertIntoDatabase(DBQueries.InsertQueries.Staff.INSERT_INTO_STAFF,
                        newStaff.getAllProperties());
            }catch(SQLException e){
                new AlertBuilder()
                        .alertType(Alert.AlertType.ERROR)
                        .alertTitle(Constant.DatabaseError.databaseErrorAlertTitle)
                        .alertContentText(Constant.DatabaseError.databaseCreateError + e.toString())
                        .build()
                        .showAndWait();
            }finally{
                loadDataFromDB();
            }
        }
    }

    @FXML
    private void handleEditStaff(){
        Staff selectedStaff = staffTable.getSelectionModel().getSelectedItem();
        if(selectedStaff != null){
            boolean onClicked = saleSystem.showStaffEditDialog(selectedStaff);
            if(onClicked){
                try{
                    dbExecute.updateDatabase(DBQueries.UpdateQueries.Staff.UPDATE_STAFF,
                            selectedStaff.getAllPropertiesForUpdate());
                }catch(SQLException e){
                    new AlertBuilder()
                            .alertType(Alert.AlertType.ERROR)
                            .alertTitle(Constant.DatabaseError.databaseErrorAlertTitle)
                            .alertContentText(Constant.DatabaseError.databaseUpdateError + e.toString())
                            .build()
                            .showAndWait();
                }finally{
                    showStaffDetail(selectedStaff);
                }
            }
        }
        else{
            new AlertBuilder()
                    .alertType(Alert.AlertType.WARNING)
                    .alertTitle("No Staff Selected\n")
                    .alertContentText("Please select a person in the table.")
                    .build()
                    .showAndWait();
        }
    }

    private DBExecuteStaff dbExecute;
    public StaffOverviewController(){
        dbExecute = new DBExecuteStaff();
    }

    @Override
    public void loadDataFromDB() {
        Task<List<Staff>> staffListTask = new Task<List<Staff>>(){
            @Override
            protected List<Staff> call() throws Exception {
                List<Staff> tmpStaffList = new ArrayList<>();
                for(int i = 0; i < 1; i++){
                    tmpStaffList = dbExecute.selectFromDatabase(DBQueries.SelectQueries.Staff.SELECT_ALL_STAFF);
                    updateProgress(i+1, 1);
                }
                return tmpStaffList;
            }
        };
        progressBar.progressProperty().bind(staffListTask.progressProperty());
        staffListTask.setOnFailed(event -> {
            new AlertBuilder()
                    .alertType(Alert.AlertType.ERROR)
                    .alertContentText(Constant.DatabaseError.databaseReturnError + event.toString())
                    .alertHeaderText(Constant.DatabaseError.databaseErrorAlertTitle)
                    .build()
                    .showAndWait();
        });
        staffListTask.setOnSucceeded(event -> {
            staffList = FXCollections.observableArrayList(staffListTask.getValue());
            staffTable.setItems(staffList);
            staffTable.getSelectionModel().selectFirst();
            FilteredList<Staff> filteredData = new FilteredList<Staff>(staffList,p->true);
            filterField.textProperty().addListener((observable,oldVal,newVal)->{
                filteredData.setPredicate(staff -> {
                    if (newVal == null || newVal.isEmpty()){
                        return true;
                    }
                    String lowerCase = newVal.toLowerCase();
                    if (staff.getFullName().toLowerCase().contains(lowerCase)){
                        return true;
                    }else if (staff.getPosition().name().toLowerCase().contains(lowerCase)){
                        return true;
                    }
                    return false;
                });
                staffTable.setItems(filteredData);
            });
        });
        executor.execute(staffListTask);
    }

    @Override
    public void setMainClass(SaleSystem saleSystem) {
        this.saleSystem = saleSystem;
        loadDataFromDB();
    }

    @Override
    public void setDialogStage(Stage stage){
        this.dialogStage = stage;
    }

    public void showStaffDetail(Staff staff){
        if(staff != null){
            staffIdLabel.setText(String.valueOf(staff.getStaffId()));
            nameLabel.setText(staff.getFullName());
            positionLabel.setText(staff.getPosition().name());
            streetLabel.setText(staff.getStreet());
            cityLabel.setText(staff.getCity());
            postalCodeLabel.setText(staff.getPostalCode());
        }
        else{
            staffIdLabel.setText("");
            nameLabel.setText("");
            positionLabel.setText("");
            streetLabel.setText("");
            cityLabel.setText("");
            postalCodeLabel.setText("");
        }
    }
}
