package net.mehvahdjukaar.dummmmmmy.dispenser;

import net.mehvahdjukaar.dummmmmmy.entity.TargetDummyEntity;
import net.mehvahdjukaar.dummmmmmy.item.TargetDummyItem;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

public class DispenserBehavior {

    private static void register(IItemProvider provider, IDispenseItemBehavior behavior) {
        DispenserBlock.registerDispenseBehavior(provider, behavior);
    }

    public static void registerBehaviors() {
        for(Item item : ForgeRegistries.ITEMS) {
            if(item instanceof TargetDummyItem){
                register(item, new SpawnDummyBehavior());
            }
        }
    }

    public static class SpawnDummyBehavior implements IDispenseItemBehavior {

        @Override
        public ItemStack dispense(IBlockSource dispenser, ItemStack itemStack) {
            if(!(itemStack.getItem() instanceof TargetDummyItem)) {
                return itemStack;
            }

            World world = dispenser.getWorld();
            Direction direction = dispenser.getBlockState().get(DispenserBlock.FACING);
            BlockPos pos = dispenser.getBlockPos().offset(direction);

            TargetDummyEntity.DummyMob dummy = new TargetDummyEntity.DummyMob(world);
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