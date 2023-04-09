package lol.estrogen;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleMod implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("ldd");

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        LOGGER.info("Hello Fabric world!");
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder = LiteralArgumentBuilder.literal("ldd");
            literalArgumentBuilder.executes(context -> {
                ServerCommandSource source = context.getSource();
                Vec3d pos = source.getPosition();
                World world = source.getWorld();

                BlockPos blockPos = new BlockPos((int) pos.x, (int) pos.y, (int) pos.z);
                Chunk chunk = world.getChunk(blockPos);

                Difficulty difficulty = world.getDifficulty();
                long timeOfDay = world.getTimeOfDay();
                long inhabitedTime = 0;
                float moonSize = 0;

                String message = "\n-- Local Difficulty Debug --\n";
                message += String.format("Position: %d, %d, %d\n", blockPos.getX(), blockPos.getY(), blockPos.getZ());
                message += String.format("Chunk: %d, %d\n", chunk.getPos().x, chunk.getPos().z);
                message += String.format("Difficulty: %s\n", difficulty.getName());
                message += String.format("Time of day: %d\n", timeOfDay);

                if (world.isChunkLoaded(blockPos)) {
                    moonSize = world.getMoonSize();
                    inhabitedTime = chunk.getInhabitedTime();

                    message += String.format("Moon size: %.2f\n", moonSize);
                    message += String.format("Inhabited time: %d\n", inhabitedTime);
                } else {
                    message += "Chunk is not loaded\n";
                }

                LocalDifficulty ld = new LocalDifficulty(difficulty, timeOfDay, inhabitedTime, moonSize);
                LocalDifficulty ldworld = world.getLocalDifficulty(blockPos);

                message += String.format("Local difficulty: %.5f (World: %.5f)\n", ld.getLocalDifficulty(), ldworld.getLocalDifficulty());
                message += String.format("Local CLD: %.5f (World: %.5f)\n", ld.getClampedLocalDifficulty(), ldworld.getClampedLocalDifficulty());

                source.sendFeedback(Text.of(message), false);

                return 1;
            });
            dispatcher.register(literalArgumentBuilder);
        });
    }
}