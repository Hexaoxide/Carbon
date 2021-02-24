/*
 * https://github.com/leonardosnt/bungeechannelapi
 *
 * Copyright (C) 2017 leonardosnt <leonrdsnt@gmail.com>
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */

package net.draycia.carbon.bukkit.util;

import java.net.InetSocketAddress;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

/**
 * Documentation copied from https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/
 *
 * @author leonardosnt (leonrdsnt@gmail.com)
 * @see <a href="https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/">https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/</a>
 */
@SuppressWarnings("all")
public class BungeeChannelApi {

  private static final WeakHashMap<Plugin, BungeeChannelApi> registeredInstances = new WeakHashMap<>();

  private final PluginMessageListener messageListener;
  private final Plugin plugin;
  private final Map<String, Queue<CompletableFuture<?>>> callbackMap;

  private Map<String, ForwardConsumer> forwardListeners;
  private ForwardConsumer globalForwardListener;

  /**
   * Get or create new BungeeChannelApi instance
   *
   * @param plugin the plugin instance.
   * @return the BungeeChannelApi instance for the {@code plugin}
   * @throws NullPointerException if the {@code plugin} is {@code null}
   */
  public synchronized static BungeeChannelApi of(Plugin plugin) {
    return registeredInstances.compute(plugin, (k, v) -> {
      if (v == null) v = new BungeeChannelApi(plugin);
      return v;
    });
  }

  public BungeeChannelApi(Plugin plugin) {
    this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
    this.callbackMap = new HashMap<>();

    // Prevent dev's from registering multiple channel listeners,
    // by unregistering the old instance.
    synchronized (registeredInstances) {
      registeredInstances.compute(plugin, (k, oldInstance) -> {
        if (oldInstance != null) oldInstance.unregister();
        return this;
      });
    }

    this.messageListener = this::onPluginMessageReceived;

    Messenger messenger = Bukkit.getServer().getMessenger();
    messenger.registerOutgoingPluginChannel(plugin, "BungeeCord");
    messenger.registerIncomingPluginChannel(plugin, "BungeeCord", messageListener);
  }

  /**
   * Set a global listener for all 'forwarded' messages.
   *
   * @param globalListener the listener
   * @see <a href="https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/#forward">https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/#forward</a>
   */
  public void registerForwardListener(ForwardConsumer globalListener) {
    this.globalForwardListener = globalListener;
  }

  /**
   * Set a listener for all 'forwarded' messages in a specific subchannel.
   *
   * @param channelName the subchannel name
   * @param listener the listener
   * @see <a href="https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/#forward">https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/#forward</a>
   */
  public void registerForwardListener(String channelName, ForwardConsumer listener) {
    if (forwardListeners == null) {
      forwardListeners = new HashMap<>();
    }
    synchronized (forwardListeners) {
      forwardListeners.put(channelName, listener);
    }
  }

  /**
   * Get the amount of players on a certain server, or on ALL the servers.
   *
   * @param serverName the server name of the server to get the player count of, or ALL to get the global player count
   * @return A {@link CompletableFuture} that, when completed, will return
   *         the amount of players on a certain server, or on ALL the servers.
   * @throws IllegalArgumentException if there is no players online.
   */
  public CompletableFuture<Integer> getPlayerCount(String serverName) {
    Player player = getFirstPlayer();
    CompletableFuture<Integer> future = new CompletableFuture<>();

    synchronized (callbackMap) {
      callbackMap.compute("PlayerCount-" + serverName, this.computeQueueValue(future));
    }

    ByteArrayDataOutput output = ByteStreams.newDataOutput();
    output.writeUTF("PlayerCount");
    output.writeUTF(serverName);
    player.sendPluginMessage(this.plugin, "BungeeCord", output.toByteArray());
    return future;
  }

  /**
   * Get a list of players connected on a certain server, or on ALL the servers.
   *
   * @param serverName the name of the server to get the list of connected players, or ALL for global online player list
   * @return A {@link CompletableFuture} that, when completed, will return a
   *         list of players connected on a certain server, or on ALL the servers.
   * @throws IllegalArgumentException if there is no players online.
   */
  public CompletableFuture<List<String>> getPlayerList(String serverName) {
    Player player = getFirstPlayer();
    CompletableFuture<List<String>> future = new CompletableFuture<>();

    synchronized (callbackMap) {
      callbackMap.compute("PlayerList-" + serverName, this.computeQueueValue(future));
    }

    ByteArrayDataOutput output = ByteStreams.newDataOutput();
    output.writeUTF("PlayerList");
    output.writeUTF(serverName);
    player.sendPluginMessage(this.plugin, "BungeeCord", output.toByteArray());
    return future;
  }

