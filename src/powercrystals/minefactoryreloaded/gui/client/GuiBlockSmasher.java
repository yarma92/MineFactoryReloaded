package powercrystals.minefactoryreloaded.gui.client;

import net.minecraft.client.gui.GuiButton;

import powercrystals.minefactoryreloaded.gui.container.ContainerBlockSmasher;
import powercrystals.minefactoryreloaded.net.Packets;
import powercrystals.minefactoryreloaded.tile.machine.TileEntityBlockSmasher;

public class GuiBlockSmasher extends GuiFactoryPowered
{
	private TileEntityBlockSmasher _smasher;
	private GuiButton _inc;
	private GuiButton _dec;
	
	public GuiBlockSmasher(ContainerBlockSmasher container, TileEntityBlockSmasher te)
	{
		super(container, te);
		_smasher = te;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		super.initGui();
		_inc = new GuiButton(1, (this.width - this.xSize) / 2 + 63, (this.height - this.ySize) / 2 + 23, 20, 20, "+");
		_dec = new GuiButton(2, (this.width - this.xSize) / 2 + 63, (this.height - this.ySize) / 2 + 53, 20, 20, "-");
		_inc.enabled = (_smasher.getFortune() < 3);
		_dec.enabled = (_smasher.getFortune() > 0);
		buttonList.add(_inc);
		buttonList.add(_dec);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		fontRendererObj.drawString(new Integer(_smasher.getFortune()).toString(), 68, 44, 4210752);
		fontRendererObj.drawString("Fortune", 64, 15, 4210752);
		// TODO: localize
	}
	
	@Override
	public void updateScreen()
	{
		super.updateScreen();
		_inc.enabled = (_smasher.getFortune() < 4);
		_dec.enabled = (_smasher.getFortune() > 0);
	}
	
	@Override
	protected void actionPerformed(GuiButton button)
	{
		if(button.id == 1)
		{
			if(_smasher.getFortune() < 3)
			{
				_smasher.setFortune(_smasher.getFortune() + 1);
				Packets.sendToServer(Packets.EnchanterButton, _tileEntity,
						(byte)(1));
			}
		}
		else
		{
			if(_smasher.getFortune() > 1)
			{
				_smasher.setFortune(_smasher.getFortune() - 1);
				Packets.sendToServer(Packets.EnchanterButton, _tileEntity,
						(byte)(-1));
			}
		}
	}
}
