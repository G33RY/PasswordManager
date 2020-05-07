package com.imregery;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML public TextField username;
    @FXML public PasswordField password;
    @FXML public Label errors;

    public static User user;

    @FXML
    public void login(ActionEvent event){
        try{
            //Create user
            user = new User();

            //Validate login
            if(user.login(username.getText(), password.getText())){
                //Change Scene to the main
                Stage stage = (Stage)username.getScene().getWindow();
                App.setRoot("main");
                stage.setTitle("Password Manager - " + user.getUsername());
                stage.setWidth(820);
                stage.setHeight(700);

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
