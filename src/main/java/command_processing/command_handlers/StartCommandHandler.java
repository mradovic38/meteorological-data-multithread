package command_processing.command_handlers;

import command_processing.Command;
import command_processing.CommandParser;
import command_processing.CommandProcessor;
import command_processing.ParseResult;
import observing.DirectoryObserver;
import report_generation.MapWriter;
import report_generation.ReportGenerator;
import utils.StationStats;
import utils.YAMLUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;

public class StartCommandHandler implements CommandHandler {

    private final ExecutorService fileProcessingThreadPool;
    private final ExecutorService observerPool;
    private final Map<Character, StationStats> inMemoryMap;
    private final ReadWriteLock readWriteLock;
    private final String directory;


    private final ExecutorService commandProcessorPool;
    private final ScheduledExecutorService scheduler;
    private final BlockingDeque<Command> commandQueue;

    private final AtomicBoolean shutdown;

    public StartCommandHandler(String directory,
                               ExecutorService fileProcessingThreadPool,
                               ExecutorService observerPool,
                               Map<Character, StationStats> inMemoryMap,
                               ReadWriteLock readWriteLock,
                               BlockingDeque<Command> commandQueue,
                               ScheduledExecutorService scheduler,
                               ExecutorService commandProcessorPool,
                               AtomicBoolean shutdown) {
        this.directory = directory;
        this.fileProcessingThreadPool = fileProcessingThreadPool;
        this.observerPool = observerPool;
        this.inMemoryMap = inMemoryMap;
        this.readWriteLock = readWriteLock;
        this.commandProcessorPool = commandProcessorPool;
        this.scheduler = scheduler;
        this.commandQueue = commandQueue;
        this.shutdown = shutdown;
    }



    public void handle(Command command) {


        Path directoryPath;
        Path exportPath = Paths.get("");

        try {
            if (this.directory == null) {
                directoryPath = Paths.get(YAMLUtils.loadFromConfig("directory"));
            } else {
                directoryPath = Paths.get(directory);
            }
        }
        catch(Exception e){
            System.err.println("[START] Error handling start command.");
            return;
        }

        if(command.getArgs().containsKey("load-jobs")){
            List<String> commandStrs = YAMLUtils.loadJobsFromYaml();
            for(String commandStr : commandStrs){
                ParseResult res = CommandParser.parse(commandStr);
                if(!res.hasErrors()){
                    Command newCommand = res.getCommand();
                    if(newCommand.getName().equalsIgnoreCase("scan")) {
                        System.out.println("[START] Added command to queue: " + commandStr);
                        commandQueue.add(newCommand);
                    }
                    else{
                        System.out.println("[START] Can only load scan commands from load_config.yaml.");
                    }
                }
                else {
                    System.err.println("[START] Error parsing command: " + commandStr);
                }
                YAMLUtils.writeJobsToYaml(new ArrayList<>());
            }
            exportPath = Paths.get(YAMLUtils.loadFromConfig("export-file"));
        }



        ScanCommandHandler scan = new ScanCommandHandler(directoryPath, fileProcessingThreadPool, shutdown);

        DirectoryObserver watcher = new DirectoryObserver(directoryPath, fileProcessingThreadPool, inMemoryMap, readWriteLock, scan, commandQueue, shutdown, observerPool);
        this.observerPool.execute(watcher);

        StatusCommandHandler status = new StatusCommandHandler();
        MapCommandHandler map = new MapCommandHandler(inMemoryMap);

        // loguje stanje inmemory mape
        MapWriter mapWriter = new MapWriter(inMemoryMap, exportPath, readWriteLock, shutdown);

        ExportmapCommandHandler exportmap = new ExportmapCommandHandler(mapWriter);

        CommandProcessor commandProcessor = new CommandProcessor(commandQueue, scan, status, map, exportmap);

        ReportGenerator reportGenerator = new ReportGenerator(mapWriter);
        this.scheduler.scheduleAtFixedRate(reportGenerator, 1, 1, TimeUnit.MINUTES);

        this.commandProcessorPool.execute(commandProcessor);
    }

}
