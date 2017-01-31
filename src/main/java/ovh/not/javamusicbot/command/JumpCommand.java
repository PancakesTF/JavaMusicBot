package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.GuildMusicManager;

import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JumpCommand extends Command {
    private static final Pattern TIME_PATTERN = Pattern.compile("(?:(?<hours>\\d{1,2}):)?(?:(?<minutes>\\d{1,2}):)?(?<seconds>\\d{1,2})");

    public JumpCommand() {
        super("jump");
    }

    @Override
    public void on(Context context) {
        GuildMusicManager musicManager = GuildMusicManager.get(context.event.getGuild());
        if (musicManager == null || musicManager.player.getPlayingTrack() == null) {
            context.reply("No music is playing on this guild!");
            return;
        }
        if (context.args.length == 0) {
            context.reply("Usage: `!!!jump <time>`\nExample: `!!!jump 03:51` - starts playing the current song "
                    + "at 3 min 51s instead of at the start.\nTime format: `hh:mm:ss`, e.g. 01:25:51 = 1 hour, "
                    + "25 minutes & 51 seconds");
            return;
        }
        Matcher matcher = TIME_PATTERN.matcher(context.args[0]);
        if (!matcher.find()) {
            context.reply("Usage: `!!!jump <time>`\nExample: `!!!jump 03:51` - starts playing the current song "
                    + "at 3 min 51s instead of at the start.\nTime format: `hh:mm:ss`, e.g. 01:25:51 = 1 hour, "
                    + "25 minutes & 51 seconds");
            return;
        }
        String sHours = matcher.group("hours");
        String sMinutes = matcher.group("minutes");
        if (sMinutes == null && sHours != null) {
            sMinutes = sHours;
            sHours = null;
        }
        String sSeconds = matcher.group("seconds");
        long hours = 0, minutes = 0, seconds = 0;
        try {
            if (sHours != null) {
                hours = Long.parseLong(sHours);
            }
            if (sMinutes != null) {
                minutes = Long.parseLong(sMinutes);
            }
            if (sSeconds != null) {
                seconds = Long.parseLong(sSeconds);
            }
        } catch (NumberFormatException e) {
            context.reply("Usage: `!!!jump <time>`\nExample: `!!!jump 03:51` - starts playing the current song "
                    + "at 3 min 51s instead of at the start.\nTime format: `hh:mm:ss`, e.g. 01:25:51 = 1 hour, "
                    + "25 minutes & 51 seconds");
            return;
        }
        long time = Duration.ofHours(hours).toMillis();
        time += Duration.ofMinutes(minutes).toMillis();
        time += Duration.ofSeconds(seconds).toMillis();
        musicManager.player.getPlayingTrack().setPosition(time);
        context.reply("Jumped to the specified position. Use `!!!nowplaying` to see the current song & position.");
    }
}
