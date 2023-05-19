package l2s.gameserver.stats.conditions;//新增 鬥力力量需求

import l2s.gameserver.model.Player;
import l2s.gameserver.stats.Env;

public class ConditionPlayerCharges extends Condition
{
	private int _count;

	public ConditionPlayerCharges(int count)
	{
		_count = count;
	}


	@Override
	protected boolean testImpl(Env env)
	{
		return ((Player) env.character).getIncreasedForce() >=_count ;
	}
}