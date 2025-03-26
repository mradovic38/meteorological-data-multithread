package command_processing;

import command_processing.command_handlers.CommandHandler;

import java.util.concurrent.BlockingQueue;


public class CommandProcessor implements Runnable{

    private final BlockingQueue<Command> commandQueue;

    private final CommandHandler scanHandler;
    private final CommandHandler statusHandler;
    private final CommandHandler mapHandler;
    private final CommandHandler exportmapHandler;


    public CommandProcessor(
                            BlockingQueue<Command> commandQueue,
                            CommandHandler scanHandler,
                            CommandHandler statusHandler,
                            CommandHandler mapHandler,
                            CommandHandler exportmapHandler) {
        this.commandQueue = commandQueue;
        this.scanHandler = scanHandler;
        this.statusHandler = statusHandler;
        this.mapHandler = mapHandler;
        this.exportmapHandler = exportmapHandler;
    }

    @Override
    public void run() {
        try {
            while(!Thread.currentThread().isInterrupted()) {

                Command command = commandQueue.take(); // ovo blokira ako je empty
                switch (command.getName()) {

                    case "SCAN":
                        this.scanHandler.handle(command);
                        break;
                    case "STATUS":
                        this.statusHandler.handle(command);
                        break;
                    case "MAP":
                        this.mapHandler.handle(command);
                        break;
                    case "EXPORTMAP":
                        this.exportmapHandler.handle(command);
                        break;
                    default:
                        System.err.println("[CMD] Error: Unknown command");
                        break;
                }

            }

        } catch (InterruptedException e) {
            System.out.println("[CMD] Command processor interrupted.");
        }

    }

}
