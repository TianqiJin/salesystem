package Controllers;


import MainClass.SaleSystem;
import db.DBExecuteProduct;
import db.DBQueries;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Customer;
import model.Product;

import java.util.Optional;


public class ProductOverviewController implements OverviewController{

    private SaleSystem saleSystem;
    private ObservableList<Product> productList;


    @FXML
    private TableView<Product> productTable;
    @FXML
    private TextField filterField;
    @FXML
    private TableColumn<Product, Integer> productIdCol;
    @FXML
    private TableColumn<Product, Integer> totalNumCol;
    @FXML
    private Label productIdLabel;
    @FXML
    private Label textualLabel;
    @FXML
    private Label sizeLabel;
    @FXML
    private Label totalNumLabel;
    @FXML
    private Label unitPriceLabel;

    @FXML
    private void initialize(){
        productIdCol.setCellValueFactory(new PropertyValueFactory<Product, Integer>("ProductId"));
        totalNumCol.setCellValueFactory(new PropertyValueFactory<Product, Integer>("totalNum"));
        loadDataFromDB();
        showProductDetail(null);
        FilteredList<Product> filteredData = new FilteredList<Product>(productList,p->true);
        filterField.textProperty().addListener((observable,oldVal,newVal)->{
            filteredData.setPredicate(product -> {
                if (newVal == null || newVal.isEmpty()){
                    return true;
                }
                String lowerCase = newVal.toLowerCase();
                if (String.valueOf(product.getTotalNum()).contains(lowerCase)){
                    return true;
                }else if (String.valueOf(product.getProductId()).contains(lowerCase)){
                    return true;
                }
                return false;
            });
            productTable.setItems(filteredData);
        });

        productTable.getSelectionModel().selectedItemProperty().addListener(
                new ChangeListener<Product>() {
                    @Override
                    public void changed(ObservableValue<? extends Product> observable, Product oldValue, Product newValue) {
                        showProductDetail(newValue);
                    }
                }
        );
    }

    @FXML
    private void handleDeleteProduct(){
        int selectedIndex = productTable.getSelectionModel().getSelectedIndex();
        if(selectedIndex >= 0){
            int tempID = productTable.getItems().get(selectedIndex).getProductId();
            int temptotalNum = productTable.getItems().get(selectedIndex).getTotalNum();
            Alert alertConfirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this product?");
            Optional<ButtonType> result =  alertConfirm.showAndWait();
            if(result.isPresent() && result.get() == ButtonType.OK){
                if(dbExecute.deleteDatabase(DBQueries.DeleteQueries.Product.DELETE_FROM_PRODUCT,
                        productTable.getItems().get(selectedIndex).getProductId()) == 0){
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Delete Product Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Error when deleting product: "+tempID+" "+temptotalNum+"pieces");
                    alert.showAndWait();
                }
                else{
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Delete Product Successfully");
                    alert.setHeaderText(null);
                    alert.setContentText("Successfully deteled product: "+tempID+" "+temptotalNum+"pieces");
                    alert.showAndWait();
                }
                productTable.getItems().remove(selectedIndex);
            }
        }
        else{
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Product Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select a product in the table.");
            alert.showAndWait();
        }

    }

    @FXML
    private void handleAddProduct(){
        Product newProduct = new Product();
        boolean okClicked = saleSystem.showProductEditDialog(newProduct);
        if(okClicked){
            if(dbExecute.insertIntoDatabase(DBQueries.InsertQueries.Product.INSERT_INTO_PRODUCT,
                    newProduct.getAllProperties()) == 0){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Unable To Add New Product");
                alert.setHeaderText(null);
                alert.setContentText("Unable To Add New Product");
                alert.showAndWait();
            }
            else{
                productList.add(newProduct);
            }
        }
    }

    @FXML
    private void handleEditProduct(){
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if(selectedProduct != null){
            boolean onClicked = saleSystem.showProductEditDialog(selectedProduct);
            if(dbExecute.updateDatabase(DBQueries.UpdateQueries.Product.UPDATE_PRODUCT,
                    selectedProduct.getAllPropertiesForUpdate()) == 0){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Unable To Edit Product");
                alert.setHeaderText(null);
                alert.setContentText("Unable To Edit Product" + selectedProduct.getProductId());
                alert.showAndWait();
            }
            if(onClicked){
                showProductDetail(selectedProduct);
            }
        }
        else{
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Product Selected");
            alert.setHeaderText(null);
            alert.setContentText("Please select a product in the table.");
            alert.showAndWait();
        }
    }

    private DBExecuteProduct dbExecute;
    public ProductOverviewController(){
        dbExecute = new DBExecuteProduct();
    }
    public void loadDataFromDB(){
        productList = FXCollections.observableArrayList(
                dbExecute.selectFromDatabase(DBQueries.SelectQueries.Product.SELECT_ALL_PRODUCT)
        );
        productTable.setItems(productList);
    }

    @Override
    public void setMainClass(SaleSystem saleSystem) {
        this.saleSystem = saleSystem;
    }

    public void showProductDetail(Product product){
        if(product != null){
            productIdLabel.setText(String.valueOf(product.getProductId()));
            textualLabel.setText(product.getTexture());
            sizeLabel.setText(product.getSize());
            totalNumLabel.setText(String.valueOf(product.getTotalNum()));
            unitPriceLabel.setText(String.valueOf(product.getUnitPrice()));
        }
        else{
            productIdLabel.setText("");
            textualLabel.setText("");
            sizeLabel.setText("");
            totalNumLabel.setText("");
            unitPriceLabel.setText("");
        }
    }

}
