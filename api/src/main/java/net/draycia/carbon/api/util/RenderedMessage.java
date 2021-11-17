package net.draycia.carbon.api.util;

import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.text.Component;

/**
 * A message that's been rendered.
 *
 * @since 2.0.0
 */
public record RenderedMessage(Component component, MessageType messageType) {

}
