package bosses;

import l2s.commons.time.cron.SchedulingPattern;
import l2s.gameserver.Announcements;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.instancemanager.ServerVariables;
import l2s.gameserver.instancemanager.SpawnManager;
import l2s.gameserver.listener.actor.OnDeathListener;
import l2s.gameserver.listener.actor.player.OnTeleportedListener;
import l2s.gameserver.listener.zone.OnZoneEnterLeaveListener;
import l2s.gameserver.model.*;
import l2s.gameserver.model.actor.listener.CharListenerList;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.SceneMovie;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

/**
 * @author Bonux
 * TODO: Написано все наугад. По возможности переписать под офф.
**/
public abstract class SevenSignsRaidManager
{
	private static enum DungeonStatus
	{
		NONE,
		DESTROY_MONSTERS,	// Стадия убийства монстров в подземелье.
		SEAL_REMNANTS_DESTROY,	// Стадия убийства двух Следов Печати.
		ENTER_TO_RAID,	// Стадия телепортации к рейду.
		RAID_DESTROY,	// Стадия убийства рейда.
		RAID_FINISHED;	// Стадия окончания рейда.

		public static final DungeonStatus[] VALUES = values();
	}

	private class DungeonZoneListener implements OnZoneEnterLeaveListener
	{
		@Override
		public void onZoneEnter(Zone zone, Creature cha)
		{
//			if(cha.isPlayer())
//			{
//			}
//			else if(cha.isMonster())
//				cha.addListener(_deathListener);
		}

		@Override
		public void onZoneLeave(Zone zone, Creature cha)
		{
			//
		}
	}

//	private class DeathListener implements OnDeathListener
//	{
//		@Override
//		public void onDeath(Creature self, Creature killer)
//		{
//			if (self.getNpcId() == 25286) {//亞納死亡
//				Announcements.announceToAll("亞納死亡");
//				GameObject obj = GameObjectsStorage.getNpc( 25283);//取出莉莉絲是否存在
//				if(obj != null && obj.isNpc())
//				{
//					NpcInstance target = (NpcInstance) obj;
//					//下面編號是自己建的一個無敵技能 類設置一小時之類
//					target.forceUseSkill(SkillEntry.makeSkillEntry(SkillEntryType.NONE, 5744, 1), target);
//				}
//
//			} else if (self.getNpcId() == 25283) {//莉莉絲死亡
//				Announcements.announceToAll("莉莉絲死亡");
//				GameObject obj = GameObjectsStorage.getNpc( 25286);//取出亞納是否存在
//				if(obj != null && obj.isNpc())
//				{
//					NpcInstance target = (NpcInstance) obj;
//					//下面編號是自己建的一個無敵技能 類設置一小時之類
//					target.forceUseSkill(SkillEntry.makeSkillEntry(SkillEntryType.NONE, 5744, 1), target);
//				}
//			}
//			if(self.isNpc())
//			{
//				if(_status == DungeonStatus.DESTROY_MONSTERS)
//				{
//					if(!ZoneUtils.checkAliveMonstersInZone(_dungeonZone))
//					{
//						setStatus(DungeonStatus.SEAL_REMNANTS_DESTROY);
//						checkStatus();
//					}
//				}
//				else if(self.getNpcId() == SEAL_REMNANT)
//				{
//					if(_status == DungeonStatus.SEAL_REMNANTS_DESTROY)
//					{
//						if(!ZoneUtils.checkAliveMonstersInZone(_dungeonZone, SEAL_REMNANT))
//						{
//							_zigguratNpc = NpcUtils.spawnSingle(getEnterGatekeeperId(), self.getLoc());
//							setStatus(DungeonStatus.ENTER_TO_RAID);
//							checkStatus();
//						}
//					}
//				}
//				else if(self.getNpcId() == getRaidId())
//				{
//					if(_status == DungeonStatus.RAID_DESTROY)
//					{
//						setState(EpicBossState.State.DEAD);
//						setStatus(DungeonStatus.RAID_FINISHED);
//						checkStatus();
//					}
//				}
//			}
//		}
//	}
	private class DeathListener implements OnDeathListener
	{
		@Override
		public void onDeath(Creature self, Creature killer)
		{
			if(self.isNpc())
			{
				if (self.getNpcId() == _state.getBossId()) {
					_state.setNextRespawnDate(getReusePattern().next(System.currentTimeMillis()));
					_state.setState(EpicBossState.State.DEAD);
					_state.save();
					checkBossState();
					return;
				}
			}

			if (self.getNpcId() == 25286) {//亞納死亡
				Announcements.announceToAll("亞納死亡");
				GameObject obj = GameObjectsStorage.getNpc( 25283);//取出莉莉絲是否存在
				if(obj != null && obj.isNpc())
				{
					NpcInstance target = (NpcInstance) obj;
					//下面編號是自己建的一個無敵技能 類設置一小時之類
					target.forceUseSkill(SkillEntry.makeSkillEntry(SkillEntryType.NONE, 5744, 1), target);
				}

			} else if (self.getNpcId() == 25283) {//莉莉絲死亡
				Announcements.announceToAll("莉莉絲死亡");
				GameObject obj = GameObjectsStorage.getNpc( 25286);//取出亞納是否存在
				if(obj != null && obj.isNpc())
				{
					NpcInstance target = (NpcInstance) obj;
					//下面編號是自己建的一個無敵技能 類設置一小時之類
					target.forceUseSkill(SkillEntry.makeSkillEntry(SkillEntryType.NONE, 5744, 1), target);
				}
			}
		}
	}
	private class TeleportedListener implements OnTeleportedListener
	{
		@Override
		public void onTeleported(Player player)
		{
			player.removeListener(_teleportedListener);
			if(_status == DungeonStatus.ENTER_TO_RAID)
				player.startScenePlayer(getRaidEnterSceneMovie());
		}
	}

