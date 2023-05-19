package ai;

import l2s.gameserver.ai.DefaultAI;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.NpcInstance;

public class ZakenAnchor extends DefaultAI
{
	private static final int DayZaken = 29176;//札肯 覺醒
	private static final int UltraDayZaken = 29181;//札肯 覺醒
	private static final int Candle = 32705;//木桶 覺醒
	private int i = 0;

	public ZakenAnchor(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		for(NpcInstance npc : actor.getAroundNpc(1000, 100))
			if(npc.getNpcId() == Candle && npc.getNpcState() == 3)
				i++;

		if(i >= 4)
		{
			if(actor.getReflection().getInstancedZoneId() == 133)//即時地區ID
			{
				actor.getReflection().addSpawnWithoutRespawn(DayZaken, actor.getLoc(), 0);
				for(int i = 0; i < 4; i++)
				{
					actor.getReflection().addSpawnWithoutRespawn(20845, actor.getLoc(), 200);
					actor.getReflection().addSpawnWithoutRespawn(20847, actor.getLoc(), 200);
				}
				actor.deleteMe();
				return true;
			}
			else if(actor.getReflection().getInstancedZoneId() == 135)//即時地區ID
			{
				for(NpcInstance npc : actor.getReflection().getNpcs())
					if(npc.getNpcId() == UltraDayZaken)
					{
						npc.getFlags().getInvulnerable().stop();
						npc.getFlags().getParalyzed().stop();
						npc.teleToLocation(actor.getLoc());
					}
				for(int i = 0; i < 4; i++)
				{
					actor.getReflection().addSpawnWithoutRespawn(29184, actor.getLoc(), 300);
					actor.getReflection().addSpawnWithoutRespawn(29183, actor.getLoc(), 300);
				}
				actor.deleteMe();
				return true;
			}
		}
		else
			i = 0;

		return false;
	}
}