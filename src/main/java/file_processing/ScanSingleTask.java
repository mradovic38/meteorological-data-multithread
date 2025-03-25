package file_processing;

import java.io.*;
import java.nio.file.*;

public class ScanSingleTask implements Runnable {
    private final Path file;
    private final double minTemp;
    private final double maxTemp;
    private final char letter;
    private final Object scanLock;
    private final BufferedWriter writer;

    public ScanSingleTask(Path file, double minTemp, double maxTemp, char letter, Object scanLock, BufferedWriter writer) {
        this.file = file;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.letter = Character.toUpperCase(letter);
        this.writer = writer;
        this.scanLock = scanLock;
    }

    @Override
    public void run() {
        try (BufferedReader reader = Files.newBufferedReader(file);) {

            // Skip CSV header
            if (file.toString().endsWith(".csv")) reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
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
            System.err.println("[SCAN] Error processing " + file + ": " + e.getMessage());
        }
    }

}