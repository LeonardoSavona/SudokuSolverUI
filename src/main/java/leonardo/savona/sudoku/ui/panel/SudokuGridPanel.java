package leonardo.savona.sudoku.ui.panel;

import leonardo.savona.sudoku.model.NoteSet;
import leonardo.savona.sudoku.model.SudokuBoard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SudokuGridPanel extends JPanel {

    public enum Mode { EDITOR, SOLVER }

    private static final int GRID_PIXELS = 540;
    private SudokuBoard board;

    private int selectedRow = -1;
    private int selectedCol = -1;
    private boolean noteMode = false;
    private boolean inputEnabled = true;   // se false non accetto numeri
    private boolean interactionEnabled = true; // se false blocco selezione/highlight avanzato
    private Mode mode = Mode.SOLVER;
    private Runnable onChange = null;

    // 👇 nuovo: celle OCR a bassa confidenza
    private boolean[][] lowConfidence = new boolean[9][9];

    public SudokuGridPanel(SudokuBoard board) {
        this.board = board;
        setFocusable(true);
        setBackground(Color.WHITE);

        // selezione da mouse
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (!interactionEnabled) return;
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

        // key bindings
        InputMap im = getInputMap(WHEN_FOCUSED);
        ActionMap am = getActionMap();

        // tasti 1-9 (riga superiore)
        for (int k = KeyEvent.VK_1; k <= KeyEvent.VK_9; k++) {
            int num = k - KeyEvent.VK_0;
            String name = "NUM_" + num;
            im.put(KeyStroke.getKeyStroke(k, 0), name);
            am.put(name, new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {
                    if (!interactionEnabled) return;
                    applyNumber(num);
                    fireChange();
                }
            });
        }
        // tastierino numerico 1-9
        for (int k = KeyEvent.VK_NUMPAD1; k <= KeyEvent.VK_NUMPAD9; k++) {
            int num = k - KeyEvent.VK_NUMPAD0;
            String name = "NUMPAD_" + num;
            im.put(KeyStroke.getKeyStroke(k, 0), name);
            am.put(name, new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {
                    if (!interactionEnabled) return;
                    applyNumber(num);
                    fireChange();
                }
            });
        }

        // clear (0 / CANC / BACKSPACE)
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_0, 0), "CLEAR");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "CLEAR");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "CLEAR");
        am.put("CLEAR", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (!interactionEnabled) return;
                applyNumber(0);
                fireChange();
            }
        });

        // frecce
        bindArrow(im, am, KeyEvent.VK_UP,    () -> { if (selectedRow > 0) selectedRow--; repaint(); });
        bindArrow(im, am, KeyEvent.VK_DOWN,  () -> { if (selectedRow < 8) selectedRow++; repaint(); });
        bindArrow(im, am, KeyEvent.VK_LEFT,  () -> { if (selectedCol > 0) selectedCol--; repaint(); });
        bindArrow(im, am, KeyEvent.VK_RIGHT, () -> { if (selectedCol < 8) selectedCol++; repaint(); });

        // toggle note (tasto N)
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, 0), "TOGGLE_NOTE");
        am.put("TOGGLE_NOTE", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (!interactionEnabled) return;
                setNoteMode(!noteMode);
            }
        });
    }

    private void bindArrow(InputMap im, ActionMap am, int key, Runnable r) {
        String name = "ARROW_" + key;
        im.put(KeyStroke.getKeyStroke(key, 0), name);
        am.put(name, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (!interactionEnabled) return;
                r.run();
            }
        });
    }

    public void setOnChange(Runnable r) { this.onChange = r; }
    private void fireChange() { if (onChange != null) onChange.run(); }

    public void setMode(Mode mode) { this.mode = mode; }

    public void setInputEnabled(boolean enabled) {
        this.inputEnabled = enabled;
        repaint();
    }

    public void setInteractionEnabled(boolean enabled) {
        this.interactionEnabled = enabled;
        repaint();
    }

    public void setBoard(SudokuBoard board) {
        this.board = board;
        this.selectedRow = -1;
        this.selectedCol = -1;
        // quando cambio board azzero anche le lowConfidence
        this.lowConfidence = new boolean[9][9];
        repaint();
    }

    public void setSelectedCell(int row, int col) {
        this.selectedRow = row;
        this.selectedCol = col;
        repaint();
    }

    public void clearSelection() {
        this.selectedRow = -1;
        this.selectedCol = -1;
        repaint();
    }

    public void setNoteMode(boolean noteMode) {
        this.noteMode = noteMode;
        repaint();
    }

    public boolean isNoteMode() { return noteMode; }

    // 👇 nuovo: arriva dall'EditorPanel dopo l'import da immagine
    public void setLowConfidence(boolean[][] marks) {
        if (marks != null && marks.length == 9 && marks[0].length == 9) {
            this.lowConfidence = marks;
        } else {
            this.lowConfidence = new boolean[9][9];
        }
        repaint();
    }

    public void applyNumber(int number) {
        if (!inputEnabled) return;           // 👈 blocco inserimenti se disattivato
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
        int originX = (getWidth() - GRID_PIXELS) / 2;
        int originY = (getHeight() - GRID_PIXELS) / 2;

        boolean hasSel = selectedRow >= 0 && selectedCol >= 0;
        int selVal = hasSel ? board.getValue(selectedRow, selectedCol) : 0;
        int selBoxRow = hasSel ? (selectedRow / 3) * 3 : -3;
        int selBoxCol = hasSel ? (selectedCol / 3) * 3 : -3;

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                int x = originX + c * cellSize;
                int y = originY + r * cellSize;

                Color bg = Color.WHITE;

                // evidenziazioni (solo in solver e con interazione attiva)
                if (interactionEnabled && mode == Mode.SOLVER && hasSel) {
                    boolean sameRow = (r == selectedRow);
                    boolean sameCol = (c == selectedCol);
                    boolean sameBox = (r >= selBoxRow && r < selBoxRow + 3 &&
                            c >= selBoxCol && c < selBoxCol + 3);
                    if (sameRow || sameCol || sameBox) {
                        bg = new Color(235, 243, 255);
                    }
                    int v = board.getValue(r, c);
                    if (selVal != 0 && v == selVal) {
                        bg = new Color(210, 230, 255);
                    }
                }

                // cella selezionata
                if (r == selectedRow && c == selectedCol) {
                    bg = new Color(200, 220, 255);
                }

                g2.setColor(bg);
                g2.fillRect(x, y, cellSize, cellSize);

                // 👇 nuovo: overlay giallo per celle a bassa confidenza (prima dei conflitti)
                if (lowConfidence[r][c]) {
                    g2.setColor(new Color(255, 255, 170, 140));
                    g2.fillRect(x, y, cellSize, cellSize);
                }

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

        // overlay disattivato
        if (!inputEnabled) {
            g2.setColor(new Color(255, 255, 255, 60));
            g2.fillRect(originX, originY, GRID_PIXELS, GRID_PIXELS);
        }

        g2.dispose();
    }
}
