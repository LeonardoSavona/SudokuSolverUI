package leonardo.savona.sudoku.solver.strategy;

import leonardo.savona.sudoku.model.Sudoku;

public abstract class Strategy {

    protected final Sudoku sudoku;

    public Strategy(Sudoku sudoku) {
        this.sudoku = sudoku;
    }

    public abstract void apply();
}
