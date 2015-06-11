package trackerSystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String args[]) {
        Socket connection = null;
        Scanner socketIn = null;
        PrintWriter socketOut = null;
        Scanner keyboardIn = new Scanner(System.in);
        int port = 1994;
        String host = "localhost";

        try {
            System.out.println("Очаквам свързване със сървъра");
            try {
                connection = new Socket(host, port);
            } catch (ConnectException e) {
                System.err.println("Не мога да осъществя връзка със сървъра");
                return;
            }
            System.out.println("Свързването със сървъра беше успешно!");

            System.out.println("Приемам първоначално съобщение от сървъра...");
            socketIn = new Scanner(new BufferedReader(new InputStreamReader(connection.getInputStream())));
            String message = socketIn.nextLine();
            String[] messages = message.split("\\|");
            for (String s : messages) {
                System.out.println(s);
            }
            String command = null;
            socketOut = new PrintWriter(connection.getOutputStream(), true);
            do {
                socketOut.flush();
                System.out.print("Въведете команда: ");
                command = keyboardIn.nextLine();
                socketOut.println(command.toLowerCase());
                System.out.println("Изчакване на отговор от сървъра...");
                String answer = socketIn.nextLine();
                String[] table = answer.split("\\|");
                System.out.println("Сървъра отговори: ");
                for (String s : table) {
                    System.out.println(s);
                }
            } while (!command.equalsIgnoreCase("exit"));
            System.out.println("Затваряне на връзката...");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                keyboardIn.close();
                if (socketIn != null)
                    socketIn.close();
                if (socketOut != null)
                    socketOut.close();
                if (connection != null)
                    connection.close();
            } catch (IOException e) {
                System.err.println("Не може да се затвори сокета");
            }
        }
    }
}
