package command_processing;

import java.util.Collections;
import java.util.Map;

public class Command {
    // ime komande
    private final String name;
    // argumenti
    private final Map<String, String> args;

    public Command(String name, Map<String, String> args) {
        this.name = name;
        // nece biti potrebe za menjanjem
        this.args = Collections.unmodifiableMap(args);
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getArgs() {
        return args;
    }

    // vadi iz mape po kljucu
    public String getArgByKey(String key) {
        return args.get(key);
    }
}