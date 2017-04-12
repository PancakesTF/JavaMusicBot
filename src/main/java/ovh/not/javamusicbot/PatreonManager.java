package ovh.not.javamusicbot;

import java.io.IOException;

public class PatreonManager extends GenericUserManager {
    PatreonManager() {
        super("patreons.json");
    }

    public boolean isPatreon(String id) {
        return list.contains(id);
    }

    public void addPatreon(String id) {
        list.add(id);
        try {
            save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removePatreon(String id) {
        list.remove(id);
        try {
            save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
