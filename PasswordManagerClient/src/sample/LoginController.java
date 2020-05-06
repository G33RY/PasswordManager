package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import sample.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LoginController {
    @FXML private TextField username;
    @FXML private PasswordField password;
    @FXML private Label errors;

    public static User user;

    @FXML
    public void login(ActionEvent event){
        try{
            //Create user
            user = new User();

            //Validate login
            if(user.login(username.getText(), password.getText())){
                //Change Scene to the main
                Parent main = FXMLLoader.load(getClass().getResource("main.fxml"));
                Stage stage = (Stage)username.getScene().getWindow();
                stage.setScene(new Scene(main, 820, 700));
                stage.setTitle("Password Manager - " + user.getUsername());
                stage.show();

                System.out.println("logged in as " + user.getUsername());
            }else{
                errors.setText("Wrong username or password");
                System.out.println("cant log in");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
