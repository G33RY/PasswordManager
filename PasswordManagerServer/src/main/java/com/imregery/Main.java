package com.imregery;

import com.g33ry.Pair;

import javax.crypto.SecretKey;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Main {
    private static Socket socket;

    public static void main(String[] args) {
        try{
            //Initialize database object
            Database db = new Database();

            //Create the socket server
            ServerSocket server = new ServerSocket(9090);

            //Infinity loop for every request
            while (true){

                //Accept socket request
                socket = server.accept();

                //Receive message
                Pair<String, Object> message = (Pair<String, Object>) Input();
                switch (message.key){

                    //GET PUBLIC KEY REQUEST
                    case "getPublicKey": {
                        System.out.println("Get Public Key request");

                        //Get the public key from the RAM database with the given mac address
                        SecretKey publicKey = db.getPublicKey((String) message.value);

                        //if public key not null send the publicKey
                        //if public key null send null
                        if(publicKey != null) Output(200, publicKey);
                        else Output(400, null);

                        break;
                    }

                    //LOGIN REQUEST
                    case "login": {
                        System.out.println("Login request");

                        //Cast message
                        HashMap<String, String> loginCreds = (HashMap<String, String>) message.value;

                        //If the login was successful
                        if(db.validateLogin(loginCreds)){
                            //Send OK response code and the accounts linked to the given username
                            Output(200, db.getAccounts(loginCreds.get("username"), loginCreds.get("macAddress")));

                            //If the username or password is wrong
                        }else{
                            //Send Bad request response code and null
                            Output(400, null);
                        }

                        break;
                    }

                    //GET ACCOUNTS REQUEST
                    case "getAccounts": {
                        System.out.println("Get Accounts request");

                        //Cast message
                        HashMap<String, String> resp = (HashMap<String, String>) message.value;

                        //Send OK response code and the accounts linked to the given username
                        Output(200, db.getAccounts(resp.get("username"), resp.get("macAddress")));

                        break;
                    }

                    //ADD ACCOUNT REQUEST
                    case "addAccount": {
                        System.out.println("Add Account request");

                        //Cast message
                        HashMap<String, String> resp = (HashMap<String, String>) message.value;

                        //Send OK response code and the return of the add account function
                        Output(200, db.addAccount(resp));
                        break;
                    }

                    //EDIT ACCOUNT REQUEST
                    case "editAccount": {
                        System.out.println("Edit Account request");

                        //Cast message
                        HashMap<String, String> resp = (HashMap<String, String>) message.value;

                        //Send OK response code and the return of the edit account function
                        Output(200, db.editAccount(resp));
                        break;
                    }

                    //DELETE ACCOUNT REQUEST
                    case "deleteAccount": {
                        System.out.println("Delete Account request");

                        //Cast message
                        HashMap<String, String> resp = (HashMap<String, String>) message.value;

                        //Send OK response code and the return of the delete account function
                        Output(200, db.deleteAccount(resp));
                        break;
                    }

                    //ADD USER REQUEST
                    case "addUser": {
                        System.out.println("Add User request");

                        //Cast message
                        HashMap<String, String> resp = (HashMap<String, String>) message.value;

                        //Send OK response code and the return of the add user function
                        Output(200, db.addUser(resp));
                        break;
                    }

                    //EDIT USER REQUEST
                    case "editUser": {
                        System.out.println("Edit User request");

                        //Cast message
                        HashMap<String, String> resp = (HashMap<String, String>) message.value;

                        //Send OK response code and the return of the edit user function
                        Output(200, db.editUser(resp));
                        break;
                    }

                    //DELETE USER REQUEST
                    case "deleteUser": {
                        System.out.println("Delete User request");

                        //Cast message
                        HashMap<String, String> resp = (HashMap<String, String>) message.value;

                        //Send OK response code and the return of the delete user function
                        Output(200, db.deleteUser(resp));
                        break;
                    }
                }
            }


        }catch(Exception exception){ exception.printStackTrace(); }
    }

    private static boolean Output(int responseCode, Object output){
        try{
            //Initialize Output stream to the client
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

            //Send output object with the given response code
            oos.writeObject(new Pair<Integer, Object>(responseCode, output));
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private static Object Input(){
        try{
            //Initialize Input stream from the client
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            //Read input object
            Object resp = ois.readObject();
            return resp;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
