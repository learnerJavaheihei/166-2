package l2s.gameserver.templates.item.upgrade.rare;

import java.util.ArrayList;
import java.util.List;
import l2s.gameserver.templates.item.upgrade.UpgradeItemData;

public class RareUpgradeData
{
	private final int id;
	private final int itemId;
	private final int enchantLevel;
	private final long price;
	private final int unk;
	private final int resultItemId;
	private final long resultItemCount;
	private final int resultItemEnchant;
	private final List<UpgradeItemData> requiredItems = new ArrayList<UpgradeItemData>();
	private final List<Integer> unkData = new ArrayList<Integer>();

	public RareUpgradeData(int id, int itemId, int enchantLevel, long price, int unk, int resultItemId, long resultItemCount, int resultItemEnchant)
	{
		this.id = id;
		this.itemId = itemId;
		this.enchantLevel = enchantLevel;
		this.price = price;
		this.unk = unk;
		this.resultItemId = resultItemId;
		this.resultItemCount = resultItemCount;
		this.resultItemEnchant = resultItemEnchant;
	}

	public int getId()
	{
		return this.id;
	}

	public int getItemId()
	{
		return this.itemId;
	}

	public int getEnchantLevel()
	{
		return this.enchantLevel;
	}

	public long getPrice()
	{
		return this.price;
	}

	public int getUnk()
	{
		return this.unk;
	}

	public int getResultItemId()
	{
		return this.resultItemId;
	}

	public long getResultItemCount()
	{
		return this.resultItemCount;
	}

	public int getResultItemEnchant()
	{
		return this.resultItemEnchant;
	}

	public void addRequiredItem(UpgradeItemData item)
	{
		this.requiredItems.add(item);
	}

	public List<UpgradeItemData> getRequiredItems()
	{
		return this.requiredItems;
	}

	public void addUnkData(int data)
	{
		this.unkData.add(Integer.valueOf(data));
	}

	public List<Integer> getUnkData()
	{
		return this.unkData;
	}
}
