package report_generation;

import utils.StationStats;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;

public class MapWriter{

    private final Map<Character, StationStats> inMemoryMap;
    private final Path exportPath;
    private final ReadWriteLock readWriteLock;

    // osigurava da exportmap i periodicni izvestaj ne rade istovremeno
    private final Object exportLock;

    private final AtomicBoolean shutdown;


    public MapWriter(Map<Character, StationStats> inMemoryMap, Path exportPath, ReadWriteLock readWriteLock, AtomicBoolean shutdown) {
        this.inMemoryMap = inMemoryMap;
        this.exportPath = exportPath;

        this.readWriteLock = readWriteLock;
        this.exportLock = new Object();
        this.shutdown = shutdown;
    }

    public void write(String prefix) {

        synchronized (exportLock) {


            if(shutdown.get()){
                System.out.println(prefix + "Shutting down...");
                return;
            }

            try (BufferedWriter writer = Files.newBufferedWriter(this.exportPath)) {

                System.out.println(prefix + "Exporting map...");

                if (inMemoryMap.isEmpty()) {
                    System.err.println(prefix + "Could not export map - Map is empty.");
                    return;
                }

                readWriteLock.readLock().lock();
                // Write CSV header
                writer.write("Letter;Station count;Sum\n");

                // Sort entries alphabetically and write data
                inMemoryMap.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .forEach(entry -> {
                            try {
                                Character letter = entry.getKey();
                                StationStats.StationStatsSnapshot snap = entry.getValue().getSnapshot();

                                String line = String.format("%c;%s;%s\n",
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
            finally{
                readWriteLock.readLock().unlock();
            }
        }
    }
}
