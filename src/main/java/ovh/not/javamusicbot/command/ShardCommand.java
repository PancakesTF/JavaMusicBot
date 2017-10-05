package ovh.not.javamusicbot.command;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import ovh.not.javamusicbot.Command;

public class ShardCommand extends Command {

    public ShardCommand() {
        super("shard", "debug");
        this.hide = true;
    }

    @Override
    public void on(Context context) {
        MessageReceivedEvent e = context.getEvent();
        JDA jda = e.getJDA();

        context.reply("This guild is on shard number `%d`.\nThe JDA status for this shard is `%s`.",
                jda.getShardInfo().getShardId(), jda.getStatus().name());
    }
}
