package org.example.demo;

public class Inventory {
    //Attributes
    private int idInventory;
    private int stokInventory;
    private int idMenu;
    private int idBranch;

    //Constructor
    public Inventory(int idInventory, int stokInventory, int idMenu, int idBranch) {
        this.idInventory = idInventory;
        this.stokInventory = stokInventory;
        this.idMenu = idMenu;
        this.idBranch = idBranch;
    }

    //GS
    public int getIdInventory() { return idInventory; }
    public void setIdInventory(int idInventory) { this.idInventory = idInventory; }

    public int getStokInventory() { return stokInventory; }
    public void setStokInventory(int stokInventory) { this.stokInventory = stokInventory; }

    public int getIdMenu() { return idMenu; }
    public void setIdMenu(int idMenu) { this.idMenu = idMenu; }

    public int getIdBranch() { return idBranch; }
    public void setIdBranch(int idBranch) { this.idBranch = idBranch; }
}