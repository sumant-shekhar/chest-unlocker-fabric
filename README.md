# Chest Unlocker

A client-side Fabric mod for Minecraft 26.2. When enabled, it listens for chest
lock-picking prompts and turns the player's view toward nearby CRIT particles
around a chest.

## Behavior

- Press **J** to toggle the mod on or off.
- Start the chest lock-picking interaction yourself. The mod arms when it sees
  the expected lock-picking chat prompt.
- While armed, it aims at CRIT particles near a chest or trapped chest.
- It stops aiming when the interaction appears complete, a screen opens, the
  relevant particles stop, you leave the world, or the timeout expires.
- It does not click, move the player, open a GUI, or send chat messages.

The mod is loaded normally by Fabric and does not hide itself. A server may
identify it through its normal mod checks. Follow the server's rules before
using it.

## Requirements

- Minecraft 26.2
- Fabric Loader 0.19.3 or later
- Fabric API 0.156.0+26.2
- Java 25

## Build

On Arch Linux with `jdk25-openjdk` installed, build with:

```sh
./gradlew build
```

The mod jar is created at `build/libs/chestunlocker-1.0.0.jar`.

## License

This project is distributed under the MIT License. See [LICENSE](LICENSE).
