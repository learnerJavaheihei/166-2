package handler.dailymissions;

import l2s.gameserver.listener.CharListener;
import l2s.gameserver.listener.actor.OnKillListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.utils.MapUtils;

/**
 * @author Bonux
**/
public class ExploreDevilsIsland extends ProgressDailyMissionHandler
{
	private class HandlerListeners implements OnKillListener
	{
		@Override
		public void onKill(Creature actor, Creature victim)
		{
			Player player = actor.getPlayer();
			if(player != null && victim.isMonster())
			{
				if(MapUtils.regionX(victim) == 21 && MapUtils.regionY(victim) == 24) // Остров Дьявола
					progressMission(player, 1, true);
			}
		}

		@Override
		public boolean ignorePetOrSummon()
		{
			return true;
		}
	}

	private final HandlerListeners _handlerListeners = new HandlerListeners();

	@Override
	public CharListener getListener()
	{
		return _handlerListeners;
	}

	@Override
	public boolean isReusable()
	{
		return false;
	}
}
