package handler.items;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.SystemMessage;

//TODO: [Bonux] Сделать через скиллы.
public class Kamaloka extends SimpleItemHandler
{
	@Override
	protected boolean useItemImpl(Player player, ItemInstance item, boolean ctrl)
	{
		int itemId = item.getItemId();

		if(!reduceItem(player, item))
			return false;

		sendUseMessage(player, item);

		switch(itemId)
		{
			case 13010://追加入場券-欲界(深淵之廳) 覺醒
			case 13297://追加入場券-欲界(深淵之廳)-活動用 覺醒
			case 20026://追加入場券-欲界(深淵之廳) 覺醒
				player.removeInstanceReusesByGroupId(1);
				break;
			case 13011://追加入場券-近緣欲界 覺醒
			case 13298://追加入場券-近緣欲界-活動用 覺醒
			case 20027://追加入場券-近緣欲界 覺醒
				player.removeInstanceReusesByGroupId(2);
				break;
			case 13012://追加入場券-欲界(深淵迷宮) 覺醒
			case 13299://追加入場券-欲界(深淵迷宮)-活動用 覺醒
			case 20028://追加入場券-欲界(深淵迷宮) 覺醒
				player.removeInstanceReusesByGroupId(3);
				break;
			default:
				return false;
		}
		return true;
	}
}
