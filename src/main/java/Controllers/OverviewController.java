package Controllers;

import MainClass.SaleSystem;
import javafx.stage.Stage;

/**
 * Created by jiawei.liu on 12/2/15.
 */
public interface OverviewController {

    void loadDataFromDB();
    void setMainClass(SaleSystem saleSystem);
    void setDialogStage(Stage stage);
}
