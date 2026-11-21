package dev.chestunlocker;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Toggle-only chest unlocker. No GUI, no chat output, no rendering.
 *
 * Flow: you click the chest yourself -> server says "You must pick the lock..."
 * -> mod arms -> it aims at the CRIT particles around the chest until it unlocks.
 */
public class ChestUnlockerClient implements ClientModInitializer {

    private static final long MAX_ARMED_MS = 20000;
    private static final long NO_PARTICLE_MS = 5000;

    private static KeyMapping toggleKey;

    private static boolean enabled = false;
    private static boolean armed = false;
    private static long armedAt = 0;
    private static long lastParticle = 0;

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("chestunlocker", "main"));
        toggleKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key.chestunlocker.toggle", InputConstants.Type.KEYSYM, InputConstants.KEY_J, category));

        ClientTickEvents.END_CLIENT_TICK.register(ChestUnlockerClient::tick);

        // listen to chat / system messages
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!enabled || overlay) return;
            String text = message.getString();
            long now = System.currentTimeMillis();

            if (text.contains("You must pick the lock on this chest")
                    || text.contains("aim at the particles on the chest")) {
                armed = true;
                armedAt = now;
                lastParticle = now;
            } else if (armed && text.toLowerCase().contains("lockpicked")) {
                armed = false;
            }
        });
    }

    private static void tick(Minecraft mc) {
        while (toggleKey.consumeClick()) {
            enabled = !enabled;
            armed = false;
        }
        if (!enabled) return;

        if (mc.player == null || mc.level == null) {
            // left the world: switch off
            enabled = false;
            armed = false;
            return;
        }

        if (!armed) return;

        long now = System.currentTimeMillis();
        if (mc.gui.screen() != null                // chest (or any menu) opened -> done
                || now - armedAt > MAX_ARMED_MS
                || now - lastParticle > NO_PARTICLE_MS) {
            armed = false;
        }
    }

    /** Called from the mixin for every particle packet. */
    public static void onParticle(ClientboundLevelParticlesPacket packet) {
        Minecraft mc = Minecraft.getInstance();
        // the handler runs once on the network thread and once on the main thread
        if (!enabled || !armed || !mc.isSameThread()) return;
        if (mc.player == null || mc.level == null || mc.gui.screen() != null) return;
        if (packet.getParticle().getType() != ParticleTypes.CRIT) return;

        double x = packet.getX();
        double y = packet.getY();
        double z = packet.getZ();

        if (!nearChest(mc, x, y, z)) return;

        lastParticle = System.currentTimeMillis();
        lookAt(mc.player, new Vec3(x, y, z));
    }

    private static boolean nearChest(Minecraft mc, double x, double y, double z) {
        BlockPos center = BlockPos.containing(x, y, z);
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 1, 1))) {
            BlockState s = mc.level.getBlockState(p);
            if (s.is(Blocks.CHEST) || s.is(Blocks.TRAPPED_CHEST)) return true;
        }
        return false;
    }

    private static void lookAt(LocalPlayer player, Vec3 target) {
        Vec3 eye = player.getEyePosition();
        double dx = target.x - eye.x;
        double dy = target.y - eye.y;
        double dz = target.z - eye.z;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, horizontal));
        player.setYRot(yaw);
        player.setYHeadRot(yaw);
        player.setXRot(Mth.clamp(pitch, -90.0f, 90.0f));
    }
}
