package org.example.demo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class KelolaTransaksiController {

    //ID
    @FXML private TextField txtCari;
    @FXML private ComboBox<String> comboStatusUpdate;

    // Tabel Besar (Master Order - ID Unik Tidak Duplikat)
    @FXML private TableView<TransactionMaster> tabelMasterOrder;
    @FXML private TableColumn<TransactionMaster, Integer> colIdOrder;
    @FXML private TableColumn<TransactionMaster, Date> colTanggal;
    @FXML private TableColumn<TransactionMaster, Time> colWaktu;
    @FXML private TableColumn<TransactionMaster, String> colCustomer;
    @FXML private TableColumn<TransactionMaster, String> colBranch;
    @FXML private TableColumn<TransactionMaster, Double> colTotalBayar;
    @FXML private TableColumn<TransactionMaster, String> colStatus;

    // Tabel Kecil (Detail Item & Kustomisasi yang dibeli)
    @FXML private TableView<TransactionDetail> tabelDetailOrder;
    @FXML private TableColumn<TransactionDetail, String> colNamaMenu;
    @FXML private TableColumn<TransactionDetail, Integer> colJumlah;
    @FXML private TableColumn<TransactionDetail, String> colKustomisasi;

    private Parent root;
    private Stage stage;
    private Scene scene;

    //Observable List
    private final ObservableList<TransactionMaster> daftarMaster = FXCollections.observableArrayList();
    private final ObservableList<TransactionDetail> daftarDetail = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        //Connect nilai ke kolom tabel master besar
        colIdOrder.setCellValueFactory(new PropertyValueFactory<>("idOrder"));
        colTanggal.setCellValueFactory(new PropertyValueFactory<>("tanggalOrder"));
        colWaktu.setCellValueFactory(new PropertyValueFactory<>("waktuOrder"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("namaCust"));
        colBranch.setCellValueFactory(new PropertyValueFactory<>("namaBranch"));
        colTotalBayar.setCellValueFactory(new PropertyValueFactory<>("totalBayar"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("statusOrder"));
        tabelMasterOrder.setItems(daftarMaster);

        //Connect nilai ke kolom tabel detail kecil
        colNamaMenu.setCellValueFactory(new PropertyValueFactory<>("namaMenu"));
        colJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlah"));
        colKustomisasi.setCellValueFactory(new PropertyValueFactory<>("kustomisasi"));
        tabelDetailOrder.setItems(daftarDetail);

        //Isi combobox status sesuai aturan CHECK Constraint customer_order_status_order_ck
        comboStatusUpdate.getItems().addAll("Pending", "Diproses", "Siap Diambil", "Selesai");

        //Load data PosgreSQL saat pertama kali dibuka
        loadMasterOrder("");

        //Kalau diklik baris tabel master, tabel detail di bawahnya otomatis terisi data item belanjaannya
        tabelMasterOrder.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                comboStatusUpdate.setValue(newSelection.getStatusOrder());
                loadDetailOrder(newSelection.getIdOrder());
            }
        });
    }

    //Narik data PosgreSQL (READ MASTER)
    private void loadMasterOrder(String kataKunci) {
        daftarMaster.clear();
        daftarDetail.clear();

        // JOIN Menghubungkan customer_order dengan customer dan branch untuk menampilkan informasi teks utuh alih-alih angka ID
        String query = "SELECT co.id_order, co.tanggal_order, co.waktu_order, c.nama_cust, b.nama_branch, co.total_bayar, co.status_order " +
                "FROM public.customer_order co " +
                "JOIN public.customer c ON co.id_cust = c.id_cust " +
                "JOIN public.branch b ON co.id_branch = b.id_branch " +
                "WHERE LOWER(c.nama_cust) LIKE ? OR LOWER(b.nama_branch) LIKE ? " +
                "ORDER BY co.id_order DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + kataKunci.toLowerCase() + "%");
            stmt.setString(2, "%" + kataKunci.toLowerCase() + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                daftarMaster.add(new TransactionMaster(
                        rs.getInt("id_order"),
                        rs.getDate("tanggal_order"),
                        rs.getTime("waktu_order"),
                        rs.getString("nama_cust"),
                        rs.getString("nama_branch"),
                        rs.getDouble("total_bayar"),
                        rs.getString("status_order")
                ));
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Gagal memuat histori transaksi: " + e.getMessage());
        }
    }

    //Narik data Detail berdasarkan ID terpilih (READ DETAIL VIA INNER JOIN & COALESCE)
    private void loadDetailOrder(int idOrder) {
        daftarDetail.clear();

        // JOIN Kolaborasi multi-tabel antara order_detail, menu, order_customization, dan customization
        // AGGREGATION Menggabungkan varian topping belanjaan per-item menggunakan fungsi string_agg() biar rapi
        String query = "SELECT m.nama_menu, od.jumlah_detail, COALESCE(string_agg(cu.nama_customization || ' (x' || ocu.jumlah_order_customization || ')', ', '), 'Original') as kustomisasi " +
                "FROM public.order_detail od " +
                "JOIN public.menu m ON od.id_menu = m.id_menu " +
                "LEFT JOIN public.order_customization ocu ON od.id_detail = ocu.id_detail " +
                "LEFT JOIN public.customization cu ON ocu.id_customization = cu.id_customization " +
                "WHERE od.id_order = ? " +
                "GROUP BY m.nama_menu, od.jumlah_detail";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idOrder);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                daftarDetail.add(new TransactionDetail(
                        rs.getString("nama_menu"),
                        rs.getInt("jumlah_detail"),
                        rs.getString("kustomisasi")
                ));
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Gagal memuat item rincian nota: " + e.getMessage());
        }
    }

    //UPDATE STATUS NOTA
    @FXML
    public void onUpdateStatusBtnClick() {
        TransactionMaster selected = tabelMasterOrder.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningAlert("Aksi Ditolak", "Pilih nota transaksi yang ingin diubah statusnya!");
            return;
        }

        String statusBaru = comboStatusUpdate.getValue();
        if (statusBaru == null) return;

        String query = "UPDATE public.customer_order SET status_order = ? WHERE id_order = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, statusBaru);
            stmt.setInt(2, selected.getIdOrder());
            stmt.executeUpdate();

            showInformationAlert("Sukses", "Status transaksi berhasil diperbarui menjadi: " + statusBaru);
            loadMasterOrder(txtCari.getText().trim());

        } catch (SQLException e) {
            // CONSTRAINT Menjamin isi pembaruan status patuh pada standarisasi isi domain CHECK constraint status_order
            showErrorAlert("Database Error", "Gagal mengubah status akibat pelanggaran aturan: " + e.getMessage());
        }
    }

    //DELETE TRANSAKSI (RESTRICT RULE ACCORDING TO DB ARCHITECTURE)
    @FXML
    public void onDeleteTransaksiBtnClick() {
        TransactionMaster selected = tabelMasterOrder.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningAlert("Aksi Ditolak", "Pilih baris transaksi yang ingin dihapus total dari database!");
            return;
        }

        // Penghapusan sekuensial berantai (Transaction Safe) untuk membersihkan foreign key di tabel anak terlebih dahulu
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            // 1. Hapus sub-child kustomisasi item nota
            String delCustom = "DELETE FROM public.order_customization WHERE id_detail IN (SELECT id_detail FROM public.order_detail WHERE id_order = ?)";
            try (PreparedStatement ps = conn.prepareStatement(delCustom)) {
                ps.setInt(1, selected.getIdOrder());
                ps.executeUpdate();
            }

            // 2. Hapus child order_detail item nota
            String delDetail = "DELETE FROM public.order_detail WHERE id_order = ?";
            try (PreparedStatement ps = conn.prepareStatement(delDetail)) {
                ps.setInt(1, selected.getIdOrder());
                ps.executeUpdate();
            }

            // 3. Hapus parent master customer_order
            String delMaster = "DELETE FROM public.customer_order WHERE id_order = ?";
            try (PreparedStatement ps = conn.prepareStatement(delMaster)) {
                ps.setInt(1, selected.getIdOrder());
                ps.executeUpdate();
            }

            conn.commit();
            showInformationAlert("Sukses", "Nota transaksi berhasil dihapus permanen!");
            loadMasterOrder("");

        } catch (SQLException e) {
            showErrorAlert("Database Error", "Gagal melakukan penghapusan data: " + e.getMessage());
        }
    }

    //SEARCH (Mencari data real-time berdasarkan input ketikan user)
    @FXML
    public void onSearchBtnClick() {
        loadMasterOrder(txtCari.getText().trim());
    }

    // Tampilkan rangkuman statistik performa kasir via kolaborasi fungsi agregasi tingkat lanjut
    @FXML
    public void onShowStatisticClick() {
        // AGGREGATION Menghitung nilai rerata nominal belanjaan nota menggunakan fungsi statistik AVG()
        // SUBQUERRY klausa nested subquery menyeleksi kuantitas transaksi sukses yang nominal belanjanya melampaui omset rata-rata
        String query = "SELECT COUNT(id_order) FROM public.customer_order WHERE total_bayar > (SELECT AVG(total_bayar) FROM public.customer_order)";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                int jumlahTransaksiHebat = rs.getInt(1);
                showInformationAlert("Rangkuman Agregasi", "Jumlah transaksi bernilai besar (di atas rata-rata): " + jumlahTransaksiHebat + " nota.");
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Gagal memproses perhitungan agregasi statistika: " + e.getMessage());
        }
    }

    //RESET
    @FXML
    public void onResetBtnClick() {
        txtCari.clear();
        comboStatusUpdate.setValue(null);
        loadMasterOrder("");
    }

    //BACK
    public void goBack(ActionEvent event) throws IOException {
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        boolean isCurrentlyMaximized = stage.isMaximized();

        root = FXMLLoader.load(getClass().getResource("transaksi-home-view.fxml"));
        scene = new Scene(root);

        stage.setScene(scene);
        stage.setMaximized(isCurrentlyMaximized);
        stage.show();
    }

    //POP UP ALERT
    private void showInformationAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showWarningAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}