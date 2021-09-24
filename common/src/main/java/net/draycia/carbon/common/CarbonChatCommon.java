package net.draycia.carbon.common;

import com.google.inject.Injector;
import net.draycia.carbon.api.CarbonChat;
import net.draycia.carbon.common.command.commands.ContinueCommand;
import net.draycia.carbon.common.command.commands.ReplyCommand;
import net.draycia.carbon.common.command.commands.WhisperCommand;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public abstract class CarbonChatCommon implements CarbonChat {

    public final void initialize(final Injector injector) {
        injector.getInstance(ContinueCommand.class);
        injector.getInstance(ReplyCommand.class);
        injector.getInstance(WhisperCommand.class);
        // lol, bukkit module can't extend Common and it can't call this
    }

}
