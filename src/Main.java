import command_processing.Command;
import command_processing.CommandProcessor;

import java.util.concurrent.*;

public class Main {
    // single thread pool za citanje komandi
    private static final ExecutorService cliPool = Executors.newSingleThreadExecutor();

    // thread pool za porcesiranje fajlova
    private static final ExecutorService fileProcessingThreadPool = Executors.newFixedThreadPool(4);

    // da bi se fajlovi izvrsavale po redu
    private static final ExecutorCompletionService<String> completionService =
            new ExecutorCompletionService<>(fileProcessingThreadPool);

    // za procesiranje komandi - delegiranje zadataka
    private static final ExecutorService commandProcessorPool = Executors.newSingleThreadExecutor();

    // single thread pool za posmatranje direktorijuma
    private static final ExecutorService observerPool = Executors.newSingleThreadExecutor();

    // za periodic report
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // blocking queue za komande
    private static final BlockingQueue<Command> commandQueue = new LinkedBlockingQueue<>();


    public static void main(String[] args) {
        DirectoryObserver watcher = new DirectoryObserver("./data", fileProcessingThreadPool);
        CLIReader cli = new CLIReader(commandQueue);
        CommandProcessor commandProcessor = new CommandProcessor(commandQueue, completionService);
        ReportGenerator reportGenerator = new ReportGenerator();

        observerPool.execute(watcher);
        commandProcessorPool.execute(commandProcessor);
        cliPool.execute(cli);
        scheduler.scheduleAtFixedRate(reportGenerator, 1, 1, TimeUnit.MINUTES);


    }

}