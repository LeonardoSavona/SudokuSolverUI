package leonardo.savona.sudoku.solver;

import java.util.ArrayList;
import java.util.List;

public class Chronology {

    private final List<SolverStep> steps = new ArrayList<>();

    public void addStep(SolverStep step) {
        if (step == null) {
            return;
        }
        if (steps.isEmpty() || !steps.get(steps.size() - 1).hasSameMatrix(step)) {
            steps.add(step);
        }
    }

    public List<SolverStep> getSteps() {
        return new ArrayList<>(steps);
    }
}
