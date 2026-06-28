package org.example.demo;

public class Menu {
    //Attributes
    private int idMenu;
    private String namaMenu;
    private double hargaMenu;
    private int idCategory;

    //Constructor
    public Menu(int idMenu, String namaMenu, double hargaMenu, int idCategory) {
        this.idMenu = idMenu;
        this.namaMenu = namaMenu;
        this.hargaMenu = hargaMenu;
        this.idCategory = idCategory;
    }

    //GS
    public int getIdMenu() { return idMenu; }
    public void setIdMenu(int idMenu) { this.idMenu = idMenu; }

    public String getNamaMenu() { return namaMenu; }
    public void setNamaMenu(String namaMenu) { this.namaMenu = namaMenu; }

    public double getHargaMenu() { return hargaMenu; }
    public void setHargaMenu(double hargaMenu) { this.hargaMenu = hargaMenu; }

    public int getIdCategory() { return idCategory; }
    public void setIdCategory(int idCategory) { this.idCategory = idCategory; }
}