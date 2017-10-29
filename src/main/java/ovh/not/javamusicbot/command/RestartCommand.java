package ovh.not.javamusicbot.command;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.audio.GuildAudioController;
import ovh.not.javamusicbot.MusicBot;

import static ovh.not.javamusicbot.utils.Utils.formatTrackDuration;

public class RestartCommand extends Command {
    public RestartCommand(MusicBot bot) {
        super(bot, "restart");
    }

    @Override
    public void on(Context context) {
        GuildAudioController musicManager = this.bot.getGuildsManager().get(context.getEvent().getGuild());
        if (musicManager == null || musicManager.getPlayer().getPlayingTrack() == null) {
            context.reply("No music is playing on this guild! To play a song use `{{prefix}}play`");
            return;
        }

        AudioTrack currentTrack = musicManager.getPlayer().getPlayingTrack();
        currentTrack.setPosition(0);

        context.reply("Restarted **%s** by **%s** `[%s]`", currentTrack.getInfo().title,
                currentTrack.getInfo().author, formatTrackDuration(currentTrack));
    }
}
