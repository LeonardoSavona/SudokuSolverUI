package leonardo.savona.sudoku.ui.panel;

import leonardo.savona.sudoku.model.Sudoku;
import leonardo.savona.sudoku.model.SudokuMetadata;
import leonardo.savona.sudoku.repository.FileSudokuRepository;
import leonardo.savona.sudoku.solver.SolverStep;
import leonardo.savona.sudoku.solver.SudokuSolver;
import leonardo.savona.sudoku.ui.SudokuPreviewRenderer;
import leonardo.savona.sudoku.ui.SudokuTemplateEntry;
import leonardo.savona.sudoku.util.SudokuModelConverter;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Collections;
import java.util.List;

public class SolutionPanel extends JPanel {

    private final FileSudokuRepository repo = new FileSudokuRepository();

    private final DefaultListModel<SudokuTemplateEntry> listModel = new DefaultListModel<>();
    private final JList<SudokuTemplateEntry> previewList = new JList<>(listModel);

    private final SudokuGridPanel gridPanel;

    private final JButton solveBtn;
    private final JButton prevBtn;
    private final JButton nextBtn;
    private final JLabel stepLabel;

    private SudokuTemplateEntry currentEntry;
    private List<SolverStep> steps = Collections.emptyList();
    private int currentStepIndex = -1;

    public SolutionPanel() {
        setLayout(new BorderLayout());

        // colonna sinistra (come negli altri panel)
        previewList.setCellRenderer(new SudokuPreviewRenderer());
        previewList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(previewList);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(180, 0));
        add(scroll, BorderLayout.WEST);

        previewList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                SudokuTemplateEntry sel = previewList.getSelectedValue();
                if (sel != null) {
                    openSudoku(sel);
                }
            }
        });

        // centro
        JPanel center = new JPanel(new BorderLayout());
        add(center, BorderLayout.CENTER);

        // top bar: pulsante "Mostra soluzione" e label
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        solveBtn = new JButton("Mostra soluzione");
        top.add(solveBtn);

        stepLabel = new JLabel("Nessuna soluzione");
        top.add(Box.createHorizontalStrut(10));
        top.add(stepLabel);

        center.add(top, BorderLayout.NORTH);

        // griglia
        gridPanel = new SudokuGridPanel(new Sudoku());
        gridPanel.setMode(SudokuGridPanel.Mode.SOLVER);
        gridPanel.setInputEnabled(false);
        JPanel gridWrap = new JPanel(new GridBagLayout());
        gridWrap.add(gridPanel);
        center.add(gridWrap, BorderLayout.CENTER);

        // bottom: navigazione step
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        prevBtn = new JButton("← Precedente");
        nextBtn = new JButton("Successivo →");
        prevBtn.setEnabled(false);
        nextBtn.setEnabled(false);
        bottom.add(prevBtn);
        bottom.add(nextBtn);
        center.add(bottom, BorderLayout.SOUTH);

        // azioni
        solveBtn.addActionListener(e -> onSolve());
        prevBtn.addActionListener(e -> showStep(currentStepIndex - 1));
        nextBtn.addActionListener(e -> showStep(currentStepIndex + 1));

        // carica la lista
        reloadTemplates();
    }

    public void reloadTemplates() {
        listModel.clear();
        for (File f : repo.listFiles()) {
            try {
                Sudoku b = repo.load(f);
                SudokuMetadata m = repo.loadMetadata(f);
                listModel.addElement(new SudokuTemplateEntry(f, b, m));
            } catch (Exception ignored) {}
        }
    }

    private void openSudoku(SudokuTemplateEntry entry) {
        this.currentEntry = entry;
        // mostriamo il sudoku com'è (input disabilitato)
        this.gridPanel.setBoard(entry.board);
        this.gridPanel.setInputEnabled(false);
        this.gridPanel.setInteractionEnabled(false);
        this.gridPanel.setHighlight(null);
        this.steps = Collections.emptyList();
        this.currentStepIndex = -1;
        updateStepLabel();
        prevBtn.setEnabled(false);
        nextBtn.setEnabled(false);
    }

    private void onSolve() {
        if (currentEntry == null) return;
        // chiamiamo il runner che usa il modulo esterno
        List<SolverStep> chronology = SudokuSolver.solveAndGetSteps(currentEntry.board);
        this.steps = chronology;
        if (chronology.isEmpty()) {
            currentStepIndex = -1;
            gridPanel.setBoard(currentEntry.board);
            stepLabel.setText("Nessuna soluzione trovata");
            prevBtn.setEnabled(false);
            nextBtn.setEnabled(false);
            gridPanel.setHighlight(null);
        } else {
            // mostriamo il primo step
            showStep(0);
            prevBtn.setEnabled(true);
            nextBtn.setEnabled(chronology.size() > 1);
        }
    }

    private void showStep(int index) {
        if (steps == null || steps.isEmpty()) return;
        if (index < 0 || index >= steps.size()) return;

        SolverStep step = steps.get(index);
        Sudoku b = SudokuModelConverter.fromMatrix(step.getMatrix());
        gridPanel.setBoard(b);
        gridPanel.setInputEnabled(false);
        gridPanel.setInteractionEnabled(false);

        if (step.getRow() != null && step.getColumn() != null) {
            gridPanel.setHighlight(new SudokuGridPanel.Highlight(step.getRow(), step.getColumn()));
        } else {
            gridPanel.setHighlight(null);
        }

        currentStepIndex = index;
        updateStepLabel();

        prevBtn.setEnabled(index > 0);
        nextBtn.setEnabled(index < steps.size() - 1);
    }

    private void updateStepLabel() {
        if (steps == null || steps.isEmpty() || currentStepIndex < 0) {
            stepLabel.setText("Nessuna soluzione");
        } else {
            SolverStep step = steps.get(currentStepIndex);
            if (step.getRow() != null && step.getColumn() != null && step.getValue() != null) {
                String strategy = step.getStrategy() == null ? "" : " (" + step.getStrategy() + ")";
                stepLabel.setText(String.format(
                        "Passo %d / %d – R%dC%d = %d%s",
                        currentStepIndex + 1,
                        steps.size(),
                        step.getRow() + 1,
                        step.getColumn() + 1,
                        step.getValue(),
                        strategy
                ));
            } else {
                stepLabel.setText(String.format("Passo %d / %d – Stato iniziale", currentStepIndex + 1, steps.size()));
            }
        }
    }
}
