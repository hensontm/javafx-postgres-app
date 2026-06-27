package org.example.demo;

public class Payment {
    //Attributes
    private int id;
    private String method;
    private double discount;

    //Constructor
    public Payment(int id, String method, double discount) {
        this.id = id;
        this.method = method;
        this.discount = discount;
    }

    //GS
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }
}