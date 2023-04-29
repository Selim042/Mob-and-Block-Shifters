package selim.mob_block_shifters;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

public class Config extends ModConfig {

	public static ForgeConfigSpec CONFIG;

	public static ForgeConfigSpec.BooleanValue VERBOSE;
	public static ForgeConfigSpec.IntValue BASE_TIER_MAX_DURABILITY;

	public static ForgeConfigSpec.ConfigValue<List<? extends String>> BLOCK_BLACKLIST;
	public static ForgeConfigSpec.BooleanValue PICKUP_UNBREAKABLE_BLOCKS;
	public static ForgeConfigSpec.IntValue UNBREAKABLE_BLOCK_MOVE_DURABILITY_MULTIPLIER;
	public static ForgeConfigSpec.IntValue TILE_ENTITY_MOVE_DURABILTY_MULTIPLIER;
	
	public static ForgeConfigSpec.ConfigValue<List<? extends String>> ENTITY_BLACKLIST;
	public static ForgeConfigSpec.BooleanValue PICKUP_BOSSES;
	public static ForgeConfigSpec.IntValue BOSS_MOVE_DURABILITY_MULTIPLIER;

	public Config(ModConfig.Type type, IConfigSpec<?> spec, ModContainer container, String fileName) {
		super(type, spec, container, fileName + ".toml");
	}

	static {
		ForgeConfigSpec.Builder commonBuilder = new ForgeConfigSpec.Builder();
		registerCommonConfig(commonBuilder);
		CONFIG = commonBuilder.build();
	}

	public static void registerCommonConfig(ForgeConfigSpec.Builder commonBuilder) {
		commonBuilder.comment("Configs for Mob & Block Shifters").push("general");

		VERBOSE = commonBuilder.comment("Should only be set to true if requested to do so.\nThis may spam your logs.")
				.define("verbose", false);
		BASE_TIER_MAX_DURABILITY = commonBuilder.comment("Maximum durability of the base tier Mob & Block Shifters.")
				.defineInRange("max_base_tier_durability", 64, 1, 1024);

		commonBuilder.pop().push("block_shifter");
		BLOCK_BLACKLIST = commonBuilder
				.comment("Blocks that should not be able to be picked up using the Block Shifter. (ex: minecraft:dirt)")
				.defineList("block_blacklist", getDefaultBlockBlacklist(), (entry) -> true);
		PICKUP_UNBREAKABLE_BLOCKS = commonBuilder.comment(
				"Should the Block Shifter be able to pickup unbreakable blocks? (ex: bedrock, barriers, portal blocks)")
				.define("pickup_unbreakable_blocks", false);
		UNBREAKABLE_BLOCK_MOVE_DURABILITY_MULTIPLIER = commonBuilder.comment(
				"Durability cost multiplier when trying to move unbreakable blocks, if enabled. (ex: bedrock, barriers, portal blocks)")
				.defineInRange("unbreakable_block_move_durabilty_modifier", 4, 1, 16);
		TILE_ENTITY_MOVE_DURABILTY_MULTIPLIER = commonBuilder
				.comment(
						"Durability cost multiplier when trying to move tile entities. (ex: chests, furnaces, barrels)")
				.defineInRange("tile_entity_move_durability_modifier", 2, 1, 16);

		commonBuilder.pop().push("mob_shifter");
		ENTITY_BLACKLIST = commonBuilder.comment(
				"Entities that should not be able to be picked up using the Mob Shifter. (ex: minecraft:creeper)")
				.defineList("entity_blacklist", getDefaultEntityBlacklist(), (entry) -> true);
		PICKUP_BOSSES = commonBuilder
				.comment("Should the Mob Shifter be able to pickup bosses? (ex: ender dragon and wither)")
				.define("pickup_bosses", false);
		BOSS_MOVE_DURABILITY_MULTIPLIER = commonBuilder.comment(
				"Durability cost multiplier when trying to move bosses, if enabled. (ex: ender dragon and wither)")
				.defineInRange("boss_move_durabilty_modifier", 4, 1, 16);
	}

	private static List<String> getDefaultBlockBlacklist() {
		List<String> defaultBlockBlacklist = new ArrayList<>();
		return defaultBlockBlacklist;
	}

	private static List<String> getDefaultEntityBlacklist() {
		List<String> defaultBlockBlacklist = new ArrayList<>();
		return defaultBlockBlacklist;
	}

	@SuppressWarnings("deprecation")
	public static boolean isBlacklisted(Block block) {
		return BLOCK_BLACKLIST.get().contains(block.builtInRegistryHolder().key().toString());
	}

	public static boolean isBlacklisted(Entity entity) {
		return ENTITY_BLACKLIST.get().contains(entity.getType().toString());
	}

}