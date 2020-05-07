package com.imregery;

import com.g33ry.Encryption;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

public class Database {

    private Statement stm;
    private HashMap<String, SecretKey> keys = new HashMap<>();

    public Database() throws SQLException {
        //Connect to database
        Connection sqliteCon = DriverManager.getConnection("jdbc:sqlite:test.db");

        //Create statement
        stm = sqliteCon.createStatement();
    }
    
    /**
     * <h2>GET PUBLIC KEY</h2>
     * <p>Get public key from the database or generate a key and insert it to the database</p>
     * @param macAddress
     * @return Public Key
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     */
    public SecretKey getPublicKey(String macAddress) throws SQLException, NoSuchAlgorithmException {

        //Search query for the row with input mac address
        ResultSet result = stm.executeQuery(String.format("SELECT * FROM macAddresses WHERE macAddress='%s' LIMIT 1", macAddress));

        //If the query found the row
        if(result.next()){

            //Get the key from the database row
            SecretKey secretKey = StringToKey(result.getString("secretKey"));

            //Add the key to the RAM database
            keys.put(macAddress, secretKey);

            //Return with the key
            return secretKey;

            //If the query doesn't found the row
        }else{
            //Generate the secret key
            SecretKey secretKey = Encryption.Generatekey();

            //Insert the mac address and the generated secret key to the table
            stm.execute(String.format("INSERT INTO macAddresses (macAddress, secretKey) VALUES ('%s', '%s') ", macAddress, KeyToString(secretKey)));

            //Add the key to the RAM database
            keys.put(macAddress, secretKey);

            //Return with the key
            return secretKey;
        }
    }

    /**
     * <h2>VALIDATE LOGIN</h2>
     * <p>Validate login with the given login credentials</p>
     * @param loginCredentials
     * @return <b>TRUE</b> = Successful login <br/> <b>FALSE</b> = Wrong login credentials
     * @throws SQLException
     * @throws IllegalBlockSizeException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     */
    public boolean validateLogin(HashMap<String, String> loginCredentials) throws SQLException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        //Decrypt username with secret key connected to the client's mac address
        String username = Encryption.Decrypt(loginCredentials.get("username"), keys.get(loginCredentials.get("macAddress")));

        //Search query for the user's row with the given username
        ResultSet result = stm.executeQuery(String.format("SELECT * FROM users WHERE username='%s' LIMIT 1", username));

        //If the query found the user
        if(result.next()){
            //Decrypt password linked to the given username with the secret key in the row
            String rowPassword = Encryption.Decrypt(result.getString("password"), StringToKey(result.getString("key")));

            //Decrypt the given password with secret key connected to the client's mac address
            String givenPassword = Encryption.Decrypt(loginCredentials.get("password"), keys.get(loginCredentials.get("macAddress")));

            //Compare the given password with the password linked to the given username
            if(givenPassword.equals(rowPassword)){
                //Return Successful login
                return true;
            }
        }

