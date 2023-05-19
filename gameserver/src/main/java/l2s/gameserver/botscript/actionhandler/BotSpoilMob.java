package l2s.gameserver.botscript.actionhandler;

import l2s.gameserver.core.BotConfig;
import l2s.gameserver.core.BotThinkTask;
import l2s.gameserver.core.IBotActionHandler;
import l2s.gameserver.ai.PlayerAI;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.skills.SkillEntry;

public class BotSpoilMob implements IBotActionHandler
{
	@Override
	public boolean doAction(Player actor, BotConfig config, boolean isSitting, boolean movable, boolean simpleActionDisable)
	{
		if(!config.isAutoSweep())
		{
			return false;
		}
		GameObject target = actor.getTarget();
		if(target == null || !target.isMonster())
		{
			return false;
		}
		MonsterInstance mob = this.getMonster(target);
		if(mob.isDead() || mob.isSpoiled())
		{
			return false;
		}
		SkillEntry skill = actor.getKnownSkill(254);
		/*技能ID254 自體變化*/
		if(skill == null)
		{
			return false;
		}
		if(!BotThinkTask.checkSkillMpCost(actor, skill))
		{
			return false;
		}
		if(!skill.checkCondition((Creature) actor, (Creature) mob, false, false, true, false, false))
		{
			return false;
		}
		actor.getAI().Cast(skill, (Creature) mob, false, false);
		return true;
	}
}