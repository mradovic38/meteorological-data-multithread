package command_processing.command_handlers;

import status_tracking.StatusTracker;
import file_processing.ScanSingleTask;
import command_processing.Command;
import utils.ScanJobContext;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ScanCommandHandler implements CommandHandler {

    // ovo prati da li su 2 scan komande na istom fajlu, pa da ne bi doslo do konflikta
    private static final Map<Path, Object> fileLocks = new ConcurrentHashMap<>();

    private final Path dir;
    private final ExecutorService fileProcessingThreadPool;

    private final Map<String, ScanJobContext> activeJobs = new ConcurrentHashMap<>();

    private final AtomicBoolean shutdown;


    public ScanCommandHandler(Path dir, ExecutorService fileProcessingThreadPool, AtomicBoolean shutdown) {
        this.fileProcessingThreadPool = fileProcessingThreadPool;
        this.dir = dir;
        this.shutdown = shutdown;
    }
    public void handle(Command command) {
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
            scan(command, min, max, letter, outputPath, dir, jobName, fileProcessingThreadPool);

        } catch (Exception e) {
            System.err.println("[CMD] SCAN error: " + e.getMessage());
        }
    }


    private void scan(Command command, double minTemp, double maxTemp, char letter, Path outputPath, Path dir, String jobName, ExecutorService executor) {
        // status -> running
        StatusTracker.updateStatus(jobName, StatusTracker.JobStatus.RUNNING, command);

        BufferedWriter writer = null;

        fileLocks.putIfAbsent(outputPath, new Object());
        Object fileLock = fileLocks.get(outputPath);

        synchronized (fileLock) {

            try {
                writer = new BufferedWriter(new FileWriter(outputPath.toFile(), false));



                List<Path> files = Files.list(dir)
                        .filter(p -> Files.isRegularFile(p) && (p.toString().endsWith(".txt") || p.toString().endsWith(".csv")))
                        .collect(Collectors.toList());


                Object scanLock = new Object(); // lock za pisanje u fajl - nisam sig da li je write atomicno

                AtomicBoolean cancelledFlag = new AtomicBoolean(false);
                // ? stoji jer nam je nebitan rezultat, ovako hocu samo da grupisem te rezultate da bih posle updatovao status
                // fora je sto je runnable ne callable ScanSingleTask, pa ne vraca nista, al nije ni bitno
                BufferedWriter finalWriter = writer;
                AtomicInteger counter = new AtomicInteger(0);
                int totalFiles = files.size();

                files.stream()
                        .map(file -> executor.submit(new ScanSingleTask(file, minTemp, maxTemp, letter, scanLock,
                                finalWriter, cancelledFlag, counter, totalFiles, jobName, activeJobs, command,
                                fileLocks, outputPath, shutdown))).collect(Collectors.toList());


            } catch (Exception e) {
                StatusTracker.updateStatus(jobName, StatusTracker.JobStatus.FAILED, command);
                System.err.println("[SCAN] " +  command.getName() +  " failed: " + e.getMessage());
            }
        }
    }

    public List<Command> cancelActiveJobs() {
        List<Command> commands = new ArrayList<>();

        for (ScanJobContext context : activeJobs.values()) {
            context.setCancelledFlag(true);
            StatusTracker.updateStatus(context.getJobId(), StatusTracker.JobStatus.CANCELLED, null);
            commands.add(context.getCommand());
        }
        activeJobs.clear();
        return commands;
    }


}
