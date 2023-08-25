package com.ducksgames.worlds;

public enum WorldLoader {

    NOT_IMPLEMENTED(0),
    POLAR(1),
    ANVIL(2),
    TNT(3),
    SLIME(4),
    ;

    private Integer id;
    WorldLoader(Integer id) {
        this.id = id;
    }

    public Integer id() {return id;}

    public static WorldLoader fromId(Integer id) {
        return WorldLoader.values()[id];
    }

}
