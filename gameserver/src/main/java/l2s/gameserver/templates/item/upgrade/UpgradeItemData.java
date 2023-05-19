package l2s.gameserver.templates.item.upgrade;

import l2s.gameserver.templates.item.data.ItemData;

public class UpgradeItemData extends ItemData
{
	private final int enchantLevel;

	public UpgradeItemData(int id, long count, int enchantLevel)
	{
		super(id, count);
		this.enchantLevel = enchantLevel;
	}

	public int getEnchantLevel()
	{
		return this.enchantLevel;
	}
}
