package l2s.gameserver.model.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.xml.holder.InstantZoneHolder;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.instancemanager.DimensionalRiftManager;
import l2s.gameserver.instancemanager.DimensionalRiftManager.DimensionalRiftRoom;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.SimpleSpawner;
import l2s.gameserver.model.Spawner;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.InstantZone;
//夢幻結界
public class DimensionalRift extends Reflection
{
	protected static final long seconds_5 = 5000L;
	protected static final int MILLISECONDS_IN_MINUTE = 60000;
	protected int _roomType;
	protected List<Integer> _completedRooms =   new ArrayList<Integer>();
	protected int jumps_current = 0;

	private Future<?> teleporterTask;

	private Future<?> spawnTask;
	private Future<?> killRiftTask;
	protected int _choosenRoom = -1;

	protected boolean _hasJumped = false;

	protected boolean isBossRoom = false;

	public DimensionalRift(Party party, int type, int room)
	{
		onCreate();
		startCollapseTimer(7200000, true);
		setName("Dimensional Rift");
		if(this instanceof DelusionChamber)
		{
			InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(type + 120);
			setInstancedZone(iz);
			setName(iz.getName());
		}
		this._roomType = type;
		setParty(party);
		if(!(this instanceof DelusionChamber))
			party.setDimensionalRift(this);
		party.setReflection(this);
		this._choosenRoom = room;
		checkBossRoom(this._choosenRoom);

		Location coords = getRoomCoord(this._choosenRoom);

		setReturnLoc(party.getPartyLeader().getLoc());
		setTeleportLoc(coords);
		for(Player p : party.getPartyMembers())
		{
			p.setVar("backCoords", getReturnLoc().toXYZString(), -1L);
			DimensionalRiftManager.teleToLocation(p, Location.findPointToStay(coords, 50, 100, getGeoIndex()), this);
			p.setReflection(this);
		}

		createSpawnTimer(this._choosenRoom);
		createTeleporterTimer();
	}

	public int getType()
	{
		return this._roomType;
	}

	public int getCurrentRoom()
	{
		return this._choosenRoom;
	}

	protected void createTeleporterTimer()
	{
		if(this.teleporterTask != null)
		{
			this.teleporterTask.cancel(false);
			this.teleporterTask = null;
		}

		this.teleporterTask = ThreadPoolManager.getInstance().schedule(() -> {

			if(this.jumps_current < getMaxJumps() && getPlayersInside(true) > 0)
			{
				this.jumps_current++;
				teleportToNextRoom();
				createTeleporterTimer();
			}
			else
			{
				createNewKillRiftTimer();
			}
		}, calcTimeToNextJump());
	}

	public void createSpawnTimer(int room)
	{
		if(this.spawnTask != null)
		{
			this.spawnTask.cancel(false);
			this.spawnTask = null;
		}
		DimensionalRiftRoom riftRoom = DimensionalRiftManager.getInstance().getRoom(_roomType, room);
		spawnTask = ThreadPoolManager.getInstance().schedule(() -> {
			for(SimpleSpawner s : riftRoom.getSpawns())
			{
				SimpleSpawner sp = s.clone();
				sp.setReflection(this);
				addSpawn(sp);
				if(!this.isBossRoom)
					sp.startRespawn();
				for(int i = 0; i < sp.getAmount(); i++)
					sp.doSpawn(true);
			}
			addSpawnWithoutRespawn(getManagerId(), riftRoom.getTeleportCoords(), 0);
		}, Config.RIFT_SPAWN_DELAY);
	}

	public void createNewKillRiftTimer()
	{
		if(this.killRiftTask != null)
		{

			this.killRiftTask.cancel(false);
			this.killRiftTask = null;
		}
		this.killRiftTask = ThreadPoolManager.getInstance().schedule(() -> {
			if(isCollapseStarted())
				return;
			for(Player p : getParty().getPartyMembers())
			{
				if(p != null && p.getReflection() == this)
					DimensionalRiftManager.getInstance().teleportToWaitingRoom(p);
			}
			collapse();
		}, 100L);
	}

	public void partyMemberInvited()
	{
		createNewKillRiftTimer();
	}

	public void partyMemberExited(Player player)
	{
		if(getParty().getMemberCount() < Config.RIFT_MIN_PARTY_SIZE || getParty().getMemberCount() == 1 || getPlayersInside(true) == 0)
		{
			createNewKillRiftTimer();
		}
	}

