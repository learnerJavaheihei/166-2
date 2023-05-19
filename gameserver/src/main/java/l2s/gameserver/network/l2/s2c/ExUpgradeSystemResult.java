package l2s.gameserver.network.l2.s2c;

public class ExUpgradeSystemResult extends L2GameServerPacket
{
	private final int result;

	public ExUpgradeSystemResult(int result)
	{
		this.result = result;
	}

	protected final void writeImpl()
	{
		writeH(this.result);
	}
}
