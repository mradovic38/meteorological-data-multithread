package utils;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.List;
import java.util.Map;

public class YAMLUtils {

    public static void writeJobsToYaml(List<String> jobs) {
        File file = new File("src/main/resources/load_config.yaml");

        Map<String, Object> yamlData = loadYaml(file);

        yamlData.put("jobs", jobs);

        writeYaml(file, yamlData);
    }

    private static Map<String, Object> loadYaml(File file) {
        Yaml yaml = new Yaml();
        Map<String, Object> data = null;

        if (file.exists()) {
            try (InputStream inputStream = new FileInputStream(file)) {
                data = yaml.load(inputStream);
            } catch (IOException e) {
                throw new RuntimeException("Error loading YAML file.");
            }
        }


        if (data == null) {
            data = Map.of();
        }

        return data;
    }

    private static void writeYaml(File file, Map<String, Object> data) {
        Yaml yaml = new Yaml(getYamlOptions());

        try (FileWriter writer = new FileWriter(file)) {
            yaml.dump(data, writer);
        } catch (IOException e) {
            throw new RuntimeException("Error writing to YAML file.");
        }
    }

    private static DumperOptions getYamlOptions() {
        DumperOptions options = new DumperOptions();
        options.setIndent(2);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        return options;
    }

    public static String loadFromConfig(String key) {
        try {
            File file = new File("src/main/resources/load_config.yaml");
            Map<String, Object> config = loadYaml(file);
            return (String) config.get(key);
        } catch (Exception e) {
            System.err.println("Check if the config file exists.");
            throw new RuntimeException("Check if the config file exists.");
        }
    }

    public static List<String> loadJobsFromYaml() {
        try {
            File file = new File("src/main/resources/load_config.yaml");
            // Load existing data (if any)
            Map<String, Object> yamlData = loadYaml(file);

            // Get the 'jobs' list from the YAML data
            return (List<String>) yamlData.getOrDefault("jobs", List.of()); // Return an empty list if 'jobs' is not found
        }
        catch(Exception e){
            System.err.println("[START] Error loading jobs from load_config.yaml.");
            throw new RuntimeException("[START] Error loading jobs from load_config.yaml.");
        }
    }


}
