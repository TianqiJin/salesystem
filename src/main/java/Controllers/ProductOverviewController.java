package Controllers;

import db.DBExecuteCustomer;
import db.DBExecuteProduct;
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
import model.Customer;
import model.Product;


public class ProductOverviewController implements OverviewController{
    @FXML
    private TableView<Product> productTable;
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
    private void initialize(){
        productIdCol.setCellValueFactory(new PropertyValueFactory<Product, Integer>("ProductId"));
        totalNumCol.setCellValueFactory(new PropertyValueFactory<Product, Integer>("totalNum"));
        showProductDetail(null);
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
        productTable.getItems().remove(selectedIndex);
        //TODO: delete the corresponding info in the database
        //TODO: add if-else block for selectedIndex = -1 situation
    }

    private DBExecuteProduct dbExecute;
    public ProductOverviewController(){
        dbExecute = new DBExecuteProduct();
    }
    public void loadDataFromDB(){
        ObservableList<Product> productList = FXCollections.observableArrayList(
                dbExecute.selectFromDatabase(DBQueries.SelectQueries.Product.SELECT_ALL_PRODUCT)
        );
        productTable.setItems(productList);
    }
    public void showProductDetail(Product product){
        if(product != null){
            productIdLabel.setText(String.valueOf(product.getProductId()));
            textualLabel.setText(product.getTexture());
            sizeLabel.setText(product.getSize());
            totalNumLabel.setText(String.valueOf(product.getTotalNum()));
        }
        else{
            productIdLabel.setText("");
            textualLabel.setText("");
            sizeLabel.setText("");
            totalNumLabel.setText("");
        }
    }

}
