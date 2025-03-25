package observing;

import file_processing.FileProcessor;
import utils.StationStats;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReadWriteLock;

public class DirectoryObserver implements Runnable {
    private final Path dir;
    private final Map<Path, Long> lastModifiedMap = new ConcurrentHashMap<>();
    private final ExecutorService processingPool;

    private final Map<Character, StationStats> inMemoryMap;
    private final ReadWriteLock readWriteLock;

    public DirectoryObserver(Path dir, ExecutorService processingPool, Map<Character, StationStats> inMemoryMap, ReadWriteLock readWriteLock) {
        this.dir = dir;
        this.processingPool = processingPool;
        this.inMemoryMap = inMemoryMap;
        this.readWriteLock = readWriteLock;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                observe();
                Thread.sleep(5000); // provera svakih 5s
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void observe() {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.{txt,csv}")) {
            for (Path file : stream) {
                // promena
                long lastMod = Files.getLastModifiedTime(file).toMillis();
                Long prevMod = lastModifiedMap.put(file, lastMod);

                if(prevMod == null || prevMod != lastMod) {
                    if (prevMod == null) {
                        System.out.println("[OBS] New file detected: " + file);
                    } else {
                        System.out.println("[OBS] File modified: " + file);
                    }
                    try {
                        readWriteLock.writeLock().lock();
                        inMemoryMap.clear();
                        processingPool.submit(new FileProcessor(file, false, inMemoryMap, readWriteLock));
                    } finally {
                        readWriteLock.writeLock().unlock();
                    }

                }
            }
        } catch (IOException e) {
            System.err.println("[OBS] Error checking directory: " + e.getMessage());
        }
    }
}