package util;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.ProductTransaction;
import model.Transaction;

/**
 * Created by tjin on 12/27/15.
 */
public class ButtonCell extends TableCell<ProductTransaction, Boolean> {
    Image image = new Image(getClass().getResourceAsStream("/pics/delete.png"));
    Button cellButton = new Button();
    public ButtonCell(TableView<ProductTransaction>tableView){
        setAlignment(Pos.CENTER_LEFT);
        cellButton.setGraphic(new ImageView(image));
        cellButton.setPadding(Insets.EMPTY);
        cellButton.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent t) {
                int selectedIndex = getTableRow().getIndex();
                tableView.getItems().remove(selectedIndex);
            }
        });
    }
    //Display button if the row is not empty
    @Override
    protected void updateItem(Boolean t, boolean empty) {
        super.updateItem(t, empty);
        if(!empty){
            setGraphic(cellButton);
        }
        else{
            setGraphic(null);
        }
    }
}
