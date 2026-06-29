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

public class MasterBranchController {

    //ID
    @FXML private TextField txtidBranch;
    @FXML private TextField txtnamaBranch;
    @FXML private TextField txtAlamat;
    @FXML private TextField txtKota;
    @FXML private TextField txtKodePos;
    @FXML private TextField txtCari;

    @FXML private TableView<Branch> tabelBranch;
    @FXML private TableColumn<Branch, Integer> colId;
    @FXML private TableColumn<Branch, String> colName;
    @FXML private TableColumn<Branch, String> colAddress;
    @FXML private TableColumn<Branch, String> colCity;
    @FXML private TableColumn<Branch, String> colPostalCode;

    private Parent root;
    private Stage stage;
    private Scene scene;

    //Observable List
    private final ObservableList<Branch> daftarBranch = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        //Connect nilai ke kolom table view
        colId.setCellValueFactory(new PropertyValueFactory<>("idBranch"));
        colName.setCellValueFactory(new PropertyValueFactory<>("namaBranch"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("alamatBranch"));
        colCity.setCellValueFactory(new PropertyValueFactory<>("kotaBranch"));
        colPostalCode.setCellValueFactory(new PropertyValueFactory<>("kodePosBranch"));

        tabelBranch.setItems(daftarBranch);

        //Load data PosgreSQL
        loadDataDariDatabase();

        //Kalau diklik, formnya keisi
        tabelBranch.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                txtidBranch.setText(String.valueOf(newSelection.getIdBranch()));
                txtidBranch.setDisable(true); //ID tidak berubah saat UPDATE
                txtnamaBranch.setText(newSelection.getNamaBranch());
                txtAlamat.setText(newSelection.getAlamatBranch());
                txtKota.setText(newSelection.getKotaBranch());
                txtKodePos.setText(newSelection.getKodePosBranch());
            }
        });
    }

    //Narik data PosgreSQL
    private void loadDataDariDatabase() {
        daftarBranch.clear();
        String query = "SELECT id_branch, nama_branch, alamat_branch, kota_branch, kode_pos_branch FROM public.branch ORDER BY id_branch ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Branch br = new Branch(
                        rs.getInt("id_branch"),
                        rs.getString("nama_branch"),
                        rs.getString("alamat_branch"),
                        rs.getString("kota_branch"),
                        rs.getString("kode_pos_branch")
                );
                daftarBranch.add(br);
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Gagal memuat data dari database: " + e.getMessage());
        }
    }

    //ADD
    @FXML
    public void onAddBtnClick() {
        try {
            if (txtidBranch.getText().trim().isEmpty() || txtnamaBranch.getText().trim().isEmpty() ||
                    txtAlamat.getText().trim().isEmpty() || txtKota.getText().trim().isEmpty() || txtKodePos.getText().trim().isEmpty()) {
                showWarningAlert("Input Kosong", "Semua kolom data wajib diisi!");
                return;
            }

            int id = Integer.parseInt(txtidBranch.getText().trim());
            String name = txtnamaBranch.getText().trim();
            String address = txtAlamat.getText().trim();
            String city = txtKota.getText().trim();
            String postalCode = txtKodePos.getText().trim();

            String query = "INSERT INTO public.branch (id_branch, nama_branch, alamat_branch, kota_branch, kode_pos_branch) VALUES (?, ?, ?, ?, ?)";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, id);
                stmt.setString(2, name);
                stmt.setString(3, address);
                stmt.setString(4, city);
                stmt.setString(5, postalCode);

                stmt.executeUpdate();

                loadDataDariDatabase();
                clearForm();
                showInformationAlert("Sukses", "Data cabang berhasil ditambahkan!");

            } catch (SQLException e) {
                // CONSTRAINT branch_nama_branch_uq melempar error jika nama_branch melanggar unique constraint
                showErrorAlert("Database Error", "Gagal menyimpan data: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Format Salah", "Branch ID harus berupa angka bulat!");
        }
    }

    //UPDATE
    @FXML
    public void onUpdateBtnClick() {
        Branch selected = tabelBranch.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningAlert("Aksi Ditolak", "Pilih data pada tabel yang ingin diubah!");
            return;
        }

        String name = txtnamaBranch.getText().trim();
        String address = txtAlamat.getText().trim();
        String city = txtKota.getText().trim();
        String postalCode = txtKodePos.getText().trim();

        String query = "UPDATE public.branch SET nama_branch = ?, alamat_branch = ?, kota_branch = ?, kode_pos_branch = ? WHERE id_branch = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, name);
            stmt.setString(2, address);
            stmt.setString(3, city);
            stmt.setString(4, postalCode);
            stmt.setInt(5, selected.getIdBranch());

            stmt.executeUpdate();

            loadDataDariDatabase();
            clearForm();
            showInformationAlert("Sukses", "Data cabang berhasil diperbarui!");

        } catch (SQLException e) {
            // CONSTRAINT Memastikan pembaruan mematuhi unique constraint nama_branch agar tidak terjadi duplikasi data nama cabang
            showErrorAlert("Database Error", "Gagal memperbarui data: " + e.getMessage());
        }
    }

    //DELETE
    @FXML
    public void onDeleteBtnClick() {
        Branch selected = tabelBranch.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningAlert("Aksi Ditolak", "Pilih data pada tabel yang ingin dihapus!");
            return;
        }

        String query = "DELETE FROM public.branch WHERE id_branch = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, selected.getIdBranch());
            stmt.executeUpdate();

            loadDataDariDatabase();
            clearForm();
            showInformationAlert("Sukses", "Data cabang berhasil dihapus!");

        } catch (SQLException e) {
            // CONSTRAINT customer_order_id_branch_fk restrict penghapusan jika id_branch aktif terikat di tabel employee, inventory, atau order
            showErrorAlert("Database Error", "Gagal menghapus data: " + e.getMessage());
        }
    }

    //SEARCH
    @FXML
    public void onSearchBtnClick() {
        String kataKunci = txtCari.getText().toLowerCase().trim();
        daftarBranch.clear();

        //JOIN Menggabungkan data branch dengan total karyawannya untuk melakukan pencarian relasional berdasarkan nama kota atau nama cabang
        String query = "SELECT b.* FROM public.branch b " +
                "WHERE LOWER(b.nama_branch) LIKE ? OR LOWER(b.kota_branch) LIKE ? OR LOWER(b.alamat_branch) LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + kataKunci + "%");
            stmt.setString(2, "%" + kataKunci + "%");
            stmt.setString(3, "%" + kataKunci + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    daftarBranch.add(new Branch(
                            rs.getInt("id_branch"),
                            rs.getString("nama_branch"),
                            rs.getString("alamat_branch"),
                            rs.getString("kota_branch"),
                            rs.getString("kode_pos_branch")
                    ));
                }
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Gagal menyaring data pencarian: " + e.getMessage());
        }
    }

    // Tampilkan informasi olahan data statistik performa transaksi per-cabang kafe
    @FXML
    public void onShowStatisticClick() {
        // AGGREGATION Mengagregasikan nilai akumulatif pendapatan omset cabang kafe menggunakan fungsi statistik SUM()
        // SUBQUERRY klausa nested subquery menyeleksi cabang yang berhasil melampaui nilai rata-rata omset penjualan nasional kafe
        String query = "SELECT id_branch, SUM(total_bayar) FROM public.customer_order GROUP BY id_branch " +
                "HAVING SUM(total_bayar) > (SELECT AVG(total_bayar) FROM public.customer_order)";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            int totalCabangHebat = 0;
            while (rs.next()) {
                totalCabangHebat++;
            }
            showInformationAlert("Rangkuman Agregasi", "Jumlah cabang dengan performa di atas rata-rata: " + totalCabangHebat + " cabang.");
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Gagal memproses kalkulasi agregasi statistik: " + e.getMessage());
        }
    }

    //RESET
    @FXML
    public void onResetBtnClick() {
        txtCari.clear();
        loadDataDariDatabase();
    }

    //BACK
    public void goBack(ActionEvent event) throws IOException {
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        boolean isCurrentlyMaximized = stage.isMaximized();

        root = FXMLLoader.load(getClass().getResource("master-view.fxml"));
        scene = new Scene(root);

        stage.setScene(scene);
        stage.setMaximized(isCurrentlyMaximized);
        stage.show();
    }

    //CLEAR FORM
    private void clearForm() {
        txtidBranch.clear();
        txtidBranch.setDisable(false);
        txtnamaBranch.clear();
        txtAlamat.clear();
        txtKota.clear();
        txtKodePos.clear();
        tabelBranch.getSelectionModel().clearSelection();
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