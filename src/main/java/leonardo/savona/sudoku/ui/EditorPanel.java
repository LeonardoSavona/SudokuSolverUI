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
    private final JButton deleteBtn;
    private final JLabel statusLabel;
    private final JComboBox<String> difficultyCombo;   // 👈 nuovo

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
        bottom.add(new JLabel("Editor: 1–9, 0/Canc per svuotare"));

        // difficoltà
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

        add(bottom, BorderLayout.SOUTH);

        reloadTemplates();
        updateValidation();
    }

    private void loadTemplate(SudokuTemplateEntry entry) {
        this.board = entry.board;
        this.gridPanel.setBoard(this.board);
        this.currentFile = entry.file;
        this.deleteBtn.setEnabled(true);

        // carica difficoltà
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
        difficultyCombo.setSelectedItem("Medio");
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

    private void saveTemplate() {
        if (!saveBtn.isEnabled()) return;

        // calcolo hash attuale
        String hash = SudokuHash.hash(board);
        File newFile = new File(repo.getDirectory(), hash + ".txt");

        // caso 1: sto salvando LO STESSO sudoku (stesso file)
        if (currentFile != null && currentFile.equals(newFile)) {
            try {
                // 1. salvo la board (magari hai cambiato dei numeri)
                repo.save(board, newFile);

                // 2. carico i metadati esistenti
                SudokuMetadata meta = repo.loadMetadata(newFile);

                // 3. aggiorno solo la difficoltà
                meta.setDifficulty((String) difficultyCombo.getSelectedItem());

                // 4. risalvo i metadati -> mantiene best time e solved
                repo.saveMetadata(newFile, meta);

                JOptionPane.showMessageDialog(this, "Aggiornato: " + newFile.getName());
                reloadTemplates();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Errore salvataggio: " + ex.getMessage(),
                        "Errore", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }

        // caso 2: sto salvando un sudoku NUOVO (hash diverso)
        // quindi elimino il vecchio file (se c’era) e creo metadati nuovi
        if (currentFile != null && !currentFile.equals(newFile)) {
            // cancello vecchio sudoku + suoi metadati
            repo.deleteWithMetadata(currentFile);
        }

        try {
            // salvo il nuovo sudoku
            repo.save(board, newFile);

            // creo metadati nuovi (perché è un sudoku nuovo)
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
