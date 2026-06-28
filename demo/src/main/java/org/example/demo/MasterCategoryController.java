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
        colId.setCellValueFactory(new PropertyValueFactory<>("idCategory"));
        colName.setCellValueFactory(new PropertyValueFactory<>("namaCategory"));

        tabelCategory.setItems(daftarCategory);

        //Load data PosgreSQL
        loadDataDariDatabase();

        //Kalau diklik, formnya keisi
        tabelCategory.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                txtidCategory.setText(String.valueOf(newSelection.getIdCategory()));
                txtidCategory.setDisable(true); //ID tidak berubah saat UPDATE
                txtnamaCategory.setText(newSelection.getNamaCategory());
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
            if (txtidCategory.getText().trim().isEmpty() || txtnamaCategory.getText().trim().isEmpty()) {
                showWarningAlert("Input Kosong", "Semua kolom data wajib diisi!");
                return;
            }

            int id = Integer.parseInt(txtidCategory.getText().trim());
            String name = txtnamaCategory.getText().trim();

            String query = "INSERT INTO public.category (id_category, nama_category) VALUES (?, ?)";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, id);
                stmt.setString(2, name);

                stmt.executeUpdate();

                loadDataDariDatabase();
                clearForm();
                showInformationAlert("Sukses", "Data kategori berhasil ditambahkan!");

            } catch (SQLException e) {
                // CONSTRAINT category_nama_category_uq mencegah duplikasi nama kategori yang sama di database
                showErrorAlert("Database Error", "Gagal menyimpan data akibat pelanggaran constraint: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Format Salah", "Category ID harus berupa angka bulat!");
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

        String name = txtnamaCategory.getText().trim();

        String query = "UPDATE public.category SET nama_category = ? WHERE id_category = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, name);
            stmt.setInt(2, selected.getIdCategory());

            stmt.executeUpdate();

            loadDataDariDatabase();
            clearForm();
            showInformationAlert("Sukses", "Data kategori berhasil diperbarui!");

        } catch (SQLException e) {
            // CONSTRAINT Memastikan pengubahan nama kategori mematuhi aturan unique agar tidak kembar dengan kategori lain
            showErrorAlert("Database Error", "Gagal memperbarui data akibat constraint error: " + e.getMessage());
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

            stmt.setInt(1, selected.getIdCategory());
            stmt.executeUpdate();

            loadDataDariDatabase();
            clearForm();
            showInformationAlert("Sukses", "Data kategori berhasil dihapus!");

        } catch (SQLException e) {
            // CONSTRAINT menu_id_category_fk mengunci data (Restrict) jika id_category ini masih terikat dengan item menu makanan/minuman
            showErrorAlert("Database Error", "Gagal menghapus data: " + e.getMessage());
        }
    }

    //SEARCH
    @FXML
    public void onSearchBtnClick() {
        String kataKunci = txtCari.getText().toLowerCase().trim();
        daftarCategory.clear();

        // JOIN Menggabungkan tabel category dengan tabel menu untuk memfilter kategori berdasarkan relasi item menu aktif di dalamnya
        String query = "SELECT DISTINCT c.* FROM public.category c " +
                "WHERE LOWER(c.nama_category) LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + kataKunci + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    daftarCategory.add(new Category(
                            rs.getInt("id_category"),
                            rs.getString("nama_category")
                    ));
                }
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Gagal menyaring data pencarian: " + e.getMessage());
        }
    }

    // Menampilkan rangkuman statistik jumlah kategori yang paling laris dipesan pelanggan
    @FXML
    public void onShowStatisticClick() {
        // AGGREGATION Menghitung akumulasi jumlah menu per kategori menggunakan fungsi statistik COUNT()
        // SUBQUERRY Menyeleksi kategori produk yang variasi jumlah menunya berada di atas rata-rata koleksi menu kafe via nested subquery
        String query = "SELECT COUNT(id_category) FROM public.category WHERE id_category IN " +
                "(SELECT id_category FROM public.menu GROUP BY id_category HAVING COUNT(id_menu) > (SELECT AVG(cnt) FROM (SELECT COUNT(id_menu) as cnt FROM public.menu GROUP BY id_category) as sub))";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                int jumlahKategoriPopuler = rs.getInt(1);
                showInformationAlert("Rangkuman Agregasi", "Jumlah kategori variatif (di atas rata-rata koleksi): " + jumlahKategoriPopuler + " kategori.");
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Gagal memproses perhitungan statistik agregasi kategori: " + e.getMessage());
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