        //Return Wrong login credentials
        return false;
    }


    /**
     * <h2>GET ACCOUNTS</h2>
     * <p>Get the accounts linked to the given username and encrypt it with the key linked to the given mac address</p>
     * <h3>EncryptedUser parameter has to be encrypted with the secret key linked to the computer's mac address</h3>
     * @param EncryptedUser
     * @param macAddress
     * @return List of the accounts linked to the given username
     * @throws SQLException
     * @throws IllegalBlockSizeException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     */
    public ArrayList<HashMap<String, String>> getAccounts(String EncryptedUser, String macAddress) throws SQLException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {

        //Initialize return arraylist
        ArrayList<HashMap<String, String>> accounts = new ArrayList<>();

        //Decrypt the user's username with the key linked to the computer's mac address
        String user = Encryption.Decrypt(EncryptedUser, keys.get(macAddress));

        //Search query for the given username
        ResultSet userResult = stm.executeQuery(String.format("SELECT * FROM users WHERE username='%s' LIMIT 1", user));
        //If the search query didn't find the username return with the empty arraylist
        if(!userResult.next()) return accounts;

        //Get user's secret key
        SecretKey userKey = StringToKey(userResult.getString("key"));

        //Search query for the accounts linked to the given username
        ResultSet accountResult = stm.executeQuery(String.format("SELECT * FROM accounts WHERE user='%s'", user));

        //Loop trough all of the user's accounts
        while(accountResult.next()){
            //Initialize the map to add the arraylist
            HashMap<String, String> map = new HashMap<>();

            //Decrypt the account data in the row with the secret key in the user's data row
            //And encrypt it with the key linked to the mac address
            String EncryptedTitle = !accountResult.getString("title").isEmpty() ? Encryption.Encrypt( Encryption.Decrypt( accountResult.getString("title"), userKey ), keys.get(macAddress) ) : "";
            String EncryptedImage = !accountResult.getString("image").isEmpty() ? Encryption.Encrypt( Encryption.Decrypt( accountResult.getString("image"), userKey ), keys.get(macAddress) ) : "";
            String EncryptedUrl = !accountResult.getString("url").isEmpty() ? Encryption.Encrypt( Encryption.Decrypt( accountResult.getString("url"), userKey ), keys.get(macAddress) ) : "";
            String EncryptedUsername = !accountResult.getString("username").isEmpty() ? Encryption.Encrypt( Encryption.Decrypt( accountResult.getString("username"), userKey ), keys.get(macAddress) ) : "";
            String EncryptedEmail = !accountResult.getString("email").isEmpty() ? Encryption.Encrypt( Encryption.Decrypt( accountResult.getString("email"), userKey ), keys.get(macAddress) ) : "";
            String EncryptedPassword = !accountResult.getString("password").isEmpty() ? Encryption.Encrypt( Encryption.Decrypt( accountResult.getString("password"), userKey ), keys.get(macAddress) ) : "";


            //Put the account data to the map
            map.put("id", accountResult.getString("id"));
            map.put("title", EncryptedTitle);
            map.put("image", EncryptedImage);
            map.put("url", EncryptedUrl);
            map.put("username", EncryptedUsername);
            map.put("email", EncryptedEmail);
            map.put("password", EncryptedPassword);

            //Add the map to the arraylist
            accounts.add(map);
        }

        //Return the list of the accounts linked to the given username
        return accounts;
    }

    /**
     * <h2>ADD ACCOUNT</h2>
     * <p>Add a row to the accounts table with the given account data and encrypt it with the user's key</p>
     * <h3 style="margin: 10px 0 0 0;"><u>Data needed and it's name in the parameter hashmap:</u></h3>
     * <h3 style="margin: 0px 0 0 0;"><u>DATA MARKED WITH * HAS TO BE ENCRYPTED</u></h3>
     * <ul style="margin: 0 0 0 20px;">
     *     <li><b>*user:</b> user's username</li>
     *     <li><b>macAddress:</b> computer's macAddress</li>
     *     <li><b>*title:</b> account's title</li>
     *     <li><b>*image:</b> account's image url <i>(can be empty)</i></li>
     *     <li><b>*url:</b> account's website url</li>
     *     <li><b>*username:</b> account's username <i>(can be empty)</i></li>
     *     <li><b>*email:</b> account's email <i>(can be empty)</i></li>
     *     <li><b>*password:</b> account's password</li>
     * </ul>
     * @param accountData
     * @return <b>FALSE:</b> if the given username doesn't found in the database <br/> <b>TRUE:</b> if the query executed
     * @throws SQLException
     * @throws IllegalBlockSizeException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     */
    public boolean addAccount(HashMap<String, String> accountData) throws SQLException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {

        //Decrypt the user's username with the key linked to the computer's mac address
        String user = Encryption.Decrypt(accountData.get("user"), keys.get(accountData.get("macAddress")));

        //Search query for the given username
        ResultSet userResult = stm.executeQuery(String.format("SELECT * FROM users WHERE username='%s' LIMIT 1", user));
        //If the search query didn't find the username return with false
        if(!userResult.next()) return false;

        //Get user's secret key
        SecretKey userKey = StringToKey(userResult.getString("key"));

        //Decrypt the account data in the row with the key linked to the mac address
        //Then encrypt it with the secret key in the user's data row
        String EncryptedTitle = !accountData.get("title").isEmpty() ? Encryption.Encrypt( Encryption.Decrypt( accountData.get("title"), keys.get(accountData.get("macAddress"))  ), userKey ) : "";
        String EncryptedImage = !accountData.get("image").isEmpty() ? Encryption.Encrypt( Encryption.Decrypt( accountData.get("image"), keys.get(accountData.get("macAddress"))  ), userKey ) : "";
        String EncryptedUrl = !accountData.get("url").isEmpty() ? Encryption.Encrypt( Encryption.Decrypt( accountData.get("url"), keys.get(accountData.get("macAddress"))  ), userKey ) : "";
        String EncryptedUsername = !accountData.get("username").isEmpty() ? Encryption.Encrypt( Encryption.Decrypt( accountData.get("username"), keys.get(accountData.get("macAddress")) ), userKey ) : "";
        String EncryptedEmail = !accountData.get("email").isEmpty() ? Encryption.Encrypt( Encryption.Decrypt( accountData.get("email"), keys.get(accountData.get("macAddress")) ), userKey ) : "";
        String EncryptedPassword = !accountData.get("password").isEmpty() ? Encryption.Encrypt( Encryption.Decrypt( accountData.get("password"), keys.get(accountData.get("macAddress")) ), userKey ) : "";

        //Query string to execute
        String queryString = "INSERT INTO accounts (user, title, image, url, username, email, password) VALUES (";

        //Add the values to the query string
        queryString += "'" + user + "',"; // user
        queryString += "'" + EncryptedTitle + "',"; // title
        queryString += "'" + EncryptedImage + "',"; // image
        queryString += "'" + EncryptedUrl + "',"; // url
        queryString += "'" + EncryptedUsername + "',"; // username
        queryString += "'" + EncryptedEmail + "',"; // email
        queryString += "'" + EncryptedPassword + "'"; // password

        //Add the end of the query string
        queryString += ")";

        //Execute query
        stm.execute(queryString);

        return true;
    }

    /**
     * <h2>EDIT ACCOUNT</h2>
     * <p>Edit a row with the given id in the accounts table and set the column values to the given data</p>
     * <h3 style="margin: 10px 0 0 0;"><u>Data needed and it's name in the parameter hashmap:</u></h3>
     * <h3 style="margin: 0px 0 0 0;"><u>DATA MARKED WITH * HAS TO BE ENCRYPTED</u></h3>
     * <ul style="margin: 0 0 0 20px;">
     *     <li><b>*user:</b> user's username</li>
     *     <li><b>macAddress:</b> computer's macAddress</li>
     *     <li><b>id:</b> account's id</li>
     *     <li><b>*title:</b> account's title</li>
     *     <li><b>*image:</b> account's image url <i>(can be empty)</i></li>
     *     <li><b>*url:</b> account's website url</li>
     *     <li><b>*username:</b> account's username <i>(can be empty)</i></li>
     *     <li><b>*email:</b> account's email <i>(can be empty)</i></li>
     *     <li><b>*password:</b> account's password</li>
     * </ul>
     * @param accountData
     * @return <b>FALSE:</b> if the given username doesn't found in the database OR if the given account id doesn't found <br> <b>TRUE:</b> if the query executed
     * @throws SQLException
     * @throws IllegalBlockSizeException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     */
    public boolean editAccount(HashMap<String, String> accountData) throws SQLException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        //Decrypt the user's username with the key linked to the computer's mac address
        String user = Encryption.Decrypt(accountData.get("user"), keys.get(accountData.get("macAddress")));

        //Search query for the given username
        ResultSet userResult = stm.executeQuery(String.format("SELECT * FROM users WHERE username='%s' LIMIT 1", user));
        //If the search query didn't find the username return with false
        if(!userResult.next()) return false;

        //Get user's secret key
        SecretKey userKey = StringToKey(userResult.getString("key"));

        //Search query for the given account id
        ResultSet accountResult = stm.executeQuery(String.format("SELECT * FROM accounts WHERE id='%s' LIMIT 1", accountData.get("id")));
        //If the search query didn't find the account id return with false
        if(!accountResult.next()) return false;

        //Decrypt the account data in the row with the key linked to the mac address
        //Then encrypt it with the secret key in the user's data row
        String EncryptedTitle = !accountData.get("title").isEmpty() ? Encryption.Encrypt( Encryption.Decrypt( accountData.get("title"), keys.get(accountData.get("macAddress"))  ), userKey ) : "";
        String EncryptedImage = !accountData.get("image").isEmpty() ? Encryption.Encrypt( Encryption.Decrypt( accountData.get("image"), keys.get(accountData.get("macAddress"))  ), userKey ) : "";
        String EncryptedUrl = !accountData.get("url").isEmpty() ? Encryption.Encrypt( Encryption.Decrypt( accountData.get("url"), keys.get(accountData.get("macAddress"))  ), userKey ) : "";
        String EncryptedUsername = !accountData.get("username").isEmpty() ? Encryption.Encrypt( Encryption.Decrypt( accountData.get("username"), keys.get(accountData.get("macAddress")) ), userKey ) : "";
        String EncryptedEmail = !accountData.get("email").isEmpty() ? Encryption.Encrypt( Encryption.Decrypt( accountData.get("email"), keys.get(accountData.get("macAddress")) ), userKey ) : "";
        String EncryptedPassword = !accountData.get("password").isEmpty() ? Encryption.Encrypt( Encryption.Decrypt( accountData.get("password"), keys.get(accountData.get("macAddress")) ), userKey ) : "";

        //Query string to execute
        String queryString = "UPDATE accounts SET ";

        //Add the values to the query string
        queryString += "title='" + EncryptedTitle + "',"; // title
        queryString += "image='" + EncryptedImage + "',"; // image
        queryString += "url='" + EncryptedUrl + "',"; // url
        queryString += "username='" + EncryptedUsername + "',"; // username
        queryString += "email='" + EncryptedEmail + "',"; // email
        queryString += "password='" + EncryptedPassword + "'"; // password

        //Add which row to update
        queryString += " WHERE id='" + accountData.get("id") + "'";

        //Execute query
        stm.execute(queryString);

        return true;
    }

    /**
     * <h2>DELETE ACCOUNT</h2>
     * <p>Delete a row with the given id from the accounts table</p>
     * <h3 style="margin: 10px 0 0 0;"><u>Data needed and it's name in the parameter hashmap:</u></h3>
     * <ul style="margin: 0 0 0 20px;">
     *     <li><b>id:</b> account's id</li>
     * </ul>
     * @param accountData
     * @return <b>FALSE:</b> if the given account id doesn't found <br> <b>TRUE:</b> if the query executed
     * @throws SQLException
     */
    public boolean deleteAccount(HashMap<String, String> accountData) throws SQLException {
        //Search query for the given account id
        ResultSet accountResult = stm.executeQuery(String.format("SELECT * FROM accounts WHERE id='%s' LIMIT 1", accountData.get("id")));
        //If the search query didn't find the account id return with false
        if(!accountResult.next()) return false;

        //Execute query
        stm.execute("DELETE FROM accounts WHERE id='" + accountData.get("id") + "'");

        return true;
    }


    /**
     * <h2>ADD USER</h2>
     * <p>Add a row to the users table with the given user data and encrypt it with the generated user's key</p>
     * <h3 style="margin: 10px 0 0 0;"><u>Data needed and it's name in the parameter hashmap:</u></h3>
     * <h3 style="margin: 0px 0 0 0;"><u>DATA MARKED WITH * HAS TO BE ENCRYPTED</u></h3>
     * <ul style="margin: 0 0 0 20px;">
     *     <li><b>macAddress:</b> computer's macAddress</li>
     *     <li><b>*username:</b> user's username </li>
     *     <li><b>*email:</b> user's email </li>
     *     <li><b>*password:</b> user's password</li>
     * </ul>
     * @param userData
     * @return <b>FALSE:</b> if the given username does found in the database<br/> <b>TRUE:</b> if the query executed
     * @throws SQLException
     * @throws IllegalBlockSizeException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     */
    public boolean addUser(HashMap<String, String> userData) throws SQLException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        //Decrypt the user's username with the key linked to the computer's mac address
        String username = Encryption.Decrypt(userData.get("username"), keys.get(userData.get("macAddress")));

        //Search query for the given username
        ResultSet userResult = stm.executeQuery(String.format("SELECT * FROM users WHERE username='%s' LIMIT 1", username));
        //If the search query did find the username return with false
        if(userResult.next()) return false;

        //Generate user's secret key
        SecretKey secretKey = Encryption.Generatekey();

        //Decrypt the user's data in the row with the key linked to the mac address
        //Then encrypt it with generated the secret key
        String EncryptedEmail = Encryption.Encrypt( Encryption.Decrypt( userData.get("email"), keys.get(userData.get("macAddress")) ), secretKey );
        String EncryptedPassword = Encryption.Encrypt( Encryption.Decrypt( userData.get("password"), keys.get(userData.get("macAddress")) ), secretKey );

        //Query string to execute
        String queryString = "INSERT INTO users (username, email, password, key) VALUES (";

        //Add the values to the query string
        queryString += "'" + username + "',"; // username
        queryString += "'" + EncryptedEmail + "',"; // email
        queryString += "'" + EncryptedPassword + "',"; // password
        queryString += "'" + KeyToString(secretKey) + "'"; // secret key

        //Add the end of the query string
        queryString += ")";

        //Execute query
        stm.execute(queryString);

        return true;
    }


    /**
     * <h2>EDIT USER</h2>
     * <p>Edit a row with the given username in the users table and set the column values to the given data</p>
     * <h3 style="margin: 10px 0 0 0;"><u>Data needed and it's name in the parameter hashmap:</u></h3>
     * <h3 style="margin: 0px 0 0 0;"><u>DATA MARKED WITH * HAS TO BE ENCRYPTED</u></h3>
     * <ul style="margin: 0 0 0 20px;">
     *     <li><b>macAddress:</b> computer's macAddress</li>
     *     <li><b>*username:</b> account's username <i>(can be empty)</i></li>
     *     <li><b>*email:</b> account's email <i>(can be empty)</i></li>
     *     <li><b>*password:</b> account's password</li>
     * </ul>
     * @param userData
     * @return <b>FALSE:</b> if the given username doesn't found in the database <br> <b>TRUE:</b> if the query executed
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     * @throws IllegalBlockSizeException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws NoSuchPaddingException
     */
    public boolean editUser(HashMap<String, String> userData) throws SQLException, NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException {
        //Decrypt the user's username with the key linked to the computer's mac address
        String username = Encryption.Decrypt(userData.get("username"), keys.get(userData.get("macAddress")));

        //Search query for the given username
        ResultSet userResult = stm.executeQuery(String.format("SELECT * FROM users WHERE username='%s' LIMIT 1", username));
        //If the search query didn't find the username return with false
        if(!userResult.next()) return false;

        //Get user's secret key
        SecretKey userKey = StringToKey(userResult.getString("key"));

        //Decrypt the user's data in the row with the key linked to the mac address
        //Then encrypt it with generated the secret key
        String EncryptedEmail = Encryption.Encrypt( Encryption.Decrypt( userData.get("email"), keys.get(userData.get("macAddress")) ), userKey );
        String EncryptedPassword = Encryption.Encrypt( Encryption.Decrypt( userData.get("password"), keys.get(userData.get("macAddress")) ), userKey );

        //Query string to execute
        String queryString = "UPDATE users SET ";

        //Add the values to the query string
        queryString += "email='" + EncryptedEmail + "',"; // email
        queryString += "password='" + EncryptedPassword + "'"; // password

        //Add which row to update
        queryString += " WHERE username='" + username + "'";

        //Execute query
        stm.execute(queryString);

        return true;
    }


    /**
     * <h2>DELETE USER</h2>
     * <p>Delete a row with the given username from the users table</p>
     * <h3 style="margin: 10px 0 0 0;"><u>Data needed and it's name in the parameter hashmap:</u></h3>
     * <h3 style="margin: 0px 0 0 0;"><u>DATA MARKED WITH * HAS TO BE ENCRYPTED</u></h3>
     * <ul style="margin: 0 0 0 20px;">
     *     <li><b>macAddress:</b> computer's mac address</li>
     *     <li><b>*username:</b> user's username</li>
     * </ul>
     * @param userData
     * @return <b>FALSE:</b> if the given username doesn't found <br> <b>TRUE:</b> if the query executed
     * @throws SQLException
     * @throws IllegalBlockSizeException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     */
    public boolean deleteUser(HashMap<String, String> userData) throws SQLException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        //Decrypt the user's username with the key linked to the computer's mac address
        String username = Encryption.Decrypt(userData.get("username"), keys.get(userData.get("macAddress")));

        //Search query for the given username
        ResultSet userResult = stm.executeQuery(String.format("SELECT * FROM users WHERE username='%s' LIMIT 1", username));
        //If the search query didn't find the username return with false
        if(!userResult.next()) return false;

        //Execute query
        stm.execute("DELETE FROM users WHERE username='" + username + "'");

        return true;
    }


    /**
     * <h2>STRING TO KEY</h2>
     * <p>Convert the given readable string to a secret key</p>
     * @param keyString
     * @return converted key
     */
    private static SecretKey StringToKey(String keyString){
        byte[] decodeKey = Base64.getDecoder().decode(keyString);
        return new SecretKeySpec(decodeKey, 0, decodeKey.length, "AES");
    }

    /**
     * <h2>KEY TO STRING</h2>
     * <p>Convert the given secret key to a readable string</p>
     * @param key
     * @return converted string
     */
    private static String KeyToString(SecretKey key){
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }
}
