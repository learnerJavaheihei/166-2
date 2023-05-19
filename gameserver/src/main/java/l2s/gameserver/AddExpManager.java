package l2s.gameserver;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;
import java.util.logging.Level;

import l2s.gameserver.Config;
import l2s.gameserver.Announcements.Announce;
import l2s.gameserver.data.string.StringsHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.base.PlayerAccess;
import l2s.gameserver.model.base.SubClassType;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.components.NpcString;
import l2s.gameserver.network.l2.s2c.SayPacket2;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.utils.ArabicConv;
import l2s.gameserver.utils.ChatUtils;
import l2s.gameserver.utils.Files;

import l2s.commons.dbutils.DbUtils;

public class AddExpManager extends TimerTask {

	public static int TopLv;
	public AddExpManager() {
	}
	
	@Override
	public void run()
	{
		CheckTask();
	}
	
	public void begin() {
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(this, 10 * 1000L, Config.CHECK_ADDEXP_TIME * 1000L);
	}
	
	
	public static void CheckTask() {
		TopLv = loadTopLv();
	}
	
	private static int loadTopLv() {
		
		int temp = 87;		
		Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT level from character_subclasses where level > 0 order by level desc limit 0 , 1");
			rs = pstm.executeQuery();
			while(rs.next())
			{
				temp = rs.getInt("level");
			}
			pstm.close();
		} catch (SQLException e) {

		} finally {
			DbUtils.closeQuietly(con, pstm, rs);
		}		
		return temp;
	}

}
