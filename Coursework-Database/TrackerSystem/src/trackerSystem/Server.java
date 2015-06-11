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
                System.out.println("������ �� ���������...");
                connection = serverSocket.accept();
                System.out.println("������ �� ������: " + connection.getInetAddress().getHostName());
                System.out.println("������� ��������� ��� �������...");
                socketOut = new PrintWriter(connection.getOutputStream(), true);
                socketOut
                        .println("�������� ������� �� (��� �������� �� ������ �������, ����������� � : �����_��_�������>�����): |"
                                + "1.������ �����������. |"
                                + "2.������ �������. |"
                                + "3.������ ������� ��� ������� ������ |"
                                + "4.������ ������� �� ������ ������� �� ������� ������ |"
                                + "5.������ ������� �� ����� ������� ���� �� ������� ������ |"
                                + "6.������ ������ �� ����� ������� ���� �� ������� ������ |"
                                + "7.������� ����� ������� ���� �� ������� ������ � �������� ���� |" + "exit |");

                System.out.println("������� ������ �������� �� �������...");
                socketIn = new Scanner(new BufferedReader(new InputStreamReader(connection.getInputStream())));

                String command = null;
                loop: do {
                    socketOut.flush();
                    command = socketIn.nextLine();
                    String[] commands = command.split("\\>");
                    switch (commands[0]) {
                        case "1":
                            System.out.println("�������� ������ ������� 1.");
                            socketOut.println(connectToDB("CALL show_programmers();", commands[0]).toString());
                            break;
                        case "2":
                            System.out.println("�������� ������ ������� 2.");
                            socketOut.println(connectToDB("CALL show_projects();", commands[0]).toString());
                            break;
                        case "3":
                            System.out.println("�������� ������ ������� 3.");
                            String sqlCommand = "CALL show_project_files(\'" + commands[1] + "\');";
                            socketOut.println(connectToDB(sqlCommand, commands[0]).toString());
                            break;
                        case "4":
                            System.out.println("�������� ������ ������� 4.");
                            String sqlCommand1 = "CALL show_all_changes_on_project(\'" + commands[1] + "\');";
                            socketOut.println(connectToDB(sqlCommand1, commands[0]).toString());
                            break;
                        case "5":
                            System.out.println("�������� ������ ������� 5.");
                            String sqlCommand2 = "CALL show_changes_on_selected_project_file(" + commands[1] + ",\'"
                                    + commands[2] + "\');";
                            socketOut.println(connectToDB(sqlCommand2, commands[0]).toString());
                            break;
                        case "6":
                            System.out.println("�������� ������ ������� 6.");
                            String sqlCommand3 = "CALL show_latest_update_on_selected_project_file(" + commands[1]
                                    + ",\'" + commands[2] + "\');";
                            socketOut.println(connectToDB(sqlCommand3, commands[0]).toString());
                            break;
                        case "7":
                            System.out.println("�������� ������ ������� 6.");
                            String sqlCommand4 = "CALL show_changes_on_selected_project_file_by_date(" + commands[1]
                                    + ",\'" + commands[2] + "\'," + "\'" + commands[3] + "\');";
                            socketOut.println(connectToDB(sqlCommand4, commands[0]).toString());
                            break;
                        case "exit":
                            System.out.println("�������� ������ ������� �����");
                            socketOut.println("Bye!");
                            break loop;
                        default:
                            System.out.println("������� ��������� �������");
                            socketOut.println("��������� �������");
                            break;
                    }

                } while (!command.equalsIgnoreCase("exit"));
                System.out.println("�������� �������� � ������� : " + connection.getInetAddress().getHostName());
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
                    System.err.println("�� ���� �� ���� �������� �����");
                }

            }
        }
    }

    // ����� ����� �� ������� ��� SQL ������� � ����� ������ (������) ��� ����,
    // � ��� ����������� � ���������, �� ����� NULL
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
        // localhost � IP �� �������, 3306 � �����, Tracker � ������
        String url = "jdbc:mysql://localhost:3306/Tracker";
        String user = "ridvan";
        String pass = "secret";
        StringBuilder result = new StringBuilder();

        Connection link = Server.connect(url, user, pass);

        if (link == null) {
            result.append("MySQL �� � ������!");
            System.out.println("���, MySQL �� � ������!");
            return result;
        } else {
            System.out.println("������� �� � MySQL �������!");
        }

        // � JDBC Statement � ��������, � ResultSet � ��������� �� ���
        Statement stmt = null;
        ResultSet resultSet = null;

        try {
            stmt = link.createStatement();
            resultSet = stmt.executeQuery(command);
            switch (choice) {
            // �� ��������� ����� ��������� ������������ ������� ��� �� ��� �
            // ��������� � ������
            // result , ����� �� �� ������� �� �������
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
    //������ �� URL �����, ������ ���� �������� �� ������ �����
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
