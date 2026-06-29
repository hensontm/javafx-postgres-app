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

public class MasterEmployeeController {

    //ID
    @FXML private TextField txtidEmployee;
    @FXML private TextField txtnamaEmployee;
    @FXML private ComboBox<String> comboPosition;
    @FXML private TextField txtSalary;
    @FXML private TextField txtTanggalLahir;
    @FXML private TextField txtAlamat;
    @FXML private TextField txtidBranch;
    @FXML private TextField txtTelp;
    @FXML private TextField txtCari;

    @FXML private TableView<Employee> tabelEmployee;
    @FXML private TableColumn<Employee, Integer> colId;
    @FXML private TableColumn<Employee, String> colName;
    @FXML private TableColumn<Employee, String> colPosition;
    @FXML private TableColumn<Employee, Double> colSalary;
    @FXML private TableColumn<Employee, Date> colBirthDate;
    @FXML private TableColumn<Employee, String> colAddress;
    @FXML private TableColumn<Employee, Integer> colIdBranch;
    @FXML private TableColumn<Employee, String> colPhone;

    private Parent root;
    private Stage stage;
    private Scene scene;

    //Observable List
    private final ObservableList<Employee> daftarEmployee = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        //Connect nilai ke kolom table view
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPosition.setCellValueFactory(new PropertyValueFactory<>("position"));
        colSalary.setCellValueFactory(new PropertyValueFactory<>("salary"));
        colBirthDate.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
        colAddress.setCellValueFactory(new PropertyValueFactory<>("address"));
        colIdBranch.setCellValueFactory(new PropertyValueFactory<>("idBranch"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));

        tabelEmployee.setItems(daftarEmployee);

        //Isi combobox
        comboPosition.getItems().addAll("Manager", "Barista"); // CONSTRAINT employee_jabatan_employee_ck wajib 'Manager' atau 'Barista'

        //Load data PosgreSQL
        loadDataDariDatabase();

