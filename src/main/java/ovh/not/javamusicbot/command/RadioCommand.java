package ovh.not.javamusicbot.command;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.VoiceChannel;
import ovh.not.javamusicbot.*;
import ovh.not.javamusicbot.audio.GuildAudioController;
import ovh.not.javamusicbot.utils.LoadResultHandler;

import java.util.Iterator;
import java.util.Map;

public class RadioCommand extends Command {
    private final CommandManager commandManager;
    private final AudioPlayerManager playerManager;

    private static String usageMessage = null;

    public RadioCommand(MusicBot bot, CommandManager commandManager, AudioPlayerManager playerManager) {
        super(bot, "radio", "station", "stations", "fm", "r");
        this.commandManager = commandManager;
        this.playerManager = playerManager;

        reloadUsageMessage(bot);
    }

    public static void reloadUsageMessage(MusicBot bot) {
        StringBuilder builder = new StringBuilder("Streams a variety of radio stations.\n" +
                "Usage: `{{prefix}}radio <station>`\n" +
                "\n**Available stations:**\n");

        builder.append(bot.getConfigs().constants.getRadioStations());

        builder.append("\n\nNeed another station? Join the support server with the link in `{{prefix}}support`.");
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
        String url = this.bot.getConfigs().constants.getRadioStationUrl(station);


        if (url == null) {
            context.reply("Invalid station! For usage & stations, use `{{prefix}}radio`");
            return;
        }

        VoiceChannel channel = context.getEvent().getMember().getVoiceState().getChannel();
        if (channel == null) {
            context.reply("You must be in a voice channel!");
            return;
        }

        GuildAudioController musicManager = this.bot.getGuildsManager().getOrCreate(context.getEvent().getGuild(),
                context.getEvent().getTextChannel(), playerManager);
        if (musicManager.isOpen() && musicManager.getPlayer().getPlayingTrack() != null
                && musicManager.getChannel() != channel
                && !context.getEvent().getMember().hasPermission(musicManager.getChannel(), Permission.VOICE_MOVE_OTHERS)) {
            context.reply("dabBot is already playing music in %s so it cannot be moved. Members with the `Move Members` permission can do this.", musicManager.getChannel().getName());
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
