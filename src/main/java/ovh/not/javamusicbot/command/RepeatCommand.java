package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.GuildMusicManager;

public class RepeatCommand extends Command {
    public RepeatCommand() {
        super("repeat", "loop");
    }

    @Override
    public void on(Context context) {
        GuildMusicManager musicManager = GuildMusicManager.get(context.event.getGuild());
        if (musicManager == null || musicManager.player.getPlayingTrack() == null) {
            context.reply("No music is playing on this guild!");
            return;
        }
        boolean repeat = !musicManager.scheduler.repeat;
        musicManager.scheduler.repeat = repeat;
        context.reply("**" + (repeat ? "Enabled" : "Disabled") + "** song repeating!");
    }
}
