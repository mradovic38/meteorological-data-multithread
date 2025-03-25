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
        StatusTracker.updateStatus(jobName, StatusTracker.JobStatus.RUNNING);
        try {
            FileWriter fw = new FileWriter(outputPath.toFile(), false);
            BufferedWriter writer = new BufferedWriter(fw);

            List<Path> files = Files.list(dir)
                    .filter(p -> Files.isRegularFile(p) && (p.toString().endsWith(".txt") || p.toString().endsWith(".csv")))
                    .collect(Collectors.toList());

            List<Future<?>> futures = files.stream()
                    .map(file -> executor.submit(new ScanSingleTask(file, minTemp, maxTemp, letter, readWriteLock, writer)))
                    .collect(Collectors.toList());

            // Wait for all file tasks to complete
            for (Future<?> future : futures) {
                future.get();
            }
            StatusTracker.updateStatus(jobName, StatusTracker.JobStatus.COMPLETED);
        } catch (Exception e) {
            System.err.println("[SCAN] job failed: " + e.getMessage());
            StatusTracker.updateStatus(jobName, StatusTracker.JobStatus.FAILED);
        }
    }
}