	// Monster's
	private static final int SEAL_REMNANT = 19490;	//	След Печати

	private final OnZoneEnterLeaveListener _zoneListener = new DungeonZoneListener();
	private final OnDeathListener _deathListener = new DeathListener();
	private final OnTeleportedListener _teleportedListener = new TeleportedListener();
	private static Map<String,Long> boosSpawnedTime = new HashMap<>();
	private final Long spawnInterval = 7 * 24 * 60 * 60 * 1000L;
	private EpicBossState _state;
	private DungeonStatus _status = DungeonStatus.NONE;
	private Zone _dungeonZone;
	private Zone _raidZone;
	private NpcInstance _zigguratNpc = null;
	private ScheduledFuture<?> _processStatusTask = null;

	public void onInit()
	{
		_state = new EpicBossState(getRaidId());

		CharListenerList.addGlobal(_deathListener);
		checkBossState();
	}
	private void doTaskSpawn() {
		if (_processStatusTask !=null) {
			_processStatusTask = null;
		}
		_processStatusTask = ThreadPoolManager.getInstance().schedule(() ->
		{
			SpawnManager.getInstance().despawn(getRaidSpawnGroup());
			SpawnManager.getInstance().spawn(getRaidSpawnGroup());
			_state.setNextRespawnDate(getReusePattern().next(System.currentTimeMillis()));
			_state.setState(EpicBossState.State.ALIVE);
			_state.save();
			// 记录 boos 刷新时间
			boosSpawnedTime.put(getRaidSpawnGroup(),System.currentTimeMillis());
		}, getReusePattern().next(System.currentTimeMillis())- System.currentTimeMillis());
	}

	// 更改刷新 不要房间的状态 直接判断 boss
	public  void checkBossState() {

		if (_state.getState() == EpicBossState.State.NOTSPAWN) {
			long reuseDate = _state.getRespawnDate();
			// 系统时间大于 刷新时间 而且 boss 没刷新 就是系统有问题 或者是 第一次 数据库里面没有数据的时候   boss 把 boos 刷出来
			if(System.currentTimeMillis() >= reuseDate)
			{
				SpawnManager.getInstance().despawn(getRaidSpawnGroup());
				SpawnManager.getInstance().spawn(getRaidSpawnGroup());
				boosSpawnedTime.put(getRaidSpawnGroup(),System.currentTimeMillis());
				_state.setNextRespawnDate(getReusePattern().next(System.currentTimeMillis()));
				_state.setState(EpicBossState.State.ALIVE);
				_state.save();
			}else {
				// 系统小于 刷新时间 还没到刷新时间 不管
				doTaskSpawn();
			}
		}else if(_state.getState() == EpicBossState.State.ALIVE){
			SpawnManager.getInstance().despawn(getRaidSpawnGroup());
			SpawnManager.getInstance().spawn(getRaidSpawnGroup());

		}else if(_state.getState() == EpicBossState.State.DEAD){
			// 重新加载下 BOOS 状态
			_state.load();

			doTaskSpawn();
			long reuseDate = _state.getRespawnDate();
			// 这种情况是 可能是由于系统启动 过了刷新时间 而boss是死亡状态没有刷新 ，要补刷
			Long aLong = boosSpawnedTime.get(getRaidSpawnGroup());
			if(System.currentTimeMillis() >= reuseDate && ((aLong !=null && aLong > 0) ?((System.currentTimeMillis() - aLong) >= spawnInterval):true) )
			{
				SpawnManager.getInstance().despawn(getRaidSpawnGroup());
				SpawnManager.getInstance().spawn(getRaidSpawnGroup());
				_state.setNextRespawnDate(getReusePattern().next(System.currentTimeMillis()));
				_state.setState(EpicBossState.State.ALIVE);
				_state.save();
				boosSpawnedTime.put(getRaidSpawnGroup(),System.currentTimeMillis());

			}else {
				// 隔 100 秒将死亡状态更改为  未刷新状态
				ThreadPoolManager.getInstance().schedule(()->{
					if (_state.getState() == EpicBossState.State.DEAD) {
						_state.setState(EpicBossState.State.NOTSPAWN);
						_state.save();
					}
				},100*1000L);
			}
		}else {
			// 如果是非上面3种情况 启动定时任务去检查
			_state.setState(EpicBossState.State.NOTSPAWN);
			_state.save();
			checkBossState();
		}
	}
	public void enterBossZone(Player player){
		player.teleToLocation(Location.findPointToStay(getEnterLocation(), 80, player.getGeoIndex()));
	}

