package org.example.demo;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class ReportController {
    @FXML
    private TableView<Report> tableReport;
    @FXML
    private TableColumn<Report, String> col1;
    @FXML
    private TableColumn<Report, String> col2;
    @FXML
    private Label judulReport;

    @FXML
    public void initialize() {
        this.col1.setCellValueFactory(new PropertyValueFactory("data"));
        this.col2.setCellValueFactory(new PropertyValueFactory("hasil"));
    }

    public void riwayatTransaksi() {
        this.judulReport.setText("Riwayat Transaksi Customer");
        String sql = "SELECT\nc.nama_cust,\n'Rp ' || co.total_bayar ||\n' | ' || co.tanggal_order AS detail\nFROM Customer c\nJOIN Customer_Order co\nON c.id_cust = co.id_cust\nORDER BY co.tanggal_order DESC\n";
        this.tampil(sql, "nama_cust", "detail");
    }

    public void transaksiCustomer() {
        this.judulReport.setText("Jumlah Transaksi Customer");
        String sql = "SELECT\nc.nama_cust,\nCOUNT(co.id_order) jumlah\nFROM Customer c\nJOIN Customer_Order co\nON c.id_cust = co.id_cust\nGROUP BY c.nama_cust\nHAVING COUNT(co.id_order)>1\nORDER BY jumlah DESC\n";
        this.tampil(sql, "nama_cust", "jumlah");
    }

    public void pendapatanCabang() {
        this.judulReport.setText("Pendapatan Cabang");
        String sql = "SELECT\nb.nama_branch,\nSUM(co.total_bayar) pendapatan\nFROM Branch b\nJOIN Customer_Order co\nON b.id_branch = co.id_branch\nGROUP BY b.nama_branch\n";
        this.tampil(sql, "nama_branch", "pendapatan");
    }

    public void menuPopuler() {
        this.judulReport.setText("Menu Paling Populer");
        String sql = "SELECT\nm.nama_menu,\nSUM(od.jumlah_detail) total\nFROM Menu m\nJOIN Order_Detail od\nON m.id_menu = od.id_menu\nGROUP BY m.nama_menu\nORDER BY total DESC\nLIMIT 5\n";
        this.tampil(sql, "nama_menu", "total");
    }

    public void jamSibuk() {
        this.judulReport.setText("Jam Transaksi Tertinggi");
        String sql = "SELECT\nEXTRACT(HOUR FROM waktu_order) || ':00' jam,\nCOUNT(id_order) jumlah\nFROM Customer_Order\nGROUP BY jam\nORDER BY jumlah DESC\n";
        this.tampil(sql, "jam", "jumlah");
    }

    private void tampil(String sql, String kolom1, String kolom2) {
        ObservableList<Report> list = FXCollections.observableArrayList();

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                list.add(new Report(rs.getString(kolom1), rs.getString(kolom2)));
            }

            this.tableReport.setItems(list);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void goBack(ActionEvent event) throws IOException {
        Parent root = (Parent)FXMLLoader.load(this.getClass().getResource("hello-view.fxml"));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}