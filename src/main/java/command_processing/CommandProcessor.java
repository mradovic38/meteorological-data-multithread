package command_processing;

import utils.StationStats;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReadWriteLock;

import static command_processing.command_handlers.ExportmapCommandHandler.handleExportmapCommand;
import static command_processing.command_handlers.MapCommandHandler.handleMapCommand;
import static command_processing.command_handlers.ScanCommandHandler.handleScanCommand;
import static command_processing.command_handlers.StatusCommandHandler.handleStatusCommand;

public class CommandProcessor implements Runnable{

    private final BlockingQueue<Command> commandQueue;
    private final ExecutorService fileProcessingThreadPool;
    private final Path directory;

    private final ReadWriteLock readWriteLock;

    private final Map<Character, StationStats> inMemoryMap;


    public CommandProcessor(Path directory,
                            BlockingQueue<Command> commandQueue,
                            ExecutorService fileProcessingThreadPool,
                            Map<Character, StationStats> inMemoryMap,
                            ReadWriteLock readWriteLock){
        this.commandQueue = commandQueue;
        this.fileProcessingThreadPool = fileProcessingThreadPool;
        this.directory = directory;
        this.inMemoryMap = inMemoryMap;
        this.readWriteLock = readWriteLock;
    }

    @Override
    public void run() {
        try {
            while(!Thread.currentThread().isInterrupted()) {
                Command command = commandQueue.take(); // ovo blokira ako je empty
                switch (command.getName()) {
                    case "SCAN":
                        handleScanCommand(command, directory, fileProcessingThreadPool, readWriteLock);
                        break;
                    case "STATUS":
                        handleStatusCommand(command);
                        break;
                    case "MAP":
                        handleMapCommand(inMemoryMap, readWriteLock);
                        break;
                    case "EXPORTMAP":
                        handleExportmapCommand(fileProcessingThreadPool, inMemoryMap, readWriteLock);
                        break;
                    default:
                        System.err.println("[CMD] Error: Unknown command");
                        break;
                }
            }

        } catch (InterruptedException e) {
            System.out.println("[CMD] Command processor interrupted.");
        }
        catch (Exception e){
            System.err.println("[CMD] " + e.getMessage());
        }
    }

}
