package com.abalone;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.abalone.enums.MoveType;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

public class GUI {
    private GameBoard gameBoard = new GameBoard();;
    private int player;
    private List<Cell> marbles;
    private Cell dest;
    private IntegerProperty blue_score;
    private IntegerProperty red_score;

    public GUI() {
        this.marbles = new ArrayList<>(); // Initialize the list here
        this.dest = null;
        this.player = 1;
        this.blue_score = new SimpleIntegerProperty(0);
        this.red_score = new SimpleIntegerProperty(0);
    }

    @FXML
    private Label playerTurn;

    @FXML
    private Label redPoint;

    @FXML
    private Label bluePoint;

    @FXML
    private Button bt0_0, bt0_1, bt0_2, bt0_3, bt0_4,
            bt1_0, bt1_1, bt1_2, bt1_3, bt1_4, bt1_5,
            bt2_0, bt2_1, bt2_2, bt2_3, bt2_4, bt2_5, bt2_6,
            bt3_0, bt3_1, bt3_2, bt3_3, bt3_4, bt3_5, bt3_6, bt3_7,
            bt4_0, bt4_1, bt4_2, bt4_3, bt4_4, bt4_5, bt4_6, bt4_7, bt4_8,
            bt5_0, bt5_1, bt5_2, bt5_3, bt5_4, bt5_5, bt5_6, bt5_7,
            bt6_0, bt6_1, bt6_2, bt6_3, bt6_4, bt6_5, bt6_6,
            bt7_0, bt7_1, bt7_2, bt7_3, bt7_4, bt7_5,
            bt8_0, bt8_1, bt8_2, bt8_3, bt8_4;

    public void initialize() {
        redPoint.textProperty().bind(red_score.asString());
        bluePoint.textProperty().bind(blue_score.asString());
        // Iterate over all cells in the game board and link them to buttons
        for (Cell cell : gameBoard.getCells()) {
            try {
                String cellId = "bt" + cell.getX() + "_" + cell.getY();
                Field field = getClass().getDeclaredField(cellId);
                field.setAccessible(true);
                Button cellButton = (Button) field.get(this);
                if (cellButton != null) {
                    cell.setBt(cellButton);
                    updateCellGUI(cell);
                    cellButton.setOnAction(event -> turn(cell));
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                System.err.println("Error linking cell to button: " + e.getMessage());
                // Handle exception or log error
            }
        }
    }

    public void updateCellGUI(Cell cell) {
        Image image = new Image("abalone0.gif");

        switch (cell.getState()) {
            case 1:
                image = new Image("abalone1.gif");
                break;
            case 2:
                image = new Image("abalone2.gif");
                break;
            default:
                break;
        }
        ImageView imageView = new ImageView(image);

        // imageView.setFitHeight(50); // Set the height of the image
        imageView.setFitWidth(35); // Set the width of the image
        imageView.setPreserveRatio(true);
        cell.getBt().setGraphic(imageView);

    }

    public void updateBoard(Cell cell) {
        for (Cell cell2 : gameBoard.getCells()) {
            updateCellGUI(cell2);
        }
    }

    public void turn(Cell cell) {
        // if the marble is already pushed remove her
        if (marbles.contains(cell)) {
            marbles.remove(cell);
            updateBoard(cell);
        } else if (cell.getState() == player) {
            if (marbles.isEmpty()) {
                marbles.add(cell);
                cellPushed(cell);
            } else {
                if (marbles.size() < 3) {
                    marbles.add(cell);
                    cellPushed(cell);
                } else {
                    System.out.println("To much marbles");
                    for (Cell cell2 : marbles) {
                        updateCellGUI(cell2);
                    }
                    marbles.clear();
                    marbles.add(cell);
                    cellPushed(cell);
                    System.out.println("Choose again please");
                }
            }
        } else {
            if (cell.getState() == 0 || cell.getState() == (player == 1 ? 2 : 1) && !marbles.isEmpty()) {
                dest = cell;
                Move move = new Move(marbles, cell, player);
                if (move.isValid()) {
                    System.out.println("Move to " + move.getDirectionToDest() + " direction is valid.");
                    MoveType moveType = move.getMoveType();
                    System.out.println("The MoveType is: " + moveType);

                    move.executeMove();
                    updateBoard(cell);
                    if (move.getMoveType() == MoveType.OUT_OF_THE_BOARD) {
                        if (player == 1) {
                            red_score.set(red_score.get() + 1);
                        } else {
                            blue_score.set(blue_score.get() + 1);
                        }
                        if (red_score.get() == 6 || blue_score.get() == 6) {
                            endGame();
                        }
                    }
                    changePlayer();
                    for (Cell cell2 : marbles) {
                        updateCellGUI(cell2);
                    }
                    marbles.clear();
                    System.out.println("Move executed.");
                }
            } else {
                for (Cell cell2 : marbles)
                    updateCellGUI(cell2);

                marbles.clear();
                System.out.println("Move is invalid.");
            }
        }

    }

    private void endGame() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Game Over");
        if (player == 1) {
            alert.setHeaderText("THE RED PLAYER WON!!!!");
        } else
            alert.setHeaderText("THE BLUE PLAYER WON!!!!");

        alert.setContentText("Do you want to play another game?");

        ButtonType buttonTypeYes = new ButtonType("Yes");
        ButtonType buttonTypeNo = new ButtonType("No, exit");

        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        alert.showAndWait().ifPresent(response -> {
            if (response == buttonTypeYes) {
                restartGame();
            } else if (response == buttonTypeNo) {
                System.exit(0); // or close the window
            }
        });
    }

    private void restartGame() {
        // Reset all necessary variables and UI components to start a new game
        // Example:
        this.player = 2;
        this.blue_score = new SimpleIntegerProperty(0);
        this.red_score = new SimpleIntegerProperty(0);
        this.gameBoard = new GameBoard();
        this.marbles.clear();

        initialize();
    }

    private void changePlayer() {
        player = (player == 1 ? 2 : 1);
        if (player == 2) {
            playerTurn.setText("Blue Turn");
            playerTurn.setTextFill(Color.BLUE);
        }
        if (player == 1) {
            playerTurn.setText("Red Turn");
            playerTurn.setTextFill(Color.RED);
        }
    }

    private void cellPushed(Cell cell) {
        Image image = null;
        switch (cell.getState()) {
            case 1:
                image = new Image("abalone_red_pushed.gif");
                break;
            case 2:
                image = new Image("abalone_blue_pushed.gif");
                break;
        }
        ImageView imageView = new ImageView(image);

        imageView.setFitWidth(35);
        imageView.setPreserveRatio(true);
        cell.getBt().setGraphic(imageView);
    }
}
