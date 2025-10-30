package leonardo.savona.sudoku.ui;

import leonardo.savona.sudoku.model.SudokuBoard;
import leonardo.savona.sudoku.model.SudokuMetadata;
import leonardo.savona.sudoku.model.SudokuUtils;
import leonardo.savona.sudoku.repository.FileSudokuRepository;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SolverPanel extends JPanel {

    private enum SolveState {
        IDLE,    // sudoku caricato ma non partito
        RUNNING, // timer attivo, non posso cambiare sudoku
        PAUSED   // timer fermo, posso cambiare sudoku
    }

    private final MainFrame parent;
    private SudokuBoard board = new SudokuBoard();
    private final SudokuGridPanel gridPanel = new SudokuGridPanel(board);

    private final FileSudokuRepository repo = new FileSudokuRepository();
    private final DefaultListModel<SudokuTemplateEntry> listModel = new DefaultListModel<>();
    private final JList<SudokuTemplateEntry> previewList = new JList<>(listModel);

    private final JLabel timerLabel = new JLabel("00:00");
    private final JButton startBtn = new JButton("Start");
    private final JButton pauseBtn = new JButton("Pausa");

    private Timer timer;
    private long startTimeMillis;
    private long pausedElapsedMillis; // tempo accumulato quando faccio pausa
    private SolveState state = SolveState.IDLE;

    private File currentFile = null; // per salvare i metadati giusti

    public SolverPanel(MainFrame parent) {
        this.parent = parent;
        setLayout(new BorderLayout());

        gridPanel.setMode(SudokuGridPanel.Mode.SOLVER);
        gridPanel.setOnChange(this::handleBoardChange);

        // SINISTRA: anteprime
        previewList.setCellRenderer(new SudokuPreviewRenderer());
        previewList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(previewList);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(180, 0));
        add(scroll, BorderLayout.WEST);

        // quando clicco un sudoku dalla lista
        previewList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                if (state == SolveState.RUNNING) {
                    // non permetto cambio durante la partita
                    // ripristino selezione corrente
                    resetSelectionToCurrent();
                } else {
                    openSelectedFromList();
                }
            }
        });

        // CENTRO: griglia
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.add(gridPanel);
        add(centerWrapper, BorderLayout.CENTER);

        // TOP: timer + pulsanti
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Tempo:"));
        top.add(timerLabel);
        top.add(Box.createHorizontalStrut(12));

        startBtn.addActionListener(e -> startSolving());
        top.add(startBtn);

        pauseBtn.addActionListener(e -> togglePause());
        pauseBtn.setEnabled(false);
        top.add(pauseBtn);

        JToggleButton noteBtn = new JToggleButton("Note (N)");
        noteBtn.addActionListener(e -> gridPanel.setNoteMode(noteBtn.isSelected()));
        top.add(noteBtn);

        add(top, BorderLayout.NORTH);

        timer = new Timer(1000, e -> updateTimer());
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
        // reset timer
        stopTimer();
        timerLabel.setText("00:00");
        pausedElapsedMillis = 0;
        state = SolveState.IDLE;
        startBtn.setEnabled(true);
        pauseBtn.setEnabled(false);
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
        // parte SOLO se ho un sudoku caricato
        if (currentFile == null) {
            JOptionPane.showMessageDialog(this, "Seleziona un sudoku prima di iniziare");
            return;
        }
        startTimeMillis = System.currentTimeMillis();
        state = SolveState.RUNNING;
        timer.start();
        startBtn.setEnabled(false);
        pauseBtn.setEnabled(true);
    }

    private void togglePause() {
        if (state == SolveState.RUNNING) {
            // metto in pausa
            timer.stop();
            long now = System.currentTimeMillis();
            pausedElapsedMillis += (now - startTimeMillis);
            state = SolveState.PAUSED;
            pauseBtn.setText("Riprendi");
        } else if (state == SolveState.PAUSED) {
            // riprendo
            startTimeMillis = System.currentTimeMillis();
            timer.start();
            state = SolveState.RUNNING;
            pauseBtn.setText("Pausa");
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
        // controlliamo se è stato risolto
        if (state != SolveState.IDLE && SudokuUtils.isComplete(board)) {
            onSolved();
        }
    }

    private void onSolved() {
        stopTimer();
        long finalTime = getElapsedMillis();
        // salviamo i metadati
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

        // reset stato
        state = SolveState.IDLE;
        startBtn.setEnabled(true);
        pauseBtn.setEnabled(false);
        pauseBtn.setText("Pausa");
        pausedElapsedMillis = 0;

        // ricarico la lista per aggiornare il badge ✔
        reloadTemplates();
        // riseleziono quello corrente
        resetSelectionToCurrent();
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

        // se avevo un file selezionato lo riseleziono
        if (currentFile != null) {
            resetSelectionToCurrent();
        } else if (!entries.isEmpty()) {
            previewList.setSelectedIndex(0);
            // e apro il primo (stato IDLE)
            openSelectedFromList();
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
    }
}
