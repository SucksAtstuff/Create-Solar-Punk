package net.succ.solar_punk.advancement;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class SolarpunkTrigger extends SimpleCriterionTrigger<SolarpunkTrigger.Instance> {

    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(ServerPlayer player) {
        trigger(player, i -> true);
    }

    public record Instance() implements SimpleInstance {
        static final Codec<Instance> CODEC = Codec.unit(new Instance());

        @Override
        public Optional<ContextAwarePredicate> player() {
            return Optional.empty();
        }
    }
}
