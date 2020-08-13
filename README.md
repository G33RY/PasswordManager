# Password Manager (Java)
Password Manager developed in Java with the Maven framework.
The desktop client communicating with the server with Java sockets.

> **Note:** In the current build the client can't register new account but the server can handle register requests 

## Future plans 

 - **Android Client**
 - **Chrome Extension:** The extension will be accessible to your account and able to add password to your account

## Launch client and server

In the `DEPLOYED`  folder there is the launcher command for both the client and the server.

# How does the communication work? 

The communication between the client and the server works with request keywords. The client send the keyword to the server with the data needed, then the server process the data and send back a response code and the return data.

### Request Keywords

 - **getPublicKey:** send the client's secret key
 - **login:** compare input values with the values in the **SQLITE** database 
 - **getAccounts:** send the accounts linked to the input user
 - **addAccount:** create an account with the input values and link it to the user 
 - **editAccount:** change an account values to the input data where the account's id is equals to the given id
 - **deleteAccount:** delete an account row where the id equals to the given id
 - **addUser:** create a user with the input values
 - **editUser:** change a user values to the input data where the username is equals to the given username
 - **deleteUser:** delete an account row where the username is equals to the given username

## Encryption

**Algorithm:** AES

The encrypted data from the client is encrypted with the secret key linked to the client's mac address.\
The encrypted data in the database is encrypted with the secret key in the user's row.

![GRAPH](https://i.imgur.com/GAlWskL.png)
