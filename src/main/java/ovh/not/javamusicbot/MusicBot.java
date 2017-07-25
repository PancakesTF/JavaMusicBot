package ovh.not.javamusicbot;

import com.google.gson.Gson;
import com.mashape.unirest.http.Unirest;
import com.moandjiezana.toml.Toml;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClients;

import java.io.File;

public final class MusicBot {
    private static final String CONFIG_PATH = "config.toml";
    private static final String CONSTANTS_PATH = "constants.toml";
    public static final String USER_AGENT = "JavaMusicBot (https://github.com/sponges/JavaMusicBot)";
    public static final Gson GSON = new Gson();

    private static ConfigLoadResult configs = null;

    public static void main(String[] args) {
        RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
        HttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
        Unirest.setHttpClient(httpClient);
        if (args.length == 0) {
            new ShardManager();
            return;
        }
        int shardCount = Integer.parseInt(args[0]);
        int minShard = Integer.parseInt(args[1]);
        int maxShard = Integer.parseInt(args[2]);
        new ShardManager(shardCount, minShard, maxShard);
    }

    public static ConfigLoadResult getConfigs() {
        if (configs == null) {
            Config config = new Toml().read(new File(CONFIG_PATH)).to(Config.class);
            Constants constants = new Toml().read(new File(CONSTANTS_PATH)).to(Constants.class);
            configs = new ConfigLoadResult(config, constants);
        }
        return configs;
    }

    public static ConfigLoadResult reloadConfigs() {
        configs = null;
        return getConfigs();
    }

    public static class ConfigLoadResult {
        public Config config;
        public Constants constants;

        ConfigLoadResult(Config config, Constants constants) {
            this.config = config;
            this.constants = constants;
        }
    }
}
