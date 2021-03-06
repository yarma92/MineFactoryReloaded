package powercrystals.minefactoryreloaded.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import powercrystals.minefactoryreloaded.api.rednet.connectivity.IRedNetNoConnection;
import powercrystals.minefactoryreloaded.core.GrindingDamage;
import powercrystals.minefactoryreloaded.tile.machine.TileEntityLaserDrill;

public class BlockFakeLaser extends Block implements IRedNetNoConnection
{
	private static GrindingDamage laserDamage = new GrindingDamage("mfr.laser");

	public BlockFakeLaser()
	{
		super(Material.air);
		setHardness(-1);
		setResistance(Float.POSITIVE_INFINITY);
		setBlockBounds(0F, 0F, 0F, 0F, 0F, 0F);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
	{
		return null;
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity)
	{
		entity.attackEntityFrom(laserDamage, 2);
		entity.setFire(10);
	}

	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z)
	{
		return 15;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public boolean isAir(IBlockAccess world, int x, int y, int z)
	{
		return true;
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z)
	{
		world.scheduleBlockUpdate(x, y, z, this, 1);
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block id)
	{
		world.scheduleBlockUpdate(x, y, z, this, 1);
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random rand)
	{
		if (world.isRemote || world.getBlockMetadata(x, y, z) != 0) return;

		Block upperId = world.getBlock(x, y + 1, z);
		if (!upperId.equals(this) && !(world.getTileEntity(x, y + 1, z) instanceof TileEntityLaserDrill))
		{
			world.setBlockToAir(x, y, z);
			return;
		}

		Block lowerId = world.getBlock(x, y - 1, z);
		if ((!lowerId.equals(this) || world.getBlockMetadata(x, y - 1, z) != 0) &&
				TileEntityLaserDrill.canReplaceBlock(lowerId, world, x, y - 1, z))
		{
			world.setBlock(x, y - 1, z, this);
		}
	}

	@Override
	public boolean canRenderInPass(int pass)
	{
		return false;
	}

	@Override
	public int getRenderType()
	{
		return -1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister ir)
	{
	}
}