  /**
   * Get a list of server name strings, as defined in BungeeCord's config.yml.
   *
   * @return A {@link CompletableFuture} that, when completed, will return a
   *         list of server name strings, as defined in BungeeCord's config.yml.
   * @throws IllegalArgumentException if there is no players online.
   */
  public CompletableFuture<List<String>> getServers() {
    Player player = getFirstPlayer();
    CompletableFuture<List<String>> future = new CompletableFuture<>();

    synchronized (callbackMap) {
      callbackMap.compute("GetServers", this.computeQueueValue(future));
    }

    ByteArrayDataOutput output = ByteStreams.newDataOutput();
    output.writeUTF("GetServers");
    player.sendPluginMessage(this.plugin, "BungeeCord", output.toByteArray());
    return future;
  }

  /**
   * Connects a player to said subserver.
   *
   * @param player the player you want to teleport.
   * @param serverName the name of server to connect to, as defined in BungeeCord config.yml.
   */
  public void connect(Player player, String serverName) {
    ByteArrayDataOutput output = ByteStreams.newDataOutput();
    output.writeUTF("Connect");
    output.writeUTF(serverName);
    player.sendPluginMessage(this.plugin, "BungeeCord", output.toByteArray());
  }

  /**
   * Connect a named player to said subserver.
   *
   * @param playerName name of the player to teleport.
   * @param server name of server to connect to, as defined in BungeeCord config.yml.
   * @throws IllegalArgumentException if there is no players online.
   */
  public void connectOther(String playerName, String server) {
    Player player = getFirstPlayer();
    ByteArrayDataOutput output = ByteStreams.newDataOutput();

    output.writeUTF("ConnectOther");
    output.writeUTF(playerName);
    output.writeUTF(server);

    player.sendPluginMessage(this.plugin, "BungeeCord", output.toByteArray());
  }

  /**
   * Get the (real) IP of a player.
   *
   * @param player The player you wish to get the IP of.
   * @return A {@link CompletableFuture} that, when completed, will return the (real) IP of {@code player}.
   */
  public CompletableFuture<InetSocketAddress> getIp(Player player) {
    CompletableFuture<InetSocketAddress> future = new CompletableFuture<>();

    synchronized (callbackMap) {
      callbackMap.compute("IP", this.computeQueueValue(future));
    }

    ByteArrayDataOutput output = ByteStreams.newDataOutput();
    output.writeUTF("IP");
    player.sendPluginMessage(this.plugin, "BungeeCord", output.toByteArray());
    return future;
  }

  /**
   * Send a message (as in, a chat message) to the specified player.
   *
   * @param playerName the name of the player to send the chat message.
   * @param message the message to send to the player.
   * @throws IllegalArgumentException if there is no players online.
   */
  public void sendMessage(String playerName, String message) {
    Player player = getFirstPlayer();
    ByteArrayDataOutput output = ByteStreams.newDataOutput();

    output.writeUTF("Message");
    output.writeUTF(playerName);
    output.writeUTF(message);
    player.sendPluginMessage(this.plugin, "BungeeCord", output.toByteArray());
  }

  /**
   * Get this server's name, as defined in BungeeCord's config.yml
   *
   * @return A {@link CompletableFuture} that, when completed, will return
   *         the {@code server's} name, as defined in BungeeCord's config.yml.
   * @throws IllegalArgumentException if there is no players online.
   */
  public CompletableFuture<String> getServer() {
    Player player = getFirstPlayer();
    CompletableFuture<String> future = new CompletableFuture<>();

    synchronized (callbackMap) {
      callbackMap.compute("GetServer", this.computeQueueValue(future));
    }

    ByteArrayDataOutput output = ByteStreams.newDataOutput();
    output.writeUTF("GetServer");
    player.sendPluginMessage(this.plugin, "BungeeCord", output.toByteArray());
    return future;
  }

