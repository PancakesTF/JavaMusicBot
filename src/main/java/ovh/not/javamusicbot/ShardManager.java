package ovh.not.javamusicbot;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

import javax.security.auth.login.LoginException;

public class ShardManager {
    public final Shard[] shards;
    public UserManager userManager = null;

    ShardManager() {
        shards = new Shard[1];
        shards[0] = new Shard(this);
        if (MusicBot.getConfigs().config.patreon) {
            userManager = new UserManager(this);
        }
    }

    ShardManager(int shardCount, int minShard, int maxShard) {
        shards = new Shard[(maxShard - minShard) + 1];
        for (int shardId = minShard, index = 0; shardId < maxShard + 1; shardId++, index++) {
            System.out.println("Starting shard " + shardId + "...");
            Shard shard = new Shard(this, shardId, shardCount);
            shards[index] = shard;
        }
        if (MusicBot.getConfigs().config.patreon) {
            userManager = new UserManager(this);
        }
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
        private final boolean sharding;
        public int id = 0;
        public int shardCount = 0;
        public JDA jda = null;

        private Shard(ShardManager manager) {
            this.manager = manager;
            this.sharding = false;
            create();
        }

        private Shard(ShardManager manager, int shard, int shardCount) {
            this.manager = manager;
            this.sharding = true;
            this.id = shard;
            this.shardCount = shardCount;
            create();
        }

        private void create() {
            CommandManager commandManager = new CommandManager(this);
            MusicBot.ConfigLoadResult configs = MusicBot.getConfigs();
            JDABuilder builder = new JDABuilder(AccountType.BOT)
                    .setToken(configs.config.token)
                    .addEventListener(new Listener(commandManager, this));
            if (sharding) {
                builder.useSharding(id, shardCount);
            }
            try {
                jda = builder.buildBlocking();
                jda.getPresence().setGame(Game.of(configs.config.game));
            } catch (LoginException | InterruptedException | RateLimitedException e) {
                e.printStackTrace();
            }
        }

        public void restart() {
            System.out.println("Shutting down shard " + id + "...");
            jda.shutdown();
            System.out.println("Restarting shard " + id + "...");
            create();
            System.out.println("Shard " + id + " restarted!");
        }
    }
}
