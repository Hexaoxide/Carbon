package net.draycia.carbon.paper.internalsaccessor.impl;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.brigadier.argument.WrappedBrigadierParser;
import com.google.inject.Inject;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import net.draycia.carbon.common.command.CarbonCommand;
import net.draycia.carbon.common.command.CommandSettings;
import net.draycia.carbon.common.command.Commander;
import net.kyori.adventure.key.Key;
import net.minecraft.commands.CommandSigningContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.players.PlayerList;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class SignedArgumentPaper extends CarbonCommand {

    private final CommandManager<Commander> commandManager;

    @Inject
    public SignedArgumentPaper(
        final CommandManager<Commander> commandManager
    ) {
        this.commandManager = commandManager;
    }

    @Override
    public void init() {
        this.commandManager.command(
            this.commandManager.commandBuilder("test_signed")
                .argument(this.commandManager.argumentBuilder(MessageArgument.Message.class, "message").withParser(message()))
                .handler(ctx -> {
                    try {
                        resolveChatMessage(ctx.get("message"), ctx.get(WrappedBrigadierParser.COMMAND_CONTEXT_BRIGADIER_NATIVE_SENDER), "message", msg -> {
                            final CommandSourceStack commandSourceStack = ctx.get(WrappedBrigadierParser.COMMAND_CONTEXT_BRIGADIER_NATIVE_SENDER);
                            PlayerList playerList = commandSourceStack.getServer().getPlayerList();
                            playerList.broadcastChatMessage(msg, commandSourceStack, ChatType.bind(ChatType.SAY_COMMAND, commandSourceStack));
                        });
                    } catch (final Exception ex) {
                        throw new RuntimeException(ex);
                    }
                })
        );
    }

    @Override
    protected CommandSettings _commandSettings() {
        return new CommandSettings("signed_test");
    }

    @Override
    public Key key() {
        return Key.key("carbon", "signed_test");
    }

    public static void resolveChatMessage(MessageArgument.Message message, CommandSourceStack commandSourceStack, String string, Consumer<PlayerChatMessage> consumer) throws Exception {
        final Method resolveComponent = message.getClass().getDeclaredMethod("resolveComponent", CommandSourceStack.class);
        resolveComponent.setAccessible(true);
        Component component = (Component) resolveComponent.invoke(message, commandSourceStack);
        CommandSigningContext commandSigningContext = commandSourceStack.getSigningContext();
        @Nullable PlayerChatMessage playerChatMessage = commandSigningContext.getArgument(string);
        if (playerChatMessage != null) {
            final Method resolveSignedMessage = MessageArgument.class.getDeclaredMethod("resolveSignedMessage", Consumer.class, CommandSourceStack.class, PlayerChatMessage.class);
            resolveSignedMessage.setAccessible(true);
            resolveSignedMessage.invoke(null, consumer, commandSourceStack, playerChatMessage.withUnsignedContent(component));
        } else {
            final Method resolveDisguisedMessage = MessageArgument.class.getDeclaredMethod("resolveDisguisedMessage", Consumer.class, CommandSourceStack.class, PlayerChatMessage.class);
            resolveDisguisedMessage.setAccessible(true);
            resolveDisguisedMessage.invoke(null, consumer, commandSourceStack, PlayerChatMessage.system(message.getText()).withUnsignedContent(component));
        }
    }

    public static <C> @NonNull ArgumentParser<C, MessageArgument.Message> message() {
        return new WrappedBrigadierParser<>(MessageArgument::message);
    }

}
