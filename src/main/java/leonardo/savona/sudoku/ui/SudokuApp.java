package leonardo.savona.sudoku.ui;

import javax.swing.SwingUtilities;

public class SudokuApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
