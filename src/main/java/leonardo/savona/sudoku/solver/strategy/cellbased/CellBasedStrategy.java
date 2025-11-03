package leonardo.savona.sudoku.solver.strategy.cellbased;

import leonardo.savona.sudoku.solver.model.Cell;
import leonardo.savona.sudoku.solver.model.Sudoku;
import leonardo.savona.sudoku.solver.strategy.Strategy;

import java.util.function.BiConsumer;

public abstract class CellBasedStrategy extends Strategy {

    protected Cell cell;

    public CellBasedStrategy(Sudoku sudoku, BiConsumer<Cell, String> onValuePlaced) {
        super(sudoku, onValuePlaced);
    }

    public void apply(Cell cell) {
        this.cell = cell;
        apply();
    }
}
