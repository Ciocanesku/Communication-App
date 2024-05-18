package com.communication.sockets.var2;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.Scanner;

public class Server {
    private static final Object lock = new Object();

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(7777);
            System.out.println("Server started at 7777");

            Socket socket = serverSocket.accept();

            // Create data input and output streams
            DataInputStream inputFromClient = new DataInputStream(socket.getInputStream());
            DataOutputStream outputToClient = new DataOutputStream(socket.getOutputStream());

            // Database connection
            Connection connection = DriverManager.getConnection(
                    "jdbc:mysql://127.0.0.1:3306/login_schema",
                    "root",
                    "Ciocanandrei23"
            );

            // Thread for receiving messages from client
            Thread receiveThread = new Thread(() -> {
                try {
                    // Read the first message, which is expected to be the username
                    String username = inputFromClient.readUTF();
                    System.out.println(username + " connected");

                    // Read the password
                    String password = inputFromClient.readUTF();

                    // Authenticate user against database
                    boolean authenticated = authenticateUser(connection, username, password);

                    // Send authentication result to client
                    synchronized (lock) {
                        outputToClient.writeBoolean(authenticated);
                        outputToClient.flush();
                    }

                    if (authenticated) {
                        System.out.println("Authentication successful for " + username);
                    } else {
                        System.out.println("Authentication failed for " + username);
                        return; // Exit the thread if authentication fails
                    }

                    while (true) {
                        String messageType = inputFromClient.readUTF();
                        if (messageType.equals("TEXT")) {
                            String msgread = inputFromClient.readUTF();
                            if (msgread.equals("NO MORE")) {
                                break;
                            }
                            // Print the message
                            System.out.println(username + ": " + msgread);
                        } else if (messageType.equals("FILE")) {
                            receiveFile(inputFromClient);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
            receiveThread.start();

            // Thread for sending messages to client
            Thread sendThread = new Thread(() -> {
                try {
                    Scanner scanner = new Scanner(System.in);
                    while (true) {
                        String msgwrite = scanner.nextLine();

                        if (msgwrite.equals("NO MORE")) {
                            break;
                        }

                        if (msgwrite.startsWith("FILE:")) {
                            sendFile(msgwrite.substring(5), outputToClient);
                        } else {
                            synchronized (lock) {
                                outputToClient.writeUTF("TEXT");
                                outputToClient.writeUTF(msgwrite);
                                outputToClient.flush();
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            sendThread.start();

            // Wait for both threads to finish
            receiveThread.join();
            sendThread.join();

            // Close resources
            socket.close();
            serverSocket.close();
        } catch (IOException | InterruptedException | SQLException ex) {
            ex.printStackTrace();
        }
    }

    // Method to authenticate user against database
    private static boolean authenticateUser(Connection connection, String username, String password) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM USERS WHERE username = ? AND password = ?");
        statement.setString(1, username);
        statement.setString(2, password);
        ResultSet resultSet = statement.executeQuery();
        return resultSet.next(); // If there's a matching user, authentication succeeds
    }

    // Method to receive file from client
    private static void receiveFile(DataInputStream inputFromClient) throws IOException {
        String fileName = inputFromClient.readUTF();
        long fileSize = inputFromClient.readLong();
        FileOutputStream fileOutputStream = new FileOutputStream("./files/ServerFromClient/server_" + fileName);
        byte[] buffer = new byte[1024];
        int bytesRead;
        long totalBytesRead = 0;
        while (totalBytesRead < fileSize && (bytesRead = inputFromClient.read(buffer, 0, (int)Math.min(buffer.length, fileSize - totalBytesRead))) != -1) {
            fileOutputStream.write(buffer, 0, bytesRead);
            totalBytesRead += bytesRead;
        }
        fileOutputStream.close();
        System.out.println("File received: " + fileName);
    }


    // Method to send file to client
    private static void sendFile(String filePath, DataOutputStream outputToClient) throws IOException {
        File file = new File(filePath);
        if (file.exists()) {
            outputToClient.writeUTF("FILE");
            outputToClient.writeUTF(file.getName());
            outputToClient.writeLong(file.length());
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputToClient.write(buffer, 0, bytesRead);
            }
            outputToClient.flush(); // Make sure to flush the stream
            fileInputStream.close();
            System.out.println("File sent: " + file.getName());
        } else {
            System.out.println("File not found: " + filePath);
        }
    }
}
