package tudbut.mod.polyfire.utils;

import io.netty.buffer.Unpooled;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDeadBush;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import tudbut.mod.polyfire.PolyFire;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class BlockUtils {
    
    private static final Minecraft mc = Minecraft.getMinecraft();
    
    // Sneak!
    public static ArrayList<Block> blackList = new ArrayList<>(Arrays.asList(
            Blocks.ENDER_CHEST,
            Blocks.CHEST,
            Blocks.TRAPPED_CHEST,
            Blocks.CRAFTING_TABLE,
            Blocks.ANVIL,
            Blocks.BREWING_STAND,
            Blocks.HOPPER,
            Blocks.DROPPER,
            Blocks.DISPENSER,
            Blocks.TRAPDOOR,
            Blocks.ENCHANTING_TABLE,
            Blocks.POWERED_COMPARATOR,
            Blocks.UNPOWERED_COMPARATOR,
            Blocks.POWERED_REPEATER,
            Blocks.UNPOWERED_REPEATER,
            Blocks.CAKE,
            Blocks.STANDING_SIGN,
            Blocks.WALL_SIGN,
            Blocks.OAK_DOOR,
            Blocks.SPRUCE_DOOR,
            Blocks.BIRCH_DOOR,
            Blocks.DARK_OAK_DOOR,
            Blocks.IRON_DOOR,
            Blocks.JUNGLE_DOOR,
            Blocks.ACACIA_DOOR,
            Blocks.IRON_TRAPDOOR
    ));
    public static ArrayList<Block> shulkerList = new ArrayList<>(Arrays.asList(
            Blocks.WHITE_SHULKER_BOX,
            Blocks.ORANGE_SHULKER_BOX,
            Blocks.MAGENTA_SHULKER_BOX,
            Blocks.LIGHT_BLUE_SHULKER_BOX,
            Blocks.YELLOW_SHULKER_BOX,
            Blocks.LIME_SHULKER_BOX,
            Blocks.PINK_SHULKER_BOX,
            Blocks.GRAY_SHULKER_BOX,
            Blocks.SILVER_SHULKER_BOX,
            Blocks.CYAN_SHULKER_BOX,
            Blocks.PURPLE_SHULKER_BOX,
            Blocks.BLUE_SHULKER_BOX,
            Blocks.BROWN_SHULKER_BOX,
            Blocks.GREEN_SHULKER_BOX,
            Blocks.RED_SHULKER_BOX,
            Blocks.BLACK_SHULKER_BOX
    ));
    
    public static void attackEntityByID(int id) {
        CPacketUseEntity packet = new CPacketUseEntity();
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        buf.writeVarInt(id);
        buf.writeEnumValue(CPacketUseEntity.Action.ATTACK);
        try {
            packet.readPacketData(buf);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        PolyFire.player.connection.sendPacket(packet);
    }
    
    // Magic i don't want to explain, gets a placeable size of a neighbor block and places
    // the block on it, uses getPlaceableSide
    public static void placeBlock(BlockPos pos, boolean rotate) {
        if(pos == null)
            return;
        
        EnumFacing side = getPlaceableSide(pos);
        if (side == null) {
            //ChatUtils.print("Couldn't place a block");
            return;
        }
        BlockPos neighbour = pos.offset(side);
        EnumFacing opposite = side.getOpposite();
        Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
        Block neighbourBlock = mc.world.getBlockState(neighbour).getBlock();
        if ((BlockUtils.blackList.contains(neighbourBlock) || BlockUtils.shulkerList.contains(neighbourBlock))) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
        }
        if (rotate) BlockUtils.faceVectorPacketInstant(hitVec);
        mc.playerController.processRightClickBlock(mc.player, mc.world, neighbour, opposite, hitVec, EnumHand.MAIN_HAND);
        mc.player.swingArm(EnumHand.MAIN_HAND);
        if ((BlockUtils.blackList.contains(neighbourBlock) || BlockUtils.shulkerList.contains(neighbourBlock))) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
        }
    }
    
    // Magic i don't want to explain, gets a placeable size of a neighbor block and places
    // the block on it, uses getPlaceableSide
    public static void placeBlockPacket(BlockPos pos, boolean rotate) {
        if(pos == null)
            return;
        
        EnumFacing side = getPlaceableSide(pos);
        if (side == null) {
            //ChatUtils.print("Couldn't place a block");
            return;
        }
        BlockPos neighbour = pos.offset(side);
        EnumFacing opposite = side.getOpposite();
        Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
        Block neighbourBlock = mc.world.getBlockState(neighbour).getBlock();
        if ((BlockUtils.blackList.contains(neighbourBlock) || BlockUtils.shulkerList.contains(neighbourBlock))) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
        }
        if (rotate)
            BlockUtils.faceVectorPacketInstant(hitVec);
        float f = (float)(hitVec.x - (double)pos.getX());
        float f1 = (float)(hitVec.y - (double)pos.getY());
        float f2 = (float)(hitVec.z - (double)pos.getZ());
        mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, opposite, EnumHand.MAIN_HAND, f, f1, f2));
        mc.player.swingArm(EnumHand.MAIN_HAND);
        if ((BlockUtils.blackList.contains(neighbourBlock) || BlockUtils.shulkerList.contains(neighbourBlock))) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
        }
    }
    
    public static boolean placeBlock(BlockPos pos, EnumHand hand, boolean rotate, boolean packet) {
        EnumFacing side = getFirstFacing(pos);
        if (side == null) {
            return false;
        }
        
        BlockPos neighbour = pos.offset(side);
        EnumFacing opposite = side.getOpposite();
        
        Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
        Block neighbourBlock = mc.world.getBlockState(neighbour).getBlock();
        
        if (!mc.player.isSneaking() && (BlockUtils.blackList.contains(neighbourBlock) || BlockUtils.shulkerList.contains(neighbourBlock))) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            mc.player.setSneaking(true);
        }
        
        if (rotate) {
            faceVector(hitVec, true);
        }
        
        rightClickBlock(neighbour, hitVec, hand, opposite, packet);
        
        if (mc.player.isSneaking()) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            mc.player.setSneaking(false);
        }
        return true;
    }
    
    public static List<EnumFacing> getPossibleSides(BlockPos pos) {
        List<EnumFacing> facings = new ArrayList<>();
        for (EnumFacing side : EnumFacing.values()) {
            BlockPos neighbour = pos.offset(side);
            if (mc.world.getBlockState(neighbour).getBlock().canCollideCheck(mc.world.getBlockState(neighbour), false)) {
                IBlockState blockState = mc.world.getBlockState(neighbour);
                if (!blockState.getMaterial().isReplaceable()) {
                    facings.add(side);
                }
            }
        }
        return facings;
    }
    
    public static EnumFacing getFirstFacing(BlockPos pos) {
        for (EnumFacing facing : getPossibleSides(pos)) {
            return facing;
        }
        return null;
    }
    
    public static Vec3d getEyesPos() {
        return new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ);
    }
    
    public static void faceVector(Vec3d vec, boolean normalizeAngle) {
        float[] rotations = getLegitRotations(vec);
        mc.player.connection.sendPacket(new CPacketPlayer.Rotation(rotations[0], normalizeAngle ? MathHelper.normalizeAngle((int) rotations[1], 360) : rotations[1], mc.player.onGround));
    }
    
    public static void rightClickBlock(BlockPos pos, Vec3d vec, EnumHand hand, EnumFacing direction, boolean packet) {
        if (packet) {
            float f = (float) (vec.x - (double) pos.getX());
            float f1 = (float) (vec.y - (double) pos.getY());
            float f2 = (float) (vec.z - (double) pos.getZ());
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, direction, hand, f, f1, f2));
        } else {
            mc.playerController.processRightClickBlock(mc.player, mc.world, pos, direction, vec, hand);
        }
        mc.player.swingArm(EnumHand.MAIN_HAND);
    }
    
    public static BlockPos getRealPos(Vec3d vec3d) {
        double x;
        double y;
        double z;
        
        x = (Math.floor(vec3d.x));
        y = (Math.floor(vec3d.y));
        z = (Math.floor(vec3d.z));
        
        return new BlockPos((int)x,(int)y,(int)z);
    }
    
    public static boolean clickOnBlock(BlockPos pos, EnumHand hand) {
        return clickOnBlock(pos, hand, true);
    }
    
    public static boolean clickOnBlock(BlockPos pos, EnumHand hand, boolean rotate) {
        if(pos == null)
            return false;
        
        Vec3d hitVec = new Vec3d(pos).add(0.5, 0.5, 0.5).add(new Vec3d(EnumFacing.UP.getDirectionVec()).scale(0.5));
        Block neighbourBlock = mc.world.getBlockState(pos).getBlock();
        if(rotate)
            BlockUtils.faceVectorPacketInstant(hitVec);
        mc.playerController.processRightClickBlock(mc.player, mc.world, pos, EnumFacing.UP, hitVec, hand);
        mc.player.swingArm(hand);
        return true;
    }
    
    private static BlockPos breaking = null;
    private static Runnable done = null;
    
    public static void tick() {
        if(breaking != null) {
            breakBlock(breaking, done);
        }
    }
    
    public static void breakBlock(BlockPos pos, Runnable done) {
        lookAt(new Vec3d(pos));
        breaking = pos;
        BlockUtils.done = done;
        mc.playerController.onPlayerDamageBlock(pos, EnumFacing.DOWN);
        mc.playerController.getIsHittingBlock();
    }
    
    public static void lookAt(Vec3d pos) {
        faceVectorPacketInstant(pos);
    }
    
    public static void lookCloserTo(Vec3d pos, float amountMax) {
        mc.player.rotationYaw = MathHelper.wrapDegrees(mc.player.rotationYaw);
        mc.player.rotationPitch = MathHelper.wrapDegrees(mc.player.rotationPitch);
        
        float[] rotations;
        float length;
        
        rotations = getLegitRotations(pos);
        rotations[0] = (rotations[0] - (mc.player.rotationYaw));
        rotations[1] = (rotations[1] - (mc.player.rotationPitch));
        length = (float) Math.sqrt(rotations[0] * rotations[0] + rotations[1] * rotations[1]);
        rotations = getLegitRotations(pos);
        rotations[0] = MathHelper.wrapDegrees(((rotations[0] + 180) % 360 - (mc.player.rotationYaw + 180) % 360));
        rotations[1] = MathHelper.wrapDegrees(((rotations[1] + 180) % 360 - (mc.player.rotationPitch + 180) % 360));
        length = Math.min(length, (float) Math.sqrt(rotations[0] * rotations[0] + rotations[1] * rotations[1]));
        
        if(length > 1) {
            rotations[0] = (rotations[0] / length) * amountMax;
            rotations[1] = (rotations[1] / length) * amountMax;
        }
        else {
            rotations[0] = (rotations[0] / length) * (amountMax / 18);
            rotations[1] = (rotations[1] / length) * (amountMax / 18);
        }
        
        mc.player.rotationYaw += rotations[0];
        mc.player.rotationPitch += rotations[1];
    }
    
    // Gets a block next to a block position
    private static EnumFacing getPlaceableSide(BlockPos pos) {
        for (EnumFacing side : EnumFacing.values()) {
            BlockPos neighbour = pos.offset(side);
            if (!mc.world.getBlockState(neighbour).getBlock().canCollideCheck(mc.world.getBlockState(neighbour), false)) {
                continue;
            }
            IBlockState blockState = mc.world.getBlockState(neighbour);
            if (!blockState.getMaterial().isReplaceable() && !(blockState.getBlock() instanceof BlockTallGrass) && !(blockState.getBlock() instanceof BlockDeadBush)) {
                return side;
            }
        }
        return null; // :(
    }
    
    private static Vec3d eyesPos() {
        return new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ);
    }
    
    // Skidded magic.
    private static float[] getLegitRotations(Vec3d vec) {
        Vec3d eyesPos = eyesPos();
        double diffX = vec.x - eyesPos.x;
        double diffY = vec.y - eyesPos.y;
        double diffZ = vec.z - eyesPos.z;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        double yaw = Math.toDegrees(Math.atan2(diffZ, diffX)) - 90f;
        double pitch = (-Math.toDegrees(Math.atan2(diffY, diffXZ)));
        return new float[]{(float) (MathHelper.wrapDegrees(yaw)), (float) (MathHelper.wrapDegrees(pitch))};
    }
    
    // Makes it more legit-looking
    public static void faceVectorPacketInstant(Vec3d vec) {
        float[] rotations = getLegitRotations(vec);
        mc.player.connection.sendPacket(
                new CPacketPlayer.PositionRotation(
                        mc.player.posX, mc.player.posY, mc.player.posZ,
                        rotations[0], rotations[1], mc.player.onGround
                )
        );
    }
    
    public static BlockPos findBlock(Block... blocks) {
        World world = PolyFire.world;
        BlockPos origin = PolyFire.player.getPosition();
        
        for (int z = -5; z <= 5; z++) {
    
            for (int y = -3; y <= 7; y++) {
                
                for (int x = -5; x <= 5; x++) {
                    BlockPos pos = origin.add(x,y,z);
                    for(Block block : blocks)
                        if(world.getBlockState(pos).getBlock() == block) {
                            return pos;
                        }
                }
            }
        }
        
        return null;
    }
}