package ovh.not.javamusicbot;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import static ovh.not.javamusicbot.Utils.formatDuration;

public class LoadResultHandler implements AudioLoadResultHandler {
    private final CommandManager commandManager;
    private final GuildMusicManager musicManager;
    private final AudioPlayerManager playerManager;
    private final Command.Context context;

    private boolean verbose;
    private boolean isSearch;
    private boolean allowSearch;
    private boolean setFirstInQueue;

    public LoadResultHandler(CommandManager commandManager, GuildMusicManager musicManager, AudioPlayerManager playerManager, Command.Context context) {
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
                    formatDuration(audioTrack.getDuration())));
        }
    }

    @Override
    public void playlistLoaded(AudioPlaylist audioPlaylist) {
        if (audioPlaylist.getSelectedTrack() != null) {
            trackLoaded(audioPlaylist.getSelectedTrack());
        } else if (audioPlaylist.isSearchResult()) {
            int playlistSize = audioPlaylist.getTracks().size();
            if (playlistSize == 0) {
                context.reply("No song matches found! Usage: `%prefix%play <link or youtube video title>` or " +
                        "`%prefix%soundcloud <soundcloud song title>`");
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
                    track.getInfo().title, track.getInfo().author, formatDuration(track.getDuration()));
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
        } else {
            audioPlaylist.getTracks().forEach(musicManager.getScheduler()::queue);
            context.reply(String.format("Added **%d songs** to the queue!", audioPlaylist.getTracks().size()));
        }
    }

    @Override
    public void noMatches() {
        if (verbose) {
            if (isSearch) {
                context.reply("No song matches found! Usage: `%prefix%play <link or youtube video title>` or " +
                        "`%prefix%soundcloud <soundcloud song title>`");
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
        if (verbose) {
            context.reply("An error occurred: " + e.getMessage());
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