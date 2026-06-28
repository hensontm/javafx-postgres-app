package org.example.demo;

public class Category {
    //Attributes
    private int idCategory;
    private String namaCategory;

    //Constructor
    public Category(int idCategory, String namaCategory) {
        this.idCategory = idCategory;
        this.namaCategory = namaCategory;
    }

    //GS
    public int getIdCategory() { return idCategory; }
    public void setIdCategory(int idCategory) { this.idCategory = idCategory; }

    public String getNamaCategory() { return namaCategory; }
    public void setNamaCategory(String namaCategory) { this.namaCategory = namaCategory; }
}