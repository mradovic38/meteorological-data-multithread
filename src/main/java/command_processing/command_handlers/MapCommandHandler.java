package command_processing.command_handlers;

import command_processing.Command;
import utils.StationStats;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapCommandHandler implements CommandHandler {

    private final Map<Character, StationStats> inMemoryMap;

    public MapCommandHandler(Map<Character, StationStats> inMemoryMap){
        this.inMemoryMap = inMemoryMap;
    }
    public void handle(Command command){
        try{
            if(inMemoryMap.isEmpty()){
                System.out.println("[CMD] Map is empty");
                return;
            }

            Map<Character, StationStats> immutableMap = Map.copyOf(inMemoryMap); // uzmi trenutno stanje mape

            List<Character> keys = new ArrayList<>(immutableMap.keySet());
            keys.sort(Character::compareTo);
            System.out.println("[CMD] Current state of the map:");
            for (int i = 0; i < keys.size() - 1; i += 2) {
                Character key1 = keys.get(i);
                Character key2 = keys.get(i + 1);

                System.out.println(key1 + ": " + immutableMap.get(key1).getSnapshot().toString() + " | " +
                        key2 + ": " + immutableMap.get(key2).getSnapshot().toString());
                System.out.println(key1 + ": " + immutableMap.get(key1).getSnapshot().toString() + " | " +
                        key2 + ": " + immutableMap.get(key2).getSnapshot().toString());
            }

            // ako je neparan broj slova u mapi, printuj samo prvo
            if (keys.size() % 2 != 0) {
                Character lastKey = keys.get(keys.size() - 1);
                System.out.println(immutableMap.get(lastKey) + ": " + immutableMap.get(lastKey).getSnapshot().toString());
            }
        }
        catch (Exception e){
            System.err.println("[MAP] An error occurred while processing MAP command.");
        }

    }
}
