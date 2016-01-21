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
import javafx.scene.paint.Color;
import javafx.util.Callback;
import model.Product;

import java.sql.SQLException;
import java.util.Optional;


public class ProductOverviewController implements OverviewController{

    private SaleSystem saleSystem;
    private ObservableList<Product> productList;


    @FXML
    private TableView<Product> productTable;
    @FXML
    private TextField filterField;
    @FXML
    private TableColumn<Product, String> productIdCol;
    @FXML
    private TableColumn<Product, Integer> totalNumCol;
    @FXML
    private TableColumn<Product, String> sizeCol;
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
    private Label totalFeetLabel;
    @FXML
    private Label piecesPerBoxLabel;

    @FXML
    private void initialize(){
        productIdCol.setCellValueFactory(new PropertyValueFactory<Product, String>("ProductId"));
        totalNumCol.setCellValueFactory(new PropertyValueFactory<Product, Integer>("totalNum"));
        sizeCol.setCellValueFactory(new PropertyValueFactory<Product, String>("size"));
        totalNumCol.setCellFactory(new Callback<TableColumn<Product, Integer>, TableCell<Product, Integer>>() {
            @Override
            public TableCell<Product, Integer> call(TableColumn<Product, Integer> param) {
                return new TableCell<Product, Integer>(){
                    @Override
                    public void updateItem(Integer item, boolean empty){
                        super.updateItem(item, empty);
                        if(item == null || empty){
                            setText(null);
                            setStyle("");
                        }else{
                            setText(String.valueOf(item));
                            if(item < saleSystem.getProductWarnLimit()){
                                setStyle("-fx-background-color: chocolate");
                            }
                            else{
                                setStyle("");
                            }
                        }
                    }
                };
            }
        });
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
                }else if(product.getSize().contains(lowerCase)){
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
            String tempID = productTable.getItems().get(selectedIndex).getProductId();
            int temptotalNum = productTable.getItems().get(selectedIndex).getTotalNum();
            Alert alertConfirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this product?");
            Optional<ButtonType> result =  alertConfirm.showAndWait();
            boolean flag = true;
            if(result.isPresent() && result.get() == ButtonType.OK){
                try{
                    dbExecute.deleteDatabase(DBQueries.DeleteQueries.Product.DELETE_FROM_PRODUCT,
                            productTable.getItems().get(selectedIndex).getProductId());
                }catch(SQLException e){
                    e.printStackTrace();
                    flag = false;
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Delete Product Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Error when deleting product: "+tempID+" "+temptotalNum+"pieces");
                    alert.showAndWait();
                }finally{
                    if(flag){
                        productTable.getItems().remove(selectedIndex);
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Delete Product Successfully");
                        alert.setHeaderText(null);
                        alert.setContentText("Successfully deleted product: "+tempID+" "+temptotalNum+"pieces");
                        alert.showAndWait();
                    }
                }

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
        Product newProduct = new Product(new Product.ProductBuilder());
        boolean okClicked = saleSystem.showProductEditDialog(newProduct);
        if(okClicked){
            try{
                dbExecute.insertIntoDatabase(DBQueries.InsertQueries.Product.INSERT_INTO_PRODUCT,
                        newProduct.getAllProperties());
            }catch(SQLException e){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Unable To Add New Product");
                alert.setHeaderText(null);
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }finally{
                loadDataFromDB();
            }
        }
    }

    @FXML
    private void handleEditProduct(){
        Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
        if(selectedProduct != null){
            boolean onClicked = saleSystem.showProductEditDialog(selectedProduct);
            if(onClicked){
                try{
                    dbExecute.updateDatabase(DBQueries.UpdateQueries.Product.UPDATE_PRODUCT,
                            selectedProduct.getAllPropertiesForUpdate());
                }catch(SQLException e){
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Unable To Edit Product");
                    alert.setHeaderText(null);
                    alert.setContentText("Unable To Edit Product" + selectedProduct.getProductId());
                    alert.showAndWait();
                }finally{
                    showProductDetail(selectedProduct);
                }
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
        productTable.getSelectionModel().selectFirst();
        showProductDetail(productTable.getSelectionModel().getSelectedItem());
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
            totalFeetLabel.setText(String.valueOf(product.getTotalFeet()));
            piecesPerBoxLabel.setText(String.valueOf(product.getPiecePerBox()));
        }
        else{
            productIdLabel.setText("");
            textualLabel.setText("");
            sizeLabel.setText("");
            totalNumLabel.setText("");
            unitPriceLabel.setText("");
            totalFeetLabel.setText("");
            piecesPerBoxLabel.setText("");
        }
    }

}
