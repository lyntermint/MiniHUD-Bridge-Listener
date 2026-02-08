package com.lyntermint.minihuddebug;

import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNode;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.custom.DebugBrainCustomPayload;
import net.minecraft.network.packet.s2c.custom.DebugGoalSelectorCustomPayload;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class MinihudDebugAddon implements ClientModInitializer {
    private static final byte TYPE_PATH = 1;
    private static final byte TYPE_NEIGHBOR = 2;
    private static final byte TYPE_GOAL = 3;
    private static final byte TYPE_BRAIN = 4;
    @Override
    public void onInitializeClient() {
        PayloadTypeRegistry.playC2S().register(SubscribePayload.PAYLOAD_ID, SubscribePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(DebugDataPayload.PAYLOAD_ID, DebugDataPayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(DebugDataPayload.PAYLOAD_ID, (payload, context) -> {
            byte[] data = payload.data();
            MinecraftClient client = context.client();
            client.execute(() -> handlePayload(client, data));
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (!ClientPlayNetworking.canSend(SubscribePayload.PAYLOAD_ID)) {
                return;
            }
            ClientPlayNetworking.send(new SubscribePayload((byte) 1));
        });
    }

    private static void handlePayload(MinecraftClient client, byte[] data) {
        if (client == null || client.world == null || client.debugRenderer == null) {
            return;
        }
        PacketByteBuf buf = new PacketByteBuf(Unpooled.wrappedBuffer(data));
        if (!buf.isReadable()) {
            return;
        }
        byte type = buf.readByte();
        switch (type) {
            case TYPE_PATH -> handlePath(client, buf);
            case TYPE_NEIGHBOR -> handleNeighbor(client, buf);
            case TYPE_GOAL -> handleGoal(client, buf);
            case TYPE_BRAIN -> handleBrain(client, buf);
            default -> {
            }
        }
    }

    private static void handlePath(MinecraftClient client, PacketByteBuf buf) {
        int entityId = buf.readVarInt();
        float nodeSize = buf.readFloat();
        int count = buf.readVarInt();
        List<PathNode> nodes = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int x = buf.readVarInt();
            int y = buf.readVarInt();
            int z = buf.readVarInt();
            String typeName = buf.readString(32);
            PathNode node = new PathNode(x, y, z);
            node.type = parseType(typeName);
            nodes.add(node);
        }
        BlockPos target = count > 0 ? new BlockPos(nodes.get(count - 1).x, nodes.get(count - 1).y, nodes.get(count - 1).z) : BlockPos.ORIGIN;
        Path path = new Path(nodes, target, false);
        DebugRenderer debug = client.debugRenderer;
        debug.pathfindingDebugRenderer.addPath(entityId, path, nodeSize);
    }

    private static void handleNeighbor(MinecraftClient client, PacketByteBuf buf) {
        long time = buf.readLong();
        int x = buf.readVarInt();
        int y = buf.readVarInt();
        int z = buf.readVarInt();
        DebugRenderer debug = client.debugRenderer;
        debug.neighborUpdateDebugRenderer.addNeighborUpdate(time, new BlockPos(x, y, z));
    }

    private static void handleGoal(MinecraftClient client, PacketByteBuf buf) {
        int entityId = buf.readVarInt();
        int x = buf.readVarInt();
        int y = buf.readVarInt();
        int z = buf.readVarInt();
        int count = buf.readVarInt();
        List<DebugGoalSelectorCustomPayload.Goal> goals = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int priority = buf.readVarInt();
            boolean running = buf.readBoolean();
            String name = buf.readString(255);
            goals.add(new DebugGoalSelectorCustomPayload.Goal(priority, running, name));
        }
        DebugRenderer debug = client.debugRenderer;
        debug.goalSelectorDebugRenderer.setGoalSelectorList(entityId, new BlockPos(x, y, z), goals);
    }

    private static void handleBrain(MinecraftClient client, PacketByteBuf buf) {
        UUID uuid = buf.readUuid();
        int entityId = buf.readVarInt();
        String name = buf.readString(128);
        String profession = buf.readString(128);
        int xp = buf.readVarInt();
        float health = buf.readFloat();
        float maxHealth = buf.readFloat();
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        String inventory = buf.readString(128);
        boolean wantsGolem = buf.readBoolean();
        int anger = buf.readVarInt();

        int runningCount = buf.readVarInt();
        List<String> runningTasks = new ArrayList<>(runningCount);
        for (int i = 0; i < runningCount; i++) {
            runningTasks.add(buf.readString(128));
        }

        int possibleCount = buf.readVarInt();
        List<String> possibleActivities = new ArrayList<>(possibleCount);
        for (int i = 0; i < possibleCount; i++) {
            possibleActivities.add(buf.readString(256));
        }

        int memoriesCount = buf.readVarInt();
        List<String> memories = new ArrayList<>(memoriesCount);
        for (int i = 0; i < memoriesCount; i++) {
            memories.add(buf.readString(256));
        }

        int gossipCount = buf.readVarInt();
        List<String> gossips = new ArrayList<>(gossipCount);
        for (int i = 0; i < gossipCount; i++) {
            gossips.add(buf.readString(256));
        }

        int pathCount = buf.readVarInt();
        List<PathNode> nodes = new ArrayList<>(pathCount);
        for (int i = 0; i < pathCount; i++) {
            int px = buf.readVarInt();
            int py = buf.readVarInt();
            int pz = buf.readVarInt();
            nodes.add(new PathNode(px, py, pz));
        }
        BlockPos target = pathCount > 0 ? new BlockPos(nodes.get(pathCount - 1).x, nodes.get(pathCount - 1).y, nodes.get(pathCount - 1).z) : BlockPos.ORIGIN;
        Path path = new Path(nodes, target, false);

        int poiCount = buf.readVarInt();
        java.util.Set<BlockPos> pois = new java.util.HashSet<>();
        for (int i = 0; i < poiCount; i++) {
            int px = buf.readVarInt();
            int py = buf.readVarInt();
            int pz = buf.readVarInt();
            pois.add(new BlockPos(px, py, pz));
        }
        int potentialPoiCount = buf.readVarInt();
        java.util.Set<BlockPos> potentialPois = new java.util.HashSet<>();
        for (int i = 0; i < potentialPoiCount; i++) {
            int px = buf.readVarInt();
            int py = buf.readVarInt();
            int pz = buf.readVarInt();
            potentialPois.add(new BlockPos(px, py, pz));
        }

        DebugBrainCustomPayload.Brain brain = new DebugBrainCustomPayload.Brain(
                uuid,
                entityId,
                name,
                profession,
                xp,
                health,
                maxHealth,
                new Vec3d(x, y, z),
                inventory,
                path,
                wantsGolem,
                anger,
                runningTasks,
                possibleActivities,
                memories,
                gossips,
                pois,
                potentialPois
        );

        DebugRenderer debug = client.debugRenderer;
        debug.villageDebugRenderer.addBrain(brain);
    }

    private static PathNodeType parseType(String name) {
        if (name == null || name.isEmpty()) {
            return PathNodeType.BLOCKED;
        }
        try {
            return PathNodeType.valueOf(name);
        } catch (IllegalArgumentException ex) {
            return PathNodeType.BLOCKED;
        }
    }
}
