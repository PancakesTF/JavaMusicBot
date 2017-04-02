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
    public static final Map<Guild, GuildMusicManager> GUILDS = new HashMap<>();
    private final Guild guild;
    public final AudioPlayer player;
    public final TrackScheduler scheduler;
    private final AudioPlayerSendHandler sendHandler;
    public boolean open = false;
    public VoiceChannel channel = null;

    private GuildMusicManager(Guild guild, TextChannel textChannel, AudioPlayerManager playerManager) {
        this.guild = guild;
        this.player = playerManager.createPlayer();
        this.scheduler = new TrackScheduler(this, player, textChannel);
        this.player.addListener(scheduler);
        this.sendHandler = new AudioPlayerSendHandler(player);
        this.guild.getAudioManager().setSendingHandler(sendHandler);
    }

    public void open(VoiceChannel channel, User user) {
        try {
            final Member self = guild.getSelfMember();
            if (!self.hasPermission(channel, Permission.VOICE_CONNECT))
                throw new PermissionException(Permission.VOICE_CONNECT);
            final int userLimit = channel.getUserLimit(); // userLimit is 0 if no limit is set!
            if (!self.hasPermission(channel, Permission.MANAGE_CHANNEL) && userLimit > 0 && userLimit <= channel.getMembers().size())
                throw new PermissionException(Permission.MANAGE_CHANNEL,
                        "Unable to connect to VoiceChannel due to userlimit! Requires permission MANAGE_CHANNEL to bypass");
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
            if (manager.scheduler.textChannel != textChannel) {
                manager.scheduler.textChannel = textChannel;
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
