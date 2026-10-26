package net.succ.solar_punk.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Walks a Particle Accelerator ring from its Input. Every ring block has two ports, the
 * neighbours its tube connects to (a bend or diagonal connects to a diagonal neighbour).
 * The walk follows the ports and each next block has to connect back; the ring is formed
 * when the walk comes back around to the Input.
 */
public final class AcceleratorRing {

    // Far beyond any real ring; only stops a runaway walk.
    private static final int MAX_STEPS = 100_000;

    public enum Status { FORMED, OPEN, UNLOADED, MULTIPLE_INPUTS }

    /** A neighbour offset in the flat ring grid. */
    public record Port(int dx, int dz) {
        Port rotatedClockwise() { return new Port(-dz, dx); }
        Port opposite() { return new Port(-dx, -dz); }
        BlockPos from(BlockPos pos) { return pos.offset(dx, 0, dz); }
    }

    /**
     * @param path      ring blocks in loop order, starting with the Input (formed rings only)
     * @param size      ring blocks in the loop, the Input included (formed rings only)
     * @param diameter  shorter side of the ring's footprint (formed rings only)
     * @param openEnd   the last connected block when the ring is open, else null
     * @param openFacing the port that block points out of, when open
     */
    public record Result(Status status, List<BlockPos> path, int size, int diameter,
                         @Nullable BlockPos openEnd, @Nullable Port openFacing) {
        public boolean formed() { return status == Status.FORMED; }
    }

    private AcceleratorRing() {}

    /** The ring block's two ports, or null if it isn't a ring block. Shapes face north in their models. */
    @Nullable
    public static List<Port> portsOf(BlockState state) {
        List<Port> north;
        if (state.getBlock() instanceof ParticleAcceleratorInputBlock) {
            north = List.of(new Port(0, -1), new Port(0, 1));
        } else if (state.getBlock() instanceof ParticleAcceleratorPipeBlock) {
            PipeShape shape = PipeShape.of(state.getBlock());
            if (shape == null) return null;
            north = switch (shape) {
                case STRAIGHT -> List.of(new Port(0, -1), new Port(0, 1));
                // the curve runs from the north face to the south-west / south-east corner
                case BEND -> List.of(new Port(0, -1), new Port(-1, 1));
                case BEND_MIRRORED -> List.of(new Port(0, -1), new Port(1, 1));
                case DIAGONAL -> List.of(new Port(1, -1), new Port(-1, 1));
            };
        } else {
            return null;
        }
        // blockstates turn the model clockwise: east 1 step, south 2, west 3
        int turns = turns(state);
        Port a = north.get(0), b = north.get(1);
        for (int i = 0; i < turns; i++) {
            a = a.rotatedClockwise();
            b = b.rotatedClockwise();
        }
        return List.of(a, b);
    }

    public static Result scan(Level level, BlockPos inputPos) {
        return scan(level, inputPos, true);
    }

    /** @param checkLoaded false for Ponder's scene world, which doesn't track loaded chunks */
    public static Result scan(Level level, BlockPos inputPos, boolean checkLoaded) {
        List<Port> start = portsOf(level.getBlockState(inputPos));
        if (start == null) return new Result(Status.OPEN, List.of(), 0, 0, inputPos, null);

        Set<BlockPos> visited = new HashSet<>();
        visited.add(inputPos);
        List<BlockPos> path = new ArrayList<>();
        path.add(inputPos);
        int minX = inputPos.getX(), maxX = minX, minZ = inputPos.getZ(), maxZ = minZ;

        BlockPos previous = inputPos;
        Port heading = start.get(0);
        for (int step = 0; step < MAX_STEPS; step++) {
            BlockPos current = heading.from(previous);
            if (checkLoaded && !level.isLoaded(current))
                return new Result(Status.UNLOADED, List.of(), 0, 0, null, null);

            BlockState state = level.getBlockState(current);
            List<Port> ports = portsOf(state);
            Port back = heading.opposite();
            if (ports == null || !ports.contains(back))
                return new Result(Status.OPEN, List.of(), 0, 0, previous, heading);

            if (current.equals(inputPos)) {
                int diameter = Math.min(maxX - minX, maxZ - minZ) + 1;
                return new Result(Status.FORMED, path, path.size(), diameter, null, null);
            }
            if (state.getBlock() instanceof ParticleAcceleratorInputBlock)
                return new Result(Status.MULTIPLE_INPUTS, List.of(), 0, 0, null, null);
            if (!visited.add(current))
                return new Result(Status.OPEN, List.of(), 0, 0, previous, heading);
            path.add(current);

            minX = Math.min(minX, current.getX());
            maxX = Math.max(maxX, current.getX());
            minZ = Math.min(minZ, current.getZ());
            maxZ = Math.max(maxZ, current.getZ());

            heading = ports.get(0).equals(back) ? ports.get(1) : ports.get(0);
            previous = current;
        }
        return new Result(Status.OPEN, List.of(), 0, 0, previous, heading);
    }

    /**
     * The centre line of the tube around the whole ring, in world coordinates, for the
     * travelling particle. Follows each block's model: straight through a pipe, corner to
     * corner through a diagonal, and along the curve (straight, 22.5, then 45 degrees)
     * through a bend.
     */
    public static List<Vec3> tubePath(Level level, List<BlockPos> path) {
        List<Vec3> points = new ArrayList<>();
        int n = path.size();
        for (int i = 0; i < n; i++) {
            BlockPos pos = path.get(i);
            BlockState state = level.getBlockState(pos);
            List<Port> ports = portsOf(state);
            if (ports == null) return List.of();

            // The piece's points in pixels, from its first port to its second
            List<double[]> local = new ArrayList<>();
            local.add(portPoint(ports.get(0)));
            PipeShape shape = PipeShape.of(state.getBlock());
            if (shape == PipeShape.BEND || shape == PipeShape.BEND_MIRRORED) {
                int turns = turns(state);
                double curveX = shape == PipeShape.BEND ? 5.1716 : 10.8284;
                for (double[] p : new double[][]{{8, 4}, {curveX, 10.8284}})
                    local.add(rotateClockwise(p, turns));
            }
            local.add(portPoint(ports.get(1)));

            // Walk it in the ring's direction: in from the previous block
            BlockPos previous = path.get((i - 1 + n) % n);
            Port entry = new Port(previous.getX() - pos.getX(), previous.getZ() - pos.getZ());
            if (!entry.equals(ports.get(0))) Collections.reverse(local);

            for (double[] p : local)
                points.add(new Vec3(pos.getX() + p[0] / 16, pos.getY() + 0.5, pos.getZ() + p[1] / 16));
        }
        return points;
    }

    private static double[] portPoint(Port port) {
        return new double[]{8 + 8 * port.dx(), 8 + 8 * port.dz()};
    }

    private static int turns(BlockState state) {
        return switch (state.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> 0;
        };
    }

    /** Turns a point in a block's model clockwise around its centre, like a blockstate's y rotation. */
    private static double[] rotateClockwise(double[] p, int turns) {
        double x = p[0], z = p[1];
        for (int i = 0; i < turns; i++) {
            double nx = 16 - z;
            z = x;
            x = nx;
        }
        return new double[]{x, z};
    }

    /** The side the Input's item cap sticks out of (its left when facing away from you). */
    public static Direction capSide(BlockState inputState) {
        return inputState.getValue(BlockStateProperties.HORIZONTAL_FACING).getCounterClockWise();
    }
}
