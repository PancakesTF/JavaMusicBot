package ovh.not.javamusicbot.command;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.Config;
import ovh.not.javamusicbot.GuildMusicManager;

public class VolumeCommand extends Command {
    private final boolean patreonMode;

    public VolumeCommand(Config config) {
        super("volume", "v");
        this.patreonMode = config.patreon;
    }

    @Override
    public void on(Context context) {
        if (!patreonMode) {
            context.reply("**The volume command is dabBot premium only!**" +
                    "\nDonate for the `Super supporter` tier on patreon at https://patreon.com/dabbot to gain access.");
            return;
        }
        GuildMusicManager musicManager = GuildMusicManager.get(context.event.getGuild());
        if (musicManager == null || musicManager.player.getPlayingTrack() == null) {
            context.reply("No music is playing on this guild!");
            return;
        }
        boolean found = false;
        for (Member member : context.event.getGuild().getMembers()) {
            if ((context.shard.manager.userManager.hasSuperSupporter(member.getUser())
                    && (member.isOwner() || member.hasPermission(Permission.ADMINISTRATOR))) || member.getUser().getId().equals("87164639695110144")) {
                found = true;
                break;
            }
        }
        if (!found) {
            context.reply("**The volume command is dabBot premium only!**" +
                    "\nDonate for the `Super supporter` tier on patreon at https://patreon.com/dabbot to gain access.");
            return;
        }
        if (context.args.length == 0) {
            context.reply(String.format("Current volume: **%d**", musicManager.player.getVolume()));
            return;
        }
        try {
            int newVolume = Math.max(10, Math.min(100, Integer.parseInt(context.args[0])));
            musicManager.player.setVolume(newVolume);
            context.reply(String.format("Set volume to **%d**", newVolume));
        } catch (NumberFormatException e) {
            context.reply("Invalid volume. Bounds: `10 - 100`");
        }
    }
}
