package org.example.demo;

public class Inventory {
    //Attributes
    private int id;
    private int stok;
    private int idMenu;
    private int idBranch;

    //Constructor
    public Inventory(int id, int stok, int idMenu, int idBranch) {
        this.id = id;
        this.stok = stok;
        this.idMenu = idMenu;
        this.idBranch = idBranch;
    }

    //GS
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStok() { return stok; }
    public void setStok(int stok) { this.stok = stok; }

    public int getIdMenu() { return idMenu; }
    public void setIdMenu(int idMenu) { this.idMenu = idMenu; }

    public int getIdBranch() { return idBranch; }
    public void setIdBranch(int idBranch) { this.idBranch = idBranch; }
}