package ovh.not.javamusicbot.command;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.bramhaag.owo.OwO;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.GuildMusicManager;
import ovh.not.javamusicbot.MusicBot;
import ovh.not.javamusicbot.Utils;

import javax.annotation.Nonnull;
import java.io.IOException;

import static ovh.not.javamusicbot.MusicBot.JSON_MEDIA_TYPE;
import static ovh.not.javamusicbot.Utils.HASTEBIN_URL;
import static ovh.not.javamusicbot.Utils.encode;

public class DumpCommand extends Command {
    private final AudioPlayerManager playerManager;
    private final OwO owo;

    public DumpCommand(AudioPlayerManager playerManager) {
        super("dump");
        this.playerManager = playerManager;
        owo = new OwO.Builder()
                .setKey(MusicBot.getConfigs().config.owoKey)
                .setUploadUrl("https://paste.dabbot.org")
                .setShortenUrl("https://paste.dabbot.org")
                .build();
    }

    @Override
    public void on(Context context) {
        GuildMusicManager musicManager = GuildMusicManager.get(context.getEvent().getGuild());
        if (musicManager == null || musicManager.getPlayer().getPlayingTrack() == null) {
            context.reply("No music is playing on this guild!");
            return;
        }
        String[] items = new String[musicManager.getScheduler().getQueue().size() + 1];
        AudioTrack current = musicManager.getPlayer().getPlayingTrack();
        try {
            items[0] = Utils.encode(playerManager, current);
        } catch (IOException e) {
            e.printStackTrace();
            context.reply("An error occurred!");
            return;
        }
        int i = 1;
        for (AudioTrack track : musicManager.getScheduler().getQueue()) {
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
        owo.upload(json, "text/plain").execute(file -> {
            context.reply("Dump created! " + file.getFullUrl());
        }, throwable -> {
            throwable.printStackTrace();

            RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, json);

            Request request = new Request.Builder()
                    .url(HASTEBIN_URL)
                    .method("POST", body)
                    .build();

            MusicBot.HTTP_CLIENT.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@Nonnull Call call, @Nonnull IOException e) {
                    e.printStackTrace();
                    context.reply("An error occured!");
                }

                @Override
                public void onResponse(@Nonnull Call call, @Nonnull Response response) throws IOException {
                    context.reply(String.format("Dump created! https://hastebin.com/%s.json",
                            new JSONObject(response.body().string()).getString("key")));
                    response.close();
                }
            });
        });
    }
}
