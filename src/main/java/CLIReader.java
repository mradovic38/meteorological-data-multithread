import command_processing.Command;
import command_processing.CommandParser;
import command_processing.CommandProcessor;
import command_processing.ParseResult;
import command_processing.command_handlers.StartCommandHandler;

import java.nio.file.Path;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public class CLIReader implements Runnable {

    private final BlockingQueue<Command> commandQueue;
    private final String directoryStr;
    private final ExecutorService fileProcessingThreadPool;
    private final ExecutorService observerPool;
    private Path directory = null;
    private final ExecutorService commandProcessorPool;

    private final AtomicBoolean running = new AtomicBoolean(false);


    public CLIReader(BlockingQueue<Command> commandQueue, String directoryStr, ExecutorService commandProcessorPool, ExecutorService fileProcessingThreadPool, ExecutorService observerPool){
        this.commandQueue = commandQueue;
        this.commandProcessorPool = commandProcessorPool;
        this.fileProcessingThreadPool = fileProcessingThreadPool;
        this.observerPool = observerPool;
        this.directoryStr = directoryStr;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);

        while (!Thread.currentThread().isInterrupted()) {

            if (!scanner.hasNextLine()) continue;

            String line = scanner.nextLine().trim();

            ParseResult result = CommandParser.parse(line);
            if (result.hasErrors()) {
                System.err.println("Error: " + result.getErrorMessage());
                continue;
            }

            Command command = result.getCommand();

            // ove se ne upisuju u blokirajuci red
            if(command.getName().equals("START")){
                directory = StartCommandHandler.handleStartCommand(command, directoryStr, fileProcessingThreadPool, observerPool);
                CommandProcessor commandProcessor = new CommandProcessor(directory, commandQueue, fileProcessingThreadPool);
                commandProcessorPool.execute(commandProcessor);
                running.set(true);
                continue;
            }
            else if(command.getName().equals("SHUTDOWN")){
                System.out.println("TODO: Implement shutdown command"); //TODO implement shutdown command
                continue;
            }

            try {
                if(running.get()) // samo ako je startovano
                    commandQueue.put(command);
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