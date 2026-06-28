package org.example.demo;

import java.util.ArrayList;
import java.util.List;

public class CartItem {
    //Attributes
    private int menuId;
    private String menuName;
    private int qty;
    private double price;
    private List<CartCustomization> customizations = new ArrayList<>();

    //Constructor
    public CartItem(int menuId, String menuName, int qty, double price) {
        this.menuId = menuId;
        this.menuName = menuName;
        this.qty = qty;
        this.price = price;
    }

    public void addCustomization(CartCustomization custom) {
        this.customizations.add(custom);
    }

    //GS
    public int getMenuId() { return menuId; }
    public String getMenuName() { return menuName; }
    public int getQty() { return qty; }
    public double getPrice() { return price; }
    public List<CartCustomization> getCustomizations() { return customizations; }

    //UI Tableview Jumlah Customization
    public String getCustomizationSummary() {
        if (customizations.isEmpty()) return "-";
        StringBuilder sb = new StringBuilder();
        for (CartCustomization c : customizations) {
            sb.append(c.getName()).append(" (x").append(c.getQty()).append("), ");
        }
        return sb.substring(0, sb.length() - 2);
    }

    //Hitung TotalPrice
    public double getTotalPrice() {
        double totalCustom = 0;
        for (CartCustomization c : customizations) {
            totalCustom += c.getTotalPrice();
        }
        return (this.price * this.qty) + totalCustom;
    }
}