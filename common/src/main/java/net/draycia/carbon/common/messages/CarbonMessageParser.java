package net.draycia.carbon.common.messages;

<<<<<<< HEAD
import com.google.inject.Inject;
import com.google.inject.Singleton;
=======
>>>>>>> Initial pass - 1/?
import com.proximyst.moonshine.message.IMessageParser;
import com.proximyst.moonshine.message.ParsingContext;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
<<<<<<< HEAD
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
        if (this.checkForLegacy) {
            if (message.contains(this.legacyChar)) {
                message = message.replace(this.legacyChar, "");
                this.messageService.unsupportedLegacyChar(parsingContext.receiver(), message);
            }
        }

=======

public class CarbonMessageParser implements IMessageParser<String, Component, Audience> {

    public Component parse(final String message, final ParsingContext<Audience> parsingContext) {
>>>>>>> Initial pass - 1/?
        return MiniMessage.get().parse(message, parsingContext.placeholders());
    }

}
