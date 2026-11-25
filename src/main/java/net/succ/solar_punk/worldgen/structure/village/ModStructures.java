package net.succ.solar_punk.worldgen.structure.village;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.succ.solar_punk.SolarPunk;

public class ModStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, SolarPunk.MODID);
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES =
            DeferredRegister.create(Registries.STRUCTURE_PIECE, SolarPunk.MODID);

    public static final DeferredHolder<StructureType<?>, StructureType<SimpleVillageStructure>> SIMPLE_VILLAGE =
            STRUCTURE_TYPES.register("simple_village", () -> () -> SimpleVillageStructure.CODEC);

    public static final DeferredHolder<StructurePieceType, StructurePieceType> VILLAGE_HOUSE =
            STRUCTURE_PIECES.register("village_house",
                    () -> (StructurePieceType.StructureTemplateType) VillageHousePiece::new);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> VILLAGE_STREET =
            STRUCTURE_PIECES.register("village_street",
                    () -> (StructurePieceType.ContextlessType) VillageStreetPiece::new);

    public static void register(IEventBus eventBus) {
        STRUCTURE_TYPES.register(eventBus);
        STRUCTURE_PIECES.register(eventBus);
    }
}
