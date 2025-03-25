package tasks;

import status_tracking.StatusTracker;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.stream.Collectors;

public class ScanTask {
    private final double minTemp;
    private final double maxTemp;
    private final char letter;
    private final Path outputPath;
    private final String jobName;
    private final ExecutorService executor;
    private final ReadWriteLock readWriteLock;
    private final Path dir;

    public ScanTask(double minTemp, double maxTemp, char letter, Path outputPath, Path dir, String jobName, ExecutorService executor, ReadWriteLock readWriteLock){
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.letter = Character.toUpperCase(letter);
        this.outputPath = outputPath;
        this.dir = dir;
        this.jobName = jobName;
        this.executor = executor;
        this.readWriteLock = readWriteLock;
    }

    public void scan() {
        // status -> running
        StatusTracker.updateStatus(jobName, StatusTracker.JobStatus.RUNNING);
        try {
            FileWriter fw = new FileWriter(outputPath.toFile(), false);
            BufferedWriter writer = new BufferedWriter(fw);

            List<Path> files = Files.list(dir)
                    .filter(p -> Files.isRegularFile(p) && (p.toString().endsWith(".txt") || p.toString().endsWith(".csv")))
                    .collect(Collectors.toList());

            // ? stoji jer nam je nebitan rezultat, ovako hocu samo da grupisem te rezultate da bih posle updatovao status
            // fora je sto je runnable ne callable ScanSingleTask, pa ne vraca nista, al nije ni bitno
            List<Future<?>> futures = files.stream()
                    .map(file -> executor.submit(new ScanSingleTask(file, minTemp, maxTemp, letter, readWriteLock, writer)))
                    .collect(Collectors.toList());

            // Ceka da se zavrse svi taskovi pa ce da stavi da je completed. ovi future-i vracaju null ja msm, al to nam nije bitno, bitno da postoje
            for (Future<?> future : futures) {
                future.get();
            }
            // status -> completed
            StatusTracker.updateStatus(jobName, StatusTracker.JobStatus.COMPLETED);
        } catch (Exception e) {
            System.err.println("[SCAN] job failed: " + e.getMessage());
            StatusTracker.updateStatus(jobName, StatusTracker.JobStatus.FAILED);
        }
    }
}