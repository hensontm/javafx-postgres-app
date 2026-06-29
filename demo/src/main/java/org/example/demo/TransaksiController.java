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

public class TransaksiController {

    //ID
    @FXML private TextField txtNamaCust;
    @FXML private TextField txtTelpCust;
    @FXML private TextField txtEmailCust; // Ditambahkan untuk melengkapi constraint NOT NULL & UNIQUE email_cust
    @FXML private ComboBox<String> comboBranch;
    @FXML private ComboBox<String> comboEmployee;
    @FXML private ComboBox<String> comboPayment;
    @FXML private TextField txtDiskon;

    @FXML private ComboBox<String> comboMenu; //Pilihan Menu
    @FXML private TextField txtQtyMenu;

    @FXML private ComboBox<String> comboCustomization; //Pilihan Customization
    @FXML private TextField txtQtyCustom;

    // Tabel Kecil (Kustomisasi Sementara untuk 1 Menu yang sedang aktif)
    @FXML private TableView<CartCustomization> tabelKustomisasiSementara;
    @FXML private TableColumn<CartCustomization, String> colCustNameTemp;
    @FXML private TableColumn<CartCustomization, Integer> colCustQtyTemp;
    @FXML private TableColumn<CartCustomization, Double> colCustPriceTemp;

    // Tabel Besar (Keranjang Belanja Utama CRUD)
    @FXML private TableView<CartItem> tabelKeranjangUtama;
    @FXML private TableColumn<CartItem, String> colMenuNameCart;
    @FXML private TableColumn<CartItem, Integer> colMenuQtyCart;
    @FXML private TableColumn<CartItem, String> colCustSummaryCart;
    @FXML private TableColumn<CartItem, Double> colTotalCart;

    @FXML private Label lblTotalBayar;

    private Stage stage;
    private Scene scene;
    private Parent root;

    // List Item sementara
    private final ObservableList<CartCustomization> listKustomisasiSementara = FXCollections.observableArrayList();
    private final ObservableList<CartItem> listKeranjangUtama = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        //Connect nilai ke kolom tabel sementara
        colCustNameTemp.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCustQtyTemp.setCellValueFactory(new PropertyValueFactory<>("qty"));
        colCustPriceTemp.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        tabelKustomisasiSementara.setItems(listKustomisasiSementara);

        //Connect nilai ke kolom tabel utama
        colMenuNameCart.setCellValueFactory(new PropertyValueFactory<>("menuName"));
        colMenuQtyCart.setCellValueFactory(new PropertyValueFactory<>("qty"));
        colCustSummaryCart.setCellValueFactory(new PropertyValueFactory<>("customizationSummary"));
        colTotalCart.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        tabelKeranjangUtama.setItems(listKeranjangUtama);

        //Load semua dropdown dari PosgreSQL
        loadDropdownBranch();
        loadDropdownEmployee();
        loadDropdownPayment();
        loadDropdownMenu();
        loadDropdownCustomization();

