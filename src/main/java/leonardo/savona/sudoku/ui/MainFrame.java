package leonardo.savona.sudoku.ui;

import leonardo.savona.sudoku.model.SudokuBoard;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel centerPanel = new JPanel(cardLayout);
    private final EditorPanel editorPanel;
    private final SolverPanel solverPanel;

    public MainFrame() {
        super("Sudoku Solver");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 720);
        setLocationRelativeTo(null);

        editorPanel = new EditorPanel(this);
        solverPanel = new SolverPanel(this);

        centerPanel.add(editorPanel, "EDITOR");
        centerPanel.add(solverPanel, "SOLVER");

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton editorBtn = new JButton("Crea sudoku");
        JButton solverBtn = new JButton("Risolvi sudoku");
        editorBtn.addActionListener(e -> showEditor());
        solverBtn.addActionListener(e -> showSolver());
        topBar.add(editorBtn);
        topBar.add(solverBtn);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(topBar, BorderLayout.NORTH);
        getContentPane().add(centerPanel, BorderLayout.CENTER);

        showEditor();
    }

    public void showEditor() {
        editorPanel.reloadTemplates();
        cardLayout.show(centerPanel, "EDITOR");
    }

    public void showSolver() {
        solverPanel.reloadTemplates();
        cardLayout.show(centerPanel, "SOLVER");
    }

    public void loadBoardIntoSolver(SudokuBoard board) {
        solverPanel.setBoard(board);
        showSolver();
    }
}
