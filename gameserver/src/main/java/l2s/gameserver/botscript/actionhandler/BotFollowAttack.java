package l2s.gameserver.botscript.actionhandler;

import l2s.gameserver.botscript.BotConfigImp;
import l2s.gameserver.core.BotConfig;
import l2s.gameserver.core.BotSkillStrategy;
import l2s.gameserver.core.IBotActionHandler;
import java.util.LinkedList;
import l2s.gameserver.ai.ServitorAI;
import l2s.gameserver.model.AggroList;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.model.instances.SummonInstance;

public class BotFollowAttack implements IBotActionHandler
{
	@Override
	public boolean doAction(Player actor, BotConfig config, boolean isSitting, boolean movable, boolean simpleActionDisable)
	{
		BotConfigImp configImp = (BotConfigImp) config;
		if(config.isAutoAttack())
		{
			return false;
		}
		if(!config.isFollowAttack())
		{
			return false;
		}
		Party party = actor.getParty();
		if(party == null)
		{
			return false;
		}
		if(this.isActionsDisabledExcludeAttack(actor) || actor.isSitting())
		{
			return false;
		}
		Player leader = party.getPartyLeader();
		if(actor == leader)
		{
			return false;
		}
		double distance = leader.getDistance((GameObject) actor);
		if(distance > 2000.0)
		{
			return false;
		}
		GameObject target = leader.getTarget();
		if(target == null)
		{
			return false;
		}
		MonsterInstance mob = this.getMonster(target);
		if(target != mob)//如果目标不是怪物就返回
		{
			return false;
		}
		if(mob == null || mob.isDead())
		{
			return false;
		}
		if(actor.getTarget() != mob)
		{
			actor.setTarget((GameObject) mob);
		}
		if(configImp.isFollowAttackWhenChoosed() || this.getHate(mob, leader) > 0)
		{
			SummonInstance pet;
			boolean useStrategy = false;
			for(BotSkillStrategy s : config.getAttackStrategy())
			{
				useStrategy = s.useMe(actor, mob);
				if(useStrategy)
					break;
			}
			if(!useStrategy && config.isUsePhysicalAttack())
			{
				mob.onAction(actor, false);
			}
			if((pet = actor.getSummon()) != null && config.isSummonAttack() && !pet.isActionsDisabled())
			{
				pet.setTarget((GameObject) mob);
				pet.getAI().Attack((GameObject) mob, false, false);
			}
		}
		return true;
	}

	public int getHate(MonsterInstance mob, Player player)
	{
		AggroList.AggroInfo ai = mob.getAggroList().get((Creature) player);
		if(ai == null)
		{
			return 0;
		}
		return ai.damage;
	}
}