package npc.model;

import l2s.commons.collections.MultiValueSet;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.data.xml.holder.MultiSellHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;
import org.apache.commons.lang3.ArrayUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 * 潛能石
 **/
public class StoneInstance extends NpcInstance
{
	public StoneInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public void showChatWindow(Player player, int val, boolean firstTalk, Object... replace)
	{
		if(val == 0)
		{
			showChatWindow(player, "default/stone/" + getNpcId() + ".htm", firstTalk);
		}
		else
		{
			showChatWindow(player, "default/stone/" + getNpcId() + "-" + val + ".htm", firstTalk);
		}
	}

	private String stone[] = 
	{
		"黃晶潛能石",
		"紅晶潛能石",
		"粉晶潛能石",
		"紫晶潛能石",
		"藍晶潛能石"
	};
	//	黃晶潛能物理攻擊	1
	//	黃晶潛能魔法攻擊	2
	//	黃晶潛能物理致命高傷害	3
	//	黃晶潛能魔法致命高傷害	4
	//	紅晶潛能石HP/CP	5
	//	紅晶潛能石反傷	6
	//	紅晶潛能石減少反傷	7
	//	粉晶潛能石物理防禦	8
	//	粉晶潛能石魔法防禦	9
	//	粉晶潛能石減傷	10
	//	紫晶潛能石PVP攻擊	11
	//	藍晶潛能石PVP攻擊	12

	private int bodyPart[] = 
	{ 
		Inventory.PAPERDOLL_HEAD,       //頭        0
		Inventory.PAPERDOLL_CHEST,      //上衣      1
		Inventory.PAPERDOLL_GLOVES,     //手        2
		Inventory.PAPERDOLL_FEET,       //鞋        3
		Inventory.PAPERDOLL_BELT,       //腰帶      4
		Inventory.PAPERDOLL_NECK,       //項鍊      5
		Inventory.PAPERDOLL_LBRACELET,	//左手鐲	6
		Inventory.PAPERDOLL_RBRACELET,  //右手鐲    7
		Inventory.PAPERDOLL_BROOCH,     //胸針      8
	};
	@Override
	public void onMenuSelect(Player player, int ask, long reply, int state)
	{
		if(ask == 0)//	黃晶潛能物理攻擊	1
		{
			if(reply == 1 || reply == 2 || reply == 3 || reply == 8)//	上衣、手、鞋、胸針
			{
				showHtml(player, stone[ask], (int) reply);
			}
		}
		else if(ask == 1)//紅晶潛能石
		{
			if(reply == 0 || reply == 1 || reply == 5 || reply == 6 || reply == 7)//	頭、衣、項鍊、左/右手鐲
			{
				showHtml(player, stone[ask], (int) reply);
			}
		}
		else if(ask == 2)//粉晶潛能石
		{
			if(reply == 0 || reply == 4 || reply == 5 || reply == 6 || reply == 7)//	頭、腰帶、項鍊、左/右手鐲
			{
				showHtml(player, stone[ask], (int) reply);
			}
		}
		else if(ask == 3)//紫晶潛能石
		{
			if(reply == 2 || reply == 3 || reply == 4 || reply == 8)//	手、鞋、腰帶、胸針
			{
				showHtml(player, stone[ask], (int) reply);
			}
		}
		else if(ask == 4)//藍晶潛能石
		{
			showHtml(player, stone[ask], (int) reply);
		}
		else if(ask == 101)//黃晶潛能石-物理攻擊-衣、手、鞋和胸針
		{
			if(reply == 1 || reply == 2 || reply == 3 || reply == 8)
			{
				showHtml_Add(player, stone[0], 1, (int) reply);
			}
		}
		else if(ask == 102)//黃晶潛能石-魔法攻擊
		{
			if(reply == 1 || reply == 2 || reply == 3 || reply == 8)
			{
				showHtml_Add(player, stone[0], 2, (int) reply);
			}
		}
		else if(ask == 103)//黃晶潛能石-物理致命傷害
		{
			if(reply == 1 || reply == 2 || reply == 3 || reply == 8)
			{
				showHtml_Add(player, stone[0], 3, (int) reply);
			}
		}
		else if(ask == 104)//黃晶潛能石-魔法致命傷害
		{
			if(reply == 1 || reply == 2 || reply == 3 || reply == 8)
			{
				showHtml_Add(player, stone[0], 4, (int) reply);
			}
		}
		else if(ask == 105)//紅晶潛能石-HP/CP-頭、衣、項鍊、左/右手鐲
		{
			if(reply == 0 || reply == 1 || reply == 5 || reply == 6 || reply == 7)
			{
				showHtml_Add(player, stone[1], 1, (int) reply);
			}
		}
		else if(ask == 106)//紅晶潛能石-反傷
		{
			if(reply == 0 || reply == 1 || reply == 5 || reply == 6 || reply == 7)
			{
				showHtml_Add(player, stone[1], 2, (int) reply);
			}
		}
		else if(ask == 107)//紅晶潛能石-減反傷
		{
			if(reply == 0 || reply == 1 || reply == 5 || reply == 6 || reply == 7)
			{
				showHtml_Add(player, stone[1], 3, (int) reply);
			}
		}
		else if(ask == 108)//粉晶潛能石-物理防禦-頭、腰帶、項鍊、左/右手鐲
		{
			if(reply == 0 || reply == 4 || reply == 5 || reply == 6 || reply == 7)
			{
				showHtml_Add(player, stone[2], 1, (int) reply);
			}
		}
		else if(ask == 109)//粉晶潛能石-魔法防禦
		{
			if(reply == 0 || reply == 4 || reply == 5 || reply == 6 || reply == 7)
			{
				showHtml_Add(player, stone[2], 2, (int) reply);
			}
		}
		else if(ask == 110)//粉晶潛能石-減傷
		{
			if(reply == 0 || reply == 4 || reply == 5 || reply == 6 || reply == 7)
			{
				showHtml_Add(player, stone[2], 3, (int) reply);
			}
		}
		else if(ask == 111)//紫晶潛能石-手、鞋、腰帶和胸針
		{
			if(reply == 2 || reply == 3 || reply == 4 || reply == 8)
			{
				showHtml_Add(player, stone[3], 1, (int) reply);
			}
		}
		else if(ask == 112)//藍晶潛能石
		{
			showHtml_Add(player, stone[4], 1, (int) reply);
		}
		else if(ask == 36706)
		{
			//<Button ALIGN=LEFT ICON="NORMAL" action="bypass -h menu_select?ask=36706&reply=1">升級</Button>
			if(reply == 1 )
			{
				MultiSellHolder.getInstance().SeparateAndSend(50000001, player, 0);
			}
			else if (reply == 2)
			{
				MultiSellHolder.getInstance().SeparateAndSend(50000002, player, 0);
			}
			else if (reply == 3)
			{
				MultiSellHolder.getInstance().SeparateAndSend(50000003, player, 0);
			}
			else if (reply == 4)
			{
				MultiSellHolder.getInstance().SeparateAndSend(50000004, player, 0);
			}
			else if (reply == 5)
			{
				MultiSellHolder.getInstance().SeparateAndSend(50000005, player, 0);
			}
			else if (reply == 6)
			{
				MultiSellHolder.getInstance().SeparateAndSend(50000006, player, 0);
			}
			else if (reply == 7)
			{
				MultiSellHolder.getInstance().SeparateAndSend(50000007, player, 0);
			}
			else if (reply == 8)
			{
				MultiSellHolder.getInstance().SeparateAndSend(50000008, player, 0);
			}
			else if (reply == 9)
			{
				MultiSellHolder.getInstance().SeparateAndSend(3670609, player, 0);
			}
			else if (reply == 10)
			{
				MultiSellHolder.getInstance().SeparateAndSend(3670610, player, 0);
			}
			else if (reply == 11)
			{
				MultiSellHolder.getInstance().SeparateAndSend(3670611, player, 0);
			}
			else if (reply == 12)
			{
				MultiSellHolder.getInstance().SeparateAndSend(3670612, player, 0);
			}
			else if (reply == 13)
			{
				MultiSellHolder.getInstance().SeparateAndSend(3670613, player, 0);
			}
			else if (reply == 14)
			{
				MultiSellHolder.getInstance().SeparateAndSend(3670614, player, 0);
			}
			else if (reply == 15)
			{
				MultiSellHolder.getInstance().SeparateAndSend(3670615, player, 0);
			}
			else if (reply == 16)
			{
				MultiSellHolder.getInstance().SeparateAndSend(3670616, player, 0);
			}
			else if (reply == 17)
			{
				MultiSellHolder.getInstance().SeparateAndSend(3670617, player, 0);
			}
			else if (reply == 18)
			{
				MultiSellHolder.getInstance().SeparateAndSend(3670618, player, 0);
			}
			else if (reply == 19)
			{
				MultiSellHolder.getInstance().SeparateAndSend(3670619, player, 0);
			}
		}
		return;
	}

