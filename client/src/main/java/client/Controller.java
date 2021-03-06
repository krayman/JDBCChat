package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    public TextArea spaceTop;
    @FXML
    public Label spaceBot;
    @FXML
    public TextField nickField;
    @FXML
    public Button visNick;
    @FXML
    public Button chngNick;
    @FXML
    private ListView<String> clientList;
    @FXML
    private TextArea textArea;
    @FXML
    private TextField textField;
    @FXML
    private HBox authPanel;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private HBox msgPanel;

    private final String IP_ADDRESS = "localhost";
    private final int PORT = 2548;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private Stage stage;
    private Stage regStage;
    private RegController regController;

    private boolean authenticated;
    private String nickname;
    private File base;
    private String basePath;

    private void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);
        msgPanel.setVisible(authenticated);
        msgPanel.setManaged(authenticated);
        clientList.setVisible(authenticated);
        clientList.setManaged(authenticated);
        spaceTop.setVisible(authenticated);
        spaceBot.setManaged(authenticated);
        spaceBot.setVisible(authenticated);
        visNick.setVisible(authenticated);
        visNick.setManaged(authenticated);


        if (!authenticated) {
            nickname = "";
            setTitle("ВСКАЙПЕ");
        } else {
            setTitle(String.format("[ %s ] - ВСКАЙПЕ", nickname));
        }
        textArea.clear();
    }

    Thread timeThread = new Thread(() -> {

        while (true) {
            time();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    });

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            stage = (Stage) textField.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                if (socket != null && !socket.isClosed()) {
                    try {
                        out.writeUTF("/end");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        });

        setAuthenticated(false);
        createRegWindow();
        timeThread.setDaemon(true);
        timeThread.start();
    }


    private void connect() {
        try {
            socket = new Socket(IP_ADDRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    //цикл аутентификации
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/authok ")) {
                            nickname = str.split("\\s")[1];
                            setAuthenticated(true);
                            break;
                        }

                        if (str.startsWith("/regok")) {
                            regController.addMessageTextArea("Регистрация прошла успешно");

                        }
                        if (str.startsWith("/regno")) {
                            regController.addMessageTextArea("Зарегистрироватся не удалось\n" +
                                    " возможно такой логин или никнейм уже заняты");
                        }

                        textArea.appendText(str + "\n");
                    }


                    //цикл работы
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/")) {
                            if (str.equals("/end")) {
                                recordHistory();
                                break;
                            }
                            if (str.startsWith("/loadHistory ")) {
                                String[] logArr = str.split("\\s");
                                baseAuth(logArr[1]);
                                setHistory(logArr[1]);
                            }

                            if (str.startsWith("/clientlist ")) {
                                String[] token = str.split("\\s");
                                Platform.runLater(() -> {
                                    clientList.getItems().clear();
                                    for (int i = 1; i < token.length; i++) {
                                        clientList.getItems().add(token[i]);
                                    }
                                });
                            }
                        } else {
                            textArea.appendText(str + "\n");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    setAuthenticated(false);
                    try {
                        socket.close();
                        in.close();
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(ActionEvent actionEvent) {
        if (textField.getText().trim().length() == 0) {
            return;
        }
        try {
            out.writeUTF(textField.getText());
            textField.clear();
            textField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToAuth(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connect();
        }

        String msg = String.format("/auth %s %s",
                loginField.getText().trim(), passwordField.getText().trim());
        try {
            out.writeUTF(msg);
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setTitle(String title) {
        Platform.runLater(() -> {
            stage.setTitle(title);
        });
    }

    public void clickClientList(MouseEvent mouseEvent) {
        textField.setText(String.format("/w %s ", clientList.getSelectionModel().getSelectedItem()));
    }

    public void releasedMouseClientList(MouseEvent mouseEvent) {
        System.out.println(clientList.getSelectionModel().getSelectedItem());
        System.out.println(mouseEvent.getButton());
        System.out.println(mouseEvent.getClickCount());
    }

    private void createRegWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/reg.fxml"));
            Parent root = fxmlLoader.load();
            regStage = new Stage();
            regStage.setTitle("Регистрация в чате ВСКАЙПЕ");
            regStage.setScene(new Scene(root, 400, 300));
            regStage.initModality(Modality.APPLICATION_MODAL);

            regController = fxmlLoader.getController();
            regController.setController(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void regStageShow(ActionEvent actionEvent) {
        regStage.show();
    }

    public void tryRegistration(String login, String password, String nickname) {
        String msg = String.format("/reg %s %s %s", login, password, nickname);

        if (socket == null || socket.isClosed()) {
            connect();
        }

        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void time() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy 'at' HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());
        String datePrint = formatter.format(date);
        //System.out.println(formatter.format(date));
        spaceTop.setText(datePrint);
    }

    public void changeNick(ActionEvent actionEvent) {
        String msg = String.format("/changenick %s", nickField.getText());
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
        nickField.setVisible(false);
        nickField.setManaged(false);
        chngNick.setVisible(false);
        chngNick.setManaged(false);
    }

    public void visibleNick(ActionEvent actionEvent) {
        nickField.setVisible(true);
        nickField.setManaged(true);
        chngNick.setVisible(true);
        chngNick.setManaged(true);
        visNick.setVisible(false);
        visNick.setManaged(false);
    }

    public void setHistory(String login) throws IOException {

        List listOfHistory = Files.readAllLines(Paths.get(basePath));
        if (listOfHistory.size() > 100) {
            for (int i = listOfHistory.size() - 100; i < listOfHistory.size(); i++) {
                textArea.appendText((String)listOfHistory.get(i));
                textArea.appendText("\n");
            }
        } else {
            for (Object message : listOfHistory) {
                textArea.appendText((String) message);
                textArea.appendText("\n");
            }
        }
        textArea.setScrollTop(0);
        textArea.setScrollLeft(0);
    }

    public void baseAuth(String login) throws IOException {
        basePath = "base/History " + login + ".txt";
        base = new File(basePath);
        base.createNewFile();
    }

    public void recordHistory() {
        try (FileOutputStream out = new FileOutputStream(basePath)) {
            out.write(textArea.getText().getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
