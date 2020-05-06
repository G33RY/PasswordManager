package sample;

import com.g33ry.Encryption;
import com.g33ry.Pair;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

public class User {
    private String macAddress = "";
    private String username = "";
    private String host = "";
    private int port = 9090;
    private Socket socket;
    private SecretKey publicKey;
    private ArrayList<HashMap<String, String>> accounts;

    public User(){
        try{
            //Get the mac address of the computer
            NetworkInterface netInt = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            byte[] macBytes = netInt.getHardwareAddress();
            macAddress = "";
            for(byte i : macBytes){
                String curMac = String.format("%02X", i);
                macAddress += curMac;
            }

        }catch (Exception exception){exception.printStackTrace();}
    }

    /**
     *<h2>GET PUBLIC KEY</h2>
     * <p>Send a request for secret key linked to the client's mac address</p>
     * @return <b>FALSE:</b> if the response code not OK <br/> <b>TRUE:</b> if public key acquired
     * @throws IOException
     */
    public boolean getPublicKey() throws IOException {
        //Connect to the socket server
        socket = new Socket(host, port);

        //Send getPublicKey request with the mac address
        Output("getPublicKey", macAddress);

        //Get response pair
        Pair<Integer, Object> resp = Input();

        //If response code is not OK return null
        if(resp.key != 200) return false;

        //Set client's public key
        publicKey = (SecretKey) resp.value;

        return true;
    }

    /**
     * <h2>LOGIN</h2>
     * <p>Validate login with the given password and username <br/> Decrypt and add every account received from the server to the list</p>
     * @param usr
     * @param pw
     * @return <b>FALSE:</b> if username or password is wrong<br/> <b>TRUE:</b> if login was successful
     * @throws IOException
     * @throws IllegalBlockSizeException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     */
    public boolean login(String usr, String pw) throws IOException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        //Get public key
        if(!getPublicKey()) return false;

        //Connect to the socket server
        socket = new Socket(host, port);

        //Create the map with the login credentials
        HashMap<String, String> loginCreds = new HashMap<>();

        //Add the encrypted login credential to the map
        loginCreds.put("username", Encryption.Encrypt(usr, publicKey));
        loginCreds.put("password", Encryption.Encrypt(pw, publicKey));
        loginCreds.put("macAddress", macAddress);

        //Send login request with the login credentials
        Output("login", loginCreds);

        //Get response pair
        Pair<Integer, Object> resp = Input();

        //If response code is not OK return null
        if(resp.key != 200) return false;

        //Initialize accounts list
        accounts = new ArrayList<>();

        //Decrypt account values with the client's public key
        for(HashMap<String, String> account : (ArrayList<HashMap<String, String>>)resp.value) {
            HashMap<String, String> decryptedMap = new HashMap<>();

            decryptedMap.put("id", account.get("id"));
            decryptedMap.put("title", Encryption.Decrypt(account.get("title"), publicKey));
            decryptedMap.put("image", Encryption.Decrypt(account.get("image"), publicKey));
            decryptedMap.put("url", Encryption.Decrypt(account.get("url"), publicKey));
            decryptedMap.put("username", Encryption.Decrypt(account.get("username"), publicKey));
            decryptedMap.put("email", Encryption.Decrypt(account.get("email"), publicKey));
            decryptedMap.put("password", Encryption.Decrypt(account.get("password"), publicKey));

            accounts.add(decryptedMap);
        }

        //Set username
        username = usr;

