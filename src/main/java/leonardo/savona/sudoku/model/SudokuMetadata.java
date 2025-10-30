package leonardo.savona.sudoku.model;

import java.io.*;
import java.util.Properties;

public class SudokuMetadata {
    private boolean solved;
    private long bestTimeMillis; // 0 = mai risolto

    public boolean isSolved() {
        return solved;
    }

    public long getBestTimeMillis() {
        return bestTimeMillis;
    }

    public void updateSolved(long timeMillis) {
        this.solved = true;
        if (this.bestTimeMillis == 0 || timeMillis < this.bestTimeMillis) {
            this.bestTimeMillis = timeMillis;
        }
    }

    public static SudokuMetadata load(File metaFile) {
        SudokuMetadata m = new SudokuMetadata();
        if (metaFile == null || !metaFile.exists()) {
            return m;
        }
        Properties p = new Properties();
        try (InputStream in = new FileInputStream(metaFile)) {
            p.load(in);
            m.solved = Boolean.parseBoolean(p.getProperty("solved", "false"));
            m.bestTimeMillis = Long.parseLong(p.getProperty("bestTimeMillis", "0"));
        } catch (Exception ignored) {}
        return m;
    }

    public void save(File metaFile) throws IOException {
        Properties p = new Properties();
        p.setProperty("solved", Boolean.toString(solved));
        p.setProperty("bestTimeMillis", Long.toString(bestTimeMillis));
        try (OutputStream out = new FileOutputStream(metaFile)) {
            p.store(out, "Sudoku metadata");
        }
    }
}
