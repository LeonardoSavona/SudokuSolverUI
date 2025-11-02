package leonardo.savona.sudoku.solver.model;

import leonardo.savona.sudoku.util.SudokuModelConverter;
import leonardo.savona.sudoku.solver.model.square.Square;
import leonardo.savona.sudoku.solver.Helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Sudoku {

    private final List<Cell> sudoku = new ArrayList<>();
    private final Set<Square> squares;
    private final Map<Coordinate, Set<Coordinate>> coordinatesSquares;
    private int size;

    // ================== NUOVO COSTRUTTORE ==================
    public Sudoku(int[][] values) {
        this.size = values.length;
        // costruiamo le celle
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                sudoku.add(new Cell(
                        new Coordinate(r, c),
                        values[r][c],
                        this
                ));
            }
        }
        // come nel costruttore da file
        this.coordinatesSquares = Helper.getCoordinatesSquare(size);
        this.squares = Helper.getSquares(this, coordinatesSquares);
    }
    // =======================================================

    public Sudoku(File sudokuFile) throws Exception {
        loadSudoku(sudokuFile);
        coordinatesSquares = Helper.getCoordinatesSquare(size);
        squares = Helper.getSquares(this, coordinatesSquares);
    }

    private void loadSudoku(File sudokuFile) throws Exception {
        try (FileReader fileReader = new FileReader(sudokuFile);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {

            List<List<Integer>> tempSudoku = new ArrayList<>();
            String line = bufferedReader.readLine();
            while (line != null) {
                List<Integer> row = Arrays.stream(line.split(" "))
                        .map(Integer::new)
                        .collect(Collectors.toList());

                tempSudoku.add(row);
                line = bufferedReader.readLine();
            }
            this.size = tempSudoku.size();
            for (int r = 0; r < getSize(); r++) {
                for (int c = 0; c < getSize(); c++) {
                    sudoku.add(
                            new Cell(
                                    new Coordinate(r, c),
                                    tempSudoku.get(r).get(c),
                                    this
                            )
                    );
                }
            }
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
        return sudoku.stream().filter(c -> c.getCoordinate().equals(coordinate)).findFirst().get();
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
