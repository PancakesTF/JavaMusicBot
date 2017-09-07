package ovh.not.javamusicbot.command;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.GuildMusicManager;

import java.util.List;

public class RemoveCommand extends Command {
    public RemoveCommand() {
        super("remove", "delete", "rm");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void on(Context context) {
        GuildMusicManager musicManager = GuildMusicManager.get(context.getEvent().getGuild());
        if (musicManager == null || musicManager.getPlayer().getPlayingTrack() == null) {
            context.reply("No music is playing on this guild! To play a song use `{{prefix}}play`");
            return;
        }
        if (context.getArgs().length < 1) {
            context.reply("Usage: `{{prefix}}remove <song position>`\nExample: `{{prefix}}remove 5` - moves song at "
                    + "position 5 in queue");
            return;
        }
        int position;
        try {
            position = Integer.parseInt(context.getArgs()[0]);
        } catch (NumberFormatException e) {
            context.reply("Invalid song position!");
            return;
        }
        List<AudioTrack> queue = (List<AudioTrack>) musicManager.getScheduler().getQueue();
        if (position > queue.size()) {
            context.reply("Invalid song position! Maximum: %d", queue.size());
            return;
        }
        int index = position - 1;
        AudioTrack track = queue.get(index);
        if (track == null) {
            context.reply("Invalid song!");
            return;
        }
        queue.remove(index);
        context.reply("Removed **%s** by **%s** at position **%d** from the queue!",
                track.getInfo().title, track.getInfo().author, position);
    }
}
