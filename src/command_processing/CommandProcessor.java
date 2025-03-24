package command_processing;

import Tasks.ScanTask;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorCompletionService;

public class CommandProcessor implements Runnable{

    BlockingQueue<Command> commandQueue;
    ExecutorCompletionService<String> completionService;

    public CommandProcessor(BlockingQueue<Command> commandQueue, ExecutorCompletionService<String> completionService) {
        this.commandQueue = commandQueue;
        this.completionService = completionService;
    }

    @Override
    public void run() {
        try {
            Command command = commandQueue.take(); // ovo blokira ako je empty

            switch (command.getName()){
                case "SCAN":

                    break;
                case "STATUS":

                    break;
                case "MAP":

                    break;
                case "EXPORTMAP":

                    break;
                case "START":

                    break;
                case "SHUTDOWN":

                    break;
                default:
                    System.err.println("Error: Unknown command");
                    break;
            }

        } catch (InterruptedException e) {
            System.out.println("Command processor interrupted.");
        }
    }
}
