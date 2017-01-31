package ovh.not.javamusicbot;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Listener extends ListenerAdapter {
    private static final String CARBON_DATA_URL = "https://www.carbonitex.net/discord/data/botdata.php";
    private static final String DBOTS_STATS_URL = "https://bots.discord.pw/api/bots/%s/stats";
    private final Config config;
    private final CommandManager commandManager;
    private final Pattern commandPattern;

    Listener(Config config, CommandManager commandManager) {
        this.config = config;
        this.commandManager = commandManager;
        this.commandPattern = Pattern.compile(config.regex);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        User author = event.getAuthor();
        if (author.isBot() || author.getId().equalsIgnoreCase(event.getJDA().getSelfUser().getId())) {
            return;
        }
        String content = event.getMessage().getContent();
        System.out.println(String.format("[%s] %s: %s", event.getGuild().getName(), author.getName(), content));
        Matcher matcher = commandPattern.matcher(content.replace("\r", " ").replace("\n", " "));
        if (!matcher.find()) {
            return;
        }
        String name = matcher.group(1).toLowerCase();
        Command command = commandManager.getCommand(name);
        if (command == null) {
            return;
        }
        Command.Context context = command.new Context();
        context.event = event;
        if (matcher.groupCount() > 1) {
            String[] matches = matcher.group(2).split("\\s+");
            if (matches.length > 0 && matches[0].equals("")) {
                matches = new String[0];
            }
            context.args = matches;
        }
        command.on(context);
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        int guilds = event.getJDA().getGuilds().size();
        System.out.println(String.format("Joined guild: %s - #%d", event.getGuild().getName(), guilds));
        TextChannel publicChannel = event.getGuild().getPublicChannel();
        if (publicChannel != null && publicChannel.canTalk()) {
            publicChannel.sendMessage(config.join).complete();
        }
        if (config.dev) {
            return;
        }
        JDA.ShardInfo shardInfo = event.getJDA().getShardInfo();
        int shardCount = shardInfo.getShardTotal();
        int shardId = shardInfo.getShardId();
        try {
            if (config.carbon != null && config.carbon.length() > 0) {
                Unirest.post(CARBON_DATA_URL)
                        .header("Content-Type", "application/json")
                        .header("User-Agent", MusicBot.USER_AGENT)
                        .body(new JSONObject()
                                .put("key", config.carbon)
                                .put("servercount", guilds)
                                .put("shardcount", shardCount)
                                .put("shardid", shardId))
                        .asString();
            }
            if (config.dbots != null && config.dbots.length() > 0) {
                Unirest.post(String.format(DBOTS_STATS_URL, event.getJDA().getSelfUser().getId()))
                        .header("Content-Type", "application/json")
                        .header("User-Agent", MusicBot.USER_AGENT)
                        .header("Authorization", config.dbots)
                        .body(new JSONObject()
                                .put("server_count", guilds)
                                .put("shard_count", shardCount)
                                .put("shard_id", shardId))
                        .asString();
            }
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        if (GuildMusicManager.GUILDS.containsKey(event.getGuild())) {
            GuildMusicManager musicManager = GuildMusicManager.GUILDS.remove(event.getGuild());
            musicManager.player.stopTrack();
            musicManager.scheduler.queue.clear();
            musicManager.close();
        }
    }
}
