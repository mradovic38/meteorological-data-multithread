package command_processing.command_handlers;

import command_processing.Command;
import utils.StationStats;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReadWriteLock;

public class ExportmapCommandHandler implements CommandHandler {

    private final ExecutorService fileProcessingPool;
    private final Map<Character, StationStats> inMemoryMap;
    private final ReadWriteLock readWriteLock;


    public ExportmapCommandHandler(ExecutorService fileProcessingPool,
                                   Map<Character, StationStats> inMemoryMap,
                                   ReadWriteLock readWriteLock) {

        this.fileProcessingPool = fileProcessingPool;
        this.inMemoryMap = inMemoryMap;
        this.readWriteLock = readWriteLock;
    }

    public void handle(Command command) {
        try{
            readWriteLock.readLock().lock();
            if(inMemoryMap.isEmpty()){
                throw new Exception("Could not export map - Map is empty.");
            }


        }
        catch (Exception e){
            System.err.println("[EXPORTMAP] An error occurred while processing EXPORTMAP command: " + e.getMessage());
        }
        finally {
            readWriteLock.readLock().unlock();
        }
    }
}
