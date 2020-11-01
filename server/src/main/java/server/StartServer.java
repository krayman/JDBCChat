package server;

import java.sql.*;

public class StartServer {
    private static Connection connection;
    private static Statement stmt;
    private static PreparedStatement psInsert;

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        new Server();
        connect();
        prepareAllStatements();
        //tryTread.start();
    }

    private static void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:main.db");
        stmt = connection.createStatement();
    }

    private static void prepareAllStatements() throws SQLException {
        psInsert = connection.prepareStatement("INSERT INTO auth (login, password, nickname) VALUES (? ,?, ?);");

    }

}
