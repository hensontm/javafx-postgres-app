package org.example.demo;

public class Branch {
    //Attributes
    private int idBranch;
    private String namaBranch;
    private String alamatBranch;
    private String kotaBranch;
    private String kodePosBranch;

    //Constructor
    public Branch(int idBranch, String namaBranch, String alamatBranch, String kotaBranch, String kodePosBranch) {
        this.idBranch = idBranch;
        this.namaBranch = namaBranch;
        this.alamatBranch = alamatBranch;
        this.kotaBranch = kotaBranch;
        this.kodePosBranch = kodePosBranch;
    }

    //GS
    public int getIdBranch() { return idBranch; }
    public void setIdBranch(int idBranch) { this.idBranch = idBranch; }

    public String getNamaBranch() { return namaBranch; }
    public void setNamaBranch(String namaBranch) { this.namaBranch = namaBranch; }

    public String getAlamatBranch() { return alamatBranch; }
    public void setAlamatBranch(String alamatBranch) { this.alamatBranch = alamatBranch; }

    public String getKotaBranch() { return kotaBranch; }
    public void setKotaBranch(String kotaBranch) { this.kotaBranch = kotaBranch; }

    public String getKodePosBranch() { return kodePosBranch; }
    public void setKodePosBranch(String kodePosBranch) { this.kodePosBranch = kodePosBranch; }
}