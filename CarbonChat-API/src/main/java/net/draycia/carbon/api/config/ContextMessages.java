package net.draycia.carbon.api.config;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public final class ContextMessages {

  @Setting private @NonNull String vaultBalanceNotEnough = "<red>You do not have enough balance, required: <balance><red>.";
  @Setting private @NonNull String vaultCostNotEnough = "<red>You do not have enough balance, cost: <balance><red>.";
  @Setting private @NonNull String mcmmoPartyNotInParty = "<red>You are not in a party!";
  @Setting private @NonNull String townyTownNotInTown = "<red>You are not in a town!";

  public String vaultBalanceNotEnough() {
    return this.vaultBalanceNotEnough;
  }

  public String vaultCostNotEnough() {
    return this.vaultCostNotEnough;
  }

  public String mcmmoPartyNotInParty() {
    return this.mcmmoPartyNotInParty;
  }

  public String townyTownNotInTown() {
    return this.townyTownNotInTown;
  }

}
