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
        subCommands.values().forEach(command -> builder.append(" ").append(command.names[0]));
        subCommandsString = builder.toString();
    }

    @Override
    public void on(Context context) {
        if (!Utils.stringArrayContains(MusicBot.getConfigs().config.owners, context.event.getAuthor().getId())) {
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
            if (context.args.length == 0) {
                context.reply("Restarting shard " + shard.id + "...");
                shard.restart();
            } else {
                int id = Integer.parseInt(context.args[0]);
                for (ShardManager.Shard s : shard.manager.shards) {
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
            GuildMusicManager musicManager = GuildMusicManager.get(context.event.getGuild());
            if (musicManager == null || !musicManager.open || musicManager.player.getPlayingTrack() == null) {
                context.reply("Not playing music!");
                return;
            }
            try {
                context.reply(Utils.encode(playerManager, musicManager.player.getPlayingTrack()));
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
            GuildMusicManager musicManager = GuildMusicManager.getOrCreate(context.event.getGuild(),
                    context.event.getTextChannel(), playerManager);
            if (context.args.length == 0) {
                context.reply("Usage: %prefix%a decode <base64 string>");
                return;
            }
            VoiceChannel channel = context.event.getMember().getVoiceState().getChannel();
            if (channel == null) {
                context.reply("Must be in a voice channel!");
                return;
            }
            String base64 = context.args[0];
            AudioTrack track;
            try {
                track = Utils.decode(playerManager, base64);
            } catch (IOException e) {
                e.printStackTrace();
                context.reply("An error occurred!");
                return;
            }
            if (!musicManager.open) {
                musicManager.open(channel, context.event.getAuthor());
            }
            musicManager.player.playTrack(track);
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
                context.shard.manager.userManager.loadRoles();
            } catch (Exception e) {
                context.reply("Could not resynchronized roles: " + e.getMessage());
                e.printStackTrace();
                return;
            }
            context.reply("Roles resynchronized!");
        }
    }
}
