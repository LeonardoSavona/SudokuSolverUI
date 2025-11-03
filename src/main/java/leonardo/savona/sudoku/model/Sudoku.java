package leonardo.savona.sudoku.model;

import leonardo.savona.sudoku.model.square.Square;
import leonardo.savona.sudoku.util.SudokuModelConverter;
import leonardo.savona.sudoku.util.SudokuUtils;

import java.util.*;

public class Sudoku {

    public static final int DEFAULT_SIZE = 9;

    private final List<Cell> sudoku = new ArrayList<>();
    private final Map<Coordinate, Cell> cellsByCoordinate = new HashMap<>();
    private final Set<Square> squares;
    private final Map<Coordinate, Set<Coordinate>> coordinatesSquares;
    private final int size;

    public Sudoku() {
        this(DEFAULT_SIZE);
    }

    public Sudoku(int size) {
        this.size = size;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                addCell(new Coordinate(r, c), 0);
            }
        }
        this.coordinatesSquares = SudokuUtils.getCoordinatesSquare(size);
        this.squares = SudokuUtils.getSquares(this, coordinatesSquares);
    }

    public Sudoku(int[][] values) {
        this(values.length);
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                int value = values[r][c];
                if (value != 0) {
                    getCellByCoordinate(new Coordinate(r, c)).setValue(value);
                }
            }
        }
    }

    private void addCell(Coordinate coordinate, int value) {
        Cell cell = new Cell(coordinate, value, this);
        sudoku.add(cell);
        cellsByCoordinate.put(coordinate, cell);
    }

    public int getSize() {
        return this.size;
    }

    public List<Cell> getSudoku() {
        return sudoku;
    }

    public Set<Square> getSquares() {
        return squares;
    }

    public Map<Coordinate, Set<Coordinate>> getCoordinatesSquares() {
        return coordinatesSquares;
    }

    public Cell getCell(int row, int column) {
        checkBounds(row, column);
        return getCellByCoordinate(new Coordinate(row, column));
    }

    public Cell getCellByCoordinate(Coordinate coordinate) {
        Cell cell = cellsByCoordinate.get(coordinate);
        if (cell == null) {
            throw new IllegalArgumentException("Coordinate out of bounds: " + coordinate);
        }
        return cell;
    }

    public int getValue(int row, int column) {
        return getCell(row, column).getValue();
    }

    public boolean isFixed(int row, int column) {
        return getCell(row, column).isFixed();
    }

    public NoteSet getNotes(int row, int column) {
        return getCell(row, column).getNotes();
    }

    public void toggleNote(int row, int column, int number) {
        Cell cell = getCell(row, column);
        if (cell.isFixed()) {
            return;
        }
        cell.toggleNote(number);
    }

    public void clearNotes(int row, int column) {
        getCell(row, column).clearNotes();
    }

    public void setValue(int row, int column, int value) {
        setValue(row, column, value, false);
    }

    public void setValue(int row, int column, int value, boolean fixedValue) {
        checkBounds(row, column);
        if (value < 0 || value > size) {
            throw new IllegalArgumentException("Valore non valido: " + value);
        }
        Cell cell = getCell(row, column);
        if (value == 0) {
            cell.setValue(0);
            cell.clearNotes();
            cell.setFixed(false);
            return;
        }

        cell.setValue(value);
        cell.setFixed(fixedValue);
        cell.clearNotes();
        SudokuUtils.clearOtherCellsPossibleValues(cell, this);
    }

    public void clearValue(int row, int column) {
        Cell cell = getCell(row, column);
        if (cell.isFixed()) {
            return;
        }
        cell.setValue(0);
        cell.clearNotes();
    }

    public boolean isCellEmpty(int row, int column) {
        return getValue(row, column) == 0;
    }

    public boolean isValueAllowed(int row, int column, int value) {
        if (value == 0) {
            return true;
        }
        for (int c = 0; c < size; c++) {
            if (c != column && getValue(row, c) == value) {
                return false;
            }
        }
        for (int r = 0; r < size; r++) {
            if (r != row && getValue(r, column) == value) {
                return false;
            }
        }
        int sq = (int) Math.sqrt(size);
        int baseRow = (row / sq) * sq;
        int baseCol = (column / sq) * sq;
        for (int r = baseRow; r < baseRow + sq; r++) {
            for (int c = baseCol; c < baseCol + sq; c++) {
                if ((r != row || c != column) && getValue(r, c) == value) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isCellInConflict(int row, int column) {
        int value = getValue(row, column);
        return value != 0 && !isValueAllowed(row, column, value);
    }

    public Sudoku copy() {
        Sudoku copy = new Sudoku(size);
        for (Cell cell : sudoku) {
            Coordinate coordinate = cell.getCoordinate();
            Cell copyCell = copy.getCellByCoordinate(coordinate);
            copyCell.setValue(cell.getValue());
            copyCell.setFixed(cell.isFixed());
            copyCell.setPossibleValues(new HashSet<>(cell.getPossibleValues()));
            if (!cell.getNotes().isEmpty()) {
                boolean[] notes = cell.getNotes().getAll();
                for (int i = 0; i < notes.length; i++) {
                    if (notes[i]) {
                        copyCell.getNotes().setNote(i + 1, true);
                    }
                }
            }
        }
        return copy;
    }

    @Override
    public String toString() {
        return SudokuModelConverter.getSudokuAsStandardString(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sudoku sudoku1 = (Sudoku) o;
        for (Cell cell : sudoku) {
            Cell sudoku1Cell = sudoku1.getCellByCoordinate(cell.getCoordinate());
            if (cell.getValue() != sudoku1Cell.getValue()) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sudoku);
    }

    private void checkBounds(int row, int column) {
        if (row < 0 || row >= size || column < 0 || column >= size) {
            throw new IndexOutOfBoundsException("Cell out of bounds: " + row + "," + column);
        }
    }
}
