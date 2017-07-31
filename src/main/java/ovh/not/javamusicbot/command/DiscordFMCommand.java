package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.*;

@SuppressWarnings("ConstantConditions")
public class DiscordFMCommand extends Command {

    public DiscordFMCommand() {
        super("discordfm", "dfm");
    }

    @Override
    public void on(Context context) {
        context.reply("This command has been disabled because discord.fm has shutdown.");
    }
}
