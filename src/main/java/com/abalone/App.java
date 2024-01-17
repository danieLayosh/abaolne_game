package com.abalone;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("primary", stage), 940, 680);
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml, Stage stage) throws IOException {
        scene.setRoot(loadFXML(fxml, stage));
    }

    private static Parent loadFXML(String fxml, Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/" + fxml + ".fxml"));
        loader.setControllerFactory(param -> {
            if (param == GUI.class) {
                GUI controller = new GUI();
                controller.setStage(stage); // Set the stage here
                return controller;
            }
            // Default behavior for controller creation:
            try {
                return param.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e); // Handle this exception
            }
        });
        return loader.load();
    }

    public static void main(String[] args) {
        launch();
        // GameBoard gameBoard = new GameBoard();
        // gameBoard.printBoardWithCoordinates();
        // Computer computer = new Computer(gameBoard, 1);
        // gameBoard.printBoardScore();
        // computer.printPotentialCells();
        // computer.printAllMoves();
        // gameBoard.printBoardWithCoordinates();
    }

}