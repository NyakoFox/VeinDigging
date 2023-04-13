package gay.nyako.veindigging.mixin;

import gay.nyako.veindigging.PlayerEntityAccess;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerEntity.class)
public class MixinPlayerEntityAccess implements PlayerEntityAccess {
	private boolean isVeinDigging = false;
	private boolean usingClientMod = false;

	@Override
	public boolean isVeinDigging() {
		return isVeinDigging;
	}

	@Override
	public void setVeinDigging(boolean isVeinDigging) {
		this.isVeinDigging = isVeinDigging;
	}

	@Override
	public boolean usingClientMod() {
		return usingClientMod;
	}

	@Override
	public void setUsingClientMod(boolean usingClientMod) {
		this.usingClientMod = usingClientMod;
	}

}
