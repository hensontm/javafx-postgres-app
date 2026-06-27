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

public class MasterMenuController {

    //ID
    @FXML private TextField txtidMenu;
    @FXML private TextField txtnamaMenu;
    @FXML private TextField txthargaMenu;
    @FXML private TextField txtidCategory;
    @FXML private TextField txtCari;

    @FXML private TableView<Menu> tabelMenu;
    @FXML private TableColumn<Menu, Integer> colId;
    @FXML private TableColumn<Menu, String> colName;
    @FXML private TableColumn<Menu, Double> colPrice;
    @FXML private TableColumn<Menu, Integer> colIdCategory;

    private Parent root;
    private Stage stage;
    private Scene scene;

    //Observable List
    private final ObservableList<Menu> daftarMenu = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        //Connect nilai ke kolom table view
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colIdCategory.setCellValueFactory(new PropertyValueFactory<>("idCategory"));

        tabelMenu.setItems(daftarMenu);

        //Load data PosgreSQL
        loadDataDariDatabase();

        //Kalau diklik, formnya keisi
        tabelMenu.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                txtidMenu.setText(String.valueOf(newSelection.getId()));
                txtidMenu.setDisable(true); //ID tidak berubah saat UPDATE
                txtnamaMenu.setText(newSelection.getName());
                txthargaMenu.setText(String.valueOf(newSelection.getPrice()));
                txtidCategory.setText(String.valueOf(newSelection.getIdCategory()));
            }
        });
    }

    //Narik data PosgreSQL
    private void loadDataDariDatabase() {
        daftarMenu.clear();
        String query = "SELECT id_menu, nama_menu, harga_menu, id_category FROM public.menu ORDER BY id_menu ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Menu menu = new Menu(
                        rs.getInt("id_menu"),
                        rs.getString("nama_menu"),
                        rs.getDouble("harga_menu"),
                        rs.getInt("id_category")
                );
                daftarMenu.add(menu);
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Gagal memuat data dari database: " + e.getMessage());
        }
    }

    //ADD
    @FXML
    public void onAddBtnClick() {
        try {
            int id = Integer.parseInt(txtidMenu.getText().trim());
            String name = txtnamaMenu.getText().trim();
            double price = Double.parseDouble(txthargaMenu.getText().trim());
            int idCategory = Integer.parseInt(txtidCategory.getText().trim());

            if (name.isEmpty()) {
                showWarningAlert("Input Kosong", "Nama menu wajib diisi!");
                return;
            }

            String query = "INSERT INTO public.menu (id_menu, nama_menu, harga_menu, id_category) VALUES (?, ?, ?, ?)";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, id);
                stmt.setString(2, name);
                stmt.setDouble(3, price);
                stmt.setInt(4, idCategory);

                stmt.executeUpdate();

                loadDataDariDatabase();
                clearForm();
                showInformationAlert("Sukses", "Data menu berhasil ditambahkan!");

            } catch (SQLException e) {
                showErrorAlert("Database Error", "Gagal menyimpan data: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Input Salah", "ID, Harga, dan ID Kategori harus berupa angka valid!");
        }
    }

    //UPDATE
    @FXML
    public void onUpdateBtnClick() {
        Menu selected = tabelMenu.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningAlert("Aksi Ditolak", "Pilih data pada tabel yang ingin diubah!");
            return;
        }

        try {
            String name = txtnamaMenu.getText().trim();
            double price = Double.parseDouble(txthargaMenu.getText().trim());
            int idCategory = Integer.parseInt(txtidCategory.getText().trim());

            String query = "UPDATE public.menu SET nama_menu = ?, harga_menu = ?, id_category = ? WHERE id_menu = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setString(1, name);
                stmt.setDouble(2, price);
                stmt.setInt(3, idCategory);
                stmt.setInt(4, selected.getId());

                stmt.executeUpdate();

                loadDataDariDatabase();
                clearForm();
                showInformationAlert("Sukses", "Data menu berhasil diperbarui!");

            } catch (SQLException e) {
                showErrorAlert("Database Error", "Gagal memperbarui data: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Input Salah", "Harga dan ID Kategori harus berupa angka valid!");
        }
    }

    //DELETE
    @FXML
    public void onDeleteBtnClick() {
        Menu selected = tabelMenu.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningAlert("Aksi Ditolak", "Pilih data pada tabel yang ingin dihapus!");
            return;
        }

        String query = "DELETE FROM public.menu WHERE id_menu = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, selected.getId());
            stmt.executeUpdate();

            loadDataDariDatabase();
            clearForm();
            showInformationAlert("Sukses", "Data menu berhasil dihapus!");

        } catch (SQLException e) {
            showErrorAlert("Database Error", "Gagal menghapus data: " + e.getMessage());
        }
    }

    //SEARCH
    @FXML
    public void onSearchBtnClick() {
        String kataKunci = txtCari.getText().toLowerCase().trim();
        ObservableList<Menu> hasilFilter = FXCollections.observableArrayList();

        for (Menu menu : daftarMenu) {
            if (menu.getName().toLowerCase().contains(kataKunci)) {
                hasilFilter.add(menu);
            }
        }
        tabelMenu.setItems(hasilFilter);
    }

    //RESET
    @FXML
    public void onResetBtnClick() {
        txtCari.clear();
        tabelMenu.setItems(daftarMenu);
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
        txtidMenu.clear();
        txtidMenu.setDisable(false);
        txtnamaMenu.clear();
        txthargaMenu.clear();
        txtidCategory.clear();
        tabelMenu.getSelectionModel().clearSelection();
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