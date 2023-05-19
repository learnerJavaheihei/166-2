package l2s.gameserver.skills.effects.permanent;

import l2s.gameserver.handler.effects.EffectHandler;
import l2s.gameserver.templates.skill.EffectTemplate;
import l2s.gameserver.stats.StatModifierType;
import l2s.gameserver.stats.Stats;
import l2s.gameserver.stats.conditions.Condition;
import l2s.gameserver.stats.funcs.FuncTemplate;

/**
 * @author Bonux
**/
public abstract class p_abstract_stat_effect extends EffectHandler
{
	private final StatModifierType _modifierType;

	public p_abstract_stat_effect(EffectTemplate template, Stats stat)
	{
		super(template);
		_modifierType = getParams().getEnum("type", StatModifierType.class, StatModifierType.DIFF);
		template.attachFunc(new FuncTemplate(template.getCondition(), stat, getValue(), _modifierType));
	}

	protected final StatModifierType getModifierType()
	{
		return _modifierType;
	}

	@Override
	public final Condition getCondition()
	{
		return null;
	}
}