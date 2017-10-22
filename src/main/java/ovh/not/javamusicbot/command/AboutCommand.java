package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.MusicBot;

public class AboutCommand extends Command {
    public AboutCommand() {
        super("about", "info", "support");
    }

    @Override
    public void on(Context context) {
        context.reply(MusicBot.getConfigs().config.about);
    }
}
