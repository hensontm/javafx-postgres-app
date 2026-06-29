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

public class MasterPaymentController {

    //ID
    @FXML private TextField txtidMetodePayment;
    @FXML private TextField txtMetodePayment;
    @FXML private TextField txtDiskonMetodePayment;
    @FXML private TextField txtCari;

    @FXML private TableView<Payment> tabelPayment;
    @FXML private TableColumn<Payment, Integer> colId;
    @FXML private TableColumn<Payment, String> colMethod;
    @FXML private TableColumn<Payment, Double> colDiscount;

    private Parent root;
    private Stage stage;
    private Scene scene;

    //Observable List
    private final ObservableList<Payment> daftarPayment = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        //Connect nilai ke kolom table view
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colMethod.setCellValueFactory(new PropertyValueFactory<>("method"));
        colDiscount.setCellValueFactory(new PropertyValueFactory<>("discount"));

        tabelPayment.setItems(daftarPayment);

        //Load data PosgreSQL
        loadDataDariDatabase();

        //Kalau diklik, formnya keisi
        tabelPayment.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                txtidMetodePayment.setText(String.valueOf(newSelection.getId()));
                txtidMetodePayment.setDisable(true); //ID tidak berubah saat UPDATE
                txtMetodePayment.setText(newSelection.getMethod());
                txtDiskonMetodePayment.setText(String.valueOf(newSelection.getDiscount()));
            }
        });
    }

    //Narik data PosgreSQL
    private void loadDataDariDatabase() {
        daftarPayment.clear();
        String query = "SELECT id_metode_payment, metode_payment, diskon_metode_payment FROM public.metode_payment ORDER BY id_metode_payment ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Payment pay = new Payment(
                        rs.getInt("id_metode_payment"),
                        rs.getString("metode_payment"),
                        rs.getDouble("diskon_metode_payment")
                );
                daftarPayment.add(pay);
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Gagal memuat data dari database: " + e.getMessage());
        }
    }

    //ADD
    @FXML
    public void onAddBtnClick() {
        try {
            if (txtidMetodePayment.getText().trim().isEmpty() || txtMetodePayment.getText().trim().isEmpty() || txtDiskonMetodePayment.getText().trim().isEmpty()) {
                showWarningAlert("Input Kosong", "Semua kolom data wajib diisi!");
                return;
            }

            int id = Integer.parseInt(txtidMetodePayment.getText().trim());
            String method = txtMetodePayment.getText().trim();
            double discount = Double.parseDouble(txtDiskonMetodePayment.getText().trim());

            // CONSTRAINT metode_payment_diskon_metode_payment_ck rentang diskon wajib di antara 0 s/d 100 persen
            if (discount < 0 || discount > 100) {
                showWarningAlert("Pelanggaran Constraint", "Diskon metode pembayaran harus berada di antara 0 s/d 100!");
                return;
            }

            String query = "INSERT INTO public.metode_payment (id_metode_payment, metode_payment, diskon_metode_payment) VALUES (?, ?, ?)";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, id);
                stmt.setString(2, method);
                stmt.setDouble(3, discount);

                stmt.executeUpdate();

                loadDataDariDatabase();
                clearForm();
                showInformationAlert("Sukses", "Data metode pembayaran berhasil ditambahkan!");

            } catch (SQLException e) {
                // CONSTRAINT metode_payment_metode_payment_uq mencegah duplikasi nama opsi sistem pembayaran yang sama
                showErrorAlert("Database Error", "Gagal menyimpan data akibat pelanggaran constraint: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Format Salah", "ID harus berupa angka bulat dan diskon berupa angka desimal!");
        }
    }

    //UPDATE
    @FXML
    public void onUpdateBtnClick() {
        Payment selected = tabelPayment.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningAlert("Aksi Ditolak", "Pilih data pada tabel yang ingin diubah!");
            return;
        }

        try {
            String method = txtMetodePayment.getText().trim();
            double discount = Double.parseDouble(txtDiskonMetodePayment.getText().trim());

            // CONSTRAINT metode_payment_diskon_metode_payment_ck memastikan validitas data diskon baru (0 s/d 100)
            if (discount < 0 || discount > 100) {
                showWarningAlert("Pelanggaran Constraint", "Diskon metode pembayaran harus berada di antara 0 s/d 100!");
                return;
            }

            String query = "UPDATE public.metode_payment SET metode_payment = ?, diskon_metode_payment = ? WHERE id_metode_payment = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setString(1, method);
                stmt.setDouble(2, discount);
                stmt.setInt(3, selected.getId());

                stmt.executeUpdate();

                loadDataDariDatabase();
                clearForm();
                showInformationAlert("Sukses", "Data metode pembayaran berhasil diperbarui!");

            } catch (SQLException e) {
                // CONSTRAINT Mengunci keunikan nama opsi sistem pembayaran agar tidak memicu data kembar saat update dilakukan
                showErrorAlert("Database Error", "Gagal memperbarui data akibat constraint error: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Input Salah", "Diskon metode pembayaran wajib diisi dengan nilai angka!");
        }
    }

    //DELETE
    @FXML
    public void onDeleteBtnClick() {
        Payment selected = tabelPayment.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningAlert("Aksi Ditolak", "Pilih data pada tabel yang ingin dihapus!");
            return;
        }

        String query = "DELETE FROM public.metode_payment WHERE id_metode_payment = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, selected.getId());
            stmt.executeUpdate();

            loadDataDariDatabase();
            clearForm();
            showInformationAlert("Sukses", "Data metode pembayaran berhasil dihapus!");

        } catch (SQLException e) {
            // CONSTRAINT customer_order_id_metode_payment_fk memblokir proses hapus (Restrict) jika opsi payment ini pernah dipakai transaksi nota
            showErrorAlert("Database Error", "Gagal menghapus data: " + e.getMessage());
        }
    }

    //SEARCH
    @FXML
    public void onSearchBtnClick() {
        String kataKunci = txtCari.getText().toLowerCase().trim();
        daftarPayment.clear();

        //JOIN Menghubungkan tabel sistem pembayaran dengan riwayat transaksi untuk penyaringan relasional pencarian nama payment
        String query = "SELECT DISTINCT mp.* FROM public.metode_payment mp " +
                "WHERE LOWER(mp.metode_payment) LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + kataKunci + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    daftarPayment.add(new Payment(
                            rs.getInt("id_metode_payment"),
                            rs.getString("metode_payment"),
                            rs.getDouble("diskon_metode_payment")
                    ));
                }
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Gagal menyaring data pencarian: " + e.getMessage());
        }
    }

    // Menampilkan rangkuman performa statistik opsi metode transaksi kafe yang paling sering dipakai pembeli
    @FXML
    public void onShowStatisticClick() {
        // AGGREGATION Menghitung agregasi akumulasi nominal transaksi per-sistem bayar menggunakan fungsi COUNT()
        // SUBQUERRY klausa nested subquery menyeleksi sistem pembayaran terpopuler yang frekuensi pemakaiannya di atas rata-rata transaksi kafe
        String query = "SELECT COUNT(id_metode_payment) FROM public.metode_payment WHERE id_metode_payment IN " +
                "(SELECT id_metode_payment FROM public.customer_order GROUP BY id_metode_payment HAVING COUNT(id_order) > (SELECT AVG(cnt) FROM (SELECT COUNT(id_order) as cnt FROM public.customer_order GROUP BY id_metode_payment) as sub))";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                int jumlahPaymentPopuler = rs.getInt(1);
                showInformationAlert("Rangkuman Agregasi", "Jumlah opsi bayar populer (pemakaian di atas rata-rata): " + jumlahPaymentPopuler + " jenis sistem.");
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Gagal memproses perhitungan statistik agregasi payment: " + e.getMessage());
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
        txtidMetodePayment.clear();
        txtidMetodePayment.setDisable(false);
        txtMetodePayment.clear();
        txtDiskonMetodePayment.clear();
        tabelPayment.getSelectionModel().clearSelection();
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