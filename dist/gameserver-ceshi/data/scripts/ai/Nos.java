package ai;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.NpcUtils;

/**
 * @author SanyaDC
 */

public class Nos extends Fighter
{
	public Nos(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		NpcInstance actor = getActor();
		if(Rnd.chance(10))
			{
				spawnNos(actor, 1);
			}
		super.onEvtDead(killer);	
	}
	
	private void spawnNos(NpcInstance actor, int count)
	{
				NpcInstance minion = NpcUtils.spawnSingle(20793, actor.getLoc());
		
	}
}