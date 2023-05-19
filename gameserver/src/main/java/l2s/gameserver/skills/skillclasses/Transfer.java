package l2s.gameserver.skills.skillclasses;


import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.templates.StatsSet;

public class Transfer extends Skill
{

	NpcInstance npcs;

	public Transfer(StatsSet set)
	{
		super(set);
	}

	@Override
	public boolean checkCondition(SkillEntry skillEntry, Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first, boolean sendMsg, boolean trigger)
	{
		if(activeChar.isPlayer())
		{
			int vars = ((Player) activeChar).getVarInt("InstanceDoor");
			if(vars > 0)
			{
				npcs = GameObjectsStorage.getNpc(vars);
				if(npcs != null)
				{
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected void useSkill(Creature activeChar, Creature target, boolean reflected)
	{
		if(!target.isPlayer())
			return;
		if(npcs != null)
		{
			target.teleToLocation(npcs.getX(), npcs.getY(), npcs.getZ());
		}
	}
}