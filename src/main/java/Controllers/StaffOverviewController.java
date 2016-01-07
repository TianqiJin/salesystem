package Controllers;


import MainClass.SaleSystem;
import db.DBExecuteStaff;
import db.DBQueries;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Staff;

import java.util.Optional;

public class StaffOverviewController implements OverviewController{

    private SaleSystem saleSystem;
    private ObservableList<Staff> staffList;


    @FXML
    private TableView<Staff> staffTable;
    @FXML
    private TextField filterField;
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
        nameCol.setCellValueFactory(new PropertyValueFactory<>("FullName"));
        positionCol.setCellValueFactory(new PropertyValueFactory<>("Position"));
        locationCol.setCellValueFactory(new PropertyValueFactory<>("Location"));
        loadDataFromDB();
        showStaffDetail(null);
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
                }else if (staff.getLocation().name().toLowerCase().contains(lowerCase)){
                    return true;
                }
                return false;
            });
            staffTable.setItems(filteredData);
        });
        staffTable.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<Staff>() {
                    @Override
                    public void changed(ObservableValue<? extends Staff> observable, Staff oldValue, Staff newValue) {
                        showStaffDetail(newValue);
                    }
                }
        );
    }

    @FXML
    private void handleDeleteStaff(){
        int selectedIndex = staffTable.getSelectionModel().getSelectedIndex();
        if(selectedIndex >= 0){
            String tempName = staffTable.getItems().get(selectedIndex).getFullName();
            String tempPosition = staffTable.getItems().get(selectedIndex).getPosition().name();
            Alert alertConfirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this Staff?");
            Optional<ButtonType> result =  alertConfirm.showAndWait();
            if(result.isPresent() && result.get() == ButtonType.OK){
                if(dbExecute.deleteDatabase(DBQueries.DeleteQueries.Staff.DELETE_FROM_STAFF,
                        staffTable.getItems().get(selectedIndex).getUserName()) == 0){
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Delete Staff Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Error when deleting staff "+tempName+" "+tempPosition);
                    alert.showAndWait();
                }
                else{
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Delete Staff Successfully");
                    alert.setHeaderText(null);
                    alert.setContentText("Successfully deleted staff "+tempName+" "+tempPosition);
                    alert.showAndWait();
                }
                staffTable.getItems().remove(selectedIndex);
            }
        }
        else{
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Staff Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select a person in the table.");
            alert.showAndWait();
        }

    }

    @FXML
    private void handleAddStaff(){
        Staff newStaff = new Staff();
        newStaff.setStaffId(dbExecute.getMaxNum(DBQueries.SelectQueries.Staff.SELECT_STAFF_MAX_NUM));
        boolean okClicked = saleSystem.showStaffEditDialog(newStaff);
        if(okClicked){
            if(dbExecute.insertIntoDatabase(DBQueries.InsertQueries.Staff.INSERT_INTO_STAFF,
                    newStaff.getAllProperties()) == 0){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Unable To Add New Staff");
                alert.setHeaderText(null);
                alert.setContentText("Unable To Add New Staff" + newStaff.getUserName());
                alert.showAndWait();
            }
            else{
                staffList.add(newStaff);
            }
        }
    }

    @FXML
    private void handleEditStaff(){
        Staff selectedStaff = staffTable.getSelectionModel().getSelectedItem();
        if(selectedStaff != null){
            boolean onClicked = saleSystem.showStaffEditDialog(selectedStaff);
            if(dbExecute.updateDatabase(DBQueries.UpdateQueries.Staff.UPDATE_STAFF,
                    selectedStaff.getAllPropertiesForUpdate()) == 0){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Unable To Edit Staff");
                alert.setHeaderText(null);
                alert.setContentText("Unable To Edit Staff" + selectedStaff.getFullName());
                alert.showAndWait();
            }
            if(onClicked){
                showStaffDetail(selectedStaff);
            }
        }
        else{
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Staff Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select a person in the table.");
            alert.showAndWait();
        }
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


    public void showStaffDetail(Staff staff){
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
