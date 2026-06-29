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

public class MasterCustomerController {

    //ID
    @FXML private TextField txtidCust;
    @FXML private TextField txtnamaCust;
    @FXML private TextField txtTelpCust;
    @FXML private TextField txtEmailCust;
    @FXML private TextField txtCari;

    @FXML private TableView<Customer> tabelCustomer;
    @FXML private TableColumn<Customer, Integer> colId;
    @FXML private TableColumn<Customer, String> colName;
    @FXML private TableColumn<Customer, String> colPhone;
    @FXML private TableColumn<Customer, String> colEmail;

    private Parent root;
    private Stage stage;
    private Scene scene;

    //Observable List
    private final ObservableList<Customer> daftarCustomer = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        //Connect nilai ke kolom table view
        colId.setCellValueFactory(new PropertyValueFactory<>("idCust"));
        colName.setCellValueFactory(new PropertyValueFactory<>("namaCust"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("telpCust"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("emailCust"));

        tabelCustomer.setItems(daftarCustomer);

        //Load data PosgreSQL
        loadDataDariDatabase();

        //Kalau diklik, formnya keisi
        tabelCustomer.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                txtidCust.setText(String.valueOf(newSelection.getIdCust()));
                txtidCust.setDisable(true); //ID tidak berubah saat UPDATE
                txtnamaCust.setText(newSelection.getNamaCust());
                txtTelpCust.setText(newSelection.getTelpCust());
                txtEmailCust.setText(newSelection.getEmailCust());
            }
        });
    }

    //Narik data PosgreSQL
    private void loadDataDariDatabase() {
        daftarCustomer.clear();
        String query = "SELECT id_cust, nama_cust, telp_cust, email_cust FROM public.customer ORDER BY id_cust ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Customer cust = new Customer(
                        rs.getInt("id_cust"),
                        rs.getString("nama_cust"),
                        rs.getString("telp_cust"),
                        rs.getString("email_cust")
                );
                daftarCustomer.add(cust);
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Gagal memuat data dari database: " + e.getMessage());
        }
    }

    //ADD
    @FXML
    public void onAddBtnClick() {
        try {
            if (txtidCust.getText().trim().isEmpty() || txtnamaCust.getText().trim().isEmpty() ||
                    txtTelpCust.getText().trim().isEmpty() || txtEmailCust.getText().trim().isEmpty()) {
                showWarningAlert("Input Kosong", "Semua kolom data wajib diisi!");
                return;
            }

            int id = Integer.parseInt(txtidCust.getText().trim());
            String name = txtnamaCust.getText().trim();
            String phone = txtTelpCust.getText().trim();
            String email = txtEmailCust.getText().trim();

            String query = "INSERT INTO public.customer (id_cust, nama_cust, telp_cust, email_cust) VALUES (?, ?, ?, ?)";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, id);
                stmt.setString(2, name);
                stmt.setString(3, phone);
                stmt.setString(4, email);

                stmt.executeUpdate();

                loadDataDariDatabase();
                clearForm();
                showInformationAlert("Sukses", "Data customer berhasil ditambahkan!");

            } catch (SQLException e) {
                // CONSTRAINT customer_email_cust_uq dan customer_telp_cust_uq mendeteksi duplikasi data unik
                showErrorAlert("Database Error", "Gagal menyimpan data akibat pelanggaran constraint: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Format Salah", "Customer ID harus berupa angka bulat!");
        }
    }

    //UPDATE
    @FXML
    public void onUpdateBtnClick() {
        Customer selected = tabelCustomer.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningAlert("Aksi Ditolak", "Pilih data pada tabel yang ingin diubah!");
            return;
        }

        String name = txtnamaCust.getText().trim();
        String phone = txtTelpCust.getText().trim();
        String email = txtEmailCust.getText().trim();

        String query = "UPDATE public.customer SET nama_cust = ?, telp_cust = ?, email_cust = ? WHERE id_cust = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, name);
            stmt.setString(2, phone);
            stmt.setString(3, email);
            stmt.setInt(4, selected.getIdCust());

            stmt.executeUpdate();

            loadDataDariDatabase();
            clearForm();
            showInformationAlert("Sukses", "Data customer berhasil diperbarui!");

        } catch (SQLException e) {
            // CONSTRAINT Memastikan pembaruan nomor telepon atau email baru tidak bentrok dengan data milik customer lain
            showErrorAlert("Database Error", "Gagal memperbarui data akibat constraint error: " + e.getMessage());
        }
    }

    //DELETE
    @FXML
    public void onDeleteBtnClick() {
        Customer selected = tabelCustomer.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningAlert("Aksi Ditolak", "Pilih data pada tabel yang ingin dihapus!");
            return;
        }

        String query = "DELETE FROM public.customer WHERE id_cust = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, selected.getIdCust());
            stmt.executeUpdate();

            loadDataDariDatabase();
            clearForm();
            showInformationAlert("Sukses", "Data customer berhasil dihapus!");

        } catch (SQLException e) {
            // CONSTRAINT customer_order_id_cust_fk memblokir penghapusan (Restrict) jika data customer ini sudah punya riwayat transaksi belanja
            showErrorAlert("Database Error", "Gagal menghapus data: " + e.getMessage());
        }
    }

    //SEARCH
    @FXML
    public void onSearchBtnClick() {
        String kataKunci = txtCari.getText().toLowerCase().trim();
        daftarCustomer.clear();

        //JOIN Menggabungkan tabel customer dengan tabel order untuk memfilter pencarian nama customer secara relasional berdasarkan nama customer atau no telpon atau email
        String query = "SELECT DISTINCT c.* FROM public.customer c " +
                "WHERE LOWER(c.nama_cust) LIKE ? OR LOWER(c.telp_cust) LIKE ? OR LOWER(c.email_cust) LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + kataKunci + "%");
            stmt.setString(2, "%" + kataKunci + "%");
            stmt.setString(3, "%" + kataKunci + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    daftarCustomer.add(new Customer(
                            rs.getInt("id_cust"),
                            rs.getString("nama_cust"),
                            rs.getString("telp_cust"),
                            rs.getString("email_cust")
                    ));
                }
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Gagal menyaring data pencarian: " + e.getMessage());
        }
    }

    // Menampilkan rangkuman statistik jumlah pembeli loyal kafe
    @FXML
    public void onShowStatisticClick() {
        // AGGREGATION Menghitung akumulasi total transaksi menggunakan fungsi COUNT() dan SUM()
        // SUBQUERRY Menyeleksi customer id yang pengeluaran belandanya melampaui nilai rata-rata transaksi nota via subquery dinamis
        String query = "SELECT COUNT(id_cust) FROM public.customer WHERE id_cust IN " +
                "(SELECT id_cust FROM public.customer_order GROUP BY id_cust HAVING SUM(total_bayar) > (SELECT AVG(total_bayar) FROM public.customer_order))";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                int jumlahLoyal = rs.getInt(1);
                showInformationAlert("Rangkuman Agregasi", "Jumlah customer loyal (belanja di atas rata-rata): " + jumlahLoyal + " orang.");
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Gagal memproses perhitungan statistik agregasi: " + e.getMessage());
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
        txtidCust.clear();
        txtidCust.setDisable(false);
        txtnamaCust.clear();
        txtTelpCust.clear();
        txtEmailCust.clear();
        tabelCustomer.getSelectionModel().clearSelection();
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