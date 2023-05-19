package ai.seedofdestruction;

import l2s.gameserver.network.l2.components.NpcString;
import org.apache.commons.lang3.ArrayUtils;
import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.instancemanager.SoDManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.World;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
//import l2s.gameserver.network.l2.components.SceneMovie;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage.ScreenMessageAlign;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.geometry.Location;

/**
  * @author pchayka
  */
public class Tiat extends Fighter
{
	private static final int TIAT_TRANSFORMATION_SKILL_ID = 5974;
	private static final SkillEntry TIAT_TRANSFORMATION_SKILL = SkillHolder.getInstance().getSkillEntry(TIAT_TRANSFORMATION_SKILL_ID, 1);
	private boolean _notUsedTransform = true;
	private static final int TRAPS_COUNT = 4;
	private static final Location[] TRAP_LOCS = {
			new Location(-252022, 210130, -11995, 16384),
			new Location(-248782, 210130, -11995, 16384),
			new Location(-248782, 206875, -11995, 16384),
			new Location(-252022, 206875, -11995, 16384) };
	private static final long COLLAPSE_BY_INACTIVITY_INTERVAL = 10 * 60 * 1000; // 10 мин
	private long _lastAttackTime = 0;
	private static final int TRAP_NPC_ID = 18696;
	private static final int[] TIAT_MINION_IDS = { 29162, 22538, 22540, 22547, 22542, 22548 };
	private static final String[] TIAT_TEXT = { "我會讓你們後悔挑戰我！", "你們將痛苦地死去！", "我會將你們全部消滅！" };
	private long _lastFactionNotifyTime = 0;
	private boolean _immobilized;
	private boolean _failed = false;

	public Tiat(NpcInstance actor)
	{
		super(actor);
		_immobilized = true;
		actor.getFlags().getImmobilized().start();
	}

	@Override
	protected void onEvtAttacked(Creature attacker, Skill skill, int damage)
	{
		NpcInstance actor = getActor();
		if(actor.isDead())
			return;

		_lastAttackTime = System.currentTimeMillis();

		if(_notUsedTransform && actor.getCurrentHpPercents() < 50)
		{
			if(_immobilized)
			{
				_immobilized = false;
				actor.getFlags().getImmobilized().stop();
			}
			_notUsedTransform = false;
			clearTasks();
			spawnTraps();
			actor.abortAttack(true, false);
			actor.abortCast(true, false);
			// Transform skill cast [custom: making Tiat invul while casting]
			actor.getFlags().getInvulnerable().start();
			actor.doCast(TIAT_TRANSFORMATION_SKILL, actor, true);
			ThreadPoolManager.getInstance().schedule(() ->
			{
				getActor().setCurrentHpMp(getActor().getMaxHp(), getActor().getMaxMp());
				getActor().getFlags().getInvulnerable().stop();
			}, TIAT_TRANSFORMATION_SKILL.getTemplate().getHitTime());
		}
		if(System.currentTimeMillis() - _lastFactionNotifyTime > _minFactionNotifyInterval)
		{
			_lastFactionNotifyTime = System.currentTimeMillis();
			for(NpcInstance npc : World.getAroundNpc(actor))
				if(ArrayUtils.contains(TIAT_MINION_IDS, npc.getNpcId()))
					npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 30000);
			if(Rnd.chance(15) && !_notUsedTransform)
				actor.broadcastPacket(new ExShowScreenMessage(TIAT_TEXT[Rnd.get(TIAT_TEXT.length)], 4000, ScreenMessageAlign.MIDDLE_CENTER, false));
		}
		super.onEvtAttacked(attacker, skill, damage);
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		if(actor.isDead())
			return true;

		// Коллапсируем инстанс, если Тиата не били более 10 мин
		if(!_failed && _lastAttackTime != 0 && _lastAttackTime + COLLAPSE_BY_INACTIVITY_INTERVAL < System.currentTimeMillis())
		{
			final Reflection r = actor.getReflection();
			_failed = true;

			// Показываем финальный ролик при фейле серез секунду после очистки инстанса
			ThreadPoolManager.getInstance().schedule(() ->
			{
				//for(Player pl : r.getPlayers())
					//pl.startScenePlayer(SceneMovie.TIAT_FAIL);
				r.clearReflection(5, true);
			}, 1000);
			return true;
		}
		return super.thinkActive();
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		_notUsedTransform = true;
		_lastAttackTime = 0;
		_lastFactionNotifyTime = 0;

		NpcInstance actor = getActor();
		SoDManager.addTiatKill();
		final Reflection r = actor.getReflection();
		r.setReenterTime(System.currentTimeMillis(),true);
		for(NpcInstance n : r.getNpcs())
			n.deleteMe();
		// Показываем финальный ролик серез секунду после очистки инстанса
		ThreadPoolManager.getInstance().schedule(() ->
		{
			//for(Player pl : r.getPlayers())
				//if(pl != null)
					//pl.startScenePlayer(SceneMovie.TIAT_SUCCESS);
		}, 1000);
	}

	private void spawnTraps()
	{
		NpcInstance actor = getActor();
		actor.broadcastPacket(new ExShowScreenMessage(NpcString.COME_OUT_WARRIORS_PROTECT_SEED_OF_DESTRUCTION, 5000, ScreenMessageAlign.MIDDLE_CENTER, false)); //TODO CHANGE TO NPCSTRING 1800297
		for(int i = 0; i < TRAPS_COUNT; i++)
			actor.getReflection().addSpawnWithRespawn(TRAP_NPC_ID, TRAP_LOCS[i], 0, 180);
	}
}
