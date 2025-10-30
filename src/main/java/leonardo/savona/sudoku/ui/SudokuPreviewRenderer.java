package leonardo.savona.sudoku.ui;

import leonardo.savona.sudoku.model.SudokuBoard;
import leonardo.savona.sudoku.model.SudokuMetadata;

import javax.swing.*;
import java.awt.*;

public class SudokuPreviewRenderer extends JPanel implements ListCellRenderer<SudokuTemplateEntry> {

    public SudokuPreviewRenderer() {
        setOpaque(true);
        setLayout(new BorderLayout(0, 4));
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends SudokuTemplateEntry> list,
                                                  SudokuTemplateEntry value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        removeAll();

        MiniBoardPanel preview = new MiniBoardPanel(value.board, value.metadata);
        add(preview, BorderLayout.CENTER);

        // label difficoltà sotto
        JLabel diffLabel = new JLabel(formatDifficulty(value.metadata));
        diffLabel.setHorizontalAlignment(SwingConstants.CENTER);
        diffLabel.setFont(diffLabel.getFont().deriveFont(Font.PLAIN, 11f));
        add(diffLabel, BorderLayout.SOUTH);

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            diffLabel.setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            diffLabel.setForeground(Color.DARK_GRAY);
        }

        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        return this;
    }

    private String formatDifficulty(SudokuMetadata m) {
        if (m == null) return "Diff: –";
        String d = m.getDifficulty();
        if (d == null || d.isEmpty()) d = "–";
        return "Diff: " + d;
    }

    static class MiniBoardPanel extends JPanel {
        private final SudokuBoard board;
        private final SudokuMetadata meta;

        MiniBoardPanel(SudokuBoard board, SudokuMetadata meta) {
            this.board = board;
            this.meta = meta;
            setPreferredSize(new Dimension(120, 120));
            setMinimumSize(new Dimension(120, 120));
            setMaximumSize(new Dimension(120, 120));
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setClip(0, 0, getWidth(), getHeight());

            int w = getWidth();
            int h = getHeight();

            int size = Math.min(w, h);
            int cs = size / 9;
            size = cs * 9; // 👈 niente sbordo
            int ox = (w - size) / 2;
            int oy = (h - size) / 2;

            g2.setColor(Color.WHITE);
            g2.fillRect(ox, oy, size, size);

            // numeri
            g2.setColor(Color.BLACK);
            Font original = g2.getFont();
            g2.setFont(original.deriveFont(Font.BOLD, cs * 0.45f));
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    int v = board.getValue(r, c);
                    if (v != 0) {
                        String s = String.valueOf(v);
                        FontMetrics fm = g2.getFontMetrics();
                        int x = ox + c * cs + (cs - fm.stringWidth(s)) / 2;
                        int y = oy + r * cs + (cs - fm.getHeight()) / 2 + fm.getAscent();
                        g2.drawString(s, x, y);
                    }
                }
            }
            g2.setFont(original);

            // griglia fine
            g2.setColor(Color.LIGHT_GRAY);
            for (int i = 0; i <= 9; i++) {
                int p = i * cs;
                g2.drawLine(ox, oy + p, ox + size, oy + p);
                g2.drawLine(ox + p, oy, ox + p, oy + size);
            }
            // griglia spessa
            g2.setStroke(new BasicStroke(2));
            g2.setColor(Color.BLACK);
            for (int i = 0; i <= 9; i += 3) {
                int p = i * cs;
                g2.drawLine(ox, oy + p, ox + size, oy + p);
                g2.drawLine(ox + p, oy, ox + p, oy + size);
            }

            // overlay metadati
            if (meta != null) {
                // best time
                if (meta.isSolved() && meta.getBestTimeMillis() > 0) {
                    String t = formatMillis(meta.getBestTimeMillis());
                    Font f = g2.getFont().deriveFont(Font.BOLD, 10f);
                    g2.setFont(f);
                    FontMetrics fm = g2.getFontMetrics();
                    int bw = fm.stringWidth(t) + 8;
                    int bh = fm.getHeight();
                    g2.setColor(new Color(0, 0, 0, 150));
                    g2.fillRoundRect(ox + 2, oy + 2, bw, bh, 8, 8);
                    g2.setColor(Color.WHITE);
                    g2.drawString(t, ox + 2 + 4, oy + 2 + fm.getAscent());
                }

                // check
                if (meta.isSolved()) {
                    int r = 16;
                    int px = ox + size - r - 4;
                    int py = oy + 4;
                    g2.setColor(new Color(0, 160, 0, 220));
                    g2.fillOval(px, py, r, r);
                    g2.setColor(Color.WHITE);
                    g2.setFont(g2.getFont().deriveFont(Font.BOLD, 10f));
                    g2.drawString("✔", px + 4, py + 12);
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
