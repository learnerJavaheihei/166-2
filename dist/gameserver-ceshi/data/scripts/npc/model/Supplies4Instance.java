package npc.model;

import l2s.commons.collections.MultiValueSet;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.npc.NpcTemplate;
import manager.FourSepulchersSpawn;

/**
 * @author iqman
 * @reworked by Bonux
 * @四大陵墓寶箱
**/
public class Supplies4Instance extends NpcInstance
{
	public Supplies4Instance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	public void onBypassFeedback(Player player, String command)
	{
		if(command.startsWith("openbox"))
		{
			if(player.getLevel() >= 70)
			{
				if(!FourSepulchersSpawn.rewardSupplies(player))
				{
					showChatWindow(player, "default/" + getNpcId() + "-already_rewarded.htm", false);
					return;
				}

				doDie(player);
			}
			else
			{
				showChatWindow(player, "default/" + getNpcId() + "-no_reward_level.htm", false);
			}
		}
		else
			super.onBypassFeedback(player, command);
	}
}