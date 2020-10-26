package server;

import java.util.ArrayList;
import java.util.List;
import java.sql.*;

public class DBAuthService implements AuthService {
    private static Connection connection;
    private static Statement stmt;
    private static PreparedStatement psInsert;

    private class UserData {
        String login;
        String password;
        String nickname;

        public UserData(String login, String password, String nickname) {
            this.login = login;
            this.password = password;
            this.nickname = nickname;
        }
    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) throws SQLException, ClassNotFoundException {
        connect();
        PreparedStatement psSelect;
        psSelect = connection.prepareStatement("SELECT login, password, nickname FROM auth WHERE login = ?");
        psSelect.setString(1, login);
        ResultSet rs = psSelect.executeQuery();
        if (rs.getString(1).equals(login) && rs.getString(2).equals(password)) {
            String result = rs.getString(3);
            rs.close();
            return result;
        }
        return null;
    }

    @Override
    public boolean registration(String login, String password, String nickname) {
        try {
            connect();
            psInsert = connection.prepareStatement("INSERT INTO auth (login, password, nickname) VALUES (? ,?, ?);");
            psInsert.setString(1, login);
            psInsert.setString(2, password);
            psInsert.setString(3, nickname);
            psInsert.executeUpdate();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
        return true;
    }


    private static void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:main.db");
        stmt = connection.createStatement();
    }

    @Override
    public void changeNickname(String oldNick, String newNick) {
        PreparedStatement psUpdate;
        try {
            psUpdate = connection.prepareStatement("UPDATE auth SET nickname = ? WHERE nickname = ?");
            psUpdate.setString(1, newNick);
            psUpdate.setString(2, oldNick);
            psUpdate.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
