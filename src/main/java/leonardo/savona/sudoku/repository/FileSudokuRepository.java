package leonardo.savona.sudoku.repository;

import leonardo.savona.sudoku.io.SudokuIO;
import leonardo.savona.sudoku.model.SudokuBoard;
import leonardo.savona.sudoku.model.SudokuMetadata;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileSudokuRepository {

    private final File dir;

    public FileSudokuRepository() {
        // cartella "sudokus" accanto al jar / nella working dir
        this(new File("sudokus"));
    }

    public FileSudokuRepository(File dir) {
        this.dir = dir;
        if (!dir.exists()) {
            // se non c'è la creo, così il programma non esplode
            dir.mkdirs();
        }
    }

    public File getDirectory() {
        return dir;
    }

    public List<File> listFiles() {
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".txt"));
        List<File> out = new ArrayList<>();
        if (files != null) {
            Collections.addAll(out, files);
        }
        return out;
    }

    public SudokuBoard load(File f) throws IOException {
        return SudokuIO.loadFromFile(f);
    }

    public void save(SudokuBoard board, File file) throws IOException {
        SudokuIO.saveToFile(board, file);
    }

    public File getMetadataFile(File sudokuFile) {
        String name = sudokuFile.getName();
        int idx = name.lastIndexOf('.');
        if (idx != -1) {
            name = name.substring(0, idx);
        }
        return new File(dir, name + ".meta");
    }

    public void saveMetadata(File sudokuFile, SudokuMetadata meta) throws IOException {
        File metaFile = getMetadataFile(sudokuFile);
        meta.save(metaFile);
    }

    public SudokuMetadata loadMetadata(File sudokuFile) {
        File metaFile = getMetadataFile(sudokuFile);
        return SudokuMetadata.load(metaFile);
    }

    public boolean deleteWithMetadata(File f) {
        boolean ok1 = true;
        if (f != null && f.exists()) {
            ok1 = f.delete();
        }
        File meta = getMetadataFile(f);
        boolean ok2 = true;
        if (meta.exists()) {
            ok2 = meta.delete();
        }
        return ok1 && ok2;
    }
}
