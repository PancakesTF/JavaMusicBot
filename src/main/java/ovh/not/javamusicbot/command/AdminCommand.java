package ovh.not.javamusicbot.command;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.bot.sharding.ShardManager;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ovh.not.javamusicbot.*;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AdminCommand extends Command {
    private static final Logger logger = LoggerFactory.getLogger(AdminCommand.class);

    private final Map<String, Command> subCommands = new HashMap<>();
    private final String subCommandsString;

    public AdminCommand(AudioPlayerManager playerManager) {
        super("admin", "a");
        setHidden(true);
        CommandManager.register(subCommands,
                new EvalCommand(),
                new ShutdownCommand(),
                new ShardRestartCommand(),
                new EncodeCommand(playerManager),
                new DecodeCommand(playerManager),
                new ReloadCommand(),
                new ShardStatusCommand()
        );

        subCommandsString = "Subcommands: " + subCommands.values()
                .stream()
                .distinct()
                .map(command -> command.getNames()[0])
                .collect(Collectors.joining(", "));
    }

    @Override
    public void on(Context context) {
        Config config = MusicBot.getConfigs().config;
        String authorId = context.getEvent().getAuthor().getId();

        if (!config.owners.contains(authorId) && !config.managers.contains(authorId)) {
            return;
        }

        if (context.getArgs().length == 0) {
            context.reply(subCommandsString);
            return;
        }

        if (!subCommands.containsKey(context.getArgs()[0])) {
            context.reply("Invalid subcommand!");
            return;
        }

        Command command = subCommands.get(context.getArgs()[0]);
        context.setArgs(Arrays.copyOfRange(context.getArgs(), 1, context.getArgs().length));
        command.on(context);
    }

    private enum RequiredRole {
        OWNER, MANAGER
    }

    private abstract class AdminSubCommand extends Command {
        private final RequiredRole requiredRole;

        private AdminSubCommand(RequiredRole requiredRole, String name, String... names) {
            super(name, names);
            this.requiredRole = requiredRole;
        }

        @Override
        public void on(Context context) {
            Config config = MusicBot.getConfigs().config;

            boolean isOk = false;
            String userId = context.getEvent().getAuthor().getId();

            switch (requiredRole) {
                case OWNER:
                    isOk = config.owners.contains(userId);
                    break;
                case MANAGER:
                    isOk = (config.managers != null && config.managers.contains(userId)) ||
                           (config.owners != null && config.owners.contains(userId));
                    break;
                default:
                    return; // will never happen
            }

            if (isOk) {
                run(context);
            }
        }

        protected abstract void run(Context context);
    }

    private class ShutdownCommand extends AdminSubCommand {
        private ShutdownCommand() {
            super(RequiredRole.OWNER, "shutdown");
        }

        @Override
        public void run(Context context) {
            context.reply("Shutting down!");
            context.getEvent().getJDA().asBot().getShardManager().shutdown(); // shutdown jda

            new Thread(() -> {
                try {
                    Thread.sleep(1000); // give time for jda to shutdown
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.exit(0);
            }).start();
        }
    }

    private class EvalCommand extends AdminSubCommand {
        private final ScriptEngineManager engineManager = new ScriptEngineManager();

        private EvalCommand() {
            super(RequiredRole.OWNER, "eval", "js");
        }

        @Override
        public void run(Context context) {
            ScriptEngine engine = engineManager.getEngineByName("nashorn");
            engine.put("event", context.getEvent());
            engine.put("args", context.getArgs());
            engine.put("jda", context.getEvent().getJDA());
            try {
                Object result = engine.eval(String.join(" ", context.getArgs()));
                if (result != null) context.reply(result.toString());
            } catch (ScriptException e) {
                logger.error("error performing eval command", e);
                context.reply(e.getMessage());
            }
        }
    }

    private class ShardRestartCommand extends AdminSubCommand {
        private ShardRestartCommand() {
            super(RequiredRole.MANAGER, "shardrestart", "sr");
        }

        @Override
        public void run(Context context) {
            MessageReceivedEvent event = context.getEvent();
            JDA jda = event.getJDA();
            ShardManager manager = jda.asBot().getShardManager();

            try {
                int shardId;

                if (context.getArgs().length == 0) {
                    shardId = jda.getShardInfo().getShardId();
                } else {
                    try {
                        shardId = Integer.parseInt(context.getArgs()[0]);
                        if (manager.getShardById(shardId) == null) {
                            context.reply("Invalid shard %d.", shardId);
                            return;
                        }
                    } catch (NumberFormatException e) {
                        context.reply("Invalid input %s. Must be an integer.", context.getArgs()[0]);
                        return;
                    }
                }

                context.reply("Restarting shard %d...", shardId);
                manager.restart(shardId);
            } catch (Exception e) {
                logger.error("error performing shardrestart command", e);
            }
        }
    }

    private class EncodeCommand extends AdminSubCommand {
        private final AudioPlayerManager playerManager;

        private EncodeCommand(AudioPlayerManager playerManager) {
            super(RequiredRole.OWNER, "encode");
            this.playerManager = playerManager;
        }

        @Override
        public void run(Context context) {
            GuildMusicManager musicManager = GuildMusicManager.get(context.getEvent().getGuild());
            if (musicManager == null || !musicManager.isOpen() || musicManager.getPlayer().getPlayingTrack() == null) {
                context.reply("Not playing music!");
                return;
            }
            try {
                context.reply(Utils.encode(playerManager, musicManager.getPlayer().getPlayingTrack()));
            } catch (IOException e) {
                logger.error("error performing encode command", e);
                context.reply("An error occurred!");
            }
        }
    }

    private class DecodeCommand extends AdminSubCommand {
        private final AudioPlayerManager playerManager;

        private DecodeCommand(AudioPlayerManager playerManager) {
            super(RequiredRole.OWNER, "decode");
            this.playerManager = playerManager;
        }

        @Override
        public void run(Context context) {
            GuildMusicManager musicManager = GuildMusicManager.getOrCreate(context.getEvent().getGuild(),
                    context.getEvent().getTextChannel(), playerManager);
            if (context.getArgs().length == 0) {
                context.reply("Usage: {{prefix}}a decode <base64 string>");
                return;
            }
            VoiceChannel channel = context.getEvent().getMember().getVoiceState().getChannel();
            if (channel == null) {
                context.reply("Must be in a voice channel!");
                return;
            }
            String base64 = context.getArgs()[0];
            AudioTrack track;
            try {
                track = Utils.decode(playerManager, base64);
            } catch (IOException e) {
                logger.error("error performing decode command", e);
                context.reply("An error occurred!");
                return;
            }
            if (!musicManager.isOpen()) {
                musicManager.open(channel, context.getEvent().getAuthor());
            }
            musicManager.getPlayer().playTrack(track);
        }
    }

    private class ReloadCommand extends AdminSubCommand {
        private ReloadCommand() {
            super(RequiredRole.MANAGER, "reload");
        }

        @Override
        public void run(Context context) {
            try {
                MusicBot.reloadConfigs();
                RadioCommand.reloadUsageMessage();
            } catch (Exception e) {
                logger.error("error performing reload command", e);
                context.reply("Could not reload configs: " + e.getMessage());
                return;
            }
            context.reply("Configs reloaded!");
        }
    }

    private class ShardStatusCommand extends AdminSubCommand {
        private ShardStatusCommand() {
            super(RequiredRole.MANAGER, "shardstatus", "ss");
        }

        @Override
        public void run(Context context) {
            MessageReceivedEvent event = context.getEvent();
            JDA jda = event.getJDA();
            ShardManager manager = jda.asBot().getShardManager();

            String message = manager.getShards().stream()
                    .sorted(Comparator.comparingInt(a -> a.getShardInfo().getShardId()))
                    .map(shard -> shard.getShardInfo().getShardId() + ": " + shard.getStatus())
                    .collect(Collectors.joining("\n"));

            context.reply("```\n%s```", message);
        }
    }
}
