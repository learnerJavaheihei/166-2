package l2s.gameserver.network.l2.c2s;

import org.slf4j.Logger;//20190819--
import org.slf4j.LoggerFactory;//--20190819

import l2s.gameserver.cache.CrestCache;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExSetPledgeEmblemAck;

/**
 * @author Bonux
**/
public class RequestExSetPledgeCrestLargeFirstPart extends L2GameClientPacket
{
	private int _crestPart, _crestLeght, _length;
	private byte[] _data;
	private static final Logger _log = LoggerFactory.getLogger(RequestExSetPledgeCrestLargeFirstPart.class);//20190819

	/**
	 * format: chd(b)
	 */
	@Override
	protected boolean readImpl()
	{
		_crestPart = readD();
		_crestLeght = readD();
		_length = readD();
		if((_length <= CrestCache.LARGE_CREST_PART_SIZE) && (_length == _buf.remaining()))//20190819
		{
			_data = new byte[_length];
			readB(_data);
		}
		return true;
	}

	@Override
	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;

		Clan clan = activeChar.getClan();
		if(clan == null)
			return;

		if((activeChar.getClanPrivileges() & Clan.CP_CL_EDIT_CREST) == Clan.CP_CL_EDIT_CREST)
		{
			if(clan.isPlacedForDisband())
			{
				activeChar.sendPacket(SystemMsg.AS_YOU_ARE_CURRENTLY_SCHEDULE_FOR_CLAN_DISSOLUTION_YOU_CANNOT_REGISTER_OR_DELETE_A_CLAN_CREST);
				return;
			}

			int crestId = 0;
			if(_data != null)
			{
				crestId = CrestCache.getInstance().savePledgeCrestLarge(clan.getClanId(), _crestPart, _crestLeght, _data);
				//20190819--
				if(crestId == 0)
				{
					_log.warn("读取血盟标章错误:" + activeChar.getName() + " 盟:" + clan.getClanId() + " 文件大小:" + _data.length + " _crestPart:" + _crestPart + " _crestLeght:" + _crestLeght);
				}
				//--20190819
				if(crestId > 0)
				{
					activeChar.sendPacket(SystemMsg.THE_CLAN_CREST_WAS_SUCCESSFULLY_REGISTERED);
					clan.setCrestLargeId(crestId);
					clan.broadcastClanStatus(false, true, false);
				}
				activeChar.sendPacket(new ExSetPledgeEmblemAck(_crestPart));
			}
			else if(clan.hasCrestLarge())
			{
				CrestCache.getInstance().removePledgeCrestLarge(clan.getClanId());
				clan.setCrestLargeId(crestId);
				clan.broadcastClanStatus(false, true, false);
			}
		}
	}
}