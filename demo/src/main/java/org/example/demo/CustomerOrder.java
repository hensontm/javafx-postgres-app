package org.example.demo;

import java.sql.Date;
import java.sql.Time;

public class CustomerOrder {
    //Attributes
    private int idOrder;
    private Date tanggalOrder;
    private double diskonRestoran;
    private double totalOrder;
    private double totalBayar;
    private String statusOrder;
    private int idCust;
    private int idBranch;
    private int idEmployee;
    private int idMetodePayment;
    private Time waktuOrder;

    //Constructor
    public CustomerOrder(int idOrder, Date tanggalOrder, double diskonRestoran, double totalOrder,
                         double totalBayar, String statusOrder, int idCust, int idBranch,
                         int idEmployee, int idMetodePayment, Time waktuOrder) {
        this.idOrder = idOrder;
        this.tanggalOrder = tanggalOrder;
        this.diskonRestoran = diskonRestoran;
        this.totalOrder = totalOrder;
        this.totalBayar = totalBayar;
        this.statusOrder = statusOrder;
        this.idCust = idCust;
        this.idBranch = idBranch;
        this.idEmployee = idEmployee;
        this.idMetodePayment = idMetodePayment;
        this.waktuOrder = waktuOrder;
    }

    //GS
    public int getIdOrder() { return idOrder; }
    public void setIdOrder(int idOrder) { this.idOrder = idOrder; }

    public Date getTanggalOrder() { return tanggalOrder; }
    public void setTanggalOrder(Date tanggalOrder) { this.tanggalOrder = tanggalOrder; }

    public double getDiskonRestoran() { return diskonRestoran; }
    public void setDiskonRestoran(double diskonRestoran) { this.diskonRestoran = diskonRestoran; }

    public double getTotalOrder() { return totalOrder; }
    public void setTotalOrder(double totalOrder) { this.totalOrder = totalOrder; }

    public double getTotalBayar() { return totalBayar; }
    public void setTotalBayar(double totalBayar) { this.totalBayar = totalBayar; }

    public String getStatusOrder() { return statusOrder; }
    public void setStatusOrder(String statusOrder) { this.statusOrder = statusOrder; }

    public int getIdCust() { return idCust; }
    public void setIdCust(int idCust) { this.idCust = idCust; }

    public int getIdBranch() { return idBranch; }
    public void setIdBranch(int idBranch) { this.idBranch = idBranch; }

    public int getIdEmployee() { return idEmployee; }
    public void setIdEmployee(int idEmployee) { this.idEmployee = idEmployee; }

    public int getIdMetodePayment() { return idMetodePayment; }
    public void setIdMetodePayment(int idMetodePayment) { this.idMetodePayment = idMetodePayment; }

    public Time getWaktuOrder() { return waktuOrder; }
    public void setWaktuOrder(Time waktuOrder) { this.waktuOrder = waktuOrder; }
}