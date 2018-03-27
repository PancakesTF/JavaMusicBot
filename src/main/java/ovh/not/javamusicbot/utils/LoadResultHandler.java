package ovh.not.javamusicbot.utils;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.CommandManager;
import ovh.not.javamusicbot.audio.GuildAudioController;

public class LoadResultHandler implements AudioLoadResultHandler {
    private static final Logger logger = LoggerFactory.getLogger(LoadResultHandler.class);

    private final CommandManager commandManager;
    private final GuildAudioController musicManager;
    private final AudioPlayerManager playerManager;
    private final Command.Context context;
  
    private boolean verbose;
    private boolean isSearch;
    private boolean allowSearch;
    private boolean setFirstInQueue;

    public LoadResultHandler(CommandManager commandManager, GuildAudioController musicManager, AudioPlayerManager playerManager, Command.Context context) {
        this.commandManager = commandManager;
        this.musicManager = musicManager;
        this.playerManager = playerManager;
        this.context = context;
        this.verbose = true;
    }

    @Override
    public void trackLoaded(AudioTrack audioTrack) {
        boolean playing = musicManager.getPlayer().getPlayingTrack() != null;
        musicManager.getScheduler().queue(audioTrack, setFirstInQueue);
        if (playing && verbose) {
            context.reply(String.format("Queued **%s** `[%s]`", audioTrack.getInfo().title,
                    Utils.formatTrackDuration(audioTrack)));
        }
    }

    @Override
    public void playlistLoaded(AudioPlaylist audioPlaylist) {
        if (!audioPlaylist.isSearchResult()) {
            if (audioPlaylist.getTracks().size() > 1) {
                audioPlaylist.getTracks().forEach(musicManager.getScheduler()::queue);
                context.reply(String.format("Added **%d songs** to the queue!", audioPlaylist.getTracks().size()));
            } else if (audioPlaylist.getSelectedTrack() != null) { //Dont think this can be null but just to be sure
                trackLoaded(audioPlaylist.getSelectedTrack());
            }
        } else {
            int playlistSize = audioPlaylist.getTracks().size();
            if (playlistSize == 0) {
                context.reply("No song matches found! Usage: `{{prefix}}play <link or youtube video title>` or " +
                        "`{{prefix}}soundcloud <soundcloud song title>`");
                if (musicManager.getPlayer().getPlayingTrack() == null && musicManager.getScheduler().getQueue().isEmpty()) {
                    musicManager.close();
                }
                return;
            }
            int size = playlistSize > 5 ? 5 : playlistSize;
            AudioTrack[] audioTracks = new AudioTrack[size];
            for (int i = 0; i < audioTracks.length; i++) {
                audioTracks[i] = audioPlaylist.getTracks().get(i);
            }
            Selection.Formatter<AudioTrack, String> formatter = track -> String.format("%s by %s `[%s]`",
                    track.getInfo().title, track.getInfo().author, Utils.formatTrackDuration(track));
            Selection<AudioTrack, String> selection = new Selection<>(audioTracks, formatter, (found, track) -> {
                if (!found) {
                    context.reply("Selection cancelled!");
                    if (musicManager.getPlayer().getPlayingTrack() == null && musicManager.getScheduler().getQueue().isEmpty()) {
                        musicManager.close();
                    }
                    return;
                }
                trackLoaded(track);
            });
            commandManager.getSelectors().put(context.getEvent().getMember(), selection);
            context.reply(selection.createMessage());
        }
    }

    @Override
    public void noMatches() {
        if (verbose) {
            if (isSearch) {
                context.reply("No song matches found! Usage: `{{prefix}}play <link or youtube video title>` or " +
                        "`{{prefix}}soundcloud <soundcloud song title>`");
                if (context.getEvent().getGuild().getAudioManager().isConnected() &&
                        musicManager.getPlayer().getPlayingTrack() == null && musicManager.getScheduler().getQueue().isEmpty()) {
                    musicManager.close();
                }
            } else if (allowSearch) {
                this.isSearch = true;
                playerManager.loadItem("ytsearch: " + String.join(" ", context.getArgs()), this);
            }
        }
    }

    @Override
    public void loadFailed(FriendlyException e) {
        logger.info("track load failed for query {}", e, String.join(" ", context.getArgs()));
        if (verbose) {
            context.reply("An error occurred: " + e.getMessage() + "\nPlease note that dabBot is primarily hosted in Canada and therefore cannot play songs that are blocked for copyright in Canada.");
        }
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isSearch() {
        return isSearch;
    }

    public void setSearch(boolean search) {
        isSearch = search;
    }

    public boolean isAllowSearch() {
        return allowSearch;
    }

    public void setAllowSearch(boolean allowSearch) {
        this.allowSearch = allowSearch;
    }

    public boolean isSetFirstInQueue() {
        return setFirstInQueue;
    }

    public void setSetFirstInQueue(boolean setFirstInQueue) {
        this.setFirstInQueue = setFirstInQueue;
    }
}
