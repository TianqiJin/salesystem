package Controllers;


import MainClass.SaleSystem;
import db.DBExecuteStaff;
import db.DBQueries;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Staff;

public class StaffOverviewController implements OverviewController{

    private SaleSystem saleSystem;
    private ObservableList<Staff> staffList;


    @FXML
    private TableView<Staff> staffTable;
    @FXML
    private TableColumn<Staff, String> nameCol;
    @FXML
    private TableColumn<Staff, String> positionCol;
    @FXML
    private TableColumn<Staff, String> locationCol;
    @FXML
    private Label staffIdLabel;
    @FXML
    private Label nameLabel;
    @FXML
    private Label positionLabel;
    @FXML
    private Label locationLabel;


    @FXML
    private void initialize(){
        nameCol.setCellValueFactory(new PropertyValueFactory<>("Name"));
        positionCol.setCellValueFactory(new PropertyValueFactory<>("Position"));
        locationCol.setCellValueFactory(new PropertyValueFactory<>("Location"));
        showProductDetail(null);
        staffTable.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<Staff>() {
                    @Override
                    public void changed(ObservableValue<? extends Staff> observable, Staff oldValue, Staff newValue) {
                        showProductDetail(newValue);
                    }
                }
        );
    }

    @FXML
    private void handleDeleteStaff(){
        int selectedIndex = staffTable.getSelectionModel().getSelectedIndex();
        staffTable.getItems().remove(selectedIndex);
        //TODO: delete the corresponding info in the database
        //TODO: add if-else block for selectedIndex = -1 situation
    }

    private DBExecuteStaff dbExecute;
    public StaffOverviewController(){
        dbExecute = new DBExecuteStaff();
    }

    @Override
    public void loadDataFromDB() {
        staffList = FXCollections.observableArrayList(
                dbExecute.selectFromDatabase(DBQueries.SelectQueries.Staff.SELECT_ALL_STAFF)
        );
        staffTable.setItems(staffList);
    }

    @Override
    public void setMainClass(SaleSystem saleSystem) {
        this.saleSystem = saleSystem;
    }


    public void showProductDetail(Staff staff){
        if(staff != null){
            staffIdLabel.setText(String.valueOf(staff.getStaffId()));
            nameLabel.setText(staff.getFullName());
            positionLabel.setText(staff.getPosition().name());
            locationLabel.setText(staff.getLocation().name());
        }
        else{
            staffIdLabel.setText("");
            nameLabel.setText("");
            positionLabel.setText("");
            locationLabel.setText("");
        }
    }
}
