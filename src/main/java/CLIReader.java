import command_processing.Command;
import command_processing.CommandParser;
import command_processing.ParseResult;
import command_processing.command_handlers.*;
import status_tracking.StatusTracker;
import utils.StationStats;

import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CLIReader implements Runnable {

    private final BlockingDeque<Command> commandQueue;

    private final AtomicBoolean running = new AtomicBoolean(false);

    private final StartCommandHandler startCommandHandler;
    private final ShutdownCommandHandler shutdownCommandHandler;

    private final AtomicBoolean shutdown;


    public CLIReader(BlockingDeque<Command> commandQueue,
                     String directoryStr,
                     ExecutorService commandProcessorPool,
                     ExecutorService fileProcessingThreadPool,
                     ExecutorService observerPool,
                     ScheduledExecutorService scheduler,
                     ExecutorService cliPool,
                     AtomicBoolean shutdown) {

        this.commandQueue = commandQueue;

        ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        Map<Character, StationStats> inMemoryMap = new ConcurrentHashMap<>();

        this.shutdown = shutdown;

        this.startCommandHandler = new StartCommandHandler(directoryStr, fileProcessingThreadPool,
                observerPool, inMemoryMap, readWriteLock, commandQueue, scheduler, commandProcessorPool, shutdown);


        this.shutdownCommandHandler = new ShutdownCommandHandler(commandQueue, observerPool, scheduler, fileProcessingThreadPool, cliPool, commandProcessorPool, shutdown);
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);

        while (!Thread.currentThread().isInterrupted() && !shutdown.get()) {
            if (!scanner.hasNextLine()) continue;

            String line = scanner.nextLine().trim();

            ParseResult result = CommandParser.parse(line);
            if (result.hasErrors()) {
                System.err.println("Error: " + result.getErrorMessage());
                continue;
            }

            Command command = result.getCommand();

            // ove se ne upisuju u blokirajuci red
            if(command.getName().equalsIgnoreCase("START")){
                this.startCommandHandler.handle(command);
                running.set(true);
                continue;
            }
            else if(command.getName().equalsIgnoreCase("SHUTDOWN")){
                this.shutdownCommandHandler.handle(command);
                continue;
            }

            try {
                if (running.get()) { // samo ako je startovano
                    if (command.getArgs().containsKey("job") && !command.getName().equalsIgnoreCase("STATUS")) {
                        StatusTracker.updateStatus(command.getArgByKey("job"), StatusTracker.JobStatus.PENDING, command);
                    }
                commandQueue.put(command);
            }
            else{
                System.err.println("[CLI] Error: You must start the program with START command first.");
            }
            } catch (InterruptedException e) {
                System.err.println("[CLI] Error adding command to queue.");
            }
        }

        scanner.close();
    }
}