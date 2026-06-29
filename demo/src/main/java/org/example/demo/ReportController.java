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
        this.col1.setCellValueFactory(new PropertyValueFactory<>("data"));
        this.col2.setCellValueFactory(new PropertyValueFactory<>("hasil"));
    }

    @FXML
    public void transaksiTertinggiSepanjangMasa() {
        // Alasan Analisis: Mengetahui rekor nilai nominal belanja tunggal terbesar sebagai acuan batas atas plafon program loyalitas
        this.judulReport.setText("Transaksi Tertinggi Sepanjang Masa");

        // SUBQUERY Mengevaluasi nilai total_bayar maksimum tunggal menggunakan fungsi agregat MAX() di dalam nested subquery
        String sql = "SELECT c.nama_cust, 'Rp ' || co.total_bayar || ' | Tanggal: ' || co.tanggal_order AS detail " +
                "FROM public.customer c " +
                "JOIN public.customer_order co ON c.id_cust = co.id_cust " +
                "WHERE co.total_bayar = (SELECT MAX(total_bayar) FROM public.customer_order)";

        this.tampil(sql, "nama_cust", "detail");
    }

    @FXML
    public void transaksiTertinggiTiapTanggal() {
        // Alasan Analisis: Memetakan fluktuasi puncak omset harian serta mengidentifikasi profil pelanggan VIP harian
        this.judulReport.setText("Transaksi Tertinggi Tiap Tanggal");

        // SUBQUERY Memanfaatkan inline view (subquery tabel bayangan) yang dipadukan dengan Window Function DENSE_RANK()
        // untuk melakukan perangkingan nominal order terbesar yang dipecah (PARTITION BY) berdasarkan kalender tanggal transaksi
        String sql = "SELECT nama_cust, 'Tgl: ' || tanggal_order || ' | Total: Rp ' || total_bayar AS ringkasan " +
                "FROM (" +
                "  SELECT c.nama_cust, co.tanggal_order, co.total_bayar, " +
                "         DENSE_RANK() OVER(PARTITION BY co.tanggal_order ORDER BY co.total_bayar DESC) as rank " +
                "  FROM public.customer c " +
                "  JOIN public.customer_order co ON c.id_cust = co.id_cust" +
                ") AS sub_rank " +
                "WHERE rank = 1 " +
                "ORDER BY tanggal_order DESC";

        this.tampil(sql, "nama_cust", "ringkasan");
    }

    @FXML
    public void transaksiCustomer() {
        // Alasan Analisis: Mendeteksi tingkat keaktifan pelanggan tetap untuk pemberian reward voucer promosi kafe
        this.judulReport.setText("Jumlah Transaksi Customer");

        // JOIN Menghubungkan relasi data antara tabel customer dan riwayat pesanan customer_order
        // SUBQUERY Memindahkan validasi filter pembeli aktif yang memiliki transaksi > 1 menggunakan subquery berbasis IN clause
        String sql = "SELECT c.nama_cust, COUNT(co.id_order)::text AS jumlah " +
                "FROM public.customer c " +
                "JOIN public.customer_order co ON c.id_cust = co.id_cust " +
                "WHERE c.id_cust IN (" +
                "  SELECT id_cust FROM public.customer_order GROUP BY id_cust HAVING COUNT(id_order) > 1" +
                ") " +
                "GROUP BY c.nama_cust " +
                "ORDER BY COUNT(co.id_order) DESC";

        this.tampil(sql, "nama_cust", "jumlah");
    }

    @FXML
    public void pendapatanCabang() {
        // Alasan Analisis: Mengukur efisiensi bisnis finansial dan performa pencapaian target profit penjualan omset daerah
        this.judulReport.setText("Pendapatan Cabang");

        // JOIN Mengintegrasikan data master wilayah cabang toko dengan akumulasi keuangan dari nota pesanan aktif
        // AGGREGATION Menjumlahkan total nominal dana bersih masuk dari pembeli menggunakan fungsi statistik SUM()
        // ORDER BY Menampilkan urutan pendapatan dari yang tertinggi ke terendah secara akurat (DESC)
        String sql = "SELECT b.nama_branch, 'Rp ' || SUM(co.total_bayar) AS pendapatan " +
                "FROM public.branch b JOIN public.customer_order co ON b.id_branch = co.id_branch " +
                "GROUP BY b.nama_branch " +
                "ORDER BY SUM(co.total_bayar) DESC";

        this.tampil(sql, "nama_branch", "pendapatan");
    }

    @FXML
    public void menuPopuler() {
        // Alasan Analisis: Menentukan menu andalan paling disukai sebagai landasan utama penyusunan strategi manajemen stok inventaris
        this.judulReport.setText("Menu Paling Populer");

        // SUBQUERY Menyaring kumpulan ID Menu laku menggunakan pencarian bersarang (Nested Subquery) tabel order_detail
        String sql = "SELECT m.nama_menu, sub.total " +
                "FROM public.menu m " +
                "JOIN (" +
                "  SELECT id_menu, SUM(jumlah_detail)::text AS total " +
                "  FROM public.order_detail " +
                "  GROUP BY id_menu" +
                ") AS sub ON m.id_menu = sub.id_menu " +
                "ORDER BY sub.total::int DESC LIMIT 5";

        this.tampil(sql, "nama_menu", "total");
    }

    @FXML
    public void jamSibuk() {
        // Alasan Analisis: Mengoptimalkan pengaturan alokasi jam kerja shift barista serta mempercepat efisiensi durasi pelayanan
        this.judulReport.setText("Jam Transaksi Tertinggi");

        // AGGREGATION Menhitung kuantitas intensitas nota pesanan masuk per jam operasional kafe lewat COUNT()
        // SUBQUERY Menggunakan evaluasi nested subquery untuk mengelompokkan ekstraksi jam dari field tipe TIME secara valid
        String sql = "SELECT jam, COUNT(id_order)::text AS jumlah FROM (" +
                "SELECT EXTRACT(HOUR FROM waktu_order) || ':00' AS jam, id_order FROM public.customer_order) AS sub " +
                "GROUP BY jam ORDER BY jumlah DESC";

        this.tampil(sql, "jam", "jumlah");
    }

    private void tampil(String sql, String kolom1, String kolom2) {
        ObservableList<Report> list = FXCollections.observableArrayList();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while(rs.next()) {
                list.add(new Report(rs.getString(kolom1), rs.getString(kolom2)));
            }

            // CONNECT Mengaitkan data koleksi laporan bisnis ke komponen antar-muka JavaFX TableView
            this.tableReport.setItems(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void goBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(this.getClass().getResource("hello-view.fxml"));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}