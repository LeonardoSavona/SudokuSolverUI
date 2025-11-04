package leonardo.savona.sudoku.solver.strategy;

import leonardo.savona.sudoku.model.Sudoku;
import leonardo.savona.sudoku.solver.StrategyContext;

public abstract class Strategy {

    protected final Sudoku sudoku;
    protected StrategyContext context = StrategyContext.NO_OP;

    public Strategy(Sudoku sudoku) {
        this.sudoku = sudoku;
    }

    public abstract void apply();

    public void setContext(StrategyContext context) {
        this.context = context == null ? StrategyContext.NO_OP : context;
    }
}
