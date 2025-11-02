package leonardo.savona.sudoku.ui.panel;

import leonardo.savona.sudoku.model.SudokuBoard;
import leonardo.savona.sudoku.model.SudokuMetadata;
import leonardo.savona.sudoku.model.SudokuUtils;
import leonardo.savona.sudoku.repository.FileSudokuRepository;
import leonardo.savona.sudoku.ui.MainFrame;
import leonardo.savona.sudoku.ui.SudokuPreviewRenderer;
import leonardo.savona.sudoku.ui.SudokuTemplateEntry;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SolverPanel extends JPanel {

    private enum SolveState { IDLE, RUNNING, PAUSED }

    private final MainFrame parent;
    private SudokuBoard board = new SudokuBoard();
    private final SudokuGridPanel gridPanel = new SudokuGridPanel(board);

    private final FileSudokuRepository repo = new FileSudokuRepository();
    private final DefaultListModel<SudokuTemplateEntry> listModel = new DefaultListModel<>();
    private final JList<SudokuTemplateEntry> previewList = new JList<>(listModel);

    private final JLabel timerLabel = new JLabel("00:00");
    private final JButton startBtn = new JButton("Start");
    private final JButton pauseBtn = new JButton("Pausa");
    private final JToggleButton noteBtn = new JToggleButton("Note");  // 👈 solo "Note"

    private final JLabel[] numberLabels = new JLabel[9];

    private Timer timer;
    private long startTimeMillis;
    private long pausedElapsedMillis;
    private SolveState state = SolveState.IDLE;

    private File currentFile = null;

    public SolverPanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout());

        gridPanel.setMode(SudokuGridPanel.Mode.SOLVER);
        gridPanel.setInputEnabled(false); // sudoku disattivato finché non premi start
        gridPanel.setOnChange(() -> {
            handleBoardChange();
            updateNumberBar();
        });

        // SINISTRA: lista anteprime
        previewList.setCellRenderer(new SudokuPreviewRenderer());
        previewList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(previewList);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(180, 0));
        add(scroll, BorderLayout.WEST);

        previewList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                if (state == SolveState.RUNNING) {
                    resetSelectionToCurrent();
                } else {
                    openSelectedFromList();
                }
            }
        });

        // CENTRO: colonna con controlli sopra + griglia + numeri sotto
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        JPanel column = new JPanel();
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));

        // controlli sopra
        JPanel topControls = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 5));
        topControls.setMaximumSize(new Dimension(540, 40));
        topControls.setPreferredSize(new Dimension(540, 40));

        timerLabel.setFont(timerLabel.getFont().deriveFont(Font.BOLD, 16f));
        topControls.add(new JLabel("Tempo:"));
        topControls.add(timerLabel);

        startBtn.addActionListener(e -> startSolving());
        topControls.add(startBtn);

        pauseBtn.addActionListener(e -> togglePause());
        pauseBtn.setEnabled(false);
        topControls.add(pauseBtn);

        // bottone NOTE semplice
        noteBtn.addActionListener(e -> {
            boolean sel = noteBtn.isSelected();
            gridPanel.setNoteMode(sel);
            // rendiamo visivo lo stato colore di sfondo
            noteBtn.setBackground(sel ? new Color(255, 240, 180) : null);
        });
        topControls.add(noteBtn);

        column.add(topControls);

        // griglia
        gridPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        column.add(gridPanel);

        // numeri sotto
        JPanel bottomNumbers = new JPanel();
        bottomNumbers.setLayout(new GridLayout(1, 9, 8, 0));
        bottomNumbers.setMaximumSize(new Dimension(540, 55));
        bottomNumbers.setPreferredSize(new Dimension(540, 55));
        bottomNumbers.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        for (int i = 0; i < 9; i++) {
            JLabel lab = new JLabel(String.valueOf(i + 1), SwingConstants.CENTER);
            lab.setFont(lab.getFont().deriveFont(Font.BOLD, 24f));
            numberLabels[i] = lab;
            bottomNumbers.add(lab);
        }
        column.add(bottomNumbers);

        centerWrapper.add(column);
        add(centerWrapper, BorderLayout.CENTER);

        // timer swing
        timer = new Timer(1000, e -> updateTimer());
    }

    // aggiorna colori dei numeri sotto
    private void updateNumberBar() {
        int[] counts = new int[10];
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                int v = board.getValue(r, c);
                if (v >= 1 && v <= 9) counts[v]++;
            }
        }
        for (int i = 1; i <= 9; i++) {
            JLabel lab = numberLabels[i - 1];
            if (counts[i] >= 9) {
                lab.setForeground(Color.LIGHT_GRAY);
            } else {
                lab.setForeground(Color.BLACK);
            }
        }
    }

    private void resetSelectionToCurrent() {
        if (currentFile == null) return;
        for (int i = 0; i < listModel.size(); i++) {
            SudokuTemplateEntry e = listModel.get(i);
            if (e.file.equals(currentFile)) {
                previewList.setSelectedIndex(i);
                break;
            }
        }
    }

    private void openSelectedFromList() {
        SudokuTemplateEntry sel = previewList.getSelectedValue();
        if (sel == null) return;
        this.board = cloneBoard(sel.board);
        this.gridPanel.setBoard(this.board);
        this.currentFile = sel.file;
        stopTimer();
        timerLabel.setText("00:00");
        pausedElapsedMillis = 0;
        state = SolveState.IDLE;
        startBtn.setEnabled(true);
        pauseBtn.setEnabled(false);
        noteBtn.setSelected(false);
        noteBtn.setBackground(null);
        gridPanel.setNoteMode(false);
        gridPanel.setInputEnabled(false); // appena aperto: disattivato
        updateNumberBar();
    }

    private SudokuBoard cloneBoard(SudokuBoard src) {
        SudokuBoard b = new SudokuBoard();
        for (int r = 0; r < 9; r++)
            for (int c = 0; c < 9; c++) {
                int v = src.getValue(r, c);
                if (v != 0) b.setValue(r, c, v, true);
            }
        return b;
    }

    private void startSolving() {
        if (currentFile == null) {
            JOptionPane.showMessageDialog(this, "Seleziona un sudoku prima di iniziare");
            return;
        }
        startTimeMillis = System.currentTimeMillis();
        state = SolveState.RUNNING;
        timer.start();
        startBtn.setEnabled(false);
        pauseBtn.setEnabled(true);
        gridPanel.setInputEnabled(true);   // ora puoi scrivere
    }

    private void togglePause() {
        if (state == SolveState.RUNNING) {
            timer.stop();
            long now = System.currentTimeMillis();
            pausedElapsedMillis += (now - startTimeMillis);
            state = SolveState.PAUSED;
            pauseBtn.setText("Riprendi");
            gridPanel.setInputEnabled(false);
        } else if (state == SolveState.PAUSED) {
            startTimeMillis = System.currentTimeMillis();
            timer.start();
            state = SolveState.RUNNING;
            pauseBtn.setText("Pausa");
            gridPanel.setInputEnabled(true);
        }
    }

    private long getElapsedMillis() {
        if (state == SolveState.RUNNING) {
            long now = System.currentTimeMillis();
            return pausedElapsedMillis + (now - startTimeMillis);
        } else {
            return pausedElapsedMillis;
        }
    }

    private void stopTimer() {
        timer.stop();
    }

    private void updateTimer() {
        long ms = getElapsedMillis();
        long sec = ms / 1000;
        long m = sec / 60;
        long s = sec % 60;
        timerLabel.setText(String.format("%02d:%02d", m, s));
    }

    private void handleBoardChange() {
        if (state != SolveState.IDLE && SudokuUtils.isComplete(board)) {
            onSolved();
        }
    }

    private void onSolved() {
        stopTimer();
        long finalTime = getElapsedMillis();
        if (currentFile != null) {
            SudokuMetadata meta = repo.loadMetadata(currentFile);
            meta.updateSolved(finalTime);
            try {
                repo.saveMetadata(currentFile, meta);
            } catch (Exception ignored) {}
        }
        JOptionPane.showMessageDialog(this,
                "Sudoku risolto in " + timerLabel.getText() + " 🎉",
                "Completato", JOptionPane.INFORMATION_MESSAGE);

        state = SolveState.IDLE;
        startBtn.setEnabled(true);
        pauseBtn.setEnabled(false);
        pauseBtn.setText("Pausa");
        pausedElapsedMillis = 0;
        gridPanel.setInputEnabled(false);
        noteBtn.setSelected(false);
        noteBtn.setBackground(null);
        gridPanel.setNoteMode(false);

        reloadTemplates();
        resetSelectionToCurrent();
        updateNumberBar();
    }

    public void reloadTemplates() {
        listModel.clear();
        List<File> files = repo.listFiles();
        List<SudokuTemplateEntry> entries = new ArrayList<>();
        for (File f : files) {
            try {
                SudokuBoard b = repo.load(f);
                SudokuMetadata m = repo.loadMetadata(f);
                entries.add(new SudokuTemplateEntry(f, b, m));
            } catch (Exception ignored) {}
        }
        for (SudokuTemplateEntry e : entries) {
            listModel.addElement(e);
        }

        if (currentFile != null) {
            resetSelectionToCurrent();
        } else if (!entries.isEmpty()) {
            previewList.setSelectedIndex(0);
            openSelectedFromList();
        } else {
            this.board = new SudokuBoard();
            this.gridPanel.setBoard(this.board);
            updateNumberBar();
        }
    }

    public void setBoard(SudokuBoard b) {
        this.board = b;
        this.gridPanel.setBoard(b);
        stopTimer();
        timerLabel.setText("00:00");
        pausedElapsedMillis = 0;
        state = SolveState.IDLE;
        startBtn.setEnabled(true);
        pauseBtn.setEnabled(false);
        gridPanel.setInputEnabled(false);
        noteBtn.setSelected(false);
        noteBtn.setBackground(null);
        gridPanel.setNoteMode(false);
        updateNumberBar();
    }
}
