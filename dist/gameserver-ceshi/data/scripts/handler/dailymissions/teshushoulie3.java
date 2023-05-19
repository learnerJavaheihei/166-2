package handler.dailymissions;

import l2s.gameserver.listener.CharListener;
import l2s.gameserver.listener.actor.OnKillListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.utils.MapUtils;
import l2s.gameserver.model.Party;

/**
 * @author Bonux
**/
public class teshushoulie3 extends ProgressDailyMissionHandler
{
	private class HandlerListeners implements OnKillListener
	{
		@Override
		public void onKill(Creature actor, Creature victim)
		{
			Player player = actor.getPlayer();
			if (player.isInParty())
			{
				Player playerLeader = player.getParty().getPartyLeader();
				for(Player p : player.getParty())
				{
					if(p.getDistance(player) <= 1500)
					{
						if(player != null && victim.isMonster())
						{
							if(p == playerLeader)
							{
								if(MapUtils.regionX(victim) == 20 && MapUtils.regionY(victim) == 21)
								progressMission(p, 1, true);
							}
							else
							{
								if(MapUtils.regionX(victim) == 20 && MapUtils.regionY(victim) == 21)
								progressMission(p, 1, true);
							}
						}
					}
				}
			}
			else
			{
				if(player != null && victim.isMonster())
				{
					if(MapUtils.regionX(victim) == 20 && MapUtils.regionY(victim) == 21) // Забытый Остров
						progressMission(player, 1, true);
				}
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
}
