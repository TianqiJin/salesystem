package db;

/**
 * Created by jiawei.liu on 11/22/15.
 */
public class DBQueries {
    public static class SelectQueries {
        public static class Product{
            public final static String SELECT_ALL_PRODUCT = "SELECT * FROM product";
            public final static String SELECT_PRODUCTID_PROJECT = "SELECT * FROM product WHERE ProductId = ?";
        }
        public static class Customer{
            public final static String SELECT_ALL_CUSTOMER = "SELECT * FROM Customer";
            public final static String SELECT_SINGLE_CUSTOMER = "SELECT * FROM Customer WHERE UserName = ?";
        }

        public static class Transaction{
            public final static String SELECT_ALL_TRANSACTION = "SELECT * FROM transaction";
            public final static String SELECT_ALL_TRANSACTION_FOR_REPORT = "SELECT * FROM transaction WHERE Date BETWEEN ? AND ?";
        }
        public static class Staff{
            public final static String SELECT_ALL_STAFF = "SELECT * FROM staff";
            public final static String SELECT_USERNAME_STAFF = "SELECT * FROM staff WHERE UserName = ?";
            public final static String SELECT_STAFF_MAX_NUM = "SELECT * from staff order by StaffID desc limit 1";
        }
        public static class Property{
            public final static String SELECT_ALL_PROPERTY = "SELECT * FROM Property";
        }
    }
    public static class DeleteQueries{
        public static class Customer{
            public final static String DELETE_FROM_CUSTOMER = "DELETE FROM Customer WHERE UserName = ?";
        }
        public static class Product{
            public final static String DELETE_FROM_PRODUCT = "DELETE FROM product WHERE ProductId = ?";
        }
        public static class Staff{
            public final static String DELETE_FROM_STAFF = "DELETE FROM staff WHERE UserName = ?";
        }
        public static class Transaction{
            public final static String DELETE_FROM_TRANSACTION = "DELETE FROM transaction WHERE transactionId = ?";
        }
    }
    public static class InsertQueries{
        public static class Customer{
            public final static String INSERT_INTO_CUSTOMER = "INSERT INTO Customer "
                    +"(UserName, FirstName, LastName, Street, PostalCode, City, Phone, Class, Email, StoreCredit, Company) "
                    +"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        }
        public static class Product{
            public final static String INSERT_INTO_PRODUCT = "INSERT INTO product "
                    +"(ProductId, Texture, TotalNum, UnitPrice, PiecesPerBox, Size, SizeNumeric) "
                    +"VALUES (?, ?, ?, ?, ?, ?, ?)";
        }
        public static class Transaction{
            public final static String INSERT_INTO_TRANSACTION = "INSERT INTO transaction "
                    +"(ProductInfo, Date, Payment, PaymentType, StoreCredit, StaffID, Type, Total, payinfo) "
                    +"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        }
        public static class Staff{
            public final static String INSERT_INTO_STAFF = "INSERT INTO staff "
                    +"(UserName, Password, FullName, Position, Street, City, PostalCode) "
                    +"VALUES (?, ?, ?, ?, ?, ?, ?)";
        }
    }
    public static class UpdateQueries{
        public static class Customer{
            public final static String UPDATE_CUSTOMER = "UPDATE Customer "
                    +"SET FirstName = ?, LastName = ?, Street = ?, PostalCode = ?, City = ?, Phone = ?, Class = ?, Email = ?, StoreCredit = ?, Company = ? "
                    +"WHERE UserName = ? ";
            public final static String UPDATE_CUSTOMER_STORE_CREDIT = "UPDATE Customer "
                    +"SET StoreCredit = ? "
                    +"WHERE UserName = ?";
        }
        public static class Product{
            public final static String UPDATE_PRODUCT = "UPDATE product "
                    +"SET Texture = ?, UnitPrice = ?, PiecesPerBox = ?, Size = ?, SizeNumeric = ? "
                    +"WHERE ProductId = ? ";
            public final static String UPDATE_PRODUCT_QUANTITY = "UPDATE product "
                    +"SET TotalNum = ? "
                    +"WHERE ProductId = ?";
        }
        public static class Staff{
            public final static String UPDATE_STAFF = "UPDATE staff "
                    +"SET UserName = ?, Password = ?, FullName = ?, Position = ?, Street = ?, City = ?, PostalCode = ? "
                    +"WHERE StaffID = ? ";
        }
        public static class Property{
            public final static String UPDATE_PRODUCT_WARN_LIMIT = "UPDATE Property "
                    +"SET ProductWarnLimit = ?";

            public final static String UPDATE_TAX_RATE = "UPDATE Property "
                    +"SET TaxRate = ?";
        }
        public static class Transaction{
            public final static String UPDATE_TRANSACTION_OUT = "UPDATE transaction "
                    +"SET Date = ?, Payment = ?, PaymentType = ?, StoreCredit = ?, payinfo = ? "
                    +"WHERE TransactionID = ?";
        }
    }

    public static class SearchQueries{
        public static class Staff{
            public final static String AUTHENTICATION = "SELECT Password,Position from staff WHERE UserName = ?";

        }

    }
}
