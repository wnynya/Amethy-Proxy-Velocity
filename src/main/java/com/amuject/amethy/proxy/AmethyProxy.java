package com.amuject.amethy.proxy;

import com.amuject.amethy.proxy.commands.AmethyProxyCommand;
import com.amuject.amethy.proxy.listeners.ProxyPing;
import com.google.inject.Inject;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.nio.file.Path;


@Plugin(id = "amethy-proxy", name = "Amethy Proxy Velocity", version = "0.1.0-SNAPSHOT",
  url = "https://amethy.amuject.com/proxy", description = "Amethy Proxy for Velocity", authors = {"Wany"})
public class AmethyProxy {

  private static AmethyProxy plugin;
  private final ProxyServer proxy;
  private final Logger logger;
  private final Path dataDirectory;

  @Inject
  public AmethyProxy(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
    plugin = this;

    this.proxy = proxy;
    this.logger = logger;
    this.dataDirectory = dataDirectory;
  }

  @Subscribe
  public void onInitialize(ProxyInitializeEvent event) {
    this.dataDirectory.toFile().mkdirs();

    CommandManager commandManager = proxy.getCommandManager();
    CommandMeta commandMeta = commandManager.metaBuilder("amethyproxy").aliases("ap").plugin(this).build();
    BrigadierCommand commandToRegister = AmethyProxyCommand.createBrigadierCommand(proxy);
    commandManager.register(commandMeta, commandToRegister);

    EventManager eventManager = proxy.getEventManager();
    eventManager.register(this, new ProxyPing());

    this.onLoad();
  }

  public void onLoad() {
    Console.log("Loading configurations...");

    ProxyStatus.onLoad();
    ProxyHost.onLoad();
  }

  public void onReload() {
    Console.log("Reloading configurations...");

    ProxyStatus.onReload();
    ProxyServerPing.onReload();
    ProxyHost.onReload();
  }

  public static AmethyProxy getPlugin() {
    return plugin;
  }

  public ProxyServer getProxy() {
    return this.proxy;
  }

  public Logger getLogger() {
    return this.logger;
  }

  public Path getDataDirectory() {
    return this.dataDirectory;
  }

}
