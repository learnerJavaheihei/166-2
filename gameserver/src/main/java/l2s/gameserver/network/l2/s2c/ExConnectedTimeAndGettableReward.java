package l2s.gameserver.network.l2.s2c;

/**
 * @author Bonux
**/
public class ExConnectedTimeAndGettableReward extends L2GameServerPacket
{
	public static final L2GameServerPacket STATIC = new ExConnectedTimeAndGettableReward();

	@Override
	protected final void writeImpl()
	{
		writeD(0x00);	// TODO[UNDERGROUND]: UNK
		writeD(0x00);	// TODO[UNDERGROUND]: UNK
		writeD(0x00);	// TODO[UNDERGROUND]: UNK
		writeD(0x00);	// TODO[UNDERGROUND]: UNK
		writeD(0x00);	// TODO[UNDERGROUND]: UNK
		writeD(0x00);	// TODO[UNDERGROUND]: UNK
		writeD(0x00);	// TODO[UNDERGROUND]: UNK
		writeD(0x00);	// TODO[UNDERGROUND]: UNK
		writeD(0x00);	// TODO[UNDERGROUND]: UNK
		writeD(0x00);	// TODO[UNDERGROUND]: UNK
		writeD(0x00);	// TODO[UNDERGROUND]: UNK
	}
}