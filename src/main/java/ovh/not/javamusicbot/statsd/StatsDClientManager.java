package ovh.not.javamusicbot.statsd;

import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import net.dv8tion.jda.core.JDA;
import ovh.not.javamusicbot.Config;
import ovh.not.javamusicbot.MusicBot;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class StatsDClientManager {

    private final MusicBot bot;

    private final Map<Integer, Optional<StatsDClient>> statsDClients = new ConcurrentHashMap<>();

    public StatsDClientManager(MusicBot bot) {
        this.bot = bot;
    }

    public Optional<StatsDClient> getStatsDClient(JDA jda) {
        int shardId = jda.getShardInfo().getShardId();

        return statsDClients.computeIfAbsent(shardId, $ -> {
            Config config = this.bot.getConfigs().config;
            Optional<StatsDClient> client;
            if (config.statsDHost == null || config.statsDHost.length() == 0) {
                client = Optional.empty();
            } else {
                String account = jda.getSelfUser().getName().toLowerCase().replace(" ", "_");

                client = Optional.of(new NonBlockingStatsDClient(
                        "dabbot", // prefix
                        config.statsDHost, // statsd host
                        config.statsDPort,  // statsd port

                        // constant tags applied to each update
                        "account:" + account,
                        "shard:" + shardId
                ));
            }

            statsDClients.put(shardId, client);
            return client;
        });
    }
}
