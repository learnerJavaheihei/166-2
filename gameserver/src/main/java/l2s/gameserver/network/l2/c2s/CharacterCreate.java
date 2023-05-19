package l2s.gameserver.network.l2.c2s;

import l2s.commons.dbutils.DbUtils;//恢復資料特寫
import l2s.gameserver.Announcements;//恢復資料特寫
import l2s.gameserver.Config;
import l2s.gameserver.dao.CharacterDAO;
import l2s.gameserver.database.DatabaseFactory;//恢復資料特寫
import l2s.gameserver.listener.hooks.ListenerHook;
import l2s.gameserver.listener.hooks.ListenerHookType;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.actor.instances.player.ShortCut;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.s2c.CharacterCreateFailPacket;
import l2s.gameserver.network.l2.s2c.CharacterCreateSuccessPacket;
import l2s.gameserver.network.l2.s2c.CharacterSelectionInfoPacket;
import l2s.gameserver.templates.item.StartItem;
import l2s.gameserver.templates.player.PlayerTemplate;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Util;

import java.sql.Connection;//恢復資料特寫
import java.sql.PreparedStatement;//恢復資料特寫
import java.sql.ResultSet;//恢復資料特寫

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CharacterCreate extends L2GameClientPacket
{
	private static final Logger _log = LoggerFactory.getLogger(CharacterCreate.class);

	// cSdddddddddddd
	private String _name;
	private int _sex;
	private int _classId;
	private int _hairStyle;
	private int _hairColor;
	private int _face;

	@Override
	protected boolean readImpl()
	{
		_name = readS();
		readD(); // race
		_sex = readD();
		_classId = readD();
		readD(); // int
		readD(); // str
		readD(); // con
		readD(); // men
		readD(); // dex
		readD(); // wit
		_hairStyle = readD();
		_hairColor = readD();
		_face = readD();
		return true;
	}

	@Override
	protected void runImpl()
	{
		ClassId cid = ClassId.valueOf(_classId);
		if(cid == null || !cid.isOfLevel(ClassLevel.NONE))
			return;

		if(!Util.isMatchingRegexp(_name, Config.CNAME_TEMPLATE))
			return;

		if(CharacterDAO.getInstance().getObjectIdByName(_name) > 0)
		{
			sendPacket(CharacterCreateFailPacket.REASON_CREATION_FAILED);//提示出錯
			return;
		}

		//這裡需要判斷 之前資料庫玩家的id是否存在，如果存在，再確認一下目前此玩家的帳號要跟以前一樣才可以創建這一個以前存在的角色。
		if(CheckPlayerAndAccountExists(getClient().getLogin(), _name))
		{
			sendPacket(CharacterCreateFailPacket.REASON_CREATION_FAILED);//提示出錯
			return;
		}		
		
		if(Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT != 0 && CharacterDAO.getInstance().accountCharNumber(getClient().getLogin()) >= Config.MAX_CHARACTERS_NUMBER_PER_ACCOUNT)
			return;

		if(_face > 2 || _face < 0)
		{
			_log.warn("Character Creation Failure: Character face " + _face + " is invalid. Possible client hack. " + getClient());
			sendPacket(CharacterCreateFailPacket.REASON_CREATION_FAILED);
			return;
		}

		if(_hairStyle < 0 || (_sex == 0 && _hairStyle > 4) || (_sex != 0 && _hairStyle > 6))
		{
			_log.warn("Character Creation Failure: Character hair style " + _hairStyle + " is invalid. Possible client hack. " + getClient());
			sendPacket(CharacterCreateFailPacket.REASON_CREATION_FAILED);
			return;
		}

		if(_hairColor > 3 || _hairColor < 0)
		{
			_log.warn("Character Creation Failure: Character hair color " + _hairColor + " is invalid. Possible client hack. " + getClient());
			sendPacket(CharacterCreateFailPacket.REASON_CREATION_FAILED);
			return;
		}

		Player newChar = Player.create(_classId, _sex, getClient().getLogin(), _name, _hairStyle, _hairColor, _face);
		if(newChar == null)
		{
			_log.warn("Character Creation Failure: Player.create returned null. Possible client hack. " + getClient());
			sendPacket(CharacterCreateFailPacket.REASON_CREATION_FAILED);
			return;
		}

		if(!initNewChar(newChar))
		{
			_log.warn("Character Creation Failure: Could not init new char. Possible client hack. " + getClient());
			sendPacket(CharacterCreateFailPacket.REASON_CREATION_FAILED);
			return;
		}
			
		sendPacket(CharacterCreateSuccessPacket.STATIC);

		getClient().setCharSelection(CharacterSelectionInfoPacket.loadCharacterSelectInfo(getClient().getLogin()));
	}

	public static boolean initNewChar(Player newChar)
	{
		if(!newChar.getSubClassList().restore())
			return false;

		PlayerTemplate template = newChar.getTemplate();
		newChar.setLoc(template.getStartLocation());

		if(Config.CHAR_TITLE)
			newChar.setTitle(Config.ADD_CHAR_TITLE);
		else
			newChar.setTitle("");

		newChar.setCurrentHpMp(newChar.getMaxHp(), newChar.getMaxMp());
		newChar.setCurrentCp(0); // retail

		for(StartItem i : template.getStartItems())
		{
			ItemInstance item = ItemFunctions.createItem(i.getId());
			if(i.getEnchantLevel() > 0)
				item.setEnchantLevel(i.getEnchantLevel());

			long count = i.getCount();
			if(item.isStackable())
			{
				item.setCount(count);
				newChar.getInventory().addItem(item);
			}
			else
			{
				for(long n = 0; n < count; n++)
				{
					item = ItemFunctions.createItem(i.getId());
					if(i.getEnchantLevel() > 0)
						item.setEnchantLevel(i.getEnchantLevel());
					newChar.getInventory().addItem(item);
				}
				if(item.isEquipable() && i.isEquiped())
					newChar.getInventory().equipItem(item);
			}
		}
		ItemInstance item = ItemFunctions.createItem(5249);//內掛道具放在快捷鍵--
		newChar.getInventory().addItem(item);//--內掛道具放在快捷鍵

		for(ListenerHook hook : ListenerHook.getGlobalListenerHooks(ListenerHookType.PLAYER_CREATE))
			hook.onPlayerCreate(newChar);

		newChar.rewardSkills(false, false, false, true);

		if(newChar.getSkillLevel(1001) > 0) // Soul Cry
			newChar.registerShortCut(new ShortCut(1, 0, ShortCut.TYPE_SKILL, 1001, 1, 1));
		if(newChar.getSkillLevel(1177) > 0) // Wind Strike
			newChar.registerShortCut(new ShortCut(1, 0, ShortCut.TYPE_SKILL, 1177, 1, 1));
		if(newChar.getSkillLevel(1216) > 0) // Self Heal
			newChar.registerShortCut(new ShortCut(9, 0, ShortCut.TYPE_SKILL, 1216, 1, 1));

		// add attack, take, sit shortcut
		newChar.registerShortCut(new ShortCut(0, 0, ShortCut.TYPE_ACTION, 2, -1, 1));
		newChar.registerShortCut(new ShortCut(3, 0, ShortCut.TYPE_ACTION, 5, -1, 1));
		newChar.registerShortCut(new ShortCut(4, 0, ShortCut.TYPE_ACTION, 4, -1, 1));
		newChar.registerShortCut(new ShortCut(10, 0, ShortCut.TYPE_ACTION, 0, -1, 1));
		newChar.registerShortCut(new ShortCut(11, 0, ShortCut.TYPE_ACTION, 65, -1, 1));
		newChar.registerShortCut(new ShortCut(11, 0, ShortCut.TYPE_ITEM, item.getObjectId(), -1, 1));//內掛道具放在快捷鍵

		newChar.checkLevelUpReward(true);

		newChar.setOnlineStatus(false);

		newChar.store(false);
		newChar.getInventory().store();
		newChar.deleteMe();
		return true;
	}
	//恢復資料特寫--
	private boolean CheckPlayerAndAccountExists(String account, String name)
	{		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		boolean check = false;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT account_name FROM _backup_characters where char_name= ?");
			statement.setString(1, name);
			rset = statement.executeQuery();
			if(rset.next())
			{
				String _account = rset.getString("account_name");
				if(!_account.equals(account))//如果名字跟帳號不一樣，不給創建此帳號
				{
					check = true;
				}
			}
			statement.close();
		}
		catch(Exception e)
		{}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return check;
	}
	//--恢復資料特寫
}