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
    @FXML private TextField txtalamatBranch;
    @FXML private TextField txtkotaBranch;
    @FXML private TextField txtkodePosBranch;
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
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colCity.setCellValueFactory(new PropertyValueFactory<>("city"));
        colPostalCode.setCellValueFactory(new PropertyValueFactory<>("postalCode"));

        tabelBranch.setItems(daftarBranch);

        //Load data PosgreSQL
        loadDataDariDatabase();

        //Kalau diklik, formnya keisi
        tabelBranch.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                txtidBranch.setText(String.valueOf(newSelection.getId()));
                txtidBranch.setDisable(true); //ID tidak berubah saat UPDATE
                txtnamaBranch.setText(newSelection.getName());
                txtalamatBranch.setText(newSelection.getAddress());
                txtkotaBranch.setText(newSelection.getCity());
                txtkodePosBranch.setText(newSelection.getPostalCode());
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
            int id = Integer.parseInt(txtidBranch.getText().trim());
            String name = txtnamaBranch.getText().trim();
            String address = txtalamatBranch.getText().trim();
            String city = txtkotaBranch.getText().trim();
            String postal = txtkodePosBranch.getText().trim();

            if (name.isEmpty() || address.isEmpty() || city.isEmpty() || postal.isEmpty()) {
                showWarningAlert("Input Kosong", "Semua kolom data wajib diisi!");
                return;
            }

            String query = "INSERT INTO public.branch (id_branch, nama_branch, alamat_branch, kota_branch, kode_pos_branch) VALUES (?, ?, ?, ?, ?)";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, id);
                stmt.setString(2, name);
                stmt.setString(3, address);
                stmt.setString(4, city);
                stmt.setString(5, postal);

                stmt.executeUpdate();

                loadDataDariDatabase();
                clearForm();
                showInformationAlert("Sukses", "Data cabang berhasil ditambahkan!");

            } catch (SQLException e) {
                showErrorAlert("Database Error", "Gagal menyimpan data: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Input Salah", "ID Branch harus berupa angka valid!");
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

        try {
            String name = txtnamaBranch.getText().trim();
            String address = txtalamatBranch.getText().trim();
            String city = txtkotaBranch.getText().trim();
            String postal = txtkodePosBranch.getText().trim();

            String query = "UPDATE public.branch SET nama_branch = ?, alamat_branch = ?, kota_branch = ?, kode_pos_branch = ? WHERE id_branch = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setString(1, name);
                stmt.setString(2, address);
                stmt.setString(3, city);
                stmt.setString(4, postal);
                stmt.setInt(5, selected.getId());

                stmt.executeUpdate();

                loadDataDariDatabase();
                clearForm();
                showInformationAlert("Sukses", "Data cabang berhasil diperbarui!");

            } catch (SQLException e) {
                showErrorAlert("Database Error", "Gagal memperbarui data: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Input Salah", "Proses update gagal.");
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

            stmt.setInt(1, selected.getId());
            stmt.executeUpdate();

            loadDataDariDatabase();
            clearForm();
            showInformationAlert("Sukses", "Data cabang berhasil dihapus!");

        } catch (SQLException e) {
            showErrorAlert("Database Error", "Gagal menghapus data: " + e.getMessage());
        }
    }

    //SEARCH
    @FXML
    public void onSearchBtnClick() {
        String kataKunci = txtCari.getText().toLowerCase().trim();
        ObservableList<Branch> hasilFilter = FXCollections.observableArrayList();

        for (Branch br : daftarBranch) {
            if (br.getName().toLowerCase().contains(kataKunci) ||
                    br.getCity().toLowerCase().contains(kataKunci) ||
                    br.getAddress().toLowerCase().contains(kataKunci)) {
                hasilFilter.add(br);
            }
        }
        tabelBranch.setItems(hasilFilter);
    }

    //RESET
    @FXML
    public void onResetBtnClick() {
        txtCari.clear();
        tabelBranch.setItems(daftarBranch);
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
        txtidBranch.clear();
        txtidBranch.setDisable(false);
        txtnamaBranch.clear();
        txtalamatBranch.clear();
        txtkotaBranch.clear();
        txtkodePosBranch.clear();
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