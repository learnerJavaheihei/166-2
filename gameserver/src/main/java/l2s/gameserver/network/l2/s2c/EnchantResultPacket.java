package l2s.gameserver.network.l2.s2c;

public class EnchantResultPacket extends L2GameServerPacket
{
	private final int _resultId, _crystalId;
	private final long _count;
	private final int _enchantLevel;
	private final int _itemId;//強化失敗給予加工石
	private final int _giveStoneCount;//強化失敗給予加工石
	//public static final EnchantResultPacket SUCESS = new EnchantResultPacket(0, 0, 0, 0); // вещь заточилась, в статичном виде не используется
	//public static final EnchantResultPacket FAILED = new EnchantResultPacket(1, 0, 0); // вещь разбилась, требует указания получившихся кристаллов, в статичном виде не используется
	public static final EnchantResultPacket CANCEL = new EnchantResultPacket(2, 0, 0, 0, 0, 0); //強化失敗給予加工石
	public static final EnchantResultPacket BLESSED_FAILED = new EnchantResultPacket(3, 0, 0, 0, 0, 0); //強化失敗給予加工石
	public static final EnchantResultPacket FAILED_NO_CRYSTALS = new EnchantResultPacket(4, 0, 0, 0, 0, 0); //強化失敗給予加工石
	public static final EnchantResultPacket ANCIENT_FAILED = new EnchantResultPacket(5, 0, 0, 0, 0, 0); //強化失敗給予加工石

	public EnchantResultPacket(int resultId, int crystalId, long count, int enchantLevel, int itemId, int giveStoneCount)//強化失敗給予加工石
	{
		_resultId = resultId;
		_crystalId = crystalId;
		_count = count;
		_enchantLevel = enchantLevel;
		_itemId = itemId;//強化失敗給予加工石
		_giveStoneCount = giveStoneCount;//強化失敗給予加工石
	}

	@Override
	protected final void writeImpl()
	{
		writeD(_resultId);
		writeD(_crystalId); // item id кристаллов
		writeQ(_count); // количество кристаллов
		//強化失敗給予加工石--
		if(_resultId == 0)//這一個是強化成功出現的東西
		{
			writeD(0x00);// 4 這一個應該是告初是否出現 強化石之類的
			writeQ(0x00);// 8 強化失敗出現石頭的 物品
		}
		else if(_itemId == 91462)//這一個是強化失敗且等級達到需求時要給的訊息
		{
			writeD(91462);// 4 這一個應該是告初是否出現 強化石之類的
			writeQ(_giveStoneCount);// 8 應該出現武器強化石，因為你失敗了
		}
		else if(_itemId == 91463)//這一個是強化失敗且等級達到需求時要給的訊息
		{
			writeD(91463);// 4 這一個應該是告初是否出現 強化石之類的
			writeQ(_giveStoneCount);// 8 應該出現武器強化石，因為你失敗了
		}
		else
		{
			writeD(0x00);// 4 這一個應該是告初是否出現 強化石之類的
			writeQ(0x00);// 8 強化失敗出現石頭的 物品
		}
		//--強化失敗給予加工石
		writeD(_enchantLevel); // уровень заточки
		writeH(0x00); // uNK
		writeH(0x00); // uNK
		writeH(0x00); // uNK
	}
}