package file_processing;

import utils.StationStats;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

public class FileProcessor implements Runnable {
    private final Path file;
    private final ReadWriteLock lock;
    private final Map<Character, StationStats> inMemoryMap;

    public FileProcessor(Path file, Map<Character, StationStats> inMemoryMap, ReadWriteLock lock) {
        this.file = file;
        this.lock = lock;
        this.inMemoryMap = inMemoryMap;
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
            inMemoryMap.computeIfAbsent(firstChar, k -> new StationStats()) // ako nema u mapi napravi novi
                    .addMeasurement(temp); // dodaj mu measurement
        } finally {
            lock.writeLock().unlock();
        }
    }


}