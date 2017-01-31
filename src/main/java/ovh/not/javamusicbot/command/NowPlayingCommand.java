package ovh.not.javamusicbot.command;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.GuildMusicManager;

import static ovh.not.javamusicbot.Utils.formatDuration;

public class NowPlayingCommand extends Command {
    private static final String NOW_PLAYING_FORMAT = "Currently playing **%s** by **%s** `[%s/%s]`";

    public NowPlayingCommand() {
        super("nowplaying", "current", "now", "np");
    }

    @Override
    public void on(Context context) {
        GuildMusicManager musicManager = GuildMusicManager.get(context.event.getGuild());
        if (musicManager == null || musicManager.player.getPlayingTrack() == null) {
            context.reply("No music is playing on this guild!");
            return;
        }
        AudioTrack currentTrack = musicManager.player.getPlayingTrack();
        context.reply(String.format(NOW_PLAYING_FORMAT, currentTrack.getInfo().title, currentTrack.getInfo().author,
                formatDuration(currentTrack.getPosition()), formatDuration(currentTrack.getDuration())));
    }
}
