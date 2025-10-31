package leonardo.savona.sudoku.ui;

import leonardo.savona.sudoku.model.SudokuBoard;
import leonardo.savona.sudoku.model.SudokuMetadata;
import leonardo.savona.sudoku.model.SudokuUtils;
import leonardo.savona.sudoku.ocr.AssistedImportDialog;
import leonardo.savona.sudoku.ocr.AssistedSudokuImporter;
import leonardo.savona.sudoku.repository.FileSudokuRepository;
import leonardo.savona.sudoku.util.SudokuHash;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
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
    private final JButton importBtn;
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

        // ===== PARTE DESTRA =====
        JPanel rightPanel = new JPanel(new BorderLayout());
        add(rightPanel, BorderLayout.CENTER);

        // griglia centrata
        this.gridPanel = new SudokuGridPanel(board);
        this.gridPanel.setMode(SudokuGridPanel.Mode.EDITOR);
        this.gridPanel.setOnChange(this::updateValidation);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.add(gridPanel);
        rightPanel.add(centerWrapper, BorderLayout.CENTER);

        // barra comandi in basso (solo destra)
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

        clearBtn = new JButton("Pulisci");
        clearBtn.addActionListener(e -> clearBoard());
        bottom.add(clearBtn);

        importBtn = new JButton("Importa da immagine…");
        importBtn.addActionListener(e -> importFromImage());
        bottom.add(importBtn);

        deleteBtn = new JButton("Elimina");
        deleteBtn.addActionListener(e -> deleteCurrent());
        deleteBtn.setEnabled(false);
        bottom.add(deleteBtn);

        statusLabel = new JLabel("Pronto");
        bottom.add(Box.createHorizontalStrut(20));
        bottom.add(statusLabel);

        rightPanel.add(bottom, BorderLayout.SOUTH);

        // carica i sudoku esistenti
        reloadTemplates();
        updateValidation();
    }

    /**
     * Apre un file chooser, carica l'immagine (PNG/JPG), la mostra nel dialogo con overlay,
     * e poi passa l'immagine allineata all'importer.
     */
    private void importFromImage() {
        JFileChooser chooser = new JFileChooser();
        int res = chooser.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        File imgFile = chooser.getSelectedFile();
        try {
            // 1. carico l'immagine come BufferedImage
            BufferedImage baseImg = ImageIO.read(imgFile);
            if (baseImg == null) {
                // probabilmente è un SVG o formato non supportato da ImageIO
                JOptionPane.showMessageDialog(
                        this,
                        "Formato immagine non supportato (usa PNG o JPG).",
                        "Import immagine",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // 2. apro il dialogo con overlay
            Window owner = SwingUtilities.getWindowAncestor(this);
            AssistedImportDialog dlg;
            if (owner instanceof Frame) {
                dlg = new AssistedImportDialog((Frame) owner, baseImg);
            } else {
                dlg = new AssistedImportDialog((Frame) null, baseImg);
            }
            dlg.setVisible(true);

            BufferedImage aligned = dlg.getResult();
            if (aligned == null) {
                // utente ha annullato
                return;
            }

            // 3. passo al nostro importer "manuale"
            AssistedSudokuImporter importer = new AssistedSudokuImporter();
            SudokuBoard imported = importer.importSudoku(aligned);

            // 4. mostro nell'editor
            this.board = imported;
            this.gridPanel.setBoard(imported);
            this.currentFile = null;           // è nuovo
            this.deleteBtn.setEnabled(false);  // ancora non esiste su disco
            this.difficultyCombo.setSelectedItem("Medio");
            updateValidation();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Errore durante l'import: " + ex.getMessage(),
                    "Errore import immagine",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTemplate(SudokuTemplateEntry entry) {
        this.board = entry.board;
        this.gridPanel.setBoard(this.board);
        this.currentFile = entry.file;
        this.deleteBtn.setEnabled(true);

        if (entry.metadata != null) {
            String d = entry.metadata.getDifficulty();
            if (d != null) {
                difficultyCombo.setSelectedItem(d);
            } else {
                difficultyCombo.setSelectedItem("Medio");
            }
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

    /**
     * Salva il sudoku corrente in base all'hash.
     * Se stiamo salvando lo stesso file, manteniamo i metadati (solved, bestTime).
     */
    private void saveTemplate() {
        if (!saveBtn.isEnabled()) return;

        String hash = SudokuHash.hash(board);
        File newFile = new File(repo.getDirectory(), hash + ".txt");

        // caso: stesso file
        if (currentFile != null && currentFile.equals(newFile)) {
            try {
                repo.save(board, newFile);

                SudokuMetadata meta = repo.loadMetadata(newFile);
                meta.setDifficulty((String) difficultyCombo.getSelectedItem());
                repo.saveMetadata(newFile, meta);

                JOptionPane.showMessageDialog(this,
                        "Aggiornato: " + newFile.getName());
                reloadTemplates();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Errore salvataggio: " + ex.getMessage(),
                        "Errore", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }

        // caso: sudoku cambiato → nuovo hash → elimino vecchio
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
            JOptionPane.showMessageDialog(this,
                    "Salvato: " + newFile.getName());
            reloadTemplates();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Errore salvataggio: " + ex.getMessage(),
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
