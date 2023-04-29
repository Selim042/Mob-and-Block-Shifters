package selim.mob_block_shifters;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;

public class MobShifterDispenser implements DispenseItemBehavior {

	private final boolean isEmpty;

	public MobShifterDispenser(boolean isEmpty) {
		this.isEmpty = isEmpty;
	}

	@Override
	public ItemStack dispense(BlockSource source, ItemStack itemStack) {
		Level world = source.getLevel();
		BlockState blockState = source.getBlockState();
		Direction facing = blockState.getValue(DispenserBlock.FACING);
		BlockPos pos = source.getPos().relative(facing);

		if (isEmpty) {
			List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, new AABB(pos),
					EntitySelector.NO_SPECTATORS);
			entities.removeIf((entity) -> entity instanceof Player);
			if (entities.size() == 0)
				return itemStack;
			return MobShifterItem.pickupEntity(itemStack, MobBlockShifters.getFakePlayer((ServerLevel) world),
					entities.get(0));
		} else {
			pos = pos.relative(facing.getOpposite());
			FakePlayer fakePlayer = MobBlockShifters.getFakePlayer((ServerLevel) world);
			UseOnContext ctx = new UseOnContext(world, fakePlayer, InteractionHand.MAIN_HAND, itemStack,
					new BlockHitResult(new Vec3(pos.getX(), pos.getY(), pos.getZ()), facing, pos, false));
			return MobShifterItem.placeEntity(ctx);
		}
	}

}