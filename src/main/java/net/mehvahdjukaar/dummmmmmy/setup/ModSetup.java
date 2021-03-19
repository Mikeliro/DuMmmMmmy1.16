package net.mehvahdjukaar.dummmmmmy.setup;

import net.mehvahdjukaar.dummmmmmy.common.NetworkHandler;
import net.mehvahdjukaar.dummmmmmy.entity.TargetDummyEntity;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;


public class ModSetup {

    public static void init(final FMLCommonSetupEvent event) {
        GlobalEntityTypeAttributes.put(Registry.TARGET_DUMMY.get(), TargetDummyEntity.setCustomAttributes().create());

        NetworkHandler.registerMessages();

        DispenserBlock.registerDispenseBehavior(Registry.DUMMY_ITEM.get(), new SpawnDummyBehavior());
    }

    public static class SpawnDummyBehavior implements IDispenseItemBehavior {
        @Override
        public ItemStack dispense(IBlockSource dispenser, ItemStack itemStack) {

            World world = dispenser.getWorld();
            Direction direction = dispenser.getBlockState().get(DispenserBlock.FACING);
            BlockPos pos = dispenser.getBlockPos().offset(direction);

            TargetDummyEntity dummy = new TargetDummyEntity(world);
            float f = direction.getHorizontalAngle();
            dummy.setLocationAndAngles(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, f, 0.0F);

            //EntityType.applyItemNBT(world, context.getPlayer(), dummy, itemstack.getTag());
            world.addEntity(dummy);

            itemStack.shrink(1);

            world.playEvent(1000, dispenser.getBlockPos(), 0);

            return itemStack;
        }
    }

}
