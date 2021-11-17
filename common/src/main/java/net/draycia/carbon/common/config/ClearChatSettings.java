package net.draycia.carbon.common.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
@DefaultQualifier(NonNull.class)
// TODO: Config versioning. This isn't automatically added to existing configs otherwise.
public class ClearChatSettings {

    @Comment("The message that will be sent to each player.")
    private String message = "";

    @Comment("The number of times the message will be sent to each player.")
    private int iterations = 50;

    @Comment("The message to be sent after chat is cleared.")
    private String broadcast = "<gold>Chat has been cleared by </gold><green><display_name><green><gold>.";

    private @MonotonicNonNull Component messageComponent = null;

    public Component message() {
        if (this.messageComponent == null) {
            this.messageComponent = MiniMessage.miniMessage().deserialize(this.message);
        }

        return this.messageComponent;
    }

    public int iterations() {
        return this.iterations;
    }

    private @MonotonicNonNull Component broadcastComponent = null;

    public Component broadcast() {
        if (this.broadcastComponent == null) {
            this.broadcastComponent = MiniMessage.miniMessage().deserialize(this.broadcast);
        }

        return this.broadcastComponent;
    }

}
