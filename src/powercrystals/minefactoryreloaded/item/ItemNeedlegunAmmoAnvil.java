package powercrystals.minefactoryreloaded.item;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityFallingSand;
import net.minecraft.world.World;

public class ItemNeedlegunAmmoAnvil extends ItemNeedlegunAmmoBlock
{
	public ItemNeedlegunAmmoAnvil(int id, int blockId, int meta)
	{
		super(id, blockId, meta);
		setMaxDamage(0);
	}
	
	public ItemNeedlegunAmmoAnvil(int id)
	{
		this(id, Block.anvil.blockID, 3);
	}
	
	@Override
	public float getSpread()
	{
		return 0.5f;
	}

	@Override
	protected void placeBlockAt(World world, int x, int y, int z, double distance)
	{
		if(!world.isRemote)
		{
	        EntityFallingSand anvil = new EntityFallingSand(world, x + 0.5, y + 0.5, z + 0.5,
	        		_blockId, _blockMeta);
	        anvil.setIsAnvil(true);
	        world.spawnEntityInWorld(anvil);
	        anvil.fallDistance = ((float)distance) + 1f;
	        anvil.fallTime = 3;
	        anvil.onUpdate();
		}
	}
}
