package ai;

import java.util.concurrent.atomic.AtomicBoolean;

import l2s.commons.util.Rnd;
import l2s.gameserver.Announcements;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.World;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.utils.NpcUtils;

/**
 * @author Bonux (bonuxq@gmail.com)
 * 09.02.2019
 * Developed for L2-Scripts.com
 **/
public class BalthusKnightsZakenAI extends Fighter {

    private static final int RASH_NPC_ID = 31716;	// Лаш - Адъютант Поддержки

	private AtomicBoolean firstTimeAttacked = new AtomicBoolean(true);
	private static final SkillEntry S_ZAKEN_TRANS_NIGHT2DAY = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4223, 1);
	private static final SkillEntry S_ZAKEN_TRANS_DAY2NIGHT = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4224, 1);

	private static final SkillEntry S_ZAKEN_REGEN1 = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4227, 1);
	private static final SkillEntry S_ZAKEN_REGEN2 = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4242, 1);

	private static final SkillEntry S_ZAKEN_RANGE_DRAIN_DAY = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 50006, 1);
	private static final SkillEntry S_ZAKEN_RANGE_DRAIN_NIGHT = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 50006, 2);
	private static final SkillEntry S_ZAKEN_RANGE_DUAL_ATTACK_DAY = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 50007, 1);
	private static final SkillEntry S_ZAKEN_RANGE_DUAL_ATTACK_NIGHT = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 50007, 2);
	private static final SkillEntry S_ZAKEN_KNOCKDOWN_DAY = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 50008, 1);
	private static final SkillEntry S_ZAKEN_KNOCKDOWN_NIGHT = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 50008, 2);
	
    public BalthusKnightsZakenAI(NpcInstance actor) {
        super(actor);
    }

    @Override
    protected void onEvtDead(Creature killer) {
        NpcInstance actor = getActor();
        Reflection reflection = actor.getReflection();
        NpcUtils.spawnSingle(RASH_NPC_ID, actor.getLoc(), reflection);
        reflection.setReenterTime(System.currentTimeMillis(), false);
        reflection.startCollapseTimer(5, true);
        super.onEvtDead(killer);
    }
    
	@Override
	protected void onEvtAttacked(Creature creature, Skill skill, int damage)
	{
		if (!creature.isPlayer() && !creature.isSummon())
			return;
		if (getActor().getLifeTime() > 0 && getActor().inMyTerritory(getActor()))
		{
			addAttackDesire(creature, 1, 200);
		}

		if (Rnd.get(15) < 1)
		{
			int skillRandom = Rnd.get(15 * 15);
			if (skillRandom < 2)
			{
				addUseSkillDesire(getActor(), S_ZAKEN_REGEN1, 0, 1, 1000000);
			}
			else if (skillRandom < 4)
			{
				addUseSkillDesire(getActor(), S_ZAKEN_KNOCKDOWN_DAY, 0, 1, 1000000);
			}
			else if (skillRandom < 8)
			{
				addUseSkillDesire(getActor(), S_ZAKEN_RANGE_DRAIN_DAY, 0, 1, 1000000);
				showMessage(NpcString.YOUR_BLOOD_WILL_BE_MY_FLESH, 10000);
			}
			else if (skillRandom < 15)
			{
				Creature topDesireTarget = getActor().getAggroList().getMostHated(-1);
				if (creature != topDesireTarget && getActor().getDistance(creature) < 100)
				{
					addUseSkillDesire(getActor(), S_ZAKEN_RANGE_DUAL_ATTACK_DAY, 0, 1, 1000000);
					showMessage(NpcString.LOSERS_YOU_FALL_UNDER_MY_SWORD, 10000);
				}
			}
		}
		getActor().getAggroList().addDamageHate(creature, 0, damage);
	}
	private void showMessage(NpcString npcString, int radius)
	{
		NpcInstance actor = getActor();

		ExShowScreenMessage msg = new ExShowScreenMessage(npcString, 10000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, true, true);
		for(Player player : World.getAroundPlayers(actor, radius))
			player.sendPacket(msg);
	}
}
