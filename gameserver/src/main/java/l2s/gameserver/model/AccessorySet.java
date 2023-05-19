package l2s.gameserver.model;

import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.hash.TIntHashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;

public final class AccessorySet
{
	private final TIntHashSet _necklace = new TIntHashSet();
	private final TIntHashSet _right_ear = new TIntHashSet();
	private final TIntHashSet _left_ear = new TIntHashSet();
	private final TIntHashSet _right_finger = new TIntHashSet();
	private final TIntHashSet _left_finger = new TIntHashSet();
	
	private final TIntObjectHashMap<List<SkillEntry>> _skills = new TIntObjectHashMap<List<SkillEntry>>();
	private final List<SkillEntry> _shieldSkills = new ArrayList<SkillEntry>();
	private final List<SkillEntry> _enchant6skills = new ArrayList<SkillEntry>();
	private final List<SkillEntry> _enchant7skills = new ArrayList<SkillEntry>();
	private final List<SkillEntry> _enchant8skills = new ArrayList<SkillEntry>();
	private final List<SkillEntry> _enchant9skills = new ArrayList<SkillEntry>();
	private final List<SkillEntry> _enchant10skills = new ArrayList<SkillEntry>();

	public AccessorySet( String[] necklace, String[] right_ear, String[] left_ear, String[] right_finger, String[] left_finger,  String[] enchant6skills, String[] enchant7skills, String[] enchant8skills, String[] enchant9skills, String[] enchant10skills)
	{
		_necklace.addAll(parseItemIDs(necklace));
		_right_ear.addAll(parseItemIDs(right_ear));
		_left_ear.addAll(parseItemIDs(left_ear));
		_right_finger.addAll(parseItemIDs(right_finger));
		_left_finger.addAll(parseItemIDs(left_finger));

		if(enchant6skills != null)
		{
			for(String skill : enchant6skills)
			{
				StringTokenizer st = new StringTokenizer(skill, "-");
				if(st.hasMoreTokens())
				{
					int skillId = Integer.parseInt(st.nextToken());
					int skillLvl = Integer.parseInt(st.nextToken());
					_enchant6skills.add(SkillEntry.makeSkillEntry(SkillEntryType.NONE, skillId, skillLvl));
				}
			}
		}

		if(enchant7skills != null)
		{
			for(String skill : enchant7skills)
			{
				StringTokenizer st = new StringTokenizer(skill, "-");
				if(st.hasMoreTokens())
				{
					int skillId = Integer.parseInt(st.nextToken());
					int skillLvl = Integer.parseInt(st.nextToken());
					_enchant7skills.add(SkillEntry.makeSkillEntry(SkillEntryType.NONE, skillId, skillLvl));
				}
			}
		}

		if(enchant8skills != null)
		{
			for(String skill : enchant8skills)
			{
				StringTokenizer st = new StringTokenizer(skill, "-");
				if(st.hasMoreTokens())
				{
					int skillId = Integer.parseInt(st.nextToken());
					int skillLvl = Integer.parseInt(st.nextToken());
					_enchant8skills.add(SkillEntry.makeSkillEntry(SkillEntryType.NONE, skillId, skillLvl));
				}
			}
		}

		if(enchant9skills != null)
		{
			for(String skill : enchant9skills)
			{
				StringTokenizer st = new StringTokenizer(skill, "-");
				if(st.hasMoreTokens())
				{
					int skillId = Integer.parseInt(st.nextToken());
					int skillLvl = Integer.parseInt(st.nextToken());
					_enchant9skills.add(SkillEntry.makeSkillEntry(SkillEntryType.NONE, skillId, skillLvl));
				}
			}
		}

		if(enchant10skills != null)
		{
			for(String skill : enchant10skills)
			{
				StringTokenizer st = new StringTokenizer(skill, "-");
				if(st.hasMoreTokens())
				{
					int skillId = Integer.parseInt(st.nextToken());
					int skillLvl = Integer.parseInt(st.nextToken());
					_enchant10skills.add(SkillEntry.makeSkillEntry(SkillEntryType.NONE, skillId, skillLvl));
				}
			}
		}
	}

	private static int[] parseItemIDs(String[] items)
	{
		TIntHashSet result = new TIntHashSet();
		if(items != null)
		{
			for(String s_id : items)
			{
				int id = Integer.parseInt(s_id);
				if(id > 0)
				{
					result.add(id);
				}
			}
		}
		return result.toArray();
	}

