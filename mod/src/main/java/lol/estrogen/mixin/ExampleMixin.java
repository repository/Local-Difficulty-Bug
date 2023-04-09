package lol.estrogen.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalDifficulty.class)
public class ExampleMixin {
	@Inject(at = @At("HEAD"), method = "setLocalDifficulty")
	private void setLocalDifficulty(Difficulty difficulty, long timeOfDay, long inhabitedTime, float moonSize, CallbackInfoReturnable<Float> cir) {
		System.out.printf("D: %s, TOD: %d, IT: %d, MS: %.2f\n", difficulty.getName(), timeOfDay, inhabitedTime, moonSize);
		// print trace
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		for (StackTraceElement stackTraceElement : stackTrace) {
			System.out.println(stackTraceElement);
		}
	}
}