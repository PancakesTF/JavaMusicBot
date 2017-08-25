package ovh.not.javamusicbot;

import com.moandjiezana.toml.Toml;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class ConstantsTest {

    private static final String CONSTANTS_PATH = "constants.toml";

    @Test
    public void parseConstants() {
        System.out.println("Parsing constants...");
        try {
            new Toml().read(new File(CONSTANTS_PATH)).to(Config.class);
        } catch (Exception ex) {
            Assert.fail("An unexpected error occurred while parsing constants: " + ex.getClass().getSimpleName());
            ex.printStackTrace();
        }
    }
}