        return true;
    }

    /**
     * <h2>GET ACCOUNTS</h2>
     * <p>Decrypt and add every account received from the server to the list</p>
     * @return accounts list
     * @throws IOException
     * @throws IllegalBlockSizeException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     */
    public ArrayList<HashMap<String, String>> getAccounts() throws IOException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        //Check if login was successful
        if(username == null) return null;

        //Recreate accounts list
        accounts = new ArrayList<>();

        //Connect to the socket server
        socket = new Socket(host, port);

        //Create an output map with the data needed
        HashMap<String, String> outputMap = new HashMap<>();
        outputMap.put("macAddress", macAddress);
        outputMap.put("username", Encryption.Encrypt(username, publicKey));

        //Send getAccounts request with the data
        Output("getAccounts", outputMap);

        //Get response pair
        Pair<Integer, Object> resp = Input();

        //If response code is not OK return the empty list
        if(resp.key != 200) return accounts;

        //Decrypt account values with the client's public key
        for(HashMap<String, String> account : (ArrayList<HashMap<String, String>>)resp.value) {
            HashMap<String, String> decryptedMap = new HashMap<>();

            decryptedMap.put("id", account.get("id"));
            decryptedMap.put("title", Encryption.Decrypt(account.get("title"), publicKey));
            decryptedMap.put("image", Encryption.Decrypt(account.get("image"), publicKey));
            decryptedMap.put("url", Encryption.Decrypt(account.get("url"), publicKey));
            decryptedMap.put("username", Encryption.Decrypt(account.get("username"), publicKey));
            decryptedMap.put("email", Encryption.Decrypt(account.get("email"), publicKey));
            decryptedMap.put("password", Encryption.Decrypt(account.get("password"), publicKey));

            accounts.add(decryptedMap);
        }

        return accounts;
    }

    /**
     * <h2>ADD ACCOUNT</h2>
     * <p>Add an account to the database</p>
     * @param account
     * @return <b>FALSE:</b> if the response code is not OK or something went wrong on the server<br/> <b>TRUE:</b> if everything went good
     * @throws IllegalBlockSizeException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IOException
     */
    public boolean addAccount(HashMap<String, String> account) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, IOException {
        //Create output map with the encrypted data needed
        HashMap<String, String> outputMap = new HashMap<>();
        outputMap.put("macAddress", macAddress);
        outputMap.put("user", Encryption.Encrypt(username, publicKey));
        outputMap.put("title", Encryption.Encrypt(account.get("title"), publicKey));
        outputMap.put("image", Encryption.Encrypt(account.get("image"), publicKey));
        outputMap.put("url", Encryption.Encrypt(account.get("url"), publicKey));
        outputMap.put("username", Encryption.Encrypt(account.get("username"), publicKey));
        outputMap.put("email", Encryption.Encrypt(account.get("email"), publicKey));
        outputMap.put("password", Encryption.Encrypt(account.get("password"), publicKey));


        //Connect to the socket server
        socket = new Socket(host, port);

        //Send addAccount request with the encrypted data
        Output("addAccount", outputMap);

        //Get response pair
        Pair<Integer, Object> resp = Input();

        //If response code is not OK return null
        if(resp.key != 200) return false;

        //Return with the response value
        return (boolean)resp.value;
    }

    /**
     * <h2>EDIT ACCOUNT</h2>
     * <p>Edit an account in the database</p>
     * @param account
     * @return <b>FALSE:</b> if the response code is not OK or something went wrong on the server<br/> <b>TRUE:</b> if everything went good
     * @throws IllegalBlockSizeException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IOException
     */
    public boolean editAccount(HashMap<String, String> account) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, IOException {
        //Create output map with the encrypted data needed
        HashMap<String, String> outputMap = new HashMap<>();
        outputMap.put("macAddress", macAddress);
        outputMap.put("id", account.get("id"));
        outputMap.put("user", Encryption.Encrypt(username, publicKey));
        outputMap.put("title", Encryption.Encrypt(account.get("title"), publicKey));
        outputMap.put("image", Encryption.Encrypt(account.get("image"), publicKey));
        outputMap.put("url", Encryption.Encrypt(account.get("url"), publicKey));
        outputMap.put("username", Encryption.Encrypt(account.get("username"), publicKey));
        outputMap.put("email", Encryption.Encrypt(account.get("email"), publicKey));
        outputMap.put("password", Encryption.Encrypt(account.get("password"), publicKey));


        //Connect to the socket server
        socket = new Socket(host, port);

        //Send addAccount request with the encrypted data
        Output("editAccount", outputMap);

        //Get response pair
        Pair<Integer, Object> resp = Input();

        //If response code is not OK return null
        if(resp.key != 200) return false;

        //Return with the response value
        return (boolean)resp.value;
    }

    /**
     * <h2>DELETE ACCOUNT</h2>
     * <p>Delete an account from the database</p>
     * @param id
     * @return <b>FALSE:</b> if the response code is not OK or something went wrong on the server<br/> <b>TRUE:</b> if everything went good
     * @throws IOException
     */
    public boolean deleteAccount(String id) throws IOException {
        //Create output map with the encrypted data needed
        HashMap<String, String> outputMap = new HashMap<>();
        outputMap.put("id", id);


        //Connect to the socket server
        socket = new Socket(host, port);

        //Send addAccount request with the encrypted data
        Output("deleteAccount", outputMap);

        //Get response pair
        Pair<Integer, Object> resp = Input();

        //If response code is not OK return null
        if(resp.key != 200) return false;

        //Return with the response value
        return (boolean)resp.value;
    }

    /**
     * <h2>ADD USER</h2>
     * <p>Add a user to the database</p>
     * @param user
     * @return <b>FALSE:</b> if the response code is not OK or something went wrong on the server<br/> <b>TRUE:</b> if everything went good
     * @throws IllegalBlockSizeException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IOException
     */
    public boolean addUser(HashMap<String, String> user) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, IOException {
        //Create output map with the encrypted data needed
        HashMap<String, String> outputMap = new HashMap<>();
        outputMap.put("macAddress", macAddress);
        outputMap.put("username", Encryption.Encrypt(user.get("username"), publicKey));
        outputMap.put("email", Encryption.Encrypt(user.get("email"), publicKey));
        outputMap.put("password", Encryption.Encrypt(user.get("password"), publicKey));


        //Connect to the socket server
        socket = new Socket(host, port);

        //Send addAccount request with the encrypted data
        Output("addUser", outputMap);

        //Get response pair
        Pair<Integer, Object> resp = Input();

        //If response code is not OK return null
        if(resp.key != 200) return false;

        //Return with the response value
        return (boolean)resp.value;
    }

    /**
     * <h2>EDIT USER</h2>
     * <p>Edit a user in the database</p>
     * @param user
     * @return <b>FALSE:</b> if the response code is not OK or something went wrong on the server<br/> <b>TRUE:</b> if everything went good
     * @throws IllegalBlockSizeException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IOException
     */
    public boolean editUser(HashMap<String, String> user) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, IOException {
        //Create output map with the encrypted data needed
        HashMap<String, String> outputMap = new HashMap<>();
        outputMap.put("macAddress", macAddress);
        outputMap.put("username", Encryption.Encrypt(user.get("username"), publicKey));
        outputMap.put("email", Encryption.Encrypt(user.get("email"), publicKey));
        outputMap.put("password", Encryption.Encrypt(user.get("password"), publicKey));


        //Connect to the socket server
        socket = new Socket(host, port);

        //Send addAccount request with the encrypted data
        Output("editUser", outputMap);

        //Get response pair
        Pair<Integer, Object> resp = Input();

        //If response code is not OK return null
        if(resp.key != 200) return false;

        //Return with the response value
        return (boolean)resp.value;
    }

    /**
     * <h2>DELETE USER</h2>
     * <p>Delete a user from the database</p>
     * @param usr
     * @return <b>FALSE:</b> if the response code is not OK or something went wrong on the server<br/> <b>TRUE:</b> if everything went good
     * @throws IOException
     * @throws IllegalBlockSizeException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     */
    public boolean deleteUser(String usr) throws IOException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        //Create output map with the encrypted data needed
        HashMap<String, String> outputMap = new HashMap<>();
        outputMap.put("username", Encryption.Encrypt(usr, publicKey));

        //Connect to the socket server
        socket = new Socket(host, port);

        //Send addAccount request with the encrypted data
        Output("deleteUser", outputMap);

        //Get response pair
        Pair<Integer, Object> resp = Input();

        //If response code is not OK return null
        if(resp.key != 200) return false;

        //Return with the response value
        return (boolean)resp.value;
    }

    public String getUsername() {
        return username;
    }

    private boolean Output(String  requestName, Object output){
        try{
            //Initialize Output stream to the client
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

            //Send output object with the given response code
            oos.writeObject(new Pair<String, Object>(requestName, output));
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private Pair<Integer, Object> Input(){
        try{
            //Initialize Input stream from the client
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            //Read input object with the response code
            Pair<Integer, Object> resp = (Pair<Integer, Object>) ois.readObject();
            return resp;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

}
