package ovh.not.javamusicbot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.PermissionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

import static ovh.not.javamusicbot.Utils.getPrivateChannel;

public class GuildMusicManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(GuildMusicManager.class);

    private static final Map<Guild, GuildMusicManager> GUILDS = new ConcurrentHashMap<>();
    private final Guild guild;
    private final AudioPlayer player;
    private final TrackScheduler scheduler;
    private final AudioPlayerSendHandler sendHandler;
    private volatile boolean open = false;
    private Optional<VoiceChannel> channel = Optional.empty();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private GuildMusicManager(Guild guild, TextChannel textChannel, AudioPlayerManager playerManager) {
        this.guild = guild;
        this.player = playerManager.createPlayer();
        this.scheduler = new TrackScheduler(this, player, textChannel);
        this.player.addListener(scheduler);
        this.sendHandler = new AudioPlayerSendHandler(player);
        this.guild.getAudioManager().setSendingHandler(sendHandler);
    }

    public static Map<Guild, GuildMusicManager> getGUILDS() {
        return GUILDS;
    }

    public Guild getGuild() {
        return guild;
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

    // request to open/close voice connection, 2s timeout PER GUILD
    private void submitTask(Runnable runnable) {
        try {
            executorService.submit(runnable).get(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOGGER.error("Error submitting task in GuildMusicManager", e);
        }
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
                            + channel.getName() + " voice channel.**\nTo fix this, allow dabBot to `Connect` " +
                            "and `Speak` in that voice channel.\nIf you are not the guild owner, please send " +
                            "this to them.").complete();
                } else {
                    LOGGER.error("an error occured opening voice connection", e);
                }
            }
        });
    }

    public void close() {
        submitTask(() -> {
            guild.getAudioManager().closeAudioConnection();
            this.channel = null;
            open = false;
        });
    }

    public static GuildMusicManager getOrCreate(Guild guild, TextChannel textChannel, AudioPlayerManager playerManager) {
        if (GUILDS.containsKey(guild)) {
            GuildMusicManager manager = GUILDS.get(guild);
            if (manager.scheduler.getTextChannel() != textChannel) {
                manager.scheduler.setTextChannel(textChannel);
            }
            return manager;
        }
        GuildMusicManager musicManager = new GuildMusicManager(guild, textChannel, playerManager);
        GUILDS.put(guild, musicManager);
        return musicManager;
    }

    public static GuildMusicManager get(Guild guild) {
        return GUILDS.get(guild);
    }
}
