package l2s.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import l2s.commons.dbutils.DbUtils;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.Zone.ZoneType;
import l2s.gameserver.utils.ItemFunctions;

/**
 * @author iqman
 * @reworked by Bonux
**/
public class PvPRewardManager
{
	private static final String PVP_REWARD_VAR = "@pvp_manager";
	
	private static final boolean no_msg = Config.DISALLOW_MSG_TO_PL;
	
	private static boolean basicCheck(Player killed, Player killer)
	{
		if(killed == null || killer == null)
			return false;

		if(killed.getLevel() < Config.PVP_REWARD_MIN_PL_LEVEL)
		{
			if(!no_msg)
			{
				if(killer.isLangRus())
					killer.sendMessage("PvP系統：您殺死了一名玩家，但是他的等級太低了。建議等級：" + Config.PVP_REWARD_MIN_PL_LEVEL);
				else
					killer.sendMessage("PvP系统：您杀死了一名玩家，但是他的等级太低了。建议等级：" + Config.PVP_REWARD_MIN_PL_LEVEL);
			}		
			return false;
		}

		if(killed.getClassLevel().ordinal() < Config.PVP_REWARD_MIN_PL_PROFF)
		{
			if(!no_msg)
			{
				if(killer.isLangRus())
					killer.sendMessage("PvP系統：您殺死了一名玩家，但是他的職業太低了。建議職業：" + (Config.PVP_REWARD_MIN_PL_PROFF - 1));
				else
					killer.sendMessage("PvP系统：您杀死了一名玩家，但是他的职业太低了。建议职业：" + (Config.PVP_REWARD_MIN_PL_PROFF - 1));
			}		
			return false;
		}

		if((System.currentTimeMillis() - killer.getLastAccess()) < (Config.PVP_REWARD_MIN_PL_UPTIME_MINUTE * 60000))
		{
			if(!no_msg)
			{
				if(killer.isLangRus())
					killer.sendMessage("PvP系統：您殺死了一名玩家，但是您當前在遊戲時間過低，建議最低遊戲時間：" + Config.PVP_REWARD_MIN_PL_UPTIME_MINUTE + " 分鐘。");
				else
					killer.sendMessage("PvP系统：您杀死了一名玩家，但是您当前在游戏时间过低，建议最低游戏时间：" + Config.PVP_REWARD_MIN_PL_UPTIME_MINUTE + " 分钟。");
			}			
			return false;
		}

		if((System.currentTimeMillis() - killed.getLastAccess()) < (Config.PVP_REWARD_MIN_PL_UPTIME_MINUTE * 60000))
		{
			if(!no_msg)
			{
				if(killer.isLangRus())
					killer.sendMessage("PvP系統：您殺死了一名玩家，但是他當前在遊戲時間過低，建議最少遊戲時間：" + Config.PVP_REWARD_MIN_PL_UPTIME_MINUTE + " 分鐘。");
				else
					killer.sendMessage("PvP系统：您杀死了一名玩家，但是他当前在游戏时间过低，建议最少游戏时间：" + Config.PVP_REWARD_MIN_PL_UPTIME_MINUTE + " 分钟。");
			}		
			return false;
		}

		if(!Config.PVP_REWARD_PK_GIVE && killer.isPK())
		{
			if(!no_msg)
			{
				if(killer.isLangRus())
					killer.sendMessage("PvP系統：您殺死了一名玩家，但不允許PK殺死。");
				else
					killer.sendMessage("PvP系统：您杀死了一名玩家，但不允许PK杀死。");
			}			
			return false;
		}

		if(!Config.PVP_REWARD_ON_EVENT_GIVE && (killer.isInOlympiadMode() || killer.getTeam() != TeamType.NONE))
		{
			if(!no_msg)
			{
				if(killer.isLangRus())
					killer.sendMessage("PvP系統：您殺死了一名玩家，但活動期間內殺死不算在內。");	
				else
					killer.sendMessage("PvP系统：您杀死了一名玩家，但活动期间内杀死不算在内。");
			}		
			return false;
		}

		if(Config.PVP_REWARD_ONLY_BATTLE_ZONE && (!killer.isInZone(ZoneType.battle_zone) || !killed.isInZone(ZoneType.battle_zone)))
		{
			if(!no_msg)
			{
				if(killer.isLangRus())
					killer.sendMessage("PvP系統：您殺死了一名玩家，只允許在戰場上殺死。");
				else
					killer.sendMessage("PvP系统：您杀死了一名玩家，只允许在战场上杀死。");
			}			
			return false;
		}

		if(!Config.PVP_REWARD_SAME_PARTY_GIVE)
		{
			if(killer.getParty() != null && killer.getParty() == killed.getParty() && (killer.getParty().getCommandChannel() == null || killer.getParty().getCommandChannel() == killed.getParty().getCommandChannel()))
			{
				if(!no_msg)
				{
					if(killer.isLangRus())
						killer.sendMessage("PvP系統：您殺死了一名玩家，但歸屬於同隊伍中，不算在內。");	
					else
						killer.sendMessage("PvP系统：您杀死了一名玩家，但归属于同队伍中，不算在内。");
				}				
				return false;
			}
		}

		if(!Config.PVP_REWARD_SAME_CLAN_GIVE && killer.getClan() != null && killer.getClan() == killed.getClan())
		{
			if(!no_msg)
			{
				if(killer.isLangRus())
					killer.sendMessage("PvP系統：您殺死了一名玩家，但歸屬於同血盟中，不算在內。");	
				else
					killer.sendMessage("PvP系统：您杀死了一名玩家，但归属于同血盟中，不算在内。");
			}
			return false;
		}

		if(!Config.PVP_REWARD_SAME_ALLY_GIVE && killer.getAllyId() > 0 && killer.getAllyId() == killed.getAllyId())
		{
			if(!no_msg)
			{
				if(killer.isLangRus())
					killer.sendMessage("PvP系統：您殺死了一名玩家，但歸屬於同聯盟中，不算在內。");
				else
					killer.sendMessage("PvP系统：您杀死了一名玩家，但归属于同联盟中，不算在内。");
			}		
			return false;
		}

		if(killer.getNetConnection() != null && killer.getNetConnection().getHWID() != null && killed.getNetConnection() != null && killed.getNetConnection().getHWID() != null)
		{
			if(!Config.PVP_REWARD_SAME_HWID_GIVE && killer.getNetConnection().getHWID().equals(killed.getNetConnection().getHWID()))
			{
				if(!no_msg)
				{
					if(killer.isLangRus())
						killer.sendMessage("PvP系統：您殺死了一名玩家，但似乎都在同一台PC上玩家，這是不允許的。");	
					else
						killer.sendMessage("PvP系统：您杀死了一名玩家，但似乎都在同一台PC上玩家，这是不允许的。");
				}		
				return false;
			}
		}

		if(!Config.PVP_REWARD_SAME_IP_GIVE && killer.getIP().equals(killed.getIP()))
		{
			if(!no_msg)
			{
				if(killer.isLangRus())
					killer.sendMessage("PvP系統：您殺死了一名玩家，但似乎都在同IP上玩家，這是不允許的。");
				else
					killer.sendMessage("PvP系统：您杀死了一名玩家，但似乎都在同IP上玩家，这是不允许的。");
			}
			return false;
		}

		if(Config.PVP_REWARD_SPECIAL_ANTI_TWINK_TIMER && (System.currentTimeMillis() - killed.getCreateTime()) < (Config.PVP_REWARD_HR_NEW_CHAR_BEFORE_GET_ITEM * 60000 * 60))
		{
			if(!no_msg)
			{
				if(killer.isLangRus())
					killer.sendMessage("PvP系統：您殺死了一名玩家，但是此角色短時間內創建的，建議創建角色不少於："+Config.PVP_REWARD_HR_NEW_CHAR_BEFORE_GET_ITEM+" 小時！");	
				else
					killer.sendMessage("PvP系统：您杀死了一名玩家，但是此角色短时间内创建的，建议创建角色不少于："+Config.PVP_REWARD_HR_NEW_CHAR_BEFORE_GET_ITEM+" 小时！");
			}		
			return false;
		}

		if(Config.PVP_REWARD_CHECK_EQUIP && !checkEquip(killed))
		{
			if(!no_msg)
			{
				if(killer.isLangRus())
					killer.sendMessage("PvP系統：您殺死了一名玩家，但他的裝備很低。");	
				else
					killer.sendMessage("PvP系统：您杀死了一名玩家，但他的装备很低。");
			}			
			return false;	
		}
		return true;	
	}

