package ai.freya;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.NpcInstance;

/**
 * @author pchayka
 */

public class Glacier extends Fighter
{
	public Glacier(NpcInstance actor)
	{
		super(actor);
		actor.block();
	}

	@Override
	protected void onEvtSpawn()
	{
		super.onEvtSpawn();
		getActor().setNpcState(1);
		ThreadPoolManager.getInstance().schedule(new Freeze(), 800);
		ThreadPoolManager.getInstance().schedule(new Despawn(), 30000L);
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		for(Creature cha : getActor().getAroundCharacters(350, 100))
			if(cha.isPlayer())
				cha.altOnMagicUse(cha, SkillHolder.getInstance().getSkillEntry(6301, 1));

		super.onEvtDead(killer);
	}

	private class Freeze implements Runnable
	{
		@Override
		public void run()
		{
			getActor().setNpcState(2);
		}
	}

	private class Despawn implements Runnable
	{
		@Override
		public void run()
		{
			getActor().deleteMe();
		}
	}
}