package npc;

import org.apache.commons.lang3.ArrayUtils;

import l2s.commons.util.Rnd;
import l2s.gameserver.listener.hooks.ListenerHook;
import l2s.gameserver.listener.hooks.ListenerHookType;
import l2s.gameserver.listener.script.OnInitScriptListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.NpcUtils;

/**
 * @author SanyaDC
 * @reworked by Bonux
**/
public class GiantsCave extends ListenerHook implements OnInitScriptListener
{
	private static final int MONSTER_SPAWN_CHANCE_SOLO = 10;	// 10%
	private static final int MONSTER_SPAWN_CHANCE_PARTY = 30;	// 30%

	private static final int MONSTER_DESPAWN_DELAY_SOLO = 30 * 1000;	// 30 сек
	private static final int MONSTER_DESPAWN_DELAY_PARTY = 10 * 60 * 1000;	// 10 мин, проверить на оффе

	private static final int[] MONSTER_NPC_IDS = {
		20646,	// 哈朗卡
		20647,	// 英葛則
		20648,	// 巴藍伍德
		20649,	// 罕羅特
		20650	// 科蘭洛特
	};

	private static final int SUMMON_MONSTER_NPC_ID_SOLO = 24017;	// 紐克 改造的下巨人
	private static final int SUMMON_MONSTER_NPC_ID_PARTY = 24023;	// 紐克杭特 下巨人刺殺者

	@Override
	public void onInit()
	{
		for(int npcId : MONSTER_NPC_IDS)
			addHookNpc(ListenerHookType.NPC_KILL, npcId);
	}

	@Override
	public void onNpcKill(NpcInstance npc, Player killer)
	{
		if(!ArrayUtils.contains(MONSTER_NPC_IDS, npc.getNpcId()))
			return;

		if(killer.isInParty() && killer.getParty().getPartyMembers().size() >= 5)
		{
			if(Rnd.chance(MONSTER_SPAWN_CHANCE_PARTY))
				NpcUtils.spawnSingle(SUMMON_MONSTER_NPC_ID_PARTY, npc.getLoc(), MONSTER_DESPAWN_DELAY_PARTY);
		}
		else
		{
			if(Rnd.chance(MONSTER_DESPAWN_DELAY_SOLO))
				NpcUtils.spawnSingle(SUMMON_MONSTER_NPC_ID_SOLO, npc.getLoc(), MONSTER_DESPAWN_DELAY_SOLO);
		}
	}
}