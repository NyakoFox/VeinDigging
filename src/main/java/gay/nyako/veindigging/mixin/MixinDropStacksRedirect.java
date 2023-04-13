package gay.nyako.veindigging.mixin;

import gay.nyako.veindigging.VeinDiggingMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Block.class)
public class MixinDropStacksRedirect {

	@Redirect(method = "afterBreak", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;dropStacks(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/entity/BlockEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/item/ItemStack;)V"))
	private void veindigging$dropStacksRedirect(BlockState state, World world, BlockPos pos, BlockEntity blockEntity, Entity entity, ItemStack tool) {
		if (world instanceof ServerWorld) {
			Block.getDroppedStacks(state, (ServerWorld)world, pos, blockEntity, entity, tool).forEach(stack ->
			{
				if (VeinDiggingMod.CURRENTLY_VEINDIGGING) {
					Block.dropStack(world, VeinDiggingMod.CURRENT_BLOCK_POS, stack);
				} else {
					Block.dropStack(world, pos, stack);
				}
			});
			state.onStacksDropped((ServerWorld)world, pos, tool, true);
		}
	}
}
