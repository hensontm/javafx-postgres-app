package org.example.demo;

public class TransactionDetail {
    //Attributes
    private String namaMenu;
    private int jumlah;
    private String kustomisasi;

    //Constructor
    public TransactionDetail(String namaMenu, int jumlah, String kustomisasi) {
        this.namaMenu = namaMenu;
        this.jumlah = jumlah;
        this.kustomisasi = kustomisasi;
    }

    //GS
    public String getNamaMenu() { return namaMenu; }
    public int getJumlah() { return jumlah; }
    public String getKustomisasi() { return kustomisasi; }
}