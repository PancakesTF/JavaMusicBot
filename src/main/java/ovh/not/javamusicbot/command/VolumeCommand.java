package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.Command;

public class VolumeCommand extends Command {
    public VolumeCommand() {
        super("volume", "v");
        hide = true;
    }

    @Override
    public void on(Context context) {
        context.reply("To change the bot volume, right click it in the voice channel and use the volume slider.");
    }
}
