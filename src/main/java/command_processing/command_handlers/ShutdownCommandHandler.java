package command_processing.command_handlers;

import command_processing.Command;
import status_tracking.StatusTracker;
import utils.JobInfo;
import utils.YAMLUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public class ShutdownCommandHandler implements CommandHandler{

    private final BlockingDeque<Command> commands;

    private final AtomicBoolean shutdown;
    private final ExecutorService observerPool;
    private final ScheduledExecutorService reportPool;
    private final ExecutorService fileProcessingPool;

    private final ExecutorService cliPool;
    private final ExecutorService commandProcessingPool;


    public ShutdownCommandHandler(BlockingDeque<Command> commands, ExecutorService observerPool, ScheduledExecutorService reportPool,
                                  ExecutorService fileProcessingPool, ExecutorService cliPool, ExecutorService commandProcessingPool,
                                  AtomicBoolean shutdown){
        this.commands = commands;
        this.reportPool = reportPool;
        this.observerPool = observerPool;
        this.fileProcessingPool = fileProcessingPool;
        this.cliPool = cliPool;
        this.commandProcessingPool = commandProcessingPool;

        this.shutdown = shutdown;
    }
    @Override
    public void handle(Command command) {
        System.out.println("[SHUTDOWN] Shutting down...");

        this.shutdown.set(true);

        try{
            this.commands.putFirst(new Command(true)); // stavi poison pill
        }
        catch (Exception e){
            System.out.println("[SHUTDOWN] Error adding poison pill to queue.");
        }

        try {
            synchronized (observerPool) {
                observerPool.notifyAll();
                observerPool.shutdown();
            }

            synchronized (fileProcessingPool) {
                fileProcessingPool.notifyAll();
                fileProcessingPool.shutdown();
            }

            synchronized (reportPool) {
                reportPool.notifyAll();
                reportPool.shutdown();
            }

            commandProcessingPool.shutdown();

            System.out.println("[SHUTDOWN] Saving unfinished jobs to load_config.yaml...");

            YAMLUtils.writeJobsToYaml(StatusTracker.getCommandsForRunningOrPendingJobs());

            cliPool.shutdown();

            System.out.println("[SHUTDOWN] Done. Exiting...");
        }
        catch (Exception e){
            System.out.println("[SHUTDOWN] Error shutting down.");
        }
    }


}
