package io.github.frostmta.zadrinth.client.mixin;

import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Estado real da tecla (o dedo/tecla está mesmo apertando?). */
@Mixin(KeyBinding.class)
public interface KeyBindingAccessor {
	@Accessor("pressed")
	boolean zadrinth$isPhysicallyPressed();
}
