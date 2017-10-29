package ovh.not.javamusicbot.command;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import ovh.not.javamusicbot.CommandManager;
import ovh.not.javamusicbot.MusicBot;

public class SoundCloudCommand extends BasePlayCommand {
    public SoundCloudCommand(MusicBot bot, CommandManager commandManager, AudioPlayerManager playerManager) {
        super(bot, commandManager, playerManager, "soundcloud", "sc");
        // so that when the LoadResultHandler fails it doesn't try to search on youtube :ok_hand:
        this.allowSearch = false;
        this.isSearch = true;
    }

    @Override
    protected String noArgumentMessage() {
        return "Usage: `{{prefix}}soundcloud <song title>` - searches for a song from soundcloud\n\n" +
                "If you already have a link to a song, use `{{prefix}}play <link>`";
    }

    @Override
    protected String[] transformQuery(String[] args) {
        args[0] = "scsearch:" + args[0];
        return args;
    }
}