	private void checkStatus()
	{
		if(_state.getState() == EpicBossState.State.INTERVAL)
		{
			long reuseDate = getReusePattern().next(_state.getRespawnDate());
			if(System.currentTimeMillis() > reuseDate)
			{
				setState(EpicBossState.State.NOTSPAWN);
				setStatus(DungeonStatus.DESTROY_MONSTERS);
			}
			else if(_status != DungeonStatus.NONE)
				setStatus(DungeonStatus.NONE);

			if(_status == DungeonStatus.NONE)
			{
				_processStatusTask = ThreadPoolManager.getInstance().schedule(() ->
				{
					setState(EpicBossState.State.NOTSPAWN);
					setStatus(DungeonStatus.DESTROY_MONSTERS);
					checkStatus();
				}, reuseDate - System.currentTimeMillis());
			}
		}

		if(_state.getState() == EpicBossState.State.NOTSPAWN)
		{
			if(_status != DungeonStatus.DESTROY_MONSTERS && _status != DungeonStatus.SEAL_REMNANTS_DESTROY && _status != DungeonStatus.ENTER_TO_RAID)
				setStatus(DungeonStatus.DESTROY_MONSTERS);
		}
		else if(_state.getState() == EpicBossState.State.ALIVE)
		{
			if(_status != DungeonStatus.RAID_DESTROY)
				setStatus(DungeonStatus.RAID_DESTROY);
		}
		else if(_state.getState() == EpicBossState.State.DEAD)
		{
			if(_status != DungeonStatus.RAID_FINISHED)
				setStatus(DungeonStatus.RAID_FINISHED);
		}

		switch(_status)
		{
			case NONE:
				SpawnManager.getInstance().despawn(getRaidZigguratSpawnGroup());
				break;
			case DESTROY_MONSTERS:
				SpawnManager.getInstance().spawn(getDungeonMonstersSpawnGroup());
				break;
			case SEAL_REMNANTS_DESTROY:
				SpawnManager.getInstance().despawn(getDungeonMonstersSpawnGroup());
				SpawnManager.getInstance().spawn(getSealRemnantsSpawnGroup());
				break;
			case ENTER_TO_RAID:
				SpawnManager.getInstance().despawn(getSealRemnantsSpawnGroup());
				break;
			case RAID_DESTROY:
				SpawnManager.getInstance().spawn(getRaidSpawnGroup());
				break;
			case RAID_FINISHED:
			{
				SpawnManager.getInstance().despawn(getRaidSpawnGroup());
				SpawnManager.getInstance().spawn(getRaidZigguratSpawnGroup());

				if(_zigguratNpc != null)
					_zigguratNpc.deleteMe();

				_processStatusTask = ThreadPoolManager.getInstance().schedule(() ->
				{
					setState(EpicBossState.State.INTERVAL);
					setStatus(DungeonStatus.NONE);
					checkStatus();
				}, 3600000L);
				break;
			}
		}
	}

	private void setState(EpicBossState.State value)
	{
		if(value == EpicBossState.State.ALIVE)
			_state.setNextRespawnDate(getReusePattern().next(System.currentTimeMillis()));
		_state.setState(value);
		_state.save();
	}

	private void setStatus(DungeonStatus value)
	{
		_status = value;
		ServerVariables.set(getDungeonStatusVar(), _status.ordinal());
	}