	public void addSkills(int partsCount, String[] skills)
	{
		List<SkillEntry> skillList = new ArrayList<SkillEntry>();
		if(skills != null)
		{
			for(String skill : skills)
			{
				StringTokenizer st = new StringTokenizer(skill, "-");
				if(st.hasMoreTokens())
				{
					int skillId = Integer.parseInt(st.nextToken());
					int skillLvl = Integer.parseInt(st.nextToken());
					skillList.add(SkillEntry.makeSkillEntry(SkillEntryType.NONE, skillId, skillLvl));
				}
			}
		}
		_skills.put(partsCount, skillList);
	}

	/**
	 * Checks if player have equipped all items from set (not checking shield)
	 * @param player whose inventory is being checked
	 * @return True if player equips whole set
	 */
	public boolean containAll(Player player)
	{
		Inventory inv = player.getInventory();
		ItemInstance necklaceItem = inv.getPaperdollItem(Inventory.PAPERDOLL_NECK);
		ItemInstance right_earItem = inv.getPaperdollItem(Inventory.PAPERDOLL_REAR);
		ItemInstance left_earItem = inv.getPaperdollItem(Inventory.PAPERDOLL_LEAR);
		ItemInstance right_fingerItem = inv.getPaperdollItem(Inventory.PAPERDOLL_RFINGER);
		ItemInstance left_fingerItem = inv.getPaperdollItem(Inventory.PAPERDOLL_LFINGER);

		int necklace = 0;
		int right_ear = 0;
		int left_ear = 0;
		int right_finger = 0;
		int left_finger = 0;

		if(necklaceItem != null)
			necklace = necklaceItem.getItemId();
		if(right_earItem != null)
			right_ear = right_earItem.getItemId();
		if(left_earItem != null)
			left_ear = left_earItem.getItemId();
		if(right_fingerItem != null)
			right_finger = right_fingerItem.getItemId();
		if(left_fingerItem != null)
			left_finger = left_fingerItem.getItemId();

		return containAll( necklace, right_ear, left_ear, right_finger, left_finger);
	}

	public boolean containAll( int necklace, int right_ear, int left_ear, int right_finger, int left_finger)
	{
		if(!_necklace.isEmpty() && !_necklace.contains(necklace))
			return false;
		if(!_right_ear.isEmpty() && !_right_ear.contains(right_ear))
			return false;
		if(!_left_ear.isEmpty() && !_left_ear.contains(left_ear))
			return false;
		if(!_right_finger.isEmpty() && !_right_finger.contains(right_finger))
			return false;
		if(!_left_finger.isEmpty() && !_left_finger.contains(left_finger))
			return false;

		return true;
	}

	public boolean containItem(int slot, int itemId)
	{
		switch(slot)
		{
			case Inventory.PAPERDOLL_NECK:
				return _necklace.contains(itemId);
			case Inventory.PAPERDOLL_REAR:
				return _right_ear.contains(itemId);
			case Inventory.PAPERDOLL_LEAR:
				return _left_ear.contains(itemId);
			case Inventory.PAPERDOLL_RFINGER:
				return _right_finger.contains(itemId);
			case Inventory.PAPERDOLL_LFINGER:
				return _left_finger.contains(itemId);
			default:
				return false;
		}
	}

	public int getEquipedSetPartsCount(Player player)
	{
		Inventory inv = player.getInventory();
		ItemInstance necklaceItem = inv.getPaperdollItem(Inventory.PAPERDOLL_NECK);
		ItemInstance right_earItem = inv.getPaperdollItem(Inventory.PAPERDOLL_REAR);
		ItemInstance left_earItem = inv.getPaperdollItem(Inventory.PAPERDOLL_LEAR);
		ItemInstance right_fingerItem = inv.getPaperdollItem(Inventory.PAPERDOLL_RFINGER);
		ItemInstance left_fingerItem = inv.getPaperdollItem(Inventory.PAPERDOLL_LFINGER);

		int necklace = 0;
		int right_ear = 0;
		int left_ear = 0;
		int right_finger = 0;
		int left_finger = 0;

		if(necklaceItem != null)
			necklace = necklaceItem.getItemId();
		if(right_earItem != null)
			right_ear = right_earItem.getItemId();
		if(left_earItem != null)
			left_ear = left_earItem.getItemId();
		if(right_fingerItem != null)
			right_finger = right_fingerItem.getItemId();
		if(left_fingerItem != null)
			left_finger = left_fingerItem.getItemId();

		int result = 0;
		if(!_necklace.isEmpty() && _necklace.contains(necklace))
			result++;
		if(!_right_ear.isEmpty() && _right_ear.contains(right_ear))
			result++;
		if(!_left_ear.isEmpty() && _left_ear.contains(left_ear))
			result++;
		if(!_right_finger.isEmpty() && _right_finger.contains(right_finger))
			result++;
		if(!_left_finger.isEmpty() && _left_finger.contains(left_finger))
			result++;

		return result;
	}

