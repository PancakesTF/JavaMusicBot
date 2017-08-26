package ovh.not.javamusicbot;

import com.google.gson.Gson;
import com.moandjiezana.toml.Toml;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.File;

public final class MusicBot {
    private static final String CONFIG_PATH = "config.toml";
    private static final String CONSTANTS_PATH = "constants.toml";
    public static final String USER_AGENT = "JavaMusicBot (https://github.com/sponges/JavaMusicBot)";
    public static final Gson GSON = new Gson();
    public static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json");

    public static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .addInterceptor(chain -> {
                // copying the request to a builder
                Request.Builder builder = chain.request().newBuilder();

                // adding user agent header
                builder.addHeader("User-Agent", MusicBot.USER_AGENT);

                // building the new request
                Request request = builder.build();

                // logging
                String method = request.method();
                String uri = request.url().uri().toString();
                System.out.printf("Sending request: %s %s\n", method, uri);

                return chain.proceed(request);
            }).build();

    private static ConfigLoadResult configs = null;

    public static void main(String[] args) {
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
