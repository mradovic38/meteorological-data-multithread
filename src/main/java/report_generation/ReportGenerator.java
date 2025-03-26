package report_generation;

import utils.StationStats;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

public class ReportGenerator implements Runnable{

    private final Map<Character, StationStats> inMemoryMap;
    private final ReadWriteLock readWriteLock;
    private final Object exportLock;
    private final MapWriter mapWriter;

    public ReportGenerator(Map<Character, StationStats> inMemoryMap, ReadWriteLock readWriteLock, Path exportPath, Object exportLock){
        this.inMemoryMap = inMemoryMap;
        this.readWriteLock = readWriteLock;
        this.exportLock = exportLock;
        this.mapWriter = new MapWriter(inMemoryMap, exportPath);
    }
    @Override
    public void run() {
        try {
            synchronized (exportLock) {
                readWriteLock.readLock().lock();

                System.out.println("[REPORT_GEN] Generating report...");

                if (inMemoryMap.isEmpty()) {
                    throw new Exception("Could not export map - Map is empty.");
                }

                mapWriter.write("[REPORT_GEN] ");
            }
        } catch (Exception e) {
            System.err.println("[REPORT_GEN] " + e.getMessage());
        } finally {
            readWriteLock.readLock().unlock();
        }

    }
}
