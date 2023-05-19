package ai;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import l2s.commons.util.Rnd;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.ai.DefaultAI;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.EarthQuakePacket;
import l2s.gameserver.network.l2.s2c.PlaySoundPacket;
import l2s.gameserver.network.l2.s2c.SocialActionPacket;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;

import static l2s.gameserver.ai.CtrlIntention.AI_INTENTION_ACTIVE;

/**
 * AI боса Байума.<br>
 * - Мгновенно убивает первого ударившего<br>
 * - Для атаки использует только скилы по следующей схеме:
 * <li>Стандартный набор: 80% - 4127, 10% - 4128, 10% - 4129
 * <li>если хп < 50%: 70% - 4127, 10% - 4128, 10% - 4129, 10% - 4131
 * <li>если хп < 25%: 60% - 4127, 10% - 4128, 10% - 4129, 10% - 4131, 10% - 4130
 *
 * @author SYS
 * @reworked by Bonux
 */
public abstract class AbstractZakenAI extends DefaultAI
{

	private AtomicBoolean firstTimeAttacked = new AtomicBoolean(true);

	public AbstractZakenAI(NpcInstance actor)
	{
		super(actor);
	}

	@Override
	public boolean isGlobalAI()
	{
		return true;
	}

	@Override
	protected void onEvtSpawn() {
		super.onEvtSpawn();
		NpcInstance npc = getActor();
		npc.getFlags().getImmobilized().start();
		npc.broadcastPacket(new PlaySoundPacket(PlaySoundPacket.Type.MUSIC, "BS02_A", 1, 0, npc.getLoc()));
		npc.broadcastPacket(new SocialActionPacket(npc.getObjectId(), 2));
	}

	@Override
	protected void onEvtTimer(int timerId, Object arg1, Object arg2) {
		super.onEvtTimer(timerId, arg1, arg2);

		NpcInstance actor = getActor();
	}

	@Override
	protected boolean createNewTask()
	{
		clearTasks();
		Creature target;
		if((target = prepareTarget()) == null)
		{
			if(getIntention() != CtrlIntention.AI_INTENTION_ACTIVE)
			{
				changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
				//return maybeMoveToHome(true);
			}
			return false;
		}

		return true;
	}
	@Override
	protected void onEvtDead(Creature killer)
	{
		firstTimeAttacked.set(true);

		NpcInstance actor = getActor();
		actor.broadcastPacket(new PlaySoundPacket(PlaySoundPacket.Type.MUSIC, "BS02_D", 1, 0, actor.getLoc()));
		super.onEvtDead(killer);
	}


}