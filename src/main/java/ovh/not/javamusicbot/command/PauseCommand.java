package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.audio.GuildAudioController;
import ovh.not.javamusicbot.MusicBot;

public class PauseCommand extends Command {
    public PauseCommand(MusicBot bot) {
        super(bot,"pause", "resume");
    }

    @Override
    public void on(Context context) {
        GuildAudioController musicManager = this.bot.getGuildsManager().get(context.getEvent().getGuild());
        if (musicManager == null || musicManager.getPlayer().getPlayingTrack() == null) {
            context.reply("No music is playing on this guild! To play a song use `{{prefix}}play`");
            return;
        }

        boolean action = !musicManager.getPlayer().isPaused();
        musicManager.getPlayer().setPaused(action);

        if (action) {
            context.reply("Paused music playback! Use `{{prefix}}resume` to resume.");
        } else {
            context.reply("Resumed music playback!");
        }
    }
}
