package ovh.not.javamusicbot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.PermissionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ovh.not.javamusicbot.MusicBot;
import ovh.not.javamusicbot.TrackScheduler;

import java.util.Optional;

import static ovh.not.javamusicbot.utils.Utils.getPrivateChannel;

public class GuildAudioController {
    private static final Logger LOGGER = LoggerFactory.getLogger(GuildAudioController.class);

    private final Guild guild;
    private final AudioPlayerManager playerManager;
    private final AudioPlayer player;
    private final TrackScheduler scheduler;
    private final AudioPlayerSendHandler sendHandler;
    private final MusicBot bot;
    private volatile boolean open = false;
    private Optional<VoiceChannel> channel = Optional.empty();

    GuildAudioController(MusicBot bot, Guild guild, TextChannel textChannel, AudioPlayerManager playerManager) {
        this.bot = bot;
        this.guild = guild;
        this.playerManager = playerManager;
        this.player = playerManager.createPlayer();
        this.scheduler = new TrackScheduler(this, player, textChannel);
        this.player.addListener(scheduler);
        this.sendHandler = new AudioPlayerSendHandler(player);
        this.guild.getAudioManager().setSendingHandler(sendHandler);
    }

    public Guild getGuild() {
        return guild;
    }

    public AudioPlayerManager getPlayerManager() {
        return playerManager;
    }

    public AudioPlayer getPlayer() {
        return player;
    }

    public TrackScheduler getScheduler() {
        return scheduler;
    }

    public AudioPlayerSendHandler getSendHandler() {
        return sendHandler;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public VoiceChannel getChannel() {
        return channel.get();
    }

    public void setChannel(VoiceChannel channel) {
        this.channel = Optional.of(channel);
    }

    private void submitTask(Runnable runnable) {
        new Thread(runnable).start();
    }

    public void open(VoiceChannel channel, User user) {
        submitTask(() -> {
            try {
                final Member self = guild.getSelfMember();

                if (!self.hasPermission(channel, Permission.VOICE_CONNECT))
                    throw new PermissionException(Permission.VOICE_CONNECT.getName());

                guild.getAudioManager().openAudioConnection(channel);
                guild.getAudioManager().setSelfDeafened(true);

                this.channel = Optional.of(channel);
                open = true;
            } catch (PermissionException e) {
                if (user != null && !user.isBot()) {
                    getPrivateChannel(user).sendMessage("**dabBot does not have permission to connect to the "
                            + channel.getName() + " voice channel.**\nTo fix this, allow dabBot to `View Channel`, " +
                            "`Connect` and `Speak` in that voice channel.\nIf you are not the guild owner, please send " +
                            "this to them.").complete();
                } else {
                    LOGGER.error("an error occured opening voice connection", e);
                }
            }
        });

        this.bot.getStatsDClientManager().getStatsDClient(guild.getJDA())
                .ifPresent(statsd -> statsd.incrementCounter("voicechannels"));
    }

    public void close() {
        submitTask(() -> {
            guild.getAudioManager().closeAudioConnection();
            this.channel = null;
            open = false;
        });

        this.bot.getStatsDClientManager().getStatsDClient(guild.getJDA())
                .ifPresent(statsd -> statsd.decrementCounter("voicechannels"));
    }
}
