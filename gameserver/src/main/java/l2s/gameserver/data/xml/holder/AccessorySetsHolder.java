package l2s.gameserver.data.xml.holder;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.List;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.model.AccessorySet;

public final class AccessorySetsHolder extends AbstractHolder
{
	private static final AccessorySetsHolder _instance = new AccessorySetsHolder();
	private final TIntObjectHashMap<List<AccessorySet>> _accessorySets = new TIntObjectHashMap<List<AccessorySet>>();

	public static AccessorySetsHolder getInstance()
	{
		return _instance;
	}

	public void addAccessorySet(AccessorySet accessoryset)
	{
		for(int id : accessoryset.getNecklaceIds())
		{
			List<AccessorySet> sets = _accessorySets.get(id);
			if(sets == null)
				sets = new ArrayList<AccessorySet>();
			sets.add(accessoryset);
			_accessorySets.put(id, sets);
		}

		for(int id : accessoryset.getRight_earIds())
		{
			List<AccessorySet> sets = _accessorySets.get(id);
			if(sets == null)
				sets = new ArrayList<AccessorySet>();
			sets.add(accessoryset);
			_accessorySets.put(id, sets);
		}

		for(int id : accessoryset.getLeft_earIds())
		{
			List<AccessorySet> sets = _accessorySets.get(id);
			if(sets == null)
				sets = new ArrayList<AccessorySet>();
			sets.add(accessoryset);
			_accessorySets.put(id, sets);
		}

		for(int id : accessoryset.getRight_fingerIds())
		{
			List<AccessorySet> sets = _accessorySets.get(id);
			if(sets == null)
				sets = new ArrayList<AccessorySet>();
			sets.add(accessoryset);
			_accessorySets.put(id, sets);
		}

		for(int id : accessoryset.getLeft_fingerIds())
		{
			List<AccessorySet> sets = _accessorySets.get(id);
			if(sets == null)
				sets = new ArrayList<AccessorySet>();
			sets.add(accessoryset);
			_accessorySets.put(id, sets);
		}
	}

	public List<AccessorySet> getAccessorySets(int id)
	{
		return _accessorySets.get(id);
	}

	@Override
	public int size()
	{
		return _accessorySets.size();
	}

	@Override
	public void clear()
	{
		_accessorySets.clear();
	}
}