	public void manualTeleport(Player player, NpcInstance npc)
	{
		if(!player.isInParty() || !player.getParty().isInReflection() || !(player.getParty().getReflection() instanceof DimensionalRift))
		{
			return;
		}
		if(!player.getParty().isLeader(player))
		{
			DimensionalRiftManager.getInstance().showHtmlFile(player, "rift/NotPartyLeader.htm", npc);
			return;
		}
		if(!this.isBossRoom)
		{
			if(this._hasJumped)
			{
				DimensionalRiftManager.getInstance().showHtmlFile(player, "rift/AlreadyTeleported.htm", npc);
				return;
			}
			this._hasJumped = true;
		}
		else
		{
			manualExitRift(player, npc);
			return;
		}
		teleportToNextRoom();
	}

	public void manualExitRift(Player player, NpcInstance npc)
	{
		if(!player.isInParty() || !player.getParty().isInDimensionalRift())
		{
			return;
		}
		if(!player.getParty().isLeader(player))
		{
			DimensionalRiftManager.getInstance().showHtmlFile(player, "rift/NotPartyLeader.htm", npc);
			return;	
		}
		createNewKillRiftTimer();
	}

	protected void teleportToNextRoom()
	{
		this._completedRooms.add(Integer.valueOf(this._choosenRoom));
		for(Spawner s : getSpawns())
		{
			s.deleteAll();
		}
		int size = DimensionalRiftManager.getInstance().getRooms(this._roomType).size();
		if(getType() >= 11 && this.jumps_current == getMaxJumps())
		{
			this._choosenRoom = 9;
		}
		else
		{
			List<Integer> notCompletedRooms = new ArrayList<Integer>();
			for(int i = 1; i <= size; i++)
			{
				if(!this._completedRooms.contains(Integer.valueOf(i)))
					notCompletedRooms.add(Integer.valueOf(i));
			}
			_choosenRoom = (notCompletedRooms.get(Rnd.get(notCompletedRooms.size()))).intValue();
		}
		checkBossRoom(this._choosenRoom);
		setTeleportLoc(getRoomCoord(this._choosenRoom));
		for(Player p : getParty().getPartyMembers())
		{
			if(p.getReflection() == this)
				DimensionalRiftManager.teleToLocation(p, Location.findPointToStay(getRoomCoord(this._choosenRoom), 50, 100, getGeoIndex()), this);
		}
		createSpawnTimer(this._choosenRoom);
	}

	public void collapse()
	{
		if(isCollapseStarted())
		{
			return;
		}
		Future<?> task = this.teleporterTask;
		if(task != null)
		{
			this.teleporterTask = null;
			task.cancel(false);
		}

		task = this.spawnTask;
		if(task != null)
		{
			this.spawnTask = null;
			task.cancel(false);
		}

		task = this.killRiftTask;
		if(task != null)
		{
			this.killRiftTask = null;
			task.cancel(false);
		}
		this._completedRooms = null;
		Party party = getParty();
		if(party != null)
		{
			party.setDimensionalRift(null);
		}
		super.collapse();
	}

	protected long calcTimeToNextJump()
	{
		if(this.isBossRoom)
			return 3600000L;
		return (Config.RIFT_AUTO_JUMPS_TIME * 60000 + Rnd.get(Config.RIFT_AUTO_JUMPS_TIME_RAND));
	}

	public void memberDead(Player player)
	{
		if(getPlayersInside(true) == 0)
		{
			createNewKillRiftTimer();
		}
	}

	public void usedTeleport(Player player)
	{
		if(getPlayersInside(false) < Config.RIFT_MIN_PARTY_SIZE)
		{
			createNewKillRiftTimer();
		}
	}

	public void checkBossRoom(int room)
	{
		this.isBossRoom = DimensionalRiftManager.getInstance().getRoom(this._roomType, room).isBossRoom();
	}

	public Location getRoomCoord(int room)
	{
		return DimensionalRiftManager.getInstance().getRoom(this._roomType, room).getTeleportCoords();
	}

	public int getMaxJumps()
	{
		return Math.max(Math.min(Config.RIFT_MAX_JUMPS, 8), 1);
	}

	public boolean canChampions()
	{
		return true;
	}

	public String getName()
	{
		return "Dimensional Rift";
	}

	protected int getManagerId()
	{
		return 31865;
	}

	protected int getPlayersInside(boolean alive)
	{
		if(this._playerCount == 0)
		{
			return 0;
		}
		int sum = 0;
		for(Player p : getPlayers())
		{
			if(!alive || !p.isDead())
				sum++;
		}
		return sum;
	}

	public void removeObject(GameObject o)
	{
		if(o.isPlayer() && this._playerCount <= 1)
			createNewKillRiftTimer();
		super.removeObject(o);
	}
}
