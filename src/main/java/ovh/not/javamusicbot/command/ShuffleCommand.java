package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.audio.GuildAudioController;
import ovh.not.javamusicbot.MusicBot;

import java.util.Collections;
import java.util.List;

public class ShuffleCommand extends Command {
    public ShuffleCommand(MusicBot bot) {
        super(bot, "shuffle");
    }

    @Override
    public void on(Context context) {
        GuildAudioController musicManager = this.bot.getGuildsManager().get(context.getEvent().getGuild());
        if (musicManager == null || musicManager.getPlayer().getPlayingTrack() == null) {
            context.reply("No music is playing on this guild! To play a song use `{{prefix}}play`");
            return;
        }

        Collections.shuffle((List<?>) musicManager.getScheduler().getQueue());
        context.reply("Queue shuffled!");
    }
}