	public int tryEnterToDungeon(Player player)
	{
		if(_status == DungeonStatus.ENTER_TO_RAID)
		{
			if(getMinMembersCount() > Party.MAX_SIZE)
			{
				if(player.getParty() == null)
					return 4;

				if(player.getParty().getCommandChannel() == null)
					return 5;

				if(!player.getParty().getCommandChannel().isLeaderCommandChannel(player))
					return 6;

				int channelMemberCount = player.getParty().getCommandChannel().getMemberCount();
				if(channelMemberCount > getMaxMembersCount() || channelMemberCount < getMinMembersCount())
					return 7;

				for(Player p : player.getParty().getCommandChannel().getMembers())
				{
					if(p.getLevel() > getMaxLevel() || p.getLevel() < getMinLevel())
						return 8;
				}

				for(Player p : player.getParty().getCommandChannel().getMembers())
				{
					p.addListener(_teleportedListener);
					p.teleToLocation(Location.findPointToStay(getRaidEnterLocation(), 80, p.getGeoIndex()));
				}
			}
			else if(getMinMembersCount() > 1)
			{
				if(player.getParty() == null)
					return 4;

				if(!player.getParty().isLeader(player))
					return 6;

				int partyMemberCount = player.getParty().getMemberCount();
				if(partyMemberCount > getMaxMembersCount() || partyMemberCount < getMinMembersCount())
					return 7;

				for(Player p : player.getParty().getPartyMembers())
				{
					if(p.getLevel() > getMaxLevel() || p.getLevel() < getMinLevel())
						return 8;
				}

				for(Player p : player.getParty().getPartyMembers())
				{
					p.addListener(_teleportedListener);
					p.teleToLocation(Location.findPointToStay(getRaidEnterLocation(), 80, p.getGeoIndex()));
				}
			}
			else if(getMinMembersCount() == 1)
			{
				if(player.getLevel() > getMaxLevel() || player.getLevel() < getMinLevel())
					return 8;

				player.addListener(_teleportedListener);
				player.teleToLocation(Location.findPointToStay(getRaidEnterLocation(), 80, player.getGeoIndex()));
			}

			if(_processStatusTask == null)
			{
				_processStatusTask = ThreadPoolManager.getInstance().schedule(() ->
				{
					setState(EpicBossState.State.ALIVE);
					setStatus(DungeonStatus.RAID_DESTROY);
					checkStatus();
				}, getRaidEnterSceneMovie().getDuration());
			}
		}
		else
		{
			if(_status == DungeonStatus.NONE || _status == DungeonStatus.RAID_FINISHED)
				return 1;

			if(player.getLevel() > getMaxLevel())
				return 2;

			if(player.getLevel() < getMinLevel())
				return 3;
			setState(EpicBossState.State.ALIVE);
			checkStatus();
			SpawnManager.getInstance().spawn(getRaidSpawnGroup());
			if(_status == DungeonStatus.RAID_DESTROY)
				player.teleToLocation(Location.findPointToStay(getRaidEnterLocation(), 80, player.getGeoIndex()));
			else
				player.teleToLocation(Location.findPointToStay(getEnterLocation(), 80, player.getGeoIndex()));
		}
		return 0;
	}

	public void tryExitFromDungeon(Player player)
	{
		if(_status == DungeonStatus.RAID_FINISHED)
			expelFromDungeon(player);
		else
			exitFromDungeon(player);
	}

	private void exitFromDungeon(Player player)
	{
		player.teleToLocation(Location.findPointToStay(getExitLocation(), 80, player.getGeoIndex()));
	}

	private void expelFromDungeon(Player player)
	{
		player.teleToLocation(Location.findPointToStay(getOutsideLocation(), 80, player.getGeoIndex()));
	}

	protected abstract int getMinLevel();

	protected abstract int getMaxLevel();

	protected abstract int getMinMembersCount();

	protected abstract int getMaxMembersCount();

	protected abstract SchedulingPattern getReusePattern();

	protected abstract Location getEnterLocation();

	protected abstract Location getExitLocation();

	protected abstract Location getOutsideLocation();

	protected abstract Location getRaidEnterLocation();

	protected abstract String getDungeonStatusVar();

	protected abstract String getDungeonZoneName();

	protected abstract String getRaidZoneName();

	protected abstract SceneMovie getRaidEnterSceneMovie();

	protected abstract int getEnterGatekeeperId();

	protected abstract int getRaidId();

	protected abstract String getDungeonMonstersSpawnGroup();

	protected abstract String getSealRemnantsSpawnGroup();

	protected abstract String getRaidSpawnGroup();

	protected abstract String getRaidZigguratSpawnGroup();
}