package com.amuject.amethy.proxy;

public class Console {

  public static void log(String string) {
    AmethyProxy.getPlugin().getLogger().info(string);
  }

}
