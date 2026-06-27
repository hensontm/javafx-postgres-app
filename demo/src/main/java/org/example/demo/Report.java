package org.example.demo;

public class Report {
    private String data;
    private String hasil;

    public Report(String data, String hasil) {
        this.data = data;
        this.hasil = hasil;
    }

    public String getData() {
        return this.data;
    }

    public String getHasil() {
        return this.hasil;
    }
}