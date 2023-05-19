package instances;

import java.util.concurrent.TimeUnit;

import l2s.commons.threading.RunnableWrapper;
import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.utils.ReflectionUtils;

/**
 * Класс контролирует ночного Закена
 *
 * @author pchayka
 */

public class ZakenNight extends Reflection
{
	private static final int Zaken = 29022;//札肯
	private static final long initdelay = 480 * 1000L;		// 480
	private Location[] zakenspawn = {new Location(55272, 219080, -2952), new Location(55272, 219080, -3224), new Location(55272, 219080, -3496),};

	@Override
	protected void onCreate()
	{
		super.onCreate();
		ThreadPoolManager.getInstance().schedule( new ZakenSpawn(this), initdelay + Rnd.get(120, 240) * 1000L);
	}

	public class ZakenSpawn implements Runnable
	{
		Reflection _r;

		public ZakenSpawn(Reflection r)
		{
			_r = r;
		}

		@Override
		public void run()
		{

			Location rndLoc = zakenspawn[Rnd.get(zakenspawn.length)];
			_r.addSpawnWithoutRespawn(Zaken, rndLoc, 0);
			for(int i = 0; i < 4; i++)
			{
				_r.addSpawnWithoutRespawn(29026, rndLoc, 200);
				_r.addSpawnWithoutRespawn(29024, rndLoc, 200);
			}
		}
	}
}