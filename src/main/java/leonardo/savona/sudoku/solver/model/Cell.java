package leonardo.savona.sudoku.solver.model;

import leonardo.savona.sudoku.solver.model.SudokuModelUtils;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Cell {

    private final Coordinate coordinate;
    private final Sudoku sudoku;
    private int value;
    private Set<Integer> possibleValues = new HashSet<>();
    private boolean fixed;
    private final NoteSet notes = new NoteSet();

    public Cell(Coordinate coordinate, int value, Sudoku sudoku) {
        this.coordinate = coordinate;
        this.value = value;
        this.sudoku = sudoku;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public int getValue() {
        return value;
    }

    public Set<Integer> getPossibleValues() {
        return possibleValues;
    }

    public void addPossibleValue(Integer possibleValue) {
        possibleValues.add(possibleValue);
    }

    public void clearPossibleValues() {
        possibleValues.clear();
    }

    public void removePossibleValue(Integer value) {
        possibleValues.remove(value);
    }

    public void setPossibleValues(Set<Integer> possibleValues) {
        this.possibleValues = possibleValues;
    }

    public void setValue(Integer value) {
        this.value = value;
        if (value != 0) {
            notes.clear();
        }
    }

    public boolean isNumberFound() {
        if (getPossibleValues().size() == 1) {
            setValue((Integer) getPossibleValues().toArray()[0]);
            clearPossibleValues();
            SudokuModelUtils.clearOtherCellsPossibleValues(this, sudoku);
            return true;
        }
        return false;
    }

    public boolean isEmpty() {
        return getValue() == 0 && getPossibleValues().isEmpty();
    }

    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public NoteSet getNotes() {
        return notes;
    }

    public void clearNotes() {
        notes.clear();
    }

    @Override
    public String toString() {
        return "Cell{" + coordinate +
                ", value=" + value +
                ", possibleValues=" + possibleValues +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cell cell = (Cell) o;
        return coordinate.equals(cell.coordinate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coordinate);
    }
}
