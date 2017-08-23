package ovh.not.javamusicbot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.Member;
import org.apache.http.client.config.RequestConfig;
import ovh.not.javamusicbot.command.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CommandManager {
    public final Map<String, Command> commands = new HashMap<>();
    public final Map<Member, Selection<AudioTrack, String>> selectors = new HashMap<>();

    CommandManager(ShardManager.Shard shard) {
        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

        playerManager.registerSourceManager(new YoutubeAudioSourceManager());
        playerManager.registerSourceManager(new SoundCloudAudioSourceManager());
        playerManager.registerSourceManager(new BandcampAudioSourceManager());
        playerManager.registerSourceManager(new VimeoAudioSourceManager());
        playerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
        playerManager.registerSourceManager(new BeamAudioSourceManager());
        playerManager.registerSourceManager(new HttpAudioSourceManager());

        CommandManager.register(commands,
                new AboutCommand(),
                new AdminCommand(shard, playerManager),
                new ChooseCommand(this),
                new DiscordFMCommand(this, playerManager),
                new DumpCommand(playerManager),
                new HelpCommand(this),
                new InviteCommand(),
                new JumpCommand(),
                new LoadCommand(playerManager),
                new LoopCommand(),
                new MoveCommand(),
                new NowPlayingCommand(),
                new PauseCommand(),
                new PlayCommand(this, playerManager),
                new QueueCommand(),
                new RadioCommand(this, playerManager),
                new RemoveCommand(),
                new ReorderCommand(),
                new RepeatCommand(),
                new ResetCommand(),
                new RestartCommand(),
                new SearchCommand(this),
                new ShuffleCommand(),
                new SkipCommand(),
                new SoundCloudCommand(this, playerManager),
                new StopCommand(),
                new VolumeCommand()
        );
    }

    public static void register(Map<String, Command> commands, Command... cmds) {
        for (Command command : cmds) {
            for (String name : command.names) {
                if (commands.containsKey(name)) {
                    throw new RuntimeException(String.format("Command name collision %s in %s!", name,
                            command.getClass().getName()));
                }
                commands.put(name, command);
            }
        }
    }

    Command getCommand(String name) {
        return commands.get(name);
    }
}
