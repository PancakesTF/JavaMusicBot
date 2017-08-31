package ovh.not.javamusicbot.command;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.VoiceChannel;
import ovh.not.javamusicbot.*;

import java.util.Iterator;
import java.util.Map;

public class RadioCommand extends Command {
    private final CommandManager commandManager;
    private final AudioPlayerManager playerManager;

    private static String usageMessage = null;

    public RadioCommand(CommandManager commandManager, AudioPlayerManager playerManager) {
        super("radio", "station", "stations", "fm", "r");
        this.commandManager = commandManager;
        this.playerManager = playerManager;
        reloadUsageMessage();
    }

    public static void reloadUsageMessage() {
        StringBuilder builder = new StringBuilder("Streams a variety of radio stations.\n" +
                "Usage: `%prefix%radio <station>`\n" +
                "\n**Available stations:**\n");
        Iterator<String> iterator = MusicBot.getConfigs().constants.radioStations.keySet().iterator();
        while (iterator.hasNext()) {
            String station = iterator.next();
            builder.append(station.substring(1, station.length() - 1));
            if (iterator.hasNext()) {
                builder.append(", ");
            }
        }
        builder.append("\n\nNeed another station? Join the support server with the link in `%prefix%support`.");
        usageMessage = builder.toString();
    }

    @Override
    public void on(Context context) {
        if (context.getArgs().length == 0) {
            if (usageMessage.length() < 2000) {
                context.reply(usageMessage);
            }
            String message = usageMessage;
            while (message.length() > 1950) {
                StringBuilder builder = new StringBuilder();
                int i = 0;
                for (char c : message.toCharArray()) {
                    builder.append(c);
                    i++;
                    if (i > 1950 && c == ',') {
                        i++;
                        break;
                    }
                }
                message = message.substring(i);
                context.reply(builder.toString());
            }
            context.reply(message);
            return;
        }
        String station = "\"" + String.join(" ", context.getArgs()) + "\"";
        String url = null;
        for (Map.Entry<String, String> entry : MusicBot.getConfigs().constants.radioStations.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(station)) {
                url = entry.getValue();
                break;
            }
        }
        if (url == null) {
            context.reply("Invalid station! For usage & stations, use `%prefix%radio`");
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
        musicManager.getScheduler().getQueue().clear();
        musicManager.getScheduler().setRepeat(false);
        musicManager.getScheduler().setLoop(false);
        musicManager.getPlayer().stopTrack();
        playerManager.loadItem(url, handler);
        if (!musicManager.isOpen()) {
            musicManager.open(channel, context.getEvent().getAuthor());
        }
    }
}
