package server;

import java.sql.SQLException;

public interface AuthService {
    String getNicknameByLoginAndPassword(String login, String password) throws SQLException, ClassNotFoundException;
    boolean registration(String login, String password, String nickname) throws SQLException, ClassNotFoundException;
    void changeNickname(String oldNick, String newNick);
}
