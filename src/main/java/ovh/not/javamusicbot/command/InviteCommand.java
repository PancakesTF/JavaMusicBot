package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.MusicBot;

public class InviteCommand extends Command {
    public InviteCommand(MusicBot bot) {
        super(bot,"invite", "addbot");
    }

    @Override
    public void on(Context context) {
        context.reply("Invite dabBot: " + this.bot.getConfigs().config.invite);
    }
}
