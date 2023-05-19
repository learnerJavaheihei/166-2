package l2s.gameserver.data.xml.parser;

import java.io.File;
import java.util.Iterator;

import org.dom4j.Element;
import l2s.commons.data.xml.AbstractParser;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.AccessorySetsHolder;
import l2s.gameserver.model.AccessorySet;

public final class AccessorySetsParser extends AbstractParser<AccessorySetsHolder>
{
	private static final AccessorySetsParser _instance = new AccessorySetsParser();

	public static AccessorySetsParser getInstance()
	{
		return _instance;
	}

	private AccessorySetsParser()
	{
		super(AccessorySetsHolder.getInstance());
	}

	@Override
	public File getXMLPath()
	{
		return new File(Config.DATAPACK_ROOT, "data/accssory_sets.xml");
	}

	@Override
	public String getDTDFileName()
	{
		return "accssory_sets.dtd";
	}

	@Override
	protected void readData(Element rootElement) throws Exception
	{
		for(Iterator<Element> iterator = rootElement.elementIterator(); iterator.hasNext();)
		{
			Element element = iterator.next();
			if("set".equalsIgnoreCase(element.getName()))
			{
				String[]  necklace = null, right_ear = null, left_ear = null, right_finger = null, left_finger = null,  enchant6skills = null, enchant7skills = null, enchant8skills = null, enchant9skills = null, enchant10skills = null;
				if(element.attributeValue("necklace") != null)
					necklace = element.attributeValue("necklace").split(";");
				if(element.attributeValue("right_ear") != null)
					right_ear = element.attributeValue("right_ear").split(";");
				if(element.attributeValue("left_ear") != null)
					left_ear = element.attributeValue("left_ear").split(";");
				if(element.attributeValue("right_finger") != null)
					right_finger = element.attributeValue("right_finger").split(";");
				if(element.attributeValue("left_finger") != null)
					left_finger = element.attributeValue("left_finger").split(";");
				if(element.attributeValue("enchant6skills") != null)
					enchant6skills = element.attributeValue("enchant6skills").split(";");
				if(element.attributeValue("enchant7skills") != null)
					enchant7skills = element.attributeValue("enchant7skills").split(";");
				if(element.attributeValue("enchant8skills") != null)
					enchant8skills = element.attributeValue("enchant8skills").split(";");
				if(element.attributeValue("enchant9skills") != null)
					enchant9skills = element.attributeValue("enchant9skills").split(";");
				if(element.attributeValue("enchant10skills") != null)
					enchant10skills = element.attributeValue("enchant10skills").split(";");

				AccessorySet accessorySet = new AccessorySet( necklace, right_ear, left_ear, right_finger, left_finger, enchant6skills, enchant7skills, enchant8skills, enchant9skills, enchant10skills);
				for(Iterator<Element> subIterator = element.elementIterator(); subIterator.hasNext();)
				{
					Element subElement = subIterator.next();
					if("set_skills".equalsIgnoreCase(subElement.getName()))
					{
						int partsCount = Integer.parseInt(subElement.attributeValue("parts"));
						String[] skills = subElement.attributeValue("skills").split(";");
						accessorySet.addSkills(partsCount, skills);
					}
				}
				getHolder().addAccessorySet(accessorySet);
			}
		}
	}
}