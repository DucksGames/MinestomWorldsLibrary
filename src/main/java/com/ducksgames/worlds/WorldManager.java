package com.ducksgames.worlds;

import com.ducksgames.worlds.commands.SetWorldSpawn;
import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.generator.Generator;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class WorldManager {

    private final static Logger LOGGER = LoggerFactory.getLogger(WorldManager.class);

    private final File worldsDirectory;

    private final InstanceManager instanceManager = MinecraftServer.getInstanceManager();

    public WorldManager(File worldsDirectory) {
        this.worldsDirectory = worldsDirectory;
        MinecraftServer.getCommandManager().register(new SetWorldSpawn(this));
    }

    public void shutdown() {
        try {
            saveAll().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    
    public Collection<WorldInstance> worlds() {
        return instanceManager.getInstances().stream()
                .filter(inst -> inst instanceof WorldInstance)
                .map(inst -> (WorldInstance) inst)
                .collect(Collectors.toSet());
    }

    public Optional<WorldInstance> worldByName(String name) {
        return instanceManager.getInstances().stream()
                .filter(inst -> inst instanceof WorldInstance)
                .map(inst -> (WorldInstance) inst)
                .filter(inst -> inst.worldInfo().name().equalsIgnoreCase(name))
                .findFirst();
    }
    public Optional<WorldInstance> worldByInstance(Instance instance) {
        return instanceManager.getInstances().stream()
                .filter(inst -> inst instanceof WorldInstance && inst.equals(instance))
                .map(inst -> (WorldInstance) inst)
                .findFirst();
    }
    
    public CompletableFuture<Void> saveAll() {
        return CompletableFuture.allOf(
                instanceManager.getInstances().stream()
                        .filter(inst -> inst instanceof WorldInstance)
                        .map(inst -> (WorldInstance) inst)
                        .map((wi) -> wi.save()).toArray(CompletableFuture[]::new));
    }

    public void deleteWorld(@NotNull WorldInstance world) {
        instanceManager.unregisterInstance(world);
        world.directory().delete();
    }

    public WorldInstance createWorld(@NotNull String name) {
        return createWorld(name, unit -> {});
    }

    public WorldInstance createWorld(@NotNull String name, @NotNull Generator generator) throws IllegalArgumentException {
        if (worldByName(name).isPresent()) {
            throw new IllegalArgumentException("A world with that name already exists.");
        }

        LOGGER.info("Creating world '" + name + "' in " + worldsDirectory.getName() + " directory.");

        File directory = new File(worldsDirectory, name);
        int index = 0;
        while (directory.exists()) {
            directory = new File(worldsDirectory, name + "-" + (++index));
        }

        WorldInfo info = WorldInfo.of(infoFile(directory));
        info.setName(name);

        WorldInstance instance;
        try {
            instance = new WorldInstance(directory, info);
            instance.setGenerator(generator);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Cannot create world.");
        }
        instance.worldInfo().setGenerator(generator.getClass().getSimpleName());
        instance.save();

        instanceManager.registerInstance(instance);
        return instance;
    }
    
    public WorldInstance loadWorld(@NotNull File directory) throws IllegalArgumentException{
        if (!directory.exists()) {
            throw new IllegalArgumentException("A world with that name does not exist.");
        }

        WorldInfo info = WorldInfo.of(infoFile(directory));
        LOGGER.info("Loading world '" + info.name() + "' from " + worldsDirectory.getName() + " directory.");

        WorldInstance instance;
        try {
            instance = new WorldInstance(directory, info);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Cannot load world.");
        }
        instanceManager.registerInstance(instance);
        return instance;
    }

    public WorldInstance loadWorld(@NotNull String name) throws IllegalArgumentException, IllegalStateException {
        File[] files = worldsDirectory.listFiles();
        if (files == null) {
            throw new IllegalStateException("Worlds directory does not exist.");
        }

        // find by world info
        for (File directory : files) {
            WorldInfo info = WorldInfo.of(infoFile(directory));
            if (info.name().equalsIgnoreCase(name)) {
                return loadWorld(directory);
            }
        }

        // find by directory name
        for (File directory : files) {
            if (directory.getName().equalsIgnoreCase(name)) {
                return loadWorld(directory);
            }
        }

        throw new IllegalArgumentException("A world with that name does not exist.");
    }

    private File infoFile(File worldDirectory) {
        return new File(worldDirectory, "worldinfo.json");
    }
}
