package l2s.gameserver.instancemanager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import l2s.commons.geometry.Rectangle;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.geometry.Territory;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.SimpleSpawner;
import l2s.gameserver.model.entity.DimensionalRift;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.s2c.ExTeleportToLocationActivate;
import l2s.gameserver.network.l2.s2c.TeleportToLocationPacket;
import l2s.gameserver.templates.npc.NpcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
//夢幻結界
public class DimensionalRiftManager
{
	private static final Logger _log = LoggerFactory.getLogger(DimensionalRiftManager.class);
	private static DimensionalRiftManager _instance;
	private Map<Integer, Map<Integer, DimensionalRiftRoom>> _rooms = new HashMap<Integer, Map<Integer, DimensionalRiftRoom>>();

	private static final int DIMENSIONAL_FRAGMENT_ITEM_ID = 7079;

	public static DimensionalRiftManager getInstance()
	{
		if(_instance == null)
		{
			_instance = new DimensionalRiftManager();
		}
		return _instance;
	}

	public DimensionalRiftManager()
	{
		load();
	}

	public DimensionalRiftRoom getRoom(int type, int room)
	{
		return _rooms.get(Integer.valueOf(type)).get(Integer.valueOf(room));
	}

	public Map<Integer, DimensionalRiftRoom> getRooms(int type)
	{
		return _rooms.get(Integer.valueOf(type));
	}

	public void load()
	{
		int countGood = 0, countBad = 0;
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			File file = new File(Config.DATAPACK_ROOT, "data/dimensional_rift.xml");
			if(!file.exists())
			{
				throw new IOException();
			}
			Document doc = factory.newDocumentBuilder().parse(file);
			Location tele = new Location();
			int xMin = 0, xMax = 0, yMin = 0, yMax = 0, zMin = 0, zMax = 0;
			for(Node rift = doc.getFirstChild(); rift != null; rift = rift.getNextSibling())
			{
				if("rift".equalsIgnoreCase(rift.getNodeName()))
					for(Node area = rift.getFirstChild(); area != null; area = area.getNextSibling())
					{
						if("area".equalsIgnoreCase(area.getNodeName()))
						{
							NamedNodeMap attrs = area.getAttributes();
							int type = Integer.parseInt(attrs.getNamedItem("type").getNodeValue());
							for(Node room = area.getFirstChild(); room != null; room = room.getNextSibling())
							{
								if("room".equalsIgnoreCase(room.getNodeName()))
								{
									attrs = room.getAttributes();
									int roomId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
									Node boss = attrs.getNamedItem("isBossRoom");
									boolean isBossRoom = (boss != null) ? Boolean.parseBoolean(boss.getNodeValue()) : false;
									Territory territory = null;
									for(Node coord = room.getFirstChild(); coord != null; coord = coord.getNextSibling())
									{
										if("teleport".equalsIgnoreCase(coord.getNodeName()))
										{
											attrs = coord.getAttributes();
											tele = Location.parseLoc(attrs.getNamedItem("loc").getNodeValue());
										}
										else if("zone".equalsIgnoreCase(coord.getNodeName()))
										{
											attrs = coord.getAttributes();
											xMin = Integer.parseInt(attrs.getNamedItem("xMin").getNodeValue());
											xMax = Integer.parseInt(attrs.getNamedItem("xMax").getNodeValue());
											yMin = Integer.parseInt(attrs.getNamedItem("yMin").getNodeValue());
											yMax = Integer.parseInt(attrs.getNamedItem("yMax").getNodeValue());
											zMin = Integer.parseInt(attrs.getNamedItem("zMin").getNodeValue());
											zMax = Integer.parseInt(attrs.getNamedItem("zMax").getNodeValue());
											territory = (new Territory()).add((new Rectangle(xMin, yMin, xMax, yMax)).setZmin(zMin).setZmax(zMax));
										}
									}
									if(territory == null)
									{
										_log.error("DimensionalRiftManager: invalid spawn data for room id " + roomId + "!");
									}
									if(!_rooms.containsKey(Integer.valueOf(type)))
									{
										//new HashMap<Integer, Map<Integer, DimensionalRiftRoom>>()
										_rooms.put(Integer.valueOf(type), new HashMap<Integer, DimensionalRiftRoom>());
									}
									(_rooms.get(Integer.valueOf(type))).put(Integer.valueOf(roomId), new DimensionalRiftRoom(territory, tele, isBossRoom));

									for(Node spawn = room.getFirstChild(); spawn != null; spawn = spawn.getNextSibling())
									{
										if("spawn".equalsIgnoreCase(spawn.getNodeName()))
										{
											attrs = spawn.getAttributes();
											int mobId = Integer.parseInt(attrs.getNamedItem("mobId").getNodeValue());
											int delay = Integer.parseInt(attrs.getNamedItem("delay").getNodeValue());
											int count = Integer.parseInt(attrs.getNamedItem("count").getNodeValue());
											NpcTemplate template = NpcHolder.getInstance().getTemplate(mobId);
											if(template == null)
												_log.warn("Template " + mobId + " not found!");
											if(!_rooms.containsKey(Integer.valueOf(type)))
											{
												_log.warn("Type " + type + " not found!");
											}
											else if(!(_rooms.get(Integer.valueOf(type))).containsKey(Integer.valueOf(roomId)))
											{
												_log.warn("Room " + roomId + " in Type " + type + " not found!");
											}
											if(template != null && _rooms.containsKey(Integer.valueOf(type)) && (_rooms.get(Integer.valueOf(type))).containsKey(Integer.valueOf(roomId)))
											{
												SimpleSpawner spawnDat = new SimpleSpawner(template);
												spawnDat.setTerritory(territory);
												spawnDat.setHeading(-1);
												spawnDat.setSpawnRange(territory);
												spawnDat.setRespawnDelay(delay);
												spawnDat.setAmount(count);
												( (_rooms.get(Integer.valueOf(type))).get(Integer.valueOf(roomId))).getSpawns().add(spawnDat);
												countGood++;
											}
											else
											{
												countBad++;
											}
										}
									}
								}
							}
						}
					}
			}
		}
		catch(Exception e)
		{
			_log.error("DimensionalRiftManager: Error on loading dimensional rift spawns!", e);
		}
		int typeSize = _rooms.keySet().size();
		int roomSize = 0;

