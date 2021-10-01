package net.draycia.carbon.common.command.commands;

import cloud.commandframework.CommandManager;
import cloud.commandframework.arguments.compound.FlagArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.permission.Permission;
import com.google.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.draycia.carbon.api.users.CarbonPlayer;
import net.draycia.carbon.common.command.Commander;
import net.draycia.carbon.common.command.PlayerCommander;
import net.draycia.carbon.common.command.arguments.CarbonPlayerArgument;
import net.draycia.carbon.common.command.arguments.OptionValueParser;
import net.draycia.carbon.common.messages.CarbonMessageService;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

public class NicknameCommand {

    @Inject
    public NicknameCommand(
        final CommandManager<Commander> commandManager,
        final CarbonMessageService messageService,
        final CarbonPlayerArgument carbonPlayerArgument
    ) {
        final var nicknameArgument = FlagArgument.<Commander, String>ofType(String.class, "value")
            .withParser(new OptionValueParser<>())
            .asOptional()
            .build();

        var command = commandManager.commandBuilder("nickname", "nick")
            .flag(commandManager.flagBuilder("duration")
                .withAliases("d")
                .withArgument(StringArgument.optional("duration"))
                .withPermission(Permission.of("carbon.nickname.duration"))
            )
            .flag(commandManager.flagBuilder("player")
                .withAliases("p")
                .withArgument(carbonPlayerArgument.newInstance(true, "recipient"))
                .withPermission(Permission.of("carbon.nickname.others"))
            )
            .flag(commandManager.flagBuilder("nickname")
                .withAliases("n")
                .withArgument(nicknameArgument)
                .withPermission(Permission.of("carbon.nickname.set"))
            )
            .permission("carbon.nickname.self")
            .senderType(PlayerCommander.class)
            .handler(handler -> {
                CarbonPlayer sender = ((PlayerCommander)handler.getSender()).carbonPlayer();
                long expirationTime = -1; // TODO: implement timed nicknames
                @MonotonicNonNull String durationFormat = null;

                if (handler.flags().contains("duration")) {
                    expirationTime = this.parsePeriod(handler.flags().get("duration"));
                    durationFormat = this.formatMillisDuration(expirationTime);
                }

                // Setting nickname
                if (handler.flags().contains("nickname")) {
                    final var nickname = MiniMessage.get().parse(handler.flags().get("nickname"));

                    final @MonotonicNonNull CarbonPlayer target = handler.flags().get("player");

                    // Setting other player's nickname
                    if (target != null && !target.equals(sender)) {
                        if (expirationTime > -1) {
                            target.temporaryDisplayName(nickname, expirationTime);
                            messageService.temporaryNicknameSet(target, nickname, durationFormat);
                            messageService.temporaryNicknameSetOthers(sender, target.username(), nickname, durationFormat);
                        } else {
                            target.displayName(nickname);
                            messageService.nicknameSet(target, nickname);
                            messageService.nicknameSetOthers(sender, target.username(), nickname);
                        }
                    } else {
                        // Setting own nickname
                        if (expirationTime > -1) {
                            sender.temporaryDisplayName(nickname, expirationTime);
                            messageService.temporaryNicknameSet(sender, nickname, durationFormat);
                        } else {
                            sender.displayName(nickname);
                            messageService.nicknameSet(sender, nickname);
                        }
                    }
                } else if (handler.flags().contains("player")) {
                    // Checking other player's nickname
                    final CarbonPlayer target = handler.flags().get("player");

                    if (target.temporaryDisplayName() != null) {
                        messageService.temporaryNicknameShowOthers(sender, target.username(),target.displayName(),
                            this.formatMillisDuration(target.temporaryDisplayNameExpiration()));
                    } else if (target.displayName() != null) {
                        messageService.nicknameShowOthers(sender, target.username(), target.displayName());
                    } else {
                        messageService.nicknameShowOthersUnset(sender, target.username());
                    }
                } else {
                    // Checking own nickname
                    if (sender.temporaryDisplayName() != null) {
                        messageService.temporaryNicknameShow(sender, sender.username(),sender.displayName(),
                            this.formatMillisDuration(sender.temporaryDisplayNameExpiration()));
                    } else if (sender.displayName() != null){
                        messageService.nicknameShow(sender, sender.username(), sender.displayName());
                    } else {
                        messageService.nicknameShowUnset(sender, sender.username());
                    }
                }
            })
            .build();

        commandManager.command(command);
    }

    // https://stackoverflow.com/a/56395975
    private final Pattern periodPattern = Pattern.compile("([0-9]+)([smhdWMY])");

    private Long parsePeriod(@MonotonicNonNull String period) {
        period = Objects.requireNonNull(period).toLowerCase(Locale.ENGLISH);

        final Matcher matcher = periodPattern.matcher(period);
        Instant instant = Instant.now();

        while (matcher.find()) {
            final int num = Integer.parseInt(matcher.group(1));
            final String typ = matcher.group(2);

            switch (typ) {
                case "s" -> instant = instant.plus(Duration.ofSeconds(num));
                case "m" -> instant = instant.plus(Duration.ofMinutes(num));
                case "h" -> instant = instant.plus(Duration.ofHours(num));
                case "d" -> instant = instant.plus(Duration.ofDays(num));
                case "W" -> instant = instant.plus(Period.ofWeeks(num));
                case "M" -> instant = instant.plus(Period.ofMonths(num));
                case "Y" -> instant = instant.plus(Period.ofYears(num));
            }
        }

        return instant.toEpochMilli();
    }

    private final List<ChronoUnit> temporalUnits = List.of(ChronoUnit.YEARS, ChronoUnit.MONTHS, ChronoUnit.WEEKS,
        ChronoUnit.DAYS, ChronoUnit.HOURS, ChronoUnit.MINUTES, ChronoUnit.SECONDS);

    private String formatMillisDuration(final long millisSinceEpoch) {
        final var millisBetween = millisSinceEpoch - System.currentTimeMillis();
        var period = Period.from(Duration.ofMillis(millisBetween)); // TODO: this doesn't work
        var stringJoiner = new StringJoiner(", ");

        for (final var temporalUnit : temporalUnits) {
            final var count = period.get(temporalUnit);

            // Check if == 0 instead of > 0 in case the millisSinceEpoch refers to the past
            // !! the distinction is important
            if (count != 0) {
                period = switch (temporalUnit) {
                    case YEARS -> period.minusYears(count);
                    case MONTHS -> period.minusMonths(count);
                    case WEEKS -> period.minus(Period.ofWeeks((int)count));
                    case DAYS -> period.minusDays(count);
                    case HOURS, MINUTES, SECONDS -> period.minus(Duration.of(count, temporalUnit));
                    default -> period;
                };

                stringJoiner.add(String.format("%d %s", count, temporalUnit));
            }
        }

        final var suffix = millisBetween > 0 ? "" : " ago";

        return stringJoiner + suffix;
    }

}
