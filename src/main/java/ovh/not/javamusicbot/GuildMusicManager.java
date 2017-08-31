package ovh.not.javamusicbot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.exceptions.PermissionException;

import java.util.HashMap;
import java.util.Map;

import static ovh.not.javamusicbot.Utils.getPrivateChannel;

public class GuildMusicManager {

    private static final Map<Guild, GuildMusicManager> GUILDS = new HashMap<>();
    private final Guild guild;
    private final AudioPlayer player;
    private final TrackScheduler scheduler;
    private final AudioPlayerSendHandler sendHandler;
    private boolean open = false;
    private VoiceChannel channel = null;

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
        return channel;
    }

    public void setChannel(VoiceChannel channel) {
        this.channel = channel;
    }


    public void open(VoiceChannel channel, User user) {
        try {
            final Member self = guild.getSelfMember();
            if (!self.hasPermission(channel, Permission.VOICE_CONNECT))
                throw new PermissionException(Permission.VOICE_CONNECT.getName());
            guild.getAudioManager().openAudioConnection(channel);
            guild.getAudioManager().setSelfDeafened(true);
            this.channel = channel;
            open = true;
        } catch (PermissionException e) {
            if (user != null && !user.isBot()) {
                getPrivateChannel(user).sendMessage("**dabBot does not have permission to connect to the "
                        + channel.getName() + " voice channel.**\nTo fix this, allow dabBot to `Connect` " +
                        "and `Speak` in that voice channel.\nIf you are not the guild owner, please send " +
                        "this to them.").complete();
            } else {
                e.printStackTrace();
            }
        }
    }

    public void close() {
        guild.getAudioManager().closeAudioConnection();
        this.channel = null;
        open = false;
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