	private static boolean checkEquip(Player killed)
	{
		if(killed.getWeaponsExpertisePenalty() > 0 || killed.getArmorsExpertisePenalty() > 0)
			return false;

		ItemInstance weapon = killed.getActiveWeaponInstance();
		if(weapon == null)
			return false;

		if(weapon.getGrade().extOrdinal() < Config.PVP_REWARD_WEAPON_GRADE_TO_CHECK)
			return false;

		return true;	
	}
	
	public static void tryGiveReward(Player victim, Player player)
	{
		if(!Config.ALLOW_PVP_REWARD)
			return;

		if(!isNoDelayActive(victim, player))
		{
			if(player.isLangRus())
				player.sendMessage("PvP系統：您最近已經殺死過此玩家！短時間內殺死不累計。");
			else
				player.sendMessage("PvP系统：您最近已经杀死过此玩家！短时间内杀死不累计。");
			return;
		}

		if(!basicCheck(victim,player))
			return;

		victim.setVar(PVP_REWARD_VAR + "_" + player.getObjectId(), true, (System.currentTimeMillis() + (Config.PVP_REWARD_DELAY_ONE_KILL * 1000)));

		giveItem(player);

		if(Config.PVP_REWARD_LOG_KILLS)
			logCombat(player, victim);

		if(Config.PVP_REWARD_SEND_SUCC_NOTIF)
		{
			if(victim.isLangRus())
				victim.sendMessage("PvP系統：您已被殺害！");
			else
				victim.sendMessage("PvP系统：您已被杀害！");

			if(player.isLangRus())
				player.sendMessage("PvP系統：您殺死了玩家！");
			else
				player.sendMessage("PvP系统：您杀死了玩家！");
		}
	}

	private static void logCombat(Player killer, Player victim)
	{
		String kill_name = killer.getName();
		String victim_name = victim.getName();
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO pvp_system_log (killer,victim) values (?,?)");
			statement.setString(1, kill_name);
			statement.setString(2, victim_name);
			statement.execute();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}		
	}

	private static void giveItem(Player player)
	{
		if(player == null)
			return;

		if(Config.PVP_REWARD_REWARD_IDS.length != Config.PVP_REWARD_COUNTS.length)
			return;

		if(Config.PVP_REWARD_RANDOM_ONE)
		{
			int index = Rnd.get(Config.PVP_REWARD_REWARD_IDS.length);
			int rewardId = Config.PVP_REWARD_REWARD_IDS[index];
			long rewardCount = Config.PVP_REWARD_COUNTS[index];
			if(rewardId > 0 && rewardCount > 0)
				ItemFunctions.addItem(player, rewardId, rewardCount, true);
		}
		else
		{
			for(int i = 0 ; i < Config.PVP_REWARD_REWARD_IDS.length - 1 ; i++)
			{
				int rewardId = Config.PVP_REWARD_REWARD_IDS[i];
				long rewardCount = Config.PVP_REWARD_COUNTS[i];
				if(rewardId > 0 && rewardCount > 0)
					ItemFunctions.addItem(player, Config.PVP_REWARD_REWARD_IDS[i], Config.PVP_REWARD_COUNTS[i], true);
			}
		}
	}

	private static boolean isNoDelayActive(Player victim, Player killer)
	{
		String delay = victim.getVar(PVP_REWARD_VAR + "_" + killer.getObjectId());
		if(delay == null)
			return true;
		return false;	
	}	
}