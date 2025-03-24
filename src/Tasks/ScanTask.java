package Tasks;

import command_processing.Command;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Callable;

public class ScanTask implements Callable<String> {
    @Override
    public String call() throws Exception {
        return "";
    }
//    private final Command command;
//    private final Path outputPath;
//
//    public ScanTask(Command command) {
//        this.command = command;
//        this.outputPath = Paths.get(command.getArgByKey("output"));
//    }
//
//    @Override
//    public String call() throws Exception {
//        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardOpenOption.CREATE)) {
//
//            Files.walk(Paths.get(command.directory()))
//                    .filter(p -> Files.isRegularFile(p) && (p.toString().endsWith(".txt") || p.toString().endsWith(".csv")))
//                    .forEach(p -> processFile(p, writer));
//        }
//        return null;
//    }
//
//    private void processFile(Path file, BufferedWriter writer) {
//        try (BufferedReader reader = Files.newBufferedReader(file)) {
//            if (file.toString().endsWith(".csv")) reader.readLine();
//
//            String line;
//            while ((line = reader.readLine()) != null) {
//                String[] parts = line.split(";");
//                if (parts.length != 2) continue;
//
//                String station = parts[0].trim();
//                double temp = Double.parseDouble(parts[1].trim());
//
//                if (station.toUpperCase().startsWith(params.letter()) &&
//                        temp >= params.minTemp() && temp <= params.maxTemp()) {
//                    writer.write(line);
//                    writer.newLine();
//                }
//            }
//        } catch (IOException e) {
//            System.err.println("Error processing SCAN for file " + file + ".");
//        }
//    }
}