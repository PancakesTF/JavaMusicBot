package ovh.not.javamusicbot.command;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.GuildMusicManager;
import ovh.not.javamusicbot.MusicBot;
import ovh.not.javamusicbot.Utils;

public class VolumeCommand extends Command {
    public VolumeCommand() {
        super("volume", "v");
    }

    @Override
    public void on(Context context) {
        if (!MusicBot.getConfigs().config.patreon) {
            context.reply("**The volume command is dabBot premium only!**" +
                    "\nDonate for the `Super supporter` tier on Patreon at https://patreon.com/dabbot to gain access.");
            return;
        }

        MessageReceivedEvent event = context.getEvent();
        Guild guild = event.getGuild();

        GuildMusicManager musicManager = GuildMusicManager.get(guild);
        if (musicManager == null || musicManager.getPlayer().getPlayingTrack() == null) {
            context.reply("No music is playing on this guild! To play a song use `{{prefix}}play`");
            return;
        }

        // user is not a super supporter & there is not a super supporter with admin on the server
        if (!Utils.allowedSuperSupporterPatronAccess(event.getAuthor())
                && !Utils.allowedSuperSupporterPatronAccess(guild)) {
            context.reply("**The volume command is dabBot premium only!**" +
                    "\nDonate for the `Super supporter` tier on Patreon at https://patreon.com/dabbot to gain access.");
            return;
        }

        if (context.getArgs().length == 0) {
            context.reply("Current volume: **%d**", musicManager.getPlayer().getVolume());
            return;
        }

        try {
            int newVolume = Math.max(1, Math.min(150, Integer.parseInt(context.getArgs()[0])));
            musicManager.getPlayer().setVolume(newVolume);
            context.reply("Set volume to **%d**", newVolume);
        } catch (NumberFormatException e) {
            context.reply("Invalid volume. Bounds: `10 - 100`");
        }
    }
}
