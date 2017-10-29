package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.audio.GuildAudioController;
import ovh.not.javamusicbot.MusicBot;

public class LoopCommand extends Command {
    public LoopCommand(MusicBot bot) {
        super(bot, "loop");
    }

    @Override
    public void on(Context context) {
        GuildAudioController musicManager = this.bot.getGuildsManager().get(context.getEvent().getGuild());
        if (musicManager == null || musicManager.getPlayer().getPlayingTrack() == null) {
            context.reply("No music is playing on this guild! To play a song use `{{prefix}}play`");
            return;
        }

        boolean loop = !musicManager.getScheduler().isLoop();
        musicManager.getScheduler().setLoop(loop);

        context.reply("**%s** queue looping!", loop ? "Enabled" : "Disabled");
    }
}
