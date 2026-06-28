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
        //Connect nilai ke kolom table view
        this.col1.setCellValueFactory(new PropertyValueFactory("data"));
        this.col2.setCellValueFactory(new PropertyValueFactory("hasil"));
    }

    public void riwayatTransaksi() {
        // Alasan Analisis: Mengetahui preferensi belanja real-time guna menyusun personalisasi program loyalitas konsumen
        this.judulReport.setText("Riwayat Transaksi Customer");

        // JOIN Menggabungkan entitas data profil pelanggan customer dengan histori transaksi nota penjualan
        String sql = "SELECT c.nama_cust, 'Rp ' || co.total_bayar || ' | ' || co.tanggal_order AS detail " +
                "FROM public.customer c JOIN public.customer_order co ON c.id_cust = co.id_cust " +
                "ORDER BY co.tanggal_order DESC";

        this.tampil(sql, "nama_cust", "detail");
    }

    public void transaksiCustomer() {
        // Alasan Analisis: Mendeteksi tingkat keaktifan pelanggan tetap untuk pemberian reward voucer promosi kafe
        this.judulReport.setText("Jumlah Transaksi Customer");

        // JOIN Menghubungkan relasi data antara tabel customer dan riwayat pesanan customer_order
        // AGGREGATION Melakukan komputasi perhitungan frekuensi belanja per pembeli via fungsi agregat COUNT()
        String sql = "SELECT c.nama_cust, COUNT(co.id_order)::text AS jumlah " +
                "FROM public.customer c JOIN public.customer_order co ON c.id_cust = co.id_cust " +
                "GROUP BY c.nama_cust " +
                "HAVING COUNT(co.id_order) > 1 " +
                "ORDER BY jumlah DESC";

        this.tampil(sql, "nama_cust", "jumlah");
    }

    public void pendapatanCabang() {
        // Alasan Analisis: Mengukur efisiensi bisnis finansial dan performa pencapaian target profit penjualan omset daerah
        this.judulReport.setText("Pendapatan Cabang");

        // JOIN Mengintegrasikan data master wilayah cabang toko dengan akumulasi keuangan dari nota pesanan aktif
        // AGGREGATION Menjumlahkan total nominal dana bersih masuk dari pembeli menggunakan fungsi statistik SUM()
        String sql = "SELECT b.nama_branch, 'Rp ' || SUM(co.total_bayar) AS pendapatan " +
                "FROM public.branch b JOIN public.customer_order co ON b.id_branch = co.id_branch " +
                "GROUP BY b.nama_branch";

        this.tampil(sql, "nama_branch", "pendapatan");
    }

    public void menuPopuler() {
        // Alasan Analisis: Menentukan menu andalan paling disukai sebagai landasan utama penyusunan strategi manajemen stok inventaris
        this.judulReport.setText("Menu Paling Populer");

        // JOIN Mengkolaborasikan data katalog menu kuliner dengan kuantitas item terjual dari order_detail
        // AGGREGATION Menghitung total volume akumulasi kuantitas produk laku terjual via fungsi agregat SUM()
        String sql = "SELECT m.nama_menu, SUM(od.jumlah_detail)::text AS total " +
                "FROM public.menu m JOIN public.order_detail od ON m.id_menu = od.id_menu " +
                "GROUP BY m.nama_menu " +
                "ORDER BY total DESC LIMIT 5";

        this.tampil(sql, "nama_menu", "total");
    }

    public void jamSibuk() {
        // Alasan Analisis: Mengoptimalkan pengaturan alokasi jam kerja shift barista serta mempercepat efisiensi durasi pelayanan
        this.judulReport.setText("Jam Transaksi Tertinggi");

        // AGGREGATION Menghitung kuantitas intensitas nota pesanan masuk per jam operasional kafe lewat COUNT()
        // SUBQUERRY Menggunakan evaluasi nested subquery untuk mengelompokkan ekstraksi jam dari field tipe TIME secara valid
        String sql = "SELECT jam, COUNT(id_order)::text AS jumlah FROM (" +
                "SELECT EXTRACT(HOUR FROM waktu_order) || ':00' AS jam, id_order FROM public.customer_order) AS sub " +
                "GROUP BY jam ORDER BY jumlah DESC";

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

            // CONNECT Mengaitkan data koleksi laporan bisnis ke komponen antar-muka JavaFX TableView
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