package ovh.not.javamusicbot.command;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.VoiceChannel;
import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.CommandManager;
import ovh.not.javamusicbot.GuildMusicManager;
import ovh.not.javamusicbot.LoadResultHandler;

import java.util.Set;

abstract class BasePlayCommand extends Command {
    private final CommandManager commandManager;
    private final AudioPlayerManager playerManager;
    boolean allowSearch = true;
    boolean isSearch = false;

    BasePlayCommand(CommandManager commandManager, AudioPlayerManager playerManager, String name, String... names) {
        super(name, names);
        this.commandManager = commandManager;
        this.playerManager = playerManager;
    }

    @Override
    public void on(Context context) {
        if (context.getArgs().length == 0) {
            context.reply(this.noArgumentMessage());
            return;
        }

        VoiceChannel channel = context.getEvent().getMember().getVoiceState().getChannel();
        if (channel == null) {
            context.reply("You must be in a voice channel!");
            return;
        }

        GuildMusicManager musicManager = GuildMusicManager.getOrCreate(context.getEvent().getGuild(),
                context.getEvent().getTextChannel(), playerManager);
        if (musicManager.isOpen() && musicManager.getPlayer().getPlayingTrack() != null
                && musicManager.getChannel() != channel
                && !context.getEvent().getMember().hasPermission(musicManager.getChannel(), Permission.VOICE_MOVE_OTHERS)) {
            context.reply("dabBot is already playing music in " + musicManager.getChannel().getName() + " so it cannot " +
                    "be moved. Members with the `VOICE_MOVE_OTHERS` permission are exempt from this.");
            return;
        }

        LoadResultHandler handler = new LoadResultHandler(commandManager, musicManager, playerManager, context);
        handler.setAllowSearch(allowSearch);
        handler.setSearch(isSearch);

        Set<String> flags = context.parseFlags();
        if (flags.contains("first") || flags.contains("f")) {
            handler.setSetFirstInQueue(true);
        }

        context.setArgs(this.transformQuery(context.getArgs()));

        playerManager.loadItem(String.join(" ", context.getArgs()), handler);
        if (!musicManager.isOpen()) {
            musicManager.open(channel, context.getEvent().getAuthor());
        }
    }

    protected abstract String noArgumentMessage();

    protected String[] transformQuery(String[] args) {
        return args;
    }
}
