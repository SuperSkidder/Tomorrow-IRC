package superskidder.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import superskidder.QQ.ChatClient;
import superskidder.QQ.QQTest;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("gui.fxml"));
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        QQTest.clientTest = new ChatClient("127.0.0.1", 8404);
        QQTest.clientTest.start();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
