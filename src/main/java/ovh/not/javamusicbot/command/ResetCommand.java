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
        if (GuildMusicManager.getGUILDS().containsKey(context.getEvent().getGuild())) {
            GuildMusicManager musicManager = GuildMusicManager.getGUILDS().remove(context.getEvent().getGuild());
            musicManager.getPlayer().stopTrack();
            musicManager.getScheduler().getQueue().clear();
            musicManager.close();
            context.reply("Reset GuildMusicManager!");
        } else {
            context.reply("This guild does not have a GuildMusicManager.");
        }
    }
}
