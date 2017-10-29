package ovh.not.javamusicbot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.Member;
import ovh.not.javamusicbot.command.*;
import ovh.not.javamusicbot.utils.Selection;

import java.util.HashMap;
import java.util.Map;

public class CommandManager {
    private final Map<String, Command> commands = new HashMap<>();
    private final Map<Member, Selection<AudioTrack, String>> selectors = new HashMap<>();

    CommandManager(MusicBot bot) {
        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

        playerManager.registerSourceManager(new YoutubeAudioSourceManager());
        playerManager.registerSourceManager(new SoundCloudAudioSourceManager());
        playerManager.registerSourceManager(new BandcampAudioSourceManager());
        playerManager.registerSourceManager(new VimeoAudioSourceManager());
        playerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
        playerManager.registerSourceManager(new BeamAudioSourceManager());
        playerManager.registerSourceManager(new HttpAudioSourceManager());

        CommandManager.register(commands,
                new AboutCommand(bot),
                new AdminCommand(bot, playerManager),
                new ChooseCommand(bot, this),
                new DiscordFMCommand(bot, this, playerManager),
                new DumpCommand(bot, playerManager),
                new HelpCommand(bot),
                new InviteCommand(bot),
                new JumpCommand(bot),
                new LoadCommand(bot, playerManager),
                new LoopCommand(bot),
                new NowPlayingCommand(bot),
                new PauseCommand(bot),
                new PlayCommand(bot, this, playerManager),
                new ProvidersCommand(bot, playerManager),
                new QueueCommand(bot),
                new RadioCommand(bot, this, playerManager),
                new RemoveCommand(bot),
                new ReorderCommand(bot),
                new RepeatCommand(bot),
                new RestartCommand(bot),
                new SearchCommand(bot, this),
                new ShardCommand(bot),
                new ShuffleCommand(bot),
                new SkipCommand(bot),
                new SoundCloudCommand(bot, this, playerManager),
                new StopCommand(bot),
                new VolumeCommand(bot)
        );
    }

    public Map<String, Command> getCommands() {
        return commands;
    }

    public Map<Member, Selection<AudioTrack, String>> getSelectors() {
        return selectors;
    }


    public static void register(Map<String, Command> commands, Command... cmds) {
        for (Command command : cmds) {
            for (String name : command.getNames()) {
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
