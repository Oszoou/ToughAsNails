/*******************************************************************************
 * Copyright 2021, the Glitchfiend Team.
 * All rights reserved.
 ******************************************************************************/
package toughasnails.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import toughasnails.api.block.TANBlocks;
import toughasnails.api.item.TANItems;
import toughasnails.api.thirst.ThirstHelper;
import toughasnails.block.RainCollectorBlock;
import toughasnails.init.ModTags;

public class FilledCanteenItem extends Item
{
    int tier;

    public FilledCanteenItem(int tier, Properties properties)
    {
        super(properties);
        this.tier = tier;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand)
    {
        ItemStack stack = player.getItemInHand(hand);
        HitResult rayTraceResult = getPlayerPOVHitResult(world, player, ClipContext.Fluid.SOURCE_ONLY);

        if (rayTraceResult.getType() == HitResult.Type.BLOCK)
        {
            BlockPos pos = ((BlockHitResult)rayTraceResult).getBlockPos();

            if (world.mayInteract(player, pos))
            {
                BlockState state = world.getBlockState(pos);

                if (state.getBlock() instanceof RainCollectorBlock)
                {
                    // Fill the canteen from purified water from a rain collector
                    int waterLevel = state.getValue(RainCollectorBlock.LEVEL);

                    if (waterLevel > 0 && !world.isClientSide())
                    {
                        world.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0F, 1.0F);
                        ((RainCollectorBlock) TANBlocks.RAIN_COLLECTOR).setWaterLevel(world, pos, state, waterLevel - 1);
                        return InteractionResultHolder.success(this.replaceCanteen(stack, player, new ItemStack(getPurifiedWaterCanteen())));
                    }
                }
                else if (world.getFluidState(pos).is(FluidTags.WATER))
                {
                    // Fill the canteen with water in the world
                    world.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0F, 1.0F);

                    Holder<Biome> biome = player.level().getBiome(player.blockPosition());
                    Item canteenItem;

                    if (biome.is(ModTags.Biomes.DIRTY_WATER_BIOMES))
                    {
                        canteenItem = getDirtyWaterCanteen();
                    }
                    else if (biome.is(ModTags.Biomes.PURIFIED_WATER_BIOMES))
                    {
                        canteenItem = getPurifiedWaterCanteen();
                    }
                    else
                    {
                        canteenItem = getWaterCanteen();
                    }

                    return InteractionResultHolder.sidedSuccess(this.replaceCanteen(stack, player, new ItemStack(canteenItem)), world.isClientSide());
                }
            }
        }

        if (ThirstHelper.canDrink(player, this.canAlwaysDrink()))
        {
            return ItemUtils.startUsingInstantly(world, player, hand);
        }

        return InteractionResultHolder.fail(player.getItemInHand(hand));
    }

    protected ItemStack replaceCanteen(ItemStack oldStack, Player player, ItemStack newStack)
    {
        player.awardStat(Stats.ITEM_USED.get(this));
        return ItemUtils.createFilledResult(oldStack, player, newStack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity entityLiving)
    {
        Player player = entityLiving instanceof Player ? (Player)entityLiving : null;

        // Do nothing if this isn't a player
        if (player == null)
            return stack;

        player.awardStat(Stats.ITEM_USED.get(this));

        // Damage the item if we're on the server and the player isn't in creative mode
        if (!worldIn.isClientSide && !player.getAbilities().instabuild)
        {
            boolean[] broken = new boolean[]{false};
            stack.hurtAndBreak(1, player, (entity) -> broken[0] = true);
            if (broken[0])
            {
                return new ItemStack(getEmptyCanteen());
            }
        }

        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack)
    {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack)
    {
        return UseAnim.DRINK;
    }

    @Override
    public boolean isEnchantable(ItemStack stack)
    {
        return false;
    }

    public boolean canAlwaysDrink()
    {
        return false;
    }

    public Item getEmptyCanteen()
    {
        switch (this.tier)
        {
            default: case 0: return TANItems.EMPTY_LEATHER_CANTEEN;
            case 1: return TANItems.EMPTY_COPPER_CANTEEN;
            case 2: return TANItems.EMPTY_IRON_CANTEEN;
            case 3: return TANItems.EMPTY_GOLD_CANTEEN;
            case 4: return TANItems.EMPTY_DIAMOND_CANTEEN;
            case 5: return TANItems.EMPTY_NETHERITE_CANTEEN;
        }
    }

    public Item getDirtyWaterCanteen()
    {
        switch (this.tier)
        {
            default: case 0: return TANItems.LEATHER_DIRTY_WATER_CANTEEN;
            case 1: return TANItems.COPPER_DIRTY_WATER_CANTEEN;
            case 2: return TANItems.IRON_DIRTY_WATER_CANTEEN;
            case 3: return TANItems.GOLD_DIRTY_WATER_CANTEEN;
            case 4: return TANItems.DIAMOND_DIRTY_WATER_CANTEEN;
            case 5: return TANItems.NETHERITE_DIRTY_WATER_CANTEEN;
        }
    }

    public Item getWaterCanteen()
    {
        switch (this.tier)
        {
            default: case 0: return TANItems.LEATHER_WATER_CANTEEN;
            case 1: return TANItems.COPPER_WATER_CANTEEN;
            case 2: return TANItems.IRON_WATER_CANTEEN;
            case 3: return TANItems.GOLD_WATER_CANTEEN;
            case 4: return TANItems.DIAMOND_WATER_CANTEEN;
            case 5: return TANItems.NETHERITE_WATER_CANTEEN;
        }
    }

    public Item getPurifiedWaterCanteen()
    {
        switch (this.tier)
        {
            default: case 0: return TANItems.LEATHER_PURIFIED_WATER_CANTEEN;
            case 1: return TANItems.COPPER_PURIFIED_WATER_CANTEEN;
            case 2: return TANItems.IRON_PURIFIED_WATER_CANTEEN;
            case 3: return TANItems.GOLD_PURIFIED_WATER_CANTEEN;
            case 4: return TANItems.DIAMOND_PURIFIED_WATER_CANTEEN;
            case 5: return TANItems.NETHERITE_PURIFIED_WATER_CANTEEN;
        }
    }
}
