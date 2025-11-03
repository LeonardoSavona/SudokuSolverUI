package leonardo.savona.sudoku.util;

import leonardo.savona.sudoku.model.Cell;
import leonardo.savona.sudoku.model.Coordinate;
import leonardo.savona.sudoku.model.Sudoku;
import leonardo.savona.sudoku.model.square.Square;

import java.util.*;
import java.util.stream.Collectors;

public final class SudokuUtils {
    private SudokuUtils() {
    }

    public static boolean isComplete(Sudoku board) {
        for (int r = 0; r < board.getSize(); r++) {
            for (int c = 0; c < board.getSize(); c++) {
                if (board.getValue(r, c) == 0) {
                    return false;
                }
                if (board.isCellInConflict(r, c)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean hasConflicts(Sudoku board) {
        for (int r = 0; r < board.getSize(); r++) {
            for (int c = 0; c < board.getSize(); c++) {
                int value = board.getValue(r, c);
                if (value != 0 && !board.isValueAllowed(r, c, value)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasAnyNumber(Sudoku board) {
        for (int r = 0; r < board.getSize(); r++) {
            for (int c = 0; c < board.getSize(); c++) {
                if (board.getValue(r, c) != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Map<Coordinate, Set<Coordinate>> getCoordinatesSquare(int size) {
        Map<Coordinate, Set<Coordinate>> coordinatesSquares = new HashMap<>();

        int squareSize = (int) Math.sqrt(size);

        for (int r = 0; r < size; r += squareSize) {
            for (int c = 0; c < size; c += squareSize) {
                Coordinate coordinate = new Coordinate(r, c);
                Set<Coordinate> otherCoordinates = getOtherCoordinates(coordinate, squareSize);

                coordinatesSquares.put(coordinate, otherCoordinates);
            }
        }

        Map<Coordinate, Set<Coordinate>> toAdd = new HashMap<>();
        for (Map.Entry<Coordinate, Set<Coordinate>> entry : coordinatesSquares.entrySet()) {
            for (Coordinate coordinate : entry.getValue()) {
                Set<Coordinate> coordinatesToAdd = new HashSet<>(entry.getValue());
                coordinatesToAdd.add(entry.getKey());
                coordinatesToAdd.remove(coordinate);
                toAdd.put(coordinate, coordinatesToAdd);
            }
        }

        coordinatesSquares.putAll(toAdd);
        return coordinatesSquares;
    }

    private static Set<Coordinate> getOtherCoordinates(Coordinate coordinate, int squareSize) {
        int row = coordinate.getRow();
        int col = coordinate.getColumn();

        Set<Coordinate> result = new HashSet<>();
        for (int r = row; r < row + squareSize; r++) {
            for (int c = col; c < col + squareSize; c++) {
                result.add(new Coordinate(r, c));
            }
        }

        return result;
    }

    public static Set<Square> getSquares(Sudoku sudoku, Map<Coordinate, Set<Coordinate>> coordinatesSquare) {
        Set<Square> squares = new HashSet<>();
        for (Coordinate coordinate : coordinatesSquare.keySet()) {
            Set<Cell> cells = coordinatesSquare.get(coordinate).stream()
                    .map(sudoku::getCellByCoordinate)
                    .collect(Collectors.toSet());
            cells.add(sudoku.getCellByCoordinate(coordinate));
            Square square = new Square(cells);
            squares.add(square);
        }

        return squares;
    }

    public static void clearOtherCellsPossibleValues(Cell cell, Sudoku sudoku) {
        clearOtherRowCellsPossibleValues(cell, sudoku);
        clearOtherColumnCellsPossibleValues(cell, sudoku);
        clearOtherSquareCellsPossibleValues(cell, sudoku);
    }

    private static void clearOtherRowCellsPossibleValues(Cell cell, Sudoku sudoku) {
        sudoku.getSudoku().forEach(c -> {
            if (c.getCoordinate().getRow() == cell.getCoordinate().getRow() && !c.getPossibleValues().isEmpty()) {
                c.removePossibleValue(cell.getValue());
                if (c.isNumberFound()) {
                    clearOtherCellsPossibleValues(c, sudoku);
                }
            }
        });
    }

    private static void clearOtherColumnCellsPossibleValues(Cell cell, Sudoku sudoku) {
        sudoku.getSudoku().forEach(c -> {
            if (c.getCoordinate().getColumn() == cell.getCoordinate().getColumn() && !c.getPossibleValues().isEmpty()) {
                c.removePossibleValue(cell.getValue());
                if (c.isNumberFound()) {
                    clearOtherCellsPossibleValues(c, sudoku);
                }
            }
        });
    }

    private static void clearOtherSquareCellsPossibleValues(Cell cell, Sudoku sudoku) {
        Set<Coordinate> coordinates = sudoku.getCoordinatesSquares().get(cell.getCoordinate());
        for (Coordinate coordinate : coordinates) {
            Cell related = sudoku.getCellByCoordinate(coordinate);
            if (!related.getPossibleValues().isEmpty()) {
                related.removePossibleValue(cell.getValue());
                if (related.isNumberFound()) {
                    clearOtherCellsPossibleValues(related, sudoku);
                }
            }
        }
    }

    public static Square getSquareFromCell(Sudoku sudoku, Cell cell) {
        return sudoku.getSquares().stream().filter(s -> s.getCells().contains(cell)).findFirst().get();
    }
}