        //Kalau diklik, formnya keisi
        tabelEmployee.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                txtidEmployee.setText(String.valueOf(newSelection.getId()));
                txtidEmployee.setDisable(true); //ID tidak berubah saat UPDATE
                txtnamaEmployee.setText(newSelection.getName());
                comboPosition.setValue(newSelection.getPosition());
                txtSalary.setText(String.valueOf(newSelection.getSalary()));
                txtTanggalLahir.setText(String.valueOf(newSelection.getBirthDate()));
                txtAlamat.setText(newSelection.getAddress());
                txtidBranch.setText(String.valueOf(newSelection.getIdBranch()));
                txtTelp.setText(newSelection.getPhone());
            }
        });
    }

    //Narik data PosgreSQL
    private void loadDataDariDatabase() {
        daftarEmployee.clear();
        String query = "SELECT id_employee, nama_employee, jabatan_employee, gaji_employee, tanggal_lahir_employee, alamat_employee, id_branch, telp_employee FROM public.employee ORDER BY id_employee ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Employee emp = new Employee(
                        rs.getInt("id_employee"),
                        rs.getString("nama_employee"),
                        rs.getString("jabatan_employee"),
                        rs.getDouble("gaji_employee"),
                        rs.getDate("tanggal_lahir_employee"),
                        rs.getString("alamat_employee"),
                        rs.getInt("id_branch"),
                        rs.getString("telp_employee")
                );
                daftarEmployee.add(emp);
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Gagal memuat data dari database: " + e.getMessage());
        }
    }

    //ADD
    @FXML
    public void onAddBtnClick() {
        try {
            //Ambil data string murni dari komponen UI dulu
            String idRaw = txtidEmployee.getText().trim();
            String name = txtnamaEmployee.getText().trim();
            String position = comboPosition.getValue();
            String salaryRaw = txtSalary.getText().trim();
            String birthDateRaw = txtTanggalLahir.getText().trim();
            String address = txtAlamat.getText().trim();
            String idBranchRaw = txtidBranch.getText().trim();
            String phone = txtTelp.getText().trim();

            //VALIDASI UTAMA: Cek kekosongan string murni terlebih dahulu!
            if (idRaw.isEmpty() || name.isEmpty() || position == null || salaryRaw.isEmpty() ||
                    birthDateRaw.isEmpty() || address.isEmpty() || idBranchRaw.isEmpty() || phone.isEmpty()) {
                showWarningAlert("Input Kosong", "Semua kolom data wajib diisi!");
                return; // Berhenti di sini kalau ada yang kosong
            }

            //PARSING DATA: Aman dieksekusi karena string dijamin sudah ada isinya
            int id = Integer.parseInt(idRaw);
            double salary = Double.parseDouble(salaryRaw);
            Date birthDate = Date.valueOf(birthDateRaw); // Tidak akan crash karena string tidak kosong
            int idBranch = Integer.parseInt(idBranchRaw);

            //CONSTRAINT employee_gaji_employee_ck nilai gaji tidak boleh bernilai negatif (>= 0)
            if (salary < 0) {
                showWarningAlert("Pelanggaran Constraint", "Gaji tidak boleh negatif!");
                return;
            }

            String query = "INSERT INTO public.employee (id_employee, nama_employee, jabatan_employee, gaji_employee, tanggal_lahir_employee, alamat_employee, id_branch, telp_employee) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, id);
                stmt.setString(2, name);
                stmt.setString(3, position);
                stmt.setDouble(4, salary);
                stmt.setDate(5, birthDate);
                stmt.setString(6, address);
                stmt.setInt(7, idBranch);
                stmt.setString(8, phone);

                stmt.executeUpdate();

                loadDataDariDatabase();
                clearForm();
                showInformationAlert("Sukses", "Data karyawan berhasil ditambahkan!");

            } catch (SQLException e) {
                // CONSTRAINT employee_id_branch_fk validasi foreign key branch dan employee_telp_employee_uq unique phone
                showErrorAlert("Database Error", "Gagal menyimpan data: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Format Salah", "ID, Gaji, dan ID Branch harus berupa angka valid!");
        } catch (IllegalArgumentException e) {
            showErrorAlert("Format Salah", "Format Tanggal Lahir harus YYYY-MM-DD!");
        }
    }

    //UPDATE
    @FXML
    public void onUpdateBtnClick() {
        Employee selected = tabelEmployee.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningAlert("Aksi Ditolak", "Pilih data pada tabel yang ingin diubah!");
            return;
        }

        try {
            //Ambil data string murni dari komponen UI di awal
            String name = txtnamaEmployee.getText().trim();
            String position = comboPosition.getValue();
            String salaryRaw = txtSalary.getText().trim();
            String birthDateRaw = txtTanggalLahir.getText().trim();
            String address = txtAlamat.getText().trim();
            String idBranchRaw = txtidBranch.getText().trim();
            String phone = txtTelp.getText().trim();

            //VALIDASI UTAMA: Cek kekosongan string murni dan ComboBox terlebih dahulu
            if (name.isEmpty() || position == null || salaryRaw.isEmpty() || birthDateRaw.isEmpty() ||
                    address.isEmpty() || idBranchRaw.isEmpty() || phone.isEmpty()) {
                showWarningAlert("Input Kosong", "Semua kolom data wajib diisi!");
                return;
            }

            //PARSING DATA: Aman dieksekusi karena string dijamin sudah ada isinya
            double salary = Double.parseDouble(salaryRaw);
            int idBranch = Integer.parseInt(idBranchRaw);
            Date birthDate = Date.valueOf(birthDateRaw);

            // CONSTRAINT employee_gaji_employee_ck memastikan nilai gaji baru valid (>= 0)
            if (salary < 0) {
                showWarningAlert("Pelanggaran Constraint", "Gaji tidak boleh negatif!");
                return;
            }

            String query = "UPDATE public.employee SET nama_employee = ?, jabatan_employee = ?, gaji_employee = ?, tanggal_lahir_employee = ?, alamat_employee = ?, id_branch = ?, telp_employee = ? WHERE id_employee = ?";

            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setString(1, name);
                stmt.setString(2, position);
                stmt.setDouble(3, salary);
                stmt.setDate(4, birthDate);
                stmt.setString(5, address);
                stmt.setInt(6, idBranch);
                stmt.setString(7, phone);
                stmt.setInt(8, selected.getId());

                stmt.executeUpdate();

                loadDataDariDatabase();
                clearForm();
                showInformationAlert("Sukses", "Data karyawan berhasil diperbarui!");

            } catch (SQLException e) {
                // CONSTRAINT Memastikan update mematuhi aturan unique phone dan foreign key branch_id_branch_pk
                showErrorAlert("Database Error", "Gagal memperbarui data: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            showErrorAlert("Format Salah", "Gaji dan Branch ID harus berupa angka yang valid!");
        } catch (IllegalArgumentException e) {
            showErrorAlert("Format Salah", "Format Tanggal Lahir harus YYYY-MM-DD!");
        }
    }

    //DELETE
    @FXML
    public void onDeleteBtnClick() {
        Employee selected = tabelEmployee.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningAlert("Aksi Ditolak", "Pilih data pada tabel yang ingin dihapus!");
            return;
        }

        String query = "DELETE FROM public.employee WHERE id_employee = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, selected.getId());
            stmt.executeUpdate();

            loadDataDariDatabase();
            clearForm();
            showInformationAlert("Sukses", "Data karyawan berhasil dihapus!");

        } catch (SQLException e) {
            // CONSTRAINT customer_order_id_employee_fk restriksi jika id_employee masih dipakai di transaksi nota
            showErrorAlert("Database Error", "Gagal menghapus data: " + e.getMessage());
        }
    }

    //SEARCH
    @FXML
    public void onSearchBtnClick() {
        String kataKunci = txtCari.getText().toLowerCase().trim();
        daftarEmployee.clear();

        //JOIN Menghubungkan tabel employee dengan tabel branch untuk melakukan pencarian silang berdasarkan filter lokasi cabang berdasarkan nama employee atau jabatan employee atau nama canag
        String query = "SELECT e.* FROM public.employee e JOIN public.branch b ON e.id_branch = b.id_branch " +
                "WHERE LOWER(e.nama_employee) LIKE ? OR LOWER(e.jabatan_employee) LIKE ? OR LOWER(b.nama_branch) LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + kataKunci + "%");
            stmt.setString(2, "%" + kataKunci + "%");
            stmt.setString(3, "%" + kataKunci + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    daftarEmployee.add(new Employee(
                            rs.getInt("id_employee"),
                            rs.getString("nama_employee"),
                            rs.getString("jabatan_employee"),
                            rs.getDouble("gaji_employee"),
                            rs.getDate("tanggal_lahir_employee"),
                            rs.getString("alamat_employee"),
                            rs.getInt("id_branch"),
                            rs.getString("telp_employee")
                    ));
                }
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Gagal melakukan pencarian: " + e.getMessage());
        }
    }

    // Tampilkan informasi olahan data statistika performa gaji karyawan
    @FXML
    public void onShowStatisticClick() {
        // AGGREGATION Memanfaatkan fungsi AVG() untuk mencari rata-rata nominal gaji karyawan
        // SUBQUERRY klausa seleksi bertingkat mencari total karyawan dengan kriteria gaji di atas nilai rata-rata internal
        String query = "SELECT COUNT(id_employee) FROM public.employee WHERE gaji_employee > (SELECT AVG(gaji_employee) FROM public.employee)";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                int jumlahKaryawan = rs.getInt(1);
                showInformationAlert("Rangkuman Agregasi", "Jumlah karyawan dengan gaji di atas rata-rata: " + jumlahKaryawan + " orang.");
            }
        } catch (SQLException e) {
            showErrorAlert("Database Error", "Gagal memproses kalkulasi agregasi: " + e.getMessage());
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
        txtidEmployee.clear();
        txtidEmployee.setDisable(false);
        txtnamaEmployee.clear();
        comboPosition.setValue(null);
        txtSalary.clear();
        txtTanggalLahir.clear();
        txtAlamat.clear();
        txtidBranch.clear();
        txtTelp.clear();
        tabelEmployee.getSelectionModel().clearSelection();
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