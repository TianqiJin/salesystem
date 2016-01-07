package db;

/**
 * Created by jiawei.liu on 11/22/15.
 */
public class DBQueries {
    public static class SelectQueries {
        public static class Product{
            public final static String SELECT_ALL_PRODUCT = "SELECT * FROM product";
            public final static String SELECT_PRODUCTID_PROJECT = "SELECT * FROM product WHERE ProductID = ?";
        }
        public static class Customer{
            public final static String SELECT_ALL_CUSTOMER = "SELECT * FROM Customer";
        }

        public static class Transaction{
            public final static String SELECT_ALL_TRANSACTION = "SELECT * FROM transaction";
        }
        public static class Staff{
            public final static String SELECT_ALL_STAFF = "SELECT * FROM staff";
            public final static String SELECT_USERNAME_STAFF = "SELECT * FROM staff WHERE UserName = ?";
            public final static String SELECT_STAFF_MAX_NUM = "SELECT * from staff order by StaffID desc limit 1";
        }
    }
    public static class DeleteQueries{
        public static class Customer{
            public final static String DELETE_FROM_CUSTOMER = "DELETE FROM Customer WHERE UserName = ?";
        }
        public static class Product{
            public final static String DELETE_FROM_PRODUCT = "DELETE FROM product WHERE ProductID = ?";
        }
        public static class Staff{
            public final static String DELETE_FROM_STAFF = "DELETE FROM staff WHERE UserName = ?";
        }
    }
    public static class InsertQueries{
        public static class Customer{
            public final static String INSERT_INTO_CUSTOMER = "INSERT INTO Customer "
                    +"(UserName, FirstName, LastName, Street, PostalCode, City, Phone, Class, Email, StoreCredit)"
                    +"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        }
        public static class Product{
            public final static String INSERT_INTO_PRODUCT = "INSERT INTO product "
                    +"(ProductId, Texture, Size, TotalNum, UnitPrice)"
                    +"VALUES (?, ?, ?, ?, ?)";
        }
        public static class Transaction{
            public final static String INSERT_INTO_TRANSACTION = "INSERT INTO transaction "
                    +"(ProductInfo, Date, Payment, PaymentType, StaffID, Type)"
                    +"VALUES (?, ?, ?, ?, ?, ?)";
        }
        public static class Staff{
            public final static String INSERT_INTO_STAFF = "INSERT INTO staff "
                    +"(UserName, Password, FullName, Position, Location)"
                    +"VALUES (?, ?, ?, ?, ?)";
        }
    }
    public static class UpdateQueries{
        public static class Customer{
            public final static String UPDATE_CUSTOMER = "UPDATE Customer "
                    +"SET FirstName = ?, LastName = ?, Street = ?, PostalCode = ?, City = ?, Phone = ?, Class = ?, Email = ?, StoreCredit = ? "
                    +"WHERE UserName = ? ";
            public final static String UPDATE_CUSTOMER_STORE_CREDIT = "UPDATE Customer "
                    +"SET StoreCredit = ? "
                    +"WHERE UserName = ?";
        }
        public static class Product{
            public final static String UPDATE_PRODUCT = "UPDATE product "
                    +"SET Texture = ?, Size = ?, TotalNum = ?, UnitPrice = ? "
                    +"WHERE ProductID = ? ";
            public final static String UPDATE_PRODUCT_QUANTITY = "UPDATE product "
                    +"SET TotalNum = ? "
                    +"WHERE ProductID = ?";
        }
        public static class Staff{
            public final static String UPDATE_STAFF = "UPDATE staff "
                    +"SET UserName = ?, Password = ?, FullName = ?, Position = ?, Location = ?"
                    +"WHERE StaffID = ? ";
        }
    }

    public static class SearchQueries{
        public static class Staff{
            public final static String AUTHENTICATION = "SELECT Password,Position from staff WHERE UserName = ?";

        }

    }
}
