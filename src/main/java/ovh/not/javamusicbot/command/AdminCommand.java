package ovh.not.javamusicbot.command;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.io.MessageInput;
import com.sedmelluq.discord.lavaplayer.tools.io.MessageOutput;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.DecodedTrackHolder;
import net.dv8tion.jda.core.entities.VoiceChannel;
import ovh.not.javamusicbot.*;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class AdminCommand extends Command {
    private final Map<String, Command> subCommands = new HashMap<>();
    private final String subCommandsString;

    public AdminCommand(ShardManager.Shard shard, AudioPlayerManager playerManager) {
        super("admin", "a");
        hide = true;
        CommandManager.register(subCommands,
                new EvalCommand(shard),
                new StopCommand(),
                new ShardRestartCommand(shard),
                new EncodeCommand(playerManager),
                new DecodeCommand(playerManager),
                new ReloadCommand(),
                new ResyncCommand()
        );
        StringBuilder builder = new StringBuilder("Subcommands:");
        subCommands.values().forEach(command -> builder.append(" ").append(command.getNames()[0]));
        subCommandsString = builder.toString();
    }

    @Override
    public void on(Context context) {
        if (!Utils.stringArrayContains(MusicBot.getConfigs().config.owners, context.getEvent().getAuthor().getId())) {
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

    private class StopCommand extends Command {
        private StopCommand() {
            super("stop");
        }

        @Override
        public void on(Context context) {
            context.getEvent().getJDA().shutdown();
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
            engine.put("event", context.getEvent());
            engine.put("args", context.getArgs());
            engine.put("shard", shard);
            try {
                Object result = engine.eval(String.join(" ", context.getArgs()));
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
            if (context.getArgs().length == 0) {
                context.reply("Restarting shard " + shard.id + "...");
                shard.restart();
            } else {
                int id = Integer.parseInt(context.getArgs()[0]);
                for (ShardManager.Shard s : shard.manager.getShards()) {
                    if (s.id == id) {
                        context.reply("Restarting shard " + s.id + "...");
                        s.restart();
                        return;
                    }
                }
                context.reply("Invalid shard " + id + ".");
            }
        }
    }

    private class EncodeCommand extends Command {
        private final AudioPlayerManager playerManager;

        private EncodeCommand(AudioPlayerManager playerManager) {
            super("encode");
            this.playerManager = playerManager;
        }

        @Override
        public void on(Context context) {
            GuildMusicManager musicManager = GuildMusicManager.get(context.getEvent().getGuild());
            if (musicManager == null || !musicManager.isOpen() || musicManager.getPlayer().getPlayingTrack() == null) {
                context.reply("Not playing music!");
                return;
            }
            try {
                context.reply(Utils.encode(playerManager, musicManager.getPlayer().getPlayingTrack()));
            } catch (IOException e) {
                e.printStackTrace();
                context.reply("An error occurred!");
            }
        }
    }

    private class DecodeCommand extends Command {
        private final AudioPlayerManager playerManager;

        private DecodeCommand(AudioPlayerManager playerManager) {
            super("decode");
            this.playerManager = playerManager;
        }

        @Override
        public void on(Context context) {
            GuildMusicManager musicManager = GuildMusicManager.getOrCreate(context.getEvent().getGuild(),
                    context.getEvent().getTextChannel(), playerManager);
            if (context.getArgs().length == 0) {
                context.reply("Usage: %prefix%a decode <base64 string>");
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
                e.printStackTrace();
                context.reply("An error occurred!");
                return;
            }
            if (!musicManager.isOpen()) {
                musicManager.open(channel, context.getEvent().getAuthor());
            }
            musicManager.getPlayer().playTrack(track);
        }
    }

    private class ReloadCommand extends Command {
        private ReloadCommand() {
            super("reload");
        }

        @Override
        public void on(Context context) {
            try {
                MusicBot.reloadConfigs();
                RadioCommand.reloadUsageMessage();
            } catch (Exception e) {
                context.reply("Could not reload configs: " + e.getMessage());
                e.printStackTrace();
                return;
            }
            context.reply("Configs reloaded!");
        }
    }

    private class ResyncCommand extends Command {
        private ResyncCommand() {
            super("resync", "resynchronized");
        }

        @Override
        public void on(Context context) {
            try {
                context.getShard().manager.getUserManager().loadRoles();
            } catch (Exception e) {
                context.reply("Could not resynchronized roles: " + e.getMessage());
                e.printStackTrace();
                return;
            }
            context.reply("Roles resynchronized!");
        }
    }
}
