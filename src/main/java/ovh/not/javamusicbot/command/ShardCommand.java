package ovh.not.javamusicbot.command;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.MusicBot;

public class ShardCommand extends Command {

    public ShardCommand(MusicBot bot) {
        super(bot, "shard", "debug");
        setHidden(true);
    }

    @Override
    public void on(Context context) {
        MessageReceivedEvent e = context.getEvent();
        JDA jda = e.getJDA();

        context.reply("This guild is on shard number `%d`.\nThe JDA status for this shard is `%s`.",
                jda.getShardInfo().getShardId(), jda.getStatus().name());
    }
}
