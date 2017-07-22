package ovh.not.javamusicbot.command;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import ovh.not.javamusicbot.CommandManager;

public class SoundcloudCommand extends BasePlayCommand {
    public SoundcloudCommand(CommandManager commandManager, AudioPlayerManager playerManager) {
        super(commandManager, playerManager, "soundcloud", "sc");
    }

    String noArgumentMessage() {
        return "Usage: `%prefix%soundcloud <song title>` - searches for a song from soundcloud\n\n" +
                "If you already have a link to a song, use `%prefix%play <link>`";
    }

    String[] transformQuery(String[] args) {
        args[0] = "scsearch:" + args[0];
        return args;
    }
}
