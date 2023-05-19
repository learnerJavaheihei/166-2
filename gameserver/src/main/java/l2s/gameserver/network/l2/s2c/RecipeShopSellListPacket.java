package l2s.gameserver.network.l2.s2c;

import java.util.List;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ManufactureItem;
import l2s.gameserver.stats.Stats;//顯示技能機率

public class RecipeShopSellListPacket extends L2GameServerPacket
{
	private int objId, curMp, maxMp;
	private long adena;
	private List<ManufactureItem> createList;

	public RecipeShopSellListPacket(Player buyer, Player manufacturer)
	{
		objId = manufacturer.getObjectId();
		curMp = (int) manufacturer.getCurrentMp();
		maxMp = manufacturer.getMaxMp();
		adena = buyer.getAdena();
		createList = manufacturer.getCreateList();
		/* buyer.sendMessage(manufacturer.getName() + " 製作爆擊率 : + " + manufacturer.getStat().calc(Stats.CRAFT_CRITICAL_POWER, 0) + "%"); */
		//顯示技能機率
		buyer.sendMessage(manufacturer.getName() + " 增加制作成功率 : + " + manufacturer.getStat().calc(Stats.CRAFT_CHANCE_BONUS, 0) + "%");//顯示技能機率
	}

	@Override
	protected final void writeImpl()
	{
		writeD(objId);
		writeD(curMp);//Creator's MP
		writeD(maxMp);//Creator's MP
		writeQ(adena);
		writeD(createList.size());
		for(ManufactureItem mi : createList)
		{
			writeD(mi.getRecipeId());
			writeD(0x00); //unknown
			writeQ(mi.getCost());
			writeC(0x00);// 166 矮人工房 猜想是製作機率
			writeQ(0x00);// 166 矮人工房 猜想是有無暴擊
			writeQ(0x00);// 166 矮人工房 猜想是暴擊機率
		}
	}
}