package net.succ.solar_punk.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.AABB;
import net.succ.solar_punk.advancement.ModTriggers;
import net.succ.solar_punk.block.ModBlocks;

import java.util.List;

/**
 * A Dispenser puts Wire Connectors and Grow Lamps on the head of a player or armor stand in
 * front of it, like it does with armor. Only a Dispenser can: the items aren't wearable, so
 * they can't be put on by hand. Otherwise they're dispensed like any other item.
 */
public class HeadwearDispenseBehavior extends DefaultDispenseItemBehavior {

    @Override
    protected ItemStack execute(BlockSource source, ItemStack stack) {
        BlockPos front = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
        List<LivingEntity> targets = source.level().getEntitiesOfClass(LivingEntity.class, new AABB(front),
                e -> e.isAlive() && !e.isSpectator() && (e instanceof Player || e instanceof ArmorStand)
                        && e.getItemBySlot(EquipmentSlot.HEAD).isEmpty());
        if (targets.isEmpty()) return super.execute(source, stack);

        LivingEntity target = targets.get(0);
        var trigger = stack.is(ModBlocks.GROW_LAMP.get().asItem())
                ? ModTriggers.DISPENSED_GROW_LAMP : ModTriggers.DISPENSED_WIRE_CONNECTOR;
        target.setItemSlot(EquipmentSlot.HEAD, stack.split(1));
        source.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.ARMOR_EQUIP_CHAIN, SoundSource.BLOCKS, 1.0f, 1.0f);
        if (target instanceof ServerPlayer player) trigger.get().trigger(player);
        return stack;
    }
}
