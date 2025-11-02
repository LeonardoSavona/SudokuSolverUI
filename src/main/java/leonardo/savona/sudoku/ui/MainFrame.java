package leonardo.savona.sudoku.ui;

import leonardo.savona.sudoku.ui.panel.EditorPanel;
import leonardo.savona.sudoku.ui.panel.SolutionPanel;
import leonardo.savona.sudoku.ui.panel.SolverPanel;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);

    private final EditorPanel editorPanel;
    private final SolverPanel solverPanel;
    private final SolutionPanel solutionPanel;

    public MainFrame() {
        super("Sudoku");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // top bar
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton editBtn = new JButton("Crea sudoku");
        JButton solveBtn = new JButton("Risolvi sudoku");
        JButton solPanelBtn = new JButton("Soluzioni");

        top.add(editBtn);
        top.add(solveBtn);
        top.add(solPanelBtn);

        add(top, BorderLayout.NORTH);

        // pannelli
        editorPanel = new EditorPanel();
        solverPanel = new SolverPanel(this);
        solutionPanel = new SolutionPanel();

        cardPanel.add(editorPanel, "editor");
        cardPanel.add(solverPanel, "solver");
        cardPanel.add(solutionPanel, "solutions");

        add(cardPanel, BorderLayout.CENTER);

        // azioni
        editBtn.addActionListener(e -> {
            editorPanel.reloadTemplates();
            cardLayout.show(cardPanel, "editor");
        });
        solveBtn.addActionListener(e -> {
            solverPanel.reloadTemplates();
            cardLayout.show(cardPanel, "solver");
        });
        solPanelBtn.addActionListener(e -> {
            solutionPanel.reloadTemplates();
            cardLayout.show(cardPanel, "solutions");
        });

        setSize(1200, 800);
        setLocationRelativeTo(null);
    }
}
