package leonardo.savona.sudoku.solver.model;

import leonardo.savona.sudoku.util.SudokuModelConverter;

import leonardo.savona.sudoku.solver.model.square.Square;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Sudoku {

    public static final int SIZE = 9;

    private final List<Cell> sudoku = new ArrayList<>();
    private final Set<Square> squares;
    private final Map<Coordinate, Set<Coordinate>> coordinatesSquares;
    private final int size;

    public Sudoku() {
        this(SIZE);
    }

    public Sudoku(int size) {
        this.size = size;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                sudoku.add(new Cell(new Coordinate(r, c), 0, this));
            }
        }
        this.coordinatesSquares = SudokuModelUtils.getCoordinatesSquare(size);
        this.squares = SudokuModelUtils.getSquares(this, coordinatesSquares);
    }

    public Sudoku(int[][] values) {
        this(values.length);
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                int value = values[r][c];
                if (value != 0) {
                    setValue(r, c, value, false);
                }
            }
        }
    }

    public Sudoku(File sudokuFile) throws Exception {
        this(loadSudokuMatrix(sudokuFile));
    }

    private static int[][] loadSudokuMatrix(File sudokuFile) throws Exception {
        try (FileReader fileReader = new FileReader(sudokuFile);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {

            List<int[]> rows = new ArrayList<>();
            String line = bufferedReader.readLine();
            while (line != null) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    int[] row = Arrays.stream(trimmed.split("\\s+"))
                            .mapToInt(Integer::parseInt)
                            .toArray();
                    rows.add(row);
                }
                line = bufferedReader.readLine();
            }
            if (rows.isEmpty()) {
                throw new Exception("Sudoku vuoto");
            }
            int size = rows.size();
            int[][] matrix = new int[size][size];
            for (int r = 0; r < size; r++) {
                int[] row = rows.get(r);
                if (row.length != size) {
                    throw new Exception("Riga " + r + " non ha " + size + " valori");
                }
                System.arraycopy(row, 0, matrix[r], 0, size);
            }
            return matrix;
        } catch (IOException e) {
            throw new Exception(e);
        }
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

    public Cell getCellByCoordinate(Coordinate coordinate) {
        return sudoku.stream().filter(c -> c.getCoordinate().equals(coordinate)).findFirst().orElseThrow(() ->
                new IllegalArgumentException("Coordinate non presenti: " + coordinate));
    }

    public Cell getCell(int row, int column) {
        checkBounds(row, column);
        return getCellByCoordinate(new Coordinate(row, column));
    }

    public int getValue(int row, int col) {
        return getCell(row, col).getValue();
    }

    public boolean isFixed(int row, int col) {
        return getCell(row, col).isFixed();
    }

    public void setValue(int row, int col, int value, boolean fixed) {
        checkBounds(row, col);
        if (value < 0 || value > size) {
            throw new IllegalArgumentException("Valore non valido: " + value);
        }
        Cell cell = getCell(row, col);
        if (value == 0) {
            cell.setValue(0);
            cell.setFixed(false);
        } else {
            cell.setValue(value);
            cell.setFixed(fixed);
        }
    }

    public void setValue(int row, int col, int value) {
        setValue(row, col, value, false);
    }

    public void clearValue(int row, int col) {
        checkBounds(row, col);
        Cell cell = getCell(row, col);
        if (cell.isFixed()) {
            return;
        }
        cell.setValue(0);
    }

    public boolean isCellEmpty(int row, int col) {
        return getValue(row, col) == 0;
    }

    public boolean isValueAllowed(int row, int col, int value) {
        if (value == 0) return true;
        for (int c = 0; c < size; c++) if (c != col && getValue(row, c) == value) return false;
        for (int r = 0; r < size; r++) if (r != row && getValue(r, col) == value) return false;
        int sq = (int) Math.sqrt(size);
        int br = (row / sq) * sq, bc = (col / sq) * sq;
        for (int r = br; r < br + sq; r++) {
            for (int c = bc; c < bc + sq; c++) {
                if ((r != row || c != col) && getValue(r, c) == value) return false;
            }
        }
        return true;
    }

    public boolean isCellInConflict(int row, int col) {
        int v = getValue(row, col);
        if (v == 0) return false;
        return !isValueAllowed(row, col, v);
    }

    public NoteSet getNotes(int row, int col) {
        return getCell(row, col).getNotes();
    }

    public void toggleNote(int row, int col, int number) {
        Cell cell = getCell(row, col);
        if (cell.isFixed()) return;
        cell.getNotes().toggleNote(number);
    }

    public void clearNotes(int row, int col) {
        getCell(row, col).clearNotes();
    }

    public Sudoku copy() {
        Sudoku copy = new Sudoku(size);
        for (Cell cell : sudoku) {
            Coordinate coord = cell.getCoordinate();
            Cell target = copy.getCellByCoordinate(coord);
            target.setValue(cell.getValue());
            target.setFixed(cell.isFixed());
            boolean[] notes = cell.getNotes().getAll();
            if (notes.length > 0) {
                target.getNotes().setAll(notes);
            }
        }
        return copy;
    }

    private void checkBounds(int row, int col) {
        if (row < 0 || row >= size || col < 0 || col >= size) {
            throw new IndexOutOfBoundsException("Cell out of bounds: " + row + "," + col);
        }
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
}
