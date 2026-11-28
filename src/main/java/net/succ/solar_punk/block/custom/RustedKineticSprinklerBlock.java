package net.succ.solar_punk.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.succ.solar_punk.block.ModBlocks;

/**
 * A rusted, seized-up Kinetic Sprinkler for ruins: a static OBJ copy of the sprinkler's GeckoLib
 * model (rusted_kinetic_sprinkler.obj, converted from kinetic_sprinkler.bbmodel) with
 * rusty_sprinkler.png, and no block entity. An axe scrapes it back into a working Kinetic
 * Sprinkler, like the Rusted Geyser Cap.
 */
public class RustedKineticSprinklerBlock extends Block {
    public RustedKineticSprinklerBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return KineticSprinklerBlock.SHAPE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!RuinRepair.canScrape(stack))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        BlockState sprinkler = ModBlocks.KINETIC_SPRINKLER.get().defaultBlockState();
        if (!level.isClientSide) level.setBlock(pos, sprinkler, Block.UPDATE_ALL);
        RuinRepair.scrape(level, pos, sprinkler, player, stack, hand);
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
}
