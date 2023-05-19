package l2s.gameserver.network.l2.s2c;


public class ExUpgradeSystemNormalResult extends L2GameServerPacket
{
	private final int result;

	public ExUpgradeSystemNormalResult(int result)
	{
		this.result = result;
	}

	protected final void writeImpl()
	{
		writeH(this.result);
	}
}
