package selim.mob_block_shifters;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class ShifterItem extends Item {

	final protected boolean isEmpty;

	public ShifterItem(boolean isEmpty) {
		super(new Item.Properties().tab(isEmpty ? CreativeModeTab.TAB_TOOLS : null));
		this.isEmpty = isEmpty;
	}

	@Override
	public Rarity getRarity(ItemStack stack) {
		if (!isEmpty)
			return Rarity.RARE;
		return Rarity.UNCOMMON;
	}

	@Override
	public int getMaxStackSize(ItemStack stack) {
		return 1;
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return !isEmpty;
//		return false;
	}

	@Override
	public boolean isDamageable(ItemStack stack) {
		return true;
	}

	@Override
	public int getMaxDamage(ItemStack stack) {
		return 32;
	}

}
