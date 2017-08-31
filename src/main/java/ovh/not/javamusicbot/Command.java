package ovh.not.javamusicbot;

import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ovh.not.javamusicbot.Utils.getPrivateChannel;

public abstract class Command {
    private static final Pattern FLAG_PATTERN = Pattern.compile("\\s+-([a-zA-Z]+)");

    private String[] names;
    protected boolean hide = false;

    protected Command(String name, String... names) {
        this.names = new String[names.length + 1];
        this.names[0] = name;
        System.arraycopy(names, 0, this.names, 1, names.length);
    }

    public String[] getNames(){
        return names;
    }

    public void setNames(String[] names) {
        this.names = names;
    }

    public boolean isHidden(){
        return this.hide;
    }

    public abstract void on(Context context);

    protected class Context {

        private MessageReceivedEvent event;
        private ShardManager.Shard shard;
        private String[] args;

        public MessageReceivedEvent getEvent() {
            return event;
        }

        public void setEvent(MessageReceivedEvent event) {
            this.event = event;
        }

        public ShardManager.Shard getShard() {
            return shard;
        }

        public void setShard(ShardManager.Shard shard) {
            this.shard = shard;
        }

        public String[] getArgs() {
            return args;
        }

        public void setArgs(String[] args) {
            this.args = args;
        }

        public Message reply(String message) {
            try {
                return event.getChannel().sendMessage(message.replace("%prefix%", MusicBot.getConfigs().config.prefix))
                        .complete();
            } catch (PermissionException e) {
                getPrivateChannel(event.getAuthor()).sendMessage("**dabBot does not have permission to talk in the #"
                        + event.getTextChannel().getName() + " text channel.**\nTo fix this, allow dabBot to " +
                        "`Read Messages` and `Send Messages` in that text channel.\nIf you are not the guild " +
                        "owner, please send this to them.").complete();
                return null;
            }
        }

        public Set<String> parseFlags() {
            String content = String.join(" ", args);
            Matcher matcher = FLAG_PATTERN.matcher(content);
            Set<String> matches = new HashSet<>();
            while (matcher.find()) {
                matches.add(matcher.group().replaceFirst("\\s+-", ""));
            }
            content = content.replaceAll("\\s+-([a-zA-Z]+)", "");
            args = content.split("\\s+");
            return matches;
        }
    }
}
