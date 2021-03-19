
package net.mehvahdjukaar.dummmmmmy.item;

import net.mehvahdjukaar.dummmmmmy.entity.TargetDummyEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;

public class TargetDummyItem extends Item {
	public TargetDummyItem(Properties builder) {
		super(builder);
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		Direction direction = context.getFace();
		if (direction == Direction.DOWN) {
			return ActionResultType.FAIL;
		} else {
			World world = context.getWorld();
			BlockItemUseContext blockitemusecontext = new BlockItemUseContext(context);
			BlockPos blockpos = blockitemusecontext.getPos();
			BlockPos blockpos1 = blockpos.up();
			if (blockitemusecontext.canPlace() && world.getBlockState(blockpos1).isReplaceable(blockitemusecontext)) {
				double d0 = blockpos.getX();
				double d1 = blockpos.getY();
				double d2 = blockpos.getZ();
				List<Entity> list = world.getEntitiesWithinAABBExcludingEntity((Entity) null,
						new AxisAlignedBB(d0, d1, d2, d0 + 1.0D, d1 + 2.0D, d2 + 1.0D));
				if (!list.isEmpty()) {
					return ActionResultType.FAIL;
				} else {
					ItemStack itemstack = context.getItem();
					if (!world.isRemote) {
						world.removeBlock(blockpos, false);
						world.removeBlock(blockpos1, false);
						TargetDummyEntity dummy = new TargetDummyEntity(world);
						float f = (float) MathHelper.floor((MathHelper.wrapDegrees(context.getPlacementYaw() - 180.0F) + 11.25) / 22.5F) * 22.5F;
						dummy.setLocationAndAngles(d0 + 0.5D, d1, d2 + 0.5D, f, 0.0F);
						EntityType.applyItemNBT(world, context.getPlayer(), dummy, itemstack.getTag());
						world.addEntity(dummy);
						world.playSound(null, dummy.getPosX(), dummy.getPosY(), dummy.getPosZ(), SoundEvents.BLOCK_BAMBOO_PLACE,
								SoundCategory.BLOCKS, 0.75F, 0.8F);
					}
					itemstack.shrink(1);
					return ActionResultType.SUCCESS;
				}
			} else {
				return ActionResultType.FAIL;
			}
		}
	}
}

