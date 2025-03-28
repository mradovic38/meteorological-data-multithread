package command_processing;

import java.util.Collections;
import java.util.Map;

public class Command {
    // ime komande
    private String name;
    // argumenti
    private Map<String, String> args;

    private final boolean isPoison;


    public Command(String name, Map<String, String> args) {
        this.name = name;
        // nece biti potrebe za menjanjem
        this.args = Collections.unmodifiableMap(args);
        this.isPoison = false;
    }

    // poison pill konstruktor
    public Command(boolean isPoison){
        this.isPoison = isPoison;
    }

    public boolean isPoison() {
        return isPoison;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name);
        sb.append(" ");

        for(Map.Entry<String, String> arg : args.entrySet()){
            sb.append("--").append(arg.getKey()).append(" ");
            if(arg.getValue() != null)
                sb.append(arg.getValue()).append(" ");
        }
        return sb.toString();
    }
}