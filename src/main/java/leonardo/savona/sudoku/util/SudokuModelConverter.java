package leonardo.savona.sudoku.util;

import leonardo.savona.sudoku.model.Cell;
import leonardo.savona.sudoku.model.Sudoku;

import java.util.HashSet;
import java.util.Set;

/**
 * Conversion utilities between the Sudoku model and matrix/string representations.
 */
public class SudokuModelConverter {

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

    public static Sudoku fromMatrix(int[][] matrix) {
        return fromState(matrix, null);
    }

    public static Sudoku fromState(int[][] matrix, boolean[][][] notes) {
        Sudoku sudoku = new Sudoku(matrix.length);
        for (int r = 0; r < matrix.length; r++) {
            for (int c = 0; c < matrix[r].length; c++) {
                int value = matrix[r][c];
                if (value != 0) {
                    sudoku.setValue(r, c, value, false);
                }
            }
        }

        if (notes != null) {
            for (int r = 0; r < Math.min(notes.length, matrix.length); r++) {
                for (int c = 0; c < Math.min(notes[r].length, matrix[r].length); c++) {
                    Cell cell = sudoku.getCell(r, c);
                    if (cell.getValue() != 0) {
                        continue;
                    }

                    Set<Integer> possibleValues = new HashSet<>();
                    boolean[] cellNotes = notes[r][c];
                    for (int i = 0; i < cellNotes.length; i++) {
                        if (cellNotes[i]) {
                            int value = i + 1;
                            possibleValues.add(value);
                            cell.getNotes().setNote(value, true);
                        }
                    }
                    if (!possibleValues.isEmpty()) {
                        cell.setPossibleValues(possibleValues);
                    } else {
                        cell.clearPossibleValues();
                    }
                }
            }
        }

        return sudoku;
    }

    public static boolean[][][] toNotes(Sudoku sudoku) {
        int size = sudoku.getSize();
        boolean[][][] notes = new boolean[size][size][9];
        for (Cell cell : sudoku.getSudoku()) {
            int row = cell.getCoordinate().getRow();
            int col = cell.getCoordinate().getColumn();

            if (cell.getValue() != 0) {
                continue;
            }

            boolean[] manualNotes = cell.getNotes().getAll();
            for (int i = 0; i < manualNotes.length; i++) {
                notes[row][col][i] = manualNotes[i];
            }

            if (!cell.getPossibleValues().isEmpty()) {
                for (int candidate : cell.getPossibleValues()) {
                    if (candidate >= 1 && candidate <= 9) {
                        notes[row][col][candidate - 1] = true;
                    }
                }
            }
        }
        return notes;
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
