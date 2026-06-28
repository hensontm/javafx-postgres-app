package org.example.demo;

import java.sql.Date;
import java.sql.Time;

public class TransactionMaster {
    //Attributes
    private int idOrder;
    private Date tanggalOrder;
    private Time waktuOrder;
    private String namaCust;
    private String namaBranch;
    private double totalBayar;
    private String statusOrder;

    //Constructor
    public TransactionMaster(int idOrder, Date tanggalOrder, Time waktuOrder, String namaCust, String namaBranch, double totalBayar, String statusOrder) {
        this.idOrder = idOrder;
        this.tanggalOrder = tanggalOrder;
        this.waktuOrder = waktuOrder;
        this.namaCust = namaCust;
        this.namaBranch = namaBranch;
        this.totalBayar = totalBayar;
        this.statusOrder = statusOrder;
    }

    //GS
    public int getIdOrder() { return idOrder; }
    public Date getTanggalOrder() { return tanggalOrder; }
    public Time getWaktuOrder() { return waktuOrder; }
    public String getNamaCust() { return namaCust; }
    public String getNamaBranch() { return namaBranch; }
    public double getTotalBayar() { return totalBayar; }
    public String getStatusOrder() { return statusOrder; }
}