package handler.bbs.custom;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import handler.bbs.ScriptsCommunityHandler;
import handler.onshiftaction.commons.RewardListInfo;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.cache.ImagesCache;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.xml.holder.NpcHolder;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.s2c.RadarControlPacket;
import l2s.gameserver.network.l2.s2c.SayPacket2;
import l2s.gameserver.network.l2.s2c.ShowBoardPacket;
import l2s.gameserver.templates.item.ItemGrade;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.npc.NpcTemplate;

/**
 * 佈告欄搜尋系統
 */
public class CommunityDropCalculator extends ScriptsCommunityHandler
{
	private static final Logger _log = LoggerFactory.getLogger(CommunityDropCalculator.class);

	private static final IntObjectMap<Map<String, Object>> QUICK_VARS = new CHashIntObjectMap<Map<String, Object>>();

	@Override
	public void onInit()
	{
		super.onInit();
		ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> CalculateRewardChances.recacheNpcs(), 0L, 60000L);
	}

	@Override
	public String[] getBypassCommands()
	{
		return new String[] { "_dropCalc", "_dropItemsByName", "_dropMonstersByItem", "_dropMonstersByName", "_dropMonsterDetailsByItem", "_dropMonsterDetailsByName" };
	}

	@Override
	public void doBypassCommand(Player player, String bypass)
	{
		StringTokenizer st = new StringTokenizer(bypass, "_");
		String cmd = st.nextToken();
		player.setSessionVar("add_fav", null);

		if (!BBSConfig.ALLOW_DROP_CALCULATOR)
		{
			String html = HtmCache.getInstance().getHtml("scripts/handler/bbs/pages/bbs_dropCalcOff.htm", player);
			ShowBoardPacket.separateAndSend(html, player);
			return;
		}

		/*
		 * if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player))
		 * {
		 * onWrongCondition(player);
		 * return;
		 * }
		 */

		switch (cmd)
		{
		case "dropCalc":
			showMainPage(player);
			break;
		case "dropItemsByName":
			if (!st.hasMoreTokens())
			{
				showMainPage(player);
				return;
			}
			String itemName = "";
			while (st.countTokens() > 1)
				itemName += " " + st.nextToken();
			int itemsPage = 1;
			try
			{
				itemsPage = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
			}
			catch (Exception e)
			{
				player.sendMessage("Error occured, try again later!");
				doBypassCommand(player, "_dropCalc");
				return;
			}
			showDropItemsByNamePage(player, itemName.trim(), itemsPage);
			break;
		case "dropMonstersByItem":

			int itemId = 1;
			int monstersPage = 1;
			try
			{
				itemId = Integer.parseInt(st.nextToken());
				monstersPage = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
			}
			catch (Exception e)
			{
				player.sendMessage("Error occured, try again later!");
				doBypassCommand(player, "_dropCalc");
				return;
			}
			showDropMonstersByItem(player, itemId, monstersPage);
			break;
		case "dropMonsterDetailsByItem":
			int monsterId = 1;
			int nextTokn = 0;
			try
			{
				monsterId = Integer.parseInt(st.nextToken());
				if (st.hasMoreTokens())
				{
					nextTokn = Integer.parseInt(st.nextToken());
					manageButton(player, nextTokn, monsterId);
				}
			}
			catch (Exception e)
			{
				player.sendMessage("Error occured, try again later!");
				doBypassCommand(player, "_dropCalc");
				return;
			}
			showdropMonsterDetailsByItem(player, monsterId);
			break;
		case "dropMonstersByName":
			if (!st.hasMoreTokens())
			{
				showMainPage(player);
				return;
			}
			String monsterName = "";
			while (st.countTokens() > 1)
				monsterName += " " + st.nextToken();
			int monsterPage = 1;
			try
			{
				int nexttkn = Integer.parseInt(st.nextToken());
				monsterPage = nexttkn;//'st.hasMoreTokens() ? nexttkn : 1;
			}
			catch (Exception e)
			{
				player.sendMessage("Error occured, try again later!");
				doBypassCommand(player, "_dropCalc");
				return;
			}
			showDropMonstersByName(player, monsterName.trim(), monsterPage);
			break;
		case "dropMonsterDetailsByName":
			int chosenMobId = 1;
			try
			{
				chosenMobId = Integer.parseInt(st.nextToken());
				if (st.hasMoreTokens())
				{
					int nexttkn = Integer.parseInt(st.nextToken());
					manageButton(player, nexttkn, chosenMobId);
				}
			}
			catch (Exception e)
			{
				player.sendMessage("Error occured, try again later!");
				doBypassCommand(player, "_dropCalc");
				return;
			}
			showDropMonsterDetailsByName(player, chosenMobId);
			break;
		default:
			break;
		}
	}

	private static void showMainPage(Player player)
	{
		String html = HtmCache.getInstance().getHtml("scripts/handler/bbs/pages/bbs_dropCalcMain.htm", player);
		ShowBoardPacket.separateAndSend(html, player);
	}

	private static void showDropItemsByNamePage(Player player, String itemName, int page)
	{
		addQuickVar(player, "DCItemName", itemName);
		addQuickVar(player, "DCItemsPage", page);
		String html = HtmCache.getInstance().getHtml("scripts/handler/bbs/pages/bbs_dropItemsByName.htm", player);
		html = replaceItemsByNamePage(html, itemName, page);
		ShowBoardPacket.separateAndSend(html, player);
	}

	private static String replaceItemsByNamePage(String html, String itemName, int page)
	{
		String newHtml = html;

		List<ItemTemplate> itemsByName = CalculateRewardChances.getItemsByNameContainingString(itemName, true);
		itemsByName.sort(new ItemComparator(itemName));

		int itemIndex = 0;

		for (int i = 0; i < 8; i++)
		{
			itemIndex = i + (page - 1) * 8;
			ItemTemplate item = itemsByName.size() > itemIndex ? itemsByName.get(itemIndex) : null;

			newHtml = newHtml.replace("%itemIcon" + i + '%', item != null ? getItemIcon(item) : "<br>");
			newHtml = newHtml.replace("%itemName" + i + '%', item != null ? getName(item.getName()) : "<br>");
			newHtml = newHtml.replace("%itemGrade" + i + '%', item != null ? getItemGradeIcon(item) : "<br>");
			newHtml = newHtml.replace("%dropLists" + i + '%', item != null ? String.valueOf(CalculateRewardChances.getDroplistsCountByItemId(item.getItemId(), true)) : "<br>");
			newHtml = newHtml.replace("%spoilLists" + i + '%', item != null ? String.valueOf(CalculateRewardChances.getDroplistsCountByItemId(item.getItemId(), false)) : "<br>");
			newHtml = newHtml.replace("%showMonsters" + i + '%', item != null ? "<button value=\"顯示怪物名稱\" action=\"bypass _dropMonstersByItem_%itemChosenId" + i + "%\" width=120 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">" : "<br>");
			newHtml = newHtml.replace("%itemChosenId" + i + '%', item != null ? String.valueOf(item.getItemId()) : "<br>");
		}

		newHtml = newHtml.replace("%previousButton%", page > 1 ? "<button value=\"上一頁\" action=\"bypass _dropItemsByName_" + itemName + "_" + (page - 1) + "\" width=100 height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">" : "<br>");
		newHtml = newHtml.replace("%nextButton%", itemsByName.size() > itemIndex + 1 ? "<button value=\"下一頁\" action=\"bypass _dropItemsByName_" + itemName + "_" + (page + 1) + "\" width=100 height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">" : "<br>");

		newHtml = newHtml.replace("%searchItem%", itemName);
		newHtml = newHtml.replace("%page%", String.valueOf(page));

		return newHtml;
	}

	private static void showDropMonstersByItem(Player player, int itemId, int page)
	{
		addQuickVar(player, "DCItemId", itemId);
		addQuickVar(player, "DCMonstersPage", page);
		String html = HtmCache.getInstance().getHtml("scripts/handler/bbs/pages/bbs_dropMonstersByItem.htm", player);
		html = replaceMonstersByItemPage(player, html, itemId, page);
		ShowBoardPacket.separateAndSend(html, player);
	}

	private static String replaceMonstersByItemPage(Player player, String html, int itemId, int page)
	{
		String newHtml = html;

		List<CalculateRewardChances.NpcTemplateDrops> templates = CalculateRewardChances.getNpcsByDropOrSpoil(itemId);
		templates.sort(new ItemChanceComparator(player, itemId));

		int npcIndex = 0;

		for (int i = 0; i < 10; i++)
		{
			npcIndex = i + (page - 1) * 10;
			CalculateRewardChances.NpcTemplateDrops drops = templates.size() > npcIndex ? templates.get(npcIndex) : null;
			NpcTemplate npc = templates.size() > npcIndex ? templates.get(npcIndex).template : null;

			newHtml = newHtml.replace("%monsterName" + i + '%', npc != null ? getName(npc.getName()) : "<br>");
			newHtml = newHtml.replace("%monsterLevel" + i + '%', npc != null ? String.valueOf(npc.level) : "<br>");
			newHtml = newHtml.replace("%monsterAggro" + i + '%', npc != null ? String.valueOf(npc.aggroRange > 0) : "<br>");
			newHtml = newHtml.replace("%monsterType" + i + '%', npc != null ? drops.dropNoSpoil ? "掉落" : "回收" : "<br>");
			newHtml = newHtml.replace("%monsterCount" + i + '%', npc != null ? String.valueOf(getDropCount(player, npc, itemId, drops.dropNoSpoil)) : "<br>");
			newHtml = newHtml.replace("%monsterChance" + i + '%', npc != null ? String.valueOf(getDropChance(player, npc, itemId, drops.dropNoSpoil)) : "<br>");
			newHtml = newHtml.replace("%showDetails" + i + '%', npc != null ? "<button value=\"顯示詳細訊息\" action=\"bypass _dropMonsterDetailsByItem_%monsterId" + i + "%\" width=120 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">" : "<br>");
			newHtml = newHtml.replace("%monsterId" + i + '%', npc != null ? String.valueOf(npc.getId()) : "<br>");
		}

		newHtml = newHtml.replace("%previousButton%", page > 1 ? "<button value=\"上一頁\" action=\"bypass _dropMonstersByItem_%itemChosenId%_" + (page - 1) + "\" width=100 height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">" : "<br>");
		newHtml = newHtml.replace("%nextButton%", templates.size() > npcIndex + 1 ? "<button value=\"下一頁\" action=\"bypass _dropMonstersByItem_%itemChosenId%_" + (page + 1) + "\" width=100 height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">" : "<br>");

		newHtml = newHtml.replace("%searchItem%", getQuickVarS(player, "DCItemName"));
		newHtml = newHtml.replace("%searchItemPage%", String.valueOf(getQuickVarI(player, "DCItemsPage")));
		newHtml = newHtml.replace("%itemChosenId%", String.valueOf(itemId));
		newHtml = newHtml.replace("%monsterPage%", String.valueOf(page));
		return newHtml;
	}

	private static void showdropMonsterDetailsByItem(Player player, int monsterId)
	{
		String html = HtmCache.getInstance().getHtml("scripts/handler/bbs/pages/bbs_dropMonsterDetailsByItem.htm", player);
		html = replaceMonsterDetails(player, html, monsterId);

		// DO NOT ALLOW TO TELEPORT TO MOBS
		// if (!canTeleToMonster(player, monsterId, false))
		// html = html.replace("%goToNpc%", "<br>");
		// else
		// html = html.replace("%goToNpc%", "<button value=\"Go to Npc\" action=\"bypass
		// _dropMonsterDetailsByItem_"+monsterId+"_3\" width=200 height=30
		// back=\"L2UI_CT1.OlympiadWnd_DF_Fight1None_Down\"
		// fore=\"L2UI_ct1.OlympiadWnd_DF_Fight1None\">");

		// CalculateRewardChances.sendUsedImages(html, player);
		ShowBoardPacket.separateAndSend(html, player);
	}

	private static String replaceMonsterDetails(Player player, String html, int monsterId)
	{
		String newHtml = html;

		int itemId = getQuickVarI(player, "DCItemId");
		NpcTemplate template = NpcHolder.getInstance().getTemplate(monsterId);
		if (template == null)
			return newHtml;

		newHtml = newHtml.replace("%searchName%", String.valueOf(getQuickVarS(player, "DCMonsterName")));
		newHtml = newHtml.replace("%itemChosenId%", String.valueOf(getQuickVarI(player, "DCItemId")));
		newHtml = newHtml.replace("%monsterPage%", String.valueOf(getQuickVarI(player, "DCMonstersPage")));
		newHtml = newHtml.replace("%monsterId%", String.valueOf(monsterId));
		newHtml = newHtml.replace("%imageId%", String.valueOf(ImagesCache.getInstance().getImageId(monsterId + ".png") > 0 ? monsterId : 0));
		newHtml = newHtml.replace("%monsterName%", getName(template.getName()));
		newHtml = newHtml.replace("%monsterLevel%", String.valueOf(template.level));
		newHtml = newHtml.replace("%monsterAggro%", String.valueOf(template.aggroRange > 0));
		if (itemId > 0)
		{
			newHtml = newHtml.replace("%monsterDropSpecific%", String.valueOf(getDropChance(player, template, itemId, true)));
			newHtml = newHtml.replace("%monsterSpoilSpecific%", String.valueOf(getDropChance(player, template, itemId, false)));
		}
		newHtml = newHtml.replace("%monsterDropAll%", String.valueOf(CalculateRewardChances.getDrops(template, true, false).size()));
		newHtml = newHtml.replace("%monsterSpoilAll%", String.valueOf(CalculateRewardChances.getDrops(template, false, true).size()));
		newHtml = newHtml.replace("%spawnCount%", String.valueOf(CalculateRewardChances.getSpawnedCount(monsterId)));
		newHtml = newHtml.replace("%minions%", String.valueOf(template.getMinionData().size()));
		newHtml = newHtml.replace("%expReward%", String.valueOf(template.rewardExp));
		newHtml = newHtml.replace("%maxHp%", String.valueOf(template.getBaseHpMax(template.level)));
		newHtml = newHtml.replace("%maxMP%", String.valueOf(template.getBaseMpMax(template.level)));
		newHtml = newHtml.replace("%pAtk%", String.valueOf(template.getBaseMpMax(template.level)));
		newHtml = newHtml.replace("%mAtk%", String.valueOf(template.getBaseMAtk()));
		newHtml = newHtml.replace("%pDef%", String.valueOf(template.getBasePDef()));
		newHtml = newHtml.replace("%mDef%", String.valueOf(template.getBaseMDef()));
		newHtml = newHtml.replace("%atkSpd%", String.valueOf(template.getBasePAtkSpd()));
		newHtml = newHtml.replace("%castSpd%", String.valueOf(template.getBaseMAtkSpd()));
		newHtml = newHtml.replace("%runSpd%", String.valueOf(template.getBaseRunSpd()));

		return newHtml;
	}

	private static void showDropMonstersByName(Player player, String monsterName, int page)
	{
		addQuickVar(player, "DCMonsterName", monsterName);
		addQuickVar(player, "DCMonstersPage", page);
		String html = HtmCache.getInstance().getHtml("scripts/handler/bbs/pages/bbs_dropMonstersByName.htm", player);
		html = replaceMonstersByName(html, monsterName, page);
		ShowBoardPacket.separateAndSend(html, player);
	}

	private static String replaceMonstersByName(String html, String monsterName, int page)
	{
		String newHtml = html;
		List<NpcTemplate> npcTemplates = CalculateRewardChances.getNpcsContainingString(monsterName);
		npcTemplates = sortMonsters(npcTemplates, monsterName);

		int npcIndex = 0;

		for (int i = 0; i < 10; i++)
		{
			npcIndex = i + (page - 1) * 10;
			NpcTemplate npc = npcTemplates.size() > npcIndex ? npcTemplates.get(npcIndex) : null;

			newHtml = newHtml.replace("%monsterName" + i + '%', npc != null ? getName(npc.getName()) : "<br>");
			newHtml = newHtml.replace("%monsterLevel" + i + '%', npc != null ? String.valueOf(npc.level) : "<br>");
			newHtml = newHtml.replace("%monsterAggro" + i + '%', npc != null ? String.valueOf(npc.aggroRange > 0) : "<br>");
			newHtml = newHtml.replace("%monsterDrops" + i + '%', npc != null ? String.valueOf(CalculateRewardChances.getDrops(npc, true, false).size()) : "<br>");
			newHtml = newHtml.replace("%monsterSpoils" + i + '%', npc != null ? String.valueOf(CalculateRewardChances.getDrops(npc, false, true).size()) : "<br>");
			newHtml = newHtml.replace("%showDetails" + i + '%', npc != null ? "< button value =\"顯示詳細訊息\" action=\"bypass _dropMonsterDetailsByName_" + npc.getId() + "\" width=120 height=32 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">" : "<br>");
		}

		newHtml = newHtml.replace("%previousButton%", page > 1 ? "<button value=\"上一頁\" action=\"bypass _dropMonstersByName_%searchName%_" + (page - 1) + "\" width=100 height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">" : "<br>");
		newHtml = newHtml.replace("%nextButton%", npcTemplates.size() > npcIndex + 1 ? "<button value=\"下一頁\" action=\"bypass _dropMonstersByName_%searchName%_" + (page + 1) + "\" width=100 height=22 back=\"L2UI_CT1.Button_DF_Down\" fore=\"L2UI_ct1.Button_DF\">" : "<br>");

		newHtml = newHtml.replace("%searchName%", monsterName);
		newHtml = newHtml.replace("%page%", String.valueOf(page));
		return newHtml;
	}

	private static void showDropMonsterDetailsByName(Player player, int monsterId)
	{
		String html = HtmCache.getInstance().getHtml("scripts/handler/bbs/pages/bbs_dropMonsterDetailsByName.htm", player);
		html = replaceMonsterDetails(player, html, monsterId);
		//		if (!canTeleToMonster(player, monsterId, false))
		//			html = html.replace("%goToNpc%", "<br>");
		//		else
		//			html = html.replace("%goToNpc%", "<button value=\"Go to Npc\" action=\"bypass _dropMonsterDetailsByName_" + monsterId + "_3\" width=200 height=30 back=\"L2UI_CT1.OlympiadWnd_DF_Fight1None_Down\" fore=\"L2UI_ct1" + ".OlympiadWnd_DF_Fight1None\">");

		//CalculateRewardChances.sendUsedImages(html, player);
		ShowBoardPacket.separateAndSend(html, player);
	}

	private static void manageButton(Player player, int buttonId, int monsterId)
	{
		switch (buttonId)
		{
		case 1:// Show Monster on Map
			final List<Location> locs = CalculateRewardChances.getRandomSpawnsByNpc(monsterId);
			if (locs == null || locs.isEmpty())
				return;

			player.sendPacket(new RadarControlPacket(2, 2, 0, 0, 0));
			player.sendPacket(new SayPacket2(player.getObjectId(), ChatType.COMMANDCHANNEL_ALL, "", "打開地圖可以查看位置"));

			for (Location loc : locs)
				player.sendPacket(new RadarControlPacket(0, 1, loc));
			break;
		case 2:// Show Drops
			List<NpcInstance> npcs = GameObjectsStorage.getNpcs(false, monsterId);
			if (!npcs.isEmpty())
				RewardListInfo.showInfo(player, npcs.get(0), null, 1);
			break;
		// case 3:// Teleport To Monster
		// if (!canTeleToMonster(player, monsterId, true))
		// {
		// return;
		// }
		// List<NpcInstance> aliveInstance = GameObjectsStorage.getAllByNpcId(monsterId,
		// true);
		// if (!aliveInstance.isEmpty())
		// player.teleToLocation(aliveInstance.get(0).getLoc());
		// else
		// player.sendMessage("Monster isn't alive!");
		// break;
		default:
			break;
		}
	}

	// private static boolean canTeleToMonster(Player player, int monsterId, boolean
	// sendMessage)
	// {
	// if (!player.isInZonePeace())
	// {
	// if (sendMessage)
	// player.sendMessage("You can do it only in safe zone!");
	// return false;
	// }
	//
	// if (Olympiad.isRegistered(player) || player.isInOlympiadMode())
	// {
	// if (sendMessage)
	// player.sendMessage("You cannot do it while being registered in Olympiad
	// Battle!");
	// return false;
	// }
	//
	// if (Arrays.binarySearch(Config.DROP_CALCULATOR_DISABLED_TELEPORT, monsterId)
	// >= 0)
	// {
	// if (sendMessage)
	// player.sendMessage("You cannot teleport to this Npc!");
	// return false;
	// }
	//
	// return true;
	// }

	private static CharSequence getItemIcon(ItemTemplate template)
	{
		return "<img src=\"" + template.getIcon() + "\" width=32 height=32>";
	}

	private static CharSequence getItemGradeIcon(ItemTemplate template)
	{
		if (template.getGrade() == ItemGrade.NONE)
			return "";
		return "<img src=\"L2UI_CT1.Icon_DF_ItemGrade_" + template.getGrade() + "\" width=16 height=16>";
	}

	private static CharSequence getName(String name)
	{
		if (name.length() > 24)
			return "</font><font color=31B404>" + name;
		return name;
	}

	private static String getDropCount(Player player, NpcTemplate monster, int itemId, boolean drop)
	{
		long[] counts = CalculateRewardChances.getDropCounts(player, monster, drop, itemId);
		String formattedCounts = "[" + counts[0] + "..." + counts[1] + ']';
		if (formattedCounts.length() > 20)
			formattedCounts = "</font><font color=31B404>" + formattedCounts;
		return formattedCounts;
	}

	private static String getDropChance(Player player, NpcTemplate monster, int itemId, boolean drop)
	{
		String chance = CalculateRewardChances.getDropChance(player, monster, drop, itemId);
		return formatDropChance(chance);
	}

	public static String formatDropChance(String chance)
	{
		String realChance = chance;
		if (realChance.length() - realChance.indexOf('.') > 6)
			realChance = realChance.substring(0, realChance.indexOf('.') + 7);

		if (realChance.endsWith(".0"))
			realChance = realChance.substring(0, realChance.length() - 2);

		return realChance + '%';
	}

	private static class ItemComparator implements Comparator<ItemTemplate>, Serializable
	{
		private static final long serialVersionUID = -6389059445439769861L;
		private final String search;

		private ItemComparator(String search)
		{
			this.search = search;
		}

		@Override
		public int compare(ItemTemplate o1, ItemTemplate o2)
		{
			if (o1.equals(o2))
				return 0;
			if (o1.getName().equalsIgnoreCase(search))
				return -1;
			if (o2.getName().equalsIgnoreCase(search))
				return 1;

			return Integer.compare(CalculateRewardChances.getDroplistsCountByItemId(o2.getItemId(), true), CalculateRewardChances.getDroplistsCountByItemId(o1.getItemId(), true));
		}
	}

	private static class ItemChanceComparator
			implements Comparator<CalculateRewardChances.NpcTemplateDrops>, Serializable
	{
		private static final long serialVersionUID = 6323413829869254438L;
		private final int itemId;
		private final Player player;

		private ItemChanceComparator(Player player, int itemId)
		{
			this.itemId = itemId;
			this.player = player;
		}

		@Override
		public int compare(CalculateRewardChances.NpcTemplateDrops o1, CalculateRewardChances.NpcTemplateDrops o2)
		{
			BigDecimal maxDrop1 = BigDecimal.valueOf(CalculateRewardChances.getDropCounts(player, o1.template, o1.dropNoSpoil, itemId)[1]);
			BigDecimal maxDrop2 = BigDecimal.valueOf(CalculateRewardChances.getDropCounts(player, o2.template, o2.dropNoSpoil, itemId)[1]);
			BigDecimal chance1 = new BigDecimal(CalculateRewardChances.getDropChance(player, o1.template, o1.dropNoSpoil, itemId));
			BigDecimal chance2 = new BigDecimal(CalculateRewardChances.getDropChance(player, o2.template, o2.dropNoSpoil, itemId));

			int compare = chance2.multiply(maxDrop2).compareTo(chance1.multiply(maxDrop1));
			if (compare == 0)
				return o2.template.getName().compareTo(o1.template.getName());
			return compare;
		}
	}

	private static List<NpcTemplate> sortMonsters(List<NpcTemplate> npcTemplates, String monsterName)
	{
		Collections.sort(npcTemplates, new MonsterComparator(monsterName));
		return npcTemplates;
	}

	private static class MonsterComparator implements Comparator<NpcTemplate>, Serializable
	{
		private static final long serialVersionUID = 2116090903265145828L;
		private final String search;

		private MonsterComparator(String search)
		{
			this.search = search;
		}

		@Override
		public int compare(NpcTemplate o1, NpcTemplate o2)
		{
			if (o1.equals(o2))
				return 0;
			if (o1.getName().equalsIgnoreCase(search))
				return 1;
			if (o2.getName().equalsIgnoreCase(search))
				return -1;

			return o2.getName().compareTo(o2.getName());
		}
	}

	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
	}

	public static void addQuickVar(Player player, String name, Object value)
	{
		Map<String, Object> quickVars = QUICK_VARS.get(player.getObjectId());
		if (quickVars == null)
		{
			quickVars = new ConcurrentHashMap<String, Object>();
			QUICK_VARS.put(player.getObjectId(), quickVars);
		}
		quickVars.put(name, value);
	}

	public static String getQuickVarS(Player player, String name, String... defaultValue)
	{
		Map<String, Object> quickVars = QUICK_VARS.get(player.getObjectId());
		if (quickVars == null || !quickVars.containsKey(name))
		{
			if (defaultValue.length > 0)
				return defaultValue[0];
			return null;
		}
		return (String) quickVars.get(name);
	}

	private static int getQuickVarI(Player player, String name, int... defaultValue)
	{
		Map<String, Object> quickVars = QUICK_VARS.get(player.getObjectId());
		if (quickVars == null || !quickVars.containsKey(name))
		{
			if (defaultValue.length > 0)
				return defaultValue[0];
			return -1;
		}
		return ((Integer) quickVars.get(name)).intValue();
	}
}