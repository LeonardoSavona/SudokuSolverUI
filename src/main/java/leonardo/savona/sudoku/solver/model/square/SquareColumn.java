package leonardo.savona.sudoku.solver.model.square;

public class SquareColumn extends SquareEntity {

    public SquareColumn(int index) {
        super(index);
    }

    @Override
    public String toString() {
        return "Column" + cells;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SquareColumn column = (SquareColumn) o;
        return index == column.index && cells.equals(column.cells);
    }
}
