package npc.model;

import bosses.AntharasManager;
import l2s.commons.collections.MultiValueSet;
import l2s.commons.util.Rnd;
import l2s.gameserver.instancemanager.SpawnManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.npc.NpcTemplate;

/**
 * @author Bonux
**/
public final class HeartOfWardingInstance extends NpcInstance
{
	public HeartOfWardingInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	

	public void onTeleportRequest(Player talker)
	{
		String htmltext = null;
		long respawnDate = AntharasManager._state.getRespawnDate();

		if(!AntharasManager.isReborned() && System.currentTimeMillis() <=respawnDate)
		{
			SpawnManager.getInstance().despawn("antharas_dungeon_cube");
			talker.teleToLocation(AntharasManager.TELEPORT_POSITION.getX() + Rnd.get(700), AntharasManager.TELEPORT_POSITION.getY() + Rnd.get(2100), AntharasManager.TELEPORT_POSITION.getZ());
			AntharasManager.startAntharasBoss();
		}else {
			showChatWindow(talker, "default/heart_of_warding002.htm", false, (Object) null);
		}

	}

	@Override
	public void showChatWindow(Player player, int val, boolean firstTalk, Object... replace)
	{
		if(val == 0)
			showChatWindow(player, "default/heart_of_warding001.htm", firstTalk, replace);
		else
			super.showChatWindow(player, val, firstTalk, replace);
	}
}