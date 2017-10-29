package ovh.not.javamusicbot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import ovh.not.javamusicbot.MusicBot;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GuildAudioManager {
    private final MusicBot bot;
    
    private final Map<Guild, GuildAudioController> guilds = new ConcurrentHashMap<>();

    public GuildAudioManager(MusicBot bot) {
        this.bot = bot;
    }

    public GuildAudioController getOrCreate(Guild guild, TextChannel textChannel, AudioPlayerManager playerManager) {
        GuildAudioController manager = guilds.computeIfAbsent(guild, $ -> new GuildAudioController(bot, guild, textChannel, playerManager));
        manager.getScheduler().setTextChannel(textChannel);
        return manager;
    }

    public GuildAudioController get(Guild guild) {
        return guilds.get(guild);
    }

    public GuildAudioController remove(Guild guild) {
        return this.guilds.remove(guild);
    }

}