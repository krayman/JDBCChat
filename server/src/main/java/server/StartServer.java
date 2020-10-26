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
    static Thread tryTread = new Thread(()->{
        try {
            connect();
            prepareAllStatements();
           prepFill("hello", "123", "HALUU");
            System.out.println(exSelect("zxc"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
            );
    private static void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:main.db");
        stmt = connection.createStatement();
    }

    private static void prepFill(String login, String password, String nickname) throws SQLException {
      psInsert.setString(1, login);
      psInsert.setString(2, password);
      psInsert.setString(3, nickname);
      psInsert.executeUpdate();
    }

    private static void prepareAllStatements() throws SQLException {
        psInsert = connection.prepareStatement("INSERT INTO auth (login, password, nickname) VALUES (? ,?, ?);");

    }
    private static String exSelect(String login) throws SQLException {
        PreparedStatement psSelect;
        psSelect = connection.prepareStatement("SELECT login, password, nickname FROM auth WHERE login = ?");
        psSelect.setString(1, login);
        ResultSet rs = psSelect.executeQuery();
        String result = rs.getString(2);
        rs.close();
        return result;
    }
}
