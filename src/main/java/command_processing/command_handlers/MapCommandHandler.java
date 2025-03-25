package command_processing.command_handlers;

import command_processing.Command;
import utils.StationStats;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;

public class MapCommandHandler implements CommandHandler {

    private final Map<Character, StationStats> inMemoryMap;
    private final ReadWriteLock readWriteLock;

    public MapCommandHandler(Map<Character, StationStats> inMemoryMap, ReadWriteLock readWriteLock){
        this.inMemoryMap = inMemoryMap;
        this.readWriteLock = readWriteLock;
    }
    public void handle(Command command){
        try{
            readWriteLock.readLock().lock();
            if(inMemoryMap.isEmpty()){
                System.out.println("[CMD] Map is empty");
                return;
            }

            List<Character> keys = new ArrayList<>(inMemoryMap.keySet());
            System.out.println("[CMD] Current state of the map:");
            for (int i = 0; i < keys.size() - 1; i += 2) {
                Character key1 = keys.get(i);
                Character key2 = keys.get(i + 1);

                System.out.println(key1 + ": " + inMemoryMap.get(key1).getSnapshot().toString() + " | " +
                        key2 + ": " + inMemoryMap.get(key2).getSnapshot().toString());
            }

            // ako je neparan broj slova u mapi, printuj samo prvo
            if (keys.size() % 2 != 0) {
                Character lastKey = keys.get(keys.size() - 1);
                System.out.println(inMemoryMap.get(lastKey) + ": " + inMemoryMap.get(lastKey).getSnapshot().toString());
            }
        }
        catch (Exception e){
            System.err.println("[MAP] An error occurred while processing MAP command.");
        }
        finally {
            readWriteLock.readLock().unlock();
        }

    }
}
