package com.amuject.amethy.proxy;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerPing;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class ProxyStatus {

  private static HashMap<String, ProxyStatus> cache = new HashMap<>();
  private static Timer timer;
  private final RegisteredServer registeredServer;
  private ServerPing serverPing = null;
  private boolean online = false;


  private ProxyStatus(RegisteredServer registeredServer) {
    this.registeredServer = registeredServer;
  }

  private void check() {
    try {
      this.registeredServer.ping().whenCompleteAsync((sp, throwable) -> {
        if (throwable != null) {
          this.serverPing = null;
          this.online = false;
        }
        else {
          this.serverPing = sp;
          this.online = true;
        }
      });
    } catch (Exception e) {
      this.serverPing = null;
      this.online = false;
    }
  }

  public boolean isOnline() {
    return online;
  }

  public ServerPing getServerPing() {
    return this.serverPing;
  }

  public static boolean isOnline(String serverName) {
    ProxyStatus ps = cache.get(serverName);
    if (ps == null) {
      return false;
    }
    return ps.isOnline();
  }

  private static void init() {
    for (RegisteredServer server : AmethyProxy.getPlugin().getProxy().getAllServers()) {
      ProxyStatus ps = new ProxyStatus(server);
      cache.put(server.getServerInfo().getName(), ps);
    }

    timer = new Timer();

    timer.schedule( new TimerTask() {
      public void run() {
        for (ProxyStatus ps : cache.values()) {
          ps.check();
        }
      }
    }, 0, 2 * 1000);
  }

  public static void onLoad() {
    init();
  }

  public static void onReload() {
    timer.cancel();
    cache = new HashMap<>();
    init();
  }
}
