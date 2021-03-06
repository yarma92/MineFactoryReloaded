package powercrystals.minefactoryreloaded.tile.base;

import cofh.api.energy.IEnergyHandler;
import cofh.api.tileentity.IEnergyInfo;
import cofh.util.CoreUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import powercrystals.minefactoryreloaded.setup.Machine;
//import ic2.api.energy.event.EnergyTileLoadEvent;
//import ic2.api.energy.event.EnergyTileUnloadEvent;
//import ic2.api.energy.tile.IEnergySink;

/*
 * There are three pieces of information tracked - energy, work, and idle ticks.
 * 
 * Energy is stored and used when the _machine activates. The energy stored must be >= energyActivation for the activateMachine() method to be called.
 * If activateMachine() returns true, energy will be drained.
 * 
 * Work is built up and then when at 100% something happens. This is tracked/used entirely by the derived class. If not used (f.ex. harvester), return max 1.
 * 
 * Idle ticks cause an artificial delay before activateMachine() is called again. Max should be the highest value the _machine will use, to draw the
 * progress bar correctly.
 */
public abstract class TileEntityFactoryPowered extends TileEntityFactoryInventory
											implements IEnergyHandler, IEnergyInfo
														// IEnergySink,
{	
	public static final int energyPerAE = 2;
	public static final int energyPerEU = 4;
	public static final int energyPerMJ = 10;
	
	private static final int energyFudge = 80;
	
	private int _energyStored;
	private int _maxEnergyStored;
	private int _maxEnergyTick;
	private int _energyRequiredThisTick = 0;
	
	protected int _energyActivation;
	
	private int _workDone;
	
	private int _idleTicks;
	
	// IC2-related fields
	
	private boolean _isAddedToIC2EnergyNet;
	private boolean _addToNetOnNextTick;
	
	// constructors
	
	protected TileEntityFactoryPowered(Machine machine)
	{
		this(machine, machine.getActivationEnergy());
	}
	
	protected TileEntityFactoryPowered(Machine machine, int activationCost)
	{
		super(machine);
		_maxEnergyStored = machine.getMaxEnergyStorage();
		setActivationEnergy(activationCost);
		setIsActive(false);
	}
	
	// local methods
	
	protected void setActivationEnergy(int activationCost)
	{
		_energyActivation = activationCost;
		_maxEnergyTick = Math.min(activationCost * 4, _maxEnergyStored);
	}
	
	@Override
	public void updateEntity()
	{
		super.updateEntity();
		
		_energyStored = Math.min(_energyStored, getEnergyStoredMax());
		
		if (worldObj.isRemote)
		{
			machineDisplayTick();
			return;
		}
		
		if(_addToNetOnNextTick)
		{
			if(!worldObj.isRemote)
			{
				//MinecraftForge.EVENT_BUS.post(new EnergyTileLoadEvent(this));
			}
			_addToNetOnNextTick = false;
			_isAddedToIC2EnergyNet = true;
		}
		
		int energyRequired = Math.min(getEnergyStoredMax() - getEnergyStored(),
				getActivationEnergy() + energyFudge);
		
		_energyRequiredThisTick = Math.max(_energyRequiredThisTick + energyRequired,
				getMaxEnergyPerTick());
		
		setIsActive(updateIsActive(failedDrops != null));
		
		if (failedDrops != null)
		{
			setIdleTicks(getIdleTicksMax());
			return;
		}
		
		if (CoreUtils.isRedstonePowered(this))
		{
			setIdleTicks(getIdleTicksMax());
		}
		else if (_idleTicks > 0)
		{
			_idleTicks--;
		}
		else if (_energyStored >= _energyActivation)
		{
			if (activateMachine())
			{
				_energyStored -= _energyActivation;
			}
		}
	}
	
	@Override
	public void validate()
	{
		super.validate();
		if(!_isAddedToIC2EnergyNet)
		{
			_addToNetOnNextTick = true;
		}
	}
	
	@Override
	public void onChunkUnload()
	{
		super.onChunkUnload();
		if(_isAddedToIC2EnergyNet)
		{
			if(worldObj != null && !worldObj.isRemote)
			{
				//MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
			}
			_isAddedToIC2EnergyNet = false;
		}
	}
	
	protected boolean updateIsActive(boolean failedDrops)
	{
		return !failedDrops && hasSufficientPower();
	}
	
	protected abstract boolean activateMachine();
	
	@SideOnly(Side.CLIENT)
	protected void machineDisplayTick()
	{
	}
	
	@Override
	public void onDisassembled()
	{
		super.onDisassembled();
		if(_isAddedToIC2EnergyNet)
		{
			if(worldObj != null && !worldObj.isRemote)
			{
				//MinecraftForge.EVENT_BUS.post(new EnergyTileUnloadEvent(this));
			}
			_isAddedToIC2EnergyNet = false;
		}
	}
	
	public final boolean hasSufficientPower()
	{
		return _energyStored >= _energyActivation * 2;
	}
	
	public int getActivationEnergy()
	{
		return _energyActivation;
	}
	
	public int getMaxEnergyPerTick()
	{
		return _maxEnergyTick;
	}
	
	public int getEnergyStored()
	{
		return _energyStored;
	}
	
	public int getEnergyStoredMax()
	{
		return _maxEnergyStored;
	}
	
	public void setEnergyStored(int energy)
	{
		_energyStored = energy;
	}
	
	public void drainEnergy(int drainAmount)
	{
		_energyStored -= drainAmount;
	}
	
	public int getWorkDone()
	{
		return _workDone;
	}
	
	public abstract int getWorkMax();
	
	public void setWorkDone(int work)
	{
		_workDone = work;
	}
	
	public int getIdleTicks()
	{
		return _idleTicks;
	}
	
	public abstract int getIdleTicksMax();
	
	public void setIdleTicks(int ticks)
	{
		_idleTicks = ticks;
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT(tag);
		
		tag.setInteger("energyStored", _energyStored);
		tag.setInteger("workDone", _workDone);
		NBTTagCompound pp = new NBTTagCompound();
		tag.setTag("powerProvider", pp);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT(tag);
		
		_energyStored = Math.min(tag.getInteger("energyStored"), getEnergyStoredMax());
		_workDone = Math.min(tag.getInteger("workDone"), getWorkMax());
	}
	
	public int getEnergyRequired()
	{
		return Math.min(getEnergyStoredMax() - getEnergyStored(), _energyRequiredThisTick);
	}
	
	public int storeEnergy(int energy, boolean doStore)
	{
		int energyInjected = Math.max(Math.min(energy, getEnergyRequired()), 0);
		if (doStore)
		{
			_energyStored += energyInjected;
			_energyRequiredThisTick -= energyInjected;
		}
		return energyInjected;
	}
	
	public int storeEnergy(int energy) { return storeEnergy(energy, true); }
	
	// TE methods

	@Override
	public int getInfoEnergyPerTick()
	{
		return getEnergyRequired();
	}

	@Override
	public int getInfoMaxEnergyPerTick()
	{
		return getMaxEnergyPerTick();
	}

	@Override
	public int getInfoEnergy()
	{
		return getEnergyStored();
	}

	@Override
	public int getInfoMaxEnergy()
	{
		return getEnergyStoredMax();
	}
	
	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate)
	{
		return storeEnergy(maxReceive, !simulate);
	}

	@Override
	public int extractEnergy(ForgeDirection from, int maxExtract, boolean doExtract)
	{
		return 0;
	}

	@Override
	public boolean canConnectEnergy(ForgeDirection from) {
		return true;
	}

	@Override
	public int getEnergyStored(ForgeDirection from)
	{
		return getEnergyStored();
	}

    @Override
	public int getMaxEnergyStored(ForgeDirection from)
	{
		return getEnergyStoredMax();
	}
	
	/*/ IC2 methods
	
	@Override
	public double demandedEnergyUnits()
	{
		return Math.max(Math.ceil(getEnergyRequired() / (double)energyPerEU), 0);
	}
	
	@Override
	public double injectEnergyUnits(ForgeDirection from, double amount)
	{
		double euLeftOver = Math.max(amount, 0);
		euLeftOver -= storeEnergy((int)(euLeftOver * energyPerEU)) / (double)energyPerEU;
		return euLeftOver;
	}
	
	@Override
	public boolean acceptsEnergyFrom(TileEntity emitter, ForgeDirection direction)
	{
		return true;
	}
	
	@Override
	public int getMaxSafeInput()
	{
		return Integer.MAX_VALUE;
	}//*/
}
