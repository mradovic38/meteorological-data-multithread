import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FileProcessor implements Runnable {
    private final Path file;
    private static final ReadWriteLock lock = new ReentrantReadWriteLock();
    private static final Map<Character, StationStats> stationMap = new TreeMap<>();

    public FileProcessor(Path file, boolean isNew) {
        this.file = file;
    }

    @Override
    public void run() {
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            if (file.toString().endsWith(".csv")) {
                reader.readLine(); // Skip header
            }

            String line;
            while ((line = reader.readLine()) != null) {
                processLine(line);
            }
        } catch (IOException e) {
            System.err.println("Error processing file " + file + ".");
        }
    }

    private void processLine(String line) {
        String[] parts = line.split(";");
        if (parts.length != 2) return;

        String station = parts[0].trim();
        double temp = Double.parseDouble(parts[1].trim());
        char firstChar = Character.toUpperCase(station.charAt(0));

        lock.writeLock().lock();
        try {
            stationMap.compute(firstChar, (k, v) -> {
                if (v == null) return new StationStats(1, temp);
                return new StationStats(v.count + 1, v.sum + temp);
            });
        } finally {
            lock.writeLock().unlock();
        }
    }

    private static class StationStats {
        int count;
        double sum;

        StationStats(int count, double sum) {
            this.count = count;
            this.sum = sum;
        }
    }
}