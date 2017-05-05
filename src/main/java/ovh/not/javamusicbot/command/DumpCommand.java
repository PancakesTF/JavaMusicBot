package ovh.not.javamusicbot.command;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.json.JSONArray;
import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.GuildMusicManager;
import ovh.not.javamusicbot.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static ovh.not.javamusicbot.MusicBot.GSON;
import static ovh.not.javamusicbot.Utils.HASTEBIN_URL;
import static ovh.not.javamusicbot.Utils.encode;

public class DumpCommand extends Command {
    private final AudioPlayerManager playerManager;

    public DumpCommand(AudioPlayerManager playerManager) {
        super("dump");
        this.playerManager = playerManager;
    }

    @Override
    public void on(Context context) {
        GuildMusicManager musicManager = GuildMusicManager.get(context.event.getGuild());
        if (musicManager == null || musicManager.player.getPlayingTrack() == null) {
            context.reply("No music is playing on this guild!");
            return;
        }
        String[] items = new String[musicManager.scheduler.queue.size() + 1];
        AudioTrack current = musicManager.player.getPlayingTrack();
        try {
            items[0] = Utils.encode(playerManager, current);
        } catch (IOException e) {
            e.printStackTrace();
            context.reply("An error occurred!");
            return;
        }
        int i = 1;
        for (AudioTrack track : musicManager.scheduler.queue) {
            try {
                items[i] = encode(playerManager, track);
            } catch (IOException e) {
                e.printStackTrace();
                context.reply("An error occurred!");
                return;
            }
            i++;
        }
        String json = new JSONArray(items).toString();
        Unirest.post(HASTEBIN_URL).body(json).asJsonAsync(new Callback<JsonNode>() {
            @Override
            public void completed(HttpResponse<JsonNode> httpResponse) {
                context.reply(String.format("Dump created! https://hastebin.com/%s.json", httpResponse.getBody()
                        .getObject().getString("key")));
            }

            @Override
            public void failed(UnirestException e) {
                e.printStackTrace();
                context.reply("An error occured!");
            }

            @Override
            public void cancelled() {
                context.reply("Operation cancelled.");
            }
        });
    }
}
