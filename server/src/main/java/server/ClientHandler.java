package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.io.*;
import java.util.concurrent.*;


public class ClientHandler {
    DataInputStream in;
    DataOutputStream out;
    Server server;
    Socket socket;

    private String nickname;
    private String login;
    private File base;
    ExecutorService clientThread = Executors.newCachedThreadPool();

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            server.logger.info("Client connected " + socket.getRemoteSocketAddress());

            clientThread.execute(() -> {
                try {
                    //socket.setSoTimeout(5000);
                    //цикл аутентификации
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/reg ")) {
                            String[] token = str.split("\\s");
                            if (token.length < 4) {
                                continue;
                            }
                            boolean b = false;
                            try {
                                b = server.getAuthService()
                                        .registration(token[1], token[2], token[3]);
                            } catch (SQLException throwables) {
                                throwables.printStackTrace();
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                            if (b) {
                                sendMsg("/regok");
                                server.logger.info("Успешная регистрация");
                            } else {
                                sendMsg("/regno");
                                server.logger.info("Неудачная попытка регистрации");
                            }
                        }

                        if (str.startsWith("/auth ")) {
                            server.logger.info("Попытка авторизации");
                            String[] token = str.split("\\s");
                            if (token.length < 3) {
                                continue;
                            }
                            String newNick = null;
                            try {
                                try {
                                    newNick = server.getAuthService()
                                            .getNicknameByLoginAndPassword(token[1], token[2]);
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                }
                                System.out.println(newNick);
                            } catch (SQLException throwables) {
                                throwables.printStackTrace();
                            }
                            if (newNick != null) {
                                login = token[1];
                                if (!server.isLoginAuthenticated(login)) {
                                    nickname = newNick;
                                    sendMsg("/authok " + newNick);
                                    server.subscribe(this);
                                    baseAuth();
                                    break;
                                } else {
                                    sendMsg("С этим логином уже вошли в чат");
                                }
                            } else {
                                sendMsg("Неверный логин / пароль");
                            }
                        }
                    }
                    //цикл работы
                    socket.setSoTimeout(0);
                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith("/")) {
                            if (str.equals("/end")) {
                                sendMsg("/end");
                                break;
                            }
                            if (str.startsWith("/changenick ")) {
                                server.logger.info("Попытка смены ника");
                                String[] token = str.split("\\s", 2);
                                server.getAuthService().changeNickname(getNickname(), token[1]);
                            }
                            if (str.startsWith("/w ")) {
                                String[] token = str.split("\\s", 3);
                                if (token.length < 3) {
                                    continue;
                                }
                                server.privateMsg(this, token[1], token[2]);
                            }
                        } else {
                            server.broadcastMsg(this, str);
                        }
                    }


                } catch (SocketTimeoutException e) {
                    sendMsg("/end");
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    server.unsubscribe(this);
                    server.logger.info("Client disconnected " + socket.getRemoteSocketAddress());
                    try {
                        socket.close();
                        in.close();
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickname() {
        return nickname;
    }

    public String getLogin() {
        return login;
    }

    public void baseAuth() throws IOException {
        out.writeUTF("/loadHistory " + getLogin());
    }
}
