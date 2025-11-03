package leonardo.savona.sudoku.util;

import leonardo.savona.sudoku.model.Sudoku;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class SudokuHash {

    private SudokuHash() {}

    public static String hash(Sudoku board) {
        // stringa tipo "000000000000003085..." (81 caratteri)
        StringBuilder sb = new StringBuilder(board.getSize() * board.getSize());
        for (int r = 0; r < board.getSize(); r++) {
            for (int c = 0; c < board.getSize(); c++) {
                sb.append(board.getValue(r, c));
            }
        }
        return md5(sb.toString());
    }

    private static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] dig = md.digest(input.getBytes());
            // converti in esadecimale
            StringBuilder hex = new StringBuilder(dig.length * 2);
            for (byte b : dig) {
                String h = Integer.toHexString(0xFF & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            // fallback: ritorna la stringa originale (non dovrebbe succedere)
            return input;
        }
    }
}
