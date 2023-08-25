package com.ducksgames.worlds;

import com.ducksgames.worlds.polar.PolarLoader;
import dev.emortal.tnt.TNTLoader;
import dev.emortal.tnt.source.FileTNTSource;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.AnvilLoader;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.generator.Generator;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static java.io.File.separator;

public class WorldInstance extends InstanceContainer {

    private final static Logger LOGGER = LoggerFactory.getLogger(WorldInstance.class);

    private final File directory;

    private final WorldInfo worldInfo;

    public WorldInstance(@NotNull File directory, WorldInfo worldInfo) throws IOException, NBTException {
        super(UUID.randomUUID(), DimensionType.OVERWORLD);

        switch (worldInfo.worldLoader()) {
            case POLAR -> setChunkLoader(new PolarLoader(Path.of(directory.getPath() + separator + worldInfo.name() + ".polar")));
            case TNT -> {
                File temp = new File(directory.getPath() + separator + worldInfo.name() + ".tnt");
                if (!temp.exists()) temp.createNewFile();
                setChunkLoader(new TNTLoader(new FileTNTSource(Path.of(directory.getPath() + separator + worldInfo.name() + ".tnt")))); // TODO
            }
            case SLIME -> setChunkLoader(null); // TODO
            default -> setChunkLoader(new AnvilLoader(Path.of(directory.getPath() + separator + worldInfo.name())));
        }

        enableAutoChunkLoad(true);

        this.directory = directory;
        this.worldInfo = worldInfo;
        // TODO apply generator from worldinfo file

        if ( !directory.exists() ) {
            directory.mkdirs();
            save();
        }
    }

    public WorldInfo worldInfo() {
        return worldInfo;
    }

    public File directory() {
        return directory;
    }

    public CompletableFuture<Void> save() {
        LOGGER.info(String.format("Saving %s...", worldInfo.name()));
        long millis = System.currentTimeMillis();
        return CompletableFuture.allOf(
                saveInstance(),
                saveChunksToStorage(),
                CompletableFuture.runAsync(worldInfo::save)
        ).thenRun(() -> {
            float time = (System.currentTimeMillis() - millis) / 1000f;
            LOGGER.info(String.format("Saved %s in %.2fs.", worldInfo.name(), time));
        });
    }

    public void teleport(Player player) {
        Pos spawn = worldInfo().spawn();
        if (spawn == null) {
            spawn = new Pos(0, 1, 0);
        }

        loadChunk(spawn).join();

        while (!getBlock(spawn).isAir()
                || !getBlock(spawn.add(0, 1, 0)).isAir()) {
            spawn = spawn.add(0, 2, 0);
        }

        if (player.getInstance() != this) {
            player.setInstance(this);
        }
        player.teleport(spawn);
    }

}
