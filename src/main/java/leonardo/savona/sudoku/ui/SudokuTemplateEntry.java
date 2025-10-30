package leonardo.savona.sudoku.ui;

import leonardo.savona.sudoku.model.SudokuBoard;
import leonardo.savona.sudoku.model.SudokuMetadata;

import java.io.File;

public class SudokuTemplateEntry {
    public final File file;
    public final SudokuBoard board;
    public final SudokuMetadata metadata;

    public SudokuTemplateEntry(File file, SudokuBoard board, SudokuMetadata metadata) {
        this.file = file;
        this.board = board;
        this.metadata = metadata;
    }
}
