package l2s.gameserver.data.xml.parser;

import java.io.File;
import java.util.Iterator;
import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.UpgradeSystemHolder;
import l2s.gameserver.templates.item.upgrade.UpgradeItemData;
import l2s.gameserver.templates.item.upgrade.normal.NormalUpgradeData;
import l2s.gameserver.templates.item.upgrade.normal.NormalUpgradeResult;
import l2s.gameserver.templates.item.upgrade.normal.NormalUpgradeResultType;
import l2s.gameserver.templates.item.upgrade.rare.RareUpgradeData;
import org.dom4j.Element;

public final class UpgradeSystemParser extends AbstractParser<UpgradeSystemHolder>
{
	private static final UpgradeSystemParser INSTANCE = new UpgradeSystemParser();

	public static UpgradeSystemParser getInstance()
	{
		return INSTANCE;
	}

	private UpgradeSystemParser()
	{
		super(UpgradeSystemHolder.getInstance());
	}

	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/upgrade_system/");
	}

	public String getDTDFileName()
	{
		return "upgrade_system.dtd";
	}

	protected void readData(Element rootElement) throws Exception
	{
		for(Iterator<Element> i1 = rootElement.elementIterator("normal_upgrade"); i1.hasNext();)
		{
			Element e1 = i1.next();

			int id = parseInt(e1, "id");
			int type = parseInt(e1, "type");
			int itemId = parseInt(e1, "item_id");
			int enchantLevel = parseInt(e1, "enchant_level", 0);
			long price = parseLong(e1, "price", 0L);
			int unk = parseInt(e1, "unk", 0);

			NormalUpgradeData data = new NormalUpgradeData(id, type, itemId, enchantLevel, price, unk);
			for(Iterator<Element> i2 = e1.elementIterator(); i2.hasNext();)
			{
				Element e2 = i2.next();
				if("required_items".equalsIgnoreCase(e2.getName()))
				{
					for(Iterator<Element> i3 = e2.elementIterator("item"); i3.hasNext();)
					{
						Element e3 = i3.next();
						int id1 = parseInt(e3, "id");
						long count = parseLong(e3, "count");
						int enchant = parseInt(e3, "enchant", 0);
						data.addRequiredItem(new UpgradeItemData(id1, count, enchant));
					}
				}
				else if("results".equalsIgnoreCase(e2.getName()))
				{
					Element successElement = e2.element("success");
					if(successElement != null)
					{
						double chance = parseDouble(successElement, "chance");
						NormalUpgradeResult result = new NormalUpgradeResult(NormalUpgradeResultType.SUCCESS, chance);
						for(Iterator<Element> i3aa = successElement.elementIterator("item"); i3aa.hasNext();)
						{
							Element e3 = i3aa.next();
							int id1 = parseInt(e3, "id");
							long count = parseLong(e3, "count");
							int enchant = parseInt(e3, "enchant", 0);
							result.addItem(new UpgradeItemData(id1, count, enchant));
						}
						data.setSuccessResult(result);
					}
					Element failElement = e2.element("fail");
					if(failElement != null)
					{
						double chance = parseDouble(failElement, "chance");
						NormalUpgradeResult result = new NormalUpgradeResult(NormalUpgradeResultType.FAIL, chance);
						for(Iterator<Element> i3bb = failElement.elementIterator("item"); i3bb.hasNext();)
						{
							Element e3 = i3bb.next();
							int id1 = parseInt(e3, "id");
							long count = parseLong(e3, "count");
							int enchant = parseInt(e3, "enchant", 0);
							result.addItem(new UpgradeItemData(id1, count, enchant));
						}
						data.setFailResult(result);
					}
					Element bonusElement = e2.element("bonus");
					if(bonusElement != null)
					{
						double chance = parseDouble(bonusElement, "chance");
						NormalUpgradeResult result = new NormalUpgradeResult(NormalUpgradeResultType.BONUS, chance);
						for(Iterator<Element> i3cc = bonusElement.elementIterator("item"); i3cc.hasNext();)
						{
							Element e3 = i3cc.next();
							int id1 = parseInt(e3, "id");
							long count = parseLong(e3, "count");
							int enchant = parseInt(e3, "enchant", 0);
							result.addItem(new UpgradeItemData(id1, count, enchant));
						}
						data.setBonusResult(result);
					}
				}
				else if("unk_data".equalsIgnoreCase(e2.getName()))
				{
					for(Iterator<Element> i3 = e2.elementIterator("unk"); i3.hasNext();)
					{
						Element e3 = i3.next();
						int value = parseInt(e3, "value");
						data.addUnkData(value);
					}
				}
			}
			getHolder().addNormalUpgradeData(data);
		}
		for(Iterator<Element> i1 = rootElement.elementIterator("rare_upgrade"); i1.hasNext();)
		{
			Element e1 = i1.next();

			int id = parseInt(e1, "id");
			int itemId = parseInt(e1, "item_id");
			int enchantLevel = parseInt(e1, "enchant_level", 0);
			long price = parseLong(e1, "price", 0L);
			int unk = parseInt(e1, "unk", 0);
			int resultItemId = 0;
			long resultItemCount = 1L;
			int resultItemEnchant = 0;

			Element resultElement = e1.element("result_item");
			if(resultElement != null)
			{
				resultItemId = parseInt(resultElement, "id");
				resultItemCount = parseLong(resultElement, "count", 1L);
				resultItemEnchant = parseInt(resultElement, "enchant", 0);
			}
			else
			{
				warn(String.format("Cannot found result_item for rare_upgrade ID[%d]!", new Object[] { Integer.valueOf(id) }));
				continue;
			}
			RareUpgradeData data = new RareUpgradeData(id, itemId, enchantLevel, price, unk, resultItemId, resultItemCount, resultItemEnchant);
			for(Iterator<Element> i2 = e1.elementIterator(); i2.hasNext();)
			{
				Element e2 =i2.next();
				//Iterator<Element> i3;
				if("required_items".equalsIgnoreCase(e2.getName()))
				{
					for(Iterator<Element> i3 = e2.elementIterator("item"); i3.hasNext();)
					{
						Element e3 = i3.next();
						int id1 = parseInt(e3, "id");
						long count = parseLong(e3, "count");
						int enchant = parseInt(e3, "enchant", 0);
						data.addRequiredItem(new UpgradeItemData(id1, count, enchant));
					}
				}
				else if("unk_data".equalsIgnoreCase(e2.getName()))
				{
					for(Iterator<Element> i3 = e2.elementIterator("unk"); i3.hasNext();)
					{
						Element e3 = i3.next();
						int value = parseInt(e3, "value");
						data.addUnkData(value);
					}
				}
			}
			getHolder().addRareUpgradeData(data);
		}
	}
}
