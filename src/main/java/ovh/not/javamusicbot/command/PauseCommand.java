package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.GuildMusicManager;

public class PauseCommand extends Command {
    public PauseCommand() {
        super("pause", "resume");
    }

    @Override
    public void on(Context context) {
        GuildMusicManager musicManager = GuildMusicManager.get(context.getEvent().getGuild());
        if (musicManager == null || musicManager.getPlayer().getPlayingTrack() == null) {
            context.reply("No music is playing on this guild!");
            return;
        }
        boolean action = !musicManager.getPlayer().isPaused();
        musicManager.getPlayer().setPaused(action);
        if (action) {
            context.reply("Paused music playback!");
        } else {
            context.reply("Resumed music playback!");
        }
    }
}
