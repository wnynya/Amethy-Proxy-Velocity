package com.amuject.amethy.proxy.commands;

import com.amuject.amethy.proxy.AmethyProxy;
import com.amuject.amethy.proxy.ProxyHost;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;

public class AmethyProxyCommand {
  public static BrigadierCommand createBrigadierCommand(final ProxyServer proxy) {
    LiteralCommandNode<CommandSource> helloNode = BrigadierCommand.literalArgumentBuilder("amethyproxy")
      .requires(source -> source.hasPermission("amethyproxy.command"))
      .then(BrigadierCommand.requiredArgumentBuilder("argument", StringArgumentType.word())
        .suggests((ctx, builder) -> {
          builder.suggest("reload");
          return builder.buildFuture();
        })
        .executes(context -> {
          String argumentProvided = context.getArgument("argument", String.class);
          if (argumentProvided.equals("reload")) {
            AmethyProxy.getPlugin().onReload();
            return Command.SINGLE_SUCCESS;
          }
          else {
            return 0;
          }
        })
      )
      .build();
    return new BrigadierCommand(helloNode);
  }
}
