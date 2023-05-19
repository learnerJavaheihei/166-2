package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.network.l2.s2c.ExRaidBossSpawnInfo;

/**
 * @author Bonux
**/
public class RequestRaidBossSpawnInfo extends L2GameClientPacket
{
	@Override
	protected boolean readImpl()
	{
		return true;
	}

	@Override
	protected void runImpl()
	{
		sendPacket(new ExRaidBossSpawnInfo());
	}
}