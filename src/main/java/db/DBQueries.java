package db;

/**
 * Created by jiawei.liu on 11/22/15.
 */
public class DBQueries {
    public static class SelectQueries {
        public static class Product{
            public final static String SELECT_ALL_PRODUCT = "SELECT * FROM product";
            public final static String SELECT_PRODUCTID_PROJECT = "SELECT * FROM PRODUCT WHERE ProductID = ?";
        }
        public static class Customer{
            public final static String SELECT_ALL_CUSTOMER = "SELECT * FROM Customer";
        }
    }
    public static class DeleteQueries{
        public static class Customer{
            public final static String DELETE_FROM_CUSTOMER = "DELETE FROM Customer WHERE UserName = ?";
        }
    }
    public static class InsertQueries{
        public static class Customer{
            public final static String INSERT_INTO_CUSTOMER = "INSERT INTO Customer "
                    +"(UserName, FirstName, LastName, Street, PostalCode, City, Phone, Class, Email, StoreCredit)"
                    +"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        }
    }
    public static class UpdateQueries{
        public static class Customer{
            public final static String UPDATE_CUSTOMER = "UPDATE Customer "
                    +"SET FirstName = ?, LastName = ?, Street = ?, PostalCode = ?, City = ?, Phone = ?, Class = ?, Email = ?, StoreCredit = ? "
                    +"WHERE UserName = ? ";
        }
    }
}
