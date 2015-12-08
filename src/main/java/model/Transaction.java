package model;


import org.apache.log4j.Logger;

/**
 * Created by jiawei.liu on 12/2/15.
 */
public class Transaction {
    private static Logger logger = Logger.getLogger(Transaction.class);
    private int transactionId;
    private int productId;
    private int numofBoxes;
    private int numofPieces;
    private String date;
    private double payment;
    private String paymentType;
    private int staffId;
    private TransactionType type;
    private String info;


    public Transaction (Object... params){
        this.transactionId = (Integer)params[0];
        this.productId = (Integer)params[1];
        this.numofBoxes = (Integer)params[2];
        this.numofPieces = (Integer)params[3];
        this.date = (String)params[4];
        this.payment = (double)params[5];
        this.paymentType = (String)params[6];
        this.staffId = (Integer)params[7];
        this.type = (TransactionType)params[8];
        this.info = (String)params[9];
    }

    public static class TranscationBuilder{
        private int transactionId;
        private int productId;
        private int numofBoxes;
        private int numofPieces;
        private String date;
        private double payment;
        private String paymentType;
        private int staffId;
        private TransactionType type;
        private String info;

        public TranscationBuilder transcationId(int transcationId){
            this.transactionId = transcationId;
            return this;
        }

        public TranscationBuilder productId(int productId){
            this.productId = productId;
            return this;
        }

        public TranscationBuilder numofBoxes(int numofBoxes){
            this.numofBoxes = numofBoxes;
            return this;
        }

        public TranscationBuilder numofPieces(int numofPieces){
            this.numofPieces = numofPieces;
            return this;
        }

        public TranscationBuilder date(String date){
            this.date = date;
            return this;
        }

        public TranscationBuilder payment(double payment){
            this.payment = payment;
            return this;
        }

        public TranscationBuilder paymentType(String paymentType){
            this.paymentType = paymentType;
            return this;
        }

        public TranscationBuilder staffId(int staffId){
            this.staffId = staffId;
            return this;
        }

        public TranscationBuilder type(TransactionType type){
            this.type = type;
            return this;
        }

        public TranscationBuilder info(String info){
            this.info = info;
            return this;
        }

        public Transaction build(){
            return new Transaction(transactionId,productId,numofBoxes,numofPieces,date,payment,paymentType,staffId,type,info);
        }


    }


    public String getInfo() {
        return info;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public int getProductId() {
        return productId;
    }

    public int getNumofBoxes() {
        return numofBoxes;
    }

    public int getNumofPieces() {
        return numofPieces;
    }

    public String getDate() {
        return date;
    }

    public double getPayment() {
        return payment;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public int getStaffId() {
        return staffId;
    }

    public TransactionType getType() {
        return type;
    }


    public enum TransactionType{
       IN("in"),OUT("out"),RETURN("return");
        private String type;
        private TransactionType(String type){
            this.type = type;
        }
        public static TransactionType getType(String type){
            for (TransactionType ttype : TransactionType.values()){
                if (ttype.name().equalsIgnoreCase(type)){
                    return ttype;
                }
            }
            return null;
        }
    }

}
