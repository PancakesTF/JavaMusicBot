package ovh.not.javamusicbot.utils;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import ovh.not.javamusicbot.Config;
import ovh.not.javamusicbot.MusicBot;

public class PermissionReader {

    private final MusicBot bot;

    public PermissionReader(MusicBot bot) {
        this.bot = bot;
    }

    private boolean allowedPatronAccess(Guild guild, Role role) {
        Config config = this.bot.getConfigs().config;

        Guild dabbotGuild = guild.getJDA().asBot().getShardManager().getGuildById(config.discordServer);

        for (Member member : guild.getMembers()) {
            if (config.owners.contains(member.getUser().getId())) {
                return true;
            }

            if (!member.isOwner() && !member.hasPermission(Permission.ADMINISTRATOR)) {
                continue;
            }

            Member dabbotMember = dabbotGuild.getMember(member.getUser());
            if (dabbotMember == null) {
                continue;
            }

            if (dabbotMember.getRoles().contains(role)) {
                return true;
            }
        }

        return false;
    }

    public boolean allowedSupporterPatronAccess(Guild guild) {
        Config config = this.bot.getConfigs().config;

        Guild dabbotGuild = guild.getJDA().asBot().getShardManager().getGuildById(config.discordServer);
        Role supporterRole = dabbotGuild.getRoleById(config.supporterRole);

        return allowedPatronAccess(guild, supporterRole);
    }

    public boolean allowedSuperSupporterPatronAccess(Guild guild) {
        Config config = this.bot.getConfigs().config;

        Guild dabbotGuild = guild.getJDA().asBot().getShardManager().getGuildById(config.discordServer);
        Role superSupporterRole = dabbotGuild.getRoleById(config.superSupporterRole);

        return allowedPatronAccess(guild, superSupporterRole);
    }

    public boolean allowedSuperSupporterPatronAccess(User user) {
        Config config = this.bot.getConfigs().config;

        Guild dabbotGuild = user.getJDA().asBot().getShardManager().getGuildById(config.discordServer);
        Role superSupporterRole = dabbotGuild.getRoleById(config.superSupporterRole);
        Member dabbotMember = dabbotGuild.getMember(user);

        return dabbotMember != null && dabbotMember.getRoles().contains(superSupporterRole);
    }
}
