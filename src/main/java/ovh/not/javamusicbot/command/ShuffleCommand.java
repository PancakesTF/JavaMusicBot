package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.GuildMusicManager;

import java.util.Collections;
import java.util.List;

public class ShuffleCommand extends Command {
    public ShuffleCommand() {
        super("shuffle");
    }

    @Override
    public void on(Context context) {
        GuildMusicManager musicManager = GuildMusicManager.get(context.event.getGuild());
        if (musicManager == null || musicManager.player.getPlayingTrack() == null) {
            context.reply("No music is playing on this guild!");
            return;
        }
        Collections.shuffle((List<?>) musicManager.scheduler.queue);
        context.reply("Queue shuffled!");
    }
}
