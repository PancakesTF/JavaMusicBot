package ovh.not.javamusicbot;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.requests.SessionReconnectQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;

public class ShardManager {
    private static final Logger logger = LoggerFactory.getLogger(ShardManager.class);

    private final Shard[] shards;
    private UserManager userManager;

    private JDABuilder createNewBuilder() {
        return new JDABuilder(AccountType.BOT)
                .setToken(MusicBot.getConfigs().config.token);
    }

    ShardManager() {
        shards = new Shard[1];

        shards[0] = new Shard(this, createNewBuilder());

        if (MusicBot.getConfigs().config.patreon) {
            userManager = new UserManager(this);
        }
    }

    ShardManager(int shardCount, int minShard, int maxShard) {
        shards = new Shard[(maxShard - minShard) + 1];

        for (int shardId = minShard, index = 0; shardId < maxShard + 1; shardId++, index++) {
            logger.info("Starting shard " + shardId + "...");

            JDABuilder builder = createNewBuilder().setReconnectQueue(new SessionReconnectQueue());
            Shard shard = new Shard(this, builder, shardId, shardCount);
            shards[index] = shard;

            try {
                Thread.sleep(5000); // stop getting ratelimited IDENTIFY op 2
            } catch (InterruptedException e) {
                logger.error("error delaying shard start", e);
            }
        }

        if (MusicBot.getConfigs().config.patreon) {
            userManager = new UserManager(this);
        }
    }


    public Shard[] getShards() {
        return shards;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }


    public Guild getGuild(String id) {
        for (Shard shard : shards) {
            Guild guild = shard.jda.getGuildById(id);
            if (guild != null) {
                return guild;
            }
        }
        return null;
    }

    public class Shard {
        public final ShardManager manager;
        private final JDABuilder builder;
        private final boolean sharding;
        public int id = 0;
        public int shardCount = 0;
        public JDA jda = null;

        private Shard(ShardManager manager, JDABuilder builder) {
            this.manager = manager;
            this.builder = builder;
            this.sharding = false;
            create();
        }

        private Shard(ShardManager manager, JDABuilder builder, int shard, int shardCount) {
            this.manager = manager;
            this.builder = builder;
            this.sharding = true;
            this.id = shard;
            this.shardCount = shardCount;
            create();
        }

        private void create() {
            CommandManager commandManager = new CommandManager(this);
            MusicBot.ConfigLoadResult configs = MusicBot.getConfigs();

            builder.addEventListener(new Listener(commandManager, this));

            if (sharding) {
                builder.useSharding(id, shardCount);
            }

            try {
                jda = builder.buildBlocking();
                jda.getPresence().setGame(Game.of(configs.config.game));
            } catch (LoginException | InterruptedException | RateLimitedException e) {
                logger.error("error building JDA", e);
            }
        }

        public void restart() {
            logger.info("Shutting down shard " + id + "...");
            jda.shutdown();
            logger.info("Restarting shard " + id + "...");
            create();
            logger.info("Shard " + id + " restarted!");
        }
    }
}
