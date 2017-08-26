package ovh.not.javamusicbot.command;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.bramhaag.owo.OwO;
import okhttp3.*;
import org.json.JSONObject;
import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.GuildMusicManager;
import ovh.not.javamusicbot.MusicBot;
import ovh.not.javamusicbot.Pageable;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.Queue;

import static ovh.not.javamusicbot.MusicBot.JSON_MEDIA_TYPE;
import static ovh.not.javamusicbot.Utils.*;

@SuppressWarnings("unchecked")
public class QueueCommand extends Command {
    private static final String BASE_LINE = "%s by %s `[%s]`";
    private static final String CURRENT_LINE = "__Currently playing:__\n" + BASE_LINE;
    private static final String QUEUE_LINE = "\n`%02d` " + BASE_LINE;
    private static final String SONG_QUEUE_LINE = "\n\n__Song queue:__ (Page **%d** of **%d**)";
    private static final int PAGE_SIZE = 10;

    private final OwO owo;

    public QueueCommand() {
        super("queue", "list", "q");
        owo = new OwO.Builder()
                .setKey(MusicBot.getConfigs().config.owoKey)
                .setUploadUrl("https://paste.dabbot.org")
                .setShortenUrl("https://paste.dabbot.org")
                .build();
    }

    @Override
    public void on(Context context) {
        GuildMusicManager musicManager = GuildMusicManager.get(context.event.getGuild());
        if (musicManager == null || musicManager.player.getPlayingTrack() == null) {
            context.reply("No music is queued or playing on this guild!");
            return;
        }
        AudioTrack playing = musicManager.player.getPlayingTrack();
        Queue<AudioTrack> queue = musicManager.scheduler.queue;
        StringBuilder builder = new StringBuilder();
        if (context.args.length > 0 && context.args[0].equalsIgnoreCase("all")) {
            long durationTotal = playing.getDuration();
            List<AudioTrack> list = (List<AudioTrack>) queue;
            StringBuilder items = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                AudioTrack track = list.get(i);
                durationTotal += track.getDuration();
                items.append(String.format("\n%02d %s by %s [%s/%s]", i + 1, track.getInfo().title,
                        track.getInfo().author, formatDuration(track.getPosition()),
                        formatDuration(track.getDuration())));
            }
            builder.append(String.format("Song queue for %s - %d songs (%s).\nCurrent song: %s by %s [%s/%s]\n",
                    context.event.getGuild().getName(), queue.size(), formatLongDuration(durationTotal), playing.getInfo().title,
                    playing.getInfo().author, formatDuration(playing.getPosition()),
                    formatDuration(playing.getDuration())));
            builder.append(items.toString());
            owo.upload(builder.toString(), "text/plain").execute(file -> {
                context.reply("Full song queue: " + file.getFullUrl());
            }, throwable -> {
                throwable.printStackTrace();

                RequestBody body = RequestBody.create(JSON_MEDIA_TYPE, builder.toString());

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
                        context.reply(String.format("Full song queue: https://hastebin.com/raw/%s",
                                new JSONObject(response.body().string()).getString("key")));
                        response.close();
                    }
                });
            });
        } else {
            builder.append(String.format(CURRENT_LINE, playing.getInfo().title, playing.getInfo().author,
                    formatDuration(playing.getPosition()) + "/" + formatDuration(playing.getDuration())));
            Pageable<AudioTrack> pageable = new Pageable<>((List<AudioTrack>) queue);
            pageable.setPageSize(PAGE_SIZE);
            if (context.args.length > 0) {
                int page;
                try {
                    page = Integer.parseInt(context.args[0]);
                } catch (NumberFormatException e) {
                    context.reply(String.format("Invalid page! Must be an integer within the range %d - %d",
                            pageable.getMinPageRange(), pageable.getMaxPages()));
                    return;
                }
                if (page < pageable.getMinPageRange() || page > pageable.getMaxPages()) {
                    context.reply(String.format("Invalid page! Must be an integer within the range %d - %d",
                            pageable.getMinPageRange(), pageable.getMaxPages()));
                    return;
                }
                pageable.setPage(page);
            } else {
                pageable.setPage(pageable.getMinPageRange());
            }
            builder.append(String.format(SONG_QUEUE_LINE, pageable.getPage(), pageable.getMaxPages()));
            int index = 1;
            for (AudioTrack track : pageable.getListForPage()) {
                builder.append(String.format(QUEUE_LINE, ((pageable.getPage() - 1) * pageable.getPageSize()) + index, track.getInfo().title, track.getInfo().author,
                        formatDuration(track.getDuration())));
                index++;
            }
            if (pageable.getPage() < pageable.getMaxPages()) {
                builder.append("\n\n__To see the next page:__ `%prefix%queue ").append(pageable.getPage() + 1)
                        .append("`\nTo see the full queue, use `%prefix%queue all`");
            }
            context.reply(builder.toString());
        }
    }
}
