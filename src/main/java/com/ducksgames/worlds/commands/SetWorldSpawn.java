package com.ducksgames.worlds.commands;

import com.ducksgames.worlds.WorldInstance;
import com.ducksgames.worlds.WorldManager;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.entity.Player;

public class SetWorldSpawn extends Command {

    private final WorldManager manager;

    public SetWorldSpawn(WorldManager manager) {
        super("setworldspawn");
        this.manager = manager;
        setCondition((sender, commandString) -> sender instanceof Player && sender.hasPermission("lobby.setworldspawn"));
        setDefaultExecutor(this::onCommand);
    }

    public void onCommand(CommandSender sender, CommandContext context) {
        if (sender instanceof Player p) {
            WorldInstance instance = manager.worldByInstance(p.getInstance()).get();
            if (instance != null) {
                p.sendMessage("You have set the new world spawn");
                instance.worldInfo().setSpawn(p.getPosition());
            }
        } else sender.sendMessage("Player only command");
    }

}
