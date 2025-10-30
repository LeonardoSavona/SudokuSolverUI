package leonardo.savona.sudoku.model;

import java.util.Arrays;

public class NoteSet {

    private final boolean[] notes = new boolean[9]; // index 0 -> numero 1, index 8 -> numero 9

    public void setNote(int number, boolean present) {
        if (number < 1 || number > 9) {
            throw new IllegalArgumentException("Numero nota fuori range: " + number);
        }
        notes[number - 1] = present;
    }

    public boolean hasNote(int number) {
        if (number < 1 || number > 9) {
            return false;
        }
        return notes[number - 1];
    }

    public void toggleNote(int number) {
        if (number < 1 || number > 9) {
            return;
        }
        notes[number - 1] = !notes[number - 1];
    }

    public void clear() {
        Arrays.fill(notes, false);
    }

    public boolean isEmpty() {
        for (boolean b : notes) {
            if (b) return false;
        }
        return true;
    }

    public boolean[] getAll() {
        return Arrays.copyOf(notes, notes.length);
    }
}
