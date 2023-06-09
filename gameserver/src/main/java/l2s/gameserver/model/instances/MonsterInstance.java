package l2s.gameserver.model.instances;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import l2s.commons.collections.MultiValueSet;
import l2s.commons.math.SafeMath;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.model.AggroList.HateInfo;
import l2s.gameserver.model.CommandChannel;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.PlayerGroup;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.base.TeamType;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestEventType;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.model.reward.RewardItem;
import l2s.gameserver.model.reward.RewardList;
import l2s.gameserver.model.reward.RewardType;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExMagicAttackInfo;
import l2s.gameserver.network.l2.s2c.SocialActionPacket;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.templates.item.data.RewardItemData;
import l2s.gameserver.templates.npc.Faction;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ItemFunctions;

/**
 * This class manages all Monsters.
**/
public class MonsterInstance extends NpcInstance
{
	protected class GroupInfo
	{
		public HashSet<Player> players;
		public double damage;

		public GroupInfo()
		{
			this.players = new HashSet<Player>();
			this.damage = 0.;
		}
	}

	private int overhitAttackerId;
	/** Stores the extra (over-hit) damage done to the L2NpcInstance when the attacker uses an over-hit enabled skill */
	private double _overhitDamage;

	/** True if a Dwarf has used Spoil on this L2NpcInstance */
	private boolean _isSpoiled;
	private int spoilerId;
	/** Table containing all Items that a Dwarf can Sweep on this L2NpcInstance */
	private List<RewardItem> _sweepItems;
	private boolean _sweeped;
	private final Lock sweepLock = new ReentrantLock();

	private int _isChampion;

	private final boolean _canMove;

