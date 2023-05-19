package l2s.gameserver.data.xml.holder;

import java.util.HashMap;//外型加工石解除還加工石
import java.util.Map;//外型加工石解除還加工石

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.templates.item.support.AppearanceStone;

/**
 * @author Bonux
**/
public class AppearanceStoneHolder extends AbstractHolder
{
	private static final AppearanceStoneHolder _instance = new AppearanceStoneHolder();

	//private TIntObjectMap<AppearanceStone> _stones = new TIntObjectHashMap<AppearanceStone>();
	private Map<Integer, AppearanceStone> _stones = new HashMap<Integer , AppearanceStone>();//外型加工石解除還加工石

	public static AppearanceStoneHolder getInstance()
	{
		return _instance;
	}

	public void addAppearanceStone(AppearanceStone stone)
	{
		_stones.put(stone.getItemId(), stone);
	}

	public AppearanceStone getAppearanceStone(int id)
	{
		return _stones.get(id);
	}

	@Override
	public int size()
	{
		return _stones.size();
	}

	@Override
	public void clear()
	{
		_stones.clear();
	}
		//外型加工石解除還加工石--
	public int getAppearanceStoneId(int id)
	{
		for(AppearanceStone ap : _stones.values())
		{
			if(ap.getExtractItemId() == id)
			{
				return ap.getItemId();
			}
		}
		return 0;
	}
	//--外型加工石解除還加工石
}
