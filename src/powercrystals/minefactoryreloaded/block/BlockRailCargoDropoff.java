package powercrystals.minefactoryreloaded.block;

import powercrystals.minefactoryreloaded.core.UtilInventory;

import java.util.Map.Entry;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import cofh.inventory.IInventoryManager;
import cofh.inventory.InventoryManager;
import cofh.util.position.BlockPosition;

public class BlockRailCargoDropoff extends BlockFactoryRail
{
	public BlockRailCargoDropoff()
	{
		super(true, false);
		setBlockName("mfr.rail.cargo.dropoff");
	}

	@Override
	public void onMinecartPass(World world, EntityMinecart entity, int x, int y, int z)
	{
		if (world.isRemote || !(entity instanceof IInventory))
			return;

		IInventoryManager minecart = InventoryManager.create((IInventory)entity, ForgeDirection.UNKNOWN);

		for (Entry<Integer, ItemStack> contents : minecart.getContents().entrySet())
		{
			if (contents.getValue() == null)
			{
				continue;
			}

			ItemStack stackToAdd = contents.getValue().copy();
			ItemStack remaining = UtilInventory.dropStack(world, new BlockPosition(x, y, z), contents.getValue(), ForgeDirection.VALID_DIRECTIONS, ForgeDirection.UNKNOWN);

			if (remaining != null)
			{
				stackToAdd.stackSize -= remaining.stackSize;
			}

			minecart.removeItem(stackToAdd.stackSize, stackToAdd);
		}
	}
}
