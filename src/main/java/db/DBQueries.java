package db;

/**
 * Created by jiawei.liu on 11/22/15.
 */
public class DBQueries {
    public static class SelectQueries {
        public static class Product{
            public final static String SELECT_ALL_PRODUCT = "SELECT * FROM PRODUCT";
            public final static String SELECT_PRODUCTID_PROJECT = "SELECT * FROM PRODUCT WHERE ProductID = ?";
        }
        public static class Customer{
            public final static String SELECTT_ALL_CUSTOMER = "SELECT * FROM CUSTOMER";
        }
    }
    public static class InsertQueries{

    }
    public static class UpdateQueries{

    }
}
