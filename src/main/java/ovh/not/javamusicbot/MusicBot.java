package ovh.not.javamusicbot;

import com.google.gson.Gson;
import com.moandjiezana.toml.Toml;

import java.io.File;

public final class MusicBot {
    private static final String CONFIG_PATH = "config.toml";
    private static final String CONSTANTS_PATH = "constants.toml";
    public static final String USER_AGENT = "JavaMusicBot (https://github.com/sponges/JavaMusicBot)";
    public static final Gson GSON = new Gson();

    public static void main(String[] args) {
        Config config = new Toml().read(new File(CONFIG_PATH)).to(Config.class);
        System.out.println(config.liamSystems);
        Constants constants = new Toml().read(new File(CONSTANTS_PATH)).to(Constants.class);
        if (args.length == 0) {
            new ShardManager(config, constants);
            return;
        }
        int shardCount = Integer.parseInt(args[0]);
        int minShard = Integer.parseInt(args[1]);
        int maxShard = Integer.parseInt(args[2]);
        new ShardManager(config, constants, shardCount, minShard, maxShard);
    }
}