	public MonsterInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);

		_canMove = getParameter("canMove", true);
	}

	@Override
	public boolean isMovementDisabled()
	{
		return !_canMove || super.isMovementDisabled();
	}

	@Override
	public boolean isLethalImmune()
	{
		return _isChampion > 0 || super.isLethalImmune();
	}

	@Override
	public boolean isFearImmune()
	{
		return _isChampion > 0 || super.isFearImmune();
	}

	@Override
	public boolean isParalyzeImmune()
	{
		return _isChampion > 0 || super.isParalyzeImmune();
	}

	@Override
	public boolean isAutoAttackable(Creature attacker)
	{
		return attacker.getPlayer() != null || attacker.isDefender();
	}

	public int getChampion()
	{
		return _isChampion;
	}

	public void setChampion()
	{
		if(getReflection().canChampions() && canChampion() && Math.abs(getLevel()) <=50)
		{
			double random = Rnd.nextDouble();
			if(Config.ALT_CHAMPION_CHANCE2 / 100. >= random)
				setChampion(1);
			else if((Config.ALT_CHAMPION_CHANCE1 + Config.ALT_CHAMPION_CHANCE2) / 100. >= random)
				setChampion(1);
			else
				setChampion(0);
		}
		else if(getReflection().canChampions() && canChampion() && Math.abs(getLevel()) >=51)
		{
			double random = Rnd.nextDouble();
			if(Config.ALT_CHAMPION_CHANCE2 / 100. >= random)
				setChampion(2);
			else if((Config.ALT_CHAMPION_CHANCE1 + Config.ALT_CHAMPION_CHANCE2) / 100. >= random)
				setChampion(1);
			else
				setChampion(0);
		}
		else
			setChampion(0);
	}

	public void setChampion(int level)
	{
		if(level == 0)
		{
			removeSkillById(4407);
			_isChampion = 0;
		}
		else
		{
			addSkill(SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4407, level));
			_isChampion = level;
			if(getLevel() >=41 && getLevel() <=52 && level == 1)
			{
			addSkill(SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4407, 2));
			}
			if(getLevel() >=53 && getLevel() <=64 && level == 1)
			{
			addSkill(SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4407, 3));
			}
			if(getLevel() >=65 && getLevel() <=75 && level == 1)
			{
			addSkill(SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4407, 4));
			}
			if(getLevel() >=76 && level == 1)
			{
			addSkill(SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4407, 5));
			}
			if(getLevel() >=51 && getLevel() <=61 && level == 2)
			{
			addSkill(SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4407, 6));
			}
			if(getLevel() >=62 && getLevel() <=75 && level == 2)
			{
			addSkill(SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4407, 7));
			}
			if(getLevel() >=76 && level == 2)
			{
			addSkill(SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4407, 8));
			}
		}
	}

	public boolean canChampion()
	{
		return !isMinion() && getTemplate().rewardExp > 0 && getTemplate().level >= Config.ALT_CHAMPION_MIN_LEVEL && getTemplate().level <= Config.ALT_CHAMPION_TOP_LEVEL;
	}

	@Override
	public TeamType getTeam()
	{
		return getChampion() == 2 ? TeamType.RED : getChampion() == 1 ? TeamType.BLUE : TeamType.NONE;
	}

	@Override
	protected void onDespawn()
	{
		setOverhitDamage(0);
		setOverhitAttacker(null);
		clearSweep();

		super.onDespawn();
	}

	@Override
	public void onSpawnMinion(NpcInstance minion)
	{
		if(minion.isMonster())
		{
			if(getChampion() == 2)
				((MonsterInstance) minion).setChampion(1);
			else
				((MonsterInstance) minion).setChampion(0);
		}
		super.onSpawnMinion(minion);
	}

	@Override
	protected void onDeath(Creature killer)
	{
		calculateRewards(killer);

		super.onDeath(killer);
	}

	@Override
	protected void onReduceCurrentHp(double damage, Creature attacker, Skill skill, boolean awake, boolean standUp, boolean directHp, boolean isDot)
	{
		if(skill != null && skill.isOverhit())
		{
			// Calculate the over-hit damage
			// Ex: mob had 10 HP left, over-hit skill did 50 damage total, over-hit damage is 40
			double overhitDmg = (getCurrentHp() - damage) * -1;
			if(overhitDmg <= 0)
			{
				setOverhitDamage(0);
				setOverhitAttacker(null);
			}
			else
			{
				setOverhitDamage(overhitDmg);
				setOverhitAttacker(attacker);
			}
		}

		super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, isDot);
	}

	public void calculateRewards(Creature lastAttacker)
	{
		Creature topDamager = getAggroList().getTopDamager(lastAttacker);
		if(lastAttacker == null || !lastAttacker.isPlayable())
			lastAttacker = topDamager;

		if(lastAttacker == null || !lastAttacker.isPlayable())
			return;

		Player killer = lastAttacker.getPlayer();
		if(killer == null)
			return;

		Map<Playable, HateInfo> aggroMap = getAggroList().getPlayableMap();

		Set<Quest> quests = getTemplate().getEventQuests(QuestEventType.MOB_KILLED_WITH_QUEST);
		if(quests != null && !quests.isEmpty())
		{
			List<Player> players = null; // массив с игроками, которые могут быть заинтересованы в квестах
			if(isRaid() && Config.ALT_NO_LASTHIT) // Для альта на ластхит берем всех игроков вокруг
			{
				players = new ArrayList<Player>();
				for(Playable pl : aggroMap.keySet())
					if(!pl.isDead() && (isInRangeZ(pl, Config.ALT_PARTY_DISTRIBUTION_RANGE) || killer.isInRangeZ(pl, Config.ALT_PARTY_DISTRIBUTION_RANGE)))
						if(!players.contains(pl.getPlayer())) // не добавляем дважды если есть пет
							players.add(pl.getPlayer());
			}
			else if(killer.getParty() != null) // если пати то собираем всех кто подходит
			{
				players = new ArrayList<Player>(killer.getParty().getMemberCount());
				for(Player pl : killer.getParty().getPartyMembers())
					if(!pl.isDead() && (isInRangeZ(pl, Config.ALT_PARTY_DISTRIBUTION_RANGE) || killer.isInRangeZ(pl, Config.ALT_PARTY_DISTRIBUTION_RANGE)))
						players.add(pl);
			}

			for(Quest quest : quests)
			{
				Player toReward = killer;
				if(quest.getPartyType() != Quest.PARTY_NONE && players != null)
					if(isRaid() || quest.getPartyType() == Quest.PARTY_ALL) // если цель рейд или квест для всей пати награждаем всех участников
					{
						for(Player pl : players)
						{
							QuestState qs = pl.getQuestState(quest);
							if(qs != null && !qs.isCompleted())
								quest.notifyKill(this, qs);
						}
						toReward = null;
					}
					else
					{ // иначе выбираем одного
						List<Player> interested = new ArrayList<Player>(players.size());
						for(Player pl : players)
						{
							QuestState qs = pl.getQuestState(quest);
							if(qs != null && !qs.isCompleted()) // из тех, у кого взят квест
								interested.add(pl);
						}

						if(interested.isEmpty())
							continue;

						toReward = interested.get(Rnd.get(interested.size()));
						if(toReward == null)
							toReward = killer;
					}

				if(toReward != null)
				{
					QuestState qs = toReward.getQuestState(quest);
					if(qs != null && !qs.isCompleted())
						quest.notifyKill(this, qs);
				}
			}
		}
		if(topDamager != null && topDamager.isPlayable())
		{
			for(RewardList rewardList : getRewardLists())
				rollRewards(rewardList, lastAttacker, topDamager);

			Player player = topDamager.getPlayer();
			if(player != null && Math.abs(getLevel() - player.getLevel()) < 9)
			{
				for(RewardItemData reward : player.getPremiumAccount().getRewards())
				{
					if(Rnd.chance(reward.getChance()))
						ItemFunctions.addItem(player, reward.getId(), Rnd.get(reward.getMinCount(), reward.getMaxCount()));
				}

				for(RewardItemData reward : player.getVIP().getTemplate().getRewards())
				{
					if(Rnd.chance(reward.getChance()))
						ItemFunctions.addItem(player, reward.getId(), Rnd.get(reward.getMinCount(), reward.getMaxCount()));
				}

				if(getChampion() > 0 && Config.SPECIAL_ITEM_ID > 0 && Config.SPECIAL_ITEM_COUNT > 0 && Math.abs(getLevel() - player.getLevel()) < 9 && Rnd.chance(Config.SPECIAL_ITEM_DROP_CHANCE))
					ItemFunctions.addItem(player, Config.SPECIAL_ITEM_ID, Config.SPECIAL_ITEM_COUNT);
			}
			//20200102
			int lv = getLevel();
			int playerLv = player.getLevel();				
			if(lv > 20 && (!player.getVarBoolean("NoExp")) && _isChampion > 0)//怪要大於20級才掉
			{
				boolean go = false;
				if((playerLv >= lv) && ((playerLv - lv) <= 8))//自己比怪等級大八級內會掉落
				{
					go = true;
				}
				else if((playerLv <= lv) && ((lv - playerLv) <= 20))//自己的等級比怪小20級內會掉
				{
					go = true;
				}
				if(go)
				{
					double chance = 0;
					int counts = 0;
					int itemsl = 0;
					if(lv >= 20 && lv <= 29)
					{
						chance = Config.GLOBAL_DROP_ITEM_CHANCE20_29;
						counts = Config.GLOBAL_DROP_ITEM_COUNT20_29;
						itemsl = Config.GLOBAL_DROP_ITEM20_29;
					}
					else if(lv >= 30 && lv <= 39)
					{
						chance = Config.GLOBAL_DROP_ITEM_CHANCE30_39;
						counts = Config.GLOBAL_DROP_ITEM_COUNT30_39;
						itemsl = Config.GLOBAL_DROP_ITEM30_39;
					}
					else if(lv >= 40 && lv <= 49)
					{
						chance = Config.GLOBAL_DROP_ITEM_CHANCE40_49;
						counts = Config.GLOBAL_DROP_ITEM_COUNT40_49;
						itemsl = Config.GLOBAL_DROP_ITEM40_49;
					}
					else if(lv >= 50 && lv <= 59)
					{
						chance = Config.GLOBAL_DROP_ITEM_CHANCE50_59;
						counts = Config.GLOBAL_DROP_ITEM_COUNT50_59;
						itemsl = Config.GLOBAL_DROP_ITEM50_59;
					}
					else if(lv >= 60 && lv <= 69)
					{
						chance = Config.GLOBAL_DROP_ITEM_CHANCE60_69;
						counts = Config.GLOBAL_DROP_ITEM_COUNT60_69;
						itemsl = Config.GLOBAL_DROP_ITEM60_69;
					}
					else if(lv >= 70 && lv <= 79)
					{
						chance = Config.GLOBAL_DROP_ITEM_CHANCE70_79;
						counts = Config.GLOBAL_DROP_ITEM_COUNT70_79;
						itemsl = Config.GLOBAL_DROP_ITEM70_79;
					}
					else if(lv >= 80 && lv <= 89)
					{
						chance = Config.GLOBAL_DROP_ITEM_CHANCE80_89;
						counts = Config.GLOBAL_DROP_ITEM_COUNT80_89;
						itemsl = Config.GLOBAL_DROP_ITEM80_89;
					}
					chance = (player.hasPremiumAccount() ? 1.2 : 1) * chance;//會員掉率乘1.5倍
					double ch = Rnd.nextDouble();
					if(ch < chance)
					{
						ItemFunctions.addItem(player, itemsl, Rnd.get(1, counts));
					}
				}
			}
		}

		Map<PlayerGroup, GroupInfo> groupsInfo = new HashMap<PlayerGroup, GroupInfo>();
		double totalDamage = 0;

		// Разбиваем игроков по группам. По возможности используем наибольшую из доступных групп: Command Channel → Party → StandAlone (сам плюс пет :)
		for(HateInfo ai : aggroMap.values())
		{
			Player player = ai.attacker.getPlayer();
			if(player == null)
				continue;

			// Только при убийстве РБ опыт делиться на все CC.
			PlayerGroup group = isRaid() ? player.getPlayerGroup() : (player.getParty() != null ? player.getParty() : player);
			GroupInfo info = groupsInfo.get(group);
			boolean addDamage = true;
			if(info == null)
			{
				info = new GroupInfo();
				groupsInfo.put(group, info);
				addDamage = false;
			}

			for(Player p : group)
			{
				if(!p.isDead() && p.isInRangeZ(this, Config.ALT_PARTY_DISTRIBUTION_RANGE))
				{
					info.players.add(p);
					addDamage = true;
				}
			}

			if(addDamage)
			{
				info.damage += ai.damage;
				totalDamage += Math.max(0, ai.damage);
			}
		}

		totalDamage = Math.max(totalDamage, getMaxHp());

		for(Map.Entry<PlayerGroup, GroupInfo> groupInfo : groupsInfo.entrySet())
		{
			PlayerGroup group = groupInfo.getKey();
			GroupInfo info = groupInfo.getValue();

			double damage = info.damage;
			if(damage <= 1) // TODO: Чего 1 а не 0?
				continue;

			if(group instanceof CommandChannel)
			{
				// Опыт CC делиться поровну на все группы не зависимо сколько членов в группе.
				CommandChannel commandChannel = (CommandChannel) group;

				Set<Party> rewardedParties = new HashSet<Party>();
				for(Player p : info.players)
				{
					Party party = p.getParty();
					if(party != null && commandChannel.getParties().contains(party))
						rewardedParties.add(party);
				}

				for(Party party : rewardedParties)
				{
					HashSet<Player> rewardedMembers = new HashSet<Player>();
					int partylevel = 1;
					for(Player partyMember : party.getPartyMembers())
					{
						if(info.players.remove(partyMember))
						{
							if(partyMember.getLevel() > partylevel)
								partylevel = partyMember.getLevel();

							rewardedMembers.add(partyMember);
						}
					}
					double[] xpsp = calculateExpAndSp(partylevel, damage / rewardedParties.size(), totalDamage);
					xpsp[0] = applyOverhit(killer, xpsp[0]);
					party.distributeXpAndSp(xpsp[0], xpsp[1], rewardedMembers, lastAttacker, this);
				}
			}
			else if(group instanceof Party)
			{
				Party party = (Party) group;
				int partylevel = 1;
				for(Player p : info.players)
				{
					if(p.getLevel() > partylevel)
						partylevel = p.getLevel();
				}
				double[] xpsp = calculateExpAndSp(partylevel, damage, totalDamage);
				xpsp[0] = applyOverhit(killer, xpsp[0]);
				party.distributeXpAndSp(xpsp[0], xpsp[1], info.players, lastAttacker, this);
			}
			else if(group instanceof Player)
			{
				Player player = (Player) group;
				double[] xpsp = calculateExpAndSp(player.getLevel(), damage, totalDamage);
				xpsp[0] = applyOverhit(killer, xpsp[0]);
				player.addExpAndCheckBonus(this, (long) xpsp[0], (long) xpsp[1]);
			}
		}
	}

	@Override
	public void onRandomAnimation()
	{
		if(System.currentTimeMillis() - _lastSocialAction > 10000L)
		{
			broadcastPacket(new SocialActionPacket(getObjectId(), 1));
			_lastSocialAction = System.currentTimeMillis();
		}
	}

	@Override
	public void startRandomAnimation()
	{
		//У мобов анимация обрабатывается в AI
	}

	@Override
	public int getKarma()
	{
		return 0;
	}

	/**
	 * Return True if this L2NpcInstance has drops that can be sweeped.<BR><BR>
	 */
	public boolean isSpoiled()
	{
		return _isSpoiled;
	}

	public boolean isSpoiled(Player player)
	{
		if(!isSpoiled()) // если не заспойлен то false
			return false;

		//заспойлен этим игроком, и смерть наступила не более 20 секунд назад
		if(player.getObjectId() == spoilerId && (System.currentTimeMillis() - getDeathTime()) < 20000L)
			return true;

		if(player.isInParty())
			for(Player pm : player.getParty().getPartyMembers())
				if(pm.getObjectId() == spoilerId && getDistance(pm) < Config.ALT_PARTY_DISTRIBUTION_RANGE)
					return true;

		return false;
	}

	/**
	 * Set the spoil state of this L2NpcInstance.<BR><BR>
	 * @param player
	 */
	public boolean setSpoiled(Player player)
	{
		sweepLock.lock();
		try
		{
			if(isSpoiled())
				return false;
			_isSpoiled = true;
			spoilerId = player.getObjectId();
		}
		finally
		{
			sweepLock.unlock();
		}
		return true;
	}

	/**
	 * Return True if a Dwarf use Sweep on the L2NpcInstance and if item can be spoiled.<BR><BR>
	 */
	public boolean isSweepActive()
	{
		sweepLock.lock();
		try
		{
			return _sweepItems != null && _sweepItems.size() > 0;
		}
		finally
		{
			sweepLock.unlock();
		}
	}
	
	int dengji = Config.CHAO_DROP_ITEM_COUNT40;

	public boolean takeSweep(final Player player)
	{
		sweepLock.lock();
		try
		{
			_sweeped = true;

			if(_sweepItems == null || _sweepItems.isEmpty())
			{
				clearSweep();
				return false;
			}

			for(RewardItem item : _sweepItems)
			{
				if(getLevel() > dengji)
				{
					item.count *= 0;
					break;	
				}
				final ItemInstance sweep = ItemFunctions.createItem(item.itemId);
				sweep.setCount(item.count);

				if(player.isInParty() && player.getParty().isDistributeSpoilLoot())
				{
					player.getParty().distributeItem(player, sweep, null);
					continue;
				}

				if(!player.getInventory().validateCapacity(sweep) || !player.getInventory().validateWeight(sweep))
				{
					sweep.dropToTheGround(player, this);
					continue;
				}

				player.getInventory().addItem(sweep);

				SystemMessagePacket smsg;
				if(item.count == 1)
				{
					smsg = new SystemMessagePacket(SystemMsg.YOU_HAVE_OBTAINED_S1);
					smsg.addItemName(item.itemId);
					player.sendPacket(smsg);
				}
				else
				{
					smsg = new SystemMessagePacket(SystemMsg.YOU_HAVE_OBTAINED_S2_S1);
					smsg.addItemName(item.itemId);
					smsg.addLong(item.count);
					player.sendPacket(smsg);
				}

				if(player.isInParty())
				{
					if(item.count == 1)
					{
						smsg = new SystemMessagePacket(SystemMsg.C1_HAS_OBTAINED_S2_BY_USING_SWEEPER);
						smsg.addName(player);
						smsg.addItemName(item.itemId);
						player.getParty().getPartyLeader().sendPacket(smsg);
					}
					else
					{
						smsg = new SystemMessagePacket(SystemMsg.C1_HAS_OBTAINED_S3_S2_BY_USING_SWEEPER);
						smsg.addName(player);
						smsg.addItemName(item.itemId);
						smsg.addLong(item.count);
						player.getParty().getPartyLeader().sendPacket(smsg);
					}
				}
			}
			clearSweep();
			return true;
		}
		finally
		{
			sweepLock.unlock();
		}
	}

	public boolean isSweeped()
	{
		return _sweeped;
	}

	public void clearSweep()
	{
		sweepLock.lock();
		try
		{
			_isSpoiled = false;
			spoilerId = 0;
			_sweepItems = null;
		}
		finally
		{
			sweepLock.unlock();
		}
	}

	public void rollRewards(RewardList list, final Creature lastAttacker, Creature topDamager)
	{
		RewardType type = list.getType();
		if(type == RewardType.SWEEP && !isSpoiled())
			return;

		final Creature activeChar = type == RewardType.SWEEP ? lastAttacker : topDamager;
		final Player activePlayer = activeChar.getPlayer();

		if(activePlayer == null)
			return;

		final double penaltyMod = Experience.penaltyModifier(calculateLevelDiffForDrop(topDamager.getLevel()), 9);

		List<RewardItem> rewardItems = list.roll(activePlayer, penaltyMod, this);
		switch(type)
		{
			case SWEEP:
				_sweepItems = rewardItems;
				break;
			default:
				for(RewardItem drop : rewardItems)
				{
					if(!Config.DROP_ONLY_THIS.isEmpty() && !Config.DROP_ONLY_THIS.contains(drop.itemId))
					{
						if(!(Config.INCLUDE_RAID_DROP && isRaid()))
							return;
					}
					if(getLevel() > dengji)
					{
						drop.count *= 0;
						break;
					}
					dropItem(activePlayer, drop.itemId, drop.count);
				}				
				break;
		}
	}

	private double[] calculateExpAndSp(int level, double damage, double totalDamage)
	{
		/* TODO:
			if ( getInstanceUIData().getIsClassicServer() || getInstanceUIData().getIsArenaServer() )
			{
				if (myLevel < 78 )
				{
					if (TargetLevelDiff <= -11)
					{
						OutColor.R=255;
						OutColor.G=0;
						OutColor.B=0;
					}
					else if (TargetLevelDiff > -11 &&TargetLevelDiff <= -6)
					{
						OutColor.R=255;
						OutColor.G=145;
						OutColor.B=145;
					}
					else if (TargetLevelDiff > -6 &&TargetLevelDiff <= -3)
					{
						OutColor.R=250;
						OutColor.G=254;
						OutColor.B=145;
					}
					else if (TargetLevelDiff > -3 &&TargetLevelDiff <= 2)
					{
						OutColor.R=255;
						OutColor.G=255;
						OutColor.B=255;
					}
					else if (TargetLevelDiff > 2 &&TargetLevelDiff <= 5)
					{
						OutColor.R=162;
						OutColor.G=255;
						OutColor.B=171;
					}
					else if (TargetLevelDiff > 5 &&TargetLevelDiff <= 10)
					{
						OutColor.R=162;
						OutColor.G=168;
						OutColor.B=252;
					}
					else if (TargetLevelDiff > 10)
					{
						OutColor.R=0;
						OutColor.G=0;
						OutColor.B=255;
					}
				}
				// АЇАъ·№є§ 78 ~ 84 АП °жїм ·№є§ ВчАМ »ц»у ЗҐЅГё¦ БЩї© єёї©БШґЩ. 
				else if( myLevel >= 78 && myLevel < 85 )
				{		
					if (TargetLevelDiff <= -11)
					{
						OutColor.R=255;
						OutColor.G=0;
						OutColor.B=0;
					}
					else if (TargetLevelDiff > -11 &&TargetLevelDiff <= -4)
					{
						OutColor.R=255;
						OutColor.G=145;
						OutColor.B=145;
					}
					else if (TargetLevelDiff > -4 &&TargetLevelDiff <= -2)
					{
						OutColor.R=250;
						OutColor.G=254;
						OutColor.B=145;
					}
					else if (TargetLevelDiff > -2 &&TargetLevelDiff <= 1)
					{
						OutColor.R=255;
						OutColor.G=255;
						OutColor.B=255;
					}
					else if (TargetLevelDiff > 1 &&TargetLevelDiff <= 3)
					{
						OutColor.R=162;
						OutColor.G=255;
						OutColor.B=171;
					}
					else if (TargetLevelDiff > 3 &&TargetLevelDiff <= 10)
					{
						OutColor.R=162;
						OutColor.G=168;
						OutColor.B=252;
					}
					else if (TargetLevelDiff > 10)
					{
						OutColor.R=0;
						OutColor.G=0;
						OutColor.B=255;
					}
				}
				// АЇАъ·№є§ 85АМ»у АП °жїм ·№є§ ВчАМ »ц»у ЗҐЅГё¦ БЩї© єёї©БШґЩ. 
				else
				{
					if (TargetLevelDiff <= -11)
					{
						OutColor.R=255;
						OutColor.G=0;
						OutColor.B=0;
					}
					else if (TargetLevelDiff > -11 &&TargetLevelDiff <= -3)
					{
						OutColor.R=255;
						OutColor.G=145;
						OutColor.B=145;
					}
					else if (TargetLevelDiff > -3 &&TargetLevelDiff <= -2)
					{
						OutColor.R=250;
						OutColor.G=254;
						OutColor.B=145;
					}
					else if (TargetLevelDiff > -2 &&TargetLevelDiff <= 1)
					{
						OutColor.R=255;
						OutColor.G=255;
						OutColor.B=255;
					}
					else if (TargetLevelDiff > 1 &&TargetLevelDiff <= 2)
					{
						OutColor.R=162;
						OutColor.G=255;
						OutColor.B=171;
					}
					else if (TargetLevelDiff > 2 &&TargetLevelDiff <= 10)
					{
						OutColor.R=162;
						OutColor.G=168;
						OutColor.B=252;
					}
					else if (TargetLevelDiff > 10)
					{
						OutColor.R=0;
						OutColor.G=0;
						OutColor.B=255;
					}
				}
			}
			//¶уАМєкґВ 77·№є§ АМЗП ±ФДўАё·О Аь ·№є§ µїАПЗП°Ф єЇ°ж
			else
			{
				if (TargetLevelDiff <= -11)
				{
					OutColor.R=255;
					OutColor.G=0;
					OutColor.B=0;
				}
				else if (TargetLevelDiff > -11 &&TargetLevelDiff <= -6)
				{
					OutColor.R=255;
					OutColor.G=145;
					OutColor.B=145;
				}
				else if (TargetLevelDiff > -6 &&TargetLevelDiff <= -3)
				{
					OutColor.R=250;
					OutColor.G=254;
					OutColor.B=145;
				}
				else if (TargetLevelDiff > -3 &&TargetLevelDiff <= 2)
				{
					OutColor.R=255;
					OutColor.G=255;
					OutColor.B=255;
				}
				else if (TargetLevelDiff > 2 &&TargetLevelDiff <= 5)
				{
					OutColor.R=162;
					OutColor.G=255;
					OutColor.B=171;
				}
				else if (TargetLevelDiff > 5 &&TargetLevelDiff <= 10)
				{
					OutColor.R=162;
					OutColor.G=168;
					OutColor.B=252;
				}
				else if (TargetLevelDiff > 10)
				{
					OutColor.R=0;
					OutColor.G=0;
					OutColor.B=255;
				}
			}
		*/
		int diff = Math.min(Math.max(0, level - getLevel()), Config.MONSTER_LEVEL_DIFF_EXP_PENALTY.length - 1);

		double xp = SafeMath.mulAndLimit((double) getExpReward(), damage / totalDamage);
		double sp = SafeMath.mulAndLimit((double) getSpReward(), damage / totalDamage);

		double mod = (100. - Config.MONSTER_LEVEL_DIFF_EXP_PENALTY[diff]) / 100.;
		xp = SafeMath.mulAndLimit(xp, mod);
		sp = SafeMath.mulAndLimit(sp, mod);

		xp = Math.max(0., xp);
		sp = Math.max(0., sp);
		
		if(getLevel() > dengji)
		{
			xp *= 0;
			sp *= 0;
		}

		return new double[] { xp, sp };
		
	}

	private double applyOverhit(Player killer, double xp)
	{
		//修改overhit--
		if(killer.getObjectId() == overhitAttackerId)
		{
			killer.sendPacket(SystemMsg.OVERHIT);
			killer.sendPacket(new ExMagicAttackInfo(killer.getObjectId(), getObjectId(), ExMagicAttackInfo.OVERHIT));
			/*НА ОФФЕ НЕТУ: killer.sendPacket(new SystemMessage(SystemMessage.ACQUIRED_S1_BONUS_EXPERIENCE_THROUGH_OVER_HIT).addNumber(overHitExp));*/
			if(xp > 0 )
			{
				int overHitExp = calculateOverhitExp(xp);
			
				xp += overHitExp;
			}
		//修改overhit--
		}
		return xp;
	}

	@Override
	public void setOverhitAttacker(Creature attacker)
	{
		overhitAttackerId = attacker == null ? 0 : attacker.getObjectId();
	}

	public double getOverhitDamage()
	{
		return _overhitDamage;
	}

	@Override
	public void setOverhitDamage(double damage)
	{
		_overhitDamage = damage;
	}

	public int calculateOverhitExp(final double normalExp)
	{
		double overhitPercentage = getOverhitDamage() * 100 / getMaxHp();
		if(overhitPercentage > 25)
			overhitPercentage = 25;
		double overhitExp = overhitPercentage / 100 * normalExp;
		setOverhitAttacker(null);
		setOverhitDamage(0);
		return (int) Math.round(overhitExp);
	}

	@Override
	public boolean isAggressive()
	{
		return (Config.ALT_CHAMPION_CAN_BE_AGGRO || getChampion() == 0) && super.isAggressive();
	}

	@Override
	public Faction getFaction()
	{
		if(getTemplate().isNoClan())
			return Faction.NONE;

		return Config.ALT_CHAMPION_CAN_BE_SOCIAL || getChampion() == 0 ? super.getFaction() : Faction.NONE;
	}

	@Override
	public boolean isMonster()
	{
		return true;
	}

	@Override
	public Clan getClan()
	{
		return null;
	}

	@Override
	public boolean isPeaceNpc()
	{
		return false;
	}
}