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
    @FXML private TextField txtHargaMenu;
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
        colId.setCellValueFactory(new PropertyValueFactory<>("idMenu"));
        colName.setCellValueFactory(new PropertyValueFactory<>("namaMenu"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("hargaMenu"));
        colIdCategory.setCellValueFactory(new PropertyValueFactory<>("idCategory"));

        tabelMenu.setItems(daftarMenu);

        //Load data PosgreSQL
        loadDataDariDatabase();

        //Kalau diklik, formnya keisi
        tabelMenu.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                txtidMenu.setText(String.valueOf(newSelection.getIdMenu()));
                txtidMenu.setDisable(true); //ID tidak berubah saat UPDATE
                txtnamaMenu.setText(newSelection.getNamaMenu());
                txtHargaMenu.setText(String.valueOf(newSelection.getHargaMenu()));
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
            if (txtidMenu.getText().trim().isEmpty() || txtnamaMenu.getText().trim().isEmpty() ||
                    txtHargaMenu.getText().trim().isEmpty() || txtidCategory.getText().trim().isEmpty()) {
                showWarningAlert("Input Kosong", "Semua kolom data wajib diisi!");
                return;
            }

            int id = Integer.parseInt(txtidMenu.getText().trim());
            String name = txtnamaMenu.getText().trim();
            double price = Double.parseDouble(txtHargaMenu.getText().trim());
            int idCategory = Integer.parseInt(txtidCategory.getText().trim());

            // CONSTRAINT menu_harga_menu_ck memastikan nominal harga menu wajib di atas nol (> 0)
            if (price <= 0) {
                showWarningAlert("Pelanggaran Constraint", "Harga menu harus lebih besar dari 0!");
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
                // CONSTRAINT menu_nama_menu_uq atau menu_id_category_fk validasi data unik dan kecocokan foreign key id kategori
                showErrorAlert("Database Error", "Gagal menyimpan data akibat pelanggaran constraint: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Format Salah", "ID/Harga harus diisi angka!");
        }
    }

    //UPDATE
    @FXML
    public void onUpdateBtnClick() {
        Menu selected = tabelMenu.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningAlert("Aministrasi Ditolak", "Pilih data pada tabel yang ingin diubah!");
            return;
        }

        try {
            String name = txtnamaMenu.getText().trim();
            double price = Double.parseDouble(txtHargaMenu.getText().trim());
            int idCategory = Integer.parseInt(txtidCategory.getText().trim());

            // CONSTRAINT menu_harga_menu_ck memastikan nilai harga baru berada di atas nol (> 0)
            if (price <= 0) {
                showWarningAlert("Pelanggaran Constraint", "Harga menu harus lebih besar dari 0!");
                return;
            }

            String query = "UPDATE public.menu SET nama_menu = ?, harga_menu = ?, id_category = ? WHERE id_menu = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setString(1, name);
                stmt.setDouble(2, price);
                stmt.setInt(3, idCategory);
                stmt.setInt(4, selected.getIdMenu());

                stmt.executeUpdate();

                loadDataDariDatabase();
                clearForm();
                showInformationAlert("Sukses", "Data menu berhasil diperbarui!");

            } catch (SQLException e) {
                // CONSTRAINT Mengunci validitas keunikan nama produk menu agar tidak terjadi bentrok duplikasi data
                showErrorAlert("Database Error", "Gagal memperbarui data akibat constraint error: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Input Salah", "Nominal harga dan Category ID wajib menggunakan format data angka!");
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

            stmt.setInt(1, selected.getIdMenu());
            stmt.executeUpdate();

            loadDataDariDatabase();
            clearForm();
            showInformationAlert("Sukses", "Data menu berhasil dihapus!");

        } catch (SQLException e) {
            // CONSTRAINT order_detail_id_menu_fk atau inventory_id_menu_fk memblokir proses hapus (Restrict) jika menu ini masih dipakai di transaksi
            showErrorAlert("Database Error", "Gagal menghapus data: " + e.getMessage());
        }
    }

    //SEARCH
    @FXML
    public void onSearchBtnClick() {
        String kataKunci = txtCari.getText().toLowerCase().trim();
        daftarMenu.clear();

        // JOIN Menghubungkan tabel menu dengan tabel category produk untuk memfilter data pencarian secara relasional komprehensif
        String query = "SELECT m.* FROM public.menu m JOIN public.category c ON m.id_category = c.id_category " +
                "WHERE LOWER(m.nama_menu) LIKE ? OR LOWER(c.nama_category) LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + kataKunci + "%");
            stmt.setString(2, "%" + kataKunci + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    daftarMenu.add(new Menu(
                            rs.getInt("id_menu"),
                            rs.getString("nama_menu"),
                            rs.getDouble("harga_menu"),
                            rs.getInt("id_category")
                    ));
                }
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Gagal memfilter pencarian data: " + e.getMessage());
        }
    }

    // Menampilkan rangkuman performa statistik klasifikasi harga rata-rata menu kafe
    @FXML
    public void onShowStatisticClick() {
        // AGGREGATION Menghitung nilai nilai total rata-rata harga produk menu menggunakan fungsi statistik AVG()
        // SUBQUERRY klausa subquery bertingkat menyeleksi produk menu kafe yang nilai jualnya berada di atas rata-rata nasional kafe
        String query = "SELECT COUNT(id_menu) FROM public.menu WHERE harga_menu > (SELECT AVG(harga_menu) FROM public.menu)";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                int jumlahMenuPremium = rs.getInt(1);
                showInformationAlert("Rangkuman Agregasi", "Jumlah item menu premium (di atas harga rata-rata): " + jumlahMenuPremium + " produk.");
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Gagal memproses perhitungan statistik agregasi menu: " + e.getMessage());
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
        txtidMenu.clear();
        txtidMenu.setDisable(false);
        txtnamaMenu.clear();
        txtHargaMenu.clear();
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