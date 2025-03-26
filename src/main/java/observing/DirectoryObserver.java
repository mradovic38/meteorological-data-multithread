package observing;

import file_processing.FileProcessor;
import utils.StationStats;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
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
            boolean changesDetected = false;
            Set<Path> currentFiles = new HashSet<>();

            // modifikaacije / dodavanja
            for (Path file : stream) {
                currentFiles.add(file);

                long lastMod = Files.getLastModifiedTime(file).toMillis();
                Long prevMod = lastModifiedMap.put(file, lastMod);

                if(prevMod == null || prevMod != lastMod) {
                    if (prevMod == null) {
                        System.out.println("[OBS] New file detected: " + file);
                        changesDetected = true;
                    } else {
                        System.out.println("[OBS] File modified: " + file);
                        changesDetected = true;
                    }
                }
            }

            // brisanje fajla
            List<Path> missingFiles = new ArrayList<>();
            for (Path knownFile : lastModifiedMap.keySet()) {
                if (!currentFiles.contains(knownFile)) {
                    System.out.println("[OBS] File deleted: " + knownFile);
                    missingFiles.add(knownFile);
                    changesDetected = true;
                }
            }
            missingFiles.forEach(lastModifiedMap::remove);

            // ako ima promene - reprocess
            if (changesDetected) {
                processAllFiles(currentFiles);
            }


        } catch (IOException e) {
            System.err.println("[OBS] Error checking directory: " + e.getMessage());
        }
    }


    private void processAllFiles(Set<Path> currentFiles) {
        try {
            readWriteLock.writeLock().lock();
            inMemoryMap.clear();

            currentFiles.forEach(file ->
                    processingPool.submit(new FileProcessor(file, inMemoryMap, readWriteLock))
            );

            System.out.println("[OBS] Reprocessing all files (" + currentFiles.size() + " files)");
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }
}