package instances;

import l2s.commons.util.Rnd;
import l2s.gameserver.listener.actor.OnDeathListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExSendUIEventPacket;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.utils.ReflectionUtils;

/**
 * Класс контролирует высшего дневного Закена
 *
 * @author pchayka
 */

public class ZakenDay83 extends Reflection
{
	private static final int Anchor = 32468;//a, 覺醒
	private static final int UltraDayZaken = 29181;//札肯 覺醒
	private static Location[] zakenTp = {new Location(55272, 219080, -2952), new Location(55272, 219080, -3224), new Location(55272, 219080, -3496),};
	private static Location zakenSpawn = new Location(55048, 216808, -3772);
	private DeathListener _deathListener = new DeathListener();
	private long _savedTime;

	@Override
	protected void onCreate()
	{
		super.onCreate();
		addSpawnWithoutRespawn(Anchor, zakenTp[Rnd.get(zakenTp.length)], 0);
		NpcInstance zaken = addSpawnWithoutRespawn(UltraDayZaken, zakenSpawn, 0);
		zaken.addListener(_deathListener);
		zaken.getFlags().getInvulnerable().start();
		zaken.getFlags().getParalyzed().start();
		_savedTime = System.currentTimeMillis();
	}

	@Override
	public void onPlayerEnter(Player player)
	{
		super.onPlayerEnter(player);
		player.sendPacket(new ExSendUIEventPacket(player, 0, 1, (int) (System.currentTimeMillis() - _savedTime) / 1000, 0, NpcString.ELAPSED_TIME));
	}

	@Override
	public void onPlayerExit(Player player)
	{
		super.onPlayerExit(player);
		player.sendPacket(new ExSendUIEventPacket(player, 1, 1, 0, 0));
	}

	private class DeathListener implements OnDeathListener
	{
		@Override
		public void onDeath(Creature self, Creature killer)
		{
			if(self.isNpc() && self.getNpcId() == UltraDayZaken)
			{
				if(killer.isPlayer())
				{
					setReenterTime(System.currentTimeMillis(),true);

					long _timePassed = System.currentTimeMillis() - _savedTime;
					for(Player p : getPlayers())
					{
						if(_timePassed < 5 * 60 * 1000)
						{
							if(Rnd.chance(50))
								ItemFunctions.addItem(p, 15763, 1, true);
						}
						else if(_timePassed < 10 * 60 * 1000)
						{
							if(Rnd.chance(30))
								ItemFunctions.addItem(p, 15764, 1, true);
						}
						else if(_timePassed < 15 * 60 * 1000)
						{
							if(Rnd.chance(25))
								ItemFunctions.addItem(p, 15763, 1, true);
						}
					}
					for (Player p : getPlayers())
						p.sendPacket(new ExSendUIEventPacket(p, 1, 1, 0, 0));
				}
			}
		}
	}
}