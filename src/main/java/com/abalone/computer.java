package com.abalone;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.abalone.enums.Direction;
// import com.abalone.enums.MoveType;

public class Computer {
    private int player;
    private Map<Cell, Map<Cell, Direction>> board;
    private List<Cell> myMarbles; // List to store your marbles
    private List<Cell> opponentsMarbles; // List to store opponents marbles
    private List<Cell> myCellsToMoveTo;
    private List<Cell> opponentsCellsToMoveTo;
    private ArrayList<Move> moves;

    public Computer(GameBoard gameBoard, int player) {
        this.board = gameBoard.getBoard();
        this.player = player;

        this.myCellsToMoveTo = new ArrayList<>(board.keySet());
        this.opponentsCellsToMoveTo = new ArrayList<>(board.keySet());
        this.moves = new ArrayList<>();
        this.myMarbles = new ArrayList<>();
        this.opponentsMarbles = new ArrayList<>();

        cellsToMoveTo(); // Filter cells to move to
        updateMarblesList(); // Update the list of your marbles
        // printDebugInfo(); // Add this for debugging

        generateValidMoves(myMarbles);// Generate all valid moves
    }

    private void cellsToMoveTo() {
        myCellsToMoveTo.removeIf(cell -> !isNeighborOfPlayerCell(cell));
        opponentsCellsToMoveTo.removeIf(cell -> !isNeighborOfOpponentCell(cell));
    }

    private boolean isNeighborOfOpponentCell(Cell cell) {
        for (Cell neighbor : cell.getNeighbors()) {
            if (neighbor.getState() == ((player == 1) ? 2 : 1)) {
                // For each neighbor of the cell, check if it is empty or belongs to the
                // opponent
                return cell.getState() == 0 || cell.getState() == player;
            }
        }
        return false;
    }

    // Check if a cell is a neighbor of at least one of the player's cells and is
    // either empty or belongs to the opponent
    private boolean isNeighborOfPlayerCell(Cell cell) {
        for (Cell neighbor : cell.getNeighbors()) {
            if (neighbor.getState() == player) {
                // For each neighbor of the cell, check if it is empty or belongs to the
                // opponent
                return cell.getState() == 0 || cell.getState() != player;
            }
        }
        return false;
    }

    public void printPotentialCells() {
        for (Cell cell : myCellsToMoveTo) {
            System.out.print(cell.formatCoordinate() + ": " + cell.getScore() + ", ");
        }
    }

    // Update the list of your marbles
    private void updateMarblesList() {
        myMarbles.clear(); // Clear existing marbles
        opponentsMarbles.clear(); // Clear existing marbles
        for (Cell cell : board.keySet()) {
            if (cell.getState() == player) {
                myMarbles.add(cell); // Add the cell if it belongs to the player
            } else if (cell.getState() != 0) {
                opponentsMarbles.add(cell);
            }
        }
    }

    // Method to generate all valid moves
    private void generateValidMoves(List<Cell> marbles) {
        moves.clear();
        for (int i = 0; i < marbles.size(); i++) {
            for (int j = i; j < marbles.size(); j++) {
                for (int k = j; k < marbles.size(); k++) {
                    List<Cell> marblesToMove = new ArrayList<>();
                    marblesToMove.add(marbles.get(i));
                    if (j != i)
                        marblesToMove.add(marbles.get(j));
                    if (k != j && k != i)
                        marblesToMove.add(marbles.get(k));

                    for (Cell destination : (marbles.get(0).getState() == player) ? myCellsToMoveTo
                            : opponentsCellsToMoveTo) {
                        Move potentialMove = new Move(marblesToMove, destination, player);
                        if (potentialMove.isValid() && !moves.contains(potentialMove)) {
                            moves.add(potentialMove);
                        }
                    }
                }
            }
        }
    }

    public void printAllMoves() {
        if (moves.isEmpty()) {
            System.out.println("No moves available.");
            return;
        }
        for (Move move : moves) {
            System.out.println(move.toString());
        }
    }

    /*
     * private void printDebugInfo() {
     * // Print cellsToMoveTo for debugging
     * System.out.println("Cells To Move To: " + cellsToMoveTo.stream()
     * .map(Cell::formatCoordinate)
     * .collect(Collectors.joining(", ")));
     * // Print board cells and their states
     * System.out.println("Board Cells:");
     * board.keySet().forEach(cell -> System.out.println(cell.formatCoordinate() +
     * ": State " + cell.getState()));
     * }
     */

    public Move computerTurn() {
        if (moves.isEmpty()) {
            // No valid moves available, return null or handle this case as needed
            return null;
        }

        double bestEvaluation = 0;
        Move bestMove = moves.get(0);
        ArrayList<Move> bestMoves = new ArrayList<>();
        bestMoves.add(bestMove);
        for (Move move : moves) {
            double evaluation = evaluatesBoardState(move);
            if (evaluation >= bestEvaluation) {
                bestMoves.add(move);
                System.out.println(move.toString() + " The score is: " + evaluation);
                bestMove = move;
                bestEvaluation = evaluation;
            }
        }

        // ArrayList<Move> bestMoves = new ArrayList<>();
        // for (Move move : moves) {
        // if (move.getMoveType() == MoveType.INLINE || move.getMoveType() ==
        // MoveType.OUT_OF_THE_BOARD) {
        // moves2.add(move);
        // }
        // }
        // Choose a random move from the list of possible moves
        Random random = new Random();
        int randomIndex;
        if (bestMoves.size() > 0) {
            randomIndex = random.nextInt(bestMoves.size());
        } else
            randomIndex = 0;
        bestMove = bestMoves.get(randomIndex);
        System.out.println("The best move is: " + bestMove.toString() + " The score is: " + bestEvaluation);

        return bestMove;
    }

    private double evaluatesBoardState(Move move) {

        move.executeMove();// executeMove to evalute the new board state.

        double distanceScore = evaluateDistanceScore();// evaluates score from the distances

        // double marblesGroupScore = evaluateGroupScore();

        move.undoMove();// undo the move to get it back before checking another move.

        return distanceScore;
    }

    private double evaluateGroupScore() {
        double GroupScore = 0.0;
        for (Move move : moves) {
            GroupScore += (move.getSizeInLine() == 2) ? 1 : ((move.getSizeInLine() == 3) ? 2 : 0);
        }
        generateValidMoves(opponentsMarbles);

        return GroupScore;
    }

    private double evaluateDistanceScore() {
        updateMarblesList(); // update the player marbles list.

        double MydistanceScore = 0;
        for (Cell cell : myMarbles) {
            MydistanceScore += cell.getScore();// sum player's marbles score, according to the distance from the center.
        }

        double opponentsDistanceScore = 0;
        for (Cell cell : opponentsMarbles) {
            opponentsDistanceScore += cell.getScore();
        }

        return MydistanceScore - opponentsDistanceScore;
    }

}
