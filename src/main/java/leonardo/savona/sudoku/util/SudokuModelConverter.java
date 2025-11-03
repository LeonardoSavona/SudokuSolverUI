package leonardo.savona.sudoku.util;

import leonardo.savona.sudoku.model.SudokuBoard;
import leonardo.savona.sudoku.solver.model.Cell;
import leonardo.savona.sudoku.solver.model.Coordinate;
import leonardo.savona.sudoku.solver.model.Sudoku;

/**
 * Converte tra il modello dell'app (SudokuBoard)
 * e il modello del solver esterno (Sudoku) / matrici int[][]
 */
public class SudokuModelConverter {

    /**
     * Converte il nostro SudokuBoard in una matrice 9x9 di int.
     */
    public static int[][] toMatrix(SudokuBoard board) {
        int[][] m = new int[9][9];
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                m[r][c] = board.getValue(r, c);
            }
        }
        return m;
    }

    /**
     * Converte una matrice 9x9 in un SudokuBoard della tua app.
     */
    public static SudokuBoard fromMatrix(int[][] m) {
        SudokuBoard b = new SudokuBoard();
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                int v = m[r][c];
                if (v != 0) {
                    // in "soluzioni" lo consideriamo non fisso
                    b.setValue(r, c, v, false);
                }
            }
        }
        return b;
    }

    /**
     * Converte il nostro SudokuBoard nel Sudoku del modulo esterno.
     * ATTENZIONE: richiede che nel modulo esterno tu aggiunga
     * il costruttore `public Sudoku(int[][] values)`.
     */
    public static Sudoku toExternalSudoku(SudokuBoard board) {
        int[][] m = toMatrix(board);
        return new Sudoku(m);
    }

    public static int[][] toMatrix(Sudoku sudoku) {
        int size = sudoku.getSize();
        int[][] matrix = new int[size][size];
        for (Cell cell : sudoku.getSudoku()) {
            int row = cell.getCoordinate().getRow();
            int col = cell.getCoordinate().getColumn();
            matrix[row][col] = cell.getValue();
        }
        return matrix;
    }

    public static String getSudokuAsStandardString(Sudoku sudoku) {
        StringBuilder result = new StringBuilder();
        for (int r = 0; r < sudoku.getSize(); r++){
            for (int c = 0; c < sudoku.getSize(); c++) {
                result.append(
                        sudoku.getCellByCoordinate(new Coordinate(r,c)).getValue()
                ).append(" ");
            }
            result.append("\n");
        }
        return result.toString();
    }

    public static String getSudokuAsString(Sudoku sudoku) {
        StringBuilder result = new StringBuilder();
        int i = 0;

        for (int r = 0; r < sudoku.getSize(); r++) {
            result.append(getColor(-1))
                    .append("+")
                    .append(new String(new char[sudoku.getSize()]).replace("\0", "-----+"))
                    .append("\n");
            for (int c = 0; c < sudoku.getSize(); c++) {
                result.append("|")
                        .append(getColor(sudoku.getCellByCoordinate(new Coordinate(r,c)).getValue()))
                        .append(String.format("  %d  ", sudoku.getCellByCoordinate(new Coordinate(r,c)).getValue()))
                        .append(getColor(-1));
            }
            result.append("|\n");
        }

        result.append("+")
                .append(new String(new char[sudoku.getSize()]).replace("\0", "-----+"));
        return result.toString();
    }

    private static String getColor(Integer num) {
        if (num <= 0) {
            return "\u001B[0m";
        }
        switch (num % 9) {
            case 0:
                return "\u001B[30m";
            case 1:
                return "\u001B[38m";
            case 2:
                return "\u001B[32m";
            case 3:
                return "\u001B[33m";
            case 4:
                return "\u001B[34m";
            case 5:
                return "\u001B[35m";
            case 6:
                return "\u001B[36m";
            case 7:
                return "\u001B[37m";
            case 8:
                return "\u001B[31m";
            default:
                return "\u001B[0m";
        }
    }
}
