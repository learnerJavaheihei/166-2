package l2s.gameserver.templates.item.upgrade.normal;

import java.util.ArrayList;
import java.util.List;
import l2s.gameserver.templates.item.upgrade.UpgradeItemData;

public class NormalUpgradeData
{
	private final int id;
	private final int type;
	private final int itemId;
	private final int enchantLevel;
	private final long price;
	private final int unk;
	private final List<UpgradeItemData> requiredItems = new ArrayList<UpgradeItemData>();
	private final List<Integer> unkData = new ArrayList<Integer>();
	private NormalUpgradeResult successResult = null;
	private NormalUpgradeResult failResult = null;
	private NormalUpgradeResult bonusResult = null;

	public NormalUpgradeData(int id, int type, int itemId, int enchantLevel, long price, int unk)
	{
		this.id = id;
		this.type = type;
		this.itemId = itemId;
		this.enchantLevel = enchantLevel;
		this.price = price;
		this.unk = unk;
	}

	public int getId()
	{
		return this.id;
	}

	public int getType()
	{
		return this.type;
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

	public NormalUpgradeResult getSuccessResult()
	{
		return this.successResult;
	}

	public void setSuccessResult(NormalUpgradeResult successResult)
	{
		if(this.successResult != null)
		{
			return;
		}
		this.successResult = successResult;
	}

	public NormalUpgradeResult getFailResult()
	{
		return this.failResult;
	}

	public void setFailResult(NormalUpgradeResult failResult)
	{
		if(this.failResult != null)
		{
			return;
		}
		this.failResult = failResult;
	}

	public NormalUpgradeResult getBonusResult()
	{
		return this.bonusResult;
	}

	public void setBonusResult(NormalUpgradeResult bonusResult)
	{
		if(this.bonusResult != null)
		{
			return;
		}
		this.bonusResult = bonusResult;
	}
}
