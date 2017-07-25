package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.Config;
import ovh.not.javamusicbot.MusicBot;

public class InviteCommand extends Command {
    public InviteCommand() {
        super("invite", "addbot");
    }

    @Override
    public void on(Context context) {
        context.reply("Invite dabBot: " + MusicBot.getConfigs().config.invite);
    }
}
