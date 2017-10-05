package ovh.not.javamusicbot;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.StatusChangeEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.webhook.WebhookMessage;
import net.dv8tion.jda.webhook.WebhookMessageBuilder;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ovh.not.javamusicbot.MusicBot.JSON_MEDIA_TYPE;

class Listener extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(Listener.class);

    private static final String CARBON_DATA_URL = "https://www.carbonitex.net/discord/data/botdata.php";
    private static final String DBOTS_STATS_URL = "https://bots.discord.pw/api/bots/%s/stats";
    private static final String DBOTS_ORG_STATS_URL = "https://discordbots.org/api/bots/%s/stats";

    private final Pattern commandPattern = Pattern.compile(MusicBot.getConfigs().config.regex);
    private final CommandManager commandManager;

    Listener() {
        commandManager = new CommandManager();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        User author = event.getAuthor();
        if (author.isBot() || author.getId().equalsIgnoreCase(event.getJDA().getSelfUser().getId())) {
            return;
        }
        String content = event.getMessage().getContent();
        Matcher matcher = commandPattern.matcher(content.replace("\r", " ").replace("\n", " "));
        if (!matcher.find()) {
            return;
        }
        if (!event.getGuild().getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_WRITE)) {
            return;
        }
        String name = matcher.group(1).toLowerCase();
        Command command = commandManager.getCommand(name);
        if (command == null) {
            return;
        }
        Command.Context context = command.new Context();
        context.setEvent(event);
        if (matcher.groupCount() > 1) {
            String[] matches = matcher.group(2).split("\\s+");
            if (matches.length > 0 && matches[0].equals("")) {
                matches = new String[0];
            }
            context.setArgs(matches);
        }
        command.on(context);
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        JDA jda = event.getJDA();
        Guild guild = event.getGuild();
        TextChannel defaultChannel = guild.getDefaultChannel();

        long guilds = jda.getGuildCache().size();
        logger.info("Joined guild: {}", guild.getName());

        Config config = MusicBot.getConfigs().config;

        if (defaultChannel != null && defaultChannel.canTalk()) {
            defaultChannel.sendMessage(config.join).complete();
        }

        if (config.patreon) {
            if (Utils.allowedSupporterPatronAccess(guild)) {
                return;
            }

            if (defaultChannel != null && defaultChannel.canTalk()) {
                try {
                    event.getGuild().getDefaultChannel().sendMessage("**Sorry, this is the patreon only dabBot!**\nTo have this " +
                            "bot on your server, you must become a patreon at https://patreon.com/dabbot").complete();
                } catch (Exception ignored) {
                }
            }
            event.getGuild().leave().queue();
            return;
        }

        if (config.dev) {
            return;
        }

        JDA.ShardInfo shardInfo = event.getJDA().getShardInfo();
        int shardCount = shardInfo.getShardTotal();
        int shardId = shardInfo.getShardId();

        if (config.carbon != null && !config.carbon.isEmpty()) {
            RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, new JSONObject()
                    .put("key", config.carbon)
                    .put("servercount", guilds)
                    .put("shardcount", shardCount)
                    .put("shardid", shardId)
                    .toString());

            Request request = new Request.Builder()
                    .url(CARBON_DATA_URL)
                    .method("POST", body)
                    .build();

            try {
                MusicBot.HTTP_CLIENT.newCall(request).execute().close();
            } catch (IOException e) {
                logger.error("Error posting stats to carbonitex.net", e);
            }
        }

        if (config.dbots != null && !config.dbots.isEmpty()) {
            RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, new JSONObject()
                    .put("server_count", guilds)
                    .put("shard_count", shardCount)
                    .put("shard_id", shardId)
                    .toString());

            Request request = new Request.Builder()
                    .url(String.format(DBOTS_STATS_URL, event.getJDA().getSelfUser().getId()))
                    .method("POST", body)
                    .addHeader("Authorization", config.dbots)
                    .build();

            try {
                MusicBot.HTTP_CLIENT.newCall(request).execute().close();
            } catch (IOException e) {
                logger.error("Error posting stats to bots.discord.pw", e);
            }
        }

        if (config.dbotsOrg != null && !config.dbotsOrg.isEmpty()) {
            RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, new JSONObject()
                    .put("server_count", guilds)
                    .put("shard_count", shardCount)
                    .put("shard_id", shardId)
                    .toString());

            Request request = new Request.Builder()
                    .url(String.format(DBOTS_ORG_STATS_URL, event.getJDA().getSelfUser().getId()))
                    .method("POST", body)
                    .addHeader("Authorization", config.dbotsOrg)
                    .build();

            try {
                MusicBot.HTTP_CLIENT.newCall(request).execute().close();
            } catch (IOException e) {
                logger.error("Error posting stats to discordbots.org", e);
            }
        }
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        if (GuildMusicManager.getGUILDS().containsKey(event.getGuild())) {
            GuildMusicManager musicManager = GuildMusicManager.getGUILDS().remove(event.getGuild());
            musicManager.getPlayer().stopTrack();
            musicManager.getScheduler().getQueue().clear();
            musicManager.close();
        }
        event.getGuild().getAudioManager().closeAudioConnection();
    }

    @Override
    public void onStatusChange(StatusChangeEvent event) {
        switch (event.getStatus()) {
            case INITIALIZING:
            case INITIALIZED:
            case LOGGING_IN:
            case SHUTDOWN:
            case SHUTTING_DOWN:
            case LOADING_SUBSYSTEMS:
                return;
        }

        JDA.Status oldStatus = event.getOldStatus();
        JDA.Status status = event.getStatus();

        logger.info("Status changed from {} to {}", oldStatus.name(), status.name());

        Utils.getWebhookClient().ifPresent(client -> {
            Color color = Color.GREEN;

            switch (status) {
                case FAILED_TO_LOGIN:
                case DISCONNECTED:
                case ATTEMPTING_TO_RECONNECT:
                    color = Color.RED;
                    break;
                case RECONNECT_QUEUED:
                case WAITING_TO_RECONNECT:
                    color = Color.ORANGE;
                    break;
            }

            JDA jda = event.getJDA();

            if (jda.getSelfUser() == null) {
                return;
            }

            String content = String.format("%s Status changed from %s to %s",
                    jda.getShardInfo(), oldStatus.name(), status.name());

            if (status == JDA.Status.ATTEMPTING_TO_RECONNECT) {
                content = String.format("**%s**", content);
            }

            WebhookMessage message = new WebhookMessageBuilder()
                    .addEmbeds(new EmbedBuilder()
                            .setColor(color)
                            .setDescription(content)
                            .setTimestamp(new Date().toInstant())
                            .build())
                    .setUsername(jda.getSelfUser().getName())
                    .build();

            client.send(message);
        });
    }
}
