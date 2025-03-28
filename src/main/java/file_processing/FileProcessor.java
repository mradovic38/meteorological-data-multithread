package file_processing;

import utils.StationStats;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;

public class FileProcessor implements Runnable {
    private final Path file;
    private final ReadWriteLock lock;
    private final Map<Character, StationStats> inMemoryMap;
    private final AtomicBoolean shutdown;

    private final int assignedGeneration;
    private final AtomicInteger currentGeneration;

    public FileProcessor(Path file, Map<Character, StationStats> inMemoryMap, ReadWriteLock lock, AtomicBoolean shutdown,
                         AtomicInteger currentGeneration,
                         int assignedGeneration) {
        this.file = file;
        this.lock = lock;
        this.inMemoryMap = inMemoryMap;

        this.shutdown = shutdown;
        this.currentGeneration = currentGeneration;
        this.assignedGeneration = assignedGeneration;
    }

    @Override
    public void run() {
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            if (file.toString().endsWith(".csv")) {
                reader.readLine(); // Skip header
            }

            String line;
            while ((currentGeneration.get() == assignedGeneration) && !shutdown.get() && (line = reader.readLine()) != null) {
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