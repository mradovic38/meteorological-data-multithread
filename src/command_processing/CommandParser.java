package command_processing;

import java.util.*;

public class CommandParser {
    // mapiranja - u --
    private static final Map<String, Map<String, String>> COMMAND_ARGS_MAPPINGS = new HashMap<>();
    // obavezni argumenti
    private static final Map<String, Set<String>> COMMAND_REQUIRED_ARGS = new HashMap<>();

    // argumenti posle kojih ne ide vrednost
    public static final List<String> ARGS_NO_VALUE = List.of("save-jobs", "load-jobs");

    static {
        // 1. scan argumenti
        Map<String, String> scanArgsMappings = new HashMap<>();
        scanArgsMappings.put("m", "min");
        scanArgsMappings.put("M", "max");
        scanArgsMappings.put("l", "letter");
        scanArgsMappings.put("o", "output");
        scanArgsMappings.put("j", "job");
        COMMAND_ARGS_MAPPINGS.put("SCAN", scanArgsMappings);

        Set<String> scanRequired = new HashSet<>(Arrays.asList("min", "max", "letter", "output", "job"));
        COMMAND_REQUIRED_ARGS.put("SCAN", scanRequired);

        // 2. status argumenti
        Map<String, String> statusArgsMappings = new HashMap<>();
        statusArgsMappings.put("j", "job");
        COMMAND_ARGS_MAPPINGS.put("STATUS", statusArgsMappings);

        Set<String> statusRequired = new HashSet<>(List.of("job"));
        COMMAND_REQUIRED_ARGS.put("STATUS", statusRequired);

        // 3. map argumenti
        COMMAND_ARGS_MAPPINGS.put("MAP", new HashMap<>());

        // 4. exportmap argumenti
        COMMAND_ARGS_MAPPINGS.put("EXPORTMAP", new HashMap<>());

        // 5. shutdown argumenti
        Map<String, String> shutdownArgsMappings = new HashMap<>();
        shutdownArgsMappings.put("s", "save-jobs");
        COMMAND_ARGS_MAPPINGS.put("SHUTDOWN", shutdownArgsMappings);

        // 6. start argumenti
        Map<String, String> startArgsMappings = new HashMap<>();
        startArgsMappings.put("l", "load-jobs");
        COMMAND_ARGS_MAPPINGS.put("START", startArgsMappings);
    }

    public static ParseResult parse(String line) {
        // nista nije ukucano ili je prazan red
        if (line == null || line.trim().isEmpty()) {
            return new ParseResult("Empty command");
        }

        // split po razmacima, ako nema tokena nema komande
        String[] tokens = line.trim().split("\\s+");
        if (tokens.length == 0) {
            return new ParseResult("Empty command");
        }

        // prvi token je ime komande, ako ne postoji u mapi onda je nepoznata
        String commandName = tokens[0].toUpperCase();
        if (!COMMAND_ARGS_MAPPINGS.containsKey(commandName)) {
            return new ParseResult("Unknown command: " + commandName);
        }

        // MAPIRANJE ARGUMENATA
        Map<String, String> argMappings = COMMAND_ARGS_MAPPINGS.get(commandName);
        Set<String> requiredArgs = COMMAND_REQUIRED_ARGS.get(commandName);

        Map<String, String> args = new HashMap<>();
        List<String> errors = new ArrayList<>();

        int i = 1;
        while (i < tokens.length) {
            String token = tokens[i].toLowerCase();
            // mora da pocne sa - ili --
            if (!token.startsWith("-")) {
                errors.add("Invalid arg format: " + token);
                i++;
                continue;
            }

            String keyPart;
            boolean isLong = token.startsWith("--"); // duza verzija
            if (isLong) {
                keyPart = token.substring(2);
            } else if (token.startsWith("-") && token.length() == 2) {  // kraca verzija
                keyPart = token.substring(1);
            } else {
                errors.add("Invalid arg: " + token); // invalid
                i++;
                continue;
            }

            // vadi argument iz mape, duzi ili kraci oblik
            String longArg;
            if (isLong) {
                longArg = keyPart;
                if (!argMappings.containsValue(longArg)) {
                    errors.add("Unknown arg: --" + longArg);
                    i++;
                    continue;
                }
            } else {
                longArg = argMappings.get(keyPart);
                if (longArg == null) {
                    errors.add("Unknown arg: -" + keyPart);
                    i++;
                    continue;
                }
            }

            // vrednost
            String value = null;
            if (!ARGS_NO_VALUE.contains(longArg)) {
                i++;
                if (i >= tokens.length) {
                    errors.add("Missing value for arg: " + token);
                    break;
                }
                value = tokens[i];
            }

            // stavlja null kao value ako ne postoji value
            args.put(longArg, value);
            i++;
        }

        // obavezni argumenti koji nedostaju
        Set<String> missing = new HashSet<>((requiredArgs != null) ? new HashSet<>(requiredArgs) : Collections.emptySet());
        missing.removeAll(args.keySet());
        if (!missing.isEmpty()) {
            errors.add("Missing required arguments: " + String.join(", ", missing));
        }

        // ako postoje greske, ispisi ih sve, ako ne, parsiraj i salji dalje komandu
        if (!errors.isEmpty()) {
            return new ParseResult(String.join("; ", errors));
        } else {
            return new ParseResult(new Command(commandName, args));
        }
    }


}