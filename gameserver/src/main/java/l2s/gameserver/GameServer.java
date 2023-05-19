package l2s.gameserver;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.awt.*;
import java.io.BufferedReader;//記錄備份
import java.io.BufferedWriter;//記錄備份
import java.io.File;
import java.io.FileInputStream;//記錄備份--
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;//--記錄備份
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;//記錄備份
import java.util.ArrayList;
import java.util.Date;//記錄備份
import java.util.List;
import java.util.Map.Entry;

import l2s.gameserver.utils.Transaction.TransactionHandler;
import net.sf.ehcache.CacheManager;

import l2s.commons.lang.StatsUtils;
import l2s.commons.listener.Listener;
import l2s.commons.listener.ListenerList;
import l2s.commons.net.HostInfo;
import l2s.commons.net.nio.impl.SelectorStats;
import l2s.commons.net.nio.impl.SelectorThread;
import l2s.commons.versioning.Version;
import l2s.gameserver.cache.CrestCache;
import l2s.gameserver.cache.ImagesCache;
import l2s.gameserver.config.FloodProtectorConfigs;
import l2s.gameserver.config.xml.ConfigParsers;
import l2s.gameserver.config.xml.holder.HostsConfigHolder;
import l2s.gameserver.config.xml.holder.VoteRewardConfigHolder;
import l2s.gameserver.dao.CharacterDAO;
import l2s.gameserver.dao.CustomHeroDAO;
import l2s.gameserver.dao.FencesDAO;
import l2s.gameserver.dao.HidenItemsDAO;
import l2s.gameserver.dao.ItemsDAO;
import l2s.gameserver.data.BoatHolder;
import l2s.gameserver.data.xml.Parsers;
import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.data.xml.holder.StaticObjectHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.database.UpdatesInstaller;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.handler.admincommands.AdminCommandHandler;
import l2s.gameserver.handler.bbs.BbsHandlerHolder;
import l2s.gameserver.handler.bypass.BypassHolder;
import l2s.gameserver.handler.dailymissions.DailyMissionHandlerHolder;
import l2s.gameserver.handler.items.ItemHandler;
import l2s.gameserver.handler.onshiftaction.OnShiftActionHolder;
import l2s.gameserver.handler.usercommands.UserCommandHandler;
import l2s.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.instancemanager.BotCheckManager;
import l2s.gameserver.instancemanager.BotReportManager;
import l2s.gameserver.instancemanager.CoupleManager;
import l2s.gameserver.instancemanager.PetitionManager;
import l2s.gameserver.instancemanager.PlayerMessageStack;
import l2s.gameserver.instancemanager.RaidBossSpawnManager;
import l2s.gameserver.instancemanager.SpawnManager;
import l2s.gameserver.instancemanager.TrainingCampManager;
import l2s.gameserver.instancemanager.clansearch.ClanSearchManager;
import l2s.gameserver.instancemanager.games.MiniGameScoreManager;
import l2s.gameserver.listener.GameListener;
import l2s.gameserver.listener.game.OnShutdownListener;
import l2s.gameserver.listener.game.OnStartListener;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.model.entity.MonsterRace;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.network.authcomm.AuthServerCommunication;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.GamePacketHandler;
import l2s.gameserver.network.telnet.TelnetServer;
import l2s.gameserver.scripts.Scripts;
import l2s.gameserver.security.HWIDBan;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.tables.EnchantHPBonusTable;
import l2s.gameserver.tables.FakePlayersTable;
import l2s.gameserver.tables.SubClassTable;
import l2s.gameserver.taskmanager.AutomaticTasks;
import l2s.gameserver.taskmanager.ItemsAutoDestroy;
import l2s.gameserver.utils.OnlineTxtGenerator;
import l2s.gameserver.utils.Strings;
import l2s.gameserver.utils.TradeHelper;
import l2s.gameserver.utils.velocity.VelocityUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameServer
{
	public static boolean DEVELOP = false;

	public static final String PROJECT_REVISION = "L2s [9999999]";
	public static final String UPDATE_NAME = "Classic: Saviors (Secret of Empire)";

	public static final int AUTH_SERVER_PROTOCOL = 4;

	private static final Logger _log = LoggerFactory.getLogger(GameServer.class);

	public class GameServerListenerList extends ListenerList<GameServer>
	{
		public void onStart()
		{
			for(Listener<GameServer> listener : getListeners())
				if(OnStartListener.class.isInstance(listener))
					((OnStartListener) listener).onStart();
		}

		public void onShutdown()
		{
			for(Listener<GameServer> listener : getListeners())
				if(OnShutdownListener.class.isInstance(listener))
					((OnShutdownListener) listener).onShutdown();
		}
	}

	public static GameServer _instance;

	private final List<SelectorThread<GameClient>> _selectorThreads = new ArrayList<SelectorThread<GameClient>>();
	private final SelectorStats _selectorStats = new SelectorStats();
	private Version version;
	private TelnetServer statusServer;
	private final GameServerListenerList _listeners;

	private long _serverStartTimeMillis;

	private final String _licenseHost;
	private final int _onlineLimit;

	public List<SelectorThread<GameClient>> getSelectorThreads()
	{
		return _selectorThreads;
	}

	public SelectorStats getSelectorStats()
	{
		return _selectorStats;
	}

	public long getServerStartTime()
	{
		return _serverStartTimeMillis;
	}

	public String getLicenseHost()
	{
		return _licenseHost;
	}

	public int getOnlineLimit()
	{
		return _onlineLimit;
	}

	@SuppressWarnings("unchecked")
	public GameServer() throws Exception
	{
		backup();// 備份資料 //20190516 新增LOG日期
		_instance = this;
		_serverStartTimeMillis = System.currentTimeMillis();
		_listeners = new GameServerListenerList();

		new File("./log/").mkdir();

		version = new Version(GameServer.class);

		_log.info("=================================================");
		_log.info("Project Revision: ........ " + PROJECT_REVISION);
		_log.info("Build Revision: .......... " + version.getRevisionNumber());
		_log.info("Update: .................. " + UPDATE_NAME);
		_log.info("Build date: .............. " + version.getBuildDate());
		_log.info("Compiler version: ........ " + version.getBuildJdk());
		_log.info("=================================================");

		// Initialize config
		ConfigParsers.parseAllOnLoad();
		Config.load();
		FloodProtectorConfigs.load();
		VelocityUtils.init();

		final HostInfo[] hosts = HostsConfigHolder.getInstance().getGameServerHosts();
		if(hosts.length == 0)
		{
			throw new Exception("Server hosts list is empty!");
		}

		final TIntSet ports = new TIntHashSet();
		for(HostInfo host : hosts)
		{
			if(host.getAddress() != null)
			{
				while(!checkFreePort(host.getAddress(), host.getPort()))
				{
					_log.warn("Port '" + host.getPort() + "' on host '" + host.getAddress() + "' is allready binded. Please free it and restart server.");
					try
					{
						Thread.sleep(1000L);
					}
					catch(InterruptedException e2)
					{
						//
					}
				}
				ports.add(host.getPort());
			}
		}

		final int[] portsArray = ports.toArray();

		if(portsArray.length == 0)
		{
			throw new Exception("Server ports list is empty!");
		}

		_licenseHost = Config.EXTERNAL_HOSTNAME;
		_onlineLimit = Config.MAXIMUM_ONLINE_USERS;

		if(_onlineLimit == 0)
		{
			throw new Exception("Server online limit is zero!");
		}

		// Initialize database
		Class.forName(Config.DATABASE_DRIVER).newInstance();
		DatabaseFactory.getInstance().getConnection().close();

		UpdatesInstaller.checkAndInstall();

		IdFactory _idFactory = IdFactory.getInstance();
		if(!_idFactory.isInitialized())
		{
			_log.error("Could not read object IDs from DB. Please Check Your Data.");
			throw new Exception("Could not initialize the ID factory");
		}

		CacheManager.getInstance();

		ThreadPoolManager.getInstance();

		BotCheckManager.loadBotQuestions();

		HidenItemsDAO.LoadAllHiddenItems();

		CustomHeroDAO.getInstance();

		HWIDBan.getInstance().load();

		ItemHandler.getInstance();

		DailyMissionHandlerHolder.getInstance();

		Scripts.getInstance();

		GeoEngine.load();

		Strings.reload();

		GameTimeController.getInstance();

		World.init();

		Parsers.parseAll();

		ItemsDAO.getInstance();

		ThreadPoolManager.getInstance().execute(() ->{
			CrestCache.getInstance();
			ImagesCache.getInstance();
		});

		CharacterDAO.getInstance();

		ClanTable.getInstance();

		SubClassTable.getInstance();

		EnchantHPBonusTable.getInstance();

		FencesDAO.getInstance().restore();

		StaticObjectHolder.getInstance().spawnAll();

		SpawnManager.getInstance().spawnAll();

		RaidBossSpawnManager.getInstance();

		ConfigParsers.parseAllOnInit();

		Scripts.getInstance().init();

		Announcements.getInstance();
        /* add ExpManagerTime edit by cy 02/19/2023 */
        AddExpManager timer = new AddExpManager();
        timer.begin();
        /* fin */

		PlayerMessageStack.getInstance();

		if(Config.AUTODESTROY_ITEM_AFTER > 0)
			ItemsAutoDestroy.getInstance();

		MonsterRace.getInstance();

		if(Config.ENABLE_OLYMPIAD)
		{
			Olympiad.load();
			Hero.getInstance();
		}

		PetitionManager.getInstance();

		if(Config.ALLOW_WEDDING)
			CoupleManager.getInstance();

		AdminCommandHandler.getInstance().log();
		UserCommandHandler.getInstance().log();
		VoicedCommandHandler.getInstance().log();
		BbsHandlerHolder.getInstance().log();
		BypassHolder.getInstance().log();
		OnShiftActionHolder.getInstance().log();

		AutomaticTasks.init();

		ClanTable.getInstance().checkClans();

		_log.info("=[Events]=========================================");
		ResidenceHolder.getInstance().callInit();
		EventHolder.getInstance().callInit();
		_log.info("==================================================");

		BoatHolder.getInstance().spawnAll();

		Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());

		_log.info("IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());

		MiniGameScoreManager.getInstance();
		ClanSearchManager.getInstance().load();

		BotReportManager.getInstance();

		TrainingCampManager.getInstance().init();

		VoteRewardConfigHolder.getInstance().callInit();

		Shutdown.getInstance().schedule(Config.RESTART_AT_TIME, Shutdown.RESTART);

		_log.info("GameServer Started");
		_log.info("Maximum Numbers of Connected Players: " + getOnlineLimit());
		
		registerSelectorThreads(ports);

		getListeners().onStart();

		TransactionHandler.load();

		if(Config.BUFF_STORE_ENABLED)
		{
			_log.info("Restoring offline buffers...");
			int count = TradeHelper.restoreOfflineBuffers();
			_log.info("Restored " + count + " offline buffers.");
		}

		if(Config.SERVICES_OFFLINE_TRADE_RESTORE_AFTER_RESTART)
		{
			_log.info("Restoring offline traders...");
			int count = TradeHelper.restoreOfflineTraders();
			_log.info("Restored " + count + " offline traders.");
		}

		FakePlayersTable.getInstance();

		if(Config.ONLINE_GENERATOR_ENABLED)
			ThreadPoolManager.getInstance().scheduleAtFixedRate(new OnlineTxtGenerator(), 5000L, Config.ONLINE_GENERATOR_DELAY * 60 * 1000L);

		AuthServerCommunication.getInstance().start();

		Toolkit.getDefaultToolkit().beep();

		if(Config.IS_TELNET_ENABLED)
			statusServer = new TelnetServer();
		else
			_log.info("Telnet server is currently disabled.");

		_log.info("=================================================");
		String memUsage = new StringBuilder().append(StatsUtils.getMemUsage()).toString();
		for(String line : memUsage.split("\n"))
			_log.info(line);
		_log.info("=================================================");
	}

	public GameServerListenerList getListeners()
	{
		return _listeners;
	}

	public static GameServer getInstance()
	{
		return _instance;
	}

	public <T extends GameListener> boolean addListener(T listener)
	{
		return _listeners.add(listener);
	}

	public <T extends GameListener> boolean removeListener(T listener)
	{
		return _listeners.remove(listener);
	}

	private static boolean checkFreePort(String hostname, int port)
	{
		ServerSocket ss = null;
		try
		{
			if(hostname.equalsIgnoreCase("*"))
				ss = new ServerSocket(port);
			else
				ss = new ServerSocket(port, 50, InetAddress.getByName(hostname));
		}
		catch(Exception e)
		{
			return false;
		}
		finally
		{
			try
			{
				ss.close();
			}
			catch(Exception e)
			{
				//
			}
		}
		return true;
	}

	private static boolean checkOpenPort(String ip, int port)
	{
		Socket socket = null;
		try
		{
			socket = new Socket();
			socket.connect(new InetSocketAddress(ip, port), 100);
		}
		catch(Exception e)
		{
			return false;
		}
		finally
		{
			try
			{
				socket.close();
			}
			catch(Exception e)
			{
				//
			}
		}
		return true;
	}

	private void registerSelectorThreads(TIntSet ports)
	{
		final GamePacketHandler gph = new GamePacketHandler();

		for(int port : ports.toArray())
			registerSelectorThread(gph, null, port);
	}

	private void registerSelectorThread(GamePacketHandler gph, String ip, int port)
	{
		try
		{
			SelectorThread<GameClient> selectorThread = new SelectorThread<GameClient>(Config.SELECTOR_CONFIG, _selectorStats, gph, gph, gph, null);
			selectorThread.openServerSocket(ip == null ? null : InetAddress.getByName(ip), port);
			selectorThread.start();
			_selectorThreads.add(selectorThread);
		}
		catch(Exception e)
		{
			//
		}
	}

	private static void backup()//記錄備份--
	{
		Date date = new Date();
		SimpleDateFormat bartDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
		
		File mediaDiPath = new File("C:/_L2_SecretofEmpire");
		if (!mediaDiPath.exists())
		{
			mediaDiPath.mkdir();
		}
		mediaDiPath = new File("C:/_L2_SecretofEmpire/log");
		if (!mediaDiPath.exists())
		{
			mediaDiPath.mkdir();
		}
		String str = "C:/_L2_SecretofEmpire/log/" + bartDateFormat.format(date);
		
		mediaDiPath = new File(str);
		
		if (!mediaDiPath.exists())
		{
			mediaDiPath.mkdir();
		}
		
		File oldFile = new File("log");
		String[] list = oldFile.list();
		
		try
		{
			for (String element : list)
			{
				if (element.contains(".log") || element.endsWith(".csv"))
				{
					File fromFile = new File("log/" + element);
					File toFile = new File(str + "/" + element);
					try
					{
						copyFileUsingFileStreams(fromFile, toFile);
						fromFile.delete();
					}
					catch (IOException e)
					{
					}
				}
				else
				{
					if (element.endsWith(".lck"))
					{
						File delFile = new File("log/" + element);
						delFile.delete();
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.info("刪除是否錯誤呢？");
		}
		// LOGGER.info("list = " + list.length);
		
	}
	
	// 讀取檔案
	public static String Reader(String pathName) throws java.io.IOException
	{
		FileReader reader = new FileReader(pathName);
		// 承載讀取內容的buff
		StringBuffer buff = new StringBuffer();
		// 一次讀取256個字元
		
		// FileReader fr = new FileReader("G:/Counter.txt");
		
		BufferedReader br = new BufferedReader(reader);
		String strNum = ""; // 讀第一行
		
		// StringBuffer sb = new StringBuffer();
		while ((strNum = br.readLine()) != null)
		{
			buff.append(strNum + "\n");
		}
		reader.close();
		return buff.toString();
	}
	public static void Writer(String pathName, String data) throws IOException
	{
		
		File file = new File(pathName);// 建立檔案，準備寫檔
		// try{
		BufferedWriter bufWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), "utf8"));
		bufWriter.write(data);
		bufWriter.close();
	}
	private static void copyFileUsingFileStreams(File source, File dest) throws IOException
	{
		InputStream input = null;
		OutputStream output = null;
		try
		{
			input = new FileInputStream(source);
			output = new FileOutputStream(dest);
			byte[] buf = new byte[1024];
			int bytesRead;
			while ((bytesRead = input.read(buf)) > 0)
			{
				output.write(buf, 0, bytesRead);
			}
			Writer(source.getAbsolutePath(), "");
		}
		finally
		{
			input.close();
			output.close();
		}
		// 20190516 新增LOG日期 以上
	}
	//--記錄備份
	public static void main(String[] args) throws Exception
	{
		for(String arg : args)
		{
			if(arg.equalsIgnoreCase("-dev"))
				DEVELOP = true;
		}
		new GameServer();
	}

	public Version getVersion()
	{
		return version;
	}

	public TelnetServer getStatusServer()
	{
		return statusServer;
	}
}