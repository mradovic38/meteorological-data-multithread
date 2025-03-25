package command_processing.command_handlers;

import command_processing.Command;
import command_processing.CommandProcessor;
import observing.DirectoryObserver;
import org.yaml.snakeyaml.Yaml;
import report_generation.ReportGenerator;
import utils.StationStats;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;

public class StartCommandHandler implements CommandHandler {

    private final ExecutorService fileProcessingThreadPool;
    private final ExecutorService observerPool;
    private final Map<Character, StationStats> inMemoryMap;
    private final ReadWriteLock readWriteLock;
    private final String directory;

    private Path directoryPath;


    private final ExecutorService commandProcessorPool;
    private final ScheduledExecutorService scheduler;
    private final BlockingQueue<Command> commandQueue;

    public StartCommandHandler(String directory,
                               ExecutorService fileProcessingThreadPool,
                               ExecutorService observerPool,
                               Map<Character, StationStats> inMemoryMap,
                               ReadWriteLock readWriteLock,
                               BlockingQueue<Command> commandQueue,
                               ScheduledExecutorService scheduler,
                               ExecutorService commandProcessorPool) {
        this.directory = directory;
        this.fileProcessingThreadPool = fileProcessingThreadPool;
        this.observerPool = observerPool;
        this.inMemoryMap = inMemoryMap;
        this.readWriteLock = readWriteLock;
        this.commandProcessorPool = commandProcessorPool;
        this.scheduler = scheduler;
        this.commandQueue = commandQueue;
    }



    public void handle(Command command) {


        try {
            if (this.directory == null) {
                this.directoryPath = Paths.get(loadDirectoryFromConfig());
            } else {
                this.directoryPath = Paths.get(directory);
            }
        }
        catch(Exception e){
            System.err.println("[START] Error handling start command: " + e.getMessage());
            return;
        }

        DirectoryObserver watcher = new DirectoryObserver(directoryPath, fileProcessingThreadPool, inMemoryMap, readWriteLock);
        this.observerPool.execute(watcher);

        if(command.getArgs().containsKey("load-config")){
            // TODO: ucitaj stare poslove
        }


        ScanCommandHandler scan = new ScanCommandHandler(this.directoryPath, fileProcessingThreadPool, readWriteLock);
        StatusCommandHandler status = new StatusCommandHandler();
        MapCommandHandler map = new MapCommandHandler(inMemoryMap, readWriteLock);
        ExportmapCommandHandler exportmap = new ExportmapCommandHandler(fileProcessingThreadPool, inMemoryMap, readWriteLock);

        CommandProcessor commandProcessor = new CommandProcessor(this.commandQueue, scan, status, map, exportmap);

        ReportGenerator reportGenerator = new ReportGenerator(inMemoryMap, readWriteLock);
        this.scheduler.scheduleAtFixedRate(reportGenerator, 1, 1, TimeUnit.MINUTES);

        this.commandProcessorPool.execute(commandProcessor);
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
