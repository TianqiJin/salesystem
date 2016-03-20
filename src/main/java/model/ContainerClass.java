package model;

import java.util.List;

/**
 * Created by jiawei.liu on 3/19/16.
 */
public class ContainerClass {
    private List<Customer> customers;
    private Transaction transaction;

    public ContainerClass(Transaction transaction, List<Customer> customers){
        this.transaction = transaction;
        this.customers = customers;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }
}
