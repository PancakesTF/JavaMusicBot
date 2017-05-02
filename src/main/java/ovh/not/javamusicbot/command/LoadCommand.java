package ovh.not.javamusicbot.command;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import org.json.JSONArray;
import org.json.JSONObject;
import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.CommandManager;
import ovh.not.javamusicbot.MusicBot;

import java.rmi.UnexpectedException;

@SuppressWarnings("ConstantConditions")
public class LoadCommand extends Command {
    private final CommandManager commandManager;
    private final AudioPlayerManager playerManager;
    private String usageResponse = null;

    public LoadCommand(CommandManager commandManager, AudioPlayerManager playerManager) {
        super("load", "undump");
        this.commandManager = commandManager;
        this.playerManager = playerManager;
    }

    @Override
    public void on(Context context) {
        /*VoiceChannel channel = context.event.getMember().getVoiceState().getChannel();
        if (channel == null) {
            context.reply("You must be in a voice channel!");
            return;
        }
        if (usageResponse == null) {
            Message msg = context.reply("Loading libraries..");
            //load();
            msg.deleteMessage().queue();
        }
        if (context.args.length == 0) {
            context.reply(usageResponse);
            return;
        }
        GuildMusicManager musicManager = GuildMusicManager.getOrCreate(context.event.getGuild(),
                context.event.getTextChannel(), playerManager);
        if (musicManager.open && musicManager.player.getPlayingTrack() != null
                && musicManager.channel != channel
                && !context.event.getMember().hasPermission(musicManager.channel, Permission.VOICE_MOVE_OTHERS)) {
            context.reply("dabBot is already playing music in " + musicManager.channel.getName() + " so it cannot " +
                    "be moved. Members with the `VOICE_MOVE_OTHERS` permission are exempt from this.");
            return;
        }
        String libraryName = String.join(" ", context.args);
        Library library = null;
        for (Library lib : libraries) {
            if (lib != null && lib.name.equalsIgnoreCase(libraryName)) {
                library = lib;
                break;
            }
        }
        if (library == null) {
            context.reply("Invalid library! Use `%prefix%dfm` to see usage & libraries.");
            return;
        }
        String[] songs;
        try {
            songs = library.songs();
        } catch (UnirestException e) {
            e.printStackTrace();
            context.reply("An error occurred!");
            return;
        }
        if (songs == null) {
            context.reply("An error occurred!");
            return;
        }
        musicManager.scheduler.queue.clear();
        musicManager.scheduler.repeat = false;
        musicManager.player.stopTrack();
        LoadResultHandler handler = new LoadResultHandler(commandManager, musicManager, playerManager, context);
        handler.verbose = false;
        for (String song : songs) {
            playerManager.loadItem(song, handler);
        }
        if (!musicManager.open) {
            musicManager.open(channel, context.event.getAuthor());
        }*/
    }

    private class Library {
        private String id, name;

        private Library(JSONObject json) {
            try {
                this.id = json.getString("id");
                this.name = json.getString("name");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private String[] songs() throws UnirestException {
            String url = String.format("%s", id);
            JSONArray array = Unirest.get(url).header("User-Agent", MusicBot.USER_AGENT).asJson().getBody().getArray();
            String[] songs = new String[array.length()];
            for (int i = 0; i < array.length(); i++) {
                songs[i] = array.getJSONObject(i).getString("identifier");
            }
            return songs;
        }
    }

    private enum PlaylistType {
        DUMP, RHINO
    }

    private abstract class Playlist {
        protected String body = null;

        protected Playlist(String url) throws UnirestException, UnexpectedException {
            HttpResponse<String> res = Unirest.get(url).header("User-Agent", "dabBot").asString();
            if (res.getStatus() != 200) {
                throw new UnexpectedException(res.getStatusText());
            }
            body = res.getBody();
        }

        protected abstract String[] getSongs();
    }

    private class DumpedPlaylist extends Playlist {
        private DumpedPlaylist(String url) throws UnirestException, UnexpectedException {
            super(url);

        }

        @Override
        protected String[] getSongs() {
            return new String[0];
        }
    }

    private class RhinoPlaylist extends Playlist {
        private RhinoPlaylist(String url) throws UnirestException, UnexpectedException {
            super(url);
        }

        @Override
        protected String[] getSongs() {
            return new String[0];
        }
    }
}
