package net.draycia.carbon.api.util;

import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.text.Component;

public record RenderedMessage(Component component, MessageType messageType) {

}