  /**
   * Request the UUID of this player.
   *
   * @param player The player whose UUID you requested.
   * @return A {@link CompletableFuture} that, when completed, will return the UUID of {@code player}.
   */
  public CompletableFuture<String> getUUID(Player player) {
    CompletableFuture<String> future = new CompletableFuture<>();

    synchronized (callbackMap) {
      callbackMap.compute("UUID", this.computeQueueValue(future));
    }

    ByteArrayDataOutput output = ByteStreams.newDataOutput();
    output.writeUTF("UUID");
    player.sendPluginMessage(this.plugin, "BungeeCord", output.toByteArray());
    return future;
  }

  /**
   * Request the UUID of any player connected to the BungeeCord proxy.
   *
   * @param playerName the name of the player whose UUID you would like.
   * @return A {@link CompletableFuture} that, when completed, will return the UUID of {@code playerName}.
   * @throws IllegalArgumentException if there is no players online.
   */
  public CompletableFuture<String> getUUID(String playerName) {
    Player player = getFirstPlayer();
    CompletableFuture<String> future = new CompletableFuture<>();

    synchronized (callbackMap) {
      callbackMap.compute("UUIDOther-" + playerName, this.computeQueueValue(future));
    }

    ByteArrayDataOutput output = ByteStreams.newDataOutput();
    output.writeUTF("UUIDOther");
    output.writeUTF(playerName);
    player.sendPluginMessage(this.plugin, "BungeeCord", output.toByteArray());
    return future;
  }

  /**
   * Request the IP of any server on this proxy.
   *
   * @param serverName the name of the server.
   * @return A {@link CompletableFuture} that, when completed, will return the requested ip.
   * @throws IllegalArgumentException if there is no players online.
   */
  public CompletableFuture<InetSocketAddress> getServerIp(String serverName) {
    Player player = getFirstPlayer();
    CompletableFuture<InetSocketAddress> future = new CompletableFuture<>();

    synchronized (callbackMap) {
      callbackMap.compute("ServerIP-" + serverName, this.computeQueueValue(future));
    }

    ByteArrayDataOutput output = ByteStreams.newDataOutput();
    output.writeUTF("ServerIP");
    output.writeUTF(serverName);
    player.sendPluginMessage(this.plugin, "BungeeCord", output.toByteArray());
    return future;
  }

  /**
   * Kick any player on this proxy.
   *
   * @param playerName the name of the player.
   * @param kickMessage the reason the player is kicked with.
   * @throws IllegalArgumentException if there is no players online.
   */
  public void kickPlayer(String playerName, String kickMessage) {
    Player player = getFirstPlayer();
    CompletableFuture<InetSocketAddress> future = new CompletableFuture<>();

    synchronized (callbackMap) {
      callbackMap.compute("KickPlayer", this.computeQueueValue(future));
    }

    ByteArrayDataOutput output = ByteStreams.newDataOutput();
    output.writeUTF("KickPlayer");
    output.writeUTF(playerName);
    output.writeUTF(kickMessage);
    player.sendPluginMessage(this.plugin, "BungeeCord", output.toByteArray());
  }

  /**
   * Send a custom plugin message to said server. This is one of the most useful channels ever.
   * <b>Remember, the sending and receiving server(s) need to have a player online.</b>
   *
   * @param server the name of the server to send to,
   *        ALL to send to every server (except the one sending the plugin message),
   *        or ONLINE to send to every server that's online (except the one sending the plugin message).
   *
   * @param channelName Subchannel for plugin usage.
   * @param data data to send.
   * @throws IllegalArgumentException if there is no players online.
   */
  public void forward(String server, String channelName, byte[] data) {
    Player player = getFirstPlayer();

    ByteArrayDataOutput output = ByteStreams.newDataOutput();
    output.writeUTF("Forward");
    output.writeUTF(server);
    output.writeUTF(channelName);
    output.writeShort(data.length);
    output.write(data);
    player.sendPluginMessage(this.plugin, "BungeeCord", output.toByteArray());
  }

  /**
   * Send a custom plugin message to specific player.
   *
   * @param playerName the name of the player to send to.
   * @param channelName Subchannel for plugin usage.
   * @param data data to send.
   * @throws IllegalArgumentException if there is no players online.
   */
  public void forwardToPlayer(String playerName, String channelName, byte[] data) {
    Player player = getFirstPlayer();

    ByteArrayDataOutput output = ByteStreams.newDataOutput();
    output.writeUTF("ForwardToPlayer");
    output.writeUTF(playerName);
    output.writeUTF(channelName);
    output.writeShort(data.length);
    output.write(data);
    player.sendPluginMessage(this.plugin, "BungeeCord", output.toByteArray());
  }

