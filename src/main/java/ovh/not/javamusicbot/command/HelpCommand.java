package ovh.not.javamusicbot.command;

import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.MusicBot;

import java.util.stream.Collectors;

public class HelpCommand extends Command {
    public HelpCommand() {
        super("help", "commands", "h", "music");
    }

    @Override
    public void on(Context context) {
        String descriptions = MusicBot.getConfigs().constants.commandDescriptions.entrySet().stream()
                .map(e -> String.format("`%s` %s", e.getKey(), e.getValue()))
                .sorted(String::compareTo)
                .collect(Collectors.joining("\n"));

        context.reply("**Commands:**\n%s\n\n**Quick start:** Use `{{prefix}}play <link>` to start playing a song, " +
                "use the same command to add another song, `{{prefix}}skip` to go to the next song and " +
                "`{{prefix}}stop` to stop playing and leave.", descriptions);
    }
}
