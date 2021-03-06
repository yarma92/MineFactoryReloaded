package powercrystals.minefactoryreloaded.tile.machine;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import cofh.util.position.BlockPosition;
import powercrystals.minefactoryreloaded.MFRRegistry;
import powercrystals.minefactoryreloaded.api.FertilizerType;
import powercrystals.minefactoryreloaded.api.IFactoryFertilizable;
import powercrystals.minefactoryreloaded.api.IFactoryFertilizer;
import powercrystals.minefactoryreloaded.core.HarvestAreaManager;
import powercrystals.minefactoryreloaded.gui.client.GuiFactoryInventory;
import powercrystals.minefactoryreloaded.gui.client.GuiUpgradable;
import powercrystals.minefactoryreloaded.gui.container.ContainerUpgradable;
import powercrystals.minefactoryreloaded.item.ItemUpgrade;
import powercrystals.minefactoryreloaded.setup.MFRConfig;
import powercrystals.minefactoryreloaded.setup.Machine;
import powercrystals.minefactoryreloaded.tile.base.TileEntityFactoryPowered;

public class TileEntityFertilizer extends TileEntityFactoryPowered
{
	private Random _rand;
	
	public TileEntityFertilizer()
	{
		super(Machine.Fertilizer);
		_rand = new Random();
		_areaManager = new HarvestAreaManager(this, 1, 0, 0);
		setManageSolids(true);
		setCanRotate(true);
	}
	
	@Override
	protected void onFactoryInventoryChanged()
	{
		_areaManager.updateUpgradeLevel(_inventory[9]);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public GuiFactoryInventory getGui(InventoryPlayer inventoryPlayer)
	{
		return new GuiUpgradable(getContainer(inventoryPlayer), this);
	}
	
	@Override
	public ContainerUpgradable getContainer(InventoryPlayer inventoryPlayer)
	{
		return new ContainerUpgradable(this, inventoryPlayer);
	}
	
	@Override
	public boolean activateMachine()
	{
		BlockPosition bp = _areaManager.getNextBlock();
		Map<Block, IFactoryFertilizable> fertalizables = MFRRegistry.getFertilizables();
		
		Block target = worldObj.getBlock(bp.x, bp.y, bp.z);
		if (!fertalizables.containsKey(target))
		{
			setIdleTicks(getIdleTicksMax());
			return false;
		}
		
		IFactoryFertilizable fertilizable = fertalizables.get(target);
		Map<Item, IFactoryFertilizer> fertilizers = MFRRegistry.getFertilizers();
		for (int stackIndex = 0, e = getSizeInventory(); stackIndex < e; stackIndex++)
		{
			ItemStack fertStack = getStackInSlot(stackIndex);
			if (fertStack == null || !fertilizers.containsKey(fertStack.getItem()))
				continue;

			IFactoryFertilizer fertilizer = fertilizers.get(fertStack.getItem());
			FertilizerType type = fertilizer.getFertilizerType(fertStack);

			if (type == FertilizerType.None)
				continue;
			if (!fertilizable.canFertilize(worldObj, bp.x, bp.y, bp.z, type))
				continue;

			if (fertilizable.fertilize(worldObj, _rand, bp.x, bp.y, bp.z, type))
			{
				fertilizer.consume(fertStack);
				if (MFRConfig.playSounds.getBoolean(true)) // particles
					worldObj.playAuxSFXAtEntity(null, 2005, bp.x, bp.y, bp.z, _rand.nextInt(10) + 5);
				if(fertStack.stackSize <= 0)
					setInventorySlotContents(stackIndex, null);

				return true;
			}
		}
		
		setIdleTicks(getIdleTicksMax());
		return false;
	}
	
	@Override
	public int getSizeInventory()
	{
		return 10;
	}
	
	@Override
	public int getWorkMax()
	{
		return 1;
	}
	
	@Override
	public int getIdleTicksMax()
	{
		return 20;
	}
	
	@Override
	public int getStartInventorySide(ForgeDirection side)
	{
		return 0;
	}
	
	@Override
	public int getSizeInventorySide(ForgeDirection side)
	{
		return 9;
	}
	
	@Override
	public boolean canInsertItem(int slot, ItemStack stack, int sideordinal)
	{
		if (stack != null)
		{
			if(slot < 9)
			{
				return MFRRegistry.getFertilizers().containsKey(stack.getItem());
			}
			else if(slot == 9)
			{
				return stack.getItem() instanceof ItemUpgrade;
			}
		}
		return false;
	}
}
