package leonardo.savona.sudoku.ui;

import leonardo.savona.sudoku.model.NoteSet;
import leonardo.savona.sudoku.model.SudokuBoard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SudokuGridPanel extends JPanel {

    public enum Mode { EDITOR, SOLVER }

    private static final int GRID_PIXELS = 540; // dimensione fissa
    private SudokuBoard board;
    private int selectedRow = 0;
    private int selectedCol = 0;
    private boolean noteMode = false;
    private Mode mode = Mode.SOLVER;
    private Runnable onChange = null;

    public SudokuGridPanel(SudokuBoard board) {
        this.board = board;
        setFocusable(true);
        setBackground(Color.WHITE);

        // Mouse: selezione cella + focus
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                int cellSize = getCellSize();
                int originX = (getWidth() - GRID_PIXELS) / 2;
                int originY = (getHeight() - GRID_PIXELS) / 2;
                int x = e.getX() - originX;
                int y = e.getY() - originY;
                if (x >= 0 && y >= 0 && x < GRID_PIXELS && y < GRID_PIXELS) {
                    selectedCol = x / cellSize;
                    selectedRow = y / cellSize;
                    repaint();
                }
            }
        });

        // Key bindings
        InputMap im = getInputMap(WHEN_FOCUSED);
        ActionMap am = getActionMap();

        for (int k = KeyEvent.VK_1; k <= KeyEvent.VK_9; k++) {
            int num = (k - KeyEvent.VK_0);
            String name = "NUM_" + num;
            im.put(KeyStroke.getKeyStroke(k, 0), name);
            am.put(name, new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) { applyNumber(num); fireChange(); }
            });
        }

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_0, 0), "CLEAR");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "CLEAR");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "CLEAR");
        am.put("CLEAR", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { applyNumber(0); fireChange(); }
        });

        // frecce per muoversi
        bindArrow(im, am, KeyEvent.VK_UP,    () -> { if (selectedRow > 0) selectedRow--; repaint(); });
        bindArrow(im, am, KeyEvent.VK_DOWN,  () -> { if (selectedRow < 8) selectedRow++; repaint(); });
        bindArrow(im, am, KeyEvent.VK_LEFT,  () -> { if (selectedCol > 0) selectedCol--; repaint(); });
        bindArrow(im, am, KeyEvent.VK_RIGHT, () -> { if (selectedCol < 8) selectedCol++; repaint(); });

        // scorciatoia: 'N' toggla note
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, 0), "TOGGLE_NOTE");
        am.put("TOGGLE_NOTE", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                setNoteMode(!noteMode);
            }
        });
    }

    private void bindArrow(InputMap im, ActionMap am, int key, Runnable r) {
        String name = "ARROW_" + key;
        im.put(KeyStroke.getKeyStroke(key, 0), name);
        am.put(name, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { r.run(); }
        });
    }

    public void setOnChange(Runnable r) { this.onChange = r; }
    private void fireChange() { if (onChange != null) onChange.run(); }

    public void setMode(Mode mode) { this.mode = mode; }
    public Mode getMode() { return mode; }

    public void setBoard(SudokuBoard board) {
        this.board = board;
        this.selectedRow = 0;
        this.selectedCol = 0;
        repaint();
    }

    public void setNoteMode(boolean noteMode) { this.noteMode = noteMode; repaint(); }
    public boolean isNoteMode() { return noteMode; }

    /** Inserimento numero o nota in base alla modalità. */
    public void applyNumber(int number) {
        if (board == null) return;
        int r = selectedRow, c = selectedCol;
        if (r < 0 || c < 0) return;

        if (noteMode && mode == Mode.SOLVER) {
            if (number >= 1 && number <= 9) board.toggleNote(r, c, number);
        } else {
            if (number == 0) {
                board.setValue(r, c, 0, false);
            } else {
                if (mode == Mode.EDITOR) board.setValue(r, c, number, true);
                else board.setValue(r, c, number);
            }
        }
        repaint();
    }

    private int getCellSize() { return GRID_PIXELS / 9; }

    @Override public Dimension getPreferredSize() { return new Dimension(GRID_PIXELS, GRID_PIXELS); }
    @Override public Dimension getMinimumSize()   { return getPreferredSize(); }
    @Override public Dimension getMaximumSize()   { return getPreferredSize(); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (board == null) return;

        Graphics2D g2 = (Graphics2D) g.create();
        int cellSize = getCellSize();

        // centra la griglia fissa
        int originX = (getWidth() - GRID_PIXELS) / 2;
        int originY = (getHeight() - GRID_PIXELS) / 2;

        // celle
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                int x = originX + c * cellSize;
                int y = originY + r * cellSize;

                // selezione
                g2.setColor((r == selectedRow && c == selectedCol) ? new Color(220,235,255) : Color.WHITE);
                g2.fillRect(x, y, cellSize, cellSize);

                // conflitto
                if (board.isCellInConflict(r, c)) {
                    g2.setColor(new Color(255, 200, 200, 160));
                    g2.fillRect(x, y, cellSize, cellSize);
                }

                int value = board.getValue(r, c);
                if (value != 0) {
                    g2.setColor(board.isFixed(r, c) ? Color.BLACK : Color.BLUE.darker());
                    Font old = g2.getFont();
                    g2.setFont(old.deriveFont(Font.BOLD, cellSize * 0.55f));
                    FontMetrics fm = g2.getFontMetrics();
                    String s = String.valueOf(value);
                    int tx = x + (cellSize - fm.stringWidth(s)) / 2;
                    int ty = y + (cellSize - fm.getHeight()) / 2 + fm.getAscent();
                    g2.drawString(s, tx, ty);
                    g2.setFont(old);
                } else {
                    NoteSet ns = board.getNotes(r, c);
                    if (!ns.isEmpty()) {
                        g2.setColor(Color.GRAY);
                        Font old = g2.getFont();
                        g2.setFont(old.deriveFont(Font.PLAIN, cellSize * 0.20f));
                        FontMetrics fm = g2.getFontMetrics();
                        int small = cellSize / 3;
                        for (int num = 1; num <= 9; num++) {
                            if (ns.hasNote(num)) {
                                int idx = num - 1;
                                int subRow = idx / 3;
                                int subCol = idx % 3;
                                int sx = x + subCol * small;
                                int sy = y + subRow * small;
                                String s = String.valueOf(num);
                                int tx = sx + (small - fm.stringWidth(s)) / 2;
                                int ty = sy + (small - fm.getHeight()) / 2 + fm.getAscent();
                                g2.drawString(s, tx, ty);
                            }
                        }
                        g2.setFont(old);
                    }
                }
            }
        }

        // griglia sottile
        g2.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i <= 9; i++) {
            int pos = i * cellSize;
            g2.drawLine(originX, originY + pos, originX + GRID_PIXELS, originY + pos);
            g2.drawLine(originX + pos, originY, originX + pos, originY + GRID_PIXELS);
        }
        // griglia spessa
        g2.setStroke(new BasicStroke(3));
        g2.setColor(Color.BLACK);
        for (int i = 0; i <= 9; i += 3) {
            int pos = i * cellSize;
            g2.drawLine(originX, originY + pos, originX + GRID_PIXELS, originY + pos);
            g2.drawLine(originX + pos, originY, originX + pos, originY + GRID_PIXELS);
        }

        g2.dispose();
    }
}
