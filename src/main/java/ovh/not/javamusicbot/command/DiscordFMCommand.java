package ovh.not.javamusicbot.command;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.utils.IOUtil;
import org.json.JSONArray;
import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.CommandManager;
import ovh.not.javamusicbot.GuildMusicManager;
import ovh.not.javamusicbot.LoadResultHandler;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("ConstantConditions")
public class DiscordFMCommand extends Command {
    private static final String DFM_DIRECTORY_PATH = "discordfm/";

    private final CommandManager commandManager;
    private final AudioPlayerManager playerManager;
    private Collection<Library> libraries = null;
    private String usageResponse = null;

    public DiscordFMCommand(CommandManager commandManager, AudioPlayerManager playerManager) {
        super("discordfm", "dfm");
        this.commandManager = commandManager;
        this.playerManager = playerManager;
    }

    private void load() {
        libraries = Arrays.stream(new File(DFM_DIRECTORY_PATH).listFiles())
                .map(file -> new Library(file.getName(), file))
                .sorted(Comparator.comparing(o -> o.name))
                .collect(Collectors.toCollection(ArrayList::new));

        StringBuilder builder = new StringBuilder("Uses a song playlist from http://discord.fm\nUsage: `%prefix%dfm <library>`" +
                "\n\n**Available libraries:**\n");

        Iterator<Library> iterator = libraries.iterator();

        while (iterator.hasNext()) {
            Library library = iterator.next();
            builder.append(library.name);

            if (iterator.hasNext()) {
                builder.append(", ");
            }
        }

        usageResponse = builder.toString();
    }

    @Override
    public void on(Context context) {
        VoiceChannel channel = context.getEvent().getMember().getVoiceState().getChannel();
        if (channel == null) {
            context.reply("You must be in a voice channel!");
            return;
        }

        if (libraries == null || usageResponse == null) {
            Message msg = context.reply("Loading libraries..");
            load();
            msg.delete().queue();
        }

        if (context.getArgs().length == 0) {
            context.reply(usageResponse);
            return;
        }

        GuildMusicManager musicManager = GuildMusicManager.getOrCreate(context.getEvent().getGuild(),
                context.getEvent().getTextChannel(), playerManager);
        if (musicManager.isOpen() && musicManager.getPlayer().getPlayingTrack() != null
                && musicManager.getChannel() != channel
                && !context.getEvent().getMember().hasPermission(musicManager.getChannel(), Permission.VOICE_MOVE_OTHERS)) {
            context.reply("dabBot is already playing music in " + musicManager.getChannel().getName() + " so it cannot " +
                    "be moved. Members with the `VOICE_MOVE_OTHERS` permission are exempt from this.");
            return;
        }

        String libraryName = String.join(" ", context.getArgs());

        Optional<Library> library = libraries.stream()
                .filter(lib -> lib != null && lib.name.equalsIgnoreCase(libraryName))
                .findFirst();

        if (!library.isPresent()) {
            context.reply("Invalid library! Use `%prefix%dfm` to see usage & libraries.");
            return;
        }

        String[] songs;
        try {
            songs = library.get().getSongs();
        } catch (IOException e) {
            e.printStackTrace();
            context.reply("An error occurred!");
            return;
        }

        if (songs == null) {
            context.reply("An error occurred!");
            return;
        }

        musicManager.getScheduler().getQueue().clear();
        musicManager.getScheduler().setRepeat(false);
        musicManager.getScheduler().setLoop(false);
        musicManager.getPlayer().stopTrack();

        LoadResultHandler handler = new LoadResultHandler(commandManager, musicManager, playerManager, context);
        handler.setVerbose(false);

        for (String song : songs) {
            playerManager.loadItem(song, handler);
        }

        if (!musicManager.isOpen()) {
            musicManager.open(channel, context.getEvent().getAuthor());
        }
    }

    private class Library {
        private final String name;
        private final File file;

        private String[] songs = null;

        private Library(String name, File file) {
            name = name.replace("_", " ");
            this.name = name.substring(0, name.length() - 5);
            this.file = file;
        }

        private String[] getSongs() throws IOException {
            if (songs != null) {
                return songs;
            }

            JSONArray array = new JSONArray(new String(IOUtil.readFully(file)));

            String[] songs = new String[array.length()];
            for (int i = 0; i < array.length(); i++) {
                songs[i] = array.getJSONObject(i).getString("identifier");
            }

            this.songs = songs;
            return songs;
        }
    }
}
