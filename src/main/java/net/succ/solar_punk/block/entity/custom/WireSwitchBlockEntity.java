package net.succ.solar_punk.block.entity.custom;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.succ.solar_punk.Config;
import net.succ.solar_punk.block.custom.RedstoneWireRelayBlock;
import net.succ.solar_punk.block.custom.WireSwitchBlock;

import java.util.List;

/** The Wire Switch and Redstone Wire Relay: two terminals, joined while the switch is closed. */
public class WireSwitchBlockEntity extends WireNodeBlockEntity implements IHaveGoggleInformation {

    public WireSwitchBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public int terminals() {
        return 2;
    }

    @Override
    public boolean joinsTerminals() {
        BlockState state = getBlockState();
        return state.hasProperty(WireSwitchBlock.CLOSED) && state.getValue(WireSwitchBlock.CLOSED);
    }

    public void tick() {
        if (level != null && !level.isClientSide) validateWires();
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        BlockState state = getBlockState();
        boolean relay = state.hasProperty(RedstoneWireRelayBlock.INVERTED);
        CreateLang.translate(relay ? "solar_punk.tooltip.redstone_wire_relay_header"
                : "solar_punk.tooltip.wire_switch_header").forGoggles(tooltip);

        boolean closed = joinsTerminals();
        CreateLang.translate("solar_punk.tooltip.switch_state")
                .style(ChatFormatting.GRAY)
                .add(CreateLang.translate(closed ? "solar_punk.tooltip.switch_closed" : "solar_punk.tooltip.switch_open")
                        .style(closed ? ChatFormatting.GREEN : ChatFormatting.RED))
                .forGoggles(tooltip, 1);

        if (relay) {
            boolean inverted = state.getValue(RedstoneWireRelayBlock.INVERTED);
            CreateLang.translate("solar_punk.tooltip.relay_closes")
                    .style(ChatFormatting.GRAY)
                    .add(CreateLang.translate(inverted ? "solar_punk.tooltip.relay_unpowered"
                            : "solar_punk.tooltip.relay_powered").style(ChatFormatting.AQUA))
                    .forGoggles(tooltip, 1);
        }

        CreateLang.translate("solar_punk.tooltip.wires")
                .style(ChatFormatting.GRAY)
                .add(Component.literal(countOn(0) + " / " + Config.wireMaxConnections + ", "
                        + countOn(1) + " / " + Config.wireMaxConnections).withStyle(ChatFormatting.AQUA))
                .forGoggles(tooltip, 1);
        return true;
    }
}
