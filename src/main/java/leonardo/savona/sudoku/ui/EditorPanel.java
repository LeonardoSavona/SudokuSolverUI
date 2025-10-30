package leonardo.savona.sudoku.ui;

import leonardo.savona.sudoku.model.SudokuBoard;
import leonardo.savona.sudoku.model.SudokuMetadata;
import leonardo.savona.sudoku.model.SudokuUtils;
import leonardo.savona.sudoku.repository.FileSudokuRepository;
import leonardo.savona.sudoku.util.SudokuHash;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EditorPanel extends JPanel {

    private final MainFrame parent;
    private SudokuBoard board;
    private final SudokuGridPanel gridPanel;
    private final JButton saveBtn;
    private final JButton clearBtn;
    private final JLabel statusLabel;

    private final FileSudokuRepository repo = new FileSudokuRepository();
    private final DefaultListModel<SudokuTemplateEntry> listModel = new DefaultListModel<>();
    private final JList<SudokuTemplateEntry> previewList = new JList<>(listModel);

    private File currentFile = null;

    public EditorPanel(MainFrame parent) {
        this.parent = parent;
        this.board = new SudokuBoard();

        this.gridPanel = new SudokuGridPanel(board);
        this.gridPanel.setMode(SudokuGridPanel.Mode.EDITOR);
        this.gridPanel.setOnChange(this::updateValidation);

        setLayout(new BorderLayout());

        // lista a sinistra
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
                    loadTemplate(sel);
                }
            }
        });

        // centro
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.add(gridPanel);
        add(centerWrapper, BorderLayout.CENTER);

        // basso
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        saveBtn = new JButton("Salva");
        saveBtn.addActionListener(e -> saveTemplate());
        clearBtn = new JButton("Pulisci sudoku");
        clearBtn.addActionListener(e -> clearBoard());
        statusLabel = new JLabel("Pronto");

        bottom.add(new JLabel("Editor: 1–9, 0/Canc per svuotare"));
        bottom.add(Box.createHorizontalStrut(15));
        bottom.add(saveBtn);
        bottom.add(Box.createHorizontalStrut(10));
        bottom.add(clearBtn);
        bottom.add(Box.createHorizontalStrut(20));
        bottom.add(statusLabel);

        add(bottom, BorderLayout.SOUTH);

        reloadTemplates();
        updateValidation();
    }

    private void loadTemplate(SudokuTemplateEntry entry) {
        this.board = entry.board;
        this.gridPanel.setBoard(this.board);
        this.currentFile = entry.file;
        updateValidation();
    }

    private void clearBoard() {
        this.board = new SudokuBoard();
        this.gridPanel.setBoard(this.board);
        this.currentFile = null;
        updateValidation();
    }

    private void saveTemplate() {
        if (!saveBtn.isEnabled()) return;

        String hash = SudokuHash.hash(board);
        File newFile = new File(repo.getDirectory(), hash + ".txt");

        // se sto modificando un template vecchio e il nome cambia → cancello anche i metadati vecchi
        if (currentFile != null && !currentFile.equals(newFile)) {
            repo.deleteWithMetadata(currentFile);
        }

        try {
            repo.save(board, newFile);
            // se è un sudoku nuovo, i metadati partono "vuoti" (non risolto)
            SudokuMetadata meta = new SudokuMetadata();
            repo.saveMetadata(newFile, meta);

            this.currentFile = newFile;
            JOptionPane.showMessageDialog(this, "Salvato: " + newFile.getName());
            reloadTemplates();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Errore salvataggio: " + ex.getMessage(),
                    "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateValidation() {
        boolean conflicts = SudokuUtils.hasConflicts(board);
        boolean any = SudokuUtils.hasAnyNumber(board);
        saveBtn.setEnabled(!conflicts && any);
        statusLabel.setText(conflicts ? "Sudoku NON valido" : (any ? "Sudoku valido" : "Vuoto"));
        statusLabel.setForeground(conflicts ? new Color(180, 0, 0) : new Color(0, 120, 0));
        repaint();
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
    }
}
