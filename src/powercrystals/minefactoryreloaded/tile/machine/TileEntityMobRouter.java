package powercrystals.minefactoryreloaded.tile.machine;

import static powercrystals.minefactoryreloaded.item.ItemSafariNet.getEntityClass;
import static powercrystals.minefactoryreloaded.item.ItemSafariNet.isSingleUse;
import static powercrystals.minefactoryreloaded.item.ItemSafariNet.isSafariNet;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;

import cofh.util.position.BlockPosition;
import powercrystals.minefactoryreloaded.MFRRegistry;
import powercrystals.minefactoryreloaded.core.HarvestAreaManager;
import powercrystals.minefactoryreloaded.gui.client.GuiFactoryInventory;
import powercrystals.minefactoryreloaded.gui.client.GuiMobRouter;
import powercrystals.minefactoryreloaded.gui.container.ContainerMobRouter;
import powercrystals.minefactoryreloaded.setup.Machine;
import powercrystals.minefactoryreloaded.tile.base.TileEntityFactoryPowered;

public class TileEntityMobRouter extends TileEntityFactoryPowered
{
	protected int _matchMode;
	protected boolean _blacklist;
	
	public TileEntityMobRouter()
	{
		super(Machine.MobRouter);
		_areaManager = new HarvestAreaManager(this, 2, 2, 1);
		setCanRotate(true);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public GuiFactoryInventory getGui(InventoryPlayer inventoryPlayer)
	{
		return new GuiMobRouter(getContainer(inventoryPlayer), this);
	}
	
	@Override
	public ContainerMobRouter getContainer(InventoryPlayer inventoryPlayer)
	{
		return new ContainerMobRouter(this, inventoryPlayer);
	}

	@Override
	protected boolean activateMachine()
	{
		Class<?> matchClass;
		if (_inventory[0] != null)
		{
			if (!isSafariNet(_inventory[0]) || isSingleUse(_inventory[0]))
				return false;
			matchClass = getEntityClass(_inventory[0]);
		}
		else
			matchClass = EntityLivingBase.class;
		
		List<? extends EntityLivingBase> entities = worldObj.getEntitiesWithinAABB(EntityLivingBase.class,
				_areaManager.getHarvestArea().toAxisAlignedBB());
		List<Class<? extends EntityLivingBase>> blacklist = MFRRegistry.getSafariNetBlacklist();

		switch (_matchMode)
		{
		case 3:
			if (matchClass != EntityLivingBase.class)
				matchClass = matchClass.getSuperclass();
		case 2:
			if (matchClass != EntityLivingBase.class)
				matchClass = matchClass.getSuperclass();
		}
		
		for (EntityLivingBase entity : entities)
		{
			Class<?> entityClass = entity.getClass();
			if (blacklist.contains(entityClass) || EntityPlayer.class.isAssignableFrom(entityClass))
				continue;
			boolean match;
			switch (_matchMode)
			{
			case 0:
				match = matchClass == entityClass;
				break;
			case 1: case 2: case 3:
				match = matchClass.isAssignableFrom(entityClass);
				break;
			default:
				match = false;
			}
			if (match ^ _blacklist)
			{
				BlockPosition bp = BlockPosition.fromFactoryTile(this);
				bp.moveBackwards(1);
				entity.setPosition(bp.x + 0.5, bp.y + 0.5, bp.z + 0.5);
				
				return true;
			}
		}
		setIdleTicks(getIdleTicksMax());
		return false;
	}
	
	public boolean getWhiteList()
	{
		return !_blacklist;
	}
	
	public void setWhiteList(boolean whitelist)
	{
		_blacklist = !whitelist;
	}
	
	public int getMatchMode()
	{
		return _matchMode;
	}
	
	public void setMatchMode(int matchMode)
	{
		if (matchMode < 0)
			_matchMode = 3;
		else
			_matchMode = matchMode % 4;
	}

	@Override
	public int getSizeInventory()
	{
		return 1;
	}

	@Override
	public int getWorkMax()
	{
		return 1;
	}
	
	@Override
	public int getIdleTicksMax()
	{
		return 200;
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT(tag);
		tag.setInteger("mode", _matchMode);
		tag.setBoolean("blacklist", _blacklist);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT(tag);
		setMatchMode(tag.getInteger("mode"));
		_blacklist = tag.getBoolean("blacklist");
	}
}
