# MinestomWorldsLibrary

You can find this library @ [https://jitpack.io/#DucksGames/MinestomWorldsLibrary](https://jitpack.io/#DucksGames/MinestomWorldsLibrary)

### IMPORTANT
Currently this Library uses a hard fork of Polar, this removes all support for minestom-ce, this will be changed in the future and minestom-ce will be supported later, but for right now it only supports the official version of Minestom

### Quick Start
```Java
WorldManager worldManager = new WorldManager(new File("worlds"));
WorldInstance world = worldManager.createOrLoad("world", WorldLoader.POLAR, created -> created.worldInfo().setSpawn(new Pos(0, 22, 0)));
```

