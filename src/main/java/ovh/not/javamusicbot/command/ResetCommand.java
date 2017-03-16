package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.GuildMusicManager;

public class ResetCommand extends Command {
    public ResetCommand() {
        super("reset", "fix", "unfuck");
        hide = true;
    }

    @Override
    public void on(Context context) {
        if (GuildMusicManager.GUILDS.containsKey(context.event.getGuild())) {
            GuildMusicManager musicManager = GuildMusicManager.GUILDS.remove(context.event.getGuild());
            musicManager.player.stopTrack();
            musicManager.scheduler.queue.clear();
            musicManager.close();
            context.reply("Reset GuildMusicManager!");
        } else {
            context.reply("This guild does not have a GuildMusicManager.");
        }
    }
}
