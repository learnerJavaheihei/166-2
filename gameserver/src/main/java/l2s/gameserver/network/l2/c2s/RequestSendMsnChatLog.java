package l2s.gameserver.network.l2.c2s;

@SuppressWarnings("unused")
public class RequestSendMsnChatLog extends L2GameClientPacket
{
	private int unk3;
	private String unk, unk2;

	/**
	 * format: SSd
	 */
	@Override
	protected boolean readImpl()
	{
		unk = readS();
		unk2 = readS();
		unk3 = readD();
		return true;
	}

	@Override
	protected void runImpl()
	{
		//_log.info.println(getType() + " :: " + unk + " :: " + unk2 + " :: " + unk3);
	}
}