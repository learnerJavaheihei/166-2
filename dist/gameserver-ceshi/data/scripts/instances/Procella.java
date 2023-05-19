package instances;


import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.geometry.Location;
import l2s.gameserver.listener.actor.OnCurrentHpDamageListener;
import l2s.gameserver.listener.actor.OnDeathListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.templates.StatsSet;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.impl.HashIntSet;

/**
 * @author Bonux
 */
public class Procella extends Reflection
{
	private final int RAID_NPC_ID = 29107;	// Баюм
	private int _StartDelay;
	private DeathListener _deathListener = new DeathListener();
	private CurrentHpListener _currentHpListener = new CurrentHpListener();
	
	private static final SkillEntry SKILL_1 = SkillHolder.getInstance().getSkillEntry(50049, 1);//這技能放最強的
	private static final SkillEntry SKILL_2 = SkillHolder.getInstance().getSkillEntry(50050, 1);//這技能放第二強的
	private static final SkillEntry SKILL_3 = SkillHolder.getInstance().getSkillEntry(50051, 1);//這技能放第三強的
	//private final AtomicBoolean ZakenRaidSpawned = new AtomicBoolean(false);
	private final IntSet rewardedPlayers = new HashIntSet();
	private NpcInstance RAID_NPC ;
	//55256, 219096, -3232
	private static Location RAIDSpawn = new Location(212872, 181048, -15488);
	
	//clearReflection 
	@Override
	protected void onCreate()
	{
		StatsSet params = getInstancedZone().getAddParams();
		_StartDelay = params.getInteger("start_delay");
		super.onCreate();
		ThreadPoolManager.getInstance().schedule(new Spawn(), _StartDelay * 1000);
		//ThreadPoolManager.getInstance().schedule(() -> addSpawnWithoutRespawn(RAID_NPC_ID, RAIDSpawn, 0), _StartDelay);
	}
	
	public int getStartDelay()
	{
		return _StartDelay;
	}
	
	private class DeathListener implements OnDeathListener
	{
		@Override
		public void onDeath(Creature self, Creature killer)
		{
			if(self.isNpc())
			{			
				if(self.getNpcId() == RAID_NPC_ID)
				{
					startCollapseTimer(5, false);//打完離開時間
					for(Player p : getPlayers())
						p.sendPacket(new SystemMessage(SystemMessage.THIS_DUNGEON_WILL_EXPIRE_IN_S1_MINUTES).addNumber(5));
					setReenterTime(System.currentTimeMillis(),false);
				}
			}
		}
	}
	
	public class CurrentHpListener implements OnCurrentHpDamageListener
	{
		@Override
		public void onCurrentHpDamage(Creature actor, double damage, Creature attacker, Skill skill)
		{
			if(actor.isDead() )
				return;
			
			//double newHp = actor.getCurrentHp() - damage;
			//double maxHp = actor.getMaxHp();
			if(actor.isCastingNow())
			{
				//Announcements.announceToAll("isCastingNow");
				return;
			}
			
			if (Rnd.get(20) < 1)//觸發技能機率為 20下觸發一次
			{
				int skillRandom = Rnd.get(10);//第二個判斷區，隨機數15*15 = 225個  產生的數字為 0~224
				if (skillRandom == 0 )//當隨機數為 0 (0 )  觸發這一個技能
				{
					actor.doCast(SKILL_1, attacker, true);//低機率觸發最強的技能
				}
				else if (skillRandom < 4)//當隨機數為小於 4 (1 2 3)  觸發這一個技能
				{
					actor.doCast(SKILL_2, attacker, true);//中機率觸發第二強的技能
				}
				else if (skillRandom < 10)//當隨機數為小於 10 (4 5 6 7 8 9)  觸發這一個技能
				{
					actor.doCast(SKILL_3, attacker, true);//高機率觸發第三強的技能
				}				
			}			
			//if(newHp < 0.75 * maxHp)
			//{
				//ThreadPoolManager.getInstance().schedule(new SecondMorph(1), 1100);
			//}
			//if(newHp < 0.1 * maxHp)
			//{
				//ThreadPoolManager.getInstance().schedule(new ThirdMorph(1), 2000);
			//}
		}
	}

	private class Spawn implements Runnable
	{
		public Spawn()
		{
		}

		@Override
		public void run()
		{
			RAID_NPC = addSpawnWithoutRespawn(RAID_NPC_ID, RAIDSpawn, 0);
			RAID_NPC.addListener(_deathListener);
			RAID_NPC.addListener(_currentHpListener);
		}
	}
//	public boolean isRewardReceived(Player player)
//	{
//		return rewardedPlayers.contains(player.getObjectId());
//	}
//
//	public void setRewardReceived(Player player)
//	{
//		rewardedPlayers.add(player.getObjectId());
//	}
}
