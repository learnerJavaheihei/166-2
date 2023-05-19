package l2s.gameserver.botscript.actionhandler;

import l2s.gameserver.botscript.BotConfigImp;
import l2s.gameserver.core.BotConfig;
import l2s.gameserver.core.BotProperties;
import l2s.gameserver.core.BotThinkTask;
import l2s.gameserver.core.IBotActionHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.SummonInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.skills.AbnormalType;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.utils.ItemFunctions;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Collections;
import java.util.List;

public class BotSupport implements IBotActionHandler
{
	@Override
	public boolean doAction(Player actor, BotConfig config, boolean isSitting, boolean movable, boolean simpleActionDisable)
	{
		BotConfigImp botConfig;
		SkillEntry skill;
		SummonInstance pet;
		if(this.isActionsDisabledExcludeAttack(actor) || isSitting)
		{
			return false;
		}
		BotConfigImp configImp = (BotConfigImp) config;
		if(actor.getKnownSkill(110) != null && actor.getCurrentHpPercents() <= configImp.getLimitDefense() && !actor.isSkillDisabled((skill = actor.getKnownSkill(110)).getTemplate()) && BotThinkTask.checkSkillMpCost(actor, skill))
		/*技能ID110 極限防禦*/
		{
			actor.getAI().Cast(skill, actor, false, false);
			return true;
		}
		if(actor.getKnownSkill(111) != null && actor.getCurrentHpPercents() <= configImp.getLimitEvade() && !actor.isSkillDisabled((skill = actor.getKnownSkill(111)).getTemplate()) && BotThinkTask.checkSkillMpCost(actor, skill))
		/*技能ID111 極限迴避*/
		{
			actor.getAI().Cast(skill, actor, false, false);
			return true;
		}
		if(!config.getAutoItemBuffs().isEmpty())
		{
			boolean itemUse = false;
			for(int itemId : config.getAutoItemBuffs())
			{
				ItemInstance item;
				Skill skill2;
				if(!ArrayUtils.contains(BotProperties.BUFF_ITEM_IDS, itemId) || (item = actor.getInventory().getItemByItemId(itemId)) == null || actor.isSkillDisabled(skill2 = item.getTemplate().getFirstSkill().getTemplate()) || skill2.getAbnormalType() == AbnormalType.NONE || actor.getAbnormalList().contains(skill2.getAbnormalType()))
					continue;
				actor.setTarget(actor);
				itemUse = ItemFunctions.useItem(actor, item, false, false);
				if(!itemUse)
					continue;
				return true;
			}
		}
		if((pet = actor.getSummon()) != null && !config.getPetBuffs().isEmpty() && !pet.isDead() && pet.getDistance(actor) < 2500.0)
		{
			for(int skillId : config.getPetBuffs())
			{
				Creature target;
				SkillEntry skill3 = actor.getKnownSkill(skillId);
				if(skill3 == null || actor.isSkillDisabled(skill3.getTemplate()) || (target = skill3.getTemplate().getAimingTarget(actor, pet)).getAbnormalList().contains(skill3.getTemplate().getAbnormalType()))
					continue;
				actor.setTarget(target);
				actor.getAI().Cast(skill3, target, false, false);
				return true;
			}
		}
		if((botConfig = (BotConfigImp) config).getBuffSets().isEmpty())
		{
			return false;
		}
		Party party = actor.getParty();
		List<Player> players = party != null ? party.getPartyMembers() : Collections.singletonList(actor);
		for(Player player : players)
		{
			SkillEntry skillEntry;
			String buffGroupName;
			Creature target;
			if(!player.isDead() && player.getDistance(actor) <= 3000.0 && (buffGroupName = botConfig.getBuffConfig().get(player.getName())) != null)
			{
				for(int id : botConfig.getBuffSets().get(buffGroupName))
				{
					Skill skill4;
					skillEntry = actor.getKnownSkill(id);
					if(skillEntry == null || actor.isSkillDisabled(skill4 = skillEntry.getTemplate()))
						continue;
					if(!BotThinkTask.checkSkillMpCost(actor, skillEntry))
					{
						return false;
					}
					if(skill4.getTargetType() != Skill.SkillTargetType.TARGET_ALLY && skill4.getTargetType() != Skill.SkillTargetType.TARGET_CLAN && skill4.getTargetType() != Skill.SkillTargetType.TARGET_PARTY && skill4.getTargetType() != Skill.SkillTargetType.TARGET_PARTY_WITHOUT_ME && skill4.getTargetType() != Skill.SkillTargetType.TARGET_CLAN_ONLY && (target = skill4.getAimingTarget(actor, player)) != player || player.getAbnormalList().contains(skill4.getId()))
						continue;
					actor.setTarget(player);
					actor.getAI().Cast(skillEntry, player, false, false);
					if(player.getAbnormalList().contains(skill4.getAbnormalType()))
						continue;
					return true;
				}
			}
			if((pet = player.getSummon()) == null || pet.isDead() || !(pet.getDistance(actor) <= 3000.0) || (buffGroupName = botConfig.getBuffConfig().get(player.getName())) == null)
				continue;
			for(int id : botConfig.getBuffSets().get(buffGroupName))
			{
				Skill skill2;
				skillEntry = actor.getKnownSkill(id);
				if(skillEntry == null || actor.isSkillDisabled(skill2 = skillEntry.getTemplate()))
					continue;
				if(!BotThinkTask.checkSkillMpCost(actor, skillEntry))
				{
					return false;
				}
				if(skill2.getTargetType() != Skill.SkillTargetType.TARGET_ALLY && skill2.getTargetType() != Skill.SkillTargetType.TARGET_CLAN && skill2.getTargetType() != Skill.SkillTargetType.TARGET_PARTY && skill2.getTargetType() != Skill.SkillTargetType.TARGET_PARTY_WITHOUT_ME && skill2.getTargetType() != Skill.SkillTargetType.TARGET_CLAN_ONLY && (target = skill2.getAimingTarget(actor, pet)) != pet || pet.getAbnormalList().contains(skill2.getId()))
					continue;
				actor.setTarget(pet);
				actor.getAI().Cast(skillEntry, pet, false, false);
				return true;
			}
		}
		return false;
	}
}