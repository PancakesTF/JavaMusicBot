package ovh.not.javamusicbot;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;

import java.util.List;

public class UserManager {
    private final ShardManager shardManager;

    private Guild guild = null;
    private User self = null;
    private Role supporter = null;
    private Role superSupporter = null;
    private Role superDuperSupporter = null;

    UserManager(ShardManager shardManager) {
        this.shardManager = shardManager;
        loadRoles();
    }

    public void loadRoles() {
        Config config = MusicBot.getConfigs().config;
        guild = shardManager.getGuild(config.discordServer);
        self = guild.getSelfMember().getUser();
        supporter = guild.getRoleById(config.supporterRole);
        superSupporter = guild.getRoleById(config.superSupporterRole);
        superDuperSupporter = guild.getRoleById(config.superDuperSupporterRole);
    }

    private boolean hasRole(User user, Role target) {
        if (user == self) {
            return false;
        }
        Member member = guild.getMember(user);
        if (member == null) {
            return false;
        }
        List<Role> roles = member.getRoles();
        if (roles == null || roles.isEmpty()) {
            return false;
        }
        for (Role role : roles) {
            if (role == target) {
                return true;
            }
        }
        return false;
    }

    public boolean hasSupporter(User user) {
        return hasRole(user, supporter);
    }

    public boolean hasSuperSupporter(User user) {
        return hasRole(user, superSupporter);
    }

    public boolean hasSuperDuperSupporter(User user) {
        return hasRole(user, superDuperSupporter);
    }
}
