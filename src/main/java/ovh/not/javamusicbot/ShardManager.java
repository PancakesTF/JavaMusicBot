package ovh.not.javamusicbot;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

import javax.security.auth.login.LoginException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class ShardManager {
    private static final String LIAM_SYSTEMS_STATS_URL = "http://bots.liam.systems/stats";
    public final Shard[] shards;

    ShardManager(Config config, Constants constants) {
        shards = new Shard[1];
        shards[0] = new Shard(this, config, constants);
    }

    ShardManager(Config config, Constants constants, int shardCount, int minShard, int maxShard) {
        shards = new Shard[(maxShard - minShard) + 1];
        int index = 0;
        for (int shardId = minShard; shardId < maxShard + 1;) {
            System.out.println("Starting shard " + shardId + "...");
            Shard shard = new Shard(this, config, constants, shardId, shardCount);
            shards[index] = shard;
            shardId++;
            index++;
        }
    }

    public class Shard {
        public final ShardManager manager;
        private final Config config;
        private final Constants constants;
        private final boolean sharding;
        public int id = 0;
        public int shardCount = 0;
        public JDA jda = null;

        private Shard(ShardManager manager, Config config, Constants constants) {
            this.manager = manager;
            this.config = config;
            this.constants = constants;
            this.sharding = false;
            create();
            start();
        }

        private Shard(ShardManager manager, Config config, Constants constants, int shard, int shardCount) {
            this.manager = manager;
            this.config = config;
            this.constants = constants;
            this.sharding = true;
            this.id = shard;
            this.shardCount = shardCount;
            create();
            start();
        }

        private void create() {
            CommandManager commandManager = new CommandManager(config, constants, this);
            JDABuilder builder = new JDABuilder(AccountType.BOT)
                    .setToken(config.token)
                    .addListener(new Listener(config, commandManager));
            if (sharding) {
                builder.useSharding(id, shardCount);
            }
            try {
                jda = builder.buildBlocking();
                jda.getPresence().setGame(Game.of(config.game));
            } catch (LoginException | InterruptedException | RateLimitedException e) {
                e.printStackTrace();
            }
        }

        private void start() {
            if (!config.dev && config.liamSystems != null && config.liamSystems.length() > 0) {
                new Timer("", true).scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        int members = 0;
                        int connections = 0;
                        for (Guild guild : jda.getGuilds()) {
                            members += guild.getMembers().size();
                            if (guild.getAudioManager().isConnected()) {
                                connections++;
                            }
                        }
                        Unirest.post(LIAM_SYSTEMS_STATS_URL)
                                .header("Content-Type", "application/x-www-form-urlencoded")
                                .header("User-Agent", MusicBot.USER_AGENT)
                                .field("secret_key", config.liamSystems)
                                .field("shard_name", id)
                                .field("voice_connections", connections)
                                .field("total_guilds", jda.getGuilds().size())
                                .field("total_members", members)
                                .field("messages_seen", 0)
                                .field("messages_sent", 0)
                                .asJsonAsync(new Callback<JsonNode>() {
                                    @Override
                                    public void completed(HttpResponse<JsonNode> httpResponse) {
                                        try {
                                            if (!httpResponse.getBody().getObject().getString("reply").equals("success")) {
                                                System.out.println("liam systems stats request failed!");
                                            }
                                        } catch (Exception ignored) {
                                        }
                                    }

                                    @Override
                                    public void failed(UnirestException e) {
                                        e.printStackTrace();
                                    }

                                    @Override
                                    public void cancelled() {
                                    }
                                });
                    }
                }, 1000, TimeUnit.MINUTES.toMillis(10));
            }
        }

        public void restart() {
            System.out.println("Shutting down shard " + id + "...");
            jda.shutdown(false);
            System.out.println("Restarting shard " + id + "...");
            create();
            System.out.println("Shard " + id + " restarted!");
        }
    }
}
