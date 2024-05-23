package com.communication.sockets.var2;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.Scanner;

public class Client {

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 7777);

            ObjectOutputStream outputToServer = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inputFromServer = new ObjectInputStream(socket.getInputStream());

            Scanner scanner = new Scanner(System.in);
            while (true) {
                // Request username and password from user
                System.out.print("Enter username: ");
                String username = scanner.nextLine();
                System.out.print("Enter password: ");
                String password = scanner.nextLine();

                // Create and send ClientData object to server
                ClientData clientData = new ClientData(username, password);
                outputToServer.writeObject(clientData);
                outputToServer.flush();

                // Receive authentication result from server
                boolean authenticated = inputFromServer.readBoolean();

                if (authenticated) {
                    System.out.println("Authentication successful.");
                    break; // Exit the authentication loop
                } else {
                    System.out.println("Authentication failed. Please try again.");
                }
            }

            // Start a separate thread to continuously receive messages from the server
            Thread readThread = new Thread(() -> {
                try {
                    while (true) {
                        String messageType = inputFromServer.readUTF();
                        if (messageType.equals("TEXT")) {
                            String msgread = inputFromServer.readUTF();
                            // Print the message
                            System.out.println("server: " + msgread);
                        } else if (messageType.equals("FILE")) {
                            receiveFile(inputFromServer);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            readThread.start();

            while (true) {
                String msgwrite = scanner.nextLine();

                if (msgwrite.equals("NO MORE")) {
                    break;
                }

                if (msgwrite.startsWith("FILE:")) {
                    sendFile(msgwrite.substring(5), outputToServer);
                } else {
                    outputToServer.writeUTF("TEXT");
                    outputToServer.writeUTF(msgwrite);
                    outputToServer.flush(); // Make sure to flush the stream
                }
            }

            // Close resources
            readThread.join(); // Wait for readThread to finish before closing socket
            socket.close();
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    // Method to receive file from server
    private static void receiveFile(ObjectInputStream inputFromServer) throws IOException {
        String fileName = inputFromServer.readUTF();
        long fileSize = inputFromServer.readLong();
        FileOutputStream fileOutputStream = new FileOutputStream("./files/ClientFromServer/client_" + fileName);
        byte[] buffer = new byte[1024];
        int bytesRead;
        long totalBytesRead = 0;
        while (totalBytesRead < fileSize && (bytesRead = inputFromServer.read(buffer, 0, (int)Math.min(buffer.length, fileSize - totalBytesRead))) != -1) {
            fileOutputStream.write(buffer, 0, bytesRead);
            totalBytesRead += bytesRead;
        }
        fileOutputStream.close();
        System.out.println("File received: " + fileName);
    }

    // Method to send file to server
    private static void sendFile(String filePath, ObjectOutputStream outputToServer) throws IOException {
        File file = new File(filePath);
        if (file.exists()) {
            outputToServer.writeUTF("FILE");
            outputToServer.writeUTF(file.getName());
            outputToServer.writeLong(file.length());
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputToServer.write(buffer, 0, bytesRead);
            }
            outputToServer.flush(); // Make sure to flush the stream
            fileInputStream.close();
            System.out.println("File sent: " + file.getName());
        } else {
            System.out.println("File not found: " + filePath);
        }
    }
}
