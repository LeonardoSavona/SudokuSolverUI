package leonardo.savona.sudoku.ui;

import leonardo.savona.sudoku.ui.panel.AssistedImagePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class AssistedImportDialog extends JDialog {

    private final AssistedImagePanel imagePanel;
    private BufferedImage result;

    public AssistedImportDialog(Frame owner, BufferedImage image) {
        super(owner, "Allinea il sudoku", true);
        this.imagePanel = new AssistedImagePanel(image);

        setLayout(new BorderLayout());
        add(imagePanel, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton("Usa questa immagine");
        JButton cancel = new JButton("Annulla");
        bottom.add(cancel);
        bottom.add(ok);
        add(bottom, BorderLayout.SOUTH);

        ok.addActionListener(e -> {
            // ora esportiamo alla dimensione reale dell'overlay
            result = imagePanel.exportAlignedImage();
            dispose();
        });

        cancel.addActionListener(e -> {
            result = null;
            dispose();
        });

        setSize(800, 800);
        setLocationRelativeTo(owner);
    }

    public BufferedImage getResult() {
        return result;
    }
}
