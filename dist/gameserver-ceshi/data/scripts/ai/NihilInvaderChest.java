package ai;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.DefaultAI;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
//夢幻結界
public class NihilInvaderChest extends DefaultAI
{
	private static int[] _firstLevelItems = {4039, 4040, 4041, 4042, 4043, 4044};
	private static int[] _secondLevelItems = {4045, 4046, 4047};

	public NihilInvaderChest(NpcInstance actor)
	{
		super(actor);
		actor.getFlags().getImmobilized().start();
	}

	@Override
	protected void onEvtAttacked(Creature attacker, Skill skill, int damage)
	{
		NpcInstance actor = getActor();
		if(actor.getNpcId() == 18820)
		{
			if(Rnd.chance(40))
			{
				actor.broadcastPacket(new MagicSkillUse(actor, actor, 2025, 1, 0, 10));
				actor.dropItem(attacker.getPlayer(), _firstLevelItems[Rnd.get(0, _firstLevelItems.length - 1)], Rnd.get(10, 20));
				actor.doDie(null);
			}
		}
		else if(actor.getNpcId() == 18823)
		{
			if(Rnd.chance(40))
			{
				actor.broadcastPacket(new MagicSkillUse(actor, actor, 2025, 1, 0, 10));
				actor.dropItem(attacker.getPlayer(), _secondLevelItems[Rnd.get(0, _secondLevelItems.length - 1)], Rnd.get(10, 20));
				actor.doDie(null);
			}
		}
		for(NpcInstance npc : actor.getReflection().getNpcs())
			if(npc.getNpcId() == actor.getNpcId())
				npc.deleteMe();

		super.onEvtAttacked(attacker, skill, damage);
	}

	@Override
	protected void onEvtAggression(Creature target, int aggro)
	{
	}

}