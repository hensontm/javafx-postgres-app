package org.example.demo;

public class Customization {
    //Attributes
    private int idCustomization;
    private String namaCustomization;
    private double hargaCustomization;

    //Constructor
    public Customization(int idCustomization, String namaCustomization, double hargaCustomization) {
        this.idCustomization = idCustomization;
        this.namaCustomization = namaCustomization;
        this.hargaCustomization = hargaCustomization;
    }

    //GS
    public int getIdCustomization() { return idCustomization; }
    public void setIdCustomization(int idCustomization) { this.idCustomization = idCustomization; }

    public String getNamaCustomization() { return namaCustomization; }
    public void setNamaCustomization(String namaCustomization) { this.namaCustomization = namaCustomization; }

    public double getHargaCustomization() { return hargaCustomization; }
    public void setHargaCustomization(double hargaCustomization) { this.hargaCustomization = hargaCustomization; }
}