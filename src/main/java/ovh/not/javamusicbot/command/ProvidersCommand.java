package ovh.not.javamusicbot.command;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.MusicBot;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProvidersCommand extends Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProvidersCommand.class);

    private Optional<String> sourceManagersMessage = Optional.empty();

    private final AudioPlayerManager playerManager;

    public ProvidersCommand(MusicBot bot, AudioPlayerManager playerManager) {
        super(bot, "providers", "sources", "source", "provider");
        this.playerManager = playerManager;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void on(Context context) {
        if (!sourceManagersMessage.isPresent()) {
            try {
                Field field = DefaultAudioPlayerManager.class.getDeclaredField("sourceManagers");
                field.setAccessible(true);

                sourceManagersMessage = Optional.of(((List<AudioSourceManager>) field.get(playerManager))
                        .stream()
                        .map(manager -> manager.getSourceName().toLowerCase())
                        .collect(Collectors.joining(", ")));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                LOGGER.error("error getting available music providers", e);
                return;
            }
        }

        context.reply("Available music providers: %s", sourceManagersMessage.get());
    }
}
