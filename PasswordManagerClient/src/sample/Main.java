package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.HashMap;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent login = FXMLLoader.load(getClass().getResource("login.fxml"));
        primaryStage.setTitle("Password Manager - Login");
        primaryStage.setScene(new Scene(login, 800, 700));
        primaryStage.show();
    }


    public static void main(String[] args) throws Exception{

        //Add user
//        User user = new User();
//        user.getPublicKey();
//        HashMap<String, String> map = new HashMap<>();
//        map.put("username", "admin");
//        map.put("password", "admin");
//        map.put("email", "example@example.com");
//        user.addUser(map);

        launch(args);
    }
}
