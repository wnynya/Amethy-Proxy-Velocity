package com.amuject.amethy.proxy;

import com.google.gson.JsonObject;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.api.util.Favicon;
import net.kyori.adventure.text.Component;

import java.io.File;
import java.io.FileInputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.UUID;

public class ProxyServerPing {

  private static HashMap<String, String> favicons = new HashMap<>();
  private final String favicon;
  private final String description;
  private final String versionNumber;
  private final String versionName;
  private final String maxPlayers;
  private final String onlinePlayers;
  private final String samplePlayers;

  public ProxyServerPing(String favicon, String description,
                         String versionNumber, String versionName,
                         String maxPlayers, String onlinePlayers, String samplePlayers) {
    this.favicon = favicon;
    this.description = description;
    this.versionNumber = versionNumber;
    this.versionName = versionName;
    this.maxPlayers = maxPlayers;
    this.onlinePlayers = onlinePlayers;
    this.samplePlayers = samplePlayers;
  }

  public ServerPing buildWith(ServerPing serverPing) {
    ServerPing.Builder b =  ServerPing.builder();

    // Favicon
    if (this.favicon.equals("{default}")) {
      if (serverPing.getFavicon().isPresent()) {
        b.favicon(serverPing.getFavicon().get());
      }
    }
    else {
      b.favicon(new Favicon(getB64(new File(this.favicon))));
    }

    // Description
    if (this.description.equals("{default}")) {
      b.description(serverPing.getDescriptionComponent());
    }
    else {
      String description = this.description;
      b.description(Component.text(description));
    }

    // Version
    String versionNumber = this.versionNumber;
    versionNumber = versionNumber.replace("{default}", String.valueOf(serverPing.getVersion().getProtocol()));
    String versionName = this.versionName;
    versionName = versionName.replace("{default}", serverPing.getVersion().getName());
    ServerPing.Version version = new ServerPing.Version(Integer.parseInt(versionNumber), versionName);
    b.version(version);

    // Players
    ServerPing.Players players = serverPing.getPlayers().get();
    String maxPlayers = this.maxPlayers;
    maxPlayers = maxPlayers.replace("{default}", String.valueOf(players.getMax()));
    b.maximumPlayers(Integer.parseInt(maxPlayers));

    String onlinePlayers = this.onlinePlayers;
    onlinePlayers = onlinePlayers.replace("{default}", String.valueOf(players.getOnline()));
    b.onlinePlayers(Integer.parseInt(onlinePlayers));

    if (this.samplePlayers.equals("{default}")) {
      b.samplePlayers(players.getSample().toArray(new ServerPing.SamplePlayer[0]));
    }
    else {
      String samplePlayers = this.samplePlayers;
      new ServerPing.SamplePlayer(samplePlayers, UUID.randomUUID());
    }

    return b.build();
  }

  public static ProxyServerPing fromJSON(JsonObject data) {
    String favicon = data.get("favicon").getAsString();
    String description = data.get("description").getAsString();
    String versionNumber = data.getAsJsonObject("version").get("number").getAsString();
    String versionName = data.getAsJsonObject("version").get("name").getAsString();
    String maxPlayers = data.getAsJsonObject("players").get("max").getAsString();
    String onlinePlayers = data.getAsJsonObject("players").get("online").getAsString();
    String samplePlayers = data.getAsJsonObject("players").get("sample").getAsString();

    return new ProxyServerPing(favicon, description, versionNumber, versionName, maxPlayers, onlinePlayers, samplePlayers);
  }

  private static String getB64(File file) {
    String key = file.getAbsolutePath();
    String b64 = favicons.get(key);
    if (b64 == null) {
      b64 = file2b64(file);
      favicons.put(key, b64);
    }
    return b64;
  }

  private static String file2b64(File file) {
    String base64 = null;
    if (file.exists() && file.isFile() && file.length() > 0) {
      byte[] src = new byte[(int) file.length()];
      try (FileInputStream fis = new FileInputStream(file)) {
        fis.read(src);
        base64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(src);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    return base64;
  }

  public static void onReload() {
    favicons = new HashMap<>();
  }
}
