package handler.items;

import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.RadarControlPacket;
import l2s.gameserver.utils.Functions;

public class BossBook extends ScriptItemHandler
{
	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		if(!playable.isPlayer())
			return false;		
		Player activeChar = (Player) playable;
		Functions.show(HtmCache.getInstance().getHtml("BossBook-0.htm", activeChar), activeChar);
		activeChar.sendActionFailed();
		return true;
	}
}