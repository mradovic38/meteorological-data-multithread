package report_generation;

import utils.StationStats;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class MapWriter {

    private final Map<Character, StationStats> inMemoryMap;
    private final Path exportPath;

    public MapWriter(Map<Character, StationStats> inMemoryMap, Path exportPath) {
        this.inMemoryMap = inMemoryMap;
        this.exportPath = exportPath;
    }

    public void write(String prefix){
        try (BufferedWriter writer = Files.newBufferedWriter(this.exportPath)) {
            // Write CSV header
            writer.write("Letter,Station count,Sum\n");

            // Sort entries alphabetically and write data
            inMemoryMap.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEach(entry -> {
                        try {
                            Character letter = entry.getKey();
                            StationStats.StationStatsSnapshot snap = entry.getValue().getSnapshot();

                            String line = String.format("%c,%s,%s\n",
                                    letter,
                                    snap.getCount(),
                                    snap.getSum());
                            writer.write(line);
                        } catch (Exception e) {
                            System.err.println(prefix + "Error writing entry: " + e.getMessage());
                        }
                    });

            System.out.println(prefix + "Successfully exported to " + exportPath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println(prefix + "Map writing failed.");
        }
    }
}
