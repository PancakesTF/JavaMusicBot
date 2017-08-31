package ovh.not.javamusicbot.command;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.entities.Member;
import ovh.not.javamusicbot.Command;
import ovh.not.javamusicbot.CommandManager;
import ovh.not.javamusicbot.Selection;

public class ChooseCommand extends Command {
    private final CommandManager commandManager;

    public ChooseCommand(CommandManager commandManager) {
        super("choose", "pick", "select", "cancel", "c", "choos", "chose");
        this.commandManager = commandManager;
    }

    @Override
    public void on(Context context) {
        Member member = context.getEvent().getMember();
        if (!commandManager.getSelectors().containsKey(member)) {
            context.reply("You do not have a selector in this guild!");
            return;
        }
        Selection<AudioTrack, String> selection = commandManager.getSelectors().get(member);
        if (context.getArgs().length == 0) {
            commandManager.getSelectors().remove(member);
            selection.getCallback().accept(false, null);
            return;
        }
        switch (context.getArgs()[0].toLowerCase()) {
            case "c":
            case "cancel":
                commandManager.getSelectors().remove(member);
                selection.getCallback().accept(false, null);
                return;
        }
        for (String arg : context.getArgs()) {
            int selected;
            try {
                selected = Integer.parseInt(arg);
            } catch (NumberFormatException e) {
                context.reply(String.format("Invalid input `%s`. Must be an integer with the range 1 - %d. **To cancel selection**, "
                        + "use `%prefix%cancel`.", arg, selection.items.length));
                return;
            }
            if (selected < 1 || selected > selection.items.length) {
                context.reply(String.format("Invalid input `%s`. Must be an integer with the range 1 - %d. **To cancel selection**, "
                        + "use `%prefix%cancel`.", arg, selection.items.length));
                return;
            }
            AudioTrack track = selection.items[selected - 1];
            selection.getCallback().accept(true, track);
        }
        commandManager.getSelectors().remove(member);
    }
}
