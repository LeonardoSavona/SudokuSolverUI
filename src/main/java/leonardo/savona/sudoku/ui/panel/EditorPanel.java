package leonardo.savona.sudoku.ui.panel;

import leonardo.savona.sudoku.model.Sudoku;
import leonardo.savona.sudoku.model.SudokuMetadata;
import leonardo.savona.sudoku.ocr.AssistedSudokuImporter;
import leonardo.savona.sudoku.repository.FileSudokuRepository;
import leonardo.savona.sudoku.ui.AssistedImportDialog;
import leonardo.savona.sudoku.ui.LoadingDialog;
import leonardo.savona.sudoku.ui.SudokuPreviewRenderer;
import leonardo.savona.sudoku.ui.SudokuTemplateEntry;
import leonardo.savona.sudoku.util.SudokuHash;
import leonardo.savona.sudoku.util.SudokuUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class EditorPanel extends JPanel {

    private Sudoku board;
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

    public EditorPanel() {
        this.board = new Sudoku();

        setLayout(new BorderLayout());

        // colonna sinistra
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

        // destra
        JPanel rightPanel = new JPanel(new BorderLayout());
        add(rightPanel, BorderLayout.CENTER);

        this.gridPanel = new SudokuGridPanel(board);
        this.gridPanel.setMode(SudokuGridPanel.Mode.EDITOR);
        this.gridPanel.setOnChange(this::updateValidation);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.add(gridPanel);
        rightPanel.add(centerWrapper, BorderLayout.CENTER);

        // bottom bar
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

        reloadTemplates();
        updateValidation();
    }

    /**
     * Importa un sudoku da immagine con editor + OCR.
     * Durante l'OCR mostra un dialog modale di "elaborazione".
     */
    private void importFromImage() {
        JFileChooser chooser = new JFileChooser();
        int res = chooser.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        File imgFile = chooser.getSelectedFile();
        try {
            BufferedImage baseImg = ImageIO.read(imgFile);
            if (baseImg == null) {
                JOptionPane.showMessageDialog(this,
                        "Formato immagine non supportato (usa PNG o JPG).",
                        "Import immagine",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // editor di allineamento
            Window owner = SwingUtilities.getWindowAncestor(this);
            AssistedImportDialog dlg = (owner instanceof Frame)
                    ? new AssistedImportDialog((Frame) owner, baseImg)
                    : new AssistedImportDialog((Frame) null, baseImg);
            dlg.setVisible(true);

            // immagine allineata (900x900 o quella che abbiamo deciso)
            BufferedImage aligned = dlg.getResult();
            if (aligned == null) {
                // utente ha annullato
                return;
            }

            // QUI parte l'elaborazione "pesante"
            // mostriamo un dialog modale "elaborazione..."
            LoadingDialog loading = new LoadingDialog(owner, "Elaborazione sudoku in corso...");
            // lo mostriamo in un altro thread EDT-safe
            SwingUtilities.invokeLater(() -> loading.setVisible(true));

            // facciamo l'OCR in background
            SwingWorker<AssistedSudokuImporter.RecognizedSudoku, Void> worker =
                    new SwingWorker<AssistedSudokuImporter.RecognizedSudoku, Void>() {
                        @Override
                        protected AssistedSudokuImporter.RecognizedSudoku doInBackground() throws Exception {
                            AssistedSudokuImporter importer = new AssistedSudokuImporter();
                            // OCR + sudoku-aware
                            return importer.importSudoku(aligned);
                        }

                        @Override
                        protected void done() {
                            try {
                                AssistedSudokuImporter.RecognizedSudoku rs = get();
                                board = rs.board;
                                gridPanel.setBoard(rs.board);
                                gridPanel.setLowConfidence(rs.lowConfidence);

                                currentFile = null;
                                deleteBtn.setEnabled(false);
                                difficultyCombo.setSelectedItem("Medio");
                                updateValidation();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                JOptionPane.showMessageDialog(
                                        EditorPanel.this,
                                        "Errore durante l'analisi dell'immagine:\n" + ex.getMessage(),
                                        "Errore import",
                                        JOptionPane.ERROR_MESSAGE
                                );
                            } finally {
                                loading.dispose();
                            }
                        }
                    };
            worker.execute();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Errore durante l'import: " + ex.getMessage(),
                    "Errore import immagine",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void loadTemplate(SudokuTemplateEntry entry) {
        this.board = entry.board;
        this.gridPanel.setBoard(this.board);
        this.gridPanel.setLowConfidence(null); // nessuna incertezza da file
        this.currentFile = entry.file;
        this.deleteBtn.setEnabled(true);

        if (entry.metadata != null) {
            String d = entry.metadata.getDifficulty();
            difficultyCombo.setSelectedItem(d != null ? d : "Medio");
        } else {
            difficultyCombo.setSelectedItem("Medio");
        }

        updateValidation();
    }

    private void clearBoard() {
        this.board = new Sudoku();
        this.gridPanel.setBoard(this.board);
        this.gridPanel.setLowConfidence(null);
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

    private void saveTemplate() {
        if (!saveBtn.isEnabled()) return;

        String hash = SudokuHash.hash(board);
        File newFile = new File(repo.getDirectory(), hash + ".txt");

        if (currentFile != null && currentFile.equals(newFile)) {
            try {
                repo.save(board, newFile);
                SudokuMetadata meta = repo.loadMetadata(newFile);
                meta.setDifficulty((String) difficultyCombo.getSelectedItem());
                repo.saveMetadata(newFile, meta);
                JOptionPane.showMessageDialog(this, "Aggiornato: " + newFile.getName());
                reloadTemplates();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Errore salvataggio: " + ex.getMessage(),
                        "Errore",
                        JOptionPane.ERROR_MESSAGE);
            }
            return;
        }

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
            JOptionPane.showMessageDialog(this,
                    "Errore salvataggio: " + ex.getMessage(),
                    "Errore",
                    JOptionPane.ERROR_MESSAGE);
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
                Sudoku b = repo.load(f);
                SudokuMetadata m = repo.loadMetadata(f);
                entries.add(new SudokuTemplateEntry(f, b, m));
            } catch (Exception ignored) {}
        }
        for (SudokuTemplateEntry e : entries) {
            listModel.addElement(e);
        }
    }
}
