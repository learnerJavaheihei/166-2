package l2s.gameserver.core;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.botscript.BotConfigDAO;
import l2s.gameserver.botscript.BotConfigImp;
import l2s.gameserver.botscript.BotRuntimeChecker;
import l2s.gameserver.botscript.BotScriptsLoader;
import l2s.gameserver.model.Player;
import l2s.gameserver.skills.AbnormalEffect;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.utils.Functions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
public class BotEngine
{
    private static final Logger LOG = LoggerFactory.getLogger(BotEngine.class);
    private static final BotEngine INSTANCE = new BotEngine();
    private IBotConfigDAO dao;
    private IBotRuntimeChecker runTimeChecker;
    private  Map<Integer, ScheduledFuture<?>> tasks;
    private Lock switchLock;
    private Map<Integer, BotConfig> configs;
	private SkillEntry _effectSkill;
    // 连续未攻击时长
    private  Map<Integer, ScheduledFuture<?>> increaseAttackRadiusTasks;
    private Timer timer;

    /* member class not found */
    class Task {}

    public Map<Integer, BotConfig> getConfigs()
    {
        return configs;
    }

    protected BotEngine()
    {
        tasks = new HashMap<Integer, ScheduledFuture<?>>();
        increaseAttackRadiusTasks = new HashMap<Integer, ScheduledFuture<?>>();
        switchLock = new ReentrantLock();
        configs = new HashMap<Integer, BotConfig>();
        //init();

    }

    public synchronized void init()
    {
        BotScriptsLoader.load();
        dao = new BotConfigDAO();
        runTimeChecker = new BotRuntimeChecker();
        LOG.info("\u6302\u673A\u7CFB\u7EDF\u52A0\u8F7D\u6210\u529F!");
		/*\u6302\u673A\u7CFB\u7EDF\u52A0\u8F7D\u6210\u529F! 挂机系统加载成功!*/
    }

    public IBotConfigDAO getDao()
    {
        return dao;
    }

    public IBotRuntimeChecker getRunTimeChecker()
    {
        return runTimeChecker;
    }

    public BotConfig getBotConfig(Player player)
    {
        BotConfig config = configs.get(Integer.valueOf(player.getObjectId()));
        if(config == null)
        {
            config = new BotConfigImp();
            configs.put(Integer.valueOf(player.getObjectId()), config);
        }
        return configs.get(Integer.valueOf(player.getObjectId()));
    }
    public void startBotTask(Player player)
    {
        timer = new Timer();
		/*pvp活动禁用内挂*/
    	if (player.isInPvPEvent())
    	{
    		player.sendMessage("活动中无法开启内挂..");
    		return;
    	}
		/*pvp活动禁用内挂*/
		switchLock.lock();
		try
		{
			ScheduledFuture<?> botThinkTask = tasks.get(player.getObjectId());
			if(botThinkTask == null)
			{
				botThinkTask = ThreadPoolManager.getInstance().schedule(new BotThinkTask(player), 0L);
				BotConfig botConfig = BotEngine.getInstance().getBotConfig(player);
				player.sendMessage("啟動輔助（52級以前免費使用），收益100%不減少...");
				player._isInPlugIn = true;/*啟動內掛減少收益*/
				botConfig.setStartX(player.getX());
				botConfig.setStartY(player.getY());
				botConfig.setStartZ(player.getZ());
				if(botConfig.getDeathTime() > 0)
				{
					botConfig.setDeathTime(0);
				}
				botConfig.setAbort(false, "");
				tasks.put(player.getObjectId(), botThinkTask);
				player.broadcastCharInfo();
			}
            // 是否开启自动调整找怪范围
            BotConfig botConfig = BotEngine.getInstance().getBotConfig(player);
//            int Boot_setting_range = botConfig.getFindMobMaxDistance();
//            ScheduledFuture<?> increaseAttackRadiusTask = increaseAttackRadiusTasks.get(player.getObjectId());
            Adjust(player,botConfig);

            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    botConfig.releaseMemory(player);
                }
            },60*1000L,60*1000L);

        }
		finally
		{
			switchLock.unlock();
		}
		player.startAbnormalEffect(AbnormalEffect.UNK_222);//給與特殊效果標識
    }

    public void Adjust(Player player, BotConfig botConfig) {
        int Boot_setting_range = botConfig.getFindMobMaxDistance();
        ScheduledFuture<?> increaseAttackRadiusTask = increaseAttackRadiusTasks.get(player.getObjectId());
        if (botConfig.is_autoAdjustRange()) {
            autoAdjustRange(player, botConfig, increaseAttackRadiusTask, Boot_setting_range,increaseAttackRadiusTasks);
        }else{
            if(increaseAttackRadiusTask != null)
            {
                increaseAttackRadiusTask.cancel(false);
            }
            increaseAttackRadiusTasks.remove(player.getObjectId());
        }
    }

    private void autoAdjustRange(Player player, BotConfig botConfig, ScheduledFuture<?> increaseAttackRadiusTask, int boot_setting_range, Map<Integer, ScheduledFuture<?>> increaseAttackRadiusTasks) {

        if (increaseAttackRadiusTask==null) {
            increaseAttackRadiusTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {

                    if (player.isDead() || player.isAttackingNow()) {
                        botConfig.setFindMobMaxDistance(boot_setting_range);
                    }
                    // 如果上次攻击时间 距离现在超过 5分钟 将最大距离增加 1000 但不能超过最大 4500
                    if ((System.currentTimeMillis() - player.getLastAttackPacket()) >= 5*60 * 1000) {
                        botConfig.setFindMobMaxDistance(Math.min(boot_setting_range + 1000, 4500));
                    }
                }
            }, 5*60 * 1000, 1000, TimeUnit.MILLISECONDS);
            increaseAttackRadiusTasks.put(player.getObjectId(),increaseAttackRadiusTask);
        }
    }

    public void stopTask(Player player)
    {
		switchLock.lock();
		try
		{
            if (timer !=null) {
                timer.cancel();
            }
			int objectId = player.getObjectId();
			Future<?> task = tasks.get(player.getObjectId());
            Future<?> increaseAttackRadiusTask = increaseAttackRadiusTasks.get(player.getObjectId());
			player._isInPlugIn = false;/*關閉內掛減少收益*/

			if(task != null)
			{
				task.cancel(false);
			}
            if(increaseAttackRadiusTask != null)
            {
                increaseAttackRadiusTask.cancel(false);
            }
			tasks.remove(objectId);
            increaseAttackRadiusTasks.remove(objectId);
			player.sendMessage("中断辅助 - " + BotEngine.getInstance().getBotConfig(player).getAbortReason());
			player.broadcastCharInfo();
			Functions.show("<center>辅助已中断<br1>....</center>", player);
		}
		finally
		{
			switchLock.unlock();
		}
		player.stopAbnormalEffect(AbnormalEffect.UNK_222);//給與特殊效果標識
    }

    public static BotEngine getInstance()
    {
        return INSTANCE;
    }
}