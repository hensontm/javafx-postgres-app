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

public class MasterCategoryController {

    //ID
    @FXML private TextField txtidCategory;
    @FXML private TextField txtnamaCategory;
    @FXML private TextField txtCari;

    @FXML private TableView<Category> tabelCategory;
    @FXML private TableColumn<Category, Integer> colId;
    @FXML private TableColumn<Category, String> colName;

    private Parent root;
    private Stage stage;
    private Scene scene;

    //Observable List
    private final ObservableList<Category> daftarCategory = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        //Connect nilai ke kolom table view
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));

        tabelCategory.setItems(daftarCategory);

        //Load data PosgreSQL
        loadDataDariDatabase();

        //Kalau diklik, formnya keisi
        tabelCategory.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                txtidCategory.setText(String.valueOf(newSelection.getId()));
                txtidCategory.setDisable(true); //ID tidak berubah saat UPDATE
                txtnamaCategory.setText(newSelection.getName());
            }
        });
    }

    //Narik data PosgreSQL
    private void loadDataDariDatabase() {
        daftarCategory.clear();
        String query = "SELECT id_category, nama_category FROM public.category ORDER BY id_category ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Category cat = new Category(
                        rs.getInt("id_category"),
                        rs.getString("nama_category")
                );
                daftarCategory.add(cat);
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Gagal memuat data dari database: " + e.getMessage());
        }
    }

    //ADD
    @FXML
    public void onAddBtnClick() {
        try {
            int id = Integer.parseInt(txtidCategory.getText().trim());
            String name = txtnamaCategory.getText().trim();

            if (name.isEmpty()) {
                showWarningAlert("Input Kosong", "Nama kategori wajib diisi!");
                return;
            }

            String query = "INSERT INTO public.category (id_category, nama_category) VALUES (?, ?)";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, id);
                stmt.setString(2, name);

                stmt.executeUpdate();

                loadDataDariDatabase();
                clearForm();
                showInformationAlert("Sukses", "Kategori berhasil ditambahkan!");

            } catch (SQLException e) {
                showErrorAlert("Database Error", "Gagal menyimpan data: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Input Salah", "ID Category harus berupa angka valid!");
        }
    }

    //UPDATE
    @FXML
    public void onUpdateBtnClick() {
        Category selected = tabelCategory.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningAlert("Aksi Ditolak", "Pilih data pada tabel yang ingin diubah!");
            return;
        }

        try {
            String name = txtnamaCategory.getText().trim();

            String query = "UPDATE public.category SET nama_category = ? WHERE id_category = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setString(1, name);
                stmt.setInt(2, selected.getId());

                stmt.executeUpdate();

                loadDataDariDatabase();
                clearForm();
                showInformationAlert("Sukses", "Kategori berhasil diperbarui!");

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
        Category selected = tabelCategory.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningAlert("Aksi Ditolak", "Pilih data pada tabel yang ingin dihapus!");
            return;
        }

        String query = "DELETE FROM public.category WHERE id_category = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, selected.getId());
            stmt.executeUpdate();

            loadDataDariDatabase();
            clearForm();
            showInformationAlert("Sukses", "Kategori berhasil dihapus!");

        } catch (SQLException e) {
            showErrorAlert("Database Error", "Gagal menghapus data: " + e.getMessage());
        }
    }

    //SEARCH
    @FXML
    public void onSearchBtnClick() {
        String kataKunci = txtCari.getText().toLowerCase().trim();
        ObservableList<Category> hasilFilter = FXCollections.observableArrayList();

        for (Category cat : daftarCategory) {
            if (cat.getName().toLowerCase().contains(kataKunci)) {
                hasilFilter.add(cat);
            }
        }
        tabelCategory.setItems(hasilFilter);
    }

    //RESET
    @FXML
    public void onResetBtnClick() {
        txtCari.clear();
        tabelCategory.setItems(daftarCategory);
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
        txtidCategory.clear();
        txtidCategory.setDisable(false);
        txtnamaCategory.clear();
        tabelCategory.getSelectionModel().clearSelection();
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