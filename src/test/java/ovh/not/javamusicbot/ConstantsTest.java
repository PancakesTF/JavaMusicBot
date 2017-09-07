package ovh.not.javamusicbot;

import com.moandjiezana.toml.Toml;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class ConstantsTest {
    @Test
    public void parseConstants() {
        System.out.println("Parsing constants...");
        try {
            new Toml().read(new File(MusicBot.CONSTANTS_PATH)).to(Constants.class);
        } catch (Exception ex) {
            Assert.fail("An unexpected error occurred while parsing constants: " + ex.getClass().getSimpleName());
            ex.printStackTrace();
        }
    }
}