	private void showHtml_Add(Player player, String name, int lv, int reply)
	{
		ItemInstance itemInstance = player.getInventory().getPaperdollItem(bodyPart[reply]);//再判斷是否有裝備此件防具
		if(itemInstance == null)
		{
			player.sendMessage("並沒有裝備防具，請先裝備後再點擊。");
			return;
		}
		HtmlMessage adminReply = new HtmlMessage(5);
		StringBuilder replyMSG = new StringBuilder("<html><body>");
		replyMSG.append("您可以鑲嵌的類型如下列清單</br>");
		for(ItemInstance item : player.getInventory().getItems()) //1階
		{
			if(item.getName().contains(name) && item.getTemplate().getAdditionalName().equals("1階")) //用名稱去判別物品類型
			{
				replyMSG.append("<Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h npc?reset " + item.getItemId() + " " + lv + " " + reply + "\"><font color=LEVEL>使用「" + item.getName() + " " + item.getTemplate().getAdditionalName() + "」鑲嵌。</font></button>");
			}
		}
		replyMSG.append("</body></html>");
		adminReply.setHtml(replyMSG.toString());
		player.sendPacket(adminReply);
	}

	private void showHtml(Player player, String name, int reply)
	{
		ItemInstance itemInstance = player.getInventory().getPaperdollItem(bodyPart[reply]);//再判斷是否有裝備此件防具
		if(itemInstance == null)
		{
			player.sendMessage("並沒有裝備防具，請先裝備後再點擊。");
			return;
		}
		int pos = 0;
		if(itemInstance.isAugmented())
		{
			//第1碼  1 + 幾階2碼  01  + 部位1碼 0  +技能
			String s = String.valueOf(itemInstance.getVariation1Id());
			pos = Integer.parseInt(s.substring(2, 4));
		}
		HtmlMessage adminReply = new HtmlMessage(5);

		StringBuilder replyMSG = new StringBuilder("<html><body>");
		replyMSG.append("可以鑲嵌的類型如下列清單</br>");
		//Announcements.announceToAll("pos " + pos);
		for(ItemInstance item : player.getInventory().getItems())
		{
			if(item.getName().contains(name)) //用名稱去判別物品類型 5階-特技:巨力X1
			{
				String tmp = item.getTemplate().getAdditionalName().replaceAll("階", "").split("-")[0];
				int lv = Integer.parseInt(tmp);
				if(lv != 1)
				{
					//Announcements.announceToAll(item.getName() + "pos " + pos + " lv:" + lv);
					if(pos + 1 == lv)
					{
						replyMSG.append("<Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h npc?upgrade " + item.getItemId() + " " + reply + "\">使用「" + item.getName() + " " + item.getTemplate().getAdditionalName() + "」鑲嵌。</button>");
					}
					else if(pos > lv)
					{
						if(lv == 5 || lv == 9)
						{
							replyMSG.append("<Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h npc?upgrade " + item.getItemId() + " " + reply + "\">使用「" + item.getName() + " " + item.getTemplate().getAdditionalName() + "」鑲嵌。</button>");
						}
					}
					else if(pos == 13 && lv == 13)
					{
						replyMSG.append("<Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h npc?upgrade " + item.getItemId() + " " + reply + "\">使用「" + item.getName() + " " + item.getTemplate().getAdditionalName() + "」鑲嵌。</button>");
					}
				}
			}
		}
		replyMSG.append("</body></html>");
		adminReply.setHtml(replyMSG.toString());
		player.sendPacket(adminReply);
	}

