package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.Config;

public class InviteCommand extends Command {
    private final String invite;

    public InviteCommand(Config config) {
        super("invite", "addbot");
        invite = config.invite;
    }

    @Override
    public void on(Context context) {
        context.reply("Invite dabBot: " + invite);
    }
}
