package l2s.gameserver.skills.effects;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.components.StatusUpdate;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.StatusUpdatePacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author Bonux
**/
public class EffectRestoreCP extends EffectRestore
{
	public EffectRestoreCP(EffectTemplate template)
	{
		super(template);
	}

	private int calcAddToCp(Creature effector, Creature effected)
	{
		if(!effected.isPlayer())
			return 0;

		double power = getValue();
		if(power <= 0)
			return 0;

		if(_percent)
			power = effected.getMaxCp() / 100. * power;

		if(!_staticPower)
		{
			if(!_ignoreBonuses)
				power *= effected.getStat().calc(Stats.CPHEAL_EFFECTIVNESS, 100., effector, getSkill()) / 100.;
		}
		return (int) power;
	}

	private int checkRestoreCpLimits(Creature effected, double power)
	{
		double newCp = effected.getCurrentCp() + power;
		newCp = Math.max(0, Math.min(newCp, effected.getMaxCp() / 100. * effected.getStat().calc(Stats.CP_LIMIT, null, null)));
		newCp = Math.max(0, newCp - effected.getCurrentCp());
		newCp = Math.min(effected.getMaxCp() - effected.getCurrentCp(), newCp);
		return (int) newCp;
	}

	@Override
	public void onStart(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(effected.isHealBlocked())
			return;

		if(!getTemplate().isInstant())
			return;

		int addToCp = calcAddToCp(effector, effected);
		if(addToCp > 0)
		{
			addToCp = checkRestoreCpLimits(effected, addToCp);
			if(effector != effected)
				effected.sendPacket(new SystemMessagePacket(SystemMsg.S2_CP_HAS_BEEN_RESTORED_BY_C1).addName(effector).addInteger(addToCp));
			else
				effected.sendPacket(new SystemMessagePacket(SystemMsg.S1_CP_HAS_BEEN_RESTORED).addInteger(addToCp));

			effected.setCurrentCp(effected.getCurrentCp() + addToCp, false);
			StatusUpdate su = new StatusUpdate(effected, effector, StatusUpdatePacket.UpdateType.REGEN, StatusUpdatePacket.CUR_CP);
			effector.sendPacket(su);
			effected.sendPacket(su);
			effected.broadcastStatusUpdate();
			effected.sendChanges();
		}
	}

	@Override
	public boolean onActionTime(Abnormal abnormal, Creature effector, Creature effected)
	{
		if(getTemplate().isInstant())
			return false;

		if(effected.isHealBlocked())
			return true;

		int addToCp = calcAddToCp(effector, effected);
		if(addToCp > 0)
		{
			addToCp = checkRestoreCpLimits(effected, addToCp);
			effected.setCurrentCp(effected.getCurrentCp() + addToCp, false);
			StatusUpdate su = new StatusUpdate(effected, effector, StatusUpdatePacket.UpdateType.REGEN, StatusUpdatePacket.CUR_CP);
			effector.sendPacket(su);
			effected.sendPacket(su);
			effected.broadcastStatusUpdate();
			effected.sendChanges();
		}
		return true;
	}
}