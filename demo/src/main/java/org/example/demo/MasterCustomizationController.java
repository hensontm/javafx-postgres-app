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

public class MasterCustomizationController {

    //ID
    @FXML private TextField txtidCustomization;
    @FXML private TextField txtnamaCustomization;
    @FXML private TextField txthargaCustomization;
    @FXML private TextField txtCari;

    @FXML private TableView<Customization> tabelCustomization;
    @FXML private TableColumn<Customization, Integer> colId;
    @FXML private TableColumn<Customization, String> colName;
    @FXML private TableColumn<Customization, Double> colPrice;

    private Parent root;
    private Stage stage;
    private Scene scene;

    //Observable List
    private final ObservableList<Customization> daftarCustomization = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        //Connect nilai ke kolom table view
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));

        tabelCustomization.setItems(daftarCustomization);

        //Load data PosgreSQL
        loadDataDariDatabase();

        //Kalau diklik, formnya keisi
        tabelCustomization.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                txtidCustomization.setText(String.valueOf(newSelection.getId()));
                txtidCustomization.setDisable(true); //ID tidak berubah saat UPDATE
                txtnamaCustomization.setText(newSelection.getName());
                txthargaCustomization.setText(String.valueOf(newSelection.getPrice()));
            }
        });
    }

    //Narik data PosgreSQL
    private void loadDataDariDatabase() {
        daftarCustomization.clear();
        String query = "SELECT id_customization, nama_customization, harga_customization FROM public.customization ORDER BY id_customization ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Customization cust = new Customization(
                        rs.getInt("id_customization"),
                        rs.getString("nama_customization"),
                        rs.getDouble("harga_customization")
                );
                daftarCustomization.add(cust);
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Gagal memuat data dari database: " + e.getMessage());
        }
    }

    //ADD
    @FXML
    public void onAddBtnClick() {
        try {
            int id = Integer.parseInt(txtidCustomization.getText().trim());
            String name = txtnamaCustomization.getText().trim();
            double price = Double.parseDouble(txthargaCustomization.getText().trim());

            if (name.isEmpty()) {
                showWarningAlert("Input Kosong", "Nama kustomisasi wajib diisi!");
                return;
            }

            String query = "INSERT INTO public.customization (id_customization, nama_customization, harga_customization) VALUES (?, ?, ?)";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, id);
                stmt.setString(2, name);
                stmt.setDouble(3, price);

                stmt.executeUpdate();

                loadDataDariDatabase();
                clearForm();
                showInformationAlert("Sukses", "Data kustomisasi berhasil ditambahkan!");

            } catch (SQLException e) {
                showErrorAlert("Database Error", "Gagal menyimpan data: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Input Salah", "ID dan Price harus berupa angka valid!");
        }
    }

    //UPDATE
    @FXML
    public void onUpdateBtnClick() {
        Customization selected = tabelCustomization.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningAlert("Aksi Ditolak", "Pilih data pada tabel yang ingin diubah!");
            return;
        }

        try {
            String name = txtnamaCustomization.getText().trim();
            double price = Double.parseDouble(txthargaCustomization.getText().trim());

            String query = "UPDATE public.customization SET nama_customization = ?, harga_customization = ? WHERE id_customization = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setString(1, name);
                stmt.setDouble(2, price);
                stmt.setInt(3, selected.getId());

                stmt.executeUpdate();

                loadDataDariDatabase();
                clearForm();
                showInformationAlert("Sukses", "Data kustomisasi berhasil diperbarui!");

            } catch (SQLException e) {
                showErrorAlert("Database Error", "Gagal memperbarui data: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Input Salah", "Price harus berupa angka valid!");
        }
    }

    //DELETE
    @FXML
    public void onDeleteBtnClick() {
        Customization selected = tabelCustomization.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningAlert("Aksi Ditolak", "Pilih data pada tabel yang ingin dihapus!");
            return;
        }

        String query = "DELETE FROM public.customization WHERE id_customization = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, selected.getId());
            stmt.executeUpdate();

            loadDataDariDatabase();
            clearForm();
            showInformationAlert("Sukses", "Data kustomisasi berhasil dihapus!");

        } catch (SQLException e) {
            showErrorAlert("Database Error", "Gagal menghapus data: " + e.getMessage());
        }
    }

    //SEARCH
    @FXML
    public void onSearchBtnClick() {
        String kataKunci = txtCari.getText().toLowerCase().trim();
        ObservableList<Customization> hasilFilter = FXCollections.observableArrayList();

        for (Customization cust : daftarCustomization) {
            if (cust.getName().toLowerCase().contains(kataKunci)) {
                hasilFilter.add(cust);
            }
        }
        tabelCustomization.setItems(hasilFilter);
    }

    //RESET
    @FXML
    public void onResetBtnClick() {
        txtCari.clear();
        tabelCustomization.setItems(daftarCustomization);
    }

    //BACK
    @FXML
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
        txtidCustomization.clear();
        txtidCustomization.setDisable(false);
        txtnamaCustomization.clear();
        txthargaCustomization.clear();
        tabelCustomization.getSelectionModel().clearSelection();
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