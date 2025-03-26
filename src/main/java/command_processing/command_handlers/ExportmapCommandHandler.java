package command_processing.command_handlers;

import command_processing.Command;
import report_generation.MapWriter;
import utils.StationStats;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReadWriteLock;

public class ExportmapCommandHandler implements CommandHandler {

    private final ExecutorService fileProcessingPool;
    private final Map<Character, StationStats> inMemoryMap;
    private final ReadWriteLock readWriteLock;
    private final MapWriter mapWriter;
    private final Object exportLock;


    public ExportmapCommandHandler(ExecutorService fileProcessingPool,
                                   Map<Character, StationStats> inMemoryMap,
                                   ReadWriteLock readWriteLock,
                                   Path exportPath,
                                   Object exportLock) {

        this.fileProcessingPool = fileProcessingPool;
        this.inMemoryMap = inMemoryMap;
        this.readWriteLock = readWriteLock;
        this.mapWriter = new MapWriter(inMemoryMap, exportPath);
        this.exportLock = exportLock;
    }

    public void handle(Command command) {
        try {
            synchronized (exportLock) {
                readWriteLock.readLock().lock();
                if (inMemoryMap.isEmpty()) {
                    throw new Exception("Could not export map - Map is empty.");
                }

                mapWriter.write("[EXPORTMAP] ");
            }
        } catch (Exception e) {
            System.err.println("[EXPORTMAP] " + e.getMessage());
        } finally {
            readWriteLock.readLock().unlock();
        }
    }
}
