package leonardo.savona.sudoku.solver.strategy.cellbased;

import leonardo.savona.sudoku.solver.model.Cell;
import leonardo.savona.sudoku.solver.model.Sudoku;
import leonardo.savona.sudoku.solver.strategy.Strategy;

public abstract class CellBasedStrategy extends Strategy {

    protected Cell cell;

    public CellBasedStrategy(Sudoku sudoku) {
        super(sudoku);
    }

    public void apply(Cell cell) {
        this.cell = cell;
        apply();
    }
}
