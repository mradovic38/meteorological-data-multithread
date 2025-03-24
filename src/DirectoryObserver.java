import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class DirectoryObserver implements Runnable {
    private final Path dir;
    private final Map<Path, Long> lastModifiedMap = new ConcurrentHashMap<>();
    private final ExecutorService processingPool;

    public DirectoryObserver(String dirPath, ExecutorService processingPool) {
        this.dir = Paths.get(dirPath);
        this.processingPool = processingPool;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                checkDirectoryChanges();
                Thread.sleep(5000); // provea svakih 5s
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void checkDirectoryChanges() {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.{txt,csv}")) {
            for (Path file : stream) {
                // promena
                long lastMod = Files.getLastModifiedTime(file).toMillis();
                Long prevMod = lastModifiedMap.put(file, lastMod);

                if (prevMod == null) {
                    System.out.println("New file detected: " + file);
                    processingPool.submit(new FileProcessor(file, true));
                } else if (prevMod != lastMod) {
                    System.out.println("File modified: " + file);
                    processingPool.submit(new FileProcessor(file, false));
                }
            }
        } catch (IOException e) {
            System.err.println("Error checking directory: " + e.getMessage());
        }
    }
}