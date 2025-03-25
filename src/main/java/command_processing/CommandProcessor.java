package command_processing;

import command_processing.command_handlers.ScanCommandHandler;
import command_processing.command_handlers.StartCommandHandler;

import java.nio.file.Path;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CommandProcessor implements Runnable{

    private final BlockingQueue<Command> commandQueue;
    private final ExecutorService fileProcessingThreadPool;
    private Path directory = null;

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();


    public CommandProcessor(Path directory,
                            BlockingQueue<Command> commandQueue,
                            ExecutorService fileProcessingThreadPool) {
        this.commandQueue = commandQueue;
        this.fileProcessingThreadPool = fileProcessingThreadPool;
        this.directory = directory;
    }

    @Override
    public void run() {
        try {
            Command command = commandQueue.take(); // ovo blokira ako je empty
            switch (command.getName()){
                case "SCAN":
                    ScanCommandHandler.handleScanCommand(command, directory, fileProcessingThreadPool, readWriteLock);
                    break;
                case "STATUS":

                    break;
                case "MAP":

                    break;
                case "EXPORTMAP":

                    break;
                default:
                    System.err.println("[CMD] Error: Unknown command");
                    break;
            }

        } catch (InterruptedException e) {
            System.out.println("[CMD] Command processor interrupted.");
        }
    }


    public Path getDirectory() {
        return directory;
    }

    public void setDirectory(Path directory) {
        this.directory = directory;
    }
}