		for(Iterator<Integer> iterator = _rooms.keySet().iterator(); iterator.hasNext();)
		{
			int b = iterator.next();
			roomSize += (_rooms.get(Integer.valueOf(b))).keySet().size();
		}
		_log.info("DimensionalRiftManager: Loaded " + typeSize + " room types with " + roomSize + " rooms.");
		_log.info("DimensionalRiftManager: Loaded " + countGood + " dimensional rift spawns, " + countBad + " errors.");
	}

	public void reload()
	{
		for(Iterator<Integer> iterator = _rooms.keySet().iterator(); iterator.hasNext();)
		{
			int b =  iterator.next();
			(_rooms.get(Integer.valueOf(b))).clear();
		}
		_rooms.clear();
		load();
	}

	public boolean checkIfInRiftZone(Location loc, boolean ignorePeaceZone)
	{
		if(ignorePeaceZone)
			return ((_rooms.get(Integer.valueOf(0))).get(Integer.valueOf(1))).checkIfInZone(loc);
		return (((_rooms.get(Integer.valueOf(0))).get(Integer.valueOf(1))).checkIfInZone(loc) && !((_rooms.get(Integer.valueOf(0))).get(Integer.valueOf(0))).checkIfInZone(loc));
	}

	public boolean checkIfInPeaceZone(Location loc)
	{
		return ((_rooms.get(Integer.valueOf(0))).get(Integer.valueOf(0))).checkIfInZone(loc);
	}

	public void teleportToWaitingRoom(Player player)
	{
		teleToLocation(player, Location.findPointToStay(getRoom(0, 0).getTeleportCoords(), 0, 250, ReflectionManager.MAIN.getGeoIndex()), null);
	}

	public void start(Player player, int type, NpcInstance npc)
	{
		if(!player.isInParty())
		{
			showHtmlFile(player, "rift/NoParty.htm", npc);

			return;
		}
		if(!player.isGM())
		{
			if(!player.getParty().isLeader(player))
			{
				showHtmlFile(player, "rift/NotPartyLeader.htm", npc);

				return;
			}
			if(player.getParty().isInDimensionalRift())
			{
				showHtmlFile(player, "rift/Cheater.htm", npc);

				if(!player.isGM())
				{
					_log.warn("Player " + player.getName() + "(" + player.getObjectId() + ") was cheating in dimension rift area!");
				}
				return;
			}
			if(player.getParty().getMemberCount() < Config.RIFT_MIN_PARTY_SIZE)
			{
				showHtmlFile(player, "rift/SmallParty.htm", npc);

				return;
			}
			for(Player p : player.getParty().getPartyMembers())
			{
				if(!checkIfInPeaceZone(p.getLoc()))
				{
					showHtmlFile(player, "rift/NotInWaitingRoom.htm", npc);

					return;
				}
			}
			for(Player p : player.getParty().getPartyMembers())
			{
				ItemInstance i = p.getInventory().getItemByItemId(7079);
				if(i == null || i.getCount() < getNeededItems(type))
				{
					showHtmlFile(player, "rift/NoFragments.htm", npc);

					return;
				}
			}
			for(Player p : player.getParty().getPartyMembers())
			{
				if(!p.getInventory().destroyItemByItemId(7079, getNeededItems(type)))
				{
					showHtmlFile(player, "rift/NoFragments.htm", npc);
					return;
				}
			}
		}
		new DimensionalRift(player.getParty(), type, Rnd.get(1, (_rooms.get(Integer.valueOf(type))).size() - 1));
	}

	public class DimensionalRiftRoom
	{
		private final Territory _territory;
		private final Location _teleportCoords;
		private final boolean _isBossRoom;
		private final List<SimpleSpawner> _roomSpawns;

		public DimensionalRiftRoom(Territory territory, Location tele, boolean isBossRoom)
		{
			_territory = territory;
			_teleportCoords = tele;
			_isBossRoom = isBossRoom;
			_roomSpawns = new ArrayList<SimpleSpawner>();
		}

		public Location getTeleportCoords()
		{
			return _teleportCoords;
		}

		public boolean checkIfInZone(Location loc)
		{
			return checkIfInZone(loc.x, loc.y, loc.z);
		}

		public boolean checkIfInZone(int x, int y, int z)
		{
			return _territory.isInside(x, y, z);
		}

		public boolean isBossRoom()
		{
			return _isBossRoom;
		}

		public List<SimpleSpawner> getSpawns()
		{
			return _roomSpawns;
		}
	}

	private long getNeededItems(int type)
	{
		switch(type)
		{
			case 1:
				return Config.RIFT_ENTER_COST_RECRUIT;
			case 2:
				return Config.RIFT_ENTER_COST_SOLDIER;
			case 3:
				return Config.RIFT_ENTER_COST_OFFICER;
			case 4:
				return Config.RIFT_ENTER_COST_CAPTAIN;
			case 5:
				return Config.RIFT_ENTER_COST_COMMANDER;
			case 6:
				return Config.RIFT_ENTER_COST_HERO;
		}
		return Integer.MAX_VALUE;
	}

	public void showHtmlFile(Player player, String file, NpcInstance npc)
	{
		HtmlMessage html = new HtmlMessage(npc);
		html.setFile(file);
		html.replace("%t_name%", npc.getName());
		player.sendPacket(html);
	}

	public static void teleToLocation(Player player, Location loc, Reflection ref)
	{
		if(player.isTeleporting() || player.isDeleted())
			return;
		player.setIsTeleporting(true);

		player.setTarget(null);
		player.getMovement().stopMove();

		if(player.isInBoat())
		{
			player.setBoat(null);
		}
		player.breakFakeDeath();

		player.sendPacket(new TeleportToLocationPacket(player, loc));

		player.decayMe();

		player.setLoc(loc);

		if(ref == null)
		{
			player.setReflection(ReflectionManager.MAIN);
		}
		// player.setLastClientPosition(null);
		// player.setLastServerPosition(null);
		player.sendPacket(new ExTeleportToLocationActivate(player, loc));
	}
}
