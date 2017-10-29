package ovh.not.javamusicbot;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class Constants {
    private TreeMap<String, String> commandDescriptions;
    private TreeMap<String, String> radioStations;

    public Map<String, String> getCommandDescriptions() {
        return Collections.unmodifiableMap(commandDescriptions);
    }

    public String getRadioStationUrl(String name) {
        for (Map.Entry<String, String> entry : this.radioStations.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public String getRadioStations() {
        return this.radioStations.keySet().stream().map(station -> station.substring(1, station.length() - 1))
                                 .collect(Collectors.joining(", "));
    }
}
