package net.draycia.carbon.api.channels;

import net.draycia.carbon.api.config.ChannelOptions;
import net.draycia.carbon.api.users.CarbonUser;
import net.draycia.carbon.api.users.PlayerUser;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.luckperms.api.model.group.Group;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class EmptyTextChannel implements TextChannel {

  @Override
  public @NonNull List<@NonNull PlayerUser> audiences() {
    return Collections.emptyList();
  }

  @Override
  public @NonNull ChannelOptions options() {
    return ChannelOptions.defaultChannel();
  }

  @Override
  public void options(final @NonNull ChannelOptions options) {

  }

  @Override
  public @Nullable String format(final @NonNull Group group) {
    return null;
  }

  @Override
  public @Nullable String format(final @NonNull String group) {
    return null;
  }

  @Override
  public boolean isDefault() {
    return true;
  }

  @Override
  public boolean crossServer() {
    return false;
  }

  @Override
  public @Nullable String messagePrefix() {
    return null;
  }

  @Override
  public @NonNull List<String> aliases() {
    return Collections.emptyList();
  }

  @Override
  public boolean primaryGroupOnly() {
    return false;
  }

  @Override
  public boolean permissionGroupMatching() {
    return false;
  }

  @Override
  public @NonNull List<@NonNull String> groupOverrides() {
    return Collections.emptyList();
  }

  @Override
  public void sendComponent(final @NonNull PlayerUser user, final @NonNull Component component) {

  }

  @Override
  public @NonNull TextColor channelColor(final @NonNull CarbonUser user) {
    return NamedTextColor.WHITE;
  }

  @Override
  public @NonNull Map<CarbonUser, Component> parseMessage(final @NonNull PlayerUser user, final @NonNull String message, final boolean fromRemote) {
    return Collections.emptyMap();
  }

  @Override
  public @NonNull Map<CarbonUser, Component> parseMessage(final @NonNull PlayerUser user, final @NonNull Collection<@NonNull PlayerUser> recipients, final @NonNull String message, final boolean fromRemote) {
    return Collections.emptyMap();
  }

  @Override
  public boolean canPlayerUse(final @NonNull PlayerUser user) {
    return false;
  }

  @Override
  public boolean canPlayerSee(final @NonNull PlayerUser sender, final @NonNull PlayerUser target, final boolean checkSpying) {
    return false;
  }

  @Override
  public boolean canPlayerSee(final @NonNull PlayerUser target, final boolean checkSpying) {
    return false;
  }

  @Override
  public void sendComponents(final @NonNull Identity identity, final @NonNull Map<? extends CarbonUser, Component> components) {

  }

  @Override
  public void sendComponentsAndLog(final @NonNull Identity identity, final @NonNull Map<? extends CarbonUser, Component> components) {

  }

  @Override
  public @NonNull String name() {
    return "default";
  }

  @Override
  public @NonNull String key() {
    return "default";
  }

  @Override
  public boolean ignorable() {
    return false;
  }

  @Override
  public @NonNull List<@NonNull Pattern> itemLinkPatterns() {
    return Collections.emptyList();
  }

  @Override
  public @NonNull String switchMessage() {
    return "";
  }

  @Override
  public @NonNull String switchOtherMessage() {
    return "";
  }

  @Override
  public @NonNull String switchFailureMessage() {
    return "";
  }

  @Override
  public @NonNull String cannotIgnoreMessage() {
    return "";
  }

  @Override
  public @NonNull String toggleOffMessage() {
    return "";
  }

  @Override
  public @NonNull String toggleOnMessage() {
    return "";
  }

  @Override
  public @NonNull String toggleOtherOnMessage() {
    return "";
  }

  @Override
  public @NonNull String toggleOtherOffMessage() {
    return "";
  }

  @Override
  public @NonNull String cannotUseMessage() {
    return "";
  }
}
