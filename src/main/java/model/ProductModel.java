package model;

import java.util.*;

import com.sun.org.apache.bcel.internal.generic.Select;
import db.*;
import org.apache.log4j.Logger;

/**
 * Created by tjin on 2015-11-22.
 */
public class ProductModel {
    public static Logger logger= Logger.getLogger(ProductModel.class);

    private static HashMap<String, Object> productInfo;
    private static Integer productID;
    private static String texture;
    private static String size;
    private static Integer totalPieces;
    private static DBExecuteProduct dbOperation;

    public ProductModel(){
        productInfo = new HashMap<String, Object>();
        dbOperation = new DBExecuteProduct();
    }
    public void loadAllProductFromDB(){
        List<Map<String, Object>> dbProductInfo = dbOperation.selectFromDatabase(DBQueries.SelectQueries.Product.SELECT_ALL_PRODUCT);
        //Notify the Interface
    }

}
