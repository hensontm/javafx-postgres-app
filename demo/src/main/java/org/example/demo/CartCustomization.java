package org.example.demo;

public class CartCustomization {
    //Attributes
    private int customId;
    private String name;
    private int qty;
    private double totalPrice;

    //Constructor
    public CartCustomization(int customId, String name, int qty, double totalPrice) {
        this.customId = customId;
        this.name = name;
        this.qty = qty;
        this.totalPrice = totalPrice;
    }

    //GS
    public int getCustomId() { return customId; }
    public String getName() { return name; }
    public int getQty() { return qty; }
    public double getTotalPrice() { return totalPrice; }
    public void setQty(int qty) { this.qty = qty; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
}