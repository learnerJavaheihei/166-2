package bosses;

import l2s.commons.time.cron.SchedulingPattern;
import l2s.gameserver.listener.script.OnInitScriptListener;
import l2s.gameserver.network.l2.components.SceneMovie;
import l2s.gameserver.geometry.Location;

/**
 * @author Bonux
 * TODO: Написано все наугад. По возможности переписать под офф.
**/
public class AnakimManager extends SevenSignsRaidManager implements OnInitScriptListener
{
	// Raid Properties
	private static final int MIN_LEVEL = 76;	// 進入最低等級
	private static final int MAX_LEVEL = 86;	// 進入最高等級
	private static final int MIN_MEMBERS_COUNT = 3;	// 進入最小成員數量
	private static final int MAX_MEMBERS_COUNT = Integer.MAX_VALUE;	// BOSS房間最大參與人數
	private static final SchedulingPattern REUSE_PATTERN = new SchedulingPattern("0 21 * * 5"); // 重生時間

	// Dungeon Parameters
	//下面坐標改成你要刷的點進去就可以直接挑戰首領
	private static final Location DUNGEON_ENTER_LOCATION = new Location(-6500, 18000, -5400); // Точка входа в логово

	private static final Location DUNGEON_EXIT_LOCATION = new Location(171768, -17608, -4926);	// Точка выхода из логова
	private static final Location DUNGEON_OUTSIDE_LOCATION = new Location(83432, 148600, -3408);	// Точка снаружи логова
	private static final Location RAID_ENTER_LOCATION = new Location(-7272, 18000, -5450);	// Точка входа к рейду

	// Other
	private static final String DUNGEON_STATUS_VAR = "anakim_dungeon_status";
	private static final SceneMovie RAID_ENTER_SCENE_MOVIE = SceneMovie.SCENE_NECRO;	// Видео при входе к рейду

	// Zone's
	private static final String DUNGEON_ZONE_NAME = "[anakim_dungeon]";
	private static final String DUNGEON_RAID_ZONE_NAME = "[anakim_dungeon_raid]";

	// NPC's 這一個是你刷出來的晶體傳送NPC
	private static final int GATEKEEPER_ZIGGURAT = 31087;	//	Хранитель Портала Зиккурат

	// Monster's
	private static final int FALLEN_ANGEL_ANAKIM = 25286;	// Анаким - Падший Ангел

	// Spawn Group's
	private static final String DUNGEON_MONSTERS_SPAWN_GROUP = "anakim_dungeon_monsters";
	private static final String SEAL_REMNANTS_SPAWN_GROUP = "anakim_seal_remnants";
	private static final String RAID_SPAWN_GROUP = "anakim_dungeon_raid";
	private static final String RAID_ZIGGURAT_SPAWN_GROUP = "anakim_dungeon_raid_ziggurat";

	private static AnakimManager _instance;

	public static AnakimManager getInstance()
	{
		return _instance;
	}

	@Override
	public void onInit()
	{
		_instance = this;
		super.onInit();
	}

	@Override
	protected int getMinLevel()
	{
		return MIN_LEVEL;
	}

	@Override
	protected int getMaxLevel()
	{
		return MAX_LEVEL;
	}

	@Override
	protected int getMinMembersCount()
	{
		return MIN_MEMBERS_COUNT;
	}

	@Override
	protected int getMaxMembersCount()
	{
		return MAX_MEMBERS_COUNT;
	}

	@Override
	protected SchedulingPattern getReusePattern()
	{
		return REUSE_PATTERN;
	}

	@Override
	protected Location getEnterLocation()
	{
		return DUNGEON_ENTER_LOCATION;
	}

	@Override
	protected Location getExitLocation()
	{
		return DUNGEON_EXIT_LOCATION;
	}

	@Override
	protected Location getOutsideLocation()
	{
		return DUNGEON_OUTSIDE_LOCATION;
	}

	@Override
	protected Location getRaidEnterLocation()
	{
		return RAID_ENTER_LOCATION;
	}

	@Override
	protected String getDungeonStatusVar()
	{
		return DUNGEON_STATUS_VAR;
	}

	@Override
	protected String getDungeonZoneName()
	{
		return DUNGEON_ZONE_NAME;
	}

	@Override
	protected String getRaidZoneName()
	{
		return DUNGEON_RAID_ZONE_NAME;
	}

	@Override
	protected SceneMovie getRaidEnterSceneMovie()
	{
		return RAID_ENTER_SCENE_MOVIE;
	}

	@Override
	protected int getEnterGatekeeperId()
	{
		return GATEKEEPER_ZIGGURAT;
	}

	@Override
	protected int getRaidId()
	{
		return FALLEN_ANGEL_ANAKIM;
	}

	@Override
	protected String getDungeonMonstersSpawnGroup()
	{
		return DUNGEON_MONSTERS_SPAWN_GROUP;
	}

	@Override
	protected String getSealRemnantsSpawnGroup()
	{
		return SEAL_REMNANTS_SPAWN_GROUP;
	}

	@Override
	protected String getRaidSpawnGroup()
	{
		return RAID_SPAWN_GROUP;
	}

	@Override
	protected String getRaidZigguratSpawnGroup()
	{
		return RAID_ZIGGURAT_SPAWN_GROUP;
	}
}