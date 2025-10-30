package leonardo.savona.sudoku.io;

import leonardo.savona.sudoku.model.SudokuBoard;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class SudokuIO {

    /**
     * Salva SOLO i valori (fissi) della board.
     * Tutto ciò che è != 0 verrà considerato fisso quando si ricarica.
     */
    public static void saveToFile(SudokuBoard board, File file) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8))) {
            for (int r = 0; r < SudokuBoard.SIZE; r++) {
                StringBuilder sb = new StringBuilder();
                for (int c = 0; c < SudokuBoard.SIZE; c++) {
                    if (c > 0) sb.append(' ');
                    sb.append(board.getValue(r, c));
                }
                bw.write(sb.toString());
                bw.newLine();
            }
        }
    }

    /**
     * Carica un sudoku da file. Tutti i numeri diversi da 0 diventano "fixed".
     */
    public static SudokuBoard loadFromFile(File file) throws IOException {
        SudokuBoard board = new SudokuBoard();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            int row = 0;
            while ((line = br.readLine()) != null && row < SudokuBoard.SIZE) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length != SudokuBoard.SIZE) {
                    throw new IOException("Riga " + row + " non ha 9 numeri");
                }
                for (int col = 0; col < SudokuBoard.SIZE; col++) {
                    int value = Integer.parseInt(parts[col]);
                    if (value != 0) {
                        board.setValue(row, col, value, true);
                    }
                }
                row++;
            }
        }
        return board;
    }
}
