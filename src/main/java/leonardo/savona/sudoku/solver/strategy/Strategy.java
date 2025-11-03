package leonardo.savona.sudoku.solver.strategy;

import leonardo.savona.sudoku.solver.model.Cell;
import leonardo.savona.sudoku.solver.model.Sudoku;

import java.util.function.BiConsumer;

public abstract class Strategy {

    protected final Sudoku sudoku;
    protected final BiConsumer<Cell, String> onValuePlaced;

    public Strategy(Sudoku sudoku, BiConsumer<Cell, String> onValuePlaced) {
        this.sudoku = sudoku;
        this.onValuePlaced = onValuePlaced;
    }

    protected void notifyValuePlacement(Cell cell, String strategyName) {
        if (onValuePlaced != null && cell != null && cell.getValue() != 0) {
            onValuePlaced.accept(cell, strategyName);
        }
    }

    public abstract void apply();
}
