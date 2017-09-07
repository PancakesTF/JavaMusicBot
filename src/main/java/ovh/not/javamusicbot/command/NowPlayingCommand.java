package ovh.not.javamusicbot.command;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.GuildMusicManager;

import static ovh.not.javamusicbot.Utils.formatDuration;
import static ovh.not.javamusicbot.Utils.formatTrackDuration;

public class NowPlayingCommand extends Command {
    private static final String NOW_PLAYING_FORMAT = "Currently playing **%s** by **%s** `[%s/%s]`\nSong URL: %s";

    public NowPlayingCommand() {
        super("nowplaying", "current", "now", "np");
    }

    @Override
    public void on(Context context) {
        GuildMusicManager musicManager = GuildMusicManager.get(context.getEvent().getGuild());
        if (musicManager == null || musicManager.getPlayer().getPlayingTrack() == null) {
            context.reply("No music is playing on this guild! To play a song use `{{prefix}}play`");
            return;
        }
        AudioTrack currentTrack = musicManager.getPlayer().getPlayingTrack();
        context.reply(NOW_PLAYING_FORMAT, currentTrack.getInfo().title, currentTrack.getInfo().author,
                formatDuration(currentTrack.getPosition()), formatTrackDuration(currentTrack),
                currentTrack.getInfo().uri);
    }
}
