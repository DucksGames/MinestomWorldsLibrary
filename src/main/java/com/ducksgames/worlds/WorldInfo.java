package com.ducksgames.worlds;

import com.google.gson.*;
import net.minestom.server.coordinate.Pos;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;

public class WorldInfo {


    public final static Logger LOGGER = LoggerFactory.getLogger(WorldInfo.class);
    private final static Gson gson = new GsonBuilder()
            .registerTypeAdapter(Pos.class, new PosSerializer())
            .registerTypeAdapter(WorldLoader.class, new WorldLoaderSerializer())
            .enableComplexMapKeySerialization()
            .create();

    private transient File file;

    private String name;
    private String generator;
    private List<Object> generatorArgs;
    private List<String> authors;
    private HashMap<Object, Object> pluginInfo = new HashMap<>();
    private Pos spawn;
    private WorldLoader loader;

    private WorldInfo() {}

    public String name() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String generator() {
        return generator;
    }

    public List<Object> generatorAgs() {
        return generatorArgs;
    }

    public List<String> authors() {
        return authors;
    }

    public HashMap<Object, Object> pluginInfo() {
        return pluginInfo;
    }

    public void pluginInfo(Object objOne, Object objTwo) {
        pluginInfo.put(objOne, objTwo);
    }

    public void setGenerator(String generator) {
        this.generator = generator;
    }

    public void setGenerator(String generator, List<Object> generatorArgs) {
        setGenerator(generator);
        this.generatorArgs = generatorArgs;
    }

    public WorldLoader worldLoader() {return loader;}

    public void setWorldLoader(WorldLoader loader) {
        this.loader = loader;
    }

    public Pos spawn() {
        return spawn;
    }

    public void setSpawn(Pos pos) {
        this.spawn = pos;
    }

    public void save() {
        try {
            if (!file.exists()) file.createNewFile();

            var writer = Files.newBufferedWriter(file.toPath());
            gson.toJson(this, writer);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static WorldInfo of(@NotNull File file) {
        WorldInfo info = null;
        if (file.exists()) {
            try {
                var reader = Files.newBufferedReader(file.toPath());
                info = gson.fromJson(reader, WorldInfo.class);
                reader.close();
            } catch (Exception exception ) {
                exception.printStackTrace();
            }
        }

        if (info == null) info = new WorldInfo();
        if (info.name == null) info.name = file.getParentFile().getName();
        if (info.loader == null) info.loader = WorldLoader.ANVIL;

        info.file = file;
        return info;
    }

    public static class PosSerializer implements JsonSerializer<Pos>, JsonDeserializer<Pos> {

        @Override
        public Pos deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            var obj = json.getAsJsonObject();

            var x = obj.get("x").getAsDouble();
            var y = obj.get("y").getAsDouble();
            var z = obj.get("z").getAsDouble();
            var yaw = obj.get("yaw").getAsFloat();
            var pitch = obj.get("pitch").getAsFloat();
            return new Pos(x,y,z,yaw,pitch);
        }

        @Override
        public JsonElement serialize(Pos src, Type typeOfSrc, JsonSerializationContext context) {
            var obj = new JsonObject();
            obj.add("x", new JsonPrimitive((src.x())));
            obj.add("y", new JsonPrimitive((src.y())));
            obj.add("z", new JsonPrimitive((src.z())));
            obj.add("yaw", new JsonPrimitive((src.yaw())));
            obj.add("pitch", new JsonPrimitive((src.pitch())));
            return obj;
        }

    }

    private static class WorldLoaderSerializer implements JsonSerializer<WorldLoader>, JsonDeserializer<WorldLoader> {
        @Override
        public WorldLoader deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return WorldLoader.fromId(json.getAsInt());
        }

        @Override
        public JsonElement serialize(WorldLoader src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.id());
        }
    }


}
