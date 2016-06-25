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
            public final static String SELECT_ALL_CUSTOMER = "SELECT * FROM customer";
            public final static String SELECT_SINGLE_CUSTOMER = "SELECT * FROM customer WHERE UserName = ?";
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
            public final static String SELECT_ALL_PROPERTY = "SELECT * FROM property";
        }
    }
    public static class DeleteQueries{
        public static class Customer{
            public final static String DELETE_FROM_CUSTOMER = "DELETE FROM customer WHERE UserName = ?";
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
            public final static String INSERT_INTO_CUSTOMER = "INSERT INTO customer "
                    +"(UserName, FirstName, LastName, Street, PostalCode, City, Phone, Class, Email, StoreCredit, Company, PstNumber) "
                    +"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        }
        public static class Product{
            public final static String INSERT_INTO_PRODUCT = "INSERT INTO product "
                    +"(ProductId, Texture, TotalNum, UnitPrice, PiecesPerBox, Size, SizeNumeric) "
                    +"VALUES (?, ?, ?, ?, ?, ?, ?)";
        }
        public static class Transaction{
            public final static String INSERT_INTO_TRANSACTION = "INSERT INTO transaction "
                    +"(ProductInfo, Date, Payment, PaymentType, StoreCredit, StaffID, Type, Total, payinfo, GstTax, PstTax) "
                    +"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        }
        public static class Staff{
            public final static String INSERT_INTO_STAFF = "INSERT INTO staff "
                    +"(UserName, Password, FullName, Position, Street, City, PostalCode, Phone) "
                    +"VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        }
    }
    public static class UpdateQueries{
        public static class Customer{
            public final static String UPDATE_CUSTOMER = "UPDATE customer "
                    +"SET FirstName = ?, LastName = ?, Street = ?, PostalCode = ?, City = ?, Phone = ?, Class = ?, Email = ?, StoreCredit = ?, Company = ?, PstNumber = ? "
                    +"WHERE UserName = ? ";
            public final static String UPDATE_CUSTOMER_STORE_CREDIT = "UPDATE customer "
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
                    +"SET UserName = ?, Password = ?, FullName = ?, Position = ?, Street = ?, City = ?, PostalCode = ?, Phone = ?"
                    +"WHERE StaffID = ? ";
        }
        public static class Property{
            public final static String UPDATE_PRODUCT_WARN_LIMIT = "UPDATE property "
                    +"SET ProductWarnLimit = ?";

            public final static String UPDATE_GST_RATE = "UPDATE property "
                    +"SET GstTax = ?";

            public final static String UPDATE_PST_RATE = "UPDATE property "
                    +"SET PstTax = ?";
            public final static String UPDATE_GST_NUMBER = "UPDATE property "
                    +"SET GstNum = ?";
            public final static String UPDATE_PROPERTY = "UPDATE property "
                    +"SET ProductWarnLimit = ?, GstTax = ?, PstTax = ?, GstNum = ?, UserClass =? "
                    +"WHERE PropertyId = 1";
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
