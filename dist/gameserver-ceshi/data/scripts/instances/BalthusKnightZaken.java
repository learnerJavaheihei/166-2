package instances;

import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.impl.HashIntSet;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Bonux
 */
public class BalthusKnightZaken extends Reflection
{
	private final int Zaken_RAID_NPC_ID = 29119;	// Баюм


	private final AtomicBoolean ZakenRaidSpawned = new AtomicBoolean(false);
	private final IntSet rewardedPlayers = new HashIntSet();

	//55256, 219096, -3232
	private static Location zakenSpawn = new Location(55256, 219096, -3232);
	
	@Override
	protected void onCreate()
	{
		super.onCreate();
		NpcInstance zaken = addSpawnWithoutRespawn(Zaken_RAID_NPC_ID, zakenSpawn, 0);
	}
	

	public boolean isRewardReceived(Player player)
	{
		return rewardedPlayers.contains(player.getObjectId());
	}

	public void setRewardReceived(Player player)
	{
		rewardedPlayers.add(player.getObjectId());
	}
}