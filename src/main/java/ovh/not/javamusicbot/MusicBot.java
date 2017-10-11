package ovh.not.javamusicbot;

import com.google.gson.Gson;
import com.moandjiezana.toml.Toml;
import net.dv8tion.jda.bot.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.requests.SessionReconnectQueue;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.File;

public final class MusicBot {
    private static final Logger logger = LoggerFactory.getLogger(MusicBot.class);

    public static final String CONFIG_PATH = "config.toml";
    public static final String CONSTANTS_PATH = "constants.toml";
    public static final String USER_AGENT = "JavaMusicBot v1.0-BETA (https://github.com/ducc/JavaMusicBot)";
    public static final Gson GSON = new Gson();
    public static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json");
    
    private static final Object CONFIG_LOCK = new Object();

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
                logger.info("OkHttpClient: {} {}", method, uri);

                return chain.proceed(request);
            }).build();

    private static ConfigLoadResult configs = null;

    public static void main(String[] args) {
        Config config = getConfigs().config;

        DefaultShardManagerBuilder builder = new DefaultShardManagerBuilder()
                .setReconnectQueue(new SessionReconnectQueue())
                .addEventListener(new Listener())
                .setToken(config.token)
                .setAudioEnabled(true)
                .setGame(Game.of(config.game));

        if (args.length < 3) {
            builder.setShardTotal(1).setShards(0);
        } else {
            try {
                int shardTotal = Integer.parseInt(args[0]);
                int minShardId = Integer.parseInt(args[1]);
                int maxShardId = Integer.parseInt(args[2]);

                builder.setShardTotal(shardTotal).setShards(minShardId, maxShardId);
            } catch (Exception ex) {
                logger.warn("Could not instantiate with given args! Usage: <shard total> <min shard> <max shard>");
                return;
            }
        }

        // todo set reconnect ipc queue (when alpaca adds support for it)

        try {
            builder.buildAsync();
        } catch (LoginException | RateLimitedException e) {
            logger.error("error on call to ShardManager#buildBlocking", e);
        }
    }

    public static ConfigLoadResult getConfigs() {
        synchronized (CONFIG_LOCK) {
            if (configs == null) {
                Config config = new Toml().read(new File(CONFIG_PATH)).to(Config.class);
                Constants constants = new Toml().read(new File(CONSTANTS_PATH)).to(Constants.class);
                configs = new ConfigLoadResult(config, constants);
            }
            return configs;
        }
    }

    public static ConfigLoadResult reloadConfigs() {
        synchronized (CONFIG_LOCK) {
            configs = null;
            return getConfigs();
        }
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