  @SuppressWarnings("unchecked")
  private void onPluginMessageReceived(String channel, Player player, byte[] message) {
    if (!channel.equalsIgnoreCase("BungeeCord")) return;

    ByteArrayDataInput input = ByteStreams.newDataInput(message);
    String subchannel = input.readUTF();

    synchronized (callbackMap) {
      Queue<CompletableFuture<?>> callbacks;

      if (subchannel.equals("PlayerCount") || subchannel.equals("PlayerList") ||
        subchannel.equals("UUIDOther") || subchannel.equals("ServerIP")) {
        try {
          String identifier = input.readUTF(); // Server/player name
          callbacks = callbackMap.get(subchannel + "-" + identifier);

          if (callbacks == null || callbacks.isEmpty())  {
            return;
          }

          CompletableFuture<?> callback = callbacks.poll();

          try {
            switch (subchannel) {
              case "PlayerCount":
                ((CompletableFuture<Integer>) callback).complete(input.readInt());
                break;

              case "PlayerList":
                ((CompletableFuture<List<String>>) callback).complete(Arrays.asList(input.readUTF().split(", ")));
                break;

              case "UUIDOther":
                ((CompletableFuture<String>) callback).complete(input.readUTF());
                break;

              case "ServerIP": {
                String ip = input.readUTF();
                int port = input.readUnsignedShort();
                ((CompletableFuture<InetSocketAddress>) callback).complete(new InetSocketAddress(ip, port));
                break;
              }
            }
          } catch(Exception ex) {
            callback.completeExceptionally(ex);
          }
        } catch (final Exception ignored) {
        }

        return;
      }

      callbacks = callbackMap.get(subchannel);

      if (callbacks == null) {
        short dataLength = input.readShort();
        byte[] data = new byte[dataLength];
        input.readFully(data);

        if (globalForwardListener != null) {
          globalForwardListener.accept(subchannel, player, data);
        }

        if (forwardListeners != null) {
          synchronized (forwardListeners) {
            ForwardConsumer listener = forwardListeners.get(subchannel);
            if (listener != null) {
              listener.accept(subchannel, player, data);
            }
          }
        }

        return;
      }

      if (callbacks.isEmpty()) {
        return;
      }

      final CompletableFuture<?> callback = callbacks.poll();

      try {
        switch (subchannel) {
          case "GetServers":
            ((CompletableFuture<List<String>>) callback).complete(Arrays.asList(input.readUTF().split(", ")));
            break;

          case "GetServer":
          case "UUID":
            ((CompletableFuture<String>) callback).complete(input.readUTF());
            break;

          case "IP": {
            String ip = input.readUTF();
            int port = input.readInt();
            ((CompletableFuture<InetSocketAddress>) callback).complete(new InetSocketAddress(ip, port));
            break;
          }

          default:
            break;
        }
      } catch(Exception ex) {
        callback.completeExceptionally(ex);
      }
    }
  }

  /**
   * Unregister message channels.
   */
  public void unregister() {
    Messenger messenger = Bukkit.getServer().getMessenger();
    messenger.unregisterIncomingPluginChannel(plugin, "BungeeCord", messageListener);
    messenger.unregisterOutgoingPluginChannel(plugin);
    callbackMap.clear();
  }

  private BiFunction<String, Queue<CompletableFuture<?>>, Queue<CompletableFuture<?>>> computeQueueValue(CompletableFuture<?> queueValue) {
    return (key, value) -> {
      if (value == null) value = new ArrayDeque<>();
      value.add(queueValue);
      return value;
    };
  }

  private Player getFirstPlayer() {
    /*
     * if Bukkit Version < 1.7.10 then Bukkit.getOnlinePlayers() will return
     * a Player[] otherwise it'll return a Collection<? extends Player>.
     */
    Player firstPlayer = getFirstPlayer0(Bukkit.getOnlinePlayers());

    if (firstPlayer == null) {
      throw new IllegalArgumentException("Bungee Messaging Api requires at least one player to be online.");
    }

    return firstPlayer;
  }

  private Player getFirstPlayer0(Player[] playerArray) {
    return playerArray.length > 0 ? playerArray[0] : null;
  }

  private Player getFirstPlayer0(Collection<? extends Player> playerCollection) {
    return Iterables.getFirst(playerCollection, null);
  }

  @FunctionalInterface
  public interface ForwardConsumer {
    void accept(String channel, Player player, byte[] data);
  }
}
