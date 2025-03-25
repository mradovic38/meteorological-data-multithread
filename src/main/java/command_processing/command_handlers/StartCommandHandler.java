package command_processing.command_handlers;

import command_processing.Command;
import observing.DirectoryObserver;
import org.yaml.snakeyaml.Yaml;
import utils.StationStats;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReadWriteLock;

public class StartCommandHandler {

    public static Path handleStartCommand(Command command,
                                          String directory,
                                          ExecutorService fileProcessingThreadPool,
                                          ExecutorService observerPool,
                                          Map<Character, StationStats> inMemoryMap,
                                          ReadWriteLock readWriteLock) {
        Path directoryPath;

        try {
            if (directory == null) {
                directoryPath = Paths.get(loadDirectoryFromConfig());
            } else {
                directoryPath = Paths.get(directory);
            }
        }
        catch(Exception e){
            System.err.println("[START] Error handling start command: " + e.getMessage());
            return null;
        }

        DirectoryObserver watcher = new DirectoryObserver(directoryPath, fileProcessingThreadPool, inMemoryMap, readWriteLock);
        observerPool.execute(watcher);

        if(command.getArgs().containsKey("load-config")){
            // TODO: ucitaj stare poslove
        }

        return directoryPath;
    }


    private static String loadDirectoryFromConfig() {
        try (InputStream input = new FileInputStream("src/main/resources/load_config.yaml")) {
            Yaml yaml = new Yaml();
            Map<String, Object> config = yaml.load(input);
            return (String) config.get("directory");  // Assuming YAML has a 'directory' key
        } catch (Exception e) {
            throw new RuntimeException("Check if the config file exists.");
        }
    }
}
