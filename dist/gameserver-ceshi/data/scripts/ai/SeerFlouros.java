package ai;

import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.Mystic;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.SimpleSpawner;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.NpcUtils;
import l2s.gameserver.geometry.Location;
//欲界 (深淵之廳)
/**
 * AI Seer Flouros.<br>
 * - Спавнит "миньонов" при атаке.<br>
 * - _hps - таблица процентов hp, после которых спавнит "миньонов".<br>
 * @author n0nam3
 */
public class SeerFlouros extends Mystic
{
	private int _hpCount = 0;
	private static final int MOB = 18560;//普勞羅素的手下 覺醒
	private static final int MOBS_COUNT = 2;
	private static final int[] _hps = { 80, 60, 40, 30, 20, 10, 5, -5,-10 };

	public SeerFlouros(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtAttacked(Creature attacker, Skill skill, int damage)
	{
		NpcInstance actor = getActor();
		if(!actor.isDead())
			if(actor.getCurrentHpPercents() < _hps[Math.min(_hpCount, (_hps.length - 2))])
			{
				spawnMobs(attacker);
				_hpCount++;
				_hpCount = Math.min(_hpCount, 7);
			}
		super.onEvtAttacked(attacker, skill, damage);
	}

	private void spawnMobs(Creature attacker)
	{
		NpcInstance actor = getActor();
		for(int i = 0; i < MOBS_COUNT; i++)
			try
			{
				NpcUtils.spawnSingle(MOB, new Location(actor.getLoc().getX(), actor.getLoc().getY(), actor.getLoc().getZ(), 0), actor.getReflection(), 15 * 60 * 1000);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		_hpCount = 0;
		super.onEvtDead(killer);
	}
}