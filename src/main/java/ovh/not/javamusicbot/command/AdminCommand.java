package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.CommandManager;
import ovh.not.javamusicbot.Config;
import ovh.not.javamusicbot.ShardManager;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AdminCommand extends Command {
    private final Map<String, Command> subCommands = new HashMap<>();
    private final Config config;
    private final String subCommandsString;

    public AdminCommand(Config config, ShardManager.Shard shard) {
        super("admin", "a");
        hide = true;
        this.config = config;
        CommandManager.register(subCommands,
                new EvalCommand(shard),
                new StopCommand(),
                new ShardRestartCommand(shard)
        );
        StringBuilder builder = new StringBuilder("Subcommands:");
        subCommands.values().forEach(command -> builder.append(" ").append(command.names[0]));
        subCommandsString = builder.toString();
    }

    @Override
    public void on(Context context) {
        if (!context.event.getAuthor().getId().equals(config.owner)) {
            return;
        }
        if (context.args.length == 0) {
            context.reply(subCommandsString);
            return;
        }
        if (!subCommands.containsKey(context.args[0])) {
            context.reply("Invalid subcommand!");
            return;
        }
        Command command = subCommands.get(context.args[0]);
        context.args = Arrays.copyOfRange(context.args, 1, context.args.length);
        command.on(context);
    }

    private class StopCommand extends Command {
        private StopCommand() {
            super("stop");
        }

        @Override
        public void on(Context context) {
            context.event.getJDA().shutdown();
            System.exit(0);
        }
    }

    private class EvalCommand extends Command {
        private final ScriptEngineManager engineManager = new ScriptEngineManager();
        private final ShardManager.Shard shard;

        private EvalCommand(ShardManager.Shard shard) {
            super("eval", "js");
            this.shard = shard;
        }

        @Override
        public void on(Context context) {
            ScriptEngine engine = engineManager.getEngineByName("nashorn");
            engine.put("event", context.event);
            engine.put("args", context.args);
            engine.put("shard", shard);
            try {
                Object result = engine.eval(String.join(" ", context.args));
                if (result != null) context.reply(result.toString());
            } catch (ScriptException e) {
                e.printStackTrace();
                context.reply(e.getMessage());
            }
        }
    }

    private class ShardRestartCommand extends Command {
        private final ShardManager.Shard shard;

        private ShardRestartCommand(ShardManager.Shard shard) {
            super("shardrestart", "sr");
            this.shard = shard;
        }

        @Override
        public void on(Context context) {
            context.reply("Restarting shard " + shard.id + "...");
            shard.restart();
        }
    }
}
