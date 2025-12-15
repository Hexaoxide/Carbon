package net.draycia.carbon.paper.integration.parties;

import com.alessiodp.parties.api.Parties;
import com.alessiodp.parties.api.interfaces.Party;
import com.alessiodp.parties.api.interfaces.PartyPlayer;
import com.google.inject.Inject;
import net.draycia.carbon.api.channels.ChannelPermissions;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.api.users.UserManager;
import net.draycia.carbon.common.channels.ConfigChatChannel;
import net.draycia.carbon.common.channels.messages.ConfigChannelMessageSource;
import net.draycia.carbon.common.messages.CarbonMessages;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static net.draycia.carbon.api.channels.ChannelPermissionResult.channelPermissionResult;

@DefaultQualifier(NonNull.class)
@ConfigSerializable
public class PartiesPartyChannel extends ConfigChatChannel {

    public static final String FILE_NAME = "parties-party.conf";

    private transient @MonotonicNonNull
    @Inject CarbonMessages messages;
    private transient @MonotonicNonNull @Inject UserManager<?> users;

    public PartiesPartyChannel() {
        this.key = Key.key("carbon", "partychat");
        this.commandAliases = List.of("pc");

        this.messageSource = new ConfigChannelMessageSource();
        this.messageSource.defaults = Map.of(
            "default_format", "(party: %party%) <display_name>: <message>",
            "console", "[party: %party%] <username>: <message>"
        );
    }

    @Override
    public ChannelPermissions permissions() {
        return ChannelPermissions.uniformDynamic(player -> channelPermissionResult(
            this.party(player) != null,
            () -> this.messages.cannotUsePartiesPartyChannel(player)
        ));
    }

    @Override
    public List<Audience> recipients(final CarbonPlayer sender) {
        final @Nullable Party party = this.party(sender);

        if (party == null) {
            if (sender.online()) {
                sender.sendMessage(this.messages.cannotUsePartiesPartyChannel(sender));
            }

            return Collections.emptyList();
        }

        final List<Audience> recipients = new ArrayList<>();
        for (final PartyPlayer player : party.getOnlineMembers()) {
            final @Nullable CarbonPlayer carbon = this.users.user(player.getPlayerUUID()).getNow(null);
            if (carbon != null) {
                recipients.add(carbon);
            }
        }

        recipients.add(this.server.console());

        return recipients;
    }

    private @Nullable Party party(final CarbonPlayer player) {
        return Parties.getApi().getPartyOfPlayer(player.uuid());
    }

    @Override
    public boolean shouldCrossServer() {
        return false;
    }

}
