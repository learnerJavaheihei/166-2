package l2s.gameserver.utils;

import l2s.commons.dbutils.DbUtils;
import l2s.commons.util.Rnd;
import l2s.gameserver.Announcements;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.htm.HtmTemplates;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.utils.Transaction.TransactionBankCount;
import l2s.gameserver.utils.Transaction.TransactionBankDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;

//歡迎界面購買物品
public final class MyUtilsFunction
{
	private static final Logger _log = LoggerFactory.getLogger(Files.class);
	
	private MyUtilsFunction()
	{}
	//60 90957,1;49518,1;29010,1;29023,1;29024,1
	static final String[][] ItemId = {
			// 第一個是幣數量  第二個是物品及數量  第三個是等級限制 第四個是購買次數 輸入999 表示可買999 也等於無限次數了
			{ "68", "29520,800", "85", "1" }, //這一個是鎖引0
			{ "10", "48400,10", "85", "10" },//這一個是鎖引1
			{ "30", "70758,1", "85", "999" },//這一個是鎖引2
			{ "30", "88823,1", "85", "999" },//這一個是鎖引3
			{ "90", "88824,1", "85", "999" },//這一個是鎖引4
			{ "15", "88889,1;70754,3;71124,3;90138,5;90963,3", "85", "1" },//這一個是鎖引5
	};
	private static final int MemberConis = 88888;//會員幣編號
	public static void onBypassFeedback(Player player,String inputString)
	{
		final String[] buypassOptions = inputString.split(" ");
		String page = "welcome.htm";
		if(buypassOptions[buypassOptions.length - 1].contains("htm"))
		{
			page = buypassOptions[buypassOptions.length - 1];
		}
		//BuyItems 20 這一個是傳進來的參數 表示
		//bypass -h MyUtils_BuyItems 1 welcome.htm
		if(inputString.startsWith("BuyItems"))
		{
			ItemTemplate tmp = ItemHolder.getInstance().getTemplate(MemberConis);

			int index = Integer.parseInt(buypassOptions[1]);
			int money = Integer.parseInt(ItemId[index][0]); //取出第一個值 假設為"60"
			String items = ItemId[index][1];//取出第二個值假設為 "90957,1;49518,1;29010,1;29023,1;29024,1"

			int level = Integer.parseInt(ItemId[index][2]); //取出等級
			int count = Integer.parseInt(ItemId[index][3]); //取出購買次數

			if(player.getLevel() > level)//超過20級  但20級可以買
			{
				player.sendMessage("超过" + level + "级不可购买。");
				return;
			}
			if(!checkCanBuy(player, index, count))
			{
				player.sendMessage("已买过 限定" + count + "次。");
				return;
			}
			if(!ItemFunctions.deleteItem(player, MemberConis, money))
			{
				player.sendMessage(tmp.getName() + "数量不足无法购买。");
				return;
			}
			
			if(giveItem(player, items))
			{
				player.sendMessage("购买成功。");
				insertBuyItem(player, index);
				showPage(player, page);//傳回頁面
			}
			else
			{
				player.sendMessage("购买失败。");
			}
		}
		//bypass -h MyUtils_ShowView_Info.htm  指令要這樣子寫，預設就會傳到 viewself目錄下的XXX.htm
		else if(inputString.startsWith("ShowView_Welcome"))
		{
			showPage(player, page);
		}
		else if(inputString.startsWith("GetMemberCoin"))
		{
			//bypass -h MyUtils_GetMemberCoin
			int memberCoins = CheckMemberHaveConis(player);
			HtmTemplates tpls = HtmCache.getInstance().getTemplates("member/GetMemberCoin.htm", player);
			String html = tpls.get(0);
			
			if(buypassOptions.length == 2)
			{
				int getCoins = Integer.parseInt(buypassOptions[1]);
				;
				if(getCoins > memberCoins)
				{
					String tmp = tpls.get(3);
					html = html.replace("<?content?>", tmp);
					sendHtmlMessage(player, html);
					return;
				} //CheckMemberHaveConis
				if(UpdateMemberConis(player, -getCoins))
				{
					ItemFunctions.addItem(player, 88888, getCoins, true);
				}
				memberCoins = memberCoins - getCoins;
			}

			if(memberCoins == 0)
			{
				String tmp = tpls.get(2);
				html = html.replace("<?content?>", tmp);
				sendHtmlMessage(player, html);
				return;
			}
			String tmp = tpls.get(4);
			tmp = tmp.replace("<$memberCoins$>", memberCoins + "");
			html = html.replace("<?content?>", tmp);
			sendHtmlMessage(player, html);
		}
		//扭蛋
		else if(inputString.startsWith("CreateItems"))
		{
			//bypass -h MyUtils_CreateItems
			
			if(player.getInventory().getCountOf(29520) <16 || player.getLevel() <=75)
			{
				player.sendMessage("所需「魔力币」不足 16 个或角色等級小於76級");;
				return;
			}
			if (!player.getInventory().destroyItemByItemId(29520, 16)) {
				player.sendMessage("扣除失败，请联系管理员！");
			}
			int itemNum = RndGetItem();;
			ItemInstance item = ItemFunctions.createItem(itemNum);
			String html = HtmCache.getInstance().getHtml("merchant/34087-1.htm", player);
			html = html.replace("<$items$>", item.getName());
			HtmlMessage msg = new HtmlMessage(5);
			msg.setHtml(html);
			player.sendPacket(msg);
			player.sendActionFailed();
			int cjiditem = 1;
			//給物品
			if(itemNum == 88801 || itemNum == 88802 || itemNum == 88806 || itemNum == 88807 )
			{
				cjiditem = 1;
				//cjiditem = 10;
			}
			if(itemNum == 88803 || itemNum == 88804 || itemNum == 88809 || itemNum == 88810 || itemNum == 90020 )
			{
				cjiditem = 1;
				//cjiditem = 5;
			}
			ItemFunctions.addItem(player, itemNum, cjiditem, true);
			ItemFunctions.addItem(player, 4037, 1, true);
			if(itemNum == 70005 || itemNum == 90020 || itemNum == 4180 || itemNum == 88814 || itemNum == 88819 || itemNum == 88820 || itemNum == 88813 || itemNum == 88818 || itemNum == 4185 || itemNum == 88803 || itemNum == 88804 || itemNum == 88810 || itemNum == 4182 || itemNum == 4188 || itemNum == 6660 || itemNum == 90823 || itemNum == 90824 || itemNum == 6662 || itemNum == 6661 || itemNum == 91602 || itemNum == 90822 || itemNum == 90763 || itemNum == 49580 || itemNum == 90992 || itemNum == 91550)
			{
			Announcements.announceToAll(new SystemMessage(7499).addName(player).addItemName(item.getItemId()));
			}
		}
		//金幣抽獎扭蛋
		else if(inputString.startsWith("FreeCreateItems"))
		{
			//bypass -h MyUtils_FreeCreateItems
			if(player.getInventory().getCountOf(57) <3690000 || player.getLevel() <=75)
			{
				player.sendMessage("所需「金幣」不足 369 萬或角色等級小於76級");;
				return;
			}
			if (!player.getInventory().destroyItemByItemId(57, 3690000)) {
				player.sendMessage("扣除失败，请联系管理员！");
			}
			int itemNum = RndGetItem1();;
			ItemInstance item = ItemFunctions.createItem(itemNum);
			String html = HtmCache.getInstance().getHtml("merchant/34087-2.htm", player);
			html = html.replace("<$items$>", item.getName());
			HtmlMessage msg = new HtmlMessage(1);
			msg.setHtml(html);
			player.sendPacket(msg);
			player.sendActionFailed();
			int cjiditem = 1;
			//給物品
			if(itemNum == 88801 || itemNum == 88802 || itemNum == 88806 || itemNum == 88807 )
			{
				cjiditem = 1;
				//cjiditem = 6;
			}
			if(itemNum == 88803 || itemNum == 88804 || itemNum == 88809 || itemNum == 88810 || itemNum == 90020 )
			{
				cjiditem = 1;
				//cjiditem = 3;
			}
			ItemFunctions.addItem(player, itemNum, cjiditem, true);
			if(itemNum == 70005 || itemNum == 90020 || itemNum == 4180 || itemNum == 88814 || itemNum == 88819 || itemNum == 88820 || itemNum == 88813 || itemNum == 88818 || itemNum == 4185 || itemNum == 88803 || itemNum == 88804 || itemNum == 88810 || itemNum == 4182 || itemNum == 4188 || itemNum == 6660 || itemNum == 90823 || itemNum == 90824 || itemNum == 6662 || itemNum == 6661 || itemNum == 91602 || itemNum == 90822 || itemNum == 90763 || itemNum == 49580 || itemNum == 90992 || itemNum == 91550)
			{
			Announcements.announceToAll(new SystemMessage(7499).addName(player).addItemName(item.getItemId()));
			}
		}
		//抽检卷抽獎扭蛋
		else if(inputString.startsWith("jiangjuanItems"))
		{
			//bypass -h MyUtils_FreeCreateItems
			if(player.getInventory().getCountOf(48400) <1 || player.getLevel() <=75)
			{
				player.sendMessage("所需「抽奖券」不足 1 个或角色等級小於76級");;
				return;
			}
			if (!player.getInventory().destroyItemByItemId(48400, 1)) {
				player.sendMessage("扣除失败，请联系管理员！");
			}
			int itemNum = RndGetItem1();;
			ItemInstance item = ItemFunctions.createItem(itemNum);
			String html = HtmCache.getInstance().getHtml("merchant/34087-4.htm", player);
			html = html.replace("<$items$>", item.getName());
			HtmlMessage msg = new HtmlMessage(1);
			msg.setHtml(html);
			player.sendPacket(msg);
			player.sendActionFailed();
			int cjiditem = 1;
			//給物品
			if(itemNum == 88801 || itemNum == 88802 || itemNum == 88806 || itemNum == 88807 )
			{
				cjiditem = 1;
				//cjiditem = 10;
			}
			if(itemNum == 88803 || itemNum == 88804 || itemNum == 88809 || itemNum == 88810 || itemNum == 90020 )
			{
				cjiditem = 1;
				//cjiditem = 5;
			}
			ItemFunctions.addItem(player, itemNum, cjiditem, true);
			ItemFunctions.addItem(player, 4037, 1, true);
			if(itemNum == 70005 || itemNum == 90020 || itemNum == 4180 || itemNum == 88814 || itemNum == 88819 || itemNum == 88820 || itemNum == 88813 || itemNum == 88818 || itemNum == 4185 || itemNum == 88803 || itemNum == 88804 || itemNum == 88810 || itemNum == 4182 || itemNum == 4188 || itemNum == 6660 || itemNum == 90823 || itemNum == 90824 || itemNum == 6662 || itemNum == 6661 || itemNum == 91602 || itemNum == 90822 || itemNum == 90763 || itemNum == 49580 || itemNum == 90992 || itemNum == 91550)
			{
			Announcements.announceToAll(new SystemMessage(7499).addName(player).addItemName(item.getItemId()));
			}
		}
		//新手金币抽扭蛋
		else if(inputString.startsWith("shiwanchoujiang"))
		{
			//bypass -h MyUtils_FreeCreateItems
			if(player.getInventory().getCountOf(57) <680000)
			{
				player.sendMessage("所需「金幣」不足 68 萬");;
				return;
			}
			if (!player.getInventory().destroyItemByItemId(57, 680000)) {
				player.sendMessage("扣除失败，请联系管理员！");
			}
			int itemNum = RndGetItem5();;
			ItemInstance item = ItemFunctions.createItem(itemNum);
			String html = HtmCache.getInstance().getHtml("merchant/34087-9.htm", player);
			html = html.replace("<$items$>", item.getName());
			HtmlMessage msg = new HtmlMessage(1);
			msg.setHtml(html);
			player.sendPacket(msg);
			player.sendActionFailed();
			int cjiditem = 1;
			ItemFunctions.addItem(player, itemNum, cjiditem, true);
			if(itemNum == 70486 || itemNum == 48066 || itemNum == 70758 || itemNum == 70753 || itemNum == 70885 || itemNum == 49469 || itemNum == 71078 || itemNum == 48046 || itemNum == 6660 || itemNum == 6661 || itemNum == 6662)
			{
			Announcements.announceToAll(new SystemMessage(7498).addName(player).addItemName(item.getItemId()));
			}
		}
		//新手魔力币抽扭蛋
		else if(inputString.startsWith("liangxiaoyuechou"))
		{
			//bypass -h MyUtils_FreeCreateItems
			if(player.getInventory().getCountOf(29520) <5)
			{
				player.sendMessage("所需「魔力幣」不足 5 個");;
				return;
			}
			if (!player.getInventory().destroyItemByItemId(29520, 5)) {
				player.sendMessage("扣除失败，请联系管理员！");
			}

			int itemNum = RndGetItem5();;
			ItemInstance item = ItemFunctions.createItem(itemNum);
			String html = HtmCache.getInstance().getHtml("merchant/34087-9.htm", player);
			html = html.replace("<$items$>", item.getName());
			HtmlMessage msg = new HtmlMessage(1);
			msg.setHtml(html);
			player.sendPacket(msg);
			player.sendActionFailed();
			int cjiditem = 1;
			ItemFunctions.addItem(player, itemNum, cjiditem, true);
			if(itemNum == 70486 || itemNum == 48066 || itemNum == 70758 || itemNum == 70753 || itemNum == 70885 || itemNum == 49469 || itemNum == 71078 || itemNum == 48046 || itemNum == 6660 || itemNum == 6661 || itemNum == 6662)
			{
			Announcements.announceToAll(new SystemMessage(7498).addName(player).addItemName(item.getItemId()));
			}
		}
		//高級魔力币抽扭蛋
		else if(inputString.startsWith("gaojixiaoyuechou"))
		{
			//bypass -h MyUtils_FreeCreateItems
			if(player.getInventory().getCountOf(29520) <20 || player.getLevel() <=79)
			{
				player.sendMessage("所需「魔力币」不足 20 个或角色等級小於80級");;
				return;
			}
			//刪除
			if (!player.getInventory().destroyItemByItemId(29520, 20)) {
				player.sendMessage("扣除失败，请联系管理员！");
			}
			int itemNum = gaojiRndGetItem();;
			ItemInstance item = ItemFunctions.createItem(itemNum);
			String html = HtmCache.getInstance().getHtml("merchant/34087-12.htm", player);
			html = html.replace("<$items$>", item.getName());
			HtmlMessage msg = new HtmlMessage(1);
			msg.setHtml(html);
			player.sendPacket(msg);
			player.sendActionFailed();
			int cjiditem = 1;
			//給物品
			if(itemNum == 88801 || itemNum == 88806)
			{
				cjiditem = 50;
			}
			if(itemNum == 88802 || itemNum == 88807 )
			{
				cjiditem = 4;
			}
			if(itemNum == 49471 || itemNum == 49472)
			{
				cjiditem = 20;
			}
			if(itemNum == 48875)
			{
				cjiditem = 5;
			}
			ItemFunctions.addItem(player, itemNum, cjiditem, true);
			ItemFunctions.addItem(player, 4037, 1, true);
			if(itemNum == 48875 || itemNum == 44185 || itemNum == 44180 || itemNum == 44184 || itemNum == 44179 || itemNum == 88801 || itemNum == 88802 || itemNum == 88806 || itemNum == 88807 || itemNum == 49682 || itemNum == 91150  || itemNum == 91217 || itemNum == 91210 || itemNum == 91211 || itemNum == 91212 || itemNum == 80333 || itemNum == 80334 || itemNum == 80335)
			{
			Announcements.announceToAll(new SystemMessage(7499).addName(player).addItemName(item.getItemId()));
			}
		}
		//高級金币抽扭蛋
		else if(inputString.startsWith("gaojijinbichou"))
		{
			//bypass -h MyUtils_FreeCreateItems
			if(player.getInventory().getCountOf(57) <200000000 || player.getLevel() <=79)
			{
				player.sendMessage("所需「金幣」不足 2億 或角色等級小於80級");
				return;
			}
			if (!player.getInventory().destroyItemByItemId(57, 200000000)) {
				player.sendMessage("扣除失败，请联系管理员！");
			}
			int itemNum = gaojiRndGetItem();;
			ItemInstance item = ItemFunctions.createItem(itemNum);
			String html = HtmCache.getInstance().getHtml("merchant/34087-11.htm", player);
			html = html.replace("<$items$>", item.getName());
			HtmlMessage msg = new HtmlMessage(1);
			msg.setHtml(html);
			player.sendPacket(msg);
			player.sendActionFailed();
			int cjiditem = 1;
			//給物品
			if(itemNum == 48875)
			{
				itemNum = 49472;
			}
			if(itemNum == 88801 || itemNum == 88806)
			{
				cjiditem = 2;
			}
			if(itemNum == 88802 || itemNum == 88807 )
			{
				cjiditem = 1;
			}
			ItemFunctions.addItem(player, itemNum, cjiditem, true);
			if(itemNum == 48875 || itemNum == 49682 || itemNum == 91150  || itemNum == 91217 || itemNum == 91210 || itemNum == 91211 || itemNum == 91212 || itemNum == 80333 || itemNum == 80334 || itemNum == 80335)
			{
			Announcements.announceToAll(new SystemMessage(7499).addName(player).addItemName(item.getItemId()));
			}
		}
		//金幣抽材1倍料扭蛋
		else if(inputString.startsWith("cailiaoItems"))
		{
			//bypass -h MyUtils_FreeCreateItems
			if(player.getInventory().getCountOf(57) < 30000)
			{
				player.sendMessage("所需「金幣」不足 3 萬");;
				return;
			}
			if (!player.getInventory().destroyItemByItemId(57, 30000)) {
				player.sendMessage("扣除失败，请联系管理员！");
			}
			int itemNum = RndGetItem2();;
			ItemInstance item = ItemFunctions.createItem(itemNum);
			String html = HtmCache.getInstance().getHtml("merchant/34087-3.htm", player);
			html = html.replace("<$items$>", item.getName());
			HtmlMessage msg = new HtmlMessage(1);
			msg.setHtml(html);
			player.sendPacket(msg);
			player.sendActionFailed();
			int cjiditem = 10;
			//給物品
			if(itemNum == 5554 || itemNum == 1888 || itemNum == 5550 || itemNum == 4042 || itemNum == 1893 || itemNum == 1889 || itemNum == 1881 || itemNum == 1462 || itemNum == 2134 || itemNum == 5552 || itemNum == 5551 || itemNum == 4048 || itemNum == 5553 || itemNum == 1890 || itemNum == 1895 || itemNum == 1887 || itemNum == 4043 || itemNum == 4044 || itemNum == 1894 || itemNum == 1885 )
			{
				cjiditem = 1;
			}
			ItemFunctions.addItem(player, itemNum, cjiditem, true);
		}
		//金幣抽材料10倍抽
		else if(inputString.startsWith("shibeicailiao"))
		{
			//bypass -h MyUtils_FreeCreateItems
			if(player.getInventory().getCountOf(57) < 300000)
			{
				player.sendMessage("所需「金幣」不足 30 萬");;
				return;
			}
			if (!player.getInventory().destroyItemByItemId(57, 300000)) {
				player.sendMessage("扣除失败，请联系管理员！");
			}
			int itemNum = RndGetItem2();;
			ItemInstance item = ItemFunctions.createItem(itemNum);
			String html = HtmCache.getInstance().getHtml("merchant/34087-5.htm", player);
			html = html.replace("<$items$>", item.getName());
			HtmlMessage msg = new HtmlMessage(1);
			msg.setHtml(html);
			player.sendPacket(msg);
			player.sendActionFailed();
			int cjiditem = 100;
			//給物品
			if(itemNum == 5554 || itemNum == 1888 || itemNum == 5550 || itemNum == 4042 || itemNum == 1893 || itemNum == 1889 || itemNum == 1881 || itemNum == 1462 || itemNum == 2134 || itemNum == 5552 || itemNum == 5551 || itemNum == 4048 || itemNum == 5553 || itemNum == 1890 || itemNum == 1895 || itemNum == 1887 || itemNum == 4043 || itemNum == 4044 || itemNum == 1894 || itemNum == 1885 )
			{
				cjiditem = 10;
			}
			ItemFunctions.addItem(player, itemNum, cjiditem, true);
		}
		//金幣抽材料100倍抽
		else if(inputString.startsWith("yibaibeicailiao"))
		{
			//bypass -h MyUtils_FreeCreateItems
			if(player.getInventory().getCountOf(57) < 3000000)
			{
				player.sendMessage("所需「金幣」不足 300 萬");;
				return;
			}
			if (!player.getInventory().destroyItemByItemId(57, 3000000)) {
				player.sendMessage("扣除失败，请联系管理员！");
			}
			int itemNum = RndGetItem2();;
			ItemInstance item = ItemFunctions.createItem(itemNum);
			String html = HtmCache.getInstance().getHtml("merchant/34087-6.htm", player);
			html = html.replace("<$items$>", item.getName());
			HtmlMessage msg = new HtmlMessage(1);
			msg.setHtml(html);
			player.sendPacket(msg);
			player.sendActionFailed();
			int cjiditem = 1000;
			//給物品
			if(itemNum == 5554 || itemNum == 1888 || itemNum == 5550 || itemNum == 4042 || itemNum == 1893 || itemNum == 1889 || itemNum == 1881 || itemNum == 1462 || itemNum == 2134 || itemNum == 5552 || itemNum == 5551 || itemNum == 4048 || itemNum == 5553 || itemNum == 1890 || itemNum == 1895 || itemNum == 1887 || itemNum == 4043 || itemNum == 4044 || itemNum == 1894 || itemNum == 1885 )
			{
				cjiditem = 100;
			}
			ItemFunctions.addItem(player, itemNum, cjiditem, true);
		}
		//魔力币抽材料300倍抽
		else if(inputString.startsWith("xiaoyuebi300bei"))
		{
			//bypass -h MyUtils_FreeCreateItems
			if(player.getInventory().getCountOf(29520) < 10)
			{
				player.sendMessage("所需「魔力币」不足 10 个");;
				return;
			}
			if (!player.getInventory().destroyItemByItemId(29520, 10)) {
				player.sendMessage("扣除失败，请联系管理员！");
			}
			int itemNum = RndGetItem2();;
			ItemInstance item = ItemFunctions.createItem(itemNum);
			String html = HtmCache.getInstance().getHtml("merchant/34087-7.htm", player);
			html = html.replace("<$items$>", item.getName());
			HtmlMessage msg = new HtmlMessage(1);
			msg.setHtml(html);
			player.sendPacket(msg);
			player.sendActionFailed();
			int cjiditem = 3000;
			//給物品
			if(itemNum == 5554 || itemNum == 1888 || itemNum == 5550 || itemNum == 4042 || itemNum == 1893 || itemNum == 1889 || itemNum == 1881 || itemNum == 1462 || itemNum == 2134 || itemNum == 5552 || itemNum == 5551 || itemNum == 4048 || itemNum == 5553 || itemNum == 1890 || itemNum == 1895 || itemNum == 1887 || itemNum == 4043 || itemNum == 4044 || itemNum == 1894 || itemNum == 1885 )
			{
				cjiditem = 300;
			}
			ItemFunctions.addItem(player, itemNum, cjiditem, true);
		}
		else if(inputString.startsWith("DownLevel"))//bypass -h MyUtils_LevelDown
		{
			if(player.getLevel() < 40)//自己新加等級限制
			{
				player.sendMessage("等级不足無法回收经验");
				return;//自己新加等級限制
			}
			if(player.getLevel() > 90)//自己新加等級限制
			return;//自己新加等級限制
			int itemid = 0;
			int item57 = 0;
			int itemID57 = 0;
			long exp57 = 0;
		if(player.getLevel() >= 40&&player.getLevel() < 50){
			 itemid = 88891;
			 itemID57 = 88888;
			 item57 = 20;
			 exp57 = 500000;
		}
			if(player.getLevel() >= 50&&player.getLevel() < 60){
			 itemid = 88892;
			 itemID57 = 88888;
			 item57 = 50;
			 exp57 = 5000000;
		}
			if(player.getLevel() >= 60&&player.getLevel() < 70){
			 itemid = 88893;
			 itemID57 = 88888;
			 item57 = 100;
			 exp57 = 30000000;
		}
			if(player.getLevel() >= 70&&player.getLevel() < 80){
			 itemid = 88894;
			 itemID57 = 88888;
			 item57 = 150;
			 exp57 = 100000000;
		}
			if(player.getLevel() >= 80&&player.getLevel() < 90){
			 itemid = 88895;
			 itemID57 = 88888;
			 item57 = 200;
			 exp57 = 200000000;
		}
			if(!ItemFunctions.deleteItem(player, itemID57,item57 ))
			{
				player.sendMessage("所需道具不足無法回收经验");
				return;
			}
			long exp =  - exp57;
			player.addExpAndSp(exp, 0);
			ItemFunctions.addItem(player, itemid, 1);//自己新加道具給與
			player.broadcastPacket(new MagicSkillUse(player, player, 23128, 1, 1, 0));//這一個應該是升級的那一種發光特效。
			/* IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler("cfg");
			if(vch != null)
				vch.useVoicedCommand("cfg", player, ""); */
		}
		////bypass -h MyUtils_GetMemberCoin
		else if(inputString.startsWith("DownSp"))//降SP设定
		{
			if(player.getLevel() < 10)//自己新加等級限制
			{
				player.sendMessage("等级不足無法回收SP");
				return;//自己新加等級限制
			}
			if(player.getSp() < 10000)//自己新加等級限制
			{
				player.sendMessage("SP不足2万無法回收SP");
				return;//自己新加等級限制
			}
			if(!ItemFunctions.deleteItem(player, 57,1000 ))
			{
				player.sendMessage("金幣不足無法回收SP");
				return;
			}
			int Sp = - 10000;
			player.addExpAndSp(0, Sp);
			ItemFunctions.addItem(player, 49596, 1);//自己新加道具給與
			player.broadcastPacket(new MagicSkillUse(player, player, 23128, 1, 1, 0));//這一個應該是升級的那一種發光特效。
			/* IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler("cfg");
			if(vch != null)
				vch.useVoicedCommand("cfg", player, ""); */
		}//降SP设定
	}
	public static void transactionBank(Player player) {
		HtmTemplates tpls = HtmCache.getInstance().getTemplates("member/transactionBank.htm", player);
		String html = tpls.get(0);
		if (TransactionBankCount.exchangeRate == null) {
			BigDecimal read = TransactionBankCount.getInstance().read();
			html = html.replace("%gold%", numFormat(Long.parseLong(String.valueOf(read))));
		}else {
			html = html.replace("%gold%", numFormat(Long.parseLong(String.valueOf(TransactionBankCount.exchangeRate))));
		}
		html = html.replace("%procedureRate%", String.valueOf(Math.round(TransactionBankDao.procedureRate*100))+"%");
		sendHtmlMessage(player, html);
	}

