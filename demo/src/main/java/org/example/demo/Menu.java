package org.example.demo;

public class Menu {
    //Attributes
    private int id;
    private String name;
    private double price;
    private int idCategory;

    //Constructor
    public Menu(int id, String name, double price, int idCategory) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.idCategory = idCategory;
    }

    //GS
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getIdCategory() { return idCategory; }
    public void setIdCategory(int idCategory) { this.idCategory = idCategory; }
}