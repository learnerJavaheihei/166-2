package l2s.gameserver.stats.funcs;

import l2s.gameserver.stats.Env;
import l2s.gameserver.stats.StatModifierType;
import l2s.gameserver.stats.Stats;

/**
 * @author Bonux
 * Хрень временная, чтобы плавно внедрять и переписывать новую систему калькуляции статтов.
**/
public class FuncNew extends Func
{
	private final StatModifierType _modifierType;

	public FuncNew(Stats stat, int order, Object owner, double value, StatModifierType modifierType)
	{
		super(stat, order, owner, value);
		_modifierType = modifierType;
	}

	@Override
	public void calc(Env env)
	{
		switch(getModifierType())
		{
			case DIFF:
			case PER:
				env.value += value;
				break;
		}
	}

	@Override
	public StatModifierType getModifierType()
	{
		return _modifierType;
	}
}
