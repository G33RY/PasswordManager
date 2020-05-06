package sample;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.HashMap;

public class MainController {
    @FXML private VBox list;
    @FXML private Label image;
    @FXML private Label label;
    @FXML private Label name;
    @FXML private TextField title;
    @FXML private TextField imageUrl;
    @FXML private TextField url;
    @FXML private TextField AccUsername;
    @FXML private TextField AccEmail;
    @FXML private TextField AccPassword;
    @FXML private TextField search;

    @FXML private Button refreshBtn;
    @FXML private Button searchBtn;
    @FXML private Button addBtn;
    @FXML private Button deleteBtn;
    @FXML private Button saveBtn;

    private User user;
    private Integer curId = 0;

    public void initialize(){
        try {
            //Get user object from the login controller
            user = LoginController.user;

            //Set name label's text to the username
            name.setText(user.getUsername());

            //List all accounts
            eRefresh(null, 0);

            //Set event handler for buttons
            refreshBtn.setOnMouseClicked( e -> { eRefresh(null, 0); });
            searchBtn.setOnMouseClicked( e -> { eSearch(); });
            addBtn.setOnMouseClicked( e -> { eAdd(); });
            deleteBtn.setOnMouseClicked( e -> { eDelete(); });
            saveBtn.setOnMouseClicked( e -> { eSave(); });

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void eSearch(){
        try{
            //Accounts match with the search text
            ArrayList<HashMap<String, String>> accounts = new ArrayList<>();

            //Gather all matching accounts
            for(HashMap<String, String> account : user.getAccounts()){
                if(account.get("title").toLowerCase().contains(search.getText().toLowerCase())){
                    accounts.add(account);
                }
            }

            //Refresh list
            eRefresh(accounts, 0);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void eEdit(Integer id) {
        try{
            //Set current id to the given id parameter
            curId = id;

            //Refresh list
            eRefresh(null, id);

            HashMap<String, String> curAccount = null;

            //Find account with the given id
            for(HashMap<String, String> account : user.getAccounts()) {
                if(Integer.parseInt(account.get("id")) == id){
                    curAccount = account;
                    break;
                }
            }

            //If account doesn't found return
            if(curAccount == null) return;

            //If image empty set to none
            //If not set to url(image)
            if(!curAccount.get("image").isEmpty()){
                image.setStyle("-fx-background-image: url('"+ curAccount.get("image") +"'); -fx-background-radius: 50%;");
                image.setText("");
            }else{
                image.setStyle("-fx-background-image: none");
                image.setText(curAccount.get("title").substring(0, 1));
            }

            //Set every field to the account's values
            label.setText(curAccount.get("title"));
            title.setText(curAccount.get("title"));
            imageUrl.setText(curAccount.get("image"));
            url.setText(curAccount.get("url"));
            AccUsername.setText(curAccount.get("username"));
            AccEmail.setText(curAccount.get("email"));
            AccPassword.setText(curAccount.get("password"));

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void eCopy(Integer id) {
        try{
            HashMap<String, String> curAccount = null;

            //Find account with the given id
            for(HashMap<String, String> account : user.getAccounts()) {
                if(Integer.parseInt(account.get("id")) == id){
                    curAccount = account;
                    break;
                }
            }

            //If account doesn't found return
            if(curAccount == null) return;

            //Get clipboard
            Clipboard clipboard = Clipboard.getSystemClipboard();

            //Create a clipboard content
            ClipboardContent clipboardContent = new ClipboardContent();

            //Add the password to the clipboard content
            clipboardContent.putString(curAccount.get("password"));

            //Set clipboard to the created clipboard content
            clipboard.setContent(clipboardContent);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void eDelete() {
        try{
            user.deleteAccount(Integer.toString(curId));

            //Refresh list
            eRefresh(null, -1);

            //Set fields to empty
            eAdd();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void eSave() {
        try{

            //If image empty set to none
            //If not set to url(image)
            if(!imageUrl.getText().isEmpty()){
                image.setStyle("-fx-background-image: url('"+ imageUrl.getText() +"'); -fx-background-radius: 50%;");
                image.setText("");
            }else{
                image.setStyle("-fx-background-image: none");
                image.setText(title.getText().substring(0, 1));
            }

            //Set every field to the account's values
            label.setText(title.getText());

            if(curId == 0){
                HashMap<String, String> map = new HashMap<>();
                map.put("user", user.getUsername());
                map.put("title", title.getText());
                map.put("image", imageUrl.getText());
                map.put("url", url.getText());
                map.put("username", AccUsername.getText());
                map.put("email", AccEmail.getText());
                map.put("password", AccPassword.getText());

                user.addAccount(map);
                eRefresh(null, -1);
                eAdd();
            }else{
                HashMap<String, String> map = new HashMap<>();
                map.put("user", user.getUsername());
                map.put("id", Integer.toString(curId));
                map.put("title", title.getText());
                map.put("image", imageUrl.getText());
                map.put("url", url.getText());
                map.put("username", AccUsername.getText());
                map.put("email", AccEmail.getText());
                map.put("password", AccPassword.getText());

                user.editAccount(map);
                eRefresh(null, curId);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void eAdd(){
        eRefresh(null, -1);
        curId = 0;
        image.setStyle("-fx-background-image: none");
        label.setText("");
        title.setText("Example 1");
        imageUrl.setText("");
        url.setText("example.com");
        AccUsername.setText("Example");
        AccEmail.setText("example@example.com");
        AccPassword.setText("ExamplePassword");
    }

    public void eRefresh(ArrayList<HashMap<String, String>> InputAccounts, Integer id) {
        try{

            list.getChildren().clear();
            ArrayList<HashMap<String, String>> accounts = user.getAccounts();
            if(InputAccounts != null) accounts = InputAccounts;
            for(int i=0; i< accounts.size(); i++){
                HashMap<String, String> account = accounts.get(i);

                GridPane gridPane = new GridPane();
                gridPane.getStyleClass().add("accountBox");
                if(id == Integer.parseInt(account.get("id")) || curId == Integer.parseInt(account.get("id"))) gridPane.getStyleClass().add("selected");
                gridPane.getColumnConstraints().add(new ColumnConstraints(55));
                gridPane.getColumnConstraints().add(new ColumnConstraints(125));
                gridPane.getColumnConstraints().add(new ColumnConstraints(70));
                gridPane.getRowConstraints().add(new RowConstraints(50));
                gridPane.setOnMouseClicked(e ->{
                    eEdit(Integer.parseInt(account.get("id")));});
                gridPane.setUserData(account.get("id"));
                HBox imageBox = new HBox();
                imageBox.getStyleClass().add("imageBox");
                GridPane.setRowIndex(imageBox, 0);
                GridPane.setColumnIndex(imageBox, 0);
                if(account.get("image").isEmpty() || account.get("image").isBlank()){
                    imageBox.getChildren().add(new Label(account.get("title").substring(0, 1)));
                }else{
                    imageBox.getChildren().add(new Label());
                    imageBox.getChildren().get(0).setStyle("-fx-background-image: url('" + account.get("image") + "'); -fx-background-radius: 50%;");
                }
                gridPane.getChildren().add(imageBox);
                HBox labelBox = new HBox();
                labelBox.getStyleClass().add("labelBox");
                GridPane.setRowIndex(labelBox, 0);
                GridPane.setColumnIndex(labelBox, 1);
                labelBox.getChildren().add(new Label(account.get("title")));
                gridPane.getChildren().add(labelBox);
                HBox accBtnsBox = new HBox();
                accBtnsBox.getStyleClass().add("accBtnsBox");
                GridPane.setRowIndex(accBtnsBox, 0);
                GridPane.setColumnIndex(accBtnsBox, 2);
                Button copyBtn = new Button();
                copyBtn.getStyleClass().add("copyBtn");
                copyBtn.setOnMouseClicked(e ->{
                    eCopy(Integer.parseInt(account.get("id")));});
                accBtnsBox.getChildren().add(copyBtn);
                Button editBtn = new Button();
                editBtn.getStyleClass().add("editBtn");
                editBtn.setOnMouseClicked(e ->{
                    eEdit(Integer.parseInt(account.get("id")));});
                accBtnsBox.getChildren().add(editBtn);
                gridPane.getChildren().add(accBtnsBox);
                list.getChildren().add(gridPane);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
