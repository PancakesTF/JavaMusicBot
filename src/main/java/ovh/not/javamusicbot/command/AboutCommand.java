package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.Config;
import ovh.not.javamusicbot.MusicBot;

public class AboutCommand extends Command {
    private final String about;

    public AboutCommand() {
        super("about", "info", "support");
        this.about = MusicBot.getConfigs().config.about;
    }

    @Override
    public void on(Context context) {
        context.reply(about);
    }
}
