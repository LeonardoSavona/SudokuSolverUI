package leonardo.savona.sudoku.ui;

import javax.swing.*;
import java.awt.*;

public class LoadingDialog extends JDialog {

    public LoadingDialog(Window owner, String message) {
        super(owner, "Attendere...", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JLabel label = new JLabel(message);
        label.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        add(label, BorderLayout.NORTH);

        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        bar.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        add(bar, BorderLayout.CENTER);

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }
}
