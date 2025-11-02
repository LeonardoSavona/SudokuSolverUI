package leonardo.savona.sudoku.ui.panel;

import leonardo.savona.sudoku.model.SudokuBoard;
import leonardo.savona.sudoku.model.SudokuMetadata;
import leonardo.savona.sudoku.model.SudokuUtils;
import leonardo.savona.sudoku.repository.FileSudokuRepository;
import leonardo.savona.sudoku.ui.MainFrame;
import leonardo.savona.sudoku.ui.SudokuPreviewRenderer;
import leonardo.savona.sudoku.ui.SudokuTemplateEntry;
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
    private final JButton deleteBtn;
    private final JLabel statusLabel;
    private final JComboBox<String> difficultyCombo;

    private final FileSudokuRepository repo = new FileSudokuRepository();
    private final DefaultListModel<SudokuTemplateEntry> listModel = new DefaultListModel<>();
    private final JList<SudokuTemplateEntry> previewList = new JList<>(listModel);

    private File currentFile = null;

    public EditorPanel(MainFrame parent) {
        this.parent = parent;
        this.board = new SudokuBoard();

        setLayout(new BorderLayout());

        // ===== COLONNA SINISTRA: anteprime =====
        previewList.setCellRenderer(new SudokuPreviewRenderer());
        previewList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(previewList);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(180, 0)); // stessa larghezza del solver
        add(scroll, BorderLayout.WEST);

        previewList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                SudokuTemplateEntry sel = previewList.getSelectedValue();
                if (sel != null) {
                    loadTemplate(sel);
                }
            }
        });

        // ===== PARTE DESTRA: griglia + barra comandi =====
        // pannello destro che contiene (CENTER=griglia, SOUTH=pulsanti)
        JPanel rightPanel = new JPanel(new BorderLayout());
        add(rightPanel, BorderLayout.CENTER);

        // griglia al centro, centrata
        this.gridPanel = new SudokuGridPanel(board);
        this.gridPanel.setMode(SudokuGridPanel.Mode.EDITOR);
        this.gridPanel.setOnChange(this::updateValidation);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.add(gridPanel);
        rightPanel.add(centerWrapper, BorderLayout.CENTER);

        // barra comandi in basso (solo nella parte destra)
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.add(new JLabel("Editor: 1–9, 0/Canc per svuotare"));

        bottom.add(new JLabel("   Difficoltà:"));
        difficultyCombo = new JComboBox<>(new String[]{"Facile", "Medio", "Difficile", "Esperto"});
        difficultyCombo.setSelectedItem("Medio");
        bottom.add(difficultyCombo);

        saveBtn = new JButton("Salva");
        saveBtn.addActionListener(e -> saveTemplate());
        bottom.add(Box.createHorizontalStrut(10));
        bottom.add(saveBtn);

        clearBtn = new JButton("Pulisci sudoku");
        clearBtn.addActionListener(e -> clearBoard());
        bottom.add(clearBtn);

        deleteBtn = new JButton("Elimina sudoku");
        deleteBtn.addActionListener(e -> deleteCurrent());
        deleteBtn.setEnabled(false);
        bottom.add(deleteBtn);

        statusLabel = new JLabel("Pronto");
        bottom.add(Box.createHorizontalStrut(20));
        bottom.add(statusLabel);

        rightPanel.add(bottom, BorderLayout.SOUTH);

        // carico subito i template
        reloadTemplates();
        updateValidation();
    }

    private void loadTemplate(SudokuTemplateEntry entry) {
        this.board = entry.board;
        this.gridPanel.setBoard(this.board);
        this.currentFile = entry.file;
        this.deleteBtn.setEnabled(true);

        // carichiamo la difficoltà dai metadati, se c'è
        if (entry.metadata != null) {
            String d = entry.metadata.getDifficulty();
            if (d != null) difficultyCombo.setSelectedItem(d);
        } else {
            difficultyCombo.setSelectedItem("Medio");
        }

        updateValidation();
    }

    private void clearBoard() {
        this.board = new SudokuBoard();
        this.gridPanel.setBoard(this.board);
        this.currentFile = null;
        this.deleteBtn.setEnabled(false);
        this.difficultyCombo.setSelectedItem("Medio");
        updateValidation();
    }

    private void deleteCurrent() {
        if (currentFile == null) return;
        int res = JOptionPane.showConfirmDialog(this,
                "Vuoi davvero eliminare questo sudoku?",
                "Conferma eliminazione",
                JOptionPane.YES_NO_OPTION);
        if (res != JOptionPane.YES_OPTION) return;

        repo.deleteWithMetadata(currentFile);
        clearBoard();
        reloadTemplates();
    }

    // >>> versione corretta che non azzera metadati esistenti <<<
    private void saveTemplate() {
        if (!saveBtn.isEnabled()) return;

        // calcolo hash attuale
        String hash = SudokuHash.hash(board);
        File newFile = new File(repo.getDirectory(), hash + ".txt");

        // caso 1: sto salvando LO STESSO sudoku (stesso file)
        if (currentFile != null && currentFile.equals(newFile)) {
            try {
                repo.save(board, newFile);

                // carico metadati esistenti e aggiorno solo la difficoltà
                SudokuMetadata meta = repo.loadMetadata(newFile);
                meta.setDifficulty((String) difficultyCombo.getSelectedItem());
                repo.saveMetadata(newFile, meta);

                JOptionPane.showMessageDialog(this, "Aggiornato: " + newFile.getName());
                reloadTemplates();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Errore salvataggio: " + ex.getMessage(),
                        "Errore", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }

        // caso 2: sudoku nuovo (hash diverso)
        if (currentFile != null && !currentFile.equals(newFile)) {
            repo.deleteWithMetadata(currentFile);
        }

        try {
            repo.save(board, newFile);
            SudokuMetadata meta = new SudokuMetadata();
            meta.setDifficulty((String) difficultyCombo.getSelectedItem());
            repo.saveMetadata(newFile, meta);

            this.currentFile = newFile;
            this.deleteBtn.setEnabled(true);
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
