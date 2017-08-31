package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.GuildMusicManager;

public class RepeatCommand extends Command {
    public RepeatCommand() {
        super("repeat");
    }

    @Override
    public void on(Context context) {
        GuildMusicManager musicManager = GuildMusicManager.get(context.getEvent().getGuild());
        if (musicManager == null || musicManager.getPlayer().getPlayingTrack() == null) {
            context.reply("No music is playing on this guild!");
            return;
        }
        boolean repeat = !musicManager.getScheduler().isRepeat();
        musicManager.getScheduler().setRepeat(repeat);
        context.reply("**" + (repeat ? "Enabled" : "Disabled") + "** song repeating!");
    }
}
