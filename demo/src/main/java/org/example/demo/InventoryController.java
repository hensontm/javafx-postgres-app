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

public class InventoryController {

    //ID
    @FXML private TextField txtidInventory;
    @FXML private TextField txtStok;
    @FXML private TextField txtidMenu;
    @FXML private TextField txtidBranch;
    @FXML private TextField txtCari;

    @FXML private TableView<Inventory> tabelInventory;
    @FXML private TableColumn<Inventory, Integer> colId;
    @FXML private TableColumn<Inventory, Integer> colStock;
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
        colId.setCellValueFactory(new PropertyValueFactory<>("idInventory"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stokInventory"));
        colIdMenu.setCellValueFactory(new PropertyValueFactory<>("idMenu"));
        colIdBranch.setCellValueFactory(new PropertyValueFactory<>("idBranch"));

        tabelInventory.setItems(daftarInventory);

        //Load data PosgreSQL
        loadDataDariDatabase();

        //Kalau diklik, formnya keisi
        tabelInventory.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                txtidInventory.setText(String.valueOf(newSelection.getIdInventory()));
                txtidInventory.setDisable(true); //ID tidak berubah saat UPDATE
                txtStok.setText(String.valueOf(newSelection.getStokInventory()));
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
            //Ambil data string murni dari komponen UI di awal
            String idRaw = txtidInventory.getText().trim();
            String stockRaw = txtStok.getText().trim();
            String idMenuRaw = txtidMenu.getText().trim();
            String idBranchRaw = txtidBranch.getText().trim();

            //VALIDASI UTAMA: Cek kekosongan string murni terlebih dahulu
            if (idRaw.isEmpty() || stockRaw.isEmpty() || idMenuRaw.isEmpty() || idBranchRaw.isEmpty()) {
                showWarningAlert("Input Kosong", "Semua kolom data wajib diisi!");
                return;
            }

            //PARSING DATA: Aman dieksekusi karena string ID dijamin sudah ada isinya
            int id = Integer.parseInt(idRaw);
            int stock = Integer.parseInt(stockRaw);
            int idMenu = Integer.parseInt(idMenuRaw);
            int idBranch = Integer.parseInt(idBranchRaw);

            // CONSTRAINT inventory_stok_inventory_ck nilai stok gudang tidak boleh bernilai kurang dari nol (>= 0)
            if (stock < 0) {
                showWarningAlert("Pelanggaran Constraint", "Stok barang tidak boleh negatif!");
                return;
            }

            String query = "INSERT INTO public.inventory (id_inventory, stok_inventory, id_menu, id_branch) VALUES (?, ?, ?, ?)";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, id);
                stmt.setInt(2, stock);
                stmt.setInt(3, idMenu);
                stmt.setInt(4, idBranch);

                stmt.executeUpdate();

                loadDataDariDatabase();
                clearForm();
                showInformationAlert("Sukses", "Data inventory berhasil ditambahkan!");

            } catch (SQLException e) {
                // CONSTRAINT inventory_id_menu_id_branch_uq validasi kombinasi menu-cabang unik dan foreign key integrity
                showErrorAlert("Database Error", "Gagal menyimpan data akibat pelanggaran constraint: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Format Salah", "Semua inputan wajib diisi dengan angka bulat!");
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
            //Ambil data string murni dari komponen UI di awal
            String stockRaw = txtStok.getText().trim();
            String idMenuRaw = txtidMenu.getText().trim();
            String idBranchRaw = txtidBranch.getText().trim();

            //VALIDASI UTAMA: Cek kekosongan string murni terlebih dahulu
            if (stockRaw.isEmpty() || idMenuRaw.isEmpty() || idBranchRaw.isEmpty()) {
                showWarningAlert("Input Kosong", "Semua kolom data wajib diisi!");
                return;
            }

            //PARSING DATA: Aman dieksekusi karena string dijamin sudah ada isinya
            int stock = Integer.parseInt(stockRaw);
            int idMenu = Integer.parseInt(idMenuRaw);
            int idBranch = Integer.parseInt(idBranchRaw);

            // CONSTRAINT inventory_stok_inventory_ck memastikan pembaruan nilai jumlah stok baru tidak minus (>= 0)
            if (stock < 0) {
                showWarningAlert("Pelanggaran Constraint", "Stok barang tidak boleh negatif!");
                return;
            }

            String query = "UPDATE public.inventory SET stok_inventory = ?, id_menu = ?, id_branch = ? WHERE id_inventory = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, stock);
                stmt.setInt(2, idMenu);
                stmt.setInt(3, idBranch);
                stmt.setInt(4, selected.getIdInventory());

                stmt.executeUpdate();

                loadDataDariDatabase();
                clearForm();
                showInformationAlert("Sukses", "Data inventory berhasil diperbarui!");

            } catch (SQLException e) {
                // CONSTRAINT Memastikan kombinasi menu dan cabang tidak duplikat melanggar aturan unique key database
                showErrorAlert("Database Error", "Gagal memperbarui data akibat constraint error: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Input Salah", "Input data stok, menu ID, dan branch ID wajib berupa angka!");
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

            stmt.setInt(1, selected.getIdInventory());
            stmt.executeUpdate();

            loadDataDariDatabase();
            clearForm();
            showInformationAlert("Sukses", "Data inventory berhasil dihapus!");

        } catch (SQLException e) {
            showErrorAlert("Database Error", "Gagal menghapus data: " + e.getMessage());
        }
    }

    //SEARCH
    @FXML
    public void onSearchBtnClick() {
        String kataKunci = txtCari.getText().toLowerCase().trim();
        daftarInventory.clear();

        //JOIN Menghubungkan tabel inventory dengan tabel menu dan branch untuk pencarian silang berbasis nama menu atau nama cabang
        String query = "SELECT i.* FROM public.inventory i " +
                "JOIN public.menu m ON i.id_menu = m.id_menu " +
                "JOIN public.branch b ON i.id_branch = b.id_branch " +
                "WHERE LOWER(m.nama_menu) LIKE ? OR LOWER(b.nama_branch) LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + kataKunci + "%");
            stmt.setString(2, "%" + kataKunci + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    daftarInventory.add(new Inventory(
                            rs.getInt("id_inventory"),
                            rs.getInt("stok_inventory"),
                            rs.getInt("id_menu"),
                            rs.getInt("id_branch")
                    ));
                }
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Gagal melakukan penyaringan pencarian data: " + e.getMessage());
        }
    }

    // Menampilkan rangkuman total stok yang berada di atas rata-rata kapasitas gudang nasional
    @FXML
    public void onShowStatisticClick() {
        // AGGREGATION Menghitung nilai total stok gudang menggunakan kombinasi fungsi statistika SUM()
        // SUBQUERRY Menyeleksi data stok cabang yang nilainya berada di atas ambang batas rata-rata internal via nested subquery
        String query = "SELECT SUM(stok_inventory) FROM public.inventory WHERE stok_inventory > (SELECT AVG(stok_inventory) FROM public.inventory)";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                int totalStokHebat = rs.getInt(1);
                showInformationAlert("Rangkuman Agregasi", "Total stok barang melimpah (di atas rata-rata): " + totalStokHebat + " item.");
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Gagal memproses pengolahan data statistik agregasi: " + e.getMessage());
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

        root = FXMLLoader.load(getClass().getResource("transaksi-home.fxml"));
        scene = new Scene(root);

        stage.setScene(scene);
        stage.setMaximized(isCurrentlyMaximized);
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