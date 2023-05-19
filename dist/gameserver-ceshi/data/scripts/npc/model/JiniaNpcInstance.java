package npc.model;

import instances.FreyaHard;
import instances.FreyaNormal;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ReflectionUtils;

/**
 * 芙蕾雅
 */

public final class JiniaNpcInstance extends NpcInstance
{
	private static final long serialVersionUID = 1L;

	private static final int normalFreyaIzId = 139;
	private static final int extremeFreyaIzId = 144;

	public JiniaNpcInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if(command.equalsIgnoreCase("request_normalfreya"))
		{
			Reflection r = player.getActiveReflection();
			if(r != null)
			{
				if(player.canReenterInstance(normalFreyaIzId))
					player.teleToLocation(r.getTeleportLoc(), r);
			}
			else if(player.canEnterInstance(normalFreyaIzId))
			{
				ReflectionUtils.enterReflection(player, new FreyaNormal(), normalFreyaIzId);
			}
		}
		else if(command.equalsIgnoreCase("request_extremefreya"))
		{
			Reflection r = player.getActiveReflection();
			if(r != null)
			{
				if(player.canReenterInstance(extremeFreyaIzId))
					player.teleToLocation(r.getTeleportLoc(), r);
			}
			else if(player.canEnterInstance(extremeFreyaIzId))
			{
				ReflectionUtils.enterReflection(player, new FreyaHard(), extremeFreyaIzId);
			}
		}
		else
			super.onBypassFeedback(player, command);
	}
}