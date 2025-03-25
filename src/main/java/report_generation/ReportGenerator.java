package report_generation;

import utils.StationStats;

import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

public class ReportGenerator implements Runnable{

    private final Map<Character, StationStats> inMemoryMap;
    private final ReadWriteLock readWriteLock;

    public ReportGenerator(Map<Character, StationStats> inMemoryMap, ReadWriteLock readWriteLock) {
        this.inMemoryMap = inMemoryMap;
        this.readWriteLock = readWriteLock;
    }
    @Override
    public void run() {

        System.out.println("Generating report...");
    }
}
