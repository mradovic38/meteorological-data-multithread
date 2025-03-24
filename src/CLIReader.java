import Tasks.ScanTask;
import command_processing.Command;
import command_processing.CommandParser;
import command_processing.ParseResult;

import java.util.Scanner;
import java.util.concurrent.BlockingQueue;

public class CLIReader implements Runnable {

    BlockingQueue<Command> commandQueue;

    public CLIReader(BlockingQueue<Command> commandQueue){
        this.commandQueue = commandQueue;
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

            try {
                commandQueue.put(command);
            } catch (InterruptedException e) {
                System.err.println("Error adding command to queue.");
            }
        }
        scanner.close();
    }
}