	public static void chooseExchange(Player player, String param) {
		HtmTemplates tpls = HtmCache.getInstance().getTemplates("member/chooseExchange.htm", player);
		String html = tpls.get(0);
		html = html.replace("%exchangeKind%",param.equals("chooseGoldExchange") || param.equals("金币")?"\u91d1\u5e01":"新魔力币");

		/** 判断玩家拥有多少金币或者赞助币 来显示红色，当刷新的时候要考虑*/
		long countOfGold = player.getInventory().getCountOf(57);
		long countOfVipGold = player.getInventory().getCountOf(29520);
		long procedure = Math.round(TransactionBankCount.exchangeRate.doubleValue() * TransactionBankDao.procedureRate);
		// 如果是兑换金币
		if (param.equals("chooseGoldExchange") || param.equals("\u91d1\u5e01")) {
			html = html.replace("%exchangeGoldKind%",numFormat(Long.parseLong(String.valueOf(TransactionBankCount.exchangeRate))));
			// 如果玩家赞助币币小于30 显示红色
			if (countOfVipGold >=0 && countOfVipGold < 30L) {
				html = html.replaceFirst("%consumeGoldKind%","<font color=\"FF0000\">30</font>");
			}
			html = html.replace("%consume%","新魔力币");
			html = html.replace("%consumeGoldKind%","30");
			html = html.replace("%getTheGold%","应得："+numFormat(TransactionBankCount.exchangeRate.longValue() - procedure));
		}else if(param.equals("chooseVipGoldExchange") || param.equals("新魔力币")){
			html = html.replace("%exchangeGoldKind%","30");
			// 如果玩家金币小于汇率 显示红色
			if(countOfGold >=0 &&  BigDecimal.valueOf(countOfGold).compareTo(TransactionBankCount.exchangeRate) == -1){
				html = html.replaceFirst("%consumeGoldKind%","<font color=\"FF0000\">"+numFormat(Long.parseLong(String.valueOf(TransactionBankCount.exchangeRate)))+"</font>");
			}
			html = html.replace("%consume%","金");
			html = html.replace("消耗:","汇率:");
			html = html.replace("%consumeGoldKind%",numFormat(Long.parseLong(String.valueOf(TransactionBankCount.exchangeRate))));
			// 最终消耗的金币 新增
			if(countOfGold <TransactionBankCount.exchangeRate.longValue() + procedure){
				html = html.replace("%getTheGold%","实际消耗：<font color=\"FF0000\""+numFormat(TransactionBankCount.exchangeRate.longValue() + procedure)+"</font>");
			}else if(countOfGold >=0 && countOfGold > TransactionBankCount.exchangeRate.longValue() + procedure){
				html = html.replace("%getTheGold%","实际消耗："+numFormat(TransactionBankCount.exchangeRate.longValue() + procedure));
			}
		}
		html = html.replace("%procedureRateGold%",numFormat(procedure));
		html = html.replace("%ownVipGold%","新魔力币"+numFormat(countOfVipGold));
		html = html.replace("%ownGold%","金币"+numFormat(countOfGold));
		sendHtmlMessage(player, html);
	}
	// 交易行确认交易
	public static void defineExchange(Player player, String param) {
		HtmTemplates tpls = HtmCache.getInstance().getTemplates("member/defineExchange.htm", player);
		String html = tpls.get(0);
		StringBuilder builder = new StringBuilder(html);
		long countOfGold = player.getInventory().getCountOf(57);
		long countOfVipGold = player.getInventory().getCountOf(29520);

		if (param != null && param.equals("chooseGoldExchange")) {
			// 删除金币不足的页面
			int startIndexOf = html.indexOf("<$goldContents$>");
			int lastIndexOf = html.lastIndexOf("<$VIPContents$>");
			StringBuilder stringBuilder = builder.delete(startIndexOf, lastIndexOf);
			html = stringBuilder.toString();
			html = html.replace("<$VIPContents$>","");
			html = html.replace("<$contents$>","<button value=\"前往充值\" action=\"bypass -h htmbypass_bot.\" width=120 height=20 back=\"Button_DF_Down\" fore=\"Button_DF\">");
			html = html.replace("%defineTitle%","新魔力币");
			player.sendMessage("您的新魔力币不足");
		}else if(param != null && param.equals("chooseVipGoldExchange")){
			int startIndexOf = html.indexOf("<$VIPContents$>");
			int lastIndexOf = html.lastIndexOf("<$contents$>");
			StringBuilder stringBuilder = builder.delete(startIndexOf, lastIndexOf);
			html = stringBuilder.toString();
			html = html.replace("<$contents$>","<table><tr><td>" +
					"<button value=\"确认\" action=\"bypass -h htmbypass_bot.chooseExchange\" width=120 height=20 back=\"Button_DF_Down\" fore=\"Button_DF\">" +
					"</td><td>"+
					"<button value=\"取消\" action=\"bypass -h htmbypass_bot.chooseExchange\" width=120 height=20 back=\"Button_DF_Down\" fore=\"Button_DF\">"+
					"</td></tr></table>");
			html = html.replace("<$goldContents$>","");
			html = html.replace("%defineTitle%","\u91d1\u5e01");
			player.sendMessage("您的金币不足");
		}
		html = html.replace("%ownVipGold%","新魔力币"+numFormat(countOfVipGold));
		html = html.replace("%ownGold%","金币"+numFormat(countOfGold));
		sendHtmlMessage(player, html);
	}

