package l2s.gameserver.data.xml.holder;

import java.util.HashMap;
import java.util.Map;
import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.templates.item.upgrade.normal.NormalUpgradeData;
import l2s.gameserver.templates.item.upgrade.rare.RareUpgradeData;

public final class UpgradeSystemHolder extends AbstractHolder
{
	private static UpgradeSystemHolder INSTANCE = new UpgradeSystemHolder();

	public static UpgradeSystemHolder getInstance()
	{
		return INSTANCE;
	}

	private final Map<Integer, NormalUpgradeData> normalUpgradeDatas = new HashMap<Integer, NormalUpgradeData>();
	private final Map<Integer, RareUpgradeData> rareUpgradeDatas = new HashMap<Integer, RareUpgradeData>();

	public void addNormalUpgradeData(NormalUpgradeData data)
	{
		normalUpgradeDatas.put(Integer.valueOf(data.getId()), data);
	}

	public NormalUpgradeData getNormalUpgradeData(int id)
	{
		return normalUpgradeDatas.get(id);
	}

	public void addRareUpgradeData(RareUpgradeData data)
	{
		rareUpgradeDatas.put(Integer.valueOf(data.getId()), data);
	}

	public RareUpgradeData getRareUpgradeData(int id)
	{
		return this.rareUpgradeDatas.get(id);
	}

	public void log()
	{
		info(String.format("loaded %d normal upgrade data(s) count.", new Object[] { Integer.valueOf(this.normalUpgradeDatas.size()) }));
		info(String.format("loaded %d rare upgrade data(s) count.", new Object[] { Integer.valueOf(this.rareUpgradeDatas.size()) }));
	}

	public int size()
	{
		return this.normalUpgradeDatas.size() + this.rareUpgradeDatas.size();
	}

	public void clear()
	{
		this.normalUpgradeDatas.clear();
		this.rareUpgradeDatas.clear();
	}
}
