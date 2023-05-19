package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;//修復最大連線
import l2s.gameserver.model.GameObjectsStorage;//修復最大連線
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.GameClient.GameClientState;
import l2s.gameserver.network.l2.components.HtmlMessage;//修復最大連線
import l2s.gameserver.network.l2.s2c.ActionFailPacket;
import l2s.gameserver.network.l2.s2c.CharacterSelectedPacket;
import l2s.gameserver.network.l2.s2c.ExNeedToChangeName;
import l2s.gameserver.utils.AutoBan;

import org.apache.commons.lang3.StringUtils;

public class CharacterSelected extends L2GameClientPacket
{
	private int _charSlot;

	/**
	 * Format: cdhddd
	 */
	@Override
	protected boolean readImpl()
	{
		_charSlot = readD();
		return true;
	}

	@Override
	protected void runImpl()
	{
		GameClient client = getClient();

		if(client.getActiveChar() != null)
			return;

		if(!client.secondaryAuthed())
		{
			sendPacket(ActionFailPacket.STATIC);
			return;
		}

		int objId = client.getObjectIdForSlot(_charSlot);
		if(AutoBan.isBanned(objId))
		{
			sendPacket(ActionFailPacket.STATIC);
			return;
		}

		Player activeChar = client.loadCharFromDisk(_charSlot);
		if(activeChar == null)
		{
			sendPacket(ActionFailPacket.STATIC);
			return;
		}
		//修復最大連線--
		int count =0;
		if (Config.DUALBOX_CHECK_MAX_PLAYERS_PER_IP > 0)
		{
			for(Player player : GameObjectsStorage.getPlayers(false, false))
			{
				if(player.getSuperIP().equals(activeChar.getSuperIP()))
				{
					count++;
				}
			}
			if(count > Config.DUALBOX_CHECK_MAX_PLAYERS_PER_IP)
			{
				HtmlMessage msg = new HtmlMessage(5); //
				msg.setFile("IPRestriction.htm");
				msg.replace("%max%", String.valueOf(Config.DUALBOX_CHECK_MAX_PLAYERS_PER_IP));
				activeChar.sendPacket(msg);
				sendPacket(ActionFailPacket.STATIC);
				return;
			}
		}
		//--修復最大連線

		if(activeChar.getAccessLevel() < 0)
			activeChar.setAccessLevel(0);

		client.setState(GameClientState.IN_GAME);
		activeChar.setOnlineStatus(true); // Заглушка для МА, TODO: Перевести на другой МА или МА перевести на xml-rpc.

		String changedOldName = activeChar.getVar(Player.CHANGED_OLD_NAME);
		if(changedOldName != null && !StringUtils.isEmpty(changedOldName))
		{
			sendPacket(new ExNeedToChangeName(ExNeedToChangeName.TYPE_PLAYER, ExNeedToChangeName.NONE_REASON, changedOldName));
			return;
		}
		sendPacket(new CharacterSelectedPacket(activeChar, client.getSessionKey().playOkID1));
	}
}