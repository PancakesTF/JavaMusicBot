package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.MusicBot;

public class AboutCommand extends Command {
    public AboutCommand(MusicBot bot) {
        super(bot, "about", "info", "support");
    }

    @Override
    public void on(Context context) {
        context.reply(this.bot.getConfigs().config.about);
    }
}
