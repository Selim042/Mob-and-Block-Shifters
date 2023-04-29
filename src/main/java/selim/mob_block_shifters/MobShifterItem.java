package selim.mob_block_shifters;

import java.util.List;
import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.common.Tags;

public class MobShifterItem extends ShifterItem {

	public MobShifterItem(boolean isEmpty) {
		super(isEmpty);
	}

	@Override
	public void appendHoverText(ItemStack itemStack, Level world, List<Component> list, TooltipFlag flag) {
		CompoundTag nbt = itemStack.getTag();
		if (!isEmpty && nbt != null && nbt.contains("entity")) {
			Optional<EntityType<?>> optEntityType = EntityType.byString(nbt.getString("entity"));
			if (optEntityType.isPresent()) {
				String entityName = optEntityType.get().getDescription().getString();
				list.add(Component.translatable("misc." + MobBlockShifters.MODID + ".mob_shifter_contains_known_entity",
						entityName).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
			} else
				list.add(Component
						.translatable("misc." + MobBlockShifters.MODID + ".mob_shifter_contains_unknown_entity")
						.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
			list.add(Component.empty());
			list.add(Component.translatable("misc." + MobBlockShifters.MODID + ".mob_shifter_click_to_release")
					.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		} else {
			list.add(Component.translatable("misc." + MobBlockShifters.MODID + ".mob_shifter_empty")
					.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
			list.add(Component.empty());
			list.add(Component.translatable("misc." + MobBlockShifters.MODID + ".mob_shifter_click_to_pickup")
					.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		}
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity entity,
			InteractionHand hand) {
		// TODO move pickup to here
		// World world = entity.getEntityWorld();
		// if (!world.isRemote) {
		// NBTTagCompound nbt = itemStack.getTagCompound();
		// if (nbt == null) {
		// itemStack.setTagCompound(new NBTTagCompound());
		// nbt = itemStack.getTagCompound();
		// }
		// if (nbt.getString("entity").equals("")) { // picking up entity
		// nbt.setString("entity", EntityList.getEntityString(entity));
		// NBTTagCompound entityNbt = new NBTTagCompound();
		// entity.writeToNBT(entityNbt);
		// nbt.setTag("data", entityNbt);
		// world.removeEntity(entity);
		// world.playSound(player, new
		// BlockPos(entity.posX,entity.posY,entity.posZ),
		// SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 1.0F,
		// 1.0F);
		// }
		// }
		return InteractionResult.SUCCESS;
	}

	@Override
	public boolean onLeftClickEntity(ItemStack itemStack, Player player, Entity entity) {
		pickupEntity(itemStack, player, entity);
		return true;
	}

	public static ItemStack pickupEntity(ItemStack itemStack, Player player, Entity entity) {
		Level world = entity.getLevel();
		boolean isBoss = entity.getType().getTags().anyMatch((tag) -> tag.equals(Tags.EntityTypes.BOSSES));
		boolean configPickupCheck = !Config.isBlacklisted(entity);
		if (configPickupCheck && isBoss)
			configPickupCheck = Config.PICKUP_BOSSES.get();

		if (!world.isClientSide && configPickupCheck && entity instanceof LivingEntity && !(entity instanceof Player)) {
			CompoundTag nbt = itemStack.getTag();
			if (nbt == null) {
				nbt = new CompoundTag();
				itemStack.setTag(nbt);
			}
			if (!nbt.contains("entity")) { // picking up entity
				ItemStack newStack = new ItemStack(MobBlockShifters.MOB_SHIFTER.get());
				newStack.setTag(itemStack.getOrCreateTag());
				nbt = newStack.getOrCreateTag();

				nbt.putString("entity", EntityType.getKey(entity.getType()).toString());
				CompoundTag entityNbt = new CompoundTag();
				entity.save(entityNbt);
				nbt.put("data", entityNbt);
				entity.remove(RemovalReason.DISCARDED);
				player.playNotifySound(SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
				world.playSound(player, new BlockPos(entity.xo, entity.yo, entity.zo), SoundEvents.ENDERMAN_TELEPORT,
						SoundSource.PLAYERS, 1.0F, 1.0F);

				player.setItemInHand(InteractionHand.MAIN_HAND, newStack);
				return newStack;
			}
		}
		return itemStack;
	}

	public static ItemStack placeEntity(UseOnContext ctx) {
		Level world = ctx.getLevel();
		Player player = ctx.getPlayer();
		InteractionHand hand = ctx.getHand();
		BlockPos pos = ctx.getClickedPos();
		Direction facing = ctx.getClickedFace();
		ItemStack itemStack = ctx.getItemInHand();
		if (!world.isClientSide) {
			CompoundTag nbt = itemStack.getTag();
			if (nbt == null)
				return itemStack;
			ItemStack newStack = new ItemStack(MobBlockShifters.MOB_SHIFTER_EMPTY.get());
			newStack.setTag(itemStack.getOrCreateTag());
			nbt = newStack.getOrCreateTag();

			pos = pos.relative(facing);
			Optional<Entity> optEntityType = EntityType.create(nbt.getCompound("data"), world);
			if (optEntityType.isEmpty())
				return itemStack;
			Entity entity = optEntityType.get();
			nbt.remove("entity");
			nbt.remove("data");
			entity.moveTo(pos.getX() + 0.5, pos.getY() + 0.25, pos.getZ() + 0.5);
			world.addFreshEntity(entity);
			player.setItemInHand(hand, newStack);

			boolean isBoss = entity.getType().getTags().anyMatch((tag) -> tag.equals(Tags.EntityTypes.BOSSES));
			int damageToApply = 1;
			if (isBoss)
				damageToApply *= Config.BOSS_MOVE_DURABILITY_MULTIPLIER.get();

			newStack.hurtAndBreak(damageToApply, player, (e) -> e.broadcastBreakEvent(EquipmentSlot.MAINHAND));
			player.playNotifySound(SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
			world.playSound(player, new BlockPos(entity.xo, entity.yo, entity.zo), SoundEvents.ENDERMAN_TELEPORT,
					SoundSource.PLAYERS, 1.0F, 1.0F);
			return newStack;
		}
		return itemStack;
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx) {
		Level world = ctx.getLevel();
		if (!world.isClientSide) {
			Player player = ctx.getPlayer();
			ItemStack newStack;
//			if (isEmpty)
//				newStack = pickupBlock(ctx);
//			else
			if (!isEmpty)
				newStack = placeEntity(ctx);
			else
				return InteractionResult.FAIL;
			if (player != null) {
				InteractionHand hand = ctx.getHand();
				player.setItemInHand(hand, newStack);

				player.swing(hand);
			}
		}
		return InteractionResult.SUCCESS;
	}

	@OnlyIn(Dist.CLIENT)
	public static class ItemColorMobShifter implements ItemColor {

		@Override
		public int getColor(ItemStack stack, int tintIndex) {
			if (tintIndex == 0)
				return -1;
			CompoundTag nbt = stack.getTag();
			if (nbt == null)
				return -1;
			Optional<EntityType<?>> optEntityType = EntityType.byString(nbt.getString("entity"));
			if (optEntityType.isPresent()) {
				SpawnEggItem eggInfo = ForgeSpawnEggItem.fromEntityType(optEntityType.get());
				return eggInfo == null ? -1 : eggInfo.getColor(tintIndex - 1);
			}
			return -1;
		}

	}

}
