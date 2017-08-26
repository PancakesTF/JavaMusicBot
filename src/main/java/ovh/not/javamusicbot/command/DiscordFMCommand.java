package ovh.not.javamusicbot.command;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.VoiceChannel;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import ovh.not.javamusicbot.*;

import java.io.IOException;

@SuppressWarnings("ConstantConditions")
public class DiscordFMCommand extends Command {
    private static final String DFM_BASE_URL = "http://temp.discord.fm";
    private static final String DFM_LIBRARIES_URL = DFM_BASE_URL + "/libraries/json";
    private static final String DFM_LIBRARY_URL = DFM_BASE_URL + "/libraries/%s/json";

    private final CommandManager commandManager;
    private final AudioPlayerManager playerManager;
    private Library[] libraries = null;
    private String usageResponse = null;

    public DiscordFMCommand(CommandManager commandManager, AudioPlayerManager playerManager) {
        super("discordfm", "dfm");
        this.commandManager = commandManager;
        this.playerManager = playerManager;
    }

    private void load() {
        try {
            Request request = new Request.Builder().url(DFM_LIBRARIES_URL).build();
            Response response = MusicBot.HTTP_CLIENT.newCall(request).execute();

            JSONArray array = new JSONArray(response.body().string());
            response.close();

            this.libraries = new Library[array.length()];
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                libraries[i] = new Library(object);
            }
            StringBuilder builder = new StringBuilder("Uses a song playlist from http://discord.fm\nUsage: `%prefix%dfm <library>`" +
                    "\n\n**Available libraries:**\n");
            for (int i = 0; i < libraries.length; i++) {
                builder.append(libraries[i].name);
                if (i != libraries.length - 1) {
                    builder.append(", ");
                }
            }
            this.usageResponse = builder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            this.libraries = null;
            this.usageResponse = null;
        }
    }

    @Override
    public void on(Context context) {
        VoiceChannel channel = context.event.getMember().getVoiceState().getChannel();
        if (channel == null) {
            context.reply("You must be in a voice channel!");
            return;
        }
        if (libraries == null || usageResponse == null) {
            Message msg = context.reply("Loading libraries..");
            load();
            msg.delete().queue();
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
        } catch (IOException e) {
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
        musicManager.scheduler.loop = false;
        musicManager.player.stopTrack();
        LoadResultHandler handler = new LoadResultHandler(commandManager, musicManager, playerManager, context);
        handler.verbose = false;
        for (String song : songs) {
            playerManager.loadItem(song, handler);
        }
        if (!musicManager.open) {
            musicManager.open(channel, context.event.getAuthor());
        }
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

        private String[] songs() throws IOException {
            String url = String.format(DFM_LIBRARY_URL, id);

            Request request = new Request.Builder().url(url).build();
            Response response = MusicBot.HTTP_CLIENT.newCall(request).execute();

            JSONArray array = new JSONArray(response.body().string());
            response.close();

            String[] songs = new String[array.length()];
            for (int i = 0; i < array.length(); i++) {
                songs[i] = array.getJSONObject(i).getString("identifier");
            }
            return songs;
        }
    }
}
