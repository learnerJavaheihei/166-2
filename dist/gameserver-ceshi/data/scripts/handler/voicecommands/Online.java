package handler.voicecommands;

import l2s.gameserver.Config;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.tables.FakePlayersTable;

public class Online extends ScriptVoiceCommandHandler
{
	private final String[] COMMANDS = new String[] { "online" };

	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String target)
	{
		if(!Config.ALLOW_TOTAL_ONLINE)
			return false;

		if(command.equals("online"))
		{
			int i = GameObjectsStorage.getPlayers(true, true).size() + FakePlayersTable.getActiveFakePlayersCount();
			int j = GameObjectsStorage.getOfflinePlayers().size();
			if(activeChar.isLangRus())
			{
				activeChar.sendMessage("目前「 "+i+" 」玩家在線。");
				activeChar.sendMessage("其中「 "+j+" 」處於離線交易模式。");
			}	
			else
			{
				activeChar.sendMessage("目前「 "+i+" 」玩家在线。");
				activeChar.sendMessage("其中「 "+j+" 」处于离线交易模式。");			
			}
			return true;
		}
		return false;
	}

	@Override
	public String[] getVoicedCommandList()
	{
		return COMMANDS;
	}
}