	public static String numFormat(long num){
		DecimalFormat decimalFormat = new DecimalFormat("#,###.##");
		String format = decimalFormat.format(num);
		if(String.valueOf(num).length()>15){
			return format.substring(0, 19);
		}else {
			return format;
		}
	}

	private static boolean giveItem(Player player, String inputString)
	{
		String[] suite = inputString.split(";");
		ItemTemplate item;
		for(String myId : suite)
		{
			String[] obj = myId.split(",");
			int id = Integer.parseInt(obj[0]);
			int count = Integer.parseInt(obj[1]);
			item = ItemHolder.getInstance().getTemplate(Integer.parseInt(obj[0]));
			if(item != null)
			{
				ItemFunctions.addItem(player, id, count, true);
			}
			else
			{
				_log.warn(player.getName() + "花費會員幣購買物品| " + inputString + "| 有不存在id" + id);
				return false;
			}
		}
		return true;
	}
	public static int CheckMemberHaveConis(Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		int Counts = 0;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT sum(point) as pt FROM _game_acc where account = ?");
			statement.setString(1, player.getAccountName());
			rset = statement.executeQuery();
			if (rset.next())
			{
				Counts = rset.getInt("pt");
			}
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return Counts;
	}
	public static boolean UpdateMemberConis(Player player,int Counts)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			
			statement = con.prepareStatement("INSERT INTO _game_acc (point, account ,createtime) VALUES (?,?, unix_timestamp(now()))");
			statement.setInt(1, Counts);
			statement.setString(2, player.getAccountName());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}
	private static void insertBuyItem(Player player, int index)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO _player_buy_gift (account ,num ,buytime) VALUES(?,?,?)");
			statement.setString(1, player.getAccountName());
			statement.setInt(2, index);
			statement.setLong(3, System.currentTimeMillis() / 1000);
			statement.execute();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	private static boolean checkCanBuy(Player player, int index ,int counts)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		int times = 0;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT count(*) as cnt  FROM _player_buy_gift where account = ? and num = ?");
			statement.setString(1, player.getAccountName());
			statement.setInt(2, index);
			rset = statement.executeQuery();
			if(rset.next())
			{
				times =  rset.getInt("cnt");;
			}
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return counts > times;
	}
	private static void showPage(Player player, String page)
	{
		String errs = HtmCache.getInstance().getHtml(page, player);
		HtmlMessage msg = new HtmlMessage(5);
		msg.setHtml(errs);
		player.sendPacket(msg);
		player.sendActionFailed();
	}
	private static void sendHtmlMessage(Player player,String html)
	{
		HtmlMessage msg = new HtmlMessage(5);
		msg.setHtml(html);
		player.sendPacket(msg);
		player.sendActionFailed();	
	}
	/*扭蛋--贊助幣抽獎內容*/
	private static int RndGetItem()
	{
		double R = Rnd.get() * 100;
		// d50  c30  b15   a4   boss1
		if(R < 60) // 60% 機率 取得D
		{
			return GroupD[Rnd.get(GroupD.length)];
		}
		else if(R < 80) // 80-50 = 20  所以20%機率取得 C
		{
			return GroupC[Rnd.get(GroupC.length)];
		}
		else if(R < 95) // 95-85 = 15  所以15%機率取得 DD
		{
			return GroupDD[Rnd.get(GroupDD.length)];
		}
		else if(R < 98) // 98-95 = 3  所以3%機率取得 CC
		{
			return GroupCC[Rnd.get(GroupCC.length)];
		}
		else if(R < 99.5) // 99-98 = 1.5  所以1%機率取得 DDD
		{
			return GroupDDD[Rnd.get(GroupDDD.length)];
		}
		else if(R < 100) // 100-99 = 0.5  所以1%機率取得 CCC
		{
			return GroupCCC[Rnd.get(GroupCCC.length)];
		}
		return GroupD[Rnd.get(GroupD.length)];
	}
	private static int[] GroupD =
		{
			49756,//绣布
			70633,//蓝色王朝变身卷轴
			13016,//自由传送卷轴
		};
	private static int[] GroupC =
		{
			/* 49693,//5000万经验跳跃药水 */
			90917,//1000万经验卷
			90017,//A武器材料礼盒
			49762,//A防具礼盒
			49763,//A首饰礼盒
			91013,//A頂武器製作材料
			//88806,王朝装备核心
			//88801,王朝武器装备核心
			49756,//绣布
			70005,//封印古文5级
			90020//最高级的染料
		};
	private static int[] GroupDD =
		{
			91013,//A頂武器製作材料
			49762,//A防具礼盒
			90017,//A武器材料礼盒
			49762,//A防具礼盒
			49763,//A首饰礼盒
			49763,//A首饰礼盒
			49472,//S76防具材料礼盒
			49471,//S76武器材料礼盒
			/* 49693,//5000万经验跳跃药水
			49694,//1亿经验跳跃药水 */
			//44179,王朝武器制作卷轴
			//44184,王朝装备制作卷轴
			//44179,王朝武器制作卷轴
			//44184,王朝装备制作卷轴
			//88806,王朝装备核心
			//88801,王朝武器核心
			//88801,王朝武器核心
			//88801,王朝武器核心
			//88807,天命装备核心
			//88807,天命装备核心
			90020//最高级的染料
		};
	private static int[] GroupCC =
		{
			//88802,伊克鲁斯武器核心 
			//88802,伊克鲁斯武器核心
			//88802,伊克鲁斯武器核心
			//88807,天命装备核心
			//88807,天命装备核心
			//49694,1亿经验跳跃药水
			//44180,//伊克鲁斯武器制作卷轴
			//44180,//伊克鲁斯武器制作卷轴
			//44185,//天命装备制作卷轴
			//44185,//天命装备制作卷轴
			49472,//S76防具材料礼盒
			49471,//S76武器材料礼盒
			6660,//蚂蚁戒指
			90823,//封印的蚂蚁人偶
			90824,//封印的奥尔芬人偶
			//88817,王朝装备礼盒
			6662,//核心戒指
			//36416,补天神石
			6661//奥尔芬耳环
		};
	private static int[] GroupDDD =
		{
			//88803,薄暮武器核心
			//88809,薄暮贵族装备核心
			//88803,薄暮武器核心
			//88809,薄暮贵族装备核心
			/* 49694,//1亿经验跳跃药水
			49695,//2亿经验跳跃药水 */
			//44181,薄暮武器制作卷轴
			//44187,薄暮贵族装备制作卷轴
			//44181,薄暮武器制作卷轴
			//44187,薄暮贵族装备制作卷轴
			//88812,王朝武器礼盒
			91602,//封印的XX8人偶
			90822,//封印的巴温人偶
			90763,//札肯耳环
			49580,//巴温戒指
			90992,//安塔瑞斯耳环
			//88804,佩里尔武器核心
			//88810,博佩斯装备核心
			//44182,佩里尔武器制作卷轴
			//44188,博佩斯装备制作卷轴
			//36413,玄鐵
			91550//XX8项链
		};
	private static int[] GroupCCC =
		{
			//88804,佩里尔武器核心
			//88804,佩里尔武器核心
			//88804,佩里尔武器核心
			49682,//巴温的灵魂
			49853//傲慢护身符书
			//88803,薄暮武器核心
			//88810,博佩斯装备核心
			//88810,博佩斯装备核心
			//88810,博佩斯装备核心
			//90273,5亿经验跳跃药水
			//44182,佩里尔武器制作卷轴
			//44182,佩里尔武器制作卷轴
			//44182,佩里尔武器制作卷轴
			//44188,博佩斯装备制作卷轴
			//44188,博佩斯装备制作卷轴
			//36414,龙之印记
			//44188博佩斯装备制作卷轴
		};
/*扭蛋--金子幣抽獎內容*/
	private static int RndGetItem1()
	{
		double R = Rnd.get() * 100;
		// d50  c30  b15   a4   boss1
		if(R < 60) // 50% 機率 取得D
		{
			return GroupB[Rnd.get(GroupB.length)];
		}
		else if(R < 80) // 80-60 = 20  所以30%機率取得 C
		{
			return GroupA[Rnd.get(GroupA.length)];
		}
		else if(R < 95) // 95-80 = 15  所以30%機率取得 C
		{
			return GroupBB[Rnd.get(GroupBB.length)];
		}
		else if(R < 98) // 98-95 = 3  所以30%機率取得 C
		{
			return GroupAA[Rnd.get(GroupAA.length)];
		}
		else if(R < 99.9) // 99.5-98 = 1.9  所以30%機率取得 C
		{
			return GroupBBB[Rnd.get(GroupBBB.length)];
		}
		else if(R < 100) // 80-50 = 0.1  所以30%機率取得 C
		{
			return GroupAAA[Rnd.get(GroupAAA.length)];
		}
		return GroupD[Rnd.get(GroupD.length)];
	}
	private static int[] GroupB =
		{
			70633,//蓝色王朝变身卷轴
			13016//自由传送卷轴
		};
	private static int[] GroupA =
		{
			90917,//1000万经验卷
			90017,//A武器材料礼盒
			49762,//A防具礼盒
			49763,//A首饰礼盒
			49756,//绣布
			49756,//绣布
			70005//封印古文5级
		};
	private static int[] GroupBB =
		{
			49762,//A防具礼盒
			49762,//A防具礼盒
			49763,//A首饰礼盒
			49763,//A首饰礼盒
			91013,//A頂武器製作材料
			//44179,王朝武器制作卷轴
			//88806,王朝装备核心
			90020//最高级的染料
		};
	private static int[] GroupAA =
		{
			//88806,王朝装备核心
			//44187,薄暮装备制作卷轴
			//88801,王朝武器装备核心
			//88807,天命装备核心
			91013,//A頂武器製作材料
			49472,//S76防具材料礼盒
			//44185天命装备制作卷轴
		};
	private static int[] GroupBBB =
		{
			/* 49695,//2亿经验跳跃药水 */
			//88802,一路克斯武器核心
			//88807,天命装备核心
			//88809,薄暮装备核心
			//44187,薄暮装备制作卷轴
			6660,//蚂蚁戒指
			90823,//封印的蚂蚁人偶
			90824,//封印的奥尔芬人偶
			6661//奥尔芬耳环
		};
	private static int[] GroupAAA =
		{
			//88809,薄暮装备核心
			//88807,天命装备核心
			49852,//傲慢护身符书
			/* 90273,//5亿经验跳跃药水 */
			//44188,博佩斯装备制作卷轴
			90763,//札肯耳环
			90992,//安塔瑞斯耳环
			91602,//封印的XX8人偶
			90822,//封印的巴温人偶
			49580,//巴温戒指
			91550//XX8项链
			//88817王朝装备礼盒
		};
