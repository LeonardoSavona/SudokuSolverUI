package leonardo.savona.sudoku.ui;

import leonardo.savona.sudoku.model.SudokuBoard;
import leonardo.savona.sudoku.model.SudokuMetadata;

import javax.swing.*;
import java.awt.*;

public class SudokuPreviewRenderer extends JPanel implements ListCellRenderer<SudokuTemplateEntry> {

    public SudokuPreviewRenderer() {
        setOpaque(true);
        setLayout(new BorderLayout());
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends SudokuTemplateEntry> list,
                                                  SudokuTemplateEntry value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        removeAll();
        MiniBoardPanel preview = new MiniBoardPanel(value.board, value.metadata);
        add(preview, BorderLayout.CENTER);

        if (isSelected) {
            setBackground(list.getSelectionBackground());
        } else {
            setBackground(list.getBackground());
        }
        setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        return this;
    }

    static class MiniBoardPanel extends JPanel {
        private final SudokuBoard b;
        private final SudokuMetadata m;

        MiniBoardPanel(SudokuBoard b, SudokuMetadata m) {
            this.b = b;
            this.m = m;
            setPreferredSize(new Dimension(120, 120));
            setMinimumSize(new Dimension(120, 120));
            setMaximumSize(new Dimension(120, 120));
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            int size = Math.min(getWidth(), getHeight());
            int cs = size / 9;
            int ox = (getWidth() - size) / 2;
            int oy = (getHeight() - size) / 2;

            g2.setColor(Color.WHITE);
            g2.fillRect(ox, oy, size, size);

            // numeri
            g2.setColor(Color.BLACK);
            Font old = g2.getFont();
            g2.setFont(old.deriveFont(Font.BOLD, cs * 0.45f));
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    int v = b.getValue(r, c);
                    if (v != 0) {
                        String s = String.valueOf(v);
                        FontMetrics fm = g2.getFontMetrics();
                        int x = ox + c * cs + (cs - fm.stringWidth(s)) / 2;
                        int y = oy + r * cs + (cs - fm.getHeight()) / 2 + fm.getAscent();
                        g2.drawString(s, x, y);
                    }
                }
            }
            g2.setFont(old);

            // griglia
            g2.setColor(Color.LIGHT_GRAY);
            for (int i = 0; i <= 9; i++) {
                int p = i * cs;
                g2.drawLine(ox, oy + p, ox + size, oy + p);
                g2.drawLine(ox + p, oy, ox + p, oy + size);
            }
            g2.setStroke(new BasicStroke(2));
            g2.setColor(Color.BLACK);
            for (int i = 0; i <= 9; i += 3) {
                int p = i * cs;
                g2.drawLine(ox, oy + p, ox + size, oy + p);
                g2.drawLine(ox + p, oy, ox + p, oy + size);
            }

            // overlay metadati
            if (m != null && m.isSolved()) {
                // pallino verde + best time
                g2.setColor(new Color(0, 160, 0, 220));
                int r = 16;
                g2.fillOval(getWidth() - r - 4, 4, r, r);
                g2.setColor(Color.WHITE);
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 10f));
                g2.drawString("✔", getWidth() - r - 4 + 4, 4 + 12);

                if (m.getBestTimeMillis() > 0) {
                    String t = formatMillis(m.getBestTimeMillis());
                    g2.setColor(new Color(0, 0, 0, 180));
                    g2.fillRoundRect(4, 4, g2.getFontMetrics().stringWidth(t) + 8, 16, 8, 8);
                    g2.setColor(Color.WHITE);
                    g2.drawString(t, 8, 4 + 12);
                }
            }

            g2.dispose();
        }

        private String formatMillis(long ms) {
            long sec = ms / 1000;
            long m = sec / 60;
            long s = sec % 60;
            return String.format("%02d:%02d", m, s);
        }
    }
}
