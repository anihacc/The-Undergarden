package quek.undergarden.block.world;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import quek.undergarden.registry.UGBlocks;
import quek.undergarden.registry.UGItems;

import javax.annotation.Nullable;
import java.util.Random;

public class UGStemBlock extends StemBlock implements IGrowable {

    public static final IntegerProperty AGE = BlockStateProperties.AGE_0_7;
    protected static final VoxelShape[] SHAPES = new VoxelShape[]{Block.makeCuboidShape(7.0D, 0.0D, 7.0D, 9.0D, 2.0D, 9.0D), Block.makeCuboidShape(7.0D, 0.0D, 7.0D, 9.0D, 4.0D, 9.0D), Block.makeCuboidShape(7.0D, 0.0D, 7.0D, 9.0D, 6.0D, 9.0D), Block.makeCuboidShape(7.0D, 0.0D, 7.0D, 9.0D, 8.0D, 9.0D), Block.makeCuboidShape(7.0D, 0.0D, 7.0D, 9.0D, 10.0D, 9.0D), Block.makeCuboidShape(7.0D, 0.0D, 7.0D, 9.0D, 12.0D, 9.0D), Block.makeCuboidShape(7.0D, 0.0D, 7.0D, 9.0D, 14.0D, 9.0D), Block.makeCuboidShape(7.0D, 0.0D, 7.0D, 9.0D, 16.0D, 9.0D)};
    private final StemGrownBlock crop;

    public UGStemBlock(StemGrownBlock crop) {
        super(crop, Properties.create(Material.PLANTS)
                .doesNotBlockMovement()
                .tickRandomly()
                .hardnessAndResistance(0F)
                .sound(SoundType.STEM)
                .noDrops()
        );
        this.crop = crop;
        this.setDefaultState(this.stateContainer.getBaseState().with(AGE, 0));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPES[state.get(AGE)];
    }

    @Override
    public boolean isValidGround(BlockState state, IBlockReader worldIn, BlockPos pos) {
        return state.getBlock() == UGBlocks.deepsoil_farmland.get();
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        BlockPos blockpos = pos.down();
        return isValidGround(worldIn.getBlockState(blockpos), worldIn, blockpos);
    }

    @Override
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        tick(state, worldIn, pos, rand);
        if (!worldIn.isAreaLoaded(pos, 1)) return; // Forge: prevent loading unloaded chunks when checking neighbor's light
        if (worldIn.getLightSubtracted(pos, 0) >= 0) {
            float f = UGCropsBlock.getGrowthChance(this, worldIn, pos);
            if (net.minecraftforge.common.ForgeHooks.onCropsGrowPre(worldIn, pos, state, rand.nextInt((int)(25.0F / f) + 1) == 0)) {
                int i = state.get(AGE);
                if (i < 7) {
                    worldIn.setBlockState(pos, state.with(AGE, i + 1), 2);
                } else {
                    Direction direction = Direction.Plane.HORIZONTAL.random(rand);
                    BlockPos blockpos = pos.offset(direction);
                    BlockState soil = worldIn.getBlockState(blockpos.down());
                    Block block = soil.getBlock();
                    if (worldIn.isAirBlock(blockpos) && (soil.canSustainPlant(worldIn, blockpos.down(), Direction.UP, this) || block == Blocks.FARMLAND || block == UGBlocks.deepsoil_farmland.get())) {
                        worldIn.setBlockState(blockpos, this.crop.getDefaultState());
                        worldIn.setBlockState(pos, this.crop.getAttachedStem().getDefaultState().with(HorizontalBlock.HORIZONTAL_FACING, direction));
                    }
                }
                net.minecraftforge.common.ForgeHooks.onCropsGrowPost(worldIn, pos, state);
            }

        }
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    protected Item getSeedItem() {
            return UGItems.gloomgourd_seeds.get();
    }

    @Override
    public ItemStack getItem(IBlockReader worldIn, BlockPos pos, BlockState state) {
        Item item = this.getSeedItem();
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    @Override
    public boolean canGrow(IBlockReader worldIn, BlockPos pos, BlockState state, boolean isClient) {
        return state.get(AGE) != 7;
        //return true;
    }

    @Override
    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void grow(ServerWorld worldIn, Random rand, BlockPos pos, BlockState state) {
        int i = Math.min(7, state.get(AGE) + MathHelper.nextInt(worldIn.rand, 2, 5));
        BlockState blockstate = state.with(AGE, i);
        worldIn.setBlockState(pos, blockstate, 2);
        if (i == 7) {
            blockstate.tick(worldIn, pos, worldIn.rand);
        }

    }
}