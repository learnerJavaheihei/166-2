package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;

/**
 * @author VISTALL
 * @date 23:33/23.03.2011
 */
public class RequestGoodsInventoryInfo extends L2GameClientPacket
{
	@Override
	protected boolean readImpl()
	{
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;
		//player.sendPacket(new ExGoodsInventoryInfo(player));
	}
}
