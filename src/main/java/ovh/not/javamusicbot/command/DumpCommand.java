package ovh.not.javamusicbot.command;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.GuildMusicManager;

import java.util.ArrayList;
import java.util.List;

import static ovh.not.javamusicbot.MusicBot.GSON;
import static ovh.not.javamusicbot.Utils.HASTEBIN_URL;

public class DumpCommand extends Command {
    public DumpCommand() {
        super("dump");
    }

    @Override
    public void on(Context context) {
        GuildMusicManager musicManager = GuildMusicManager.get(context.event.getGuild());
        if (musicManager == null || musicManager.player.getPlayingTrack() == null) {
            context.reply("No music is playing on this guild!");
            return;
        }
        List<DumpItem> items = new ArrayList<>();
        AudioTrack current = musicManager.player.getPlayingTrack();
        items.add(new DumpItem(0, current.getSourceManager().getSourceName(), current.getIdentifier()));
        int i = 1;
        for (AudioTrack track : musicManager.scheduler.queue) {
            items.add(new DumpItem(i, track.getSourceManager().getSourceName(), track.getIdentifier()));
            i++;
        }
        String json = GSON.toJson(items);
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

    @SuppressWarnings("unused")
    private class DumpItem {
        private int index;
        private String source;
        private String identifier;

        private DumpItem(int index, String source, String identifier) {
            this.index = index;
            this.source = source;
            this.identifier = identifier;
        }
    }
}
