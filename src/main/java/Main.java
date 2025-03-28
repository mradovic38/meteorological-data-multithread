import command_processing.Command;
import command_processing.CommandProcessor;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {
    // single thread pool za citanje komandi
    private static final ExecutorService cliPool = Executors.newSingleThreadExecutor();

    // thread pool za porcesiranje fajlova
    private static final ExecutorService fileProcessingThreadPool = Executors.newFixedThreadPool(4);

//    private static final ExecutorCompletionService<String> completionService =
//            new ExecutorCompletionService<>(fileProcessingThreadPool);

    // za procesiranje komandi - delegiranje zadataka
    private static final ExecutorService commandProcessorPool = Executors.newSingleThreadExecutor();

    // single thread pool za posmatranje direktorijuma
    private static final ExecutorService observerPool = Executors.newSingleThreadExecutor();

    // za periodic report
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // blocking queue za komande
    private static final BlockingDeque<Command> commandQueue = new LinkedBlockingDeque<>();

    private static final AtomicBoolean shutdown = new AtomicBoolean(false);


    public static void main(String[] args) {

        String directory = null;

        if (args.length > 0) {
            directory = args[0];  // ako je prosledjen preko komandne linije
        }

        CLIReader cli = new CLIReader(commandQueue, directory, commandProcessorPool, fileProcessingThreadPool,
                observerPool, scheduler, cliPool, shutdown);

        cliPool.execute(cli);

    }

}