	public List<SkillEntry> getSkills(int partsCount)
	{
		if(_skills.get(partsCount) == null)
			return new ArrayList<SkillEntry>();

		return _skills.get(partsCount);
	}

	public List<SkillEntry> getSkillsToRemove()
	{
		List<SkillEntry> result = new ArrayList<SkillEntry>();
		for(int i : _skills.keys())
		{
			List<SkillEntry> skills = _skills.get(i);
			if(skills != null)
			{
				for(SkillEntry skill : skills)
					result.add(skill);
			}
		}
		return result;
	}

	public List<SkillEntry> getShieldSkills()
	{
		return _shieldSkills;
	}

	public List<SkillEntry> getEnchant6skills()
	{
		return _enchant6skills;
	}

	public List<SkillEntry> getEnchant7skills()
	{
		return _enchant7skills;
	}

	public List<SkillEntry> getEnchant8skills()
	{
		return _enchant8skills;
	}

	public List<SkillEntry> getEnchant9skills()
	{
		return _enchant9skills;
	}

	public List<SkillEntry> getEnchant10skills()
	{
		return _enchant10skills;
	}

	/**
	 * Checks if all parts of set are enchanted to +6 or more
	 * @param player
	 * @return
	 */
	public int getEnchantLevel(Player player)
	{
		// Player don't have full set
		if(!containAll(player))
			return 0;

		Inventory inv = player.getInventory();

		ItemInstance necklaceItem = inv.getPaperdollItem(Inventory.PAPERDOLL_NECK);
		ItemInstance right_earItem = inv.getPaperdollItem(Inventory.PAPERDOLL_REAR);
		ItemInstance left_earItem = inv.getPaperdollItem(Inventory.PAPERDOLL_LEAR);
		ItemInstance right_fingerItem = inv.getPaperdollItem(Inventory.PAPERDOLL_RFINGER);
		ItemInstance left_fingerItem = inv.getPaperdollItem(Inventory.PAPERDOLL_LFINGER);

		int value = -1;
		if(!_necklace.isEmpty())
			value = value > -1 ? Math.min(value, necklaceItem.getFixedEnchantLevel(player)) : necklaceItem.getFixedEnchantLevel(player);

		if(!_right_ear.isEmpty())
			value = value > -1 ? Math.min(value, right_earItem.getFixedEnchantLevel(player)) : right_earItem.getFixedEnchantLevel(player);

		if(!_left_ear.isEmpty())
			value = value > -1 ? Math.min(value, left_earItem.getFixedEnchantLevel(player)) : left_earItem.getFixedEnchantLevel(player);

		if(!_right_finger.isEmpty())
			value = value > -1 ? Math.min(value, right_fingerItem.getFixedEnchantLevel(player)) : right_fingerItem.getFixedEnchantLevel(player);

		if(!_left_finger.isEmpty())
			value = value > -1 ? Math.min(value, left_fingerItem.getFixedEnchantLevel(player)) : left_fingerItem.getFixedEnchantLevel(player);

		return value;
	}

	public int[] getNecklaceIds()
	{
		return _necklace.toArray();
	}

	public int[] getRight_earIds()
	{
		return _right_ear.toArray();
	}

	public int[] getLeft_earIds()
	{
		return _left_ear.toArray();
	}

	public int[] getRight_fingerIds()
	{
		return _right_finger.toArray();
	}

	public int[] getLeft_fingerIds()
	{
		return _left_finger.toArray();
	}
}