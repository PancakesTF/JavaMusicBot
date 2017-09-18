package ovh.not.javamusicbot.command;

import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import ovh.not.javamusicbot.Command;

public class ShardCommand extends Command {

    public ShardCommand() {
        super("shard");
        this.hide = true;
    }

    @Override
    public void on(Context context) {
        MessageReceivedEvent e = context.getEvent();
        context.reply("This guild is on shard number `%d`", e.getJDA().getShardInfo().getShardId());
    }
}
