package ovh.not.javamusicbot.command;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.VoiceChannel;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.GuildMusicManager;
import ovh.not.javamusicbot.MusicBot;
import ovh.not.javamusicbot.Utils;

import java.io.IOException;

@SuppressWarnings("ConstantConditions")
public class LoadCommand extends Command {
    private final AudioPlayerManager playerManager;

    public LoadCommand(AudioPlayerManager playerManager) {
        super("load", "undump");
        this.playerManager = playerManager;
    }

    @Override
    public void on(Context context) {
        VoiceChannel channel = context.event.getMember().getVoiceState().getChannel();
        if (channel == null) {
            context.reply("You must be in a voice channel!");
            return;
        }

        if (context.args.length == 0) {
            context.reply("Usage: `%prefix%load <dumped playlist url>`");
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

        String url = context.args[0];
        if (url.contains("hastebin.com") && !url.contains("raw")) {
            String name = url.substring(url.lastIndexOf("/") + 1);
            url = "https://hastebin.com/raw/" + name;
        }

        JSONArray tracks;
        try {
            Request request = new Request.Builder().url(url).build();
            Response response = MusicBot.HTTP_CLIENT.newCall(request).execute();
            tracks = new JSONArray(response.body().string());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            context.reply("An error occurred! " + e.getMessage());
            return;
        }

        musicManager.scheduler.queue.clear();
        musicManager.scheduler.repeat = false;
        musicManager.scheduler.loop = false;
        musicManager.player.stopTrack();

        for (int i = 0; i < tracks.length(); i++) {
            String encoded = tracks.getString(i);

            try {
                AudioTrack track = Utils.decode(playerManager, encoded);
                musicManager.scheduler.queue(track);
            } catch (IOException e) {
                e.printStackTrace();
                context.reply("An error occurred! " + e.getMessage());
                return;
            }
        }

        context.reply(String.format("Loaded %d tracks from <%s>!", tracks.length(), url));

        if (!musicManager.open) {
            musicManager.open(channel, context.event.getAuthor());
        }
    }
}
