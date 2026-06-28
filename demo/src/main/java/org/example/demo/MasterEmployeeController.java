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
        comboPosition.getItems().addAll("Manager", "Staff", "Supervisor", "Developer", "Barista");

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
            int id = Integer.parseInt(txtidEmployee.getText().trim());
            String name = txtnamaEmployee.getText().trim();
            String position = comboPosition.getValue();
            double salary = Double.parseDouble(txtSalary.getText().trim());
            Date birthDate = Date.valueOf(txtTanggalLahir.getText().trim());
            String address = txtAlamat.getText().trim();
            int idBranch = Integer.parseInt(txtidBranch.getText().trim());
            String phone = txtTelp.getText().trim();

            if (name.isEmpty() || position == null || address.isEmpty() || phone.isEmpty()) {
                showWarningAlert("Input Kosong", "Semua kolom data wajib diisi!");
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
                showErrorAlert("Database Error", "Gagal menyimpan data: " + e.getMessage());
            }
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
            String name = txtnamaEmployee.getText().trim();
            String position = comboPosition.getValue();
            double salary = Double.parseDouble(txtSalary.getText().trim());
            Date birthDate = Date.valueOf(txtTanggalLahir.getText().trim());
            String address = txtAlamat.getText().trim();
            int idBranch = Integer.parseInt(txtidBranch.getText().trim());
            String phone = txtTelp.getText().trim();

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
                showErrorAlert("Database Error", "Gagal memperbarui data: " + e.getMessage());
            }
        } catch (IllegalArgumentException e) {
            showErrorAlert("Input Salah", "Gaji/Branch ID harus angka dan format Tanggal Lahir harus YYYY-MM-DD!");
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
            showErrorAlert("Database Error", "Gagal menghapus data: " + e.getMessage());
        }
    }

    //SEARCH
    @FXML
    public void onSearchBtnClick() {
        String kataKunci = txtCari.getText().toLowerCase().trim();
        ObservableList<Employee> hasilFilter = FXCollections.observableArrayList();

        for (Employee emp : daftarEmployee) {
            if (emp.getName().toLowerCase().contains(kataKunci) ||
                    emp.getPosition().toLowerCase().contains(kataKunci) ||
                    emp.getAddress().toLowerCase().contains(kataKunci)) {
                hasilFilter.add(emp);
            }
        }
        tabelEmployee.setItems(hasilFilter);
    }

    //RESET
    @FXML
    public void onResetBtnClick() {
        txtCari.clear();
        tabelEmployee.setItems(daftarEmployee);
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