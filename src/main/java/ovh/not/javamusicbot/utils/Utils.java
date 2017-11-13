package ovh.not.javamusicbot.utils;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.io.MessageInput;
import com.sedmelluq.discord.lavaplayer.tools.io.MessageOutput;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public abstract class Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    public static final String HASTEBIN_URL = "https://hastebin.com/documents";
    private static final String DURATION_FORMAT = "mm:ss";
    private static final String DURATION_FORMAT_LONG = "HH:mm:ss";

    public static String formatDuration(long duration) {
        if (duration < 0) duration = 0;
        return DurationFormatUtils.formatDuration(duration, DURATION_FORMAT);
    }

    public static String formatLongDuration(long duration) {
        if (duration < 0) duration = 0;
        return DurationFormatUtils.formatDuration(duration, DURATION_FORMAT_LONG);
    }

    public static String formatTrackDuration(AudioTrack audioTrack) {
        return (audioTrack.isSeekable() || audioTrack.getInfo().isStream)
                ? formatDuration(audioTrack.getDuration()) : "\u221E"; // ∞
    }

    public static PrivateChannel getPrivateChannel(User user) {
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

    /*private static Optional<WebhookClient> webhookClient = Optional.empty();

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
    }*/
}
