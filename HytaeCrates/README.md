# HytaeCrates

A Hytale server plugin that implements a crate/key reward system with casino-style animations.

## Features

- **Physical Key System**: Enchanted items (like sticks) that act as keys to open crates
- **Customizable Crates**: Create unlimited crates with unique rewards and rarities
- **Casino-Style Animation**: Exciting slot machine spinning animation when opening crates
- **Rarity System**: 5 tiers of rarity (Common, Uncommon, Rare, Epic, Legendary)
- **Weighted Random Selection**: Configure exact drop chances for each reward
- **Particle Effects**: Visual effects based on reward rarity
- **Sound Effects**: Audio feedback during animations and wins
- **Global Announcements**: Broadcast rare+ wins to all players
- **Admin Commands**: Full in-game management of crates and keys
- **JSON Configuration**: Easy-to-edit configuration files

## Requirements

- Java 25 or later
- Hytale Server with Plugin API

## Installation

1. Download the latest `HytaeCrates.jar` from releases
2. Place in your server's `plugins` folder
3. Restart the server
4. Configure crates in `plugins/HytaeCrates/config/crates/`

## Configuration

### Main Config (`config/config.json`)

```json
{
  "prefix": "&6[Crates] &r",
  "animation": {
    "spinDuration": 4000,
    "tickSound": "ui.button.click",
    "winSound": "entity.player.levelup"
  },
  "announcements": {
    "enabled": true,
    "format": "&e{player} &7opened a &b{crate} &7and won {rarity_color}{item}&7!"
  }
}
```

### Crate Config (`config/crates/vote_crate.json`)

```json
{
  "id": "vote_crate",
  "displayName": "&bVote Crate",
  "blockType": "CHEST",
  "keyId": "vote_key",
  "keyItem": {
    "material": "STICK",
    "displayName": "&b&lVote Key",
    "enchanted": true,
    "lore": ["&7Right-click on a Vote Crate", "&7to claim your reward!"]
  },
  "rewards": [
    {
      "item": { "material": "DIAMOND", "amount": 5 },
      "rarity": "RARE",
      "weight": 15
    }
  ]
}
```

## Commands

### Player Commands
| Command | Permission | Description |
|---------|------------|-------------|
| `/crate list` | crates.use | List all available crates |
| `/crate preview <name>` | crates.use | Preview crate rewards |
| `/crate info <name>` | crates.use | Show crate details |

### Admin Commands
| Command | Permission | Description |
|---------|------------|-------------|
| `/crate set <name>` | crates.admin | Set block as crate location |
| `/crate remove` | crates.admin | Remove crate from location |
| `/crate give <player> <key> [amount]` | crates.admin | Give keys to player |
| `/crate create <name>` | crates.admin | Create new crate |
| `/crate delete <name>` | crates.admin | Delete a crate |
| `/crate reload` | crates.admin | Reload configurations |

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `crates.use` | Use crates and preview rewards | true |
| `crates.admin` | Manage crates and give keys | op |

## Rarity System

| Rarity | Color | Default Weight | Announce |
|--------|-------|----------------|----------|
| COMMON | Gray (&7) | 50 | No |
| UNCOMMON | Green (&a) | 25 | No |
| RARE | Blue (&9) | 15 | Yes |
| EPIC | Purple (&5) | 7 | Yes |
| LEGENDARY | Gold (&6) | 3 | Yes (Special) |

## How It Works

1. **Setup**: Admin creates a crate configuration and places it at a block location
2. **Keys**: Players receive keys through voting, events, or admin commands
3. **Opening**: Player right-clicks a crate with the correct key
4. **Animation**: Casino-style spinning animation plays
5. **Reward**: Item is added to inventory and win is announced

## Building from Source

```bash
# Clone the repository
git clone https://github.com/yourusername/HytaeCrates.git
cd HytaeCrates

# Build with Gradle
./gradlew build

# The JAR will be in build/libs/
```

## Project Structure

```
src/main/java/com/hytaecrates/
├── CratesPlugin.java          # Main plugin class
├── commands/                  # Command handlers
├── config/                    # Configuration models
├── crate/                     # Crate system
├── key/                       # Key management
├── reward/                    # Reward system
├── gui/                       # GUI system
├── animation/                 # Animation engine
├── listeners/                 # Event listeners
├── announcement/              # Chat announcements
└── util/                      # Utilities
```

## API Usage (For Developers)

```java
// Get the plugin instance
CratesPlugin plugin = CratesPlugin.getInstance();

// Give a key to a player
plugin.getKeyManager().giveKey(playerUuid, "vote_key", 1);

// Open a crate programmatically
Crate crate = plugin.getCrateManager().getCrate("vote_crate").get();
Reward reward = plugin.getRewardManager().selectReward(crate);

// Register a custom crate
CrateConfig config = new CrateConfig();
// ... configure ...
plugin.getCrateManager().createCrate(config);
```

## License

MIT License - See LICENSE file for details.

## Support

For issues and feature requests, please open an issue on GitHub.
