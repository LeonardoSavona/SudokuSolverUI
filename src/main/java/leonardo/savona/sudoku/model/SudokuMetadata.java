package leonardo.savona.sudoku.model;

import java.io.*;
import java.nio.file.Files;
import java.util.Properties;

public class SudokuMetadata {
    private boolean solved;
    private long bestTimeMillis; // 0 = mai risolto
    private String difficulty = "Medio";   // 👈 default

    public boolean isSolved() {
        return solved;
    }

    public long getBestTimeMillis() {
        return bestTimeMillis;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        if (difficulty != null && !difficulty.isEmpty()) {
            this.difficulty = difficulty;
        }
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
        try (InputStream in = Files.newInputStream(metaFile.toPath())) {
            p.load(in);
            m.solved = Boolean.parseBoolean(p.getProperty("solved", "false"));
            m.bestTimeMillis = Long.parseLong(p.getProperty("bestTimeMillis", "0"));
            m.difficulty = p.getProperty("difficulty", "Medio");
        } catch (Exception ignored) {}
        return m;
    }

    public void save(File metaFile) throws IOException {
        Properties p = new Properties();
        p.setProperty("solved", Boolean.toString(solved));
        p.setProperty("bestTimeMillis", Long.toString(bestTimeMillis));
        p.setProperty("difficulty", difficulty != null ? difficulty : "Medio");
        try (OutputStream out = Files.newOutputStream(metaFile.toPath())) {
            p.store(out, "Sudoku metadata");
        }
    }
}
