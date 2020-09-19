package net.draycia.carbon.common.listeners.events;

import net.draycia.carbon.api.events.PreChatFormatEvent;
import net.draycia.carbon.api.events.misc.CarbonEvents;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyFormat;
import net.kyori.event.PostOrders;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.regex.Pattern;

public class LegacyFormatHandler {

  private final @NonNull Pattern pattern =
    Pattern.compile("[§&]x[§&]([0-9a-f])[§&]([0-9a-f])[§&]([0-9a-f])[§&]([0-9a-f])[§&]([0-9a-f])[§&]([0-9a-f])");

  public LegacyFormatHandler() {
    CarbonEvents.register(PreChatFormatEvent.class, PostOrders.FIRST, false, event -> {
      // Legacy RGB
      event.format(this.pattern.matcher(event.format()).replaceAll("<#$1$2$3$4$5$6>"));

      // Legacy Colors
      for (final char c : "0123456789abcdef".toCharArray()) {
        final LegacyFormat format = LegacyComponentSerializer.parseChar(c);
        final TextColor color = format.color();

        event.format(event.format().replaceAll("[§&]" + c, "<" + color.asHexString() + ">"));
      }

      // Legacy Formatting
      for (final char c : "klmno".toCharArray()) {
        final LegacyFormat format = LegacyComponentSerializer.parseChar(c);
        final TextDecoration decoration = format.decoration();

        event.format(event.format().replaceAll("[§&]" + c, "<" + decoration.name() + ">"));
      }
    });
  }

}
