package org.example.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class TransaksiHomeController {
    //Id
    @FXML Button btn_inputTransaksi;
    @FXML Button btn_lihatTransaksi;
    @FXML Button btn_back;

    private Stage stage;
    private Scene scene;
    private Parent root;

    //BACK
    public void goBack(ActionEvent event) throws IOException {
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        boolean isCurrentlyMaximized = stage.isMaximized();

        root = FXMLLoader.load(getClass().getResource("hello-view.fxml"));
        scene = new Scene(root);

        stage.setScene(scene);
        stage.setMaximized(isCurrentlyMaximized);
        stage.show();
    }

    //Input Transaksi
    @FXML
    public void goInputTransaksi(ActionEvent e) throws IOException {
        stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        boolean isCurrentlyMaximized = stage.isMaximized();

        root = FXMLLoader.load(getClass().getResource("transaksi-view.fxml"));
        scene = new Scene(root);

        stage.setScene(scene);
        stage.setMaximized(isCurrentlyMaximized);
        stage.show();
    }

    //Lihat Transaksi
    @FXML
    public void goLihatTransaksi(ActionEvent e) throws IOException {
        stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        boolean isCurrentlyMaximized = stage.isMaximized();

        root = FXMLLoader.load(getClass().getResource("kelola-transaksi-view.fxml"));
        scene = new Scene(root);

        stage.setScene(scene);
        stage.setMaximized(isCurrentlyMaximized);
        stage.show();
    }
}