package gay.nyako.veindigging;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class VeinDiggingMod implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("veindigging");
	public static final VeinDiggingConfig CONFIG = AutoConfig.register(VeinDiggingConfig.class, GsonConfigSerializer::new).getConfig();
	public static final Identifier CHANGE_VEINDIGGING_STATE_PACKET = new Identifier("veindigging", "change_veindigging_state");
	public static final Identifier USING_CLIENT_MOD_PACKET = new Identifier("veindigging", "using_client_mod");
	public static final TagKey<Block> SHIFT_BLACKLIST_TAG = TagKey.of(Registry.BLOCK_KEY, new Identifier("veindigging", "shift_blacklist"));
	public static final TagKey<Block> SHIFT_WHITELIST_TAG = TagKey.of(Registry.BLOCK_KEY, new Identifier("veindigging", "shift_whitelist"));
	public static final TagKey<Block> BIND_BLACKLIST_TAG = TagKey.of(Registry.BLOCK_KEY, new Identifier("veindigging", "bind_blacklist"));
	public static final TagKey<Block> BIND_WHITELIST_TAG = TagKey.of(Registry.BLOCK_KEY, new Identifier("veindigging", "bind_whitelist"));
	public static final TagKey<Block> GLOBAL_BLACKLIST_TAG = TagKey.of(Registry.BLOCK_KEY, new Identifier("veindigging", "global_blacklist"));
	public static final TagKey<Block> GLOBAL_WHITELIST_TAG = TagKey.of(Registry.BLOCK_KEY, new Identifier("veindigging", "global_whitelist"));

	public static boolean CURRENTLY_VEINDIGGING = false;
	public static BlockPos CURRENT_BLOCK_POS = null;

	@Override
	public void onInitialize() {
		// Register on-break event
		CURRENTLY_VEINDIGGING = false;
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
			if (world.isClient()) return;
			if ((isShiftMining(player, state) || isBindMining(player, state)) && !CURRENTLY_VEINDIGGING) {
				CURRENTLY_VEINDIGGING = true;
				CURRENT_BLOCK_POS = pos;
				startVeinDigging(world, player, pos, state);
				CURRENTLY_VEINDIGGING = false;
			}
		});

		ServerPlayNetworking.registerGlobalReceiver(CHANGE_VEINDIGGING_STATE_PACKET, (server, player, handler, buffer, sender) -> {
			boolean state = buffer.readBoolean();
			server.execute(() -> {
				((PlayerEntityAccess) player).setVeinDigging(state);
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(USING_CLIENT_MOD_PACKET, (server, player, handler, buffer, sender) -> {
			server.execute(() -> {
				((PlayerEntityAccess) player).veindigging$setUsingClientMod(true);
			});
		});
	}

	private boolean isBindMining(PlayerEntity player, BlockState state) {
		if (!CONFIG.bindActivation) return false;
		if (!((PlayerEntityAccess) player).isVeinDigging()) return false;

		if (CONFIG.useGlobalWhitelist) {
			if (state.isIn(GLOBAL_WHITELIST_TAG)) return true;
		} else {
			if (state.isIn(GLOBAL_BLACKLIST_TAG)) return false;
		}

		if (CONFIG.useBindWhitelist) {
			return (state.isIn(BIND_WHITELIST_TAG));
		} else {
			return !state.isIn(BIND_BLACKLIST_TAG);
		}
	}

	private boolean isShiftMining(PlayerEntity player, BlockState state)
	{
		if (!CONFIG.sneakActivation) return false;
		if (((PlayerEntityAccess) player).veindigging$usingClientMod()) return false;
		if (!player.isSneaking()) return false;

		if (CONFIG.useGlobalWhitelist) {
			if (state.isIn(GLOBAL_WHITELIST_TAG)) return true;
		} else {
			if (state.isIn(GLOBAL_BLACKLIST_TAG)) return false;
		}

		if (CONFIG.useShiftWhitelist) {
			return (state.isIn(SHIFT_WHITELIST_TAG));
		} else {
			return !state.isIn(SHIFT_BLACKLIST_TAG);
		}
	}

	private void startVeinDigging(World world, PlayerEntity player, BlockPos pos, BlockState state) {
		ArrayList<BlockPos> blocksToBreak = new ArrayList<>();
		ArrayList<BlockPos> blocksChecked = new ArrayList<>();
		blocksToBreak.add(pos);

		Queue<BlockPos> blocksToCheck = new LinkedList<>();
		blocksToCheck.add(pos);

		while (!blocksToCheck.isEmpty()) {
			if (blocksToBreak.size() >= CONFIG.maxBlocks) {
				break;
			}
			BlockPos blockPos = blocksToCheck.poll();
			for (int x = -1; x <= 1; x++) {
				for (int y = -1; y <= 1; y++) {
					for (int z = -1; z <= 1; z++) {
						if (blocksToBreak.size() >= CONFIG.maxBlocks) {
							break;
						}

						BlockPos newPos = blockPos.add(x, y, z);

						// Make sure we don't check the same block twice
						if (blocksChecked.contains(newPos))
						{
							continue;
						}
						blocksChecked.add(newPos);

						// Make sure we don't check blocks that are too far away
						if (Math.abs(newPos.getX() - pos.getX()) > CONFIG.maxRange ||
								Math.abs(newPos.getY() - pos.getY()) > CONFIG.maxRange ||
								Math.abs(newPos.getZ() - pos.getZ()) > CONFIG.maxRange) {
							continue;
						}

						if (world.getBlockState(newPos).getBlock() == state.getBlock()) {
							blocksToBreak.add(newPos);
							blocksToCheck.add(newPos);
						}
					}
				}
			}
		}

		for (BlockPos blockPos : blocksToBreak) {
			ItemStack stack = player.getMainHandStack();
			if (stack.isDamageable() && stack.getDamage() >= stack.getMaxDamage() - 2) {
				break;
			}
			((ServerPlayerEntity) player).interactionManager.tryBreakBlock(blockPos);
		}
	}
}
