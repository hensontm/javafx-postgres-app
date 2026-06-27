package org.example.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class MasterController {
    //Id
    @FXML
    Button btn_employee;
    @FXML
    Button btn_branch;
    @FXML
    Button btn_inventory;
    @FXML
    Button btn_customer;
    @FXML
    Button btn_payment;
    @FXML
    Button btn_customization;
    @FXML
    Button btn_menu;
    @FXML
    Button btn_category;
    @FXML
    Button btn_back;

    private Stage stage;
    private Scene scene;
    private Parent root;

    //Back button
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

    //Employee
    @FXML
    public void goEmployee(ActionEvent e) throws IOException{
        root = FXMLLoader.load(getClass().getResource("employee-view.fxml"));
        stage = (Stage) ((Node)e.getSource()).getScene().getWindow();
        scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
    }

    //Branch
    @FXML
    public void goBranch(ActionEvent e) throws IOException{
        root = FXMLLoader.load(getClass().getResource("branch-view.fxml"));
        stage = (Stage) ((Node)e.getSource()).getScene().getWindow();
        scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
    }

    //Inventory
    @FXML
    public void goInventory(ActionEvent e) throws IOException{
        root = FXMLLoader.load(getClass().getResource("inventory-view.fxml"));
        stage = (Stage) ((Node)e.getSource()).getScene().getWindow();
        scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
    }

    //Customer
    @FXML
    public void goCustomer(ActionEvent e) throws IOException{
        root = FXMLLoader.load(getClass().getResource("customer-view.fxml"));
        stage = (Stage) ((Node)e.getSource()).getScene().getWindow();
        scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
    }

    //Payment
    @FXML
    public void goPayment(ActionEvent e) throws IOException{
        root = FXMLLoader.load(getClass().getResource("payment-view.fxml"));
        stage = (Stage) ((Node)e.getSource()).getScene().getWindow();
        scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
    }

    //Customization
    @FXML
    public void goCustomization(ActionEvent e) throws IOException{
        root = FXMLLoader.load(getClass().getResource("customization-view.fxml"));
        stage = (Stage) ((Node)e.getSource()).getScene().getWindow();
        scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
    }

    //Menu
    @FXML
    public void goMenu(ActionEvent e) throws IOException{
        root = FXMLLoader.load(getClass().getResource("menu-view.fxml"));
        stage = (Stage) ((Node)e.getSource()).getScene().getWindow();
        scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
    }

    //Category
    @FXML
    public void goCategory(ActionEvent e) throws IOException{
        root = FXMLLoader.load(getClass().getResource("category-view.fxml"));
        stage = (Stage) ((Node)e.getSource()).getScene().getWindow();
        scene = new Scene(root);

        stage.setScene(scene);
        stage.show();
    }

}