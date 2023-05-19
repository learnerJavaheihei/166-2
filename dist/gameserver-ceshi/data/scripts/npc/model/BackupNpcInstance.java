package npc.model;//恢復資料用的NPC

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.collections.MultiValueSet;
import l2s.commons.dbutils.DbUtils;
//import l2s.gameserver.Announcements;
import l2s.gameserver.data.xml.holder.ItemHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.c2s.CharacterCreate;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Log;

/**
 * @author Bonux
 **/
public class BackupNpcInstance extends NpcInstance
{
	private static final Logger _log = LoggerFactory.getLogger(CharacterCreate.class);
	public BackupNpcInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}

	@Override
	public void showChatWindow(Player player, int val, boolean firstTalk, Object... replace)
	{
		if(val == 0)
		{
			showChatWindow(player, "default/backup/" + getNpcId() + ".htm", firstTalk);
		}
		else
		{
			showChatWindow(player, "default/backup/" + getNpcId() + "-" + val + ".htm", firstTalk);
		}
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		final String[] buypassOptions = command.split(" ");
		//Announcements.announceToAll("buypassOptions[0].equals(ensoulArmor)" + buypassOptions[0].equals("ensoulArmor"));
		if(command.startsWith("Chat"))
		{
			showChatWindow(player, "default/backup/" + getNpcId() + "-" + buypassOptions[1] + ".htm", false);
		}
		else if(command.startsWith("myLevel"))//
		{
			if(CheckPlayerCanUse(player.getName(), 1))//傳回true表示有記錄了
			{
				player.sendMessage("已執行過，無法再使用。");
				return;
			}
			PlayerGetLevel(player);//給等級
			InsertPlayerLevel(player.getName(), 1);
			PlayerGetSkills(player);//給技能
			InsertPlayerLevel(player.getName(), 2);//
			PlayerGetDaily(player);//每日任務
			InsertPlayerLevel(player.getName(), 3);
		}
		else if(command.startsWith("myItems"))//
		{
			if(CheckPlayerCanUse(player.getName(), 4))//傳回true表示有記錄了
			{
				player.sendMessage("已執行過，無法再使用。");
				return;
			}
			PlayerGetItems(player);
			InsertPlayerLevel(player.getName(), 4);
		}
	}

	int[] not_Items = { //不要回復的物品清單
			91481,//靈魂結晶
			91400,//秘術書-戰甲破壞
			91401,//秘術書-卸除武器
			91402,//秘術書-咒術破壞
			91403,//秘術書-閃光
			91404,//秘術書-曲速
			91405,//秘術書-狂戰士模式
			91393,//遺忘秘術書-第1章
			91394,//遺忘秘術書-第2章
			91395,//遺忘秘術書-第3章
			91396,//遺忘秘術書-第4章
			8556,
	};
	
	
	private void PlayerGetItems(Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT item_id, count ,enchant_level ,option1 ,option2 FROM _backup_items where char_name = ?");
			statement.setString(1, player.getName());
			rset = statement.executeQuery();
			while(rset.next())
			{
				int item_id = rset.getInt("item_id");
				long count = rset.getLong("count");//修正錢過多
				
				int enchant_level = rset.getInt("enchant_level");
				int option1 = rset.getInt("option1");
				int option2 = rset.getInt("option2");
				
				if(!ArrayUtils.contains(not_Items, item_id))
				{
					if(ItemHolder.getInstance().getTemplate(item_id) != null)
					{
						ItemInstance createditem = ItemFunctions.createItem(item_id);
						if(count > 1 && createditem.isStackable())
						{
							createditem.setCount(count);
						}
						else
						{
							if(enchant_level > 0)
							{
								createditem.setEnchantLevel(enchant_level);
							}
							if(option1 > 0 && option2 > 0)
							{

								createditem.setVariation1Id(option1);
								createditem.setVariation2Id(option2);
							}
						}
						player.getInventory().addItem(createditem);
					}
					else
					{
						_log.info("玩家:" + player.getName() + " 物品id " + item_id + " 不存在。");
					}
				}
			}
		}
		catch(Exception e)
		{
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}
	private void PlayerGetDaily(Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT rewardId, lastCompleted FROM _backup_character_daily_missions where char_name = ?");
			statement.setString(1, player.getName());
			rset = statement.executeQuery();
			Map<Integer, Integer> Dailys = new HashMap<>();
			while(rset.next())
			{
				int rewardId = rset.getInt("rewardId");
				int lastCompleted = rset.getInt("lastCompleted");
				Dailys.put(rewardId, lastCompleted);
			}
			DbUtils.close(statement);
			statement = con.prepareStatement("replace into character_daily_missions(char_id,mission_id,completed,value) values(?,?,?,?)");

			for(int d : Dailys.keySet())
			{
				statement.setInt(1, player.getObjectId());//char_id
				statement.setInt(2, d);//mission_id
				int lastCompleted = Dailys.get(d);
				if(lastCompleted > 0)
				{
					statement.setInt(3, 1);//完成
				}
				else
				{
					statement.setInt(3, 0);//沒完成
				}
				statement.setInt(4, lastCompleted);
				statement.addBatch();
			}
			statement.executeBatch();
		}
		catch(Exception e)
		{
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	//這一些技能不還原
	int[] not_skills = { 11400, 11401, 11402, 11403, 11404, 51247, 51248, 51249, 51250, 51251, 51252, 51271, 51272, 51273, 51274, 51275, 51276, 51277, 51278, 51279, 51280, 51281, 60003, 60004, 60005, 60006, 60007, 60008, 60009, 60010, 60011, 60012, 60013, 60014,
	};
	private void PlayerGetSkills(Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT skill_id, skill_level FROM _backup_character_skills where char_name = ?");
			statement.setString(1, player.getName());
			rset = statement.executeQuery();
			while(rset.next())
			{
				int skill_id = rset.getInt("skill_id");
				int skill_level = rset.getInt("skill_level");
				if(!ArrayUtils.contains(not_skills, skill_id))
				{
					SkillEntry sk = SkillEntry.makeSkillEntry(SkillEntryType.NONE, skill_id, skill_level);
					if(sk != null)
					{
						player.addSkill(sk, true);
					}
					else
					{
						_log.info("玩家:" + player.getName() + " 技能 " + skill_id + " 等級 " + skill_level + " 不存在。");
					}

				}
			}
			player.sendSkillList();
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

	private void PlayerGetLevel(Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT classid, exp, sp FROM _backup_characters where account_name= ? and char_name = ?");
			statement.setString(1, player.getAccountName());
			statement.setString(2, player.getName());
			//Announcements.announceToAll("player.getAccountName() " + player.getAccountName() + " player.getName() "+ player.getName() );
			rset = statement.executeQuery();
			if(rset.next())
			{
				//Announcements.announceToAll("有無資料？");
				int classId = rset.getInt("classid");
				long exp = rset.getLong("exp");
				long sp = rset.getLong("sp");
				//Announcements.announceToAll("exp" + exp + "classId" + classId );
				player.setClassId(classId, true);
				//playable.addExpAndSp(exp, sp);
				player.addExpAndSp(exp, sp);
			}else {
				_log.info("玩家:" + player.getName() + " 回復等級 不存在。");
			}
			statement.close();
		}
		catch(Exception e)
		{
			_log.info("玩家:" + player.getName() + " 回復等級 出錯誤了！");
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	private boolean InsertPlayerLevel(String account, int tp)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		boolean check = false;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("replace into _backup_ok(char_name,types) values(?,?)");
			statement.setString(1, account);
			statement.setInt(2, tp);
			rset = statement.executeQuery();

			statement.close();
		}
		catch(Exception e)
		{
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return check;
	}

	private boolean CheckPlayerCanUse(String account, int tp)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		boolean check = false;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT char_name FROM _backup_ok where char_name= ? and types = ?");
			statement.setString(1, account);
			statement.setInt(2, tp);
			rset = statement.executeQuery();
			if(rset.next())
			{
				check = true;
			}
			statement.close();
		}
		catch(Exception e)
		{
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return check;
	}
}
