package net.succ.solar_punk.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.succ.solar_punk.SolarPunk;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, SolarPunk.MODID);

    // Geyser steam in a holiday colour (SeasonalSteam). Looks and moves like vanilla Cloud.
    public static final DeferredHolder<ParticleType<?>, ParticleType<ColorParticleOption>> TINTED_STEAM =
            PARTICLE_TYPES.register("tinted_steam", () -> new ParticleType<ColorParticleOption>(false) {
                @Override
                public MapCodec<ColorParticleOption> codec() {
                    return ColorParticleOption.codec(this);
                }

                @Override
                public StreamCodec<? super RegistryFriendlyByteBuf, ColorParticleOption> streamCodec() {
                    return ColorParticleOption.streamCodec(this);
                }
            });

    // Inside the Bioreactor's culture (AlgaeParticles): bubbles rising from the aerator, and
    // specks of algae drifting. Vanilla's bubble removes itself outside water blocks, so these
    // are our own. Both read their velocity Y as the room above them (see AlgaeParticle).
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ALGAE_BUBBLE =
            PARTICLE_TYPES.register("algae_bubble", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ALGAE_SPECK =
            PARTICLE_TYPES.register("algae_speck", () -> new SimpleParticleType(false));

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }
}
