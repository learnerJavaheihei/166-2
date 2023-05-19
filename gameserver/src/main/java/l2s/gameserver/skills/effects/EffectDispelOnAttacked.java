package l2s.gameserver.skills.effects;

import l2s.commons.lang.reference.HardReference;
import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.listener.actor.OnCurrentHpDamageListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * reworked by 銘豪 效果內收到傷害次數消除輔助
 **/ //EffectDispelOnAttacked
public final class EffectDispelOnAttacked extends EffectHandler
{
	private class EffectDispelOnAttackedImpl extends EffectHandler
	{
		private class DamageListener implements OnCurrentHpDamageListener
		{
			private final HardReference<? extends Creature> _effectorRef;
			private final HardReference<? extends Creature> _effectedRef;

			public DamageListener(Creature effector, Creature effected)
			{
				_effectorRef = effector.getRef();
				_effectedRef = effected.getRef();
			}

			@Override
			public void onCurrentHpDamage(Creature actor, double damage, Creature attacker, Skill skill)
			{

				Creature effector = _effectorRef.get();
				if (effector == null)
					return;
				_hitCount++;
				if (_hitCount >= _maxHitCount)
					actor.getAbnormalList().stop(getSkill(), false);

			}
		}

		//private AttackListener _listener;
		private int _hitCount = 0;

		public EffectDispelOnAttackedImpl(EffectTemplate template)
		{
			super(template);
		}

		private DamageListener _damageListener;

		@Override
		protected boolean checkCondition(Abnormal abnormal, Creature effector, Creature effected)
		{
			return !effected.isRaid();
		}

		@Override
		public void onStart(Abnormal abnormal, Creature effector, Creature effected)
		{
			_damageListener = new DamageListener(effector, effected);
			effected.addListener(_damageListener);
		}

		@Override
		public boolean onActionTime(Abnormal abnormal, Creature effector, Creature effected)
		{
			if (effected.isDead())
				effected.removeListener(_damageListener);

			return super.onActionTime(abnormal, effector, effected);
		}

		@Override
		public void onExit(Abnormal abnormal, Creature effector, Creature effected)
		{
			effected.removeListener(_damageListener);
		}
	}

	private final int _maxHitCount;

	public EffectDispelOnAttacked(EffectTemplate template)
	{
		super(template);
		_maxHitCount = getTemplate().getParams().getInteger("max_hits", 0);
	}

	@Override
	public EffectHandler getImpl()
	{
		return new EffectDispelOnAttackedImpl(getTemplate());
	}
}