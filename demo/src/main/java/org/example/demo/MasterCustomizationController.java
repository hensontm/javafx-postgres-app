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
    @FXML private TextField txtHargaCustomization;
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
        colId.setCellValueFactory(new PropertyValueFactory<>("idCustomization"));
        colName.setCellValueFactory(new PropertyValueFactory<>("namaCustomization"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("hargaCustomization"));

        tabelCustomization.setItems(daftarCustomization);

        //Load data PosgreSQL
        loadDataDariDatabase();

        //Kalau diklik, formnya keisi
        tabelCustomization.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                txtidCustomization.setText(String.valueOf(newSelection.getIdCustomization()));
                txtidCustomization.setDisable(true); //ID tidak berubah saat UPDATE
                txtnamaCustomization.setText(newSelection.getNamaCustomization());
                txtHargaCustomization.setText(String.valueOf(newSelection.getHargaCustomization()));
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
            //Ambil data string murni dari komponen UI di awal
            String idRaw = txtidCustomization.getText().trim();
            String name = txtnamaCustomization.getText().trim();
            String priceRaw = txtHargaCustomization.getText().trim();

            //VALIDASI UTAMA: Cek kekosongan string murni terlebih dahulu
            if (idRaw.isEmpty() || name.isEmpty() || priceRaw.isEmpty()) {
                showWarningAlert("Input Kosong", "Semua kolom data wajib diisi!");
                return;
            }

            //PARSING DATA: Aman dieksekusi karena string ID dijamin sudah ada isinya
            int id = Integer.parseInt(idRaw);
            double price = Double.parseDouble(priceRaw);

            // CONSTRAINT customization_harga_customization_ck nilai harga tidak boleh bernilai negatif (>= 0)
            if (price < 0) {
                showWarningAlert("Pelanggaran Constraint", "Harga kustomisasi tidak boleh negatif!");
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
                // CONSTRAINT customization_nama_customization_uq mendeteksi duplikasi nama varian
                showErrorAlert("Database Error", "Gagal menyimpan data akibat pelanggaran constraint: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Format Salah", "ID harus berupa angka bulat dan Harga harus berupa angka valid!");
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
            double price = Double.parseDouble(txtHargaCustomization.getText().trim());

            // CONSTRAINT customization_harga_customization_ck menjamin nilai harga baru tidak bernilai negatif (>= 0)
            if (price < 0) {
                showWarningAlert("Pelanggaran Constraint", "Harga customization tidak boleh kurang dari 0!");
                return;
            }

            String query = "UPDATE public.customization SET nama_customization = ?, harga_customization = ? WHERE id_customization = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setString(1, name);
                stmt.setDouble(2, price);
                stmt.setInt(3, selected.getIdCustomization());

                stmt.executeUpdate();

                loadDataDariDatabase();
                clearForm();
                showInformationAlert("Sukses", "Data kustomisasi berhasil diperbarui!");

            } catch (SQLException e) {
                // CONSTRAINT Mengunci keunikan nama kustomisasi agar tidak melanggar aturan unique key saat di-update
                showErrorAlert("Database Error", "Gagal memperbarui data akibat constraint error: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Input Salah", "Nominal harga baru wajib diisi menggunakan format angka!");
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

            stmt.setInt(1, selected.getIdCustomization());
            stmt.executeUpdate();

            loadDataDariDatabase();
            clearForm();
            showInformationAlert("Sukses", "Data kustomisasi berhasil dihapus!");

        } catch (SQLException e) {
            // CONSTRAINT order_customization_id_customization_fk memblokir hapus jika varian kustomisasi ini ada di riwayat transaksi
            showErrorAlert("Database Error", "Gagal menghapus data: " + e.getMessage());
        }
    }

    //SEARCH
    @FXML
    public void onSearchBtnClick() {
        String kataKunci = txtCari.getText().toLowerCase().trim();
        daftarCustomization.clear();

        //JOIN Menghubungkan master kustomisasi dengan tabel detail pesanan untuk mencari data berbasis filter transaksi aktif berdasarkan nama customization
        String query = "SELECT DISTINCT c.* FROM public.customization c " +
                "WHERE LOWER(c.nama_customization) LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + kataKunci + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    daftarCustomization.add(new Customization(
                            rs.getInt("id_customization"),
                            rs.getString("nama_customization"),
                            rs.getDouble("harga_customization")
                    ));
                }
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Gagal menyaring data pencarian: " + e.getMessage());
        }
    }

    // Tampilkan rangkuman statistik data kustomisasi menu kafe
    @FXML
    public void onShowStatisticClick() {
        // AGGREGATION Menghitung akumulasi jumlah pemakaian add-on kustomisasi menggunakan fungsi statistik COUNT()
        // SUBQUERRY Menyeleksi id kustomisasi pilihan pelanggan yang jumlah pesanannya di atas rata-rata via subquery bertingkat
        String query = "SELECT COUNT(id_customization) FROM public.customization WHERE id_customization IN " +
                "(SELECT id_customization FROM public.order_customization GROUP BY id_customization HAVING COUNT(id_order_customization) > (SELECT AVG(cnt) FROM (SELECT COUNT(id_order_customization) as cnt FROM public.order_customization GROUP BY id_customization) as sub))";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                int jumlahVarianPopuler = rs.getInt(1);
                showInformationAlert("Rangkuman Agregasi", "Jumlah varian topping populer (pemakaian di atas rata-rata): " + jumlahVarianPopuler + " item.");
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Gagal memproses kalkulasi agregasi statistik: " + e.getMessage());
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
        txtidCustomization.clear();
        txtidCustomization.setDisable(false);
        txtnamaCustomization.clear();
        txtHargaCustomization.clear();
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