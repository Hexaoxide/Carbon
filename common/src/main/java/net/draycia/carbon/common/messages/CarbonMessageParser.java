package net.draycia.carbon.common.messages;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.proximyst.moonshine.message.IMessageParser;
import com.proximyst.moonshine.message.ParsingContext;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

@Singleton
public class CarbonMessageParser implements IMessageParser<String, Component, Audience> {

    private final CarbonMessageService messageService;

    private final boolean checkForLegacy = !Boolean.getBoolean("disableLegacyCheck");
    private final String legacyChar = String.valueOf(LegacyComponentSerializer.SECTION_CHAR);

    @Inject
    public CarbonMessageParser(final CarbonMessageService messageService) {
        this.messageService = messageService;
    }

    public Component parse(String message, final ParsingContext<Audience> parsingContext) {
        if (checkForLegacy) {
            if (message.contains(legacyChar)) {
                message = message.replace(legacyChar, "");
                this.messageService.unsupportedLegacyChar(parsingContext.receiver(), message);
            }
        }

        return MiniMessage.get().parse(message, parsingContext.placeholders());
    }

}
