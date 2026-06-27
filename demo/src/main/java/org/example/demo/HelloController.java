package org.example.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloController {

    private Stage stage;
    private Scene scene;
    private Parent root;

    public void openMaster(ActionEvent event) throws IOException {

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

    public void openTransaksi(ActionEvent event) throws IOException {

        root = FXMLLoader.load(
                getClass().getResource("transaksi-view.fxml")
        );

        stage=(Stage)((Node)event.getSource())
                .getScene()
                .getWindow();

        scene=new Scene(root);

        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();

    }

    public void openReport(ActionEvent event) throws IOException {

        root = FXMLLoader.load(
                getClass().getResource("report-view.fxml")
        );

        stage=(Stage)((Node)event.getSource())
                .getScene()
                .getWindow();

        scene=new Scene(root);

        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();

    }

}