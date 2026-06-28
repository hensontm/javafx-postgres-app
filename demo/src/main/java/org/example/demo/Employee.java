package org.example.demo;

import java.sql.Date;

public class Employee {
    //Attributes
    private int id;
    private String name;
    private String position;
    private double salary;
    private Date birthDate;
    private String address;
    private int idBranch;
    private String phone;

    //Constructor
    public Employee(int id, String name, String position, double salary, Date birthDate, String address, int idBranch, String phone) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.salary = salary;
        this.birthDate = birthDate;
        this.address = address;
        this.idBranch = idBranch;
        this.phone = phone;
    }

    //GS
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }

    public Date getBirthDate() { return birthDate; }
    public void setBirthDate(Date birthDate) { this.birthDate = birthDate; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public int getIdBranch() { return idBranch; }
    public void setIdBranch(int idBranch) { this.idBranch = idBranch; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}