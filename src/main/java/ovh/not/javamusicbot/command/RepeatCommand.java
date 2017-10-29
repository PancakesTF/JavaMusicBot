package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.audio.GuildAudioController;
import ovh.not.javamusicbot.MusicBot;

public class RepeatCommand extends Command {
    public RepeatCommand(MusicBot bot) {
        super(bot, "repeat");
    }

    @Override
    public void on(Context context) {
        GuildAudioController musicManager = this.bot.getGuildsManager().get(context.getEvent().getGuild());
        if (musicManager == null || musicManager.getPlayer().getPlayingTrack() == null) {
            context.reply("No music is playing on this guild! To play a song use `{{prefix}}play`");
            return;
        }

        boolean repeat = !musicManager.getScheduler().isRepeat();
        musicManager.getScheduler().setRepeat(repeat);

        context.reply("**%s** song repeating!", repeat ? "Enabled" : "Disabled");
    }
}
