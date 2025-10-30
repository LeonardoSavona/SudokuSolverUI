package leonardo.savona.sudoku.repository;

import leonardo.savona.sudoku.io.SudokuIO;
import leonardo.savona.sudoku.model.SudokuBoard;
import leonardo.savona.sudoku.model.SudokuMetadata;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileSudokuRepository {

    private final File dir;

    public FileSudokuRepository() {
        this(new File("sudokus"));
    }

    public FileSudokuRepository(File dir) {
        this.dir = dir;
        if (!dir.exists()) dir.mkdirs();
    }

    public File getDirectory() {
        return dir;
    }

    public File getMetadataFile(File sudokuFile) {
        String name = sudokuFile.getName();
        // es: 123abc.txt -> 123abc.meta
        int idx = name.lastIndexOf('.');
        if (idx != -1) {
            name = name.substring(0, idx);
        }
        return new File(dir, name + ".meta");
    }

    public void save(SudokuBoard board, File file) throws IOException {
        SudokuIO.saveToFile(board, file);
    }

    public void saveMetadata(File sudokuFile, SudokuMetadata meta) throws IOException {
        File metaFile = getMetadataFile(sudokuFile);
        meta.save(metaFile);
    }

    public SudokuMetadata loadMetadata(File sudokuFile) {
        File metaFile = getMetadataFile(sudokuFile);
        return SudokuMetadata.load(metaFile);
    }

    public List<File> listFiles() {
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".txt"));
        List<File> out = new ArrayList<>();
        if (files != null) {
            for (File f : files) out.add(f);
        }
        return out;
    }

    public SudokuBoard load(File f) throws IOException {
        return SudokuIO.loadFromFile(f);
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
