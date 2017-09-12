package ovh.not.javamusicbot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.io.MessageInput;
import com.sedmelluq.discord.lavaplayer.tools.io.MessageOutput;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.DecodedTrackHolder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public abstract class Utils {
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
        DecodedTrackHolder holder;
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
            if (stringArrayContains(config.owners, member.getUser().getId())) {
                return true;
            }

            if (!member.isOwner() && !member.hasPermission(Permission.ADMINISTRATOR)) {
                continue;
            }

            Member dabbotMember = dabbotGuild.getMember(member.getUser());
            if (dabbotMember == null) {
                continue;
            }

            for (Role dabbotRole : dabbotMember.getRoles()) {
                if (dabbotRole == role) {
                    return true;
                }
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
}