        // Listener dinamis agar total bayar terupdate real-time saat diskon diisi atau payment diubah
        txtDiskon.textProperty().addListener((observable, oldValue, newValue) -> updateTotalBayarLabel());
        comboPayment.valueProperty().addListener((observable, oldValue, newValue) -> updateTotalBayarLabel());
    }

    //Customization
    @FXML
    public void onTambahKustomisasiClick() {
        String selectedCustomName = comboCustomization.getValue();
        if (selectedCustomName == null || txtQtyCustom.getText().trim().isEmpty()) {
            showWarningAlert("Input Kosong", "Pilih kustomisasi dan isi jumlahnya!");
            return;
        }

        try {
            int qtyInput = Integer.parseInt(txtQtyCustom.getText().trim());

            //CONSTRAINT jumlah_order_customization > 0
            if (qtyInput < 0) {
                showWarningAlert("Pelanggaran Constraint", "Jumlah kustomisasi tidak boleh kurang dari 0!");
                return;
            }

            int customId = Integer.parseInt(selectedCustomName.split(" - ")[0]);
            String namaAsliCustom = selectedCustomName.split(" - ")[1];
            double hargaSatuan = getCustomPriceFromDB(customId);

            CartCustomization dataLama = null;
            for (CartCustomization c : listKustomisasiSementara) {
                if (c.getCustomId() == customId) {
                    dataLama = c;
                    break;
                }
            }

            if (qtyInput == 0) {
                if (dataLama != null) listKustomisasiSementara.remove(dataLama);
            } else {
                if (dataLama != null) {
                    dataLama.setQty(qtyInput);
                    dataLama.setTotalPrice(hargaSatuan * qtyInput);
                } else {
                    listKustomisasiSementara.add(new CartCustomization(customId, namaAsliCustom, qtyInput, hargaSatuan * qtyInput));
                }
            }
            tabelKustomisasiSementara.refresh();
        } catch (NumberFormatException e) {
            showWarningAlert("Format Salah", "Quantity kustomisasi harus berupa angka bulat!");
        }
    }

    //Tamabah Item ke Tabel/Keranjang Utama
    @FXML
    public void onTambahKeKeranjangClick() {
        String selectedMenuName = comboMenu.getValue();
        if (selectedMenuName == null || txtQtyMenu.getText().trim().isEmpty()) {
            showWarningAlert("Input Kosong", "Pilih menu dan isi jumlah beli!");
            return;
        }

        try {
            int qtyMenu = Integer.parseInt(txtQtyMenu.getText().trim());

            // Validasi constraint order_detail jumlah_detail > 0
            if (qtyMenu <= 0) {
                showWarningAlert("Pelanggaran Constraint", "Jumlah beli menu harus lebih dari 0!");
                return;
            }

            int menuId = Integer.parseInt(selectedMenuName.split(" - ")[0]);
            String namaAsliMenu = selectedMenuName.split(" - ")[1];
            double hargaMenu = getMenuPriceFromDB(menuId);

            CartItem newItem = new CartItem(menuId, namaAsliMenu, qtyMenu, hargaMenu);
            for (CartCustomization cc : listKustomisasiSementara) {
                newItem.addCustomization(cc);
            }

            listKeranjangUtama.add(newItem);

            listKustomisasiSementara.clear();
            comboMenu.setValue(null);
            txtQtyMenu.clear();
            comboCustomization.setValue(null);
            txtQtyCustom.clear();

            updateTotalBayarLabel();
        } catch (NumberFormatException e) {
            showWarningAlert("Format Salah", "Quantity menu harus berupa angka bulat!");
        }
    }

    @FXML
    public void onHapusItemKeranjangClick() {
        CartItem selected = tabelKeranjangUtama.getSelectionModel().getSelectedItem();
        if (selected != null) {
            listKeranjangUtama.remove(selected);
            updateTotalBayarLabel();
        }
    }

    private void updateTotalBayarLabel() {
        double total = 0;
        for (CartItem item : listKeranjangUtama) {
            total += item.getTotalPrice();
        }

        double diskonResto = 0.0;
        if (!txtDiskon.getText().trim().isEmpty()) {
            try {
                diskonResto = Double.parseDouble(txtDiskon.getText().trim());
            } catch (NumberFormatException e) { diskonResto = 0.0; }
        }

        double diskonPayment = 0.0;
        if (comboPayment.getValue() != null) {
            int idPayment = Integer.parseInt(comboPayment.getValue().split(" - ")[0]);
            diskonPayment = getPaymentDiscountFromDB(idPayment);
        }

        // Penerapan rumus perhitungan diskon bertingkat akumulatif dari database revisi baru
        double totalBayarFinal = total * ((100.0 - diskonResto) / 100.0) * ((100.0 - diskonPayment) / 100.0);
        totalBayarFinal = Math.round(totalBayarFinal * 100.0) / 100.0; // ROUND(..., 2)

        lblTotalBayar.setText("Rp " + totalBayarFinal);
    }

    //Commit ke PosgreSQL
    @FXML
    public void onAddTransactionFinalClick() {
        if (listKeranjangUtama.isEmpty() || txtNamaCust.getText().trim().isEmpty() || txtEmailCust.getText().trim().isEmpty() ||
                comboBranch.getValue() == null || comboEmployee.getValue() == null || comboPayment.getValue() == null) {
            showWarningAlert("Gagal Simpan", "Keranjang kosong atau identitas nota & email belum dilengkapi!");
            return;
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Menyesuaikan parameter input ke database sesuai dengan skema tabel customer yang baru
            int customerId = checkOrInsertCustomer(conn, txtNamaCust.getText().trim(), txtTelpCust.getText().trim(), txtEmailCust.getText().trim());

            int idBranch = Integer.parseInt(comboBranch.getValue().split(" - ")[0]);
            int idEmployee = Integer.parseInt(comboEmployee.getValue().split(" - ")[0]);
            int idPayment = Integer.parseInt(comboPayment.getValue().split(" - ")[0]);

            double diskonResto = txtDiskon.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(txtDiskon.getText().trim());
            // Validasi CHECK constraint (diskon_restoran >= 0 AND diskon_restoran <= 100)
            if (diskonResto < 0 || diskonResto > 100) {
                showWarningAlert("Pelanggaran Constraint", "Diskon restoran harus berada di rentang 0 s/d 100!");
                conn.rollback();
                return;
            }

            double totalOrder = 0;
            for (CartItem item : listKeranjangUtama) totalOrder += item.getTotalPrice();

            double diskonPayment = getPaymentDiscountFromDB(idPayment);
            double totalBayarFinal = totalOrder * ((100.0 - diskonResto) / 100.0) * ((100.0 - diskonPayment) / 100.0);
            totalBayarFinal = Math.round(totalBayarFinal * 100.0) / 100.0;

            int orderId = getNextId(conn, "SELECT COALESCE(MAX(id_order), 0) + 1 FROM public.customer_order");

            // Memperbaiki status dari 'Selasai' menjadi 'Selesai' agar lolos validasi CHECK Constraint public.customer_order
            String queryOrder = "INSERT INTO public.customer_order (id_order, tanggal_order, waktu_order, diskon_restoran, total_order, total_bayar, status_order, id_cust, id_branch, id_employee, id_metode_payment) VALUES (?, CURRENT_DATE, CURRENT_TIME, ?, ?, ?, 'Selesai', ?, ?, ?, ?)";

            try (PreparedStatement stmtOrder = conn.prepareStatement(queryOrder)) {
                stmtOrder.setInt(1, orderId);
                stmtOrder.setDouble(2, diskonResto);
                stmtOrder.setDouble(3, totalOrder);
                stmtOrder.setDouble(4, totalBayarFinal);
                stmtOrder.setInt(5, customerId);
                stmtOrder.setInt(6, idBranch);
                stmtOrder.setInt(7, idEmployee);
                stmtOrder.setInt(8, idPayment);
                stmtOrder.executeUpdate();
            }

            for (CartItem item : listKeranjangUtama) {
                int detailId = getNextId(conn, "SELECT COALESCE(MAX(id_detail), 0) + 1 FROM public.order_detail");
                String queryDetail = "INSERT INTO public.order_detail (id_detail, id_order, id_menu, jumlah_detail) VALUES (?, ?, ?, ?)";

                try (PreparedStatement stmtDetail = conn.prepareStatement(queryDetail)) {
                    stmtDetail.setInt(1, detailId);
                    stmtDetail.setInt(2, orderId);
                    stmtDetail.setInt(3, item.getMenuId());
                    stmtDetail.setInt(4, item.getQty());
                    stmtDetail.executeUpdate();
                }

                for (CartCustomization custom : item.getCustomizations()) {
                    int orderCustomId = getNextId(conn, "SELECT COALESCE(MAX(id_order_customization), 0) + 1 FROM public.order_customization");
                    String queryCust = "INSERT INTO public.order_customization (id_order_customization, id_detail, id_customization, jumlah_order_customization, total_order_customization) VALUES (?, ?, ?, ?, ?)";

                    try (PreparedStatement stmtCust = conn.prepareStatement(queryCust)) {
                        stmtCust.setInt(1, orderCustomId);
                        stmtCust.setInt(2, detailId);
                        stmtCust.setInt(3, custom.getCustomId());
                        stmtCust.setInt(4, custom.getQty());
                        stmtCust.setDouble(5, custom.getTotalPrice());
                        stmtCust.executeUpdate();
                    }
                }
            }

            conn.commit();
            showInformationAlert("Sukses", "Transaksi Berhasil Disimpan ke PostgreSQL!");
            clearFullForm();

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            showErrorAlert("Database Constraint Error", "Gagal menyimpan transaksi! Periksa kelayakan constraint data.\nDetail: " + e.getMessage());
        }
    }

    // Menyesuaikan proses pencarian & insert data customer berdasarkan nama kolom skema asli DDL
    private int checkOrInsertCustomer(Connection conn, String nama, String telp, String email) throws SQLException {
        String checkQuery = "SELECT id_cust FROM public.customer WHERE LOWER(nama_cust) = LOWER(?) OR telp_cust = ? OR email_cust = ?";
        try (PreparedStatement stmt = conn.prepareStatement(checkQuery)) {
            stmt.setString(1, nama);
            stmt.setString(2, telp);
            stmt.setString(3, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("id_cust");
            }
        }
        int newCustId = getNextId(conn, "SELECT COALESCE(MAX(id_cust), 0) + 1 FROM public.customer");
        String insertQuery = "INSERT INTO public.customer (id_cust, nama_cust, telp_cust, email_cust) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
            stmt.setInt(1, newCustId);
            stmt.setString(2, nama);
            stmt.setString(3, telp);
            stmt.setString(4, email);
            stmt.executeUpdate();
        }
        return newCustId;
    }

    private int getNextId(Connection conn, String sqlQuery) throws SQLException {
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sqlQuery)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 1;
    }

    private double getPaymentDiscountFromDB(int idPayment) {
        String query = "SELECT diskon_metode_payment FROM public.metode_payment WHERE id_metode_payment = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, idPayment);
            try (ResultSet rs = stmt.executeQuery()) { if (rs.next()) return rs.getDouble(1); }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    //Tampilkan Data ComboBox dari PosgreSQL
    private void loadDropdownBranch() {
        loadGenericDropdown("SELECT id_branch, nama_branch FROM public.branch ORDER BY id_branch", comboBranch);
    }

    private void loadDropdownEmployee() {
        loadGenericDropdown("SELECT id_employee, nama_employee FROM public.employee ORDER BY id_employee", comboEmployee);
    }

    private void loadDropdownPayment() {
        comboPayment.getItems().clear();
        String query = "SELECT id_metode_payment, metode_payment, diskon_metode_payment FROM public.metode_payment ORDER BY id_metode_payment ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Payment p = new Payment(
                        rs.getInt("id_metode_payment"),
                        rs.getString("metode_payment"),
                        rs.getDouble("diskon_metode_payment")
                );
                comboPayment.getItems().add(p.getId() + " - " + p.getMethod());
            }
        } catch (SQLException e) {
            showErrorAlert("SQL Error Payment", "Gagal memuat metode pembayaran: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadDropdownMenu() {
        loadGenericDropdown("SELECT id_menu, nama_menu FROM public.menu ORDER BY id_menu", comboMenu);
    }

    private void loadDropdownCustomization() {
        loadGenericDropdown("SELECT id_customization, nama_customization FROM public.customization ORDER BY id_customization", comboCustomization);
    }

    private void loadGenericDropdown(String query, ComboBox<String> comboBox) {
        comboBox.getItems().clear();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                comboBox.getItems().add(rs.getInt(1) + " - " + rs.getString(2));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private double getMenuPriceFromDB(int id) {
        String query = "SELECT harga_menu FROM public.menu WHERE id_menu = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) { if (rs.next()) return rs.getDouble(1); }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    private double getCustomPriceFromDB(int id) {
        String query = "SELECT harga_customization FROM public.customization WHERE id_customization = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) { if (rs.next()) return rs.getDouble(1); }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0.0;
    }

    //BACK
    @FXML
    public void goBack(ActionEvent event) throws IOException {
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        boolean isCurrentlyMaximized = stage.isMaximized();
        root = FXMLLoader.load(getClass().getResource("/org/example/demo/transaksi-home.fxml"));
        scene = new Scene(root);
        stage.setScene(scene);
        stage.setMaximized(isCurrentlyMaximized);
        stage.show();
    }

    private void clearFullForm() {
        txtNamaCust.clear(); txtTelpCust.clear(); txtEmailCust.clear(); txtDiskon.clear();
        comboBranch.setValue(null); comboEmployee.setValue(null); comboPayment.setValue(null);
        listKustomisasiSementara.clear(); listKeranjangUtama.clear(); lblTotalBayar.setText("Rp 0");
    }

    private void showWarningAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content); alert.showAndWait();
    }
    private void showInformationAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content); alert.showAndWait();
    }
    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content); alert.showAndWait();
    }
}