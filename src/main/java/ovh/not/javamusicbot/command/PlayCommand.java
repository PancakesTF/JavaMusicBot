package ovh.not.javamusicbot.command;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import ovh.not.javamusicbot.CommandManager;

public class PlayCommand extends BasePlayCommand {
    public PlayCommand(CommandManager commandManager, AudioPlayerManager playerManager) {
        super(commandManager, playerManager, "play", "p");
    }

    @Override
    protected String noArgumentMessage() {
        return "Usage: `{{prefix}}play <link>` - plays a song\n" +
                "To search YouTube, use `{{prefix}}play <youtube video title>`\n" +
                "To search SoundCloud, use `{{prefix}}soundcloud <soundcloud song name>`\n" +
                "To add as first in queue, use `{{prefix}}play <link> -first`";
    }
}
