package ovh.not.javamusicbot.command;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import ovh.not.javamusicbot.*;

public class VolumeCommand extends Command {
    public VolumeCommand() {
        super("volume", "v");
    }

    @Override
    public void on(Context context) {
        if (!MusicBot.getConfigs().config.patreon) {
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
                    && (member.isOwner() || member.hasPermission(Permission.ADMINISTRATOR)))
                    || Utils.stringArrayContains(MusicBot.getConfigs().config.owners, member.getUser().getId())) {
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
            int newVolume = Math.max(1, Math.min(150, Integer.parseInt(context.args[0])));
            musicManager.player.setVolume(newVolume);
            context.reply(String.format("Set volume to **%d**", newVolume));
        } catch (NumberFormatException e) {
            context.reply("Invalid volume. Bounds: `10 - 100`");
        }
    }
}
