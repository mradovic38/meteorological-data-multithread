package file_processing;

import command_processing.Command;
import command_processing.command_handlers.ScanCommandHandler;
import status_tracking.StatusTracker;
import utils.ScanJobContext;

import java.io.*;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;

public class ScanSingleTask implements Runnable {
    private final Path file;
    private final double minTemp;
    private final double maxTemp;
    private final char letter;
    private final Object scanLock;
    private final BufferedWriter writer;

    private final AtomicBoolean cancelled;

    private final AtomicInteger counter;
    private final int totalFiles;

    private final String jobName;

    private final Map<String, ScanJobContext> activeJobs;
    private final Map<Path, Object> fileLocks;
    private final Path outputPath;

    public ScanSingleTask(Path file, double minTemp, double maxTemp, char letter, Object scanLock,
                          BufferedWriter writer, AtomicBoolean cancelled, AtomicInteger counter, int totalFiles, String jobName,
                          Map<String, ScanJobContext> activeJobs, Command command,
                          Map<Path, Object> fileLocks, Path outputPath) {
        this.file = file;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.letter = Character.toUpperCase(letter);
        this.writer = writer;
        this.scanLock = scanLock;

        this.cancelled = cancelled;

        this.totalFiles = totalFiles;
        this.counter = counter;
        this.jobName = jobName;
        this.activeJobs = activeJobs;
        this.activeJobs.put(jobName, new ScanJobContext(jobName, command, cancelled));
        this.fileLocks = fileLocks;
        this.outputPath = outputPath;
    }

    @Override
    public void run() {
        try (BufferedReader reader = Files.newBufferedReader(file);) {

            // preskoci csv header
            if (file.toString().endsWith(".csv")) reader.readLine();

            String line;
            while ((line = reader.readLine()) != null && !cancelled.get() && !Thread.currentThread().isInterrupted()) {
                String[] parts = line.split(";");
                if (parts.length != 2) continue;

                String station = parts[0].trim();
                double temp;
                try {
                    temp = Double.parseDouble(parts[1].trim());
                } catch (NumberFormatException e) {
                    continue;
                }


                if (station.toUpperCase().startsWith(String.valueOf(letter))){
                    if (temp >= minTemp && temp <= maxTemp) {
                        synchronized (scanLock){
                            writer.write(line);
                            writer.newLine();
                        }
                    }
                }
            }
            writer.flush();

        } catch (IOException e) {
            if (cancelled.get()) { // ako je korisnik cancellovao
                System.out.println("[SCAN] Processing cancelled for " + file);
                StatusTracker.updateStatus(jobName, StatusTracker.JobStatus.CANCELLED, null);
                return;
            }
            System.err.println("[SCAN] Error processing " + file + ": " + e.getMessage());
            StatusTracker.updateStatus(jobName, StatusTracker.JobStatus.FAILED, null);
        }
        finally {
            synchronized (counter) {
                counter.incrementAndGet();
                if (counter.get() == totalFiles) {
                    StatusTracker.updateStatus(jobName, StatusTracker.JobStatus.COMPLETED, null);

                    try {
                        writer.close(); // Properly close the writer
                    } catch (IOException e) {
                        System.err.println("[SCAN] Error closing writer: " + e.getMessage());
                    }

                    this.fileLocks.remove(outputPath);
                    this.activeJobs.remove(jobName);
                }
            }
        }
    }

}