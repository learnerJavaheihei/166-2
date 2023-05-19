package handler.bbs.custom;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilderFactory;

import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.model.actor.instances.player.BookMarkList;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.network.l2.s2c.ShowBoardPacket;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.utils.HtmlUtils;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Language;
import l2s.gameserver.utils.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class CommunityTeleport extends CustomCommunityHandler
{
	private static final Logger _log = LoggerFactory.getLogger(CommunityTeleport.class);

	private static final TIntObjectMap<TeleportInfo> _teleportsInfo = new TIntObjectHashMap<TeleportInfo>();

	private static final int TELEPORTS_PER_PAGE = 15;

	private static TeleportList _mainTeleportList = null;

	@Override
	public void onInit()
	{
		super.onInit();
		loadTeleportList();
	}

	@Override
	public String[] getBypassCommands()
	{
		return new String[]
		{
			"_cbbsteleport",
			"_cbbstpsavepoint",
			"_cbbsteleportdelete",
			"_cbbgroup"
		};
	}

	@Override
	protected void doBypassCommand(Player player, String bypass)
	{
		if(BBSConfig.TELEPORT_SERVICE_COST_ITEM_ID == 0)
		{
			player.sendMessage(player.isLangRus() ? "功能禁用。" : "功能禁用。");
			player.sendPacket(ShowBoardPacket.CLOSE);
			return;
		}

		StringTokenizer st = new StringTokenizer(bypass, "_");
		String cmd = st.nextToken();
		String html = "";

		if("cbbsteleport".equals(cmd))
		{
			int pointId = 0;
			if(st.hasMoreTokens())
				pointId = Integer.parseInt(st.nextToken());
			TeleportInfo info_point = _teleportsInfo.get(pointId);
			TeleportInfo info = _teleportsInfo.get(0);
			TeleportInfo infos = _teleportsInfo.get(pointId);
			if(info == null)
				return;

			int page = 0;
			if(st.hasMoreTokens())
				page = Integer.parseInt(st.nextToken());
			if(info_point instanceof TeleportPoint)
			{
				if(player.getKarma() < 0 && !BBSConfig.TELEPORT_SERVICE_TELEPORT_IF_PK)
				{
					html = HtmCache.getInstance().getHtml("scripts/handler/bbs/pages/teleports-pk.htm", player);
				}
				else if((player.getPvpFlag() != 0 ) && !BBSConfig.TELEPORT_SERVICE_TELEPORT_IF_PVP )//.isInNonPvpTime())
				{
					html = HtmCache.getInstance().getHtml("scripts/handler/bbs/pages/teleports-pvp.htm", player);
				}
				else if(player.getReflectionId() > 0)//即時地區無法使用佈告欄傳送--
				{
					html = HtmCache.getInstance().getHtml("scripts/handler/bbs/pages/teleports-Instance.htm", player);
				}//--即時地區無法使用佈告欄傳送
				else if(player.getPkKills() > 0)
				{
					html = HtmCache.getInstance().getHtml("scripts/handler/bbs/pages/teleports-pk.htm", player);
				}
				else
				{
					if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player))
					{
						onWrongCondition(player);
						return;
					}

					teleport(player, (TeleportPoint) info_point);
					player.sendPacket(ShowBoardPacket.CLOSE);
					return;
				}
			}
			else if(info instanceof TeleportList)
			{
				html = HtmCache.getInstance().getHtml("scripts/handler/bbs/pages/teleports.htm", player);
				html = html.replace("<?price?>", Util.formatAdena(BBSConfig.TELEPORT_SERVICE_COST_ITEM_COUNT));
				html = html.replace("<?item_name?>", HtmlUtils.htmlItemName(BBSConfig.TELEPORT_SERVICE_COST_ITEM_ID));
				String s = generateTeleportList(player, (TeleportList) info, page,true);
				html = html.replace("<?teleport_list?>", s);
				if((page + pointId) == 0)
				{
					s = "";
				}
				else
				{
					s = generateTeleportList(player, (TeleportList) infos, page,false);
				}
				html = html.replace("<?bm_tp_list?>", s);
				html = html.replace("<?save_tp_price?>", Util.formatAdena(BBSConfig.TELEPORT_SERVICE_BM_SAVE_COST_ITEM_COUNT));
				html = html.replace("<?save_tp_name?>", HtmlUtils.htmlItemName(BBSConfig.TELEPORT_SERVICE_BM_SAVE_COST_ITEM_ID));
			}
		}
		else if("cbbgroup".equals(cmd))
		{
			int pointId = 0;
			if(st.hasMoreTokens())
				pointId = Integer.parseInt(st.nextToken());
			TeleportInfo info = _teleportsInfo.get(pointId);
			if(info == null)
				return;
			if(info instanceof TeleportPoint)
			{
				if((player.getParty() != null) && player.getParty().isLeader(player))
				{
					if(player.getParty().isLeader(player))
					{
						for(Player p : player.getParty())
						{
							if(p.getKarma() < 0 && !BBSConfig.TELEPORT_SERVICE_TELEPORT_IF_PK)
							{
								html = HtmCache.getInstance().getHtml("scripts/handler/bbs/pages/teleports-pk.htm", p);
								broadCast(player, 0);//0
								return;
							}
							else if((p.getPvpFlag() != 0) && !BBSConfig.TELEPORT_SERVICE_TELEPORT_IF_PVP)//.isInNonPvpTime())
							{
								html = HtmCache.getInstance().getHtml("scripts/handler/bbs/pages/teleports-pvp.htm", p);
								broadCast(player, 1);//1
								return;
							}
							else if(p.getReflectionId() > 0)//即時地區無法使用佈告欄傳送--
							{
								html = HtmCache.getInstance().getHtml("scripts/handler/bbs/pages/teleports-Instance.htm", p);
								broadCast(player,2);//2
								return;
							}
							else if((player.getDistance(p) > 2000) && (player != p))//2000距離不可傳送
							{
								broadCast(player, 3);//3
								return;
							}
							else if(p.getPkKills() > 0)
							{
								html = HtmCache.getInstance().getHtml("scripts/handler/bbs/pages/teleports-pk.htm", p);
								broadCast(player, 5);//0
								return;
							}
							else
							{
								if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(p))
								{
									onWrongCondition(p);
									broadCast(player, 4);//4
									return;
								}
							}
						}
						for(Player p : player.getParty())
						{
							teleport(p, (TeleportPoint) info);
							p.sendPacket(ShowBoardPacket.CLOSE);
						}
						return;
					}
					else
					{
						broadCast(player, 5);//5
						return;
					}
				}
				else
				{
					broadCast(player, 6);//6
					return;
				}
			}
			//
		}
		else if("cbbsteleportpoint".equals(cmd))
		{
			if(BBSConfig.TELEPORT_SERVICE_FOR_PREMIUM_ONLY && !player.hasPremiumAccount())
				html = HtmCache.getInstance().getHtml("scripts/handler/bbs/pages/teleports-bm_no_premium.htm", player);
			else if(player.getKarma() < 0 && !BBSConfig.TELEPORT_SERVICE_TELEPORT_IF_PK)
			{
				html = HtmCache.getInstance().getHtml("scripts/handler/bbs/pages/teleports-pk.htm", player);
			}
			else if((player.getPvpFlag() != 0 ) && !BBSConfig.TELEPORT_SERVICE_TELEPORT_IF_PVP )//.isInNonPvpTime())
			{
				html = HtmCache.getInstance().getHtml("scripts/handler/bbs/pages/teleports-pvp.htm", player);
			}
			else if(player.getReflectionId() > 0)
			{
				html = HtmCache.getInstance().getHtml("scripts/handler/bbs/pages/teleports-Instance.htm", player);
			}
			else
			{
				if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player))
				{
					onWrongCondition(player);
					return;
				}

				int x = Integer.parseInt(st.nextToken());
				int y = Integer.parseInt(st.nextToken());	
				int z = Integer.parseInt(st.nextToken());

				if(!BookMarkList.checkTeleportConditions(player))
					return;

				if(ItemFunctions.getItemCount(player, BBSConfig.TELEPORT_SERVICE_BM_COST_ITEM_ID) < BBSConfig.TELEPORT_SERVICE_BM_COST_ITEM_COUNT)
				{
					player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
					return;
				}

				Location loc = Location.findPointToStay(new Location(x, y, z), 50, 100, player.getGeoIndex());
				player.teleToLocation(loc);
				ItemFunctions.deleteItem(player, BBSConfig.TELEPORT_SERVICE_BM_COST_ITEM_ID, BBSConfig.TELEPORT_SERVICE_BM_COST_ITEM_COUNT);
				player.sendPacket(ShowBoardPacket.CLOSE);
				return;
			}
		}
		else if("cbbsteleportdelete".equals(cmd))
		{
			if(BBSConfig.TELEPORT_SERVICE_FOR_PREMIUM_ONLY && !player.hasPremiumAccount())
				html = HtmCache.getInstance().getHtml("scripts/handler/bbs/pages/teleports-bm_no_premium.htm", player);
			else {
				String name = st.nextToken();

				Connection con = null;
				PreparedStatement statement = null;
				try {
					con = DatabaseFactory.getInstance().getConnection();
					statement = con.prepareStatement("DELETE FROM bbs_teleport_bm WHERE char_id=? AND name=?");
					statement.setInt(1, player.getObjectId());
					statement.setString(2, name);
					statement.execute();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					DbUtils.closeQuietly(con, statement);
				}

				onBypassCommand(player, "_cbbsteleport");
				return;
			}
		}
		else if("cbbstpsavepoint".equals(cmd))
		{
			if(BBSConfig.TELEPORT_SERVICE_FOR_PREMIUM_ONLY && !player.hasPremiumAccount())
				html = HtmCache.getInstance().getHtml("scripts/handler/bbs/pages/teleports-bm_no_premium.htm", player);
			else {
				if (!st.hasMoreTokens()) {
					onBypassCommand(player, "_cbbsteleport");
					return;
				}
				String bmName = st.nextToken();
				if (bmName.equals(" ") || bmName.isEmpty()) {
					player.sendMessage(player.isLangRus() ? "您必須輸入名稱。" : "您必须输入名称。");
					onBypassCommand(player, "_cbbsteleport");
					return;
				}

				if (tpNameExist(player, bmName)) {
					player.sendMessage(player.isLangRus() ? "您不能輸入已存在名稱。" : "您不能输入已存在名称。");
					onBypassCommand(player, "_cbbsteleport");
					return;
				}

				if (!checkCond(player, true)) {
					onBypassCommand(player, "_cbbsteleport");
					return;
				}

				if (ItemFunctions.getItemCount(player, BBSConfig.TELEPORT_SERVICE_BM_SAVE_COST_ITEM_ID) < BBSConfig.TELEPORT_SERVICE_BM_SAVE_COST_ITEM_COUNT) {
					player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
					return;
				}

				ItemFunctions.deleteItem(player, BBSConfig.TELEPORT_SERVICE_BM_SAVE_COST_ITEM_ID, BBSConfig.TELEPORT_SERVICE_BM_SAVE_COST_ITEM_COUNT);

				Connection con = null;
				PreparedStatement statement = null;

				try {
					con = DatabaseFactory.getInstance().getConnection();
					statement = con.prepareStatement("INSERT INTO bbs_teleport_bm (char_id,name,x,y,z) VALUES (?,?,?,?,?)");
					statement.setInt(1, player.getObjectId());
					statement.setString(2, bmName);
					statement.setInt(3, player.getX());
					statement.setInt(4, player.getY());
					statement.setInt(5, player.getZ());
					statement.execute();
				} catch (Exception e) {
					_log.warn("CommunityTeleport: cannot save tp book mark for player " + player.getName() + "");
					e.printStackTrace();
				} finally {
					player.sendMessage(player.isLangRus() ? "傳送位置保存成功。" : "传送位置保存成功。");
					DbUtils.closeQuietly(con, statement);
				}
				onBypassCommand(player, "_cbbsteleport");
				return;
			}
		}		
		ShowBoardPacket.separateAndSend(html, player);
	}

		String message_Tw[] = { "玩家「$ss」紅人無法傳送。",
			"玩家「$ss」戰鬥中無法傳送。",
			"玩家「$ss」處於即時地區狀態。",
			"玩家「$ss」並不在隊長附近。",
			"玩家「$ss」狀態異常無法傳送。",
			"隊長才可以點擊。",
			"需組隊情況下隊長才可以點擊。"};
			
		String message[] = { "玩家「$ss」红人无法传送。", 
			"玩家「$ss」战斗中无法传送。", 
			"玩家「$ss」处于即时地区状态。", 
			"玩家「$ss」并不在队长附近。", 
			"玩家「$ss」状态异常无法传送。", 
			"队长才可以点击。", 
			"需组队情况下队长才可以点击。" };

	private void broadCast(Player player, int saying)
	{
		if(player.getParty() != null)
		{
			for(Player p : player.getParty())
			{
				if(p.getLanguage() == Language.CHINESE_TW)
				{
					p.sendMessage(message_Tw[saying].replace("$ss", player.getName()));
				}
				else
				{
					p.sendMessage(message[saying].replace("$ss", player.getName()));
				}
			}
		}
		else
		{
			if(player.getLanguage() == Language.CHINESE_TW)
			{
				player.sendMessage(message_Tw[saying].replace("$ss", player.getName()));
			}
			else
			{
				player.sendMessage(message[saying].replace("$ss", player.getName()));
			}
		}
	}
	public class CBteleport
	{
		public int PlayerId = 0; // charID
		public String TpName = ""; // Loc name
		public int xC = 0; // Location coords
		public int yC = 0; //
		public int zC = 0; //
	}	

	private boolean checkCond(Player player, boolean save)
	{
		if(player.isDead())
			return false;

		if(player.getTeam() !=	TeamType.NONE)
			return false;

		if(player.isFlying() || player.isInFlyingTransform())
			return false;

		if(player.isInBoat())
			return false;

		if(player.isInStoreMode() || player.isInTrade() || player.isInOfflineMode())
			return false;
			
		if(player.isInDuel())
			return false;	

		if(save)
		{
			if(!player.getReflection().isMain() || player.isInSiegeZone() || player.isInZone(ZoneType.RESIDENCE) || player.isInZone(ZoneType.HEADQUARTER) || player.isInZone(ZoneType.battle_zone) ||player.isInZone(ZoneType.ssq_zone) || player.isInZone(ZoneType.no_restart) || player.isInZone(ZoneType.offshore) || player.isInZone(ZoneType.epic) || player.isInOlympiadMode() || player.isInSiegeZone())
			{
				player.sendMessage(player.isLangRus() ? "您無法將目前位置保存。" : "您无法将目前位置保存。");		
				return false;
			}

			if(ItemFunctions.getItemCount(player, BBSConfig.TELEPORT_SERVICE_BM_SAVE_COST_ITEM_ID) < BBSConfig.TELEPORT_SERVICE_BM_SAVE_COST_ITEM_COUNT)
			{
				player.sendMessage(player.isLangRus() ? "保存失敗，費用不足。" : "保存失败，费用不足。");		
				return false;
			}

			if(getCountTP(player) >= BBSConfig.TELEPORT_SERVICE_BM_SAVE_LIMIT)
			{
				player.sendMessage(player.isLangRus() ? "您已達到傳送位置最大保存限制。" : "您已达到传送位置最大保存限制。");
				return false;
			}
		}
		else
		{
			if(ItemFunctions.getItemCount(player, BBSConfig.TELEPORT_SERVICE_BM_COST_ITEM_ID) < BBSConfig.TELEPORT_SERVICE_BM_COST_ITEM_COUNT)
			{
				player.sendMessage(player.isLangRus() ? "傳送失敗，費用不足。" : "传送失败，费用不足。");
				return false;
			}
		}
		return true;
	}

	private int getCountTP(Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		int i = 0;
		
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT name FROM bbs_teleport_bm WHERE char_id=?");
			statement.setInt(1, player.getObjectId());
			rset = statement.executeQuery();
			while(rset.next())
				i++;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		return i;		
	
	}
	private boolean tpNameExist(Player player, String bmName)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		boolean isExist = false;
		
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT name FROM bbs_teleport_bm WHERE char_id=?");
			statement.setInt(1, player.getObjectId());
			rset = statement.executeQuery();
			while(rset.next())
			{
				String name = rset.getString("name");
				if(name.equals(bmName))
					isExist = true;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return isExist;		
	}	
	@Override
	protected void doWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
		//
	}

	private void teleport(Player player, TeleportPoint point)
	{
		if(!BookMarkList.checkTeleportConditions(player))
			return;

		if(player.getReflection().isDefault())
		{
			int castleId = point.getCastleId();
			Castle castle = castleId > 0 ? ResidenceHolder.getInstance().getResidence(Castle.class, castleId) : null;
			// Нельзя телепортироваться в города, где идет осада
			if(castle != null && castle.getSiegeEvent() != null && castle.getSiegeEvent().isInProgress())
			{
				player.sendPacket(SystemMsg.YOU_CANNOT_TELEPORT_TO_A_VILLAGE_THAT_IS_IN_A_SIEGE);
				return;
			}
		}

		int itemId = point.getItemId();
		long itemCount = point.getItemCount();
		if(ItemFunctions.getItemCount(player, itemId) < itemCount)
		{
			player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
			return;
		}

		Location loc = Location.findPointToStay(point.getLocation(), 50, 100, player.getGeoIndex());
		player.teleToLocation(loc);
		ItemFunctions.deleteItem(player, itemId, itemCount);
		player.sendPacket(ShowBoardPacket.CLOSE);
	}

	public void loadTeleportList()
	{
		Document doc = null;
		File file = new File(Config.DATAPACK_ROOT, "data/bbs_teleports.xml");
		if(!file.exists())
		{
			_log.warn("CommunityTeleport: bbs_teleports.xml file is missing.");
			return;
		}

		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			doc = factory.newDocumentBuilder().parse(file);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		try
		{
			parseTeleportList(doc);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	protected void parseTeleportList(Document doc)
	{
		for(Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if("list".equalsIgnoreCase(n.getNodeName()))
			{
				_mainTeleportList = parseTeleportList(n);
			}
		}
	}

	private TeleportList parseTeleportList(Node d)
	{
		TeleportList teleportList = new TeleportList();
		for(Language lang : Language.VALUES)
		{
			if(d.getAttributes().getNamedItem("name_" + lang.getShortName()) != null)
				teleportList.addName(lang, d.getAttributes().getNamedItem("name_" + lang.getShortName()).getNodeValue());
		}
		for(Node t = d.getFirstChild(); t != null; t = t.getNextSibling())
		{
			if("point".equalsIgnoreCase(t.getNodeName()))
			{
				final int x = Integer.parseInt(t.getAttributes().getNamedItem("x").getNodeValue());
				final int y = Integer.parseInt(t.getAttributes().getNamedItem("y").getNodeValue());
				final int z = Integer.parseInt(t.getAttributes().getNamedItem("z").getNodeValue());
				final int castleId = t.getAttributes().getNamedItem("castle_id") == null ? 0 : Integer.parseInt(t.getAttributes().getNamedItem("castle_id").getNodeValue());
				final int itemId = t.getAttributes().getNamedItem("item_id") == null ? BBSConfig.TELEPORT_SERVICE_COST_ITEM_ID : Integer.parseInt(t.getAttributes().getNamedItem("item_id").getNodeValue());
				final long itemCount = t.getAttributes().getNamedItem("item_count") == null ? BBSConfig.TELEPORT_SERVICE_COST_ITEM_COUNT : Integer.parseInt(t.getAttributes().getNamedItem("item_count").getNodeValue());
				TeleportPoint teleportPoint = new TeleportPoint(new Location(x, y, z), castleId, itemId, itemCount);
				for(Language lang : Language.VALUES)
				{
					if(t.getAttributes().getNamedItem("name_" + lang.getShortName()) != null)
						teleportPoint.addName(lang, t.getAttributes().getNamedItem("name_" + lang.getShortName()).getNodeValue());
				}
				teleportList.addPoint(teleportPoint);
			}
			else if("teleport_list".equalsIgnoreCase(t.getNodeName()))
			{
				teleportList.addPoint(parseTeleportList(t));
			}
		}
		return teleportList;
	}

	private String generateTeleportList(Player player, TeleportList list, int page,boolean first)
	{
		int parrentId = 0;
		boolean havePages = false;

		StringBuilder result = new StringBuilder();
		result.append("<table>");
		if(list == null || list.getPointsIds().length == 0)
		{
			result.append("<tr><td align=center>");
			result.append(player.isLangRus() ? "錯誤！禁用傳送列表。" : "错误！禁用传送列表。");
			result.append("</td></tr>");
		}
		else
		{
			parrentId = list.getParrentId();
			int[] pointsIds = list.getPointsIds();
			Arrays.sort(pointsIds);

			final int minPage = 0;
			final int maxPage = (pointsIds.length - 1) / TELEPORTS_PER_PAGE;
			page = Math.min(page, maxPage);
			page = Math.max(page, minPage);
			for(int i = page * TELEPORTS_PER_PAGE; i < Math.min(((page + 1) * TELEPORTS_PER_PAGE), pointsIds.length); i++)
			{
				int pointId = pointsIds[i];
				TeleportInfo info = list.getPoint(pointId);
				result.append("<tr>");
				result.append("<td align=center><button value=\"");
				result.append(info.getName(player.getLanguage()));
				result.append("\" action=\"bypass _cbbsteleport_" + info.getId() + "\" width=200 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>");
				if (!first)
				{
					result.append("<td align=center><button value=\"團隊\" action=\"bypass _cbbgroup_" + info.getId() + "\" width=40 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td>\"");
				}
				result.append("</tr>");
			}
			if(maxPage > minPage)
			{
				havePages = true;
				String pagePrev = page == minPage ? "" : HtmlUtils.htmlButton("&$543;", "bypass _cbbsteleport_" + list.getId() + "_" + (page - 1), 70, 25);
				String pageNext = page == maxPage ? "" : HtmlUtils.htmlButton("&$544;", "bypass _cbbsteleport_" + list.getId() + "_" + (page + 1), 70, 25);
				if(!pagePrev.isEmpty() || !pageNext.isEmpty())
				{
					result.append("<tr><td align=center><table><tr>");
					result.append("<td width=75 align=center>" + pagePrev + "</td>");
					result.append("<td width=75 align=center>");
					if(player.isLangRus())
						result.append("第 ");
					else
						result.append("第 ");
					result.append(page + 1);
					if(player.isLangRus())
						result.append(" 頁");
					else
						result.append(" 页");
					result.append("</td>");
					result.append("<td width=75 align=center>" + pageNext + "</td>");
					result.append("</tr></table></td></tr>");
				}
			}
		}
//		if(list != _mainTeleportList)
//		{
//			if(!havePages)
//				result.append("<tr><td align=center>&nbsp;</td></tr>");
//			result.append("<tr><td align=center><button value=\"");
//			result.append(player.isLangRus() ? "返回" : "返回");
//			result.append("\" action=\"bypass _cbbsteleport_" + parrentId + "\" width=100 height=25 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_CT1.Button_DF\"></td></tr>");
//		}
		result.append("</table>");
		return result.toString();
	}

	private String generateBMTeleportList(Player player)
	{
		StringBuilder teleports = new StringBuilder();

		CBteleport tp;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM bbs_teleport_bm WHERE char_id=?;");
			statement.setLong(1, player.getObjectId());
			rs = statement.executeQuery();
			int i = 0;
			while(rs.next())
			{
				tp = new CBteleport();
				tp.PlayerId = rs.getInt("char_id");
				tp.TpName = rs.getString("name");
				tp.xC = rs.getInt("x");
				tp.yC = rs.getInt("y");
				tp.zC = rs.getInt("z");

				if(i % 2 == 0)
					teleports.append("<table width=288 bgcolor=000000>");
				else
					teleports.append("<table width=288>");
				teleports.append("<tr>");
				teleports.append("<td width=185 align=center><button value=\"" + tp.TpName + "\" action=\"bypass _cbbsteleportpoint_" + tp.xC + "_" + tp.yC + "_" + tp.zC + "\" width=180 height=25 back=\"L2UI_ct1.button_df_down\" fore=\"L2UI_ct1.button_df\"></td>");
				teleports.append("<td width=70 align=center><button value=\"");
				teleports.append(player.isLangRus() ? "刪除" : "删除");
				teleports.append("\" action=\"bypass _cbbsteleportdelete_" + tp.TpName + "\" width=65 height=25 back=\"L2UI_ct1.button_df_down\" fore=\"L2UI_ct1.button_df\"></td>");
				teleports.append("</tr></table>");
				i++;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rs);
		}

		StringBuilder result = new StringBuilder();
		if(teleports.length() > 0)
		{
			result.append("<table>");
			result.append("<tr><td align=center>傳送費用：<font color=\"LEVEL\">");
			result.append(Util.formatAdena(BBSConfig.TELEPORT_SERVICE_BM_COST_ITEM_COUNT));
			result.append(" ");
			result.append(HtmlUtils.htmlItemName(BBSConfig.TELEPORT_SERVICE_BM_COST_ITEM_ID));
			result.append("</font></td></tr>");
			result.append("<tr><td align=center><table width=288 bgcolor=3D3D3D><tr><td width=260 align=center></td></tr></table></td></tr>");
			result.append("<tr><td align=center>");
			result.append(teleports.toString());
			result.append("</td></tr>");
			result.append("<tr><td align=center><table width=288 bgcolor=3D3D3D><tr><td width=260 align=center></td></tr></table></td></tr>");
			result.append("</table><br><br><br>");
		}
		return result.toString();
	}

	private static abstract class TeleportInfo
	{
		private final int _id;
		private final TIntObjectMap<String> _names = new TIntObjectHashMap<String>();
		private int _parrentId = 0;

		public TeleportInfo()
		{
			_id = _teleportsInfo.size();
			_teleportsInfo.put(_id, this);
		}

		public int getId()
		{
			return _id;
		}

		public void setParrentId(int value)
		{
			_parrentId = value;
		}

		public int getParrentId()
		{
			return _parrentId;
		}

		public void addName(Language lang, String name)
		{
			_names.put(lang.ordinal(), name);
		}

		public String getName(Language lang)
		{
			String name = _names.get(lang.ordinal());
			if(name == null)
			{
				Language secondLang = lang;
				do
				{
					if(secondLang == secondLang.getSecondLanguage())
						break;

					if(!Config.AVAILABLE_LANGUAGES.contains(secondLang))
						break;

					secondLang = secondLang.getSecondLanguage();
					name = _names.get(secondLang.ordinal());
				}
				while(name == null);

				if(name == null)
				{
					for(Language l : Language.VALUES)
					{
						if(!Config.AVAILABLE_LANGUAGES.contains(l))
							continue;

						if((name = _names.get(l.ordinal())) != null)
							break;
					}
				}
			}
			return name;
		}
	}

	private static class TeleportList extends TeleportInfo
	{
		private final TIntObjectHashMap<TeleportInfo> _points = new TIntObjectHashMap<TeleportInfo>();

		public TeleportList()
		{
			super();
		}

		public void addPoint(TeleportInfo point)
		{
			_points.put(point.getId(), point);
			point.setParrentId(getId());
		}

		public int[] getPointsIds()
		{
			return _points.keys();
		}

		public TeleportInfo getPoint(int id)
		{
			return _points.get(id);
		}
	}

	private static class TeleportPoint extends TeleportInfo
	{
		private final Location _loc;
		private final int _castleId;
		private final TIntObjectMap<String> _names = new TIntObjectHashMap<String>();
		private final int _itemId;
		private final long _itemCount;

		public TeleportPoint(Location loc, int castleId, int itemId, long itemCount)
		{
			super();
			_loc = loc;
			_castleId = castleId;
			_itemId = itemId;
			_itemCount = itemCount;
		}

		public Location getLocation()
		{
			return _loc;
		}

		public int getCastleId()
		{
			return _castleId;
		}

		public int getItemId()
		{
			return _itemId;
		}

		public long getItemCount()
		{
			return _itemCount;
		}
	}
}