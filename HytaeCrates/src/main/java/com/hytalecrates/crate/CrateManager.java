package com.hytalecrates.crate;

import com.google.gson.reflect.TypeToken;
import com.hytalecrates.CratesPlugin;
import com.hytalecrates.config.CrateConfig;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;

/**
 * Manages all crates in the plugin - registration, locations, and lookups.
 */
public class CrateManager {

    private final CratesPlugin plugin;
    private final Map<String, Crate> crates;
    private final Map<CrateLocation, String> locationToCrate;
    private final File locationsFile;

    public CrateManager(CratesPlugin plugin) {
        this.plugin = plugin;
        this.crates = new HashMap<>();
        this.locationToCrate = new HashMap<>();
        this.locationsFile = new File(plugin.getDataFolder(), "locations.json");
    }

    /**
     * Loads all crates from configuration.
     */
    public void loadCrates() {
        crates.clear();
        locationToCrate.clear();

        Map<String, CrateConfig> configs = plugin.getConfigManager().getCrateConfigs();
        for (CrateConfig config : configs.values()) {
            Crate crate = new Crate(config);
            crates.put(crate.getId(), crate);

            // Register the key for this crate
            plugin.getKeyManager().registerKey(crate);
        }

        // Load saved locations
        loadCrateLocations();

        plugin.getLogger().info("Loaded " + crates.size() + " crate(s).");
    }

    /**
     * Gets a crate by its ID.
     */
    public Optional<Crate> getCrate(String id) {
        return Optional.ofNullable(crates.get(id));
    }

    /**
     * Gets the crate at a specific location.
     */
    public Optional<Crate> getCrateAt(String world, int x, int y, int z) {
        CrateLocation location = new CrateLocation(world, x, y, z);
        String crateId = locationToCrate.get(location);
        if (crateId != null) {
            return getCrate(crateId);
        }
        return Optional.empty();
    }

    /**
     * Gets the crate at a specific location.
     */
    public Optional<Crate> getCrateAt(CrateLocation location) {
        String crateId = locationToCrate.get(location);
        if (crateId != null) {
            return getCrate(crateId);
        }
        return Optional.empty();
    }

    /**
     * Sets a block location as a crate.
     */
    public boolean setCrateLocation(String crateId, CrateLocation location) {
        Optional<Crate> crateOpt = getCrate(crateId);
        if (crateOpt.isEmpty()) {
            return false;
        }

        // Remove any existing crate at this location
        removeCrateLocation(location);

        // Add the new crate location
        Crate crate = crateOpt.get();
        crate.addLocation(location);
        locationToCrate.put(location, crateId);

        // Save locations
        saveCrateLocations();

        plugin.getLogger().info("Set crate " + crateId + " at " + location.toDisplayString());
        return true;
    }

    /**
     * Removes a crate from a location.
     */
    public boolean removeCrateLocation(CrateLocation location) {
        String crateId = locationToCrate.remove(location);
        if (crateId != null) {
            Optional<Crate> crateOpt = getCrate(crateId);
            crateOpt.ifPresent(crate -> crate.removeLocation(location));
            saveCrateLocations();
            plugin.getLogger().info("Removed crate from " + location.toDisplayString());
            return true;
        }
        return false;
    }

    /**
     * Checks if a location has a crate.
     */
    public boolean isCrateLocation(CrateLocation location) {
        return locationToCrate.containsKey(location);
    }

    /**
     * Checks if a location has a crate.
     */
    public boolean isCrateLocation(String world, int x, int y, int z) {
        return isCrateLocation(new CrateLocation(world, x, y, z));
    }

    /**
     * Gets all registered crates.
     */
    public Collection<Crate> getAllCrates() {
        return crates.values();
    }

    /**
     * Gets all crate IDs.
     */
    public Set<String> getCrateIds() {
        return crates.keySet();
    }

    /**
     * Gets the total number of crate locations.
     */
    public int getTotalLocations() {
        return locationToCrate.size();
    }

    /**
     * Saves crate locations to file.
     */
    public void saveCrateLocations() {
        Map<String, List<String>> locationData = new HashMap<>();

        for (Crate crate : crates.values()) {
            List<String> locations = crate.getLocations().stream()
                    .map(CrateLocation::toString)
                    .toList();
            if (!locations.isEmpty()) {
                locationData.put(crate.getId(), locations);
            }
        }

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(locationsFile), StandardCharsets.UTF_8)) {
            plugin.getConfigManager().getGson().toJson(locationData, writer);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save crate locations", e);
        }
    }

    /**
     * Loads crate locations from file.
     */
    private void loadCrateLocations() {
        if (!locationsFile.exists()) {
            return;
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(locationsFile), StandardCharsets.UTF_8)) {
            Type type = new TypeToken<Map<String, List<String>>>() {}.getType();
            Map<String, List<String>> locationData = plugin.getConfigManager().getGson().fromJson(reader, type);

            if (locationData != null) {
                for (Map.Entry<String, List<String>> entry : locationData.entrySet()) {
                    String crateId = entry.getKey();
                    Optional<Crate> crateOpt = getCrate(crateId);
                    if (crateOpt.isEmpty()) {
                        plugin.getLogger().warning("Unknown crate in locations file: " + crateId);
                        continue;
                    }

                    Crate crate = crateOpt.get();
                    for (String locStr : entry.getValue()) {
                        CrateLocation location = CrateLocation.fromString(locStr);
                        if (location != null) {
                            crate.addLocation(location);
                            locationToCrate.put(location, crateId);
                        }
                    }
                }
            }

            plugin.getLogger().info("Loaded " + locationToCrate.size() + " crate location(s).");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load crate locations", e);
        }
    }

    /**
     * Reloads all crates from configuration.
     */
    public void reload() {
        plugin.getKeyManager().clearKeys();
        loadCrates();
    }

    /**
     * Creates a new crate from configuration.
     */
    public boolean createCrate(CrateConfig config) {
        if (crates.containsKey(config.getId())) {
            return false;
        }

        plugin.getConfigManager().saveCrateConfig(config);
        Crate crate = new Crate(config);
        crates.put(crate.getId(), crate);
        plugin.getKeyManager().registerKey(crate);

        return true;
    }

    /**
     * Deletes a crate.
     */
    public boolean deleteCrate(String crateId) {
        Crate crate = crates.remove(crateId);
        if (crate == null) {
            return false;
        }

        // Remove all locations
        for (CrateLocation location : new ArrayList<>(crate.getLocations())) {
            locationToCrate.remove(location);
        }

        // Delete config file
        plugin.getConfigManager().deleteCrateConfig(crateId);
        plugin.getKeyManager().unregisterKey(crate.getKeyId());
        saveCrateLocations();

        return true;
    }
}