	private void updateCheck(Player player,ItemInstance itemInstance,ItemInstance stone1)
	{
		String op1 = itemInstance.getVariation1Id() + "";
		String op2 = itemInstance.getVariation2Id() + "";

		//要先判斷是否是同一類型的石頭才可以提升等級
		String chk = stone[Integer.parseInt(op1.substring(0, 1)) - 1];
		if(!chk.equals(stone1.getName()))
		{
			player.sendMessage("此件防具已鑲嵌「" + chk + "」潛石類型，如有要用別的潛能石請先清除。");
			return;
		}
		String id1 = "";
		//11 01 1 0
		String pos1 = op1.substring(0, 2);//類型 黃晶潛能石-物理攻擊
		String pos2 = op1.substring(2, 4);//這一個是判斷幾階的 01 =1階
		String pos3 = op1.substring(4, 5);//表示技能  正面對決
		String pos4 = op1.substring(5, 6);//表示部件身上位置
		//1 00 00 00
		String froms = op2.substring(0, 1);//類型 第一格無用編碼
		String lv5F = op2.substring(1, 2);//巨力 活血
		String lv5S = op2.substring(2, 3);//五級幾階
		String lv9F = op2.substring(3, 4);//巨力 活血
		String lv9S = op2.substring(4, 5);//九級幾階
		String lv13F = op2.substring(5, 6);//巨力 活血
		String lv13S = op2.substring(6, 7);//13級幾階

		int myLevel = Integer.parseInt(pos2);
		//先判斷是否升級
		String itemName = stone1.getTemplate().getAdditionalName();

		//13階-特技:狂暴X5/召喚即時傳送門                    13階-特技:巨力X1
		String tmp = itemName.replaceAll("階", "").replaceAll("特技:", "").replaceAll("X", "-");
		String items[] = tmp.split("-");//13-活血-1
		int item_lv = 0;//這一個是寶石的等級
		String item_name = "";
		String item_skill_lv = "";
		
		item_lv = Integer.parseInt(items[0]);
		if(items.length == 3)
		{
			item_name = items[1];
			//巨力  活血2  秘法3 頑強4 狂暴5
			if(item_name.equals("巨力"))
			{
				item_name = "1";
			}
			else if(item_name.equals("活血"))
			{
				item_name = "2";
			}
			else if(item_name.equals("秘法"))
			{
				item_name = "3";
			}
			else if(item_name.equals("頑強"))
			{
				item_name = "4";
			}
			else if(item_name.equals("狂暴"))
			{
				item_name = "5";
			}
			item_skill_lv = items[2].split("/")[0];

			if(myLevel + 1 < item_lv)
			{
				player.sendMessage("使用的物品等級過高。");
				return;
			}
		}
		if(itemName.equals("1階"))
		{
			id1 = pos1 + "01" + pos3 + pos4;
		}
		else if(itemName.equals("2階"))
		{
			//11 01 1 0
			id1 = pos1 + "02" + pos3 + pos4;
		}
		else if(itemName.equals("3階"))
		{
			id1 = pos1 + "03" + pos3 + pos4;
		}
		else if(itemName.equals("4階"))
		{
			id1 = pos1 + "04" + pos3 + pos4;
		}
		else if(itemName.equals("6階"))
		{
			id1 = pos1 + "06" + pos3 + pos4;
		}
		else if(itemName.equals("7階"))
		{
			id1 = pos1 + "07" + pos3 + pos4;
		}
		else if(itemName.equals("8階"))
		{
			id1 = pos1 + "08" + pos3 + pos4;
		}
		else if(itemName.equals("10階"))
		{
			id1 = pos1 + "10" + pos3 + pos4;
		}
		else if(itemName.equals("11階"))
		{
			id1 = pos1 + "11" + pos3 + pos4;
		}
		else if(itemName.equals("12階"))
		{
			id1 = pos1 + "12" + pos3 + pos4;
		}

		if(id1 != "")//把n階物品升級n+1階這裡處理了
		{
			if(myLevel + 1 != item_lv)
			{
				player.sendMessage("請使用相對階段物品鑲嵌。");
				return;
			}
			itemInstance.setVariation1Id(Integer.parseInt(id1));//把n階物品升級n+1階這裡處理了
			itemInstance.save();
			player.getInventory().destroyItemByItemId(stone1.getItemId(), 1);//20190622修正刪除整個背包物
			player.sendPacket(SystemMessagePacket.removeItems(stone1.getItemId(), 1));//刪除通知訊息
			player.recalculateStone();//重新計算能力及技能
			//刪除補天神石
			player.getInventory().destroyItemByItemId(36412, item_lv);//刪除補天神石碎片
			player.sendPacket(SystemMessagePacket.removeItems(36412, item_lv));//刪除通知訊息
			return;
		}
		id1 = op1;
		String id2 = String.valueOf(itemInstance.getVariation2Id());

		if(myLevel == 4 && item_lv == 5)
		{
			id2 = froms + item_name + item_skill_lv + lv9F + lv9S + lv13F + lv13S;
			pos2 = "05";
			id1 = pos1 + pos2 + pos3 + pos4;
			itemInstance.setVariation1Id(Integer.parseInt(id1));//把n階物品升級n+1階這裡處理了
		}
		else if(myLevel == 8 && item_lv == 9)
		{
			id2 = froms + lv5F + lv5S + item_name + item_skill_lv + lv13F + lv13S;
			pos2 = "09";
			id1 = pos1 + pos2 + pos3 + pos4;
			itemInstance.setVariation1Id(Integer.parseInt(id1));//把n階物品升級n+1階這裡處理了
		}
		else if(myLevel == 12 && item_lv == 13)
		{
			id2 = froms + lv5F + lv5S + lv9F + lv9S + item_name + item_skill_lv;
			pos2 = "13";
			id1 = pos1 + pos2 + pos3 + pos4;
			itemInstance.setVariation1Id(Integer.parseInt(id1));//把n階物品升級n+1階這裡處理了
		}
		else if(myLevel == 13 && item_lv == 13)//這裡是單純替換13階屬性
		{
			id2 = froms + lv5F + lv5S + lv9F + lv9S + item_name + item_skill_lv;
		}
		else if(myLevel >= 9 && item_lv == 9)//這裡是單純替換9階屬性
		{
			id2 = froms + lv5F + lv5S + item_name + item_skill_lv + lv13F + lv13S;
		}
		else if(myLevel >= 5 && item_lv == 5)//這裡是單獨替換5階屬性
		{
			id2 = froms + item_name + item_skill_lv + lv9F + lv9S + lv13F + lv13S;
		}
		boolean edit = false;
		if(item_lv == 13)
		{
			if(itemName.contains("/"))
			{
				String s = itemName.split("/")[1];//13階-特技:狂暴X5/召喚即時傳送門
				if(s.equals("正面對決"))
				{
					pos3 = "1";
				}
				else if(s.equals("治癒光輝"))
				{
					pos3 = "2";
				}
				else if(s.equals("重置"))
				{
					pos3 = "3";
				}
				else if(s.equals("誘餌分身"))
				{
					pos3 = "4";
				}
				else if(s.equals("召喚即時傳送門"))
				{
					pos3 = "5";
				}
			}
			else
			{
				pos3 = "0";
			}
			//11 01 1 0
			id1 = pos1 + pos2 + pos3 + pos4;
			itemInstance.setVariation1Id(Integer.parseInt(id1));
			edit = true;
		}
		if(id2 != "")
		{
			itemInstance.setVariation2Id(Integer.parseInt(id2));
			edit = true;
		}
		if(edit)
		{
			itemInstance.save();
			player.getInventory().destroyItem(stone1);
			player.sendPacket(SystemMessagePacket.removeItems(stone1.getItemId(), 1));//刪除通知訊息
			//刪除補天神石碎片
			player.getInventory().destroyItemByItemId(36412, item_lv);//刪除補天神石碎片
			player.sendPacket(SystemMessagePacket.removeItems(36412, item_lv));//刪除通知訊息
			player.recalculateStone();//重新計算能力及技能
		}
	}
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		final String[] buypassOptions = command.split(" ");
		if(command.startsWith("Chat"))
		{
			showChatWindow(player, "default/stone/" + getNpcId() + "-" + buypassOptions[1] + ".htm", false);
		}
		else if(buypassOptions[0].equals("showstatus"))
		{
//			String pos1 = op1.substring(0, 2);//類型 黃晶潛能石-物理攻擊
//			String pos2 = op1.substring(2, 4);//這一個是判斷幾階的 01 =1階
//			String pos3 = op1.substring(4, 5);//表示技能  正面對決
//			String pos4 = op1.substring(5, 6);//表示部件身上位置
//			//1 00 00 00
//			String froms = op2.substring(0, 1);//類型 第一格無用編碼
//			String lv5F = op2.substring(1, 2);//巨力 活血
//			String lv5S = op2.substring(2, 3);//五級幾階
//			String lv9F = op2.substring(3, 4);//巨力 活血
//			String lv9S = op2.substring(4, 5);//九級幾階
//			String lv13F = op2.substring(5, 6);//巨力 活血
//			String lv13S = op2.substring(6, 7);//13級幾階
//        	1巨力  活血2  秘法3 頑強4 狂暴5
//			1正面對決 2治癒光輝 3重置 4誘餌分身 5召喚即時傳送門
			ItemInstance itemInstance = null;
			Map<Integer, Integer> maps = new HashMap<Integer, Integer>();
			Map<Integer, Integer> maps_skill = new HashMap<Integer, Integer>();
			String arr[] = {
				"巨力",
				"活血",
				"秘法",
				"頑強",
				"狂暴"
			};
			String arr_Id[] = {
					"ill10318",
					"ill11003",
					"ill11151",
					"ill6885",
					"ill10326"
				};
			
			String brr[] = {
					"正面對決",
					"治癒光輝",
					"重置",
					"誘餌分身",
					"召喚即時傳送門"
				};
			String brr_Id[] = {
					"ll10319",
					"ill11757",
					"ill11783",
					"ill10775",
					"ill11361"
				};
			for (int i = 1 ;i <6; i++)
			{
				maps.put(i, 0);//1巨力  活血2  秘法3 頑強4 狂暴5
				maps_skill.put(i, 0);//1正面對決 2治癒光輝 3重置 4誘餌分身 5召喚即時傳送門
			}
			maps.getOrDefault(1, 0);
			int type_ID = 0;
			int type_LV = 0;
			String my_ID = "";
			int specialskills =0;
			for(int j = 0; j < bodyPart.length; j++)
			{
				itemInstance = player.getInventory().getPaperdollItem(bodyPart[j]);//再判斷是否有裝備此件防具
				if (itemInstance != null)
				{
					if (itemInstance.isAugmented())
					{
						my_ID = String.valueOf(itemInstance.getVariation2Id());
						for(int k = 0; k < 3; k++)//這一類是巨力之類的技能
						{
							type_ID = Integer.parseInt(my_ID.substring(k * 2 + 1, k * 2 + 2));
							type_LV = Integer.parseInt(my_ID.substring(k * 2 + 2, k * 2 + 3));
							if (type_LV > 0)
							{
								maps.put(type_ID, maps.get(type_ID) + type_LV);
							}
						}
						my_ID = String.valueOf(itemInstance.getVariation1Id());
						specialskills = Integer.parseInt(my_ID.substring(4, 5));
						if (specialskills != 0)
						{
							maps_skill.put(specialskills, maps_skill.get(specialskills) + 1);
						}
						
					}
				}
			}
			HtmlMessage adminReply = new HtmlMessage(5);
			StringBuilder replyMSG = new StringBuilder("<html><title>潛能石特技</title><body><br><center>當前擁有特技和點數</center><br><center><table width=260>");
//			$1技能 icon
//			$2技能等級 / 15
//			$3技能點數
//			$4 差額數量
			//$5 下一級
			String T1 = "<tr><td></td><td><font color=\"FF4040\">$0</font></td><td></td></tr><tr><td height=50><center><img src=\"icon.sk$1\" width=32 height=32></center></td><td><center>效果等級:「<font color=\"FF4040\">$2</font>」&nbsp;特技總數:「<font color=\"LEVEL\">$3</font>」</center><br1>還需「<font color=\"LEVEL\">$4</font>」點特技升級到「<font color=\"FF4040\">$5</font>」級</td><td><center><img src=\"icon.sk$1\" width=32 height=32></center></td></tr>";
			String T2_Full = "<tr><td></td><td><font color=\"FF4040\">$0</font></td><td></td></tr><tr><td height=50><center><img src=\"icon.sk$1\" width=32 height=32></center></td><td><center>效果等級:「<font color=\"FF4040\">$2</font>」&nbsp;特技總數:「<font color=\"LEVEL\">$3</font>」</center><br1><font color=\"LEVEL\">已達到滿級</font></td><td><center><img src=\"icon.sk$1\" width=32 height=32></center></td></tr>";
			String tmp ="";
			int counts = 0;
			int lv =0 ;
			for(int i = 0; i < maps.size(); i++)
			{
				//15點1級 30點2級  45點3級  60點4級 75點5級
				counts = maps.get(i + 1);
				if (counts > 0)
				{
					if (counts >= 75)
					{
						tmp = T2_Full.replace("$0", "特技:" + arr[i]);
						tmp = tmp.replace("$1", arr_Id[i]);
						lv = (int) Math.floor(counts / 15);
						tmp = tmp.replace("$2", String.valueOf(lv));
						tmp = tmp.replace("$3", String.valueOf(counts));
						replyMSG.append(tmp);
					}
					else
					{
						tmp = T1.replace("$0", "特技:" + arr[i]);
						tmp = tmp.replace("$1", arr_Id[i]);
						lv = (int) Math.floor(counts / 15);
						tmp = tmp.replace("$2", String.valueOf(lv));
						tmp = tmp.replace("$3", String.valueOf(counts));
						tmp = tmp.replace("$4", String.valueOf(((lv + 1) * 15) - counts));
						tmp = tmp.replace("$5", String.valueOf(lv + 1));
						replyMSG.append(tmp);
					}
				}
			}
			for(int i = 0; i < maps_skill.size(); i++)
			{
				counts = maps_skill.get(i + 1);
				if (counts > 0)
				{
					if (counts >= 9)
					{
						tmp = T2_Full.replace("$0", "稀有特技:" + brr[i]);
						tmp = tmp.replace("$1", brr_Id[i]);
						lv = (int) Math.floor(counts / 3);
						tmp = tmp.replace("$2", String.valueOf(lv));
						tmp = tmp.replace("$3", String.valueOf(counts));
						replyMSG.append(tmp);
					}
					else
					{
						tmp = T1.replace("$0", "稀有特技:" + brr[i]);
						tmp = tmp.replace("$1", brr_Id[i]);
						lv = (int) Math.floor(counts / 3);
						tmp = tmp.replace("$2", String.valueOf(lv));
						tmp = tmp.replace("$3", String.valueOf(counts));
						tmp = tmp.replace("$4", String.valueOf(((lv + 1) * 3) - counts));
						tmp = tmp.replace("$5", String.valueOf(lv + 1));
						replyMSG.append(tmp);
					}
				}
			}
			replyMSG.append("</table></center></body></html>");
			adminReply.setHtml(replyMSG.toString());
			player.sendPacket(adminReply);
		}
		else if(buypassOptions[0].equals("upTo5913"))
		{
			int itemLoc = Integer.parseInt(buypassOptions[1]);//取得要升級的部位
			ItemInstance itemInstance = player.getInventory().getPaperdollItem(bodyPart[itemLoc]);//再判斷是否有裝備此件防具
			if(itemInstance == null)
			{
				player.sendMessage("並沒有裝備防具。");
				return;
			}
			if(!itemInstance.isAugmented())
			{
				player.sendMessage("請至少鑲嵌1階之後再來提升。");
				return;
			}
			String op1 = itemInstance.getVariation1Id() + "";
			int pos = Integer.parseInt(op1.substring(0, 1));//分辦目前使用的是那一種石頭 { "黃晶潛能石", "紅晶潛能石", "粉晶潛能石", "紫晶潛能石", "藍晶潛能石" };
			int lv = Integer.parseInt( op1.substring(2, 4));//取出目前是幾階了
			
			
			HtmlMessage adminReply = new HtmlMessage(5);
			StringBuilder replyMSG = new StringBuilder("<html><body>");
			String first = "您可以鑲嵌的類型如下列清單</br>";
			String second ="" ;
			for(ItemInstance item : player.getInventory().getItems())
			{
				if(item.getName().contains(stone[pos-1]))
				{
					String tmp = item.getTemplate().getAdditionalName().replaceAll("階", "").split("-")[0];
					int itemLv = Integer.parseInt(tmp) ;//物品等級先減1
					
					if(lv >= itemLv) //用名稱去判別物品類型
					{
						if (itemLv == 5 || itemLv==9 || itemLv==13)
						{
							second += "<Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h npc?ItemTo5913 " + item.getItemId() + " " + op1 + " " + itemLoc + "\"><font color=LEVEL>使用「" + item.getName() + " " + item.getTemplate().getAdditionalName() + "」鑲嵌。</font></button>";
						}
						//replyMSG.append();
					}
				}
			}
			if (second.equals(""))
			{
				replyMSG.append("沒有替換的鑲嵌石。");
			}else {
				replyMSG.append(first + second);
			}
			replyMSG.append("</body></html>");
			adminReply.setHtml(replyMSG.toString());
			player.sendPacket(adminReply);
		}
		else if(buypassOptions[0].equals("ItemTo5913"))
		{
			int itemId = Integer.parseInt(buypassOptions[1]);//記錄使用的石頭
			int val = Integer.parseInt(buypassOptions[2]);//記錄裝備上應該有的能力值
			int itemLoc = Integer.parseInt(buypassOptions[3]);//記錄是裝在那一個身上的物品
			ItemInstance stone1 = player.getInventory().getItemByItemId(itemId);
			if(stone1 == null)//先判斷是否有石頭
			{
				player.sendMessage("並沒有此潛能石。");
				return;
			}
			ItemInstance itemInstance = player.getInventory().getPaperdollItem(bodyPart[itemLoc]);//再判斷是否有裝備此件防具
			if(itemInstance == null)
			{
				player.sendMessage("並沒有裝備防具。");
				return;
			}
			if(!itemInstance.isAugmented())
			{
				player.sendMessage("請確定防具是否有鑲嵌了。");
				return;
			}
			
			if(itemInstance.getVariation1Id() !=val )
			{
				player.sendMessage("似乎拿錯誤物品。");
				return;
			}
			updateCheck( player, itemInstance, stone1);
			
		}
		else if(buypassOptions[0].equals("myUpgradeAdd"))
		{
			int itemLoc = Integer.parseInt(buypassOptions[1]);//取得要升級的部位
			ItemInstance itemInstance = player.getInventory().getPaperdollItem(bodyPart[itemLoc]);//再判斷是否有裝備此件防具
			if(itemInstance == null)
			{
				player.sendMessage("並沒有裝備防具。");
				return;
			}
			if(!itemInstance.isAugmented())
			{
				player.sendMessage("請至少鑲嵌1階之後再來提升。");
				return;
			}
			
			String op1 = itemInstance.getVariation1Id() + "";
			int pos = Integer.parseInt(op1.substring(0, 1));//分辦目前使用的是那一種石頭 { "黃晶潛能石", "紅晶潛能石", "粉晶潛能石", "紫晶潛能石", "藍晶潛能石" };
			int lv = Integer.parseInt( op1.substring(2, 4));//取出目前是幾階了
			
			
			HtmlMessage adminReply = new HtmlMessage(5);
			StringBuilder replyMSG = new StringBuilder("<html><body>");
			String first = "可以鑲嵌的類型如下列清單</br>";
			String second ="" ;
			//Announcements.announceToAll(stone[pos-1]);
			for(ItemInstance item : player.getInventory().getItems())
			{
				if(item.getName().contains(stone[pos-1]))
				{
					String tmp = item.getTemplate().getAdditionalName().replaceAll("階", "").split("-")[0];
					int itemLv = Integer.parseInt(tmp) - 1;//物品等級先減1

					if(lv == itemLv) //用名稱去判別物品類型
					{
						second += "<Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h npc?upgradeNext " + item.getItemId() + " " + op1 + " " + itemLoc + "\"><font color=LEVEL>使用「" + item.getName() + " " + item.getTemplate().getAdditionalName() + "」鑲嵌。</font></button>";
						//replyMSG.append();
					}
				}
			}
			if (second.equals(""))
			{
				replyMSG.append("沒有可用的潛能石。");
			}else {
				replyMSG.append(first + second);
			}
			replyMSG.append("</body></html>");
			adminReply.setHtml(replyMSG.toString());
			player.sendPacket(adminReply);
		}		
		else if(buypassOptions[0].equals("upgradeNext"))
		{
			int itemId = Integer.parseInt(buypassOptions[1]);//記錄使用的石頭
			int val = Integer.parseInt(buypassOptions[2]);//記錄裝備上應該有的能力值
			int itemLoc = Integer.parseInt(buypassOptions[3]);//記錄是裝在那一個身上的物品
			ItemInstance stone1 = player.getInventory().getItemByItemId(itemId);
			if(stone1 == null)//先判斷是否有石頭
			{
				player.sendMessage("並沒有此潛能石。");
				return;
			}
			ItemInstance itemInstance = player.getInventory().getPaperdollItem(bodyPart[itemLoc]);//再判斷是否有裝備此件防具
			if(itemInstance == null)
			{
				player.sendMessage("並沒有裝備防具。");
				return;
			}
			if(!itemInstance.isAugmented())
			{
				player.sendMessage("請確定防具是否有鑲嵌了。");
				return;
			}
			
			if(itemInstance.getVariation1Id() !=val )
			{
				player.sendMessage("似乎拿錯誤物品。");
				return;
			}
			String op1 = itemInstance.getVariation1Id() + "";
			int pos = Integer.parseInt(op1.substring(0, 1));//分辦目前使用的是那一種石頭 { "黃晶潛能石", "紅晶潛能石", "粉晶潛能石", "紫晶潛能石", "藍晶潛能石" };
			int lv = Integer.parseInt( op1.substring(2, 4));//取出目前是幾階了
			
			if(player.getInventory().getCountOf(36412) < lv + 1)
			{
				player.sendMessage("鑲嵌至下一階需要「" + (lv + 1) + "」個補天神石碎片。");
				return;
			}
			updateCheck( player, itemInstance, stone1);
			int lv2 = Integer.parseInt(String.valueOf(itemInstance.getVariation1Id()).substring(2, 4));
			if((lv+1) ==lv2)
			{
				HtmlMessage adminReply = new HtmlMessage(5);
				StringBuilder replyMSG = new StringBuilder("<html><body>");
				String first = "可以鑲嵌的類型如下列清單</br>";
				String second = "";
				for(ItemInstance item : player.getInventory().getItems())
				{
					if(item.getName().contains(stone[pos-1]))
					{
						String tmp = item.getTemplate().getAdditionalName().replaceAll("階", "").split("-")[0];
						int itemLv = Integer.parseInt(tmp) - 1;//物品等級先減1
						if((lv2 == itemLv)) //用名稱去判別物品類型
						{
							second += "<Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h npc?upgradeNext " + item.getItemId() + " " + itemInstance.getVariation1Id() + " " + itemLoc + "\"><font color=LEVEL>使用「" + item.getName() + " " + item.getTemplate().getAdditionalName() + "」鑲嵌。</font></button>";
						}
					}
				}
				if(second.equals(""))
				{
					replyMSG.append("沒有可用的下一階潛能石。");
				}
				else
				{
					replyMSG.append(first + second);
				}
				replyMSG.append("</body></html>");
				adminReply.setHtml(replyMSG.toString());
				player.sendPacket(adminReply);
			}
		}
		else if(buypassOptions[0].equals("upgrade"))
		{
			int itemId = Integer.parseInt(buypassOptions[1]);
			int itemLoc = Integer.parseInt(buypassOptions[2]);
			ItemInstance stone1 = player.getInventory().getItemByItemId(itemId);
			if(stone1 == null)//先判斷是否有石頭
			{
				player.sendMessage("並沒有此潛能石。");
				return;
			}
			ItemInstance itemInstance = player.getInventory().getPaperdollItem(bodyPart[itemLoc]);//再判斷是否有裝備此件防具

			if(itemInstance == null)
			{
				player.sendMessage("並沒有裝備防具。");
				return;
			}
			//1 黃晶潛能石幾階   幾階  頭手腳  + 技能
			//第1碼  1 + 幾階2碼  01  + 部位1碼 0  +技能
			//黃晶潛能石    部位9 + 5階  + 9階  + 13階
			// 起始1  +0          + 0  + 0     +0   +0 
			if(itemInstance.isAugmented())
			{
				updateCheck( player, itemInstance, stone1);
			}
			else
			{
				int id1 = 0;
				int id2 = 0;
				itemLoc++; //這裡必需+1 因為dat是以加1去計算的，建議不要在改動了
				if(stone1.getName().equals("黃晶潛能石"))
				{ //110110
					id1 = Integer.parseInt("101010" + (itemLoc));
					id2 = Integer.parseInt("1000000"); //1 00 00 00 +loc 
				}
				else if(stone1.getName().equals("紅晶潛能石"))
				{
					id1 = Integer.parseInt("201010" + itemLoc);
					id2 = Integer.parseInt("1000000");
				}
				else if(stone1.getName().equals("粉晶潛能石"))
				{
					id1 = Integer.parseInt("301010" + itemLoc);
					id2 = Integer.parseInt("1000000");
				}
				else if(stone1.getName().equals("紫晶潛能石"))
				{
					id1 = Integer.parseInt("401010" + itemLoc);
					id2 = Integer.parseInt("1000000");
				}
				else if(stone1.getName().equals("藍晶潛能石"))
				{
					id1 = Integer.parseInt("501010" + itemLoc);
					id2 = Integer.parseInt("1000000");
				}
				itemInstance.setVariation1Id(id1);
				itemInstance.setVariation2Id(id2);
				itemInstance.save();
				player.getInventory().destroyItemByItemId(itemId, 1);//20190622 修正刪除整個背包的東西
				player.sendPacket(SystemMessagePacket.removeItems(stone1.getItemId(), 1));//刪除通知訊息
				player.recalculateStone();//重新計算能力及技能
			}
		}		
		else if(buypassOptions[0].equals("inherit"))
		{
			int itemLoc = Integer.parseInt(buypassOptions[1]);//取得要升級的部位
			ItemInstance itemInstance = player.getInventory().getPaperdollItem(bodyPart[itemLoc]);//再判斷是否有裝備此件防具
			if(itemInstance == null)
			{
				player.sendMessage("並沒有裝備防具。");
				return;
			}
			if(!itemInstance.isAugmented())
			{
				player.sendMessage("裝備的防具無鑲嵌請重新確認是否有誤。");
				return;
			}
			HtmlMessage adminReply = new HtmlMessage(5);
			StringBuilder replyMSG = new StringBuilder("<html><body>");
			String first = "可以繼承的物品如下列清單</br>";
			String second = "";
			for(ItemInstance item : player.getInventory().getItems())
			{
				if(item.getBodyPart() == ItemTemplate.SLOT_CHEST || item.getBodyPart() == ItemTemplate.SLOT_FULL_ARMOR)
				{
					if(itemInstance.getBodyPart() == ItemTemplate.SLOT_CHEST || itemInstance.getBodyPart() == ItemTemplate.SLOT_FULL_ARMOR)
					{
						if(item.getObjectId() != itemInstance.getObjectId())
						{
							second += "<Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h npc?inheritItems " + itemInstance.getObjectId() + " " + item.getObjectId() + " " + itemLoc + "\"><font color=LEVEL>使用「" + item.getName() + "」繼承鑲嵌。</font></button>";
						}
					}
				}
				else if(item.getBodyPart() == itemInstance.getBodyPart())
				{
					if(item.getObjectId() != itemInstance.getObjectId())
					{
						if(item.getLifeTime() == 0)
						{
							second += "<Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h npc?inheritItems " + itemInstance.getObjectId() + " " + item.getObjectId() + " " + itemLoc + "\"><font color=LEVEL>使用「" + item.getName() + "」繼承鑲嵌。</font></button>";
						}
					}
				}
			}
			if (second.equals(""))
			{
				replyMSG.append("沒有可繼承的防具清單。");
			}
			else 
			{
				replyMSG.append(first + second);
			}
			replyMSG.append("</body></html>");
			adminReply.setHtml(replyMSG.toString());
			player.sendPacket(adminReply);
		}
		else if(buypassOptions[0].startsWith("reNew"))
		{
			int itemLv = Integer.parseInt(buypassOptions[1]);//取得要升級的部位
			String first = "";
			if(itemLv == 5)
			{
				first = "可以重置的物品清單 您將花費3時光幣</br>";
			}
			else if(itemLv == 9)
			{
				first = "可以重置的物品清單 您將花費10時光幣</br>";
			}
			else if(itemLv == 13)
			{
				first = "可以重置的物品清單 您將花費30時光幣</br>";
			}
			HtmlMessage adminReply = new HtmlMessage(5);
			StringBuilder replyMSG = new StringBuilder("<html><body>");
			String second = "";
			for(ItemInstance item : player.getInventory().getItems())
			{
				if(item.getName().contains("潛能石"))
				{
					String joinName ="";
					String ItemRealName = item.getTemplate().getAdditionalName();
					if(item.getName().contains("黃晶"))
					{
						if(ItemRealName.contains("5階") && (itemLv == 5))
						{
							joinName = item.getObjectId() + " 24009 3";//黃晶潛能石錦囊  及 時光幣數量
						}
						else if(ItemRealName.contains("9階") && (itemLv == 9))
						{
							joinName = item.getObjectId() + " 24014 10";//黃晶潛能石錦囊  及 時光幣數量
						}
						else if(ItemRealName.contains("13階") && (itemLv == 13))
						{
							joinName = item.getObjectId() + " 24019 30";//黃晶潛能石錦囊  及 時光幣數量
						}
					}
					else if(item.getName().contains("紅晶"))
					{
						if(ItemRealName.contains("5階") && (itemLv == 5))
						{
							joinName = item.getObjectId() + " 24010 3";
						}
						else if(ItemRealName.contains("9階") && (itemLv == 9))
						{
							joinName = item.getObjectId() + " 24015 10";
						}
						else if(ItemRealName.contains("13階") && (itemLv == 13))
						{
							joinName = item.getObjectId() + " 24020 30";
						}
					}
					else if(item.getName().contains("粉晶"))
					{
						if(ItemRealName.contains("5階") && (itemLv == 5))
						{
							joinName = item.getObjectId() + " 24011 3";
						}
						else if(ItemRealName.contains("9階") && (itemLv == 9))
						{
							joinName = item.getObjectId() + " 24016 10";
						}
						else if(ItemRealName.contains("13階") && (itemLv == 13))
						{
							joinName = item.getObjectId() + " 24021 30";
						}
					}
					else if(item.getName().contains("紫晶"))
					{
						if(ItemRealName.contains("5階") && (itemLv == 5))
						{
							joinName = item.getObjectId() + " 24012 3";
						}
						else if(ItemRealName.contains("9階") && (itemLv == 9))
						{
							joinName = item.getObjectId() + " 24017 10";
						}
						else if(ItemRealName.contains("13階") && (itemLv == 13))
						{
							joinName = item.getObjectId() + " 24022 30";
						}
					}
					else if(item.getName().contains("藍晶"))
					{
						if(ItemRealName.contains("5階") && (itemLv == 5))
						{
							joinName = item.getObjectId() + " 24013 3";
						}
						else if(ItemRealName.contains("9階") && (itemLv == 9))
						{
							joinName = item.getObjectId() + " 24018 10";
						}
						else if(ItemRealName.contains("13階") && (itemLv == 13))
						{
							joinName = item.getObjectId() + " 24023 30";
						}
					}
					if(joinName.length() > 2)
					{
						second += "<Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h npc?ChangeItemPack " + joinName + "\"><font color=LEVEL>使用「" + item.getName() +  " " + item.getTemplate().getAdditionalName() +  "」封印成錦囊。</font></button>";
					}
				}
			}
			if (second.equals(""))
			{
				replyMSG.append("沒有可使用的清單。");
			}
			else 
			{
				replyMSG.append(first + second);
			}
			replyMSG.append("</body></html>");
			adminReply.setHtml(replyMSG.toString());
			player.sendPacket(adminReply);
		}
		else if(buypassOptions[0].equals("ChangeItemPack")) //ChangeItemPack objectId 24023 3 
		{
			int objId = Integer.parseInt(buypassOptions[1]);
			int ToItemId = Integer.parseInt(buypassOptions[2]);
			int ItemTakeCount = Integer.parseInt(buypassOptions[3]);
			ItemInstance objIdItem = player.getInventory().getItemByObjectId(objId);
			if(objIdItem == null)
			{
				player.sendMessage("物品並不存在。");
				return;
			}
			if(player.getInventory().getCountOf(88888) < ItemTakeCount)
			{
				player.sendMessage("時光幣不足「" + ItemTakeCount + "」個。");
				return;
			}
			player.getInventory().destroyItem(objIdItem);
			player.sendPacket(SystemMessagePacket.removeItems(objIdItem));//刪除通知訊息

			player.getInventory().destroyItemByItemId(88888, ItemTakeCount);
			player.sendPacket(SystemMessagePacket.removeItems(88888, ItemTakeCount));//刪除通知訊息
			ItemInstance it = player.getInventory().addItem(ToItemId, 1);
			player.getPlayer().sendPacket(SystemMessagePacket.obtainItems(it));
		}
		else if(buypassOptions[0].equals("inheritItems"))
		{
			int myItemFrom = Integer.parseInt(buypassOptions[1]);//繼承前的物品
			int myItemTo = Integer.parseInt(buypassOptions[2]);//繼承後的物品
			int itemLoc = Integer.parseInt(buypassOptions[3]);//取得要升級的部位
			ItemInstance itemInstance = player.getInventory().getPaperdollItem(bodyPart[itemLoc]);//再判斷是否有裝備此件防具
			if(itemInstance == null)
			{
				player.sendMessage("並沒有裝備防具。");
				return;
			}
			if(!itemInstance.isAugmented())
			{
				player.sendMessage("似乎拿錯了物品。");
				return;
			}
			if(itemInstance.getObjectId() != myItemFrom)
			{
				player.sendMessage("似乎拿錯了物品。");
				return;
			}
			ItemInstance itemTo = player.getInventory().getItemByObjectId(myItemTo);
			if(itemTo == null)
			{
				player.sendMessage("物品不存在。");
				return;
			}
			itemTo.setVariation1Id(itemInstance.getVariation1Id());
			itemTo.setVariation2Id(itemInstance.getVariation2Id());
			itemTo.save();
			itemInstance.setVariation1Id(0);
			itemInstance.setVariation2Id(0);
			itemInstance.save();
			player.sendMessage("恭喜繼承成功。");
			player.recalculateStone();//重新計算能力及技能
		}
		else if(buypassOptions[0].equals("reset"))
		{
			int itemId = Integer.parseInt(buypassOptions[1]);//這一個是判別物品是否存在
			int lv = Integer.parseInt(buypassOptions[2]);//判別目前等級
			int itemLoc = Integer.parseInt(buypassOptions[3]);
			ItemInstance stone1 = player.getInventory().getItemByItemId(itemId);
			if(stone1 == null)//先判斷是否有石頭
			{
				player.sendMessage("並沒有此潛能石。");
				return;
			}
			ItemInstance itemInstance = player.getInventory().getPaperdollItem(bodyPart[itemLoc]);//再判斷是否有裝備此件防具
			if(itemInstance == null)
			{
				player.sendMessage("並沒有裝備防具。");
				return;
			}
			if(itemInstance.getLifeTime() > 0)
			{
				player.sendMessage("時效防具無法嵌上潛能石。");
				return;
			}
			if(itemInstance.isAugmented())
			{
				player.sendMessage("請先清除裝備上鑲嵌的潛能石。");
				return;
			}
			if(player.getInventory().getCountOf(36412) < 1)
			{
				player.sendMessage("並沒有「1」個補天神石碎片。");
				return;
			}			
			if(stone1.getTemplate().getAdditionalName().contains("1階"))
			{
				int id1 = 0;
				int id2 = 0;
				itemLoc += 1; //這裡必需+1 因為dat是以加1去計算的，建議不要在改動了
				if(stone1.getName().equals("黃晶潛能石"))
				{
					id1 = Integer.parseInt("1" + lv + "010" + (itemLoc));
					id2 = Integer.parseInt("1000000"); //1 00 00 00 +loc 
				}
				else if(stone1.getName().equals("紅晶潛能石"))
				{
					id1 = Integer.parseInt("2" + lv + "010" + itemLoc);
					id2 = Integer.parseInt("1000000");
				}
				else if(stone1.getName().equals("粉晶潛能石"))
				{
					id1 = Integer.parseInt("3" + lv + "010" + itemLoc);
					id2 = Integer.parseInt("1000000");
				}
				else if(stone1.getName().equals("紫晶潛能石"))
				{
					id1 = Integer.parseInt("4" + lv + "010" + itemLoc);
					id2 = Integer.parseInt("1000000");
				}
				else if(stone1.getName().equals("藍晶潛能石"))
				{
					id1 = Integer.parseInt("5" + lv + "010" + itemLoc);
					id2 = Integer.parseInt("1000000");
				}
				itemInstance.setVariation1Id(id1);
				itemInstance.setVariation2Id(id2);
				itemInstance.save();
				player.getInventory().destroyItemByItemId(36412, 1L);//刪除補天神石碎片
				player.sendPacket(SystemMessagePacket.removeItems(36412, 1));//刪除通知訊息
				player.getInventory().destroyItemByItemId(itemId,1);//20190622~~修正刪除整個背包物件
				player.sendPacket(SystemMessagePacket.removeItems(stone1.getItemId(), 1));//刪除通知訊息
				player.recalculateStone();//重新計算能力及技能
			}
		}
		else if (buypassOptions[0].equals("delete"))
		{
			int itemLoc = Integer.parseInt(buypassOptions[1]);
			ItemInstance itemInstance = player.getInventory().getPaperdollItem(bodyPart[itemLoc]);//再判斷是否有裝備此件防具
			if(itemInstance == null)
			{
				player.sendMessage("並沒有裝備防具。");
				return;
			}			
			if(!itemInstance.isAugmented())
			{
				player.sendMessage("裝備的防具並沒有鑲嵌。");
				return;
			}
			if(player.getInventory().getCountOf(36412) < 20)
			{
				player.sendMessage("並沒有「20」個補天神石碎片。");
				return;
			}
			itemInstance.setVariation1Id(0);
			itemInstance.setVariation2Id(0);
			itemInstance.save();
			player.getInventory().destroyItemByItemId(36412, 20L);
			player.sendPacket(SystemMessagePacket.removeItems(36412, 20));//刪除通知訊息
			player.recalculateStone();//重新計算能力及技能
		}
		else if(command.startsWith("ensoulWeapon"))
		{
			ItemInstance itemInstance = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);//再判斷是否有裝備此件武器
			if(itemInstance == null)
			{
				player.sendMessage("並沒有裝備武器。");
				return;
			}
			int weaponId = itemInstance.getItemId();
			if(ArrayUtils.contains(weapon_gread1, weaponId))//龍碎
			{
				checkAndUpgradeWeapon(player, itemInstance, 0);
			}
			else if(ArrayUtils.contains(weapon_gread2, weaponId))//一般
			{
				checkAndUpgradeWeapon(player, itemInstance, 1);
			}
			else if(ArrayUtils.contains(weapon_gread3, weaponId))//上級
			{
				checkAndUpgradeWeapon(player, itemInstance, 2);
			}else {
				player.sendMessage("此武器無法鑄靈。");
			}
		}
		else if(command.startsWith("ensoulArmor"))
		{
			int parts = Integer.parseInt(buypassOptions[1]);//這一個是判別物品是否存在
			
			ItemInstance itemInstance = player.getInventory().getPaperdollItem(bodyArmor[parts]);//再判斷是否有裝備此件武器
			if(itemInstance == null)
			{
				player.sendMessage("並沒有裝備防具。");
				return;
			}
			int ArmorId = itemInstance.getItemId();
			if(ArrayUtils.contains(Armor_gread1, ArmorId))//龍碎 等級防具
			{
				checkAndUpgradeArmor(player, itemInstance, 0);
			}
			else if(ArrayUtils.contains(Armor_gread2, ArmorId))//一般 等級防具
			{
				checkAndUpgradeArmor(player, itemInstance, 1);
			}
			else if(ArrayUtils.contains(Armor_gread3, ArmorId))//上級 等級防具
			{
				checkAndUpgradeArmor(player, itemInstance, 2);
			}
			else
			{
				player.sendMessage("此防具無法鑄靈。");
			}
		}
		else if(command.startsWith("upgradeWeapon"))
		{
			ItemInstance itemInstance = player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);//再判斷是否有裝備此件武器
			if(itemInstance == null)
			{
				player.sendMessage("並沒有裝備武器。");
				return;
			}
			int lv = itemInstance.getMyGrade();
			if(lv == 0)
			{
				player.sendMessage("武器不附合條件。");
				return;
			}
			int weaponId = itemInstance.getItemId();
			if(ArrayUtils.contains(weapon_gread1, weaponId))//龍碎 等級防具
			{
				upGradeNextWeapon(player, itemInstance, 0, ArrayUtils.indexOf(weapon_gread1, weaponId), weapon_gread2);
			}
			else if(ArrayUtils.contains(weapon_gread2, weaponId))//一般 等級防具
			{
				upGradeNextWeapon(player, itemInstance, 1, ArrayUtils.indexOf(weapon_gread2, weaponId), weapon_gread3);
			}
			else if(ArrayUtils.contains(weapon_gread3, weaponId))//上級 等級防具
			{
				upGradeNextWeapon(player, itemInstance, 2, ArrayUtils.indexOf(weapon_gread3, weaponId), weapon_gread4);
			}
			else
			{
				player.sendMessage("此防具無法鑄靈。");
			}
		}
		else if(command.startsWith("upgradeArmor"))
		{
			int parts = Integer.parseInt(buypassOptions[1]);//這一個是判別物品是否存在
			ItemInstance itemInstance = player.getInventory().getPaperdollItem(bodyArmor[parts]);//再判斷是否有裝備此件武器
			if(itemInstance == null)
			{
				player.sendMessage("並沒有裝備防具。");
				return;
			}
			int lv = itemInstance.getMyGrade();
			if(lv == 0)
			{
				player.sendMessage("防具不附合條件。");
				return;
			}
			int ArmorId = itemInstance.getItemId();

			if(ArrayUtils.contains(Armor_gread1, ArmorId))//龍碎 等級防具
			{
				upGradeNextArmor(player, itemInstance, 0, ArrayUtils.indexOf(Armor_gread1, ArmorId), Armor_gread2);
			}
			else if(ArrayUtils.contains(Armor_gread2, ArmorId))//一般 等級防具
			{
				upGradeNextArmor(player, itemInstance, 1, ArrayUtils.indexOf(Armor_gread2, ArmorId), Armor_gread3);
			}
			else if(ArrayUtils.contains(Armor_gread3, ArmorId))//上級 等級防具
			{
				upGradeNextArmor(player, itemInstance, 2, ArrayUtils.indexOf(Armor_gread3, ArmorId), Armor_gread4);
			}
			else
			{
				player.sendMessage("此防具無法提升等級。");
			}
		}
	}
	private void DeleteDataBaseObjId(int objectId)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("delete from items_myensoul where object_id = ?");
			statement.setInt(1, objectId);
			statement.execute();
			statement.close();
		}
		catch(Exception e)
		{
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}
	private void upGradeNextWeapon(Player player, ItemInstance itemInstance, int itemGrade, int index, int[] nextLv)
	{
		if((itemInstance.getMyGrade() + 1) != (weapon_maxLv[itemGrade] + weapon_showId[itemGrade]))
		{
			player.sendMessage("請提升至最高等級再來。");
			return;
		}
		int enchant = itemInstance.getEnchantLevel();
		int op1 = itemInstance.getVariation1Id();
		int op2 = itemInstance.getVariation2Id();
		DeleteDataBaseObjId(itemInstance.getObjectId());//清除資料庫的記錄
		player.sendPacket(SystemMessagePacket.removeItems(itemInstance.getItemId(), 1));//刪除通知訊息
		player.getInventory().destroyItem(itemInstance);
		ItemInstance item = ItemFunctions.createItem(nextLv[index]);
		item.setEnchantLevel(enchant);
		item.setVariation1Id(op1);
		item.setVariation2Id(op2);
		item.setGrade(0);
		item.save();
		player.getPlayer().getInventory().addItem(item);
		player.getPlayer().sendPacket(SystemMessagePacket.obtainItems(item));
	}
	private void upGradeNextArmor(Player player, ItemInstance itemInstance, int itemGrade, int index, int[] nextLv)
	{
		if((itemInstance.getMyGrade() + 1) != (Armor_maxLv[itemGrade] + Armor_showId[itemGrade]))
		{
			player.sendMessage("請提升至最高等級再來。");
			return;
		}
		int enchant = itemInstance.getEnchantLevel();
		int op1 = itemInstance.getVariation1Id();
		int op2 = itemInstance.getVariation2Id();
		DeleteDataBaseObjId(itemInstance.getObjectId());//清除資料庫的記錄
		player.sendPacket(SystemMessagePacket.removeItems(itemInstance.getItemId(), 1));//刪除通知訊息
		player.getInventory().destroyItem(itemInstance);
		ItemInstance item = ItemFunctions.createItem(nextLv[index]);
		item.setEnchantLevel(enchant);
		item.setVariation1Id(op1);
		item.setVariation2Id(op2);
		item.setGrade(0);
		item.save();
		player.getPlayer().getInventory().addItem(item);
		player.getPlayer().sendPacket(SystemMessagePacket.obtainItems(item));
	}
	private boolean checkAndUpgradeArmor(Player player, ItemInstance itemInstance, int itemGrade)
	{
		String[] items = Armor_needitem[itemGrade].split(";");
		for(String item : items)
		{
			String[] it = item.split(",");
			if(player.getInventory().getCountOf(Integer.valueOf(it[0])) < Integer.valueOf(it[1]))
			{
				player.sendMessage("所需道具物品不足，請重新確認。");
				return false;
			}
		}
		int lv = getGradeItem(itemInstance.getObjectId());
		int topLv = 0;
		if(lv != 0)
		{
			topLv = Integer.parseInt(String.valueOf(lv).substring(4, 6));
		}
		if(topLv == Armor_maxLv[itemGrade])
		{
			player.sendMessage("已強化至最高級了，無法再次提高。");
			return false;
		}
		for(String item : items)
		{
			String[] it = item.split(",");
			player.getInventory().destroyItemByItemId(Integer.valueOf(it[0]), Integer.valueOf(it[1]));
			player.sendPacket(SystemMessagePacket.removeItems(Integer.valueOf(it[0]), Integer.valueOf(it[1])));//刪除通知訊息
		}
		if(lv == 0)
		{
			itemInstance.setGrade(Armor_showId[itemGrade]);
		}
		else
		{
			itemInstance.setGrade(lv + 1);
		}
		itemInstance.update();
		return true;
	}
	private boolean checkAndUpgradeWeapon(Player player,ItemInstance itemInstance,int itemGrade)
	{
		String[] items = weapon_needitem[itemGrade].split(";");
		for(String item : items)
		{
			String[] it = item.split(",");
			if(player.getInventory().getCountOf(Integer.valueOf(it[0])) < Integer.valueOf(it[1]))
			{
				player.sendMessage("所需道具物品不足，請重新確認。");
				return false;
			}
		}
		int lv = getGradeItem(itemInstance.getObjectId());
		int topLv = 0;
		if(lv != 0)
		{
			topLv = Integer.parseInt(String.valueOf(lv).substring(4, 6));
		}
		if(topLv == weapon_maxLv[itemGrade])
		{
			player.sendMessage("已鑄靈至最高級了，無法再次提高。");
			return false;
		}
		for(String item : items)
		{
			String[] it = item.split(",");
			player.getInventory().destroyItemByItemId(Integer.valueOf(it[0]), Integer.valueOf(it[1]));
			player.sendPacket(SystemMessagePacket.removeItems(Integer.valueOf(it[0]), Integer.valueOf(it[1])));//刪除通知訊息
		}
		if(lv == 0)
		{
			itemInstance.setGrade(weapon_showId[itemGrade]);
		}
		else
		{
			itemInstance.setGrade(lv + 1);
		}
		return true;
	}
	private int  getGradeItem(int objitemId)
	{		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		int lv =0 ;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT ensoul_id FROM items_myensoul where object_id = ? ");
			statement.setInt(1, objitemId);
			rset = statement.executeQuery();
			if(rset.next())
			lv = rset.getInt("ensoul_id");
			statement.close();
		}
		catch(Exception e)
		{
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		
		return lv;
	}
	private int bodyArmor[] = 
	{ 
		Inventory.PAPERDOLL_HEAD, //頭
		Inventory.PAPERDOLL_GLOVES, //手
		Inventory.PAPERDOLL_CHEST, //胸
		Inventory.PAPERDOLL_LEGS, //下褲
		Inventory.PAPERDOLL_FEET//腳
	};
	String[] weapon_needitem = 
	{ //可自行追加不限制只有二種物品。規則大致就是下面這樣子。
		"36416,99;36413,20",//龍碎 補天99個，玄鐵20個
		"36416,99;36413,40;36414,1",//一般 補天99個 玄鐵40個 龍之印記1個
		"36416,99;36413,60;36414,1"//上級 補天99個 玄鐵60個 龍之印記1個
	};
	int[] weapon_maxLv = { 5, 10, 15 };//龍碎  一般 上級 每一級最高級數
	int[] weapon_showId = { 100001, 100101, 100201 };//龍碎  一般 上級 一級給的技能id

	String[] Armor_needitem = 
	{ //可自行追加不限制只有二種物品。規則大致就是下面這樣子。
		"36416,99;36413,10", //防具 升級需要的石頭量，每一階段的數字
		"36416,99;36413,20;36414,1", 
		"36416,99;36413,30;36414,1" 
	};//防具 升級需要的石頭量，每一階段的數字
	int[] Armor_maxLv = { 5, 5, 10 };//防具 每一級最高級數
	int[] Armor_showId = { 100501, 100601, 100701 };//防具 一級給的技能id
	
	int[] weapon_gread1 = 
	{ 
		36417, //安塔瑞斯鉋刀	碎片
		36418, //安塔瑞斯殺刃	碎片
		36419, //安塔瑞斯投弓	碎片
		36420, //安塔瑞斯爆刃	碎片
		36421, //安塔瑞斯切刃	碎片
		36422, //安塔瑞斯風暴者	碎片
		36423, //安塔瑞斯戰爪	碎片
		36424, //安塔瑞斯復仇者	碎片
		36425, //安塔瑞斯雙鈍器	碎片
		36426, //安塔瑞斯雙刀	碎片
		36427, //巴拉卡斯鉋刀	碎片
		36428, //巴拉卡斯切刃	碎片
		36429, //巴拉卡斯殺刃	碎片
		36430, //巴拉卡斯投弓	碎片
		36431, //巴拉卡斯爆刃	碎片
		36432, //巴拉卡斯術杖	碎片
		36433, //巴拉卡斯報恨者	碎片
		36434, //林德拜爾鉋刀	碎片
		36435, //林德拜爾投弓	碎片
		36436, //林德拜爾殺刃	碎片
		36437, //林德拜爾術杖	碎片
		36438, //林德拜爾切刃	碎片
		36439, //林德拜爾射靈	碎片
		36440, //林德拜爾雙匕首	碎片
		80066, //法利昂復仇者	碎片
		80067, //法利昂戰爪		碎片
		80068, //法利昂風暴者	碎片
		80069, //法利昂報恨者	碎片
		80070, //法利昂雙鈍器	碎片
		80071, //法利昂雙刀		碎片
		80315, //法利昂雙匕首	碎片
	};
	int[] weapon_gread2 = 
	{
		36441, //安塔瑞斯鉋刀	一般
		36442, //安塔瑞斯殺刃	一般
		36443, //安塔瑞斯投弓	一般
		36444, //安塔瑞斯爆刃	一般
		36445, //安塔瑞斯切刃	一般
		36446, //安塔瑞斯風暴者	一般
		36447, //安塔瑞斯戰爪	一般
		36448, //安塔瑞斯復仇者	一般
		36449, //安塔瑞斯雙鈍器	一般
		36450, //安塔瑞斯雙刀	一般
		36451, //巴拉卡斯鉋刀	一般
		36452, //巴拉卡斯切刃	一般
		36453, //巴拉卡斯殺刃	一般
		36454, //巴拉卡斯投弓	一般
		36455, //巴拉卡斯爆刃	一般
		36456, //巴拉卡斯術杖	一般
		36457, //巴拉卡斯報恨者	一般
		36458, //林德拜爾鉋刀	一般
		36459, //林德拜爾投弓	一般
		36460, //林德拜爾殺刃	一般
		36461, //林德拜爾術杖	一般
		36462, //林德拜爾切刃	一般
		36463, //林德拜爾射靈	一般
		36464, //林德拜爾雙匕首	一般
		80072, //法利昂復仇者	一般
		80073, //法利昂戰爪		一般
		80074, //法利昂風暴者	一般
		80075, //法利昂報恨者	一般
		80076, //法利昂雙鈍器	一般
		80077, //法利昂雙刀		一般
		80316, //法利昂雙匕首	一般
	};
	int[] weapon_gread3 = 
	{
		36465, //安塔瑞斯鉋刀	上級
		36466, //安塔瑞斯殺刃	上級
		36467, //安塔瑞斯投弓	上級
		36468, //安塔瑞斯爆刃	上級
		36469, //安塔瑞斯切刃	上級
		36470, //安塔瑞斯風暴者	上級
		36471, //安塔瑞斯戰爪	上級
		36472, //安塔瑞斯復仇者	上級
		36473, //安塔瑞斯雙鈍器	上級
		36474, //安塔瑞斯雙刀	上級
		36475, //巴拉卡斯鉋刀	上級
		36476, //巴拉卡斯切刃	上級
		36477, //巴拉卡斯殺刃	上級
		36478, //巴拉卡斯投弓	上級
		36479, //巴拉卡斯爆刃	上級
		36480, //巴拉卡斯術杖	上級
		36481, //巴拉卡斯報恨者	上級
		36482, //林德拜爾鉋刀	上級
		36483, //林德拜爾投弓	上級
		36484, //林德拜爾殺刃	上級
		36485, //林德拜爾術杖	上級
		36486, //林德拜爾切刃	上級
		36487, //林德拜爾射靈	上級
		36488, //林德拜爾雙匕首	上級
		80078, //法利昂復仇者	上級
		80079, //法利昂戰爪		上級
		80080, //法利昂風暴者	上級
		80081, //法利昂報恨者	上級
		80082, //法利昂雙鈍器	上級
		80083, //法利昂雙刀		上級
		80317, //法利昂雙匕首	上級
	};
	int[] weapon_gread4 = 
	{
		36489, //安塔瑞斯鉋刀	特級
		36490, //安塔瑞斯殺刃	特級
		36491, //安塔瑞斯投弓	特級
		36492, //安塔瑞斯爆刃	特級
		36493, //安塔瑞斯切刃	特級
		36494, //安塔瑞斯風暴者	特級
		36495, //安塔瑞斯戰爪	特級
		36496, //安塔瑞斯復仇者	特級
		36497, //安塔瑞斯雙鈍器	特級
		36498, //安塔瑞斯雙刀	特級
		36499, //巴拉卡斯鉋刀	特級
		36500, //巴拉卡斯切刃	特級
		36501, //巴拉卡斯殺刃	特級
		36502, //巴拉卡斯投弓	特級
		36503, //巴拉卡斯爆刃	特級
		36504, //巴拉卡斯術杖	特級
		36505, //巴拉卡斯報恨者	特級
		36506, //林德拜爾鉋刀	特級
		36507, //林德拜爾投弓	特級
		36508, //林德拜爾殺刃	特級
		36509, //林德拜爾術杖	特級
		36510, //林德拜爾切刃	特級
		36511, //林德拜爾射靈	特級
		36512, //林德拜爾雙匕首	特級
		80084, //法利昂復仇者	特級
		80085, //法利昂戰爪		特級
		80086, //法利昂風暴者	特級
		80087, //法利昂報恨者	特級
		80088, //法利昂雙鈍器	特級
		80089, //法利昂雙刀		特級
		80318, //法利昂雙匕首	特級
	};
	int[] Armor_gread1 = 
	{
		35002, //黑暗艾迪奧斯頭盔	重裝用
		35003, //黑暗艾迪奧斯胸甲	重裝用
		35004, //黑暗艾迪奧斯脛甲	重裝用
		35005, //黑暗艾迪奧斯護手	重裝用
		35006, //黑暗艾迪奧斯靴		重裝用
		35008, //黑暗艾迪奧斯皮頭盔	輕裝用
		35009, //黑暗艾迪奧斯皮甲	輕裝用
		35010, //黑暗艾迪奧斯皮脛甲	輕裝用
		35011, //黑暗艾迪奧斯皮手套	輕裝用
		35012, //黑暗艾迪奧斯皮靴	輕裝用
		35013, //黑暗艾迪奧斯頭箍	長袍用
		35014, //黑暗艾迪奧斯外衣	長袍用
		35015, //黑暗艾迪奧斯長襪	長袍用
		35016, //黑暗艾迪奧斯手套	長袍用
		35017, //黑暗艾迪奧斯鞋		長袍用
	};
	int[] Armor_gread2 = 
	{
		27989, //真艾迪奧斯頭盔		重裝用
		27990, //真艾迪奧斯胸甲		重裝用
		27991, //真艾迪奧斯脛甲		重裝用
		27992, //真艾迪奧斯護手		重裝用
		27993, //真艾迪奧斯靴		重裝用
		27994, //真艾迪奧斯皮頭盔	輕裝用
		27995, //真艾迪奧斯皮甲		輕裝用
		27996, //真艾迪奧斯皮脛甲	輕裝用
		27997, //真艾迪奧斯皮手套	輕裝用
		27998, //真艾迪奧斯皮靴		輕裝用
		27999, //真艾迪奧斯頭箍		長袍用
		28000, //真艾迪奧斯外衣		長袍用
		28001, //真艾迪奧斯長襪		長袍用
		28002, //真艾迪奧斯手套		長袍用
		28003, //真艾迪奧斯鞋		長袍用
	};
	int[] Armor_gread3 = 
	{
		80220, //黑暗利維坦頭盔		重裝用
		80221, //黑暗利維坦胸甲		重裝用
		80222, //黑暗利維坦脛甲		重裝用
		80223, //黑暗利維坦手套		重裝用
		80224, //黑暗利維坦靴		重裝用
		80225, //黑暗利維坦皮頭盔	輕裝用
		80226, //黑暗利維坦皮甲		輕裝用
		80227, //黑暗利維坦皮脛甲	輕裝用
		80228, //黑暗利維坦皮手套	輕裝用
		80229, //黑暗利維坦皮靴		輕裝用
		80230, //黑暗利維坦頭箍		長袍用
		80231, //黑暗利維坦外衣		長袍用
		80232, //黑暗利維坦長襪		長袍用
		80233, //黑暗利維坦手套		長袍用
		80234, //黑暗利維坦鞋		長袍用
	};
	int[] Armor_gread4 = 
	{
		80235, //限定利維坦頭盔		重裝用
		80236, //限定利維坦胸甲		重裝用
		80237, //限定利維坦脛甲		重裝用
		80238, //限定利維坦手套		重裝用
		80239, //限定利維坦靴		重裝用
		80240, //限定利維坦皮頭盔	輕裝用
		80241, //限定利維坦皮甲		輕裝用
		80242, //限定利維坦皮脛甲	輕裝用
		80243, //限定利維坦皮手套	輕裝用
		80244, //限定利維坦皮靴		輕裝用
		80245, //限定利維坦頭箍		長袍用
		80246, //限定利維坦外衣		長袍用
		80247, //限定利維坦長襪		長袍用
		80248, //限定利維坦手套		長袍用
		80249, //限定利維坦鞋		長袍用
	};
}
