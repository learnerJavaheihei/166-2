package npc;

import org.apache.commons.lang3.ArrayUtils;

import l2s.commons.util.Rnd;
import l2s.gameserver.listener.hooks.ListenerHook;
import l2s.gameserver.listener.hooks.ListenerHookType;
import l2s.gameserver.listener.script.OnInitScriptListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;

/**
 * @reworked by Bonux
**/
public class TowerOfInsolence extends ListenerHook implements OnInitScriptListener
{
	// Item's
	private static final int ENERGY_OF_INSOLENCE_ITEM_ID = 49685;	// 傲慢氣息
	private static final int UNIDENTIFIED_STONE_ITEM_ID = 49766;	// 可疑的石頭

	private static final int[] ENERGY_OF_INSOLENCE_NPC_IDS = {
		20977,	// 艾爾摩亞丁的貴婦 貪慾奴隸
		21081	// 能天使 貪慾守護者
	};

	private static final int[] UNIDENTIFIED_STONE_NPC_IDS = {
		20823,	// 白金族士兵
		20826,	// 白金族射手
		20827,	// 白金族戰士
		20828,	// 白金族咒術士
		20830,	// 守護天使
		20831,	// 封印天使
		20983,	// 束縛者
		20984,	// 束縛者戰士
		20985,	// 束縛者弓手
		21062,	// 傳令天使
		21064,	// 白金族守護者弓手
		21065,	// 白金族守護者戰士
		21066,	// 白金族守護者咒術士
		21067,	// 守護者大天使
		21069, 	// 白金族守護者軍長
		21072,	// 白金族守護者族長
		21074,	// 束縛者魔法師
		21079	// 法那克的創造物
	};

	private static final int ENERGY_OF_INSOLENCE_MIN_DROP_COUNT = 1;	// TODO: Вынести в конфиг?
	private static final int ENERGY_OF_INSOLENCE_MAX_DROP_COUNT = 2;	// TODO: Вынести в конфиг?

	@Override
	public void onInit()
	{
		for(int npcId : ENERGY_OF_INSOLENCE_NPC_IDS)
			addHookNpc(ListenerHookType.NPC_KILL, npcId);
		for(int npcId : UNIDENTIFIED_STONE_NPC_IDS)
			addHookNpc(ListenerHookType.NPC_KILL, npcId);
	}

	@Override
	public void onNpcKill(NpcInstance npc, Player killer)
	{
		if(ArrayUtils.contains(ENERGY_OF_INSOLENCE_NPC_IDS, npc.getNpcId()))
		{
			npc.dropItem(killer, ENERGY_OF_INSOLENCE_ITEM_ID, Rnd.get(ENERGY_OF_INSOLENCE_MIN_DROP_COUNT, ENERGY_OF_INSOLENCE_MAX_DROP_COUNT));
		}

		if(ArrayUtils.contains(UNIDENTIFIED_STONE_NPC_IDS, npc.getNpcId()))
		{
			if((killer.getLevel() - npc.getLevel()) <= 9 && Rnd.chance(4))
				npc.dropItem(killer, UNIDENTIFIED_STONE_ITEM_ID, 1);
		}
	}
}