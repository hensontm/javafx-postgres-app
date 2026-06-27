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
    @FXML private TextField txtidPayment;
    @FXML private TextField txtmetodePayment;
    @FXML private TextField txtdiskonPayment;
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
                txtidPayment.setText(String.valueOf(newSelection.getId()));
                txtidPayment.setDisable(true); //ID tidak berubah saat UPDATE
                txtmetodePayment.setText(newSelection.getMethod());
                txtdiskonPayment.setText(String.valueOf(newSelection.getDiscount()));
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
            int id = Integer.parseInt(txtidPayment.getText().trim());
            String method = txtmetodePayment.getText().trim();
            double discount = Double.parseDouble(txtdiskonPayment.getText().trim());

            if (method.isEmpty()) {
                showWarningAlert("Input Kosong", "Metode pembayaran wajib diisi!");
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
                showInformationAlert("Sukses", "Metode pembayaran berhasil ditambahkan!");

            } catch (SQLException e) {
                showErrorAlert("Database Error", "Gagal menyimpan data: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Input Salah", "ID dan Discount harus berupa angka valid!");
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
            String method = txtmetodePayment.getText().trim();
            double discount = Double.parseDouble(txtdiskonPayment.getText().trim());

            String query = "UPDATE public.metode_payment SET metode_payment = ?, diskon_metode_payment = ? WHERE id_metode_payment = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setString(1, method);
                stmt.setDouble(2, discount);
                stmt.setInt(3, selected.getId());

                stmt.executeUpdate();

                loadDataDariDatabase();
                clearForm();
                showInformationAlert("Sukses", "Metode pembayaran berhasil diperbarui!");

            } catch (SQLException e) {
                showErrorAlert("Database Error", "Gagal memperbarui data: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Input Salah", "Discount harus berupa angka valid!");
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
            showInformationAlert("Sukses", "Metode pembayaran berhasil dihapus!");

        } catch (SQLException e) {
            showErrorAlert("Database Error", "Gagal menghapus data: " + e.getMessage());
        }
    }

    //SEARCH
    @FXML
    public void onSearchBtnClick() {
        String kataKunci = txtCari.getText().toLowerCase().trim();
        ObservableList<Payment> hasilFilter = FXCollections.observableArrayList();

        for (Payment pay : daftarPayment) {
            if (pay.getMethod().toLowerCase().contains(kataKunci)) {
                hasilFilter.add(pay);
            }
        }
        tabelPayment.setItems(hasilFilter);
    }

    //RESET
    @FXML
    public void onResetBtnClick() {
        txtCari.clear();
        tabelPayment.setItems(daftarPayment);
    }

    //BACK
    public void goBack(ActionEvent event) throws IOException {

        root = FXMLLoader.load(
                getClass().getResource("master-view.fxml")
        );

        stage=(Stage)((Node)event.getSource())
                .getScene()
                .getWindow();

        scene=new Scene(root);

        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();

    }

    //CLEAR FORM
    private void clearForm() {
        txtidPayment.clear();
        txtidPayment.setDisable(false);
        txtmetodePayment.clear();
        txtdiskonPayment.clear();
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