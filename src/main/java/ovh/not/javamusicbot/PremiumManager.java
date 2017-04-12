package ovh.not.javamusicbot;

import java.io.IOException;

public class PremiumManager extends GenericUserManager {
    PremiumManager() {
        super("premiums.json");
    }

    public boolean isPremium(String id) {
        return list.contains(id);
    }

    public void addPremium(String id) {
        list.add(id);
        try {
            save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removePremium(String id) {
        list.remove(id);
        try {
            save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
