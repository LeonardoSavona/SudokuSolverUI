package leonardo.savona.sudoku.solver;

import java.util.ArrayList;
import java.util.List;

public class Chronology {

    private final List<SolverStep> steps = new ArrayList<>();

    public void addSudoku(SolverStep step) {
        if (step == null) {
            return;
        }
        if (steps.isEmpty() || hasChanged(step)) {
            steps.add(step);
        }
    }

    private boolean hasChanged(SolverStep step) {
        SolverStep last = steps.get(steps.size() - 1);
        int[][] lastMatrix = last.getMatrix();
        int[][] currentMatrix = step.getMatrix();
        if (lastMatrix.length != currentMatrix.length) {
            return true;
        }
        for (int r = 0; r < lastMatrix.length; r++) {
            if (lastMatrix[r].length != currentMatrix[r].length) {
                return true;
            }
            for (int c = 0; c < lastMatrix[r].length; c++) {
                if (lastMatrix[r][c] != currentMatrix[r][c]) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<SolverStep> getSteps() {
        return new ArrayList<>(steps);
    }
}
