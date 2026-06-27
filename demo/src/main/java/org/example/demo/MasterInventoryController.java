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

public class MasterInventoryController {

    //ID
    @FXML private TextField txtidInventory;
    @FXML private TextField txtStok;
    @FXML private TextField txtidMenu;
    @FXML private TextField txtidBranch;
    @FXML private TextField txtCari;

    @FXML private TableView<Inventory> tabelInventory;
    @FXML private TableColumn<Inventory, Integer> colId;
    @FXML private TableColumn<Inventory, Integer> colStok;
    @FXML private TableColumn<Inventory, Integer> colIdMenu;
    @FXML private TableColumn<Inventory, Integer> colIdBranch;

    private Parent root;
    private Stage stage;
    private Scene scene;

    //Observable List
    private final ObservableList<Inventory> daftarInventory = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        //Connect nilai ke kolom table view
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colStok.setCellValueFactory(new PropertyValueFactory<>("stok"));
        colIdMenu.setCellValueFactory(new PropertyValueFactory<>("idMenu"));
        colIdBranch.setCellValueFactory(new PropertyValueFactory<>("idBranch"));

        tabelInventory.setItems(daftarInventory);

        //Load data PosgreSQL
        loadDataDariDatabase();

        //Kalau diklik, formnya keisi
        tabelInventory.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                txtidInventory.setText(String.valueOf(newSelection.getId()));
                txtidInventory.setDisable(true); //ID tidak berubah saat UPDATE
                txtStok.setText(String.valueOf(newSelection.getStok()));
                txtidMenu.setText(String.valueOf(newSelection.getIdMenu()));
                txtidBranch.setText(String.valueOf(newSelection.getIdBranch()));
            }
        });
    }

    //Narik data PosgreSQL
    private void loadDataDariDatabase() {
        daftarInventory.clear();
        String query = "SELECT id_inventory, stok_inventory, id_menu, id_branch FROM public.inventory ORDER BY id_inventory ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Inventory inv = new Inventory(
                        rs.getInt("id_inventory"),
                        rs.getInt("stok_inventory"),
                        rs.getInt("id_menu"),
                        rs.getInt("id_branch")
                );
                daftarInventory.add(inv);
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Gagal memuat data dari database: " + e.getMessage());
        }
    }

    //ADD
    @FXML
    public void onAddBtnClick() {
        try {
            int id = Integer.parseInt(txtidInventory.getText().trim());
            int stok = Integer.parseInt(txtStok.getText().trim());
            int idMenu = Integer.parseInt(txtidMenu.getText().trim());
            int idBranch = Integer.parseInt(txtidBranch.getText().trim());

            String query = "INSERT INTO public.inventory (id_inventory, stok_inventory, id_menu, id_branch) VALUES (?, ?, ?, ?)";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, id);
                stmt.setInt(2, stok);
                stmt.setInt(3, idMenu);
                stmt.setInt(4, idBranch);

                stmt.executeUpdate();

                loadDataDariDatabase();
                clearForm();
                showInformationAlert("Sukses", "Data inventori berhasil ditambahkan!");

            } catch (SQLException e) {
                showErrorAlert("Database Error", "Gagal menyimpan data: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Input Salah", "Semua kolom input harus berupa angka valid!");
        }
    }

    //UPDATE
    @FXML
    public void onUpdateBtnClick() {
        Inventory selected = tabelInventory.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningAlert("Aksi Ditolak", "Pilih data pada tabel yang ingin diubah!");
            return;
        }

        try {
            int stok = Integer.parseInt(txtStok.getText().trim());
            int idMenu = Integer.parseInt(txtidMenu.getText().trim());
            int idBranch = Integer.parseInt(txtidBranch.getText().trim());

            String query = "UPDATE public.inventory SET stok_inventory = ?, id_menu = ?, id_branch = ? WHERE id_inventory = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, stok);
                stmt.setInt(2, idMenu);
                stmt.setInt(3, idBranch);
                stmt.setInt(4, selected.getId());

                stmt.executeUpdate();

                loadDataDariDatabase();
                clearForm();
                showInformationAlert("Sukses", "Data inventori berhasil diperbarui!");

            } catch (SQLException e) {
                showErrorAlert("Database Error", "Gagal memperbarui data: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Input Salah", "Semua kolom input harus berupa angka valid!");
        }
    }

    //DELETE
    @FXML
    public void onDeleteBtnClick() {
        Inventory selected = tabelInventory.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningAlert("Aksi Ditolak", "Pilih data pada tabel yang ingin dihapus!");
            return;
        }

        String query = "DELETE FROM public.inventory WHERE id_inventory = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, selected.getId());
            stmt.executeUpdate();

            loadDataDariDatabase();
            clearForm();
            showInformationAlert("Sukses", "Data inventori berhasil dihapus!");

        } catch (SQLException e) {
            showErrorAlert("Database Error", "Gagal menghapus data: " + e.getMessage());
        }
    }

    //SEARCH
    @FXML
    public void onSearchBtnClick() {
        String kataKunci = txtCari.getText().trim();
        if (kataKunci.isEmpty()) {
            tabelInventory.setItems(daftarInventory);
            return;
        }

        ObservableList<Inventory> hasilFilter = FXCollections.observableArrayList();
        for (Inventory inv : daftarInventory) {
            if (String.valueOf(inv.getIdBranch()).equals(kataKunci) ||
                    String.valueOf(inv.getIdMenu()).equals(kataKunci)) {
                hasilFilter.add(inv);
            }
        }
        tabelInventory.setItems(hasilFilter);
    }

    //RESET
    @FXML
    public void onResetBtnClick() {
        txtCari.clear();
        tabelInventory.setItems(daftarInventory);
    }

    //BACK
    public void goBack(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("master-view.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    //CLEAR FORM
    private void clearForm() {
        txtidInventory.clear();
        txtidInventory.setDisable(false);
        txtStok.clear();
        txtidMenu.clear();
        txtidBranch.clear();
        tabelInventory.getSelectionModel().clearSelection();
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