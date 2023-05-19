package ai;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;

/*
 * @author SanyaDC
 */
public class Batur extends Fighter
{
	private static final int TIME_TO_LIVE = 60000;
	private long TIME_TO_DIE = 0;//修復 60秒內打死否則消失

	public Batur(NpcInstance actor)
	{
		super(actor);		
	}
	
	@Override
	protected void onEvtAttacked(Creature attacker, Skill skill, int damage)
	{
		NpcInstance actor = getActor();
		if(TIME_TO_DIE == 0)//修復 60秒內打死否則消失--
		{
			TIME_TO_DIE = System.currentTimeMillis() + TIME_TO_LIVE;
		}//--修復 60秒內打死否則消失
		if(actor != null && System.currentTimeMillis() >= TIME_TO_DIE)
		{
			actor.teleToLocation(-116660, -244676, -15536);//超時時，把怪傳送到別的地方，然後設置60秒後怪死亡，最終再過60秒原地重生一隻新的。
			ThreadPoolManager.getInstance().schedule(() -> {
				actor.doDie(null);
			}, 60 * 1000);//設置60秒後怪死亡
		}
		super.onEvtAttacked(attacker, skill, damage);
	}	
}
