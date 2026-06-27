package org.example.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class TransaksiController {

    private Stage stage;
    private Scene scene;
    private Parent root;

    public void goBack(ActionEvent event) throws IOException {

        root = FXMLLoader.load(
                getClass().getResource("hello-view.fxml")
        );

        stage=(Stage)((Node)event.getSource())
                .getScene()
                .getWindow();

        scene=new Scene(root);

        stage.setScene(scene);
        stage.show();

    }

}