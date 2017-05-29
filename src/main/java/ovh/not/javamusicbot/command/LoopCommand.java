package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.GuildMusicManager;

public class LoopCommand extends Command {
    public LoopCommand() {
        super("loop");
    }

    @Override
    public void on(Context context) {
        GuildMusicManager musicManager = GuildMusicManager.get(context.event.getGuild());
        if (musicManager == null || musicManager.player.getPlayingTrack() == null) {
            context.reply("No music is playing on this guild!");
            return;
        }
        boolean loop = !musicManager.scheduler.loop;
        musicManager.scheduler.loop = loop;
        context.reply("**" + (loop ? "Enabled" : "Disabled") + "** queue looping!");
    }
}
