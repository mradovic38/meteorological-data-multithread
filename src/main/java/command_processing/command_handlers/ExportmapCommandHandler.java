package command_processing.command_handlers;

import command_processing.Command;
import utils.StationStats;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReadWriteLock;

public class ExportmapCommandHandler {
    public static void handleExportmapCommand(ExecutorService fileProcessingPool,
                                              Map<Character, StationStats> inMemoryMap,
                                              ReadWriteLock readWriteLock) {
        try{
            readWriteLock.readLock().lock();
            if(inMemoryMap.isEmpty()){
                throw new Exception("Could not export map. Map is empty.");
            }

            fileProcessingPool.submit(() -> {
                // TODO: implement
            });
        }
        catch (Exception e){
            System.err.println("An error occurred while processing EXPORTMAP command: " + e.getMessage());
        }
        finally {
            readWriteLock.readLock().unlock();
        }
    }
}