/*扭蛋--高級贊助幣抽獎內容*/
	private static int gaojiRndGetItem()
	{
		double R = Rnd.get() * 100;
		// d50  c30  b15   a4   boss1
		if(R < 60) // 60% 機率 取得D
		{
			return gjGroupD[Rnd.get(gjGroupD.length)];
		}
		else if(R < 80) // 80-60 = 20  所以20%機率取得 C
		{
			return gjGroupC[Rnd.get(gjGroupC.length)];
		}
		else if(R < 95) // 95-80 = 15  所以15%機率取得 DD
		{
			return gjGroupDD[Rnd.get(gjGroupDD.length)];
		}
		else if(R < 98) // 95-87 = 3  所以3%機率取得 CC
		{
			return gjGroupCC[Rnd.get(gjGroupCC.length)];
		}
		else if(R < 99.8) // 99-98 = 1.8  所以1%機率取得 DDD
		{
			return gjGroupDDD[Rnd.get(gjGroupDDD.length)];
		}
		else if(R < 100) // 100-99.8 = 0.2  所以1%機率取得 CCC
		{
			return gjGroupCCC[Rnd.get(gjGroupCCC.length)];
		}
		return GroupD[Rnd.get(GroupD.length)];
	}
	private static int[] gjGroupD =
		{
			91158,//梅芙的強化藥劑
			49518,//海賊果實
			90520,//火龍卷軸
			13016//自由传送卷轴
		};
	private static int[] gjGroupC =
		{
			49472,//S76防具材料礼盒
			25158,//S防具強化輔助
			91157,//梅芙的強化藥劑
			90020//最高级的染料
		};
	private static int[] gjGroupDD =
		{
			49472,//S76防具材料礼盒
			49471,//S76武器材料礼盒
			25188,//S祝福的防具強化輔助
			90020//最高级的染料
		};
	private static int[] gjGroupCC =
		{
			88801,//王朝核心
			88802,//
			88806,//王朝核心
			88807,//
			44179,
			44180,
			44184,
			44185,
			48875,
			90823,//封印的蚂蚁人偶
			91150,//+10的隨機人偶
			91217,//+10的隨機頭飾
			90824//封印的奥尔芬人偶
		};
	private static int[] gjGroupDDD =
		{
			91210,//沙哈斗篷
			91211,//沙哈斗篷
			91212,//沙哈斗篷
			48875,
			91602,//封印的XX8人偶
			90822,//封印的巴温人偶
			49682//巴温的灵魂
		};
	private static int[] gjGroupCCC =
		{
			80333,//龍之首飾
			80334,//龍之首飾
			80335//龍之首飾	
		};
