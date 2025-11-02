package leonardo.savona.sudoku.solver.model.square;

public class SquareRow extends SquareEntity {

    public SquareRow(int index) {
        super(index);
    }

    @Override
    public String toString() {
        return "Row" + cells;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SquareRow row = (SquareRow) o;
        return index == row.index && cells.equals(row.cells);
    }
}
