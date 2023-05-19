package l2s.gameserver.templates.item.upgrade.normal;

import java.util.ArrayList;
import java.util.List;
import l2s.gameserver.templates.item.upgrade.UpgradeItemData;

public class NormalUpgradeResult
{
	private final NormalUpgradeResultType type;
	private final double chance;
	private final List<UpgradeItemData> items = new ArrayList<UpgradeItemData>();

	public NormalUpgradeResult(NormalUpgradeResultType type, double chance)
	{
		this.type = type;
		this.chance = chance;
	}

	public NormalUpgradeResultType getType()
	{
		return this.type;
	}

	public double getChance()
	{
		return this.chance;
	}

	public void addItem(UpgradeItemData item)
	{
		this.items.add(item);
	}

	public List<UpgradeItemData> getItems()
	{
		return this.items;
	}
}
