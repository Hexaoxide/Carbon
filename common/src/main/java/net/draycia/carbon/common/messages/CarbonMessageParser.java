package net.draycia.carbon.common.messages;

import com.google.inject.Singleton;
import com.proximyst.moonshine.message.IMessageParser;
import com.proximyst.moonshine.message.ParsingContext;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

@Singleton
public class CarbonMessageParser implements IMessageParser<String, Component, Audience> {

    public Component parse(final String message, final ParsingContext<Audience> parsingContext) {
        return MiniMessage.get().parse(message, parsingContext.placeholders());
    }

}
