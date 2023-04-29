package selim.mob_block_shifters;

import java.util.UUID;

import org.slf4j.Logger;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import selim.mob_block_shifters.MobShifterItem.ItemColorMobShifter;

@Mod.EventBusSubscriber(modid = MobBlockShifters.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@Mod(MobBlockShifters.MODID)
//@Mod(modid = MobBlockShifters.MODID, name = MobBlockShifters.NAME, version = MobBlockShifters.VERSION)
public class MobBlockShifters {

	public static final String MODID = "mob_block_shifters";
	public static final String NAME = "Mob & Block Shifters";
	public static final String VERSION = "1.0.0";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

	public static final RegistryObject<Item> MOB_SHIFTER = ITEMS.register("mob_shifter",
			() -> new MobShifterItem(false));
	public static final RegistryObject<Item> MOB_SHIFTER_EMPTY = ITEMS.register("mob_shifter_empty",
			() -> new MobShifterItem(true));
	public static final RegistryObject<Item> BLOCK_SHIFTER = ITEMS.register("block_shifter",
			() -> new BlockShifterItem(false));
	public static final RegistryObject<Item> BLOCK_SHIFTER_EMPTY = ITEMS.register("block_shifter_empty",
			() -> new BlockShifterItem(true));

	public MobBlockShifters() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		ITEMS.register(modEventBus);
		ModLoadingContext.get().getActiveContainer().addConfig(new Config(ModConfig.Type.COMMON, Config.CONFIG,
				ModLoadingContext.get().getActiveContainer(), MODID + "-common"));
	}

	@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class ClientModEvents {
		@SubscribeEvent
		public static void clientInit(RegisterColorHandlersEvent.Item event) {
			event.register(new ItemColorMobShifter(), MOB_SHIFTER.get());
		}
	}

	@SubscribeEvent
	public static void init(FMLCommonSetupEvent event) {
		DispenserBlock.registerBehavior(MOB_SHIFTER.get(), new MobShifterDispenser(false));
		DispenserBlock.registerBehavior(MOB_SHIFTER_EMPTY.get(), new MobShifterDispenser(true));
		DispenserBlock.registerBehavior(BLOCK_SHIFTER.get(), new BlockShifterDispenser(false));
		DispenserBlock.registerBehavior(BLOCK_SHIFTER_EMPTY.get(), new BlockShifterDispenser(true));
		MobBlockShifters.LOGGER.info("HI from dispenser registration");
	}

	public static FakePlayer getFakePlayer(ServerLevel world) {
		DimensionType dimID = world.dimensionType();
		FakePlayer fakePlayer = FakePlayerFactory.get(world,
				new GameProfile(UUID.randomUUID(), "MobBlockShifterFakePlayer-" + dimID));
		return fakePlayer;
	}

}