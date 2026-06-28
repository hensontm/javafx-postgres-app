package org.example.demo;

public class Customer {
    //Attributes
    private int idCust;
    private String namaCust;
    private String telpCust;
    private String emailCust;

    //Constructor
    public Customer(int idCust, String namaCust, String telpCust, String emailCust) {
        this.idCust = idCust;
        this.namaCust = namaCust;
        this.telpCust = telpCust;
        this.emailCust = emailCust;
    }

    //GS
    public int getIdCust() { return idCust; }
    public void setIdCust(int idCust) { this.idCust = idCust; }

    public String getNamaCust() { return namaCust; }
    public void setNamaCust(String namaCust) { this.namaCust = namaCust; }

    public String getTelpCust() { return telpCust; }
    public void setTelpCust(String telpCust) { this.telpCust = telpCust; }

    public String getEmailCust() { return emailCust; }
    public void setEmailCust(String emailCust) { this.emailCust = emailCust; }
}