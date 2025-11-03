package leonardo.savona.sudoku.ui;

import leonardo.savona.sudoku.model.SudokuMetadata;
import leonardo.savona.sudoku.solver.model.Sudoku;

import java.io.File;

public class SudokuTemplateEntry {
    public final File file;
    public final Sudoku board;
    public final SudokuMetadata metadata;

    public SudokuTemplateEntry(File file, Sudoku board, SudokuMetadata metadata) {
        this.file = file;
        this.board = board;
        this.metadata = metadata;
    }
}
