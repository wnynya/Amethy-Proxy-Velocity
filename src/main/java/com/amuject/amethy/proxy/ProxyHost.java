package com.amuject.amethy.proxy;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ProxyHost {

  public static List<ProxyHost> proxyHosts = new ArrayList<>();

  private final List<String> hosts;
  private final List<RegisteredServer> serversPing;
  private final ProxyServerPing pingOpened;
  private final ProxyServerPing pingClosed;

  public ProxyHost(List<String> hosts,
                   List<RegisteredServer> serversPing,
                   ProxyServerPing pingOpened, ProxyServerPing pingClosed) {
    this.hosts = hosts;
    this.serversPing = serversPing;
    this.pingOpened = pingOpened;
    this.pingClosed = pingClosed;
  }

  public static ProxyHost fromJSON(JsonObject data) {
    // Hosts
    List<String> hosts = new ArrayList<>();
    JsonArray hostsData = data.get("hosts").getAsJsonArray();
    for (JsonElement host : hostsData.asList()) {
      String hostname = host.getAsString().toUpperCase();
      hosts.add(hostname);
    }

    List<RegisteredServer> serversPing = new ArrayList<>();
    JsonArray serversPingData = data.getAsJsonObject("servers").getAsJsonArray("ping");
    for (JsonElement server : serversPingData.asList()) {
      if (AmethyProxy.getPlugin().getProxy().getServer(server.getAsString()).isPresent()) {
        RegisteredServer registeredServer = AmethyProxy.getPlugin().getProxy().getServer(server.getAsString()).get();
        serversPing.add(registeredServer);
      }
    }

    ProxyServerPing pingOpened = ProxyServerPing.fromJSON(data.getAsJsonObject("ping").getAsJsonObject("opened"));
    ProxyServerPing pingClosed = ProxyServerPing.fromJSON(data.getAsJsonObject("ping").getAsJsonObject("closed"));

    return new ProxyHost(hosts, serversPing, pingOpened, pingClosed);
  }

  public static ProxyHost fromHost(String host) {
    for (ProxyHost vh : proxyHosts) {
      if (vh.hosts.contains(host)) {
        return vh;
      }
    }
    return null;
  }

  public static void onProxyPing(ProxyPingEvent event) {
    if (event.getConnection().getVirtualHost().isEmpty()) {
      return;
    }
    String hostName = event.getConnection().getVirtualHost().get().getHostName();
    int port = event.getConnection().getVirtualHost().get().getPort();
    String host = (hostName + ":" + port).toUpperCase();

    ProxyHost ph = fromHost(host);
    if (ph == null) {
      return;
    }

    boolean opened = false;
    for (RegisteredServer server : ph.serversPing) {
      if (ProxyStatus.isOnline(server.getServerInfo().getName())) {
        opened = true;
        break;
      }
    }

    if (opened) {
      event.setPing(ph.pingOpened.buildWith(event.getPing()));
    } else {
      event.setPing(ph.pingClosed.buildWith(event.getPing()));
    }
  }

  public static void init() {
    File dir = AmethyProxy.getPlugin().getDataDirectory().resolve("hosts").toFile();
    dir.mkdirs();

    File[] files = dir.listFiles();
    if (files == null) {
      return;
    }

    for (File file : files) {
      Console.log("Loading virtual host... [" + file.getName() + "]");
      if (file.isFile() && file.getName().endsWith(".json")) {
        try {
          JsonElement json = JsonParser.parseString(Files.readString(file.toPath()));
          JsonObject data = json.getAsJsonObject();
          ProxyHost ph = fromJSON(data);
          proxyHosts.add(ph);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  public static void onLoad() {
    init();
  }

  public static void onReload() {
    proxyHosts = new ArrayList<>();
    init();
  }
}
