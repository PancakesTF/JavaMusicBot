package ovh.not.javamusicbot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.io.MessageInput;
import com.sedmelluq.discord.lavaplayer.tools.io.MessageOutput;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.webhook.WebhookClient;
import net.dv8tion.jda.webhook.WebhookClientBuilder;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    public static final String HASTEBIN_URL = "https://hastebin.com/documents";
    private static final String DURATION_FORMAT = "mm:ss";
    private static final String DURATION_FORMAT_LONG = "HH:mm:ss";

    public static String formatDuration(long duration) {
        return DurationFormatUtils.formatDuration(duration, DURATION_FORMAT);
    }

    public static String formatLongDuration(long duration) {
        return DurationFormatUtils.formatDuration(duration, DURATION_FORMAT_LONG);
    }

    public static String formatTrackDuration(AudioTrack audioTrack) {
        return (audioTrack.isSeekable() || audioTrack.getInfo().isStream)
                ? formatDuration(audioTrack.getDuration()) : "\u221E"; // ∞
    }

    static PrivateChannel getPrivateChannel(User user) {
        return user.openPrivateChannel().complete();
    }

    public static String encode(AudioPlayerManager playerManager, AudioTrack track) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        playerManager.encodeTrack(new MessageOutput(stream), track);
        byte[] encoded = Base64.getEncoder().encode(stream.toByteArray());
        return new String(encoded);
    }

    public static AudioTrack decode(AudioPlayerManager playerManager, String encoded) throws IOException {
        byte[] bytes = Base64.getDecoder().decode(encoded.getBytes());
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        return playerManager.decodeTrack(new MessageInput(stream)).decodedTrack;
    }

    public static boolean stringArrayContains(String[] array, String element) {
        for (String s : array) {
            if (s.equals(element)) {
                return true;
            }
        }
        return false;
    }

    private static boolean allowedPatronAccess(Guild guild, Role role) {
        Config config = MusicBot.getConfigs().config;

        Guild dabbotGuild = guild.getJDA().asBot().getShardManager().getGuildById(config.discordServer);

        for (Member member : guild.getMembers()) {
            if (config.owners.contains(member.getUser().getId())) {
                return true;
            }

            if (!member.isOwner() && !member.hasPermission(Permission.ADMINISTRATOR)) {
                continue;
            }

            Member dabbotMember = dabbotGuild.getMember(member.getUser());
            if (dabbotMember == null) {
                continue;
            }

            if (dabbotMember.getRoles().contains(role)) {
                return true;
            }
        }

        return false;
    }

    public static boolean allowedSupporterPatronAccess(Guild guild) {
        Config config = MusicBot.getConfigs().config;

        Guild dabbotGuild = guild.getJDA().asBot().getShardManager().getGuildById(config.discordServer);
        Role supporterRole = dabbotGuild.getRoleById(config.supporterRole);

        return allowedPatronAccess(guild, supporterRole);
    }

    public static boolean allowedSuperSupporterPatronAccess(Guild guild) {
        Config config = MusicBot.getConfigs().config;

        Guild dabbotGuild = guild.getJDA().asBot().getShardManager().getGuildById(config.discordServer);
        Role superSupporterRole = dabbotGuild.getRoleById(config.superSupporterRole);

        return allowedPatronAccess(guild, superSupporterRole);
    }

    public static boolean allowedSuperSupporterPatronAccess(User user) {
        Config config = MusicBot.getConfigs().config;

        Guild dabbotGuild = user.getJDA().asBot().getShardManager().getGuildById(config.discordServer);
        Role superSupporterRole = dabbotGuild.getRoleById(config.superSupporterRole);
        Member dabbotMember = dabbotGuild.getMember(user);

        return dabbotMember != null && dabbotMember.getRoles().contains(superSupporterRole);
    }

    private static Optional<WebhookClient> webhookClient = Optional.empty();

    public static Optional<WebhookClient> getWebhookClient() {
        if (!webhookClient.isPresent()) {
            String webhookUrl = MusicBot.getConfigs().config.statusWebhook;
            if (webhookUrl == null) {
                return Optional.empty();
            }

            Pattern pattern = Pattern.compile("webhooks/(\\d+)/(.+)$");
            Matcher matcher = pattern.matcher(webhookUrl);

            if (!matcher.find()) {
                LOGGER.error("invalid webhook, could not be matched by regex");
                return Optional.empty();
            }

            long id = Long.parseLong(matcher.group(1));
            String token = matcher.group(2);

            webhookClient = Optional.of(new WebhookClientBuilder(id, token).build());
        }

        return webhookClient;
    }

    private static final Map<Integer, Optional<StatsDClient>> statsDClients = new HashMap<>();
    private static final Object statsDLock = new Object();

    public static Optional<StatsDClient> getStatsDClient(JDA jda) {
        int shardId = jda.getShardInfo().getShardId();

        Optional<StatsDClient> client = statsDClients.get(shardId);

        if (client == null) {
            Config config = MusicBot.getConfigs().config;

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
        }

        return client;
    }
}
