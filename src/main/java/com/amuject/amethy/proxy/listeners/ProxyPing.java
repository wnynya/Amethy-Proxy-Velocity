package com.amuject.amethy.proxy.listeners;

import com.amuject.amethy.proxy.ProxyHost;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;

public class ProxyPing {

  @Subscribe(order = PostOrder.EARLY)
  public void onEvent(ProxyPingEvent event) {
    ProxyHost.onProxyPing(event);
  }

}
