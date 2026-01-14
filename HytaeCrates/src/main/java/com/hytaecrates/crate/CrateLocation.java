package com.hytaecrates.crate;

import java.util.Objects;

/**
 * Represents a location in the world where a crate is placed.
 */
public class CrateLocation {

    private final String worldName;
    private final int x;
    private final int y;
    private final int z;

    public CrateLocation(String worldName, int x, int y, int z) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Creates a CrateLocation from a serialized string (world:x:y:z).
     */
    public static CrateLocation fromString(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        String[] parts = str.split(":");
        if (parts.length != 4) {
            return null;
        }
        try {
            return new CrateLocation(
                    parts[0],
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[3])
            );
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Serializes this location to a string (world:x:y:z).
     */
    @Override
    public String toString() {
        return worldName + ":" + x + ":" + y + ":" + z;
    }

    /**
     * Gets a formatted display string for this location.
     */
    public String toDisplayString() {
        return worldName + " (" + x + ", " + y + ", " + z + ")";
    }

    // Getters
    public String getWorldName() {
        return worldName;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    /**
     * Checks if this location matches the given coordinates.
     */
    public boolean matches(String world, int x, int y, int z) {
        return this.worldName.equals(world)
                && this.x == x
                && this.y == y
                && this.z == z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CrateLocation that = (CrateLocation) o;
        return x == that.x && y == that.y && z == that.z && Objects.equals(worldName, that.worldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldName, x, y, z);
    }
}