/*扭蛋--基礎材料抽獎內容*/
	private static int RndGetItem2()
	{
		double R = Rnd.get() * 100;
		// d50  c30  b15   a4   boss1
		if(R < 100) // 50% 機率 取得D
		{
			return GroupBOSS[Rnd.get(GroupBOSS.length)];
		}
		return GroupD[Rnd.get(GroupD.length)];
	}
	private static int[] GroupBOSS =
		{
			1864,//树枝
			1866,//软皮
			1868,//线
			1867,//动物的皮
			1870,//煤矿
			1871,//木炭
			1865,//研磨级
			1872,//动物的骨头
			1869,//钢铁矿
			1873,//银块
			1874,//奥里哈鲁根原石
			1876,//米索利原石
			1877,//金刚石块
			1875,//纯石
			4043,//亚索
			4039,//粘黏剂
			4042,//恩妮雅
			4044,//梭子
			4041,//强化剂
			4040,//润滑剂
			5554,
			1888,
			5550,
			4042,
			1893,
			1889,
			1881,
			5552,
			5551,
			4048,
			5553,
			1890,
			1895,
			1887,
			4043,
			4044,
			1894,
			1885
		};
	/*--扭蛋*/
	/*扭蛋--新手抽獎內容*/
	private static int RndGetItem5()
	{
		double R = Rnd.get() * 100;
		if(R < 60) // 60% 機率 取得D
		{
			return GroupF[Rnd.get(GroupF.length)];
		}
		else if(R < 80) // 80-50 = 20  所以20%機率取得 C
		{
			return GroupT[Rnd.get(GroupT.length)];
		}
		else if(R < 95) // 95-85 = 15  所以15%機率取得 DD
		{
			return GroupFT[Rnd.get(GroupFT.length)];
		}
		else if(R < 98) // 98-95 = 3  所以3%機率取得 CC
		{
			return GroupFTFT[Rnd.get(GroupFTFT.length)];
		}
		else if(R < 99.5) // 99-98 = 1.5  所以1%機率取得 DDD
		{
			return GroupFFTT[Rnd.get(GroupFFTT.length)];
		}
		else if(R < 100) // 100-99 = 0.5  所以1%機率取得 CCC
		{
			return GroupFTFTFT[Rnd.get(GroupFTFTFT.length)];
		}
		return GroupF[Rnd.get(GroupF.length)];
	}
	private static int[] GroupF =
		{
			952,//C级防具强化卷
			70633,//蓝色王朝变身卷轴
			13016,//自由传送卷轴
			952,//C级防具强化卷
			951,//C级武器强化卷
			951//C级武器强化卷
		};
	private static int[] GroupT =
		{
			49779,//10万经验卷
			70000,//封印古文阶段1
			70486//LV1宝石礼盒
		};
	private static int[] GroupFT =
		{
			70486,//LV1宝石礼盒
			49684,//护身符结晶
			70753,//伊娃头饰强化
			70885,//斗篷强化
			49469,//坠饰研磨
			71078,//权能强化
			48046,//糊精强化
			70486//LV1宝石礼盒

		};
	private static int[] GroupFTFT =
		{
			48066,//星座糊精礼盒
			70758,//头饰礼盒
			70753,//伊娃头饰强化
			70885,//斗篷强化
			49469,//坠饰研磨
			71078,//权能强化
			48046//糊精强化
		};
	private static int[] GroupFFTT =
		{
			48066,//星座糊精礼盒
			70758,//头饰礼盒
			48066,//星座糊精礼盒
			70758//头饰礼盒

		};
	private static int[] GroupFTFTFT =
		{

			6660,//蚂蚁戒指
			6661,//奥尔芬耳环
			6662//核心戒指
		};
}