package selim.mob_block_shifters;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class BlockShifterDispenser implements DispenseItemBehavior {

	private final boolean isEmpty;

	public BlockShifterDispenser(boolean isEmpty) {
		this.isEmpty = isEmpty;
	}

	@Override
	public ItemStack dispense(BlockSource source, ItemStack itemStack) {
		BlockPos pos = source.getPos();
		Level world = source.getLevel();
		BlockState blockState = source.getBlockState();
		Direction facing = blockState.getValue(DispenserBlock.FACING);
		if (isEmpty)
			pos = pos.relative(facing);
		UseOnContext ctx = new UseOnContext(world, MobBlockShifters.getFakePlayer((ServerLevel) world),
				InteractionHand.MAIN_HAND, itemStack,
				new BlockHitResult(new Vec3(pos.getX(), pos.getY(), pos.getZ()), facing, pos, false));
		if (isEmpty)
			return BlockShifterItem.pickupBlock(ctx);
		return BlockShifterItem.placeBlock(ctx);
	}

}
