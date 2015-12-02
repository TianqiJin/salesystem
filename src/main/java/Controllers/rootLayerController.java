package Controllers;

import db.DBExecuteCustomer;
import db.DBQueries;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Customer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class rootLayerController {
    @FXML
    private TabPane tabPane;

    private FXMLLoader fXMLLoader = new FXMLLoader();

    private String[] tabList = {"Customer", "Product"};

    private Map<String, Object> tabControllerMap = new HashMap<String, Object>();

    @FXML
    private void init(){
        for (String tab : tabList) {
            tabPane.getTabs().add(new Tab(tab));
        }

        tabPane.getSelectionModel().clearSelection();
        tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
            @Override
            public void changed(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
                if (newValue.getContent() == null) {
                    try {
                        Parent root = (Parent) fXMLLoader.load(this.getClass().getResource(newValue.getText() + "Overview.fxml").openStream());
                        newValue.setContent(root);
                        OverviewController controller = fXMLLoader.getController();
                        controller.loadDataFromDB();
                        tabControllerMap.put(newValue.getText(), fXMLLoader.getController());

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    Parent root = (Parent) newValue.getContent();
                    // Optionally get the controller from Map and manipulate the content
                    // via its controller.
                }
            }
        });
        tabPane.getSelectionModel().selectFirst();

    }

}
