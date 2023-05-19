package npc.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import handler.bbs.custom.BBSConfig;
import l2s.commons.collections.MultiValueSet;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Language;
/**
 * NPC輔助
 **/
public class BufferInstance extends NpcInstance
{
	public BufferInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		final String[] buypassOptions = command.split(" ");
		if(command.startsWith("Chat"))
		{
			int val = 0;
			try
			{
				val = Integer.parseInt(command.substring(5));
			}
			catch(IndexOutOfBoundsException ioobe)
			{}
			catch(NumberFormatException nfe)
			{}
			showChatWindow(player, val, false);
		}
		else if(command.startsWith("SuperBuff"))//這一區是會員區的指令才給buff
		{
			if(player.hasPremiumAccount())//先判斷會員
			{
				int ids = 0;
				int lv = 1;
				for(int i = 1; i < buypassOptions.length; i++)
				{
					String[] obj = buypassOptions[i].split(",");
					
					if(obj.length == 2)
					{
						ids = Integer.parseInt(obj[0]);
						lv = Integer.parseInt(obj[1]);
					}
					else {
						ids = Integer.parseInt(buypassOptions[i]);
						lv = 1;
					}

					SkillEntry skk = SkillEntry.makeSkillEntry(SkillEntryType.NONE, ids, lv);
					boolean success = false;
					success = skk.getEffects(player, player);
					if(success)
						player.broadcastPacket(new MagicSkillUse(player, player, 23128, 1, 1, 0));
				}
			}
			else
			{
				if(player.getLanguage() == Language.CHINESE_TW)
				{
					player.sendMessage("高級帳號可免費使用。");
				}
				else
				{
					player.sendMessage("高级帐号可免费使用。");
				}
			}
		}
		else if(command.startsWith("Buff"))
		{
			int item = BBSConfig.BUFF_SERVICE_NPC_COST_ITEM_ID;//預設是要花費的
			long price = BBSConfig.BUFF_SERVICE_NPC_COST_ITEM_COUNT;//預設是要花費的
			String name = "";
			if( BBSConfig.BUFF_SERVICE_NPC_COST_ITEM_ID > 0)
			{
				name = ItemFunctions.createItem(BBSConfig.BUFF_SERVICE_NPC_COST_ITEM_ID).getName(); //消費的物品id
			}
			//String name = HtmlUtils.htmlItemName(BBSConfig.BUFF_SERVICE_NPC_COST_ITEM_ID); //消費的物品id
			price = price * buypassOptions.length;
			
			if(BBSConfig.BUFF_SERVICE_NPC_COST_FOR_PREMIUM_FREE && player.hasPremiumAccount())//player.hasPremiumAccount()
			{
				if(player.getLanguage() == Language.CHINESE_TW)
				{
					player.sendMessage("高級帳號可免費使用。");
				}
				else
				{
					player.sendMessage("高级帐号可免费使用。");
				}
			}
			else
			{
				if(item > 0 && price > 0)//先判斷是否有設置收費
				{
					if(player.getLevel() > BBSConfig.BUFF_SERVICE_NPC_MAX_LEVEL_FOR_FREE_BUFF)//超過等級
					{

						if(!ItemFunctions.deleteItem(player, item, price))
						{
							if(player.getLanguage() == Language.CHINESE_TW)
							{
								player.sendMessage("需求物品「" + name + "」數量不足「" + price + "」個。");
							}
							else
							{
								player.sendMessage("需求物品「" + name + "」数量不足「" + price + "」个。");
							}
							return;
						}
					}
				}
			}
			int ids = 0;
			int lv = 1;
			for(int i = 1; i < buypassOptions.length; i++)
			{
				String[] obj = buypassOptions[i].split(",");

				if(obj.length == 2)
				{
					ids = Integer.parseInt(obj[0]);
					lv = Integer.parseInt(obj[1]);
				}
				else
				{
					ids = Integer.parseInt(buypassOptions[i]);
					lv = 1;
				}
				SkillEntry skk = SkillEntry.makeSkillEntry(SkillEntryType.NONE, ids, lv);
				boolean success = false;
				success = skk.getEffects(player, player);
				if(success)
					player.broadcastPacket(new MagicSkillUse(player, player, 23128, 1, 1, 0));
			}
		}
		else if(command.startsWith("dropLv"))//指令 dropLv
		{
			if(player.getLevel() == 86)
			{
				int counts = CheckDataBaseObjId(player);//取得他轉生的次數
				if(counts >= 40) // 假設轉生次數上限為40次。
				{
					if(player.getLanguage() == Language.CHINESE_TW)
					{
						player.sendMessage("你已轉生40次已達頂峰。");
					}
					else
					{
						player.sendMessage("你已转生40次已达顶峰");
					}
					return;
				}
				int need_item = 47930;
				int need_item_counts = 100 + counts * 50;
				switch( player.getRace())
				{
					case ELF:
						need_item = 47930;//需求物品
						break;
					case DARKELF:
						need_item = 47931;
						break;
					case ORC:
						need_item = 47932;
						break;
					case DWARF:
						need_item = 47933;
						break;
					case HUMAN :
						need_item = 47934;
						break;
				}
				String ItemName = ItemHolder.getInstance().getTemplate(need_item).getName();
				//Item.
				 //ItemFunctions.createItem(111).getName();
				if(!ItemFunctions.deleteItem(player, need_item, need_item_counts))//這裡是設置需求物品數量。
				{
					if(player.getLanguage() == Language.CHINESE_TW)
					{
						player.sendMessage("需求物品「" + ItemName + "」數量不足「" + need_item_counts + "」個。");
					}
					else
					{
						player.sendMessage("需求物品「" + ItemName + "」数量不足「" + need_item_counts + "」个。");
					}
					
					return;
				}
				
				Long exp_add = Experience.getExpForLevel(1) - player.getExp();
				player.addExpAndSp(exp_add, 0, true);//設置等級1
				//ItemFunctions.addItem(player, 57, 5000, true);//给啥物品 再定义过或是只给点数？
				InsertDataBaseObjId(player);				
			}
			else
			{
				if(player.getLanguage() == Language.CHINESE_TW)
				{
					player.sendMessage("請等級達到86級再來。");
				}
				else
				{
					player.sendMessage("请等级达到86级再来");
				}
			}
		}
		else if(command.startsWith("StrongLevel"))//指令 ShowTimes
		{
			if(buypassOptions.length == 3)
			{
				int ids = Integer.parseInt(buypassOptions[1]);
				int lv = Integer.parseInt(buypassOptions[2]);
				int nowLevel = player.getSkillLevel(ids);//玩家身上目前技能等級
				int DataBaseLevel = GetPlayerSkillLevel(player,ids);//資料庫上玩家目前等級
				nowLevel = Math.max(nowLevel, 0);
				int Sccess = 0;
				if(nowLevel != DataBaseLevel)//判斷資料庫跟玩家身上的等級需要一致
				{
					//告知玩家系統異常。等級與資料庫不同步
					if(player.getLanguage() == Language.CHINESE_TW)
					{
						player.sendMessage("轉生系統異常，請與GM聯絡。");
					}
					else
					{
						player.sendMessage("转生系统异常，请与GM联络。");
					}
				}
				if((nowLevel + 1) == lv)//判斷下一級是否是目前等級+1
				{
					if(lv == 1)//如果等級1就需要新增
					{
						Sccess = InsertPlayerSkillLevel(player, ids, lv);
					}
					else//如果等級超過1就要更新
					{
						Sccess = UpdatePlayerSkillLevel(player, ids, lv);
					}
				}
				if(Sccess == 1)
				{
					SkillEntry skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, ids, lv);
					player.addSkill(skillEntry, true);
					player.sendSkillList();
					if(player.getLanguage() == Language.CHINESE_TW)
					{
						player.sendMessage("轉生強化成功。");
					}
					else
					{
						player.sendMessage("转生强化成功。");
					}
				}
				else
				{
					if(player.getLanguage() == Language.CHINESE_TW)
					{
						player.sendMessage("強化轉生技能失敗請與GM聯絡。");
					}
					else
					{
						player.sendMessage("强化转生技能失败请与GM联络。");
					}
				}			
			}
			SendMessage(player);
		}
		else if(command.startsWith("DeleteLevel"))//指令 ShowTimes
		{
			if(buypassOptions.length == 3)
			{
				int ids = Integer.parseInt(buypassOptions[1]);
				int lv = Integer.parseInt(buypassOptions[2]);
				int nowLevel = player.getSkillLevel(ids);//玩家身上目前技能等級
				nowLevel = Math.max(nowLevel, 0);
				int DataBaseLevel = GetPlayerSkillLevel(player,ids);//資料庫上玩家目前等級
				int Sccess = 0;
				if(nowLevel != DataBaseLevel)//判斷資料庫跟玩家身上的等級需要一致
				{
					//告知玩家系統異常。等級與資料庫不同步
					if(player.getLanguage() == Language.CHINESE_TW)
					{
						player.sendMessage("轉生系統異常，請與GM聯絡。");
					}
					else
					{
						player.sendMessage("转生系统异常，请与GM联络。");
					}
				}
				if((nowLevel - 1) == lv)//判斷掉級是否是目前等級-1
				{
					if(lv == 0)//如果等級1就需要刪除
					{
						Sccess = DeletePlayerSkillLevel(player, ids);
					}
					else//如果等級超過1就要更新
					{
						Sccess = UpdatePlayerSkillLevel(player, ids, lv);
					}
				}
				if(Sccess == 1)
				{
					player.removeSkill(ids, true);
					if(lv != 0)//如果等級1就需要刪除
					{
						SkillEntry skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, ids, lv);
						player.addSkill(skillEntry, true);
						player.sendSkillList();
					}
					
					if(player.getLanguage() == Language.CHINESE_TW)
					{
						player.sendMessage("移除強化成功。");
					}
					else
					{
						player.sendMessage("移除强化成功。");
					}
				}
				else
				{
					if(player.getLanguage() == Language.CHINESE_TW)
					{
						player.sendMessage("轉生技能移除失敗請與GM聯絡。");
					}
					else
					{
						player.sendMessage("转生技能移除失败请与GM联络。");
					}
				}			
			}
			SendMessage(player);
		}
		else if(command.startsWith("ResetLevel"))//指令 重置轉身點數
		{
			for(int i = 0; i < 7; i++)
			{
				player.removeSkill(30099 + i,true);//刪除自身所有技能
			}
			ResetPlayerSkill(player);//刪除資料庫所有的技能
			SendMessage(player);
		}
		else if(command.startsWith("ShowTimes"))//指令 ShowTimes
		{
			SendMessage(player);
		}
		else
			super.onBypassFeedback(player, command);
	}
	private void SendMessage(Player player)
	{
		HtmlMessage msg = new HtmlMessage(5);
		int reborntimes = CheckDataBaseObjId(player);//
		int[] Skilllevel = new int[7];//30099-30105
		int[] Toplevel = { 20, 20, 15, 15, 20, 15, 15 };//最高等級數量
		//30099-30105
		int sum = 0;
		for(int i = 0; i < 7; i++)
		{
			Skilllevel[i] = player.getSkillLevel(30099 + i);//取得技能等级
			if(Skilllevel[i] == -1)
			{
				Skilllevel[i] = 0;
			}
			sum += Skilllevel[i];
		}
		msg.setFile("default/4362-1.htm");
		msg.replace("%count%", String.valueOf(reborntimes));
		msg.replace("%RemainPoints%", String.valueOf(reborntimes - sum));
		for(int i = 0; i < 7; i++)
		{
			msg.replace("%S" + (30099 + i) + "%", String.valueOf(Skilllevel[i]));
			if(reborntimes > sum)//這裡是判斷是否出現強化 或是 滿級狀態
			{
				if(Skilllevel[i] < Toplevel[i])//判斷是否滿級狀態
				{
					if(player.getLanguage() == Language.CHINESE_TW)
					{
						msg.replace("%Strong" + (30099 + i) + "%", "<td align=center><button  value=\"強化\" action=\"bypass -h npc?StrongLevel " + (30099 + i) + " " + (Skilllevel[i] + 1) + "\" width=50 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
					}
					else
					{
						msg.replace("%Strong" + (30099 + i) + "%", "<td align=center><button  value=\"强化\" action=\"bypass -h npc?StrongLevel " + (30099 + i) + " " + (Skilllevel[i] + 1) + "\" width=50 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
					}
				}
				else
				{
					if(player.getLanguage() == Language.CHINESE_TW)
					{
						msg.replace("%Strong" + (30099 + i) + "%", "<td align=center><button  value=\"滿級\" action=\"\" width=50 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
					}
					else
					{
						msg.replace("%Strong" + (30099 + i) + "%", "<td align=center><button  value=\"满级\" action=\"\" width=50 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
					}
				}
			}
			if(Skilllevel[i] > 0)//如果出現了技能有學習則跑出可刪除的選項
			{
				if(player.getLanguage() == Language.CHINESE_TW)
				{
					msg.replace("%Delete" + (30099 + i) + "%", "<td align=center><button  value=\"移除\" action=\"bypass -h npc?DeleteLevel " + (30099 + i) + " " + (Skilllevel[i] - 1) + "\" width=50 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				}
				else
				{
					msg.replace("%Delete" + (30099 + i) + "%", "<td align=center><button  value=\"移除\" action=\"bypass -h npc?DeleteLevel " + (30099 + i) + " " + (Skilllevel[i] - 1) + "\" width=50 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				}
			}
			else
			{
				if(player.getLanguage() == Language.CHINESE_TW)
				{
					msg.replace("%Delete" + (30099 + i) + "%", "<td align=center><button  value=\"\" action=\"\" width=50 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				}
				else
				{
					msg.replace("%Delete" + (30099 + i) + "%", "<td align=center><button  value=\"\" action=\"\" width=50 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>");
				}
			}
		}
		player.sendPacket(msg);
	}
	private void InsertDataBaseObjId(Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			//statement.setInt(7, (int) (System.currentTimeMillis() / 1000));
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO character_level_down (obj_Id, char_name, post_date) VALUES (?, ?, ?)");
			statement.setInt(1, player.getObjectId());
			statement.setString(2, player.getName());
			statement.setInt(3, (int) (System.currentTimeMillis() / 1000));
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
	private int CheckDataBaseObjId(Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		int count = 0 ;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT count(*) as cnt FROM character_level_down WHERE `obj_Id` = ?");
			statement.setInt(1, player.getObjectId());
			rset = statement.executeQuery();
			if(rset.next())
				count = rset.getInt("cnt");
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
		return count;
	}
	private int ResetPlayerSkill(Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		int count = 1;//1表示成功
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_reborn_skill WHERE obj_Id = ? ");
			statement.setInt(1, player.getObjectId());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			count = 0;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return count;
	}
	private int DeletePlayerSkillLevel(Player player,int skillid)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		int count = 1;//1表示成功
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_reborn_skill WHERE obj_Id = ? AND skill_id = ? ");
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, skillid);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			count = 0;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return count;
	}
	private int UpdatePlayerSkillLevel(Player player,int skillid,int skilllevel)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		int count = 1;//1表示成功
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE character_reborn_skill SET skill_level = ? WHERE obj_Id = ? AND skill_id = ? ");
			statement.setInt(1, skilllevel);
			statement.setInt(2, player.getObjectId());
			statement.setInt(3, skillid);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			count = 0;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return count;
	}
	private int InsertPlayerSkillLevel(Player player,int skillid,int skilllevel)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		int count = 1;//1表示成功
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO character_reborn_skill(obj_Id, skill_id, skill_level) VALUES (?,?,?)");
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, skillid);
			statement.setInt(3, skilllevel);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			count = 0;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return count;
	}
	private int GetPlayerSkillLevel(Player player,int skillid)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		int count = 0 ;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT skill_level FROM character_reborn_skill WHERE `obj_Id` = ? and skill_id = ? ");
			statement.setInt(1, player.getObjectId());
			statement.setInt(2, skillid);
			rset = statement.executeQuery();
			if(rset.next())
				count = rset.getInt("skill_level");
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
		return count;
	}
}
