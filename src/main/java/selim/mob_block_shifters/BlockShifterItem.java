package selim.mob_block_shifters;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockShifterItem extends ShifterItem {

	public BlockShifterItem(boolean isEmpty) {
		super(isEmpty);
	}

	@Override
	public void appendHoverText(ItemStack itemStack, Level world, List<Component> list, TooltipFlag flag) {
		CompoundTag nbt = itemStack.getTag();
		if (!isEmpty && nbt != null && nbt.contains("state")) {
			BlockState storedBlockState = NbtUtils.readBlockState(nbt.getCompound("state"));
			Block storedBlock = storedBlockState.getBlock();
			Component displayName = storedBlock.getName();
			if (displayName != null)
				list.add(
						Component.translatable("misc." + MobBlockShifters.MODID + ".block_shifter_contains_known_block",
								displayName).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
			else
				list.add(Component
						.translatable("misc." + MobBlockShifters.MODID + ".block_shifter_contains_unknown_block")
						.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
			list.add(Component.empty());
			list.add(Component.translatable("misc." + MobBlockShifters.MODID + ".block_shifter_click_to_release")
					.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		} else {
			list.add(Component.translatable("misc." + MobBlockShifters.MODID + ".block_shifter_empty")
					.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
			list.add(Component.empty());
			list.add(Component.translatable("misc." + MobBlockShifters.MODID + ".block_shifter_click_to_pickup")
					.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		}
	}

	public static ItemStack pickupBlock(UseOnContext ctx) {
		Level world = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		ItemStack itemStack = ctx.getItemInHand();
		Player player = ctx.getPlayer();

		if (world.mayInteract(player, pos)) {
			BlockState selectedBlockState = world.getBlockState(pos);
			Block selectedBlock = selectedBlockState.getBlock();

			boolean configPickupCheck = !Config.isBlacklisted(selectedBlock);
			if (configPickupCheck && selectedBlock.defaultDestroyTime() < 0)
				configPickupCheck = Config.PICKUP_UNBREAKABLE_BLOCKS.get();
			if (!configPickupCheck)
				return itemStack;

			ItemStack newStack = new ItemStack(MobBlockShifters.BLOCK_SHIFTER.get());
			newStack.setTag(itemStack.getOrCreateTag());
			CompoundTag nbt = newStack.getOrCreateTag();
			nbt.put("state", NbtUtils.writeBlockState(selectedBlockState));
			BlockEntity tileEntity = world.getBlockEntity(pos);
			if (tileEntity != null) {
				CompoundTag entityData = tileEntity.serializeNBT();
				nbt.put("data", entityData);
				world.removeBlockEntity(pos);
			}
			world.removeBlock(pos, false);
			world.markAndNotifyBlock(pos, world.getChunkAt(pos), world.getBlockState(pos), world.getBlockState(pos), 0,
					0);
			world.updateNeighborsAt(pos, null);
			player.playNotifySound(SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
			world.playSound(player, pos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
			return newStack;
		}
		return itemStack;
	}

	public static ItemStack placeBlock(UseOnContext ctx) {
		Level world = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		ItemStack itemStack = ctx.getItemInHand();
		Player player = ctx.getPlayer();
		pos = pos.relative(ctx.getClickedFace());

		ItemStack newStack = new ItemStack(MobBlockShifters.BLOCK_SHIFTER_EMPTY.get());
		newStack.setTag(itemStack.getOrCreateTag());
		CompoundTag nbt = newStack.getOrCreateTag();
		if (nbt == null || !nbt.contains("state"))
			return itemStack;
		boolean replaceable = world.getBlockState(pos).canBeReplaced(new BlockPlaceContext(ctx));
		if (!world.isEmptyBlock(pos) && !replaceable)
			return itemStack;
		BlockState storedBlockState = NbtUtils.readBlockState(nbt.getCompound("state"));
		world.setBlockAndUpdate(pos, storedBlockState);
		BlockEntity tileEntity = world.getBlockEntity(pos);
		if (tileEntity != null && nbt.contains("data")) {
			CompoundTag entityData = nbt.getCompound("data");
			entityData.putInt("x", pos.getX());
			entityData.putInt("y", pos.getY());
			entityData.putInt("z", pos.getZ());
			tileEntity.deserializeNBT(entityData);
			nbt.remove("data");
		}
		world.markAndNotifyBlock(pos, world.getChunkAt(pos), world.getBlockState(pos), world.getBlockState(pos), 0, 0);
		world.updateNeighborsAt(pos, null);

		int damageToApply = 1;
		if (tileEntity != null)
			damageToApply *= Config.TILE_ENTITY_MOVE_DURABILTY_MULTIPLIER.get();
		if (storedBlockState.getBlock().defaultDestroyTime() < 0)
			damageToApply *= Config.UNBREAKABLE_BLOCK_MOVE_DURABILITY_MULTIPLIER.get();

		newStack.hurtAndBreak(damageToApply, ctx.getPlayer(), (e) -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
		player.playNotifySound(SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
		world.playSound(player, pos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
		return newStack;
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Level world = ctx.getLevel();
		if (!world.isClientSide) {
			Player player = ctx.getPlayer();
			ItemStack newStack;
			if (isEmpty)
				newStack = pickupBlock(ctx);
			else
				newStack = placeBlock(ctx);
			if (player != null) {
				InteractionHand hand = ctx.getHand();
				player.setItemInHand(hand, newStack);

				player.swing(hand);
			}
		}
		return InteractionResult.SUCCESS;
	}

}
