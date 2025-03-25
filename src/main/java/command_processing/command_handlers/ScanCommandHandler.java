package command_processing.command_handlers;

import status_tracking.StatusTracker;
import file_processing.ScanSingleTask;
import command_processing.Command;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.stream.Collectors;

public class ScanCommandHandler {


    public static void handleScanCommand(Command command, Path dir, ExecutorService fileProcessingThreadPool, ReadWriteLock readWriteLock) {
        try {

            // PROVERA FORMATA ARGUMENATA

            String minStr = command.getArgByKey("min");
            String maxStr = command.getArgByKey("max");
            String letterStr = command.getArgByKey("letter");
            String outputFile = command.getArgByKey("output");
            String jobName = command.getArgByKey("job");

            double min = Double.parseDouble(minStr);
            double max = Double.parseDouble(maxStr);

            if (min > max) throw new IllegalArgumentException("min > max");
            if (letterStr == null || letterStr.length() != 1)
                throw new IllegalArgumentException("Invalid letter");

            Path outputPath = Paths.get(outputFile);
            char letter = Character.toUpperCase(letterStr.charAt(0));

            // obrada
            scan(min, max, letter, outputPath, dir, jobName, fileProcessingThreadPool, readWriteLock);

        } catch (Exception e) {
            System.err.println("[CMD] SCAN error: " + e.getMessage());
        }
    }


    private static void scan(double minTemp, double maxTemp, char letter, Path outputPath, Path dir, String jobName, ExecutorService executor, ReadWriteLock readWriteLock) {
        // status -> running
        StatusTracker.updateStatus(jobName, StatusTracker.JobStatus.RUNNING);
        try {
            System.out.println("[SCAN] job started");
            readWriteLock.readLock().lock();
            System.out.println("[SCAN] read lock acquired");

            FileWriter fw = new FileWriter(outputPath.toFile(), false);
            BufferedWriter writer = new BufferedWriter(fw);

            List<Path> files = Files.list(dir)
                    .filter(p -> Files.isRegularFile(p) && (p.toString().endsWith(".txt") || p.toString().endsWith(".csv")))
                    .collect(Collectors.toList());


            Object scanLock = new Object(); // lock za pisanje u fajl - nisam sig da li je write atomicno

            // ? stoji jer nam je nebitan rezultat, ovako hocu samo da grupisem te rezultate da bih posle updatovao status
            // fora je sto je runnable ne callable ScanSingleTask, pa ne vraca nista, al nije ni bitno
            List<Future<?>> futures = files.stream()
                    .map(file -> executor.submit(new ScanSingleTask(file, minTemp, maxTemp, letter, scanLock, writer)))
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
        finally {
            readWriteLock.readLock().unlock();
        }
    }
}
