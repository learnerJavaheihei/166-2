package l2s.gameserver.model.items.listeners;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.data.xml.holder.AccessorySetsHolder;
import l2s.gameserver.model.AccessorySet;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.skills.SkillEntry;


public final class AccessorySetListener extends AbstractSkillListener
{
	private static final AccessorySetListener _instance = new AccessorySetListener();

	public static AccessorySetListener getInstance()
	{
		return _instance;
	}

	@Override
	public int onEquip(int slot, ItemInstance item, Playable actor)
	{
		if(!item.isEquipable())
			return 0;

		if(!actor.isPlayer())
			return 0;

		List<AccessorySet> AccessorySets = AccessorySetsHolder.getInstance().getAccessorySets(item.getItemId());
		if(AccessorySets == null || AccessorySets.isEmpty())
			return 0;

		Player player = actor.getPlayer();

		int accessorySetEnchant = 0;

		int flags = 0;

		List<SkillEntry> addedSkills = new ArrayList<SkillEntry>();

		for(AccessorySet AccessorySet : AccessorySets)
		{
			// checks if equipped item is part of set
			if(AccessorySet.containItem(slot, item.getItemId()))
			{
				List<SkillEntry> skills = AccessorySet.getSkills(AccessorySet.getEquipedSetPartsCount(player));
				for(SkillEntry skillEntry : skills)
					addedSkills.add(skillEntry);

				if(AccessorySet.containAll(player))
				{
					int enchantLevel = AccessorySet.getEnchantLevel(player);
					if(enchantLevel >= 8) // 修改+8出現+6
					{
						skills = AccessorySet.getEnchant6skills();
						for(SkillEntry skillEntry : skills)
							addedSkills.add(skillEntry);
					}
					if(enchantLevel >= 11) // 修改+11出現+7
					{
						skills = AccessorySet.getEnchant7skills();
						for(SkillEntry skillEntry : skills)
							addedSkills.add(skillEntry);
					}
					if(enchantLevel >= 14) // 修改+14出現+8
					{
						skills = AccessorySet.getEnchant8skills();
						for(SkillEntry skillEntry : skills)
							addedSkills.add(skillEntry);
					}
					if(enchantLevel >= 17) // 修改+17出現+9
					{
						skills = AccessorySet.getEnchant9skills();
						for(SkillEntry skillEntry : skills)
							addedSkills.add(skillEntry);
					}
					if(enchantLevel >= 20) // 修改+20出現+10
					{
						skills = AccessorySet.getEnchant10skills();
						for(SkillEntry skillEntry : skills)
							addedSkills.add(skillEntry);
					}
					if(enchantLevel > accessorySetEnchant)
						accessorySetEnchant = enchantLevel;
				}
			}
		}

		player.setAccessorySetEnchant(accessorySetEnchant);
		flags |= refreshSkills(player, item, addedSkills);
		return flags;
	}

@Override
public int onUnequip(int slot, ItemInstance item, Playable actor)
{
	if(!item.isEquipable())
		return 0;

	if(!actor.isPlayer())
		return 0;

	List<AccessorySet> AccessorySets = AccessorySetsHolder.getInstance().getAccessorySets(item.getItemId());
	if(AccessorySets == null || AccessorySets.isEmpty())
		return 0;

	Player player = actor.getPlayer();

	int flags = super.onUnequip(slot, item, actor);

	int AccessorySetEnchant = 0;

	for(AccessorySet AccessorySet : AccessorySets)
	{
		boolean remove = false;
		//boolean setPartUneqip = false;
		List<SkillEntry> removeSkillId1 = new ArrayList<SkillEntry>(); // set skill
		List<SkillEntry> removeSkillId2 = new ArrayList<SkillEntry>(); // shield skill
		List<SkillEntry> removeSkillId3 = new ArrayList<SkillEntry>(); // enchant +6 skill
		List<SkillEntry> removeSkillId4 = new ArrayList<SkillEntry>(); // enchant +7 skill
		List<SkillEntry> removeSkillId5 = new ArrayList<SkillEntry>(); // enchant +8 skill
		List<SkillEntry> removeSkillId6 = new ArrayList<SkillEntry>(); // enchant +9 skill
		List<SkillEntry> removeSkillId7 = new ArrayList<SkillEntry>(); // enchant +10 skill

		if(AccessorySet.containItem(slot, item.getItemId())) // removed part of set
		{
			remove = true;
			//setPartUneqip = true;
			removeSkillId1 = AccessorySet.getSkillsToRemove();
			removeSkillId2 = AccessorySet.getShieldSkills();//應該是沒用的
			removeSkillId3 = AccessorySet.getEnchant6skills();
			removeSkillId4 = AccessorySet.getEnchant7skills();
			removeSkillId5 = AccessorySet.getEnchant8skills();
			removeSkillId6 = AccessorySet.getEnchant9skills();
			removeSkillId7 = AccessorySet.getEnchant10skills();
		}

		if(remove)
		{
			for(SkillEntry skillEntry : removeSkillId1)
			{
				if(player.removeSkill(skillEntry, false) != null)
					flags |= Inventory.UPDATE_SKILLS_FLAG;
			}
			for(SkillEntry skillEntry : removeSkillId2)
			{
				if(player.removeSkill(skillEntry) != null)
					flags |= Inventory.UPDATE_SKILLS_FLAG;
			}
			for(SkillEntry skillEntry : removeSkillId3)
			{
				if(player.removeSkill(skillEntry) != null)
					flags |= Inventory.UPDATE_SKILLS_FLAG;
			}
			for(SkillEntry skillEntry : removeSkillId4)
			{
				if(player.removeSkill(skillEntry) != null)
					flags |= Inventory.UPDATE_SKILLS_FLAG;
			}
			for(SkillEntry skillEntry : removeSkillId5)
			{
				if(player.removeSkill(skillEntry) != null)
					flags |= Inventory.UPDATE_SKILLS_FLAG;
			}
			for(SkillEntry skillEntry : removeSkillId6)
			{
				if(player.removeSkill(skillEntry) != null)
					flags |= Inventory.UPDATE_SKILLS_FLAG;
			}
			for(SkillEntry skillEntry : removeSkillId7)
			{
				if(player.removeSkill(skillEntry) != null)
					flags |= Inventory.UPDATE_SKILLS_FLAG;
			}
		}

		int enchantLevel = AccessorySet.getEnchantLevel(player);
		if(enchantLevel > AccessorySetEnchant)
			AccessorySetEnchant = enchantLevel;

		List<SkillEntry> skills = AccessorySet.getSkills(AccessorySet.getEquipedSetPartsCount(player));
		for(SkillEntry skillEntry : skills)
		{
			if(player.addSkill(skillEntry, false) != skillEntry)
			{
				flags |= Inventory.UPDATE_SKILLS_FLAG;
			}
			item.addEquippedSkill(AccessorySet, skillEntry);
		}
	}
	player.setAccessorySetEnchant(AccessorySetEnchant);
	return flags;
}

@Override
public int onRefreshEquip(ItemInstance item, Playable actor)
{
	return onEquip(item.getEquipSlot(), item, actor);
}
}