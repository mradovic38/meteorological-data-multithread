package command_processing.command_handlers;

import tasks.ScanTask;
import command_processing.Command;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReadWriteLock;

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

            // krece posao
            ScanTask task = new ScanTask(min, max, letter, outputPath, dir, jobName, fileProcessingThreadPool, readWriteLock);
            task.scan();

        } catch (Exception e) {
            System.err.println("[CMD] SCAN error: " + e.getMessage());
        }
    }
}
