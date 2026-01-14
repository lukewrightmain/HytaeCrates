# Hytale Plugin Development Guide

A comprehensive guide based on our experience extracting the Hytale Server API and building the HytaleCrates plugin.

---

## Table of Contents

1. [Extracting the API](#extracting-the-api)
2. [Project Structure](#project-structure)
3. [Plugin Manifest](#plugin-manifest)
4. [Main Plugin Class](#main-plugin-class)
5. [Command System](#command-system)
6. [Message API](#message-api)
7. [Configuration Files](#configuration-files)
8. [Permissions](#permissions)
9. [Useful API Classes](#useful-api-classes)
10. [Known Issues & Workarounds](#known-issues--workarounds)
11. [Build & Deployment](#build--deployment)

---

## Extracting the API

### Getting Classes from HytaleServer.jar

The Hytale Server API is contained within `HytaleServer.jar`. You can inspect available classes using:

```powershell
# List all classes in the JAR
Add-Type -AssemblyName System.IO.Compression.FileSystem
[System.IO.Compression.ZipFile]::OpenRead("libs/HytaleServer.jar").Entries | 
    Where-Object { $_.FullName -like "*.class" } | 
    ForEach-Object { $_.FullName }

# Search for specific classes
[System.IO.Compression.ZipFile]::OpenRead("libs/HytaleServer.jar").Entries | 
    Where-Object { $_.Name -like "*Command*" } | 
    ForEach-Object { $_.FullName }
```

### Inspecting Class Signatures

Use `javap` to see method signatures without decompiling:

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25"
& "$env:JAVA_HOME\bin\javap.exe" -classpath libs/HytaleServer.jar com.hypixel.hytale.server.core.command.system.AbstractCommand
```

### Inventory / Giving Items (confirmed API)

The built-in server `/give` command uses `ItemStack` + `Inventory` directly:

```java
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import org.bson.BsonDocument;

// Given a Store<EntityStore> and Ref<EntityStore> to a player entity:
Player player = store.getComponent(playerEntityRef, Player.getComponentType());

// Optional metadata is BSON (the server parses JSON via BsonDocument.parse)
BsonDocument metadata = BsonDocument.parse("{\"myTag\":\"value\"}");

// Add to inventory (hotbar-first combined container)
player.getInventory()
    .getCombinedHotbarFirst()
    .addItemStack(new ItemStack(itemId, quantity, metadata));
```

### Event Registry (confirmed API)

Plugins can subscribe to server events via `JavaPlugin.getEventRegistry()` (inherited from `PluginBase`).

```java
import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent;

@Override
protected void setup() {
    // ...
    getEventRegistry().registerGlobal(
        EventPriority.NORMAL,
        PlayerInteractEvent.class,
        event -> {
            // event.getTargetBlock(), event.getItemInHand(), event.setCancelled(true), etc.
        }
    );
}
```

Useful interaction events discovered:
- `com.hypixel.hytale.server.core.event.events.player.PlayerInteractEvent`
- `com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent` (has `InteractionContext`)

### ECS Events vs EventRegistry (critical distinction)

Some events (like `UseBlockEvent.Pre`) are **ECS events** (`com.hypixel.hytale.component.system.EcsEvent`), not normal `IEvent`s.
Those will **NOT** fire through `EventRegistry`.

To handle ECS events, register a `WorldEventSystem` (or `EntityEventSystem`) via the plugin’s ECS registry proxy:

```java
import com.hypixel.hytale.component.dependency.RootDependency;
import com.hypixel.hytale.component.system.WorldEventSystem;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

// In plugin setup():
getEntityStoreRegistry().registerWorldEventType(UseBlockEvent.Pre.class);
getEntityStoreRegistry().registerSystem(new WorldEventSystem<EntityStore, UseBlockEvent.Pre>(UseBlockEvent.Pre.class) {
    @Override
    public java.util.Set<com.hypixel.hytale.component.dependency.Dependency<EntityStore>> getDependencies() {
        return RootDependency.firstSet(); // run early
    }

    @Override
    public void handle(com.hypixel.hytale.component.Store<EntityStore> store,
                       com.hypixel.hytale.component.CommandBuffer<EntityStore> buffer,
                       UseBlockEvent.Pre event) {
        // event.setCancelled(true) to stop chest-open
    }
});
```

### Block Interaction System (important for chest/container handling)

Hytale's block interaction system works differently from simple event handlers:

1. Each `BlockType` has an `interactions` map that defines what interaction runs for each action type
2. The interactions are processed by `InteractionManager` through a specific pipeline
3. Block interactions (like opening chests) may bypass standard events

**InteractionType enum** (from `com.hypixel.hytale.protocol.InteractionType`):

| Type | Trigger | Notes |
|------|---------|-------|
| `Primary` | Left mouse button | Attack/break |
| `Secondary` | Right mouse button | Place/use item on block |
| `Use` | F key | "Press F to open" prompts |
| `Pick` | Middle mouse button | Pick block |
| `Pickup` | Pickup action | Item pickup |
| `Ability1/2/3` | Ability keys | Special abilities |

**Intercepting block interactions:**

For containers (chests, crates, etc.), the F key triggers `InteractionType.Use` which runs `OpenContainerInteraction`. To intercept this:

```java
// Option 1: Handle right-click (Secondary) instead - more reliable
getEventRegistry().registerGlobal(EventPriority.FIRST, PlayerInteractEvent.class, event -> {
    if (event.getActionType() == InteractionType.Secondary) {
        Vector3i target = event.getTargetBlock();
        if (target != null && isYourSpecialBlock(target)) {
            // Handle custom interaction
            event.setCancelled(true); // Prevent default behavior
        }
    }
});

// Option 2: Use ECS WorldEventSystem for UseBlockEvent.Pre (F key)
// Note: This may not always fire before container opens depending on pipeline order
```

**PlayerInteractEvent details (deprecated - may not fire):**

```java
public class PlayerInteractEvent implements ICancellable {
    InteractionType getActionType();  // Primary, Secondary, Use, etc.
    Vector3i getTargetBlock();        // Block being interacted with
    ItemStack getItemInHand();        // Item player is holding
    Entity getTargetEntity();         // Entity being interacted with (if any)
    void setCancelled(boolean);       // Cancel default behavior
}
```

**PlayerMouseButtonEvent (recommended alternative):**

This event fires when the player clicks a mouse button. It may be more reliable than `PlayerInteractEvent`:

```java
import com.hypixel.hytale.server.core.event.events.player.PlayerMouseButtonEvent;
import com.hypixel.hytale.protocol.MouseButtonType;

getEventRegistry().registerGlobal(EventPriority.FIRST, PlayerMouseButtonEvent.class, event -> {
    var mouseButton = event.getMouseButton();
    
    // Check for right-click
    if (mouseButton != null && mouseButton.mouseButtonType == MouseButtonType.Right) {
        Vector3i target = event.getTargetBlock();
        Item heldItem = event.getItemInHand();  // Note: returns Item config, not ItemStack
        Player player = event.getPlayer();
        
        if (target != null && isYourSpecialBlock(target)) {
            // Handle custom interaction
            event.setCancelled(true);
        }
    }
});
```

| MouseButtonType | Description |
|-----------------|-------------|
| `Left` | Left mouse button |
| `Right` | Right mouse button |
| `Middle` | Middle mouse button |
| `X1` | Extra button 1 |
| `X2` | Extra button 2 |

### Discovering item asset ids (important)

Minecraft-style item names like `STICK`, `DIAMOND`, etc are **not** valid Hytale item ids.
To find the real ids, query the server’s loaded item asset map:

```java
import com.hypixel.hytale.server.core.asset.type.item.config.Item;

var map = Item.getAssetMap().getAssetMap(); // Map<String, Item>
```

In this repo we also added a debug command:

```text
/crate itemids --query=<text> [--limit=<n>]
```

Example:

```text
/crate itemids --query=stick
/crate itemids --query=rod
/crate itemids --query=sword --limit=50
```

### Key API Packages Discovered

| Package | Purpose |
|---------|---------|
| `com.hypixel.hytale.server.core.plugin` | Plugin lifecycle (`JavaPlugin`, `JavaPluginInit`) |
| `com.hypixel.hytale.server.core.command.system` | Command framework |
| `com.hypixel.hytale.server.core.command.system.arguments` | Command arguments |
| `com.hypixel.hytale.server.core.Message` | Chat messages |
| `com.hypixel.hytale.server.core.permissions` | Permission system |

---

## Project Structure

```
MyPlugin/
├── build.gradle
├── settings.gradle
├── libs/
│   └── HytaleServer.jar          # Server API (compileOnly)
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/myplugin/
│   │   │       ├── MyPlugin.java       # Main class
│   │   │       ├── commands/           # Command handlers
│   │   │       ├── config/             # Configuration classes
│   │   │       ├── listeners/          # Event listeners
│   │   │       └── util/               # Utilities
│   │   └── resources/
│   │       ├── manifest.json           # Plugin manifest (REQUIRED)
│   │       └── config/                 # Default configs
│   └── test/
│       └── java/                       # Unit tests
└── build/
    └── libs/
        └── MyPlugin-1.0.0.jar          # Built plugin
```

### build.gradle

```groovy
plugins {
    id 'java'
}

group = 'com.myplugin'
version = '1.0.0'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Hytale Server API - NOT bundled in final JAR
    compileOnly files('libs/HytaleServer.jar')
    
    // Runtime dependencies - bundled in final JAR
    implementation 'com.google.code.gson:gson:2.10.1'
    
    // Testing
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.2'
}

jar {
    manifest {
        attributes('Main-Class': 'com.myplugin.MyPlugin')
    }
    from {
        configurations.runtimeClasspath.collect { it.isDirectory() ? it : zipTree(it) }
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
```

---

## Plugin Manifest

Every plugin needs a `manifest.json` in `src/main/resources/`:

```json
{
  "Group": "com.myplugin",
  "Name": "MyPlugin",
  "Version": "1.0.0",
  "Main": "com.myplugin.MyPlugin",
  "Description": "My awesome Hytale plugin",
  "Authors": [
    { "Name": "YourName" }
  ],
  "Website": "https://github.com/yourname/myplugin",
  "Permissions": [
    {
      "Name": "myplugin.use",
      "Description": "Basic usage permission",
      "Default": true
    },
    {
      "Name": "myplugin.admin",
      "Description": "Admin commands",
      "Default": false
    }
  ],
  "Commands": [
    {
      "Name": "mycommand",
      "Description": "Main command",
      "Usage": "/mycommand <args>",
      "Aliases": [
        { "Name": "mc" },
        { "Name": "mycmd" }
      ]
    }
  ]
}
```

---

## Main Plugin Class

```java
package com.myplugin;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

public class MyPlugin extends JavaPlugin {

    private static MyPlugin instance;

    public MyPlugin(JavaPluginInit init) {
        super(init);
        instance = this;
    }

    @Override
    protected void setup() {
        getLogger().at(Level.INFO).log("Setting up MyPlugin...");
        
        // Create data directory
        try {
            Path dataDir = getDataDirectory();
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }
        } catch (Exception e) {
            getLogger().at(Level.SEVERE).withCause(e).log("Failed to create data directory!");
        }
        
        // Initialize managers, load configs, register commands
        getCommandRegistry().registerCommand(new MyCommand(this));
        
        getLogger().at(Level.INFO).log("MyPlugin setup complete!");
    }

    @Override
    protected void start() {
        getLogger().at(Level.INFO).log("MyPlugin started!");
    }

    @Override
    protected void shutdown() {
        getLogger().at(Level.INFO).log("MyPlugin shutting down...");
        // Save data, cleanup resources
    }

    public static MyPlugin getInstance() {
        return instance;
    }
}
```

### Plugin Lifecycle

1. **Constructor** - Called when plugin is loaded, receives `JavaPluginInit`
2. **setup()** - Initialize managers, load configs, register commands
3. **start()** - Plugin is fully loaded and ready
4. **shutdown()** - Server stopping, save data and cleanup

---

## Command System

### Basic Command Structure

```java
package com.myplugin.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.StringArgumentType;
import java.util.concurrent.CompletableFuture;

public class MyCommand extends AbstractCommand {

    public MyCommand() {
        super("mycommand", "My command description");
        
        // Add aliases
        addAliases("mc", "mycmd");
        
        // Add subcommands
        addSubCommand(new HelpSubCommand());
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        ctx.sendMessage(Message.raw("Hello from MyPlugin!"));
        return CompletableFuture.completedFuture(null);
    }
}
```

### Command with Arguments

```java
private static class GiveSubCommand extends AbstractCommand {
    private final OptionalArg<String> playerArg;
    private final OptionalArg<String> amountArg;

    GiveSubCommand() {
        super("give", "Give something to a player");
        requirePermission("myplugin.admin");
        
        // Define arguments
        this.playerArg = withOptionalArg("player", "Target player", StringArgumentType.word());
        this.amountArg = withOptionalArg("amount", "Amount", StringArgumentType.word());
    }

    @Override
    protected CompletableFuture<Void> execute(CommandContext ctx) {
        // Check if arguments were provided
        if (!ctx.provided(playerArg)) {
            ctx.sendMessage(Message.raw("Usage: /mycommand give <player> [amount]"));
            return CompletableFuture.completedFuture(null);
        }

        String player = ctx.get(playerArg);
        int amount = 1;
        
        if (ctx.provided(amountArg)) {
            try {
                amount = Integer.parseInt(ctx.get(amountArg));
            } catch (NumberFormatException e) {
                ctx.sendMessage(Message.raw("Invalid amount!"));
                return CompletableFuture.completedFuture(null);
            }
        }

        ctx.sendMessage(Message.raw("Gave " + amount + " to " + player));
        return CompletableFuture.completedFuture(null);
    }
}
```

### CommandContext Methods

| Method | Description |
|--------|-------------|
| `ctx.sendMessage(Message)` | Send message to command sender |
| `ctx.sender()` | Get the CommandSender |
| `ctx.sender().getUuid()` | Get sender's UUID |
| `ctx.sender().getDisplayName()` | Get sender's display name |
| `ctx.sender().hasPermission(String)` | Check permission |
| `ctx.isPlayer()` | Check if sender is a player |
| `ctx.get(arg)` | Get argument value |
| `ctx.provided(arg)` | Check if argument was provided |
| `ctx.senderAsPlayerRef()` | Get player entity reference |

### Argument Types

```java
// String (single word)
StringArgumentType.word()

// String (quoted)
StringArgumentType.string()

// String (rest of input)
StringArgumentType.greedyString()
```

### Command Argument Format

**IMPORTANT:** Hytale uses named arguments with `--` prefix, not positional arguments!

```bash
# Hytale format (correct)
/mycommand --player=Steve --amount=5

# NOT positional like Minecraft
/mycommand Steve 5
```

When you define `withOptionalArg("player", ...)`, players use `--player=value`.

**Note:** If `StringArgumentType` doesn't exist in your JAR, you may need to create a stub class that extends `SingleArgumentType<String>`:

```java
package com.hypixel.hytale.server.core.command.system.arguments.types;

import com.hypixel.hytale.server.core.command.system.ParseResult;

public class StringArgumentType extends SingleArgumentType<String> {
    private StringArgumentType() {
        super("<string>", "A text value");
    }

    public static StringArgumentType word() {
        return new StringArgumentType();
    }

    @Override
    public String parse(String input, ParseResult result) {
        return input;
    }
}
```

---

## Message API

### Basic Messages

```java
import com.hypixel.hytale.server.core.Message;

// Simple text message
ctx.sendMessage(Message.raw("Hello, world!"));

// With variables
String name = "Player";
ctx.sendMessage(Message.raw("Welcome, " + name + "!"));
```

### Color Codes

**Confirmed (via `javap` on `HytaleServer.jar`)**: Hytale uses a structured message object with style methods.

Key APIs:
- `Message.color(String)` / `Message.color(Color)`
- `Message.bold(boolean)`, `Message.italic(boolean)`, `Message.monospace(boolean)`
- `Message.insert(Message)` / `Message.insertAll(...)`
- `Message.parse(String)` (parses a string format; exact syntax varies by server build)

```java
import com.hypixel.hytale.server.core.Message;

// Apply color + bold using the real API (no raw § / & codes)
ctx.sendMessage(
    Message.raw("Legendary Win!")
        .color("#FFAA00")   // hex colors work
        .bold(true)
);

// Build multi-colored messages by inserting child messages
Message msg = Message.empty()
    .insert(Message.raw("[").color("#AAAAAA"))
    .insert(Message.raw("Crates").color("#55FFFF").bold(true))
    .insert(Message.raw("] ").color("#AAAAAA"))
    .insert(Message.raw("You received a key!").color("#55FF55"));

ctx.sendMessage(msg);
```

#### Legacy `&` color code compatibility

If you have configs/messages using Minecraft-style `&` codes, convert them into `Message` objects instead of sending raw strings.

In this repo we added a helper: `com.hytalecrates.util.MessageUtil.legacyToMessage(String)` which supports:
- Colors: `&0`-`&9`, `&a`-`&f`
- Formatting: `&l` (bold), `&o` (italic), `&r` (reset)

Example:

```java
import com.hytalecrates.util.MessageUtil;

ctx.sendMessage(MessageUtil.legacyToMessage("&6&lGold Title &7- &aGreen text"));
```

---

## Configuration Files

### Using Gson for JSON Configs

```java
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.nio.file.*;

public class ConfigManager {
    private final Path configPath;
    private final Gson gson;
    private MyConfig config;

    public ConfigManager(Path dataDir) {
        this.configPath = dataDir.resolve("config.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public void load() {
        if (Files.exists(configPath)) {
            try (Reader reader = Files.newBufferedReader(configPath)) {
                config = gson.fromJson(reader, MyConfig.class);
            } catch (IOException e) {
                config = new MyConfig(); // Defaults
            }
        } else {
            config = new MyConfig();
            save();
        }
    }

    public void save() {
        try (Writer writer = Files.newBufferedWriter(configPath)) {
            gson.toJson(config, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

### Config POJO

```java
public class MyConfig {
    private String prefix = "[MyPlugin] ";
    private boolean enabled = true;
    private int maxItems = 64;

    // Getters and setters
    public String getPrefix() { return prefix; }
    public boolean isEnabled() { return enabled; }
    public int getMaxItems() { return maxItems; }
}
```

---

## Permissions

### Checking Permissions

```java
// In command context
if (ctx.sender().hasPermission("myplugin.admin")) {
    // Admin action
}

// Require permission for entire command
public MyAdminCommand() {
    super("admin", "Admin command");
    requirePermission("myplugin.admin");
}
```

### Permission Defaults in manifest.json

```json
{
  "Permissions": [
    {
      "Name": "myplugin.use",
      "Description": "Basic usage",
      "Default": true     // All players have this
    },
    {
      "Name": "myplugin.admin",
      "Description": "Admin access",
      "Default": false    // Only ops/admins
    }
  ]
}
```

---

## Useful API Classes

### Discovered Through Inspection

| Class | Package | Purpose |
|-------|---------|---------|
| `JavaPlugin` | `...core.plugin` | Base plugin class |
| `JavaPluginInit` | `...core.plugin` | Plugin initialization data |
| `AbstractCommand` | `...command.system` | Base command class |
| `CommandContext` | `...command.system` | Command execution context |
| `CommandSender` | `...command.system` | Command sender interface |
| `Message` | `...core` | Chat message class |
| `OptionalArg<T>` | `...arguments.system` | Optional command argument |
| `SingleArgumentType<T>` | `...arguments.types` | Base argument type |
| `ArgumentType<T>` | `...arguments.types` | Argument type interface |
| `AbstractTargetPlayerCommand` | `...command.system.basecommands` | Command base that provides `Store`, `World`, and optional `--player` targeting |
| `Player` | `...server.core.entity.entities` | Player entity component (has `getInventory()`) |
| `Inventory` | `...server.core.inventory` | Player inventory accessors (hotbar/storage/combined containers) |
| `ItemStack` | `...server.core.inventory` | Stack of items with `itemId`, `quantity`, and BSON `metadata` |
| `BsonDocument` | `org.bson` | Item metadata container (used by built-in `/give`) |

### Logging

```java
import java.util.logging.Level;

// Info level
getLogger().at(Level.INFO).log("Plugin loaded!");

// With formatting
getLogger().at(Level.INFO).log("Loaded %d items.", count);

// Warning
getLogger().at(Level.WARNING).log("Config missing, using defaults");

// Error with exception
getLogger().at(Level.SEVERE).withCause(exception).log("Failed to save data!");
```

---

## Known Issues & Workarounds

### 1. Color Codes Don't Render

**Problem:** Sending Minecraft-style `§` or `&` codes as raw text shows literal characters.

**Solution:** Use Hytale `Message` styling methods (`.color(...)`, `.bold(...)`, etc.) or convert legacy `&` codes into a `Message` object before sending.

### 2. Missing API Classes

**Problem:** Some classes mentioned in code don't exist in the JAR.

**Workaround:** Create stub classes in your source under the same package path:

```
src/main/java/com/hypixel/hytale/server/core/command/system/arguments/types/StringArgumentType.java
```

### 3. Getting Player Target Block

**Solution:** Use `TargetUtil.getTargetBlock()` with `AbstractTargetPlayerCommand`:

```java
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.util.TargetUtil;
import com.hypixel.hytale.math.vector.Vector3i;

public class MyTargetCommand extends AbstractTargetPlayerCommand {
    
    public MyTargetCommand() {
        super("mytarget", "Do something with target block");
    }

    @Override
    protected void execute(CommandContext ctx, 
                          Ref<EntityStore> senderRef, 
                          Ref<EntityStore> targetEntityRef,
                          PlayerRef playerRef, 
                          World world, 
                          Store<EntityStore> store) {
        
        // Get block player is looking at (max 10 blocks away)
        Vector3i targetBlock = TargetUtil.getTargetBlock(senderRef, 10.0, store);
        
        if (targetBlock != null) {
            int x = targetBlock.x;
            int y = targetBlock.y;
            int z = targetBlock.z;
            String worldName = world.getName();
            
            ctx.sendMessage(Message.raw("Looking at: " + x + ", " + y + ", " + z));
        }
    }
}
```

**Key Classes:**
- `AbstractTargetPlayerCommand` - Base class that provides World and Store access
- `TargetUtil.getTargetBlock(Ref, distance, Store)` - Gets block player is looking at
- `Vector3i` - Block coordinates with `x`, `y`, `z` fields

---

## Build & Deployment

### Building

```powershell
# Set Java path (if needed)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-25"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

# Navigate to project
cd C:\Path\To\MyPlugin

# Run tests
.\gradlew test

# Build JAR
.\gradlew build

# Clean and rebuild
.\gradlew clean build
```

### Output

Built JAR location: `build/libs/MyPlugin-1.0.0.jar`

### Deployment

1. Copy JAR to server's `plugins/` folder
2. Start/restart the server
3. Check server logs for plugin load messages

---

## Quick Reference

### Terminal Commands

```powershell
# Inspect JAR classes
Add-Type -AssemblyName System.IO.Compression.FileSystem
[System.IO.Compression.ZipFile]::OpenRead("libs/HytaleServer.jar").Entries | 
    Where-Object { $_.Name -like "*YourSearch*" } | 
    ForEach-Object { $_.FullName }

# Inspect class signature
& "$env:JAVA_HOME\bin\javap.exe" -classpath libs/HytaleServer.jar com.package.ClassName
```

### Gradle Commands

| Command | Description |
|---------|-------------|
| `.\gradlew build` | Compile and package |
| `.\gradlew test` | Run unit tests |
| `.\gradlew clean` | Clean build artifacts |
| `.\gradlew compileJava` | Compile only (no JAR) |

---

## Resources

- **This Project:** HytaleCrates - A complete example plugin
- **Hytale Server JAR:** Contains all API classes
- **Gson Documentation:** https://github.com/google/gson

---

*Last updated: January 2026*
*Based on HytaleCrates plugin development experience*

