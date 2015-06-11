package trackerSystem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class Server {
    public static void main(String args[]) {
        ServerSocket serverSocket = null;
        Socket connection = null;
        Scanner socketIn = null;
        PrintWriter socketOut = null;
        int port = 1994;
        while (true) {
            try {
                serverSocket = new ServerSocket(port);
                System.out.println("Очаква се свързване...");
                connection = serverSocket.accept();
                System.out.println("Свърза се клиент: " + connection.getInetAddress().getHostName());
                System.out.println("Подавам съобщение към клиента...");
                socketOut = new PrintWriter(connection.getOutputStream(), true);
                socketOut
                        .println("Възможни команди са (При задаване на сложна команда, синтакситът е : номер_на_команда>избор): |"
                                + "1.Списък програмисти. |"
                                + "2.Списък проекти. |"
                                + "3.Текущи файлове във зададен проект |"
                                + "4.Списък промени на всички файлове на зададен проект |"
                                + "5.Списък промени на точно зададен файл на зададен проект |"
                                + "6.Текуща версия на точно зададен файл на зададен проект |"
                                + "7.Промени върху зададен файл на зададен проект с посочена дата |" + "exit |");

                System.out.println("Сървъра очаква подаване на команда...");
                socketIn = new Scanner(new BufferedReader(new InputStreamReader(connection.getInputStream())));

                String command = null;
                loop: do {
                    socketOut.flush();
                    command = socketIn.nextLine();
                    String[] commands = command.split("\\>");
                    switch (commands[0]) {
                        case "1":
                            System.out.println("Клиентът подаде команда 1.");
                            socketOut.println(connectToDB("CALL show_programmers();", commands[0]).toString());
                            break;
                        case "2":
                            System.out.println("Клиентът подаде команда 2.");
                            socketOut.println(connectToDB("CALL show_projects();", commands[0]).toString());
                            break;
                        case "3":
                            System.out.println("Клиентът подаде команда 3.");
                            String sqlCommand = "CALL show_project_files(\'" + commands[1] + "\');";
                            socketOut.println(connectToDB(sqlCommand, commands[0]).toString());
                            break;
                        case "4":
                            System.out.println("Клиентът подаде команда 4.");
                            String sqlCommand1 = "CALL show_all_changes_on_project(\'" + commands[1] + "\');";
                            socketOut.println(connectToDB(sqlCommand1, commands[0]).toString());
                            break;
                        case "5":
                            System.out.println("Клиентът подаде команда 5.");
                            String sqlCommand2 = "CALL show_changes_on_selected_project_file(" + commands[1] + ",\'"
                                    + commands[2] + "\');";
                            socketOut.println(connectToDB(sqlCommand2, commands[0]).toString());
                            break;
                        case "6":
                            System.out.println("Клиентът подаде команда 6.");
                            String sqlCommand3 = "CALL show_latest_update_on_selected_project_file(" + commands[1]
                                    + ",\'" + commands[2] + "\');";
                            socketOut.println(connectToDB(sqlCommand3, commands[0]).toString());
                            break;
                        case "7":
                            System.out.println("Клиентът подаде команда 6.");
                            String sqlCommand4 = "CALL show_changes_on_selected_project_file_by_date(" + commands[1]
                                    + ",\'" + commands[2] + "\'," + "\'" + commands[3] + "\');";
                            socketOut.println(connectToDB(sqlCommand4, commands[0]).toString());
                            break;
                        case "exit":
                            System.out.println("Клиентът подаде команда изход");
                            socketOut.println("Bye!");
                            break loop;
                        default:
                            System.out.println("Получих непозната команда");
                            socketOut.println("Непозната команда");
                            break;
                    }

                } while (!command.equalsIgnoreCase("exit"));
                System.out.println("Затварям връзката с клиента : " + connection.getInetAddress().getHostName());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (socketIn != null)
                        socketIn.close();
                    if (socketOut != null)
                        socketOut.close();
                    if (connection != null)
                        connection.close();
                    if (serverSocket != null)
                        serverSocket.close();
                } catch (IOException e) {
                    System.err.println("Не може да бъде затворен сокет");
                }

            }
        }
    }

    // метод който се свързва със SQL сървъра и връща ресурс (връзка) към него,
    // а ако свързването е неуспешно, ще върне NULL
    private static Connection connect(String url, String user, String password) {
        Connection result = null;
        try {
            result = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static StringBuilder connectToDB(String command, String choice) {
        // localhost е IP на сървъра, 3306 е порта, Tracker е базата
        String url = "jdbc:mysql://localhost:3306/Tracker";
        String user = "ridvan";
        String pass = "secret";
        StringBuilder result = new StringBuilder();

        Connection link = Server.connect(url, user, pass);

        if (link == null) {
            result.append("MySQL не е пуснат!");
            System.out.println("Опа, MySQL не е пуснат!");
            return result;
        } else {
            System.out.println("Свързах се с MySQL сървъра!");
        }

        // В JDBC Statement е заявката, а ResultSet е резултата от нея
        Statement stmt = null;
        ResultSet resultSet = null;

        try {
            stmt = link.createStatement();
            resultSet = stmt.executeQuery(command);
            switch (choice) {
            // По зададения избор обхождаме резултатната таблица ред по ред и
            // записваме в полето
            // result , което ще се изпрати на клиента
                case "1":
                    while (resultSet.next()) {
                        result.append("first name: " + resultSet.getString("first_name") + ", last name: "
                                + resultSet.getString("last_name") + "|");
                    }
                    break;
                case "2":
                    while (resultSet.next()) {
                        result.append("id: " + resultSet.getInt("id") + ", name: " + resultSet.getString("name")
                                + "project path : " + resultSet.getString("project_path") + ", start date: "
                                + resultSet.getDate("start_date") + "|");
                    }
                    break;
                case "3":
                    while (resultSet.next()) {
                        result.append("ID: " + resultSet.getInt("id") + " Updated by : "
                                + resultSet.getString("first_name") + "  " + resultSet.getString("last_name")
                                + " Source code: |" + readFrom(resultSet.getString("source_code")));
                    }
                    break;
                case "4":
                    while (resultSet.next()) {
                        result.append("ID: " + resultSet.getInt("id") + " Previous change id: "
                                + resultSet.getInt("prev_change") + " File id: " + resultSet.getInt("files_id")
                                + " Programmer id: " + resultSet.getInt("programmer_id") + " Code path: "
                                + resultSet.getString("new_change") + " Date: " + resultSet.getDate("date") + "|");
                    }
                    break;
                case "5":
                    while (resultSet.next()) {
                        result.append("ID: " + resultSet.getInt("id") + " Previous change id: "
                                + resultSet.getInt("prev_change") + " Updated by : "
                                + resultSet.getString("first_name") + "  " + resultSet.getString("last_name")
                                + " Date: " + resultSet.getDate("date") + " Source code: |"
                                + readFrom(resultSet.getString("new_change")));
                    }
                    break;
                case "6":
                    while (resultSet.next()) {
                        result.append("ID: " + resultSet.getInt("id") + " Previous change id: "
                                + resultSet.getInt("prev_change") + " Updated by : "
                                + resultSet.getString("first_name") + "  " + resultSet.getString("last_name")
                                + " Date: " + resultSet.getDate("date") + " Source code: |"
                                + readFrom(resultSet.getString("new_change")));
                    }
                    break;
                case "7":
                    while (resultSet.next()) {
                        result.append("ID: " + resultSet.getInt("id") + " Previous change id: "
                                + resultSet.getInt("prev_change") + " Updated by : "
                                + resultSet.getString("first_name") + "  " + resultSet.getString("last_name")
                                + " Source code: |" + readFrom(resultSet.getString("new_change")));
                    }
                    break;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
                if (resultSet != null)
                    resultSet.close();
                if (link != null)
                    link.close();
            } catch (SQLException e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }
    //Четене на URL адрес, върнат като резултат от базата данни
    private static String readFrom(String URL) {
        File file = new File(URL);
        StringBuilder strBuilder = new StringBuilder();
        BufferedReader inputStream = null;
        try {
            inputStream = new BufferedReader(new FileReader(file));

            String l;
            while ((l = inputStream.readLine()) != null) {
                strBuilder.append(l + "|");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return strBuilder.toString();
    }
}
