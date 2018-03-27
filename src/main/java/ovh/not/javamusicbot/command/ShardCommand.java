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

        if (context.getArgs().length == 2) {
            long serverId;
            int shardCount;
            try {
                serverId = Long.parseLong(context.getArgs()[0]);
                shardCount = Integer.parseInt(context.getArgs()[1]);
            } catch (NumberFormatException ignored) {
                context.reply("Usage: {{prefix}}shard <guild id> <shard count>");
                return;
            }
            long shardId = (serverId >> 22) % shardCount;
            context.reply("Guild with ID `%d` is on shard `%d`.", serverId, shardId);
        } else {
            context.reply("This guild is on shard number `%d` of `%d`.\nThe JDA status for this shard is `%s`.",
                    jda.getShardInfo().getShardId(), jda.getShardInfo().getShardTotal(), jda.getStatus().name());
        }
    }
}
