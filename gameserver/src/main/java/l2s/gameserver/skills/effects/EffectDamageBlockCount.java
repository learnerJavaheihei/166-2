package l2s.gameserver.skills.effects;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

public final class EffectDamageBlockCount extends EffectHandler
{
	private final int _hp;
	private final int _casterHpPer;
	private final int _distance;

	public EffectDamageBlockCount(EffectTemplate template)
	{
		super(template);
		_hp = getParams().getInteger("block_count", 0);
		_casterHpPer = getParams().getInteger("block_by_effector_max_hp_percent", 0);
		_distance = getParams().getInteger("distance_from_caster", 0);
	}

	@Override
	protected boolean checkCondition(Abnormal abnormal, Creature effector, Creature effected)
	{
		if (!effected.isPlayer())
			return false;

		Player player = effected.getPlayer();
		if (player == null)
			return false;

		if (effected.isDead() || effector.isDead())
			return false;

		return true;
	}

	@Override
	public void onStart(Abnormal abnormal, Creature effector, Creature effected)
	{
		if (effected.getDistance(effector.getLoc()) > _distance)
			return;

		int blockByCasterHp = 0;
		if (_casterHpPer != 0)
		{
			blockByCasterHp = effector.getMaxHp() * (_casterHpPer / 100);
		}
		effected.addDamageBlockValue(blockByCasterHp + _hp);
	}

	@Override
	public boolean onActionTime(Abnormal abnormal, Creature effector, Creature effected)
	{
		Player player = effected.getPlayer();
		if (player.isAlikeDead() || player == null)
		{
			return false;
		}

		if ((_distance != 0) && (player.getDistance(effector.getLoc()) > _distance))
		{
			return false;
		}
		return true;
	}
}