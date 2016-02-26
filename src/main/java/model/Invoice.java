package model;


import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
/**
 * Created by jiawei.liu on 2/4/16.
 */
public class Invoice {
    protected int id;
    protected Customer customer;
    protected Transaction transaction;
    protected double total;
    protected List<ProductTransaction> products;
    protected Date invoiceDate;

    public Invoice(Transaction transaction, Customer customer){
        this.transaction = transaction;
        this.id = transaction.getTransactionId();
        this.invoiceDate = Date.from(transaction.getDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        this.total = transaction.getPayment();
        this.products = new ArrayList<>();
        for(ProductTransaction pt : transaction.getProductTransactionList()){
            products.add(pt);
        }
        this.customer = customer;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public List<ProductTransaction> getProducts() {
        return products;
    }

    public void setProducts(List<ProductTransaction> products) {
        this.products = products;
    }

    public Date getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(Date invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Invoice id: ").append(id).append(" Date: ").append(invoiceDate).append(" Total cost: ").append(total).append("\u20ac\n");
        sb.append("Customer: ").append(customer.toString()).append("\n");
        for (ProductTransaction product : products) {
            sb.append(product.toString()).append("\n");
        }
        return sb.toString();
    }
}
