package l2s.gameserver.model.items.listeners;

import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.stats.Formulas;
import l2s.gameserver.templates.item.EtcItemTemplate.EtcItemType;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.support.Ensoul;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ItemSkillsListener extends AbstractSkillListener
{
	private static final ItemSkillsListener _instance = new ItemSkillsListener();

	public static ItemSkillsListener getInstance()
	{
		return _instance;
	}

	public int onEquip(int slot, ItemInstance item, Playable actor, boolean refresh)
	{
		if(!actor.isPlayer())
			return 0;

		Player player = actor.getPlayer();

		ItemTemplate template = item.getTemplate();

		if(!refresh)
			player.removeTriggers(template);

		int flags = 0;

		List<SkillEntry> addedSkills = new ArrayList<SkillEntry>();

		// Для оружия при несоотвествии грейда скилы не выдаем
		if(template.getType2() != ItemTemplate.TYPE2_WEAPON || player.getWeaponsExpertisePenalty() == 0)
		{
			if(!refresh)
				player.addTriggers(template);

			if(template.getItemType() == EtcItemType.RUNE_SELECT)
			{
				for(SkillEntry itemSkillEntry : template.getAttachedSkills())
				{
					int skillsCount = 1;
					for(ItemInstance ii : player.getInventory().getItems())
					{
						if(ii == item)
							continue;

						ItemTemplate it = ii.getTemplate();
						if(it.getItemType() == EtcItemType.RUNE_SELECT)
						{
							for(SkillEntry se : it.getAttachedSkills())
							{
								if(se == itemSkillEntry)
								{
									skillsCount++;
									break;
								}
							}
						}
					}

					int skillLevel = Math.min(itemSkillEntry.getTemplate().getMaxLevel(), skillsCount);
					SkillEntry skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, itemSkillEntry.getId(), skillLevel);
					if(skillEntry != null)
						addedSkills.add(skillEntry);
				}
			}
			else
			{
				long targetSlot = item.getTemplate().getBodyPart();//主要壺精和輔助壺精的修復--
				if(targetSlot == ItemTemplate.SLOT_AGATHION)
				{
					ItemInstance agathion = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_AGATHION_MAIN);
					if(agathion == null || agathion != null && agathion == item)
					{//--主要壺精和輔助壺精的修復
						addedSkills.addAll(Arrays.asList(template.getAttachedSkills()));//原始有的
					}//主要壺精和輔助壺精的修復--
					else
					{
						for(SkillEntry x : template.getAttachedSkills())
						{
							if(x.getTemplate().isPassive())
							{
								addedSkills.add(x);
							}
						}
					}
				}
				else
				{
					addedSkills.addAll(Arrays.asList(template.getAttachedSkills()));
				}
				item.removeEquippedSkills(addedSkills);//--主要壺精和輔助壺精的修復

				for(int e = 0; e <= item.getFixedEnchantLevel(player); e++)
				{
					List<SkillEntry> enchantSkills = template.getEnchantSkills(e);
					if(enchantSkills != null)
						addedSkills.addAll(enchantSkills);
				}

				addedSkills.addAll(item.getAppearanceStoneSkills());

				for(Ensoul ensoul : item.getNormalEnsouls())
					addedSkills.addAll(ensoul.getSkills());

				for(Ensoul ensoul : item.getSpecialEnsouls())
					addedSkills.addAll(ensoul.getSkills());
			}
		}
		if (!item.getTemplate().testCondition(actor,item,true)) {
			addedSkills.clear();
		}

		flags |= refreshSkills(actor, item, addedSkills);
		return flags;
	}

	@Override
	protected boolean canAddSkill(Playable actor, ItemInstance item, SkillEntry skillEntry)
	{
		return item.getTemplate().getItemType() == EtcItemType.RUNE_SELECT || skillEntry.getLevel() >= actor.getSkillLevel(skillEntry.getId());
	}

	@Override
	protected int onAddSkill(Playable actor, ItemInstance item, SkillEntry skillEntry)
	{
		Skill itemSkill = skillEntry.getTemplate();
		if(itemSkill.isActive())
		{
			if(!actor.isSkillDisabled(itemSkill))
			{
				long reuseDelay = Formulas.calcSkillReuseDelay(actor, itemSkill);
				reuseDelay = Math.min(reuseDelay, 30000);

				if(reuseDelay > 0)
					actor.disableSkill(itemSkill, reuseDelay);
			}
		}
		return 0;
	}

	@Override
	public int onEquip(int slot, ItemInstance item, Playable actor)
	{
		return onEquip(slot, item, actor, false);
	}

	@Override
	public int onUnequip(int slot, ItemInstance item, Playable actor)
	{
		if(!actor.isPlayer())
			return 0;

		actor.removeTriggers(item.getTemplate());
		return super.onUnequip(slot, item, actor);
	}

	@Override
	public int onRefreshEquip(ItemInstance item, Playable actor)
	{
		return onEquip(item.getEquipSlot(), item, actor, true);
	}
}