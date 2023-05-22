
package l2s.gameserver.botscript.actionhandler;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.botscript.BotConfigImp;
import l2s.gameserver.botscript.MonsterSelectUtil;
import l2s.gameserver.botscript.PetTargetChoose;
import l2s.gameserver.core.BotConfig;
import l2s.gameserver.core.BotEngine;
import l2s.gameserver.core.BotPetOwnerIdleAction;
import l2s.gameserver.core.BotSkillStrategy;
import l2s.gameserver.core.Geometry;
import l2s.gameserver.core.IBotActionHandler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import l2s.gameserver.ai.ServitorAI;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.*;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.model.instances.SummonInstance;
import l2s.gameserver.skills.SkillEntry;

public class BotAttack implements IBotActionHandler
{
	private static /* synthetic */ int[] $SWITCH_TABLE$botscript$PetTargetChoose;

	@Override
	public boolean doAction(Player actor, BotConfig config, boolean isSitting, boolean movable, boolean simpleActionDisable)
	{
		boolean doAttack = this.doAttack(actor, (BotConfigImp) config, isSitting, movable, simpleActionDisable);
		boolean doSummon = this.doSummonAttack(actor, (BotConfigImp) config, isSitting, movable, simpleActionDisable);
		return doAttack || doSummon;
	}

	private boolean doAttack(Player actor, BotConfigImp config, boolean isSitting, boolean movable, boolean simpleActionDisable)
	{
		if(!config.isAutoAttack())
		{
			return false;
		}
		if(actor.isInPeaceZone())
		{
			return false;
		}
		if(actor.isSitting())
		{
			return false;
		}
		if(this.isActionsDisabledExcludeAttack(actor) && config.isCoverMember() || !actor.isActionsDisabled())
		{
			Optional<MonsterInstance> mob = Optional.empty();
			MonsterInstance monster = null;
			MonsterInstance petTarget = this.petTarget(actor);
			if(config.isCoverMember() || (actor.getTarget() == null || !actor.getTarget().isMonster() || this.getMonster(actor.getTarget()).isDead()) && petTarget == null)
			{
				mob = MonsterSelectUtil.apply(actor);
				if(!mob.isPresent())
				{
					if(!actor.isImmobilized())
					{
						this.returnHome(actor);
					}
					return false;
				}
				monster = mob.get();
			}
			else
			{
				mob = MonsterSelectUtil.apply(actor);
				if (mob.isPresent()) {
					monster = mob.get();
				}else {
					monster = petTarget != null ? petTarget : this.getMonster(actor.getTarget());
				}

			}

			Creature monsterTarget = monster.getAI().getAttackTarget();
			// 如果 怪物 有目标 且 目标 是一个玩家 且 (目标的不是当前玩家 或者 不是 我的队友  )
			if (monsterTarget!=null && monsterTarget.isPlayer() && monsterTarget.getObjectId() != actor.getObjectId() && !monsterTarget.getPlayer().isInSameParty(actor) ) {
				config.addBlockTargetId(monster.getObjectId());
				actor.setTarget(null);
				ThreadPoolManager.getInstance().schedule(new Runnable() {
					@Override
					public void run() {
						config.releaseMemory(actor);

					}
				},   60 * 1000L, TimeUnit.MILLISECONDS);

				return false;
			}

			if(actor.getTarget() != monster)
			{
				actor.setTarget(monster);
			}

			// 如果队伍中有矮子
			// 如何判断矮子是否使用了技能
			Optional<Player> first = null;
			// 自己选择攻击
			boolean ownerAttack = false;
			Party party = actor.getParty();
			if (party != null) {
				first = party.getPartyMembers().stream().filter(play -> play.getClassId().getId() == 118).findFirst();
			}

			Player aiZi = null;
			if (first != null && first.isPresent()) {
				MonsterInstance aiZiMonster = null;
				aiZi = first.get();

				if (actor != aiZi) {
					// 矮子缺蓝的时候 自己去攻击
					if (aiZi.getCurrentMp()<31) {
						ownerAttack = true;
					}
					// 获取到队伍里矮子的 挂机配置
					LinkedList<BotSkillStrategy> botSkillStrategies = BotEngine.getInstance().getBotConfig(aiZi).getAttackStrategy();
					if (botSkillStrategies==null || botSkillStrategies.size() <= 0) {
						ownerAttack = true;
					}
					if (aiZi.isDead() || aiZi.isActionsDisabled()) {
						ownerAttack = true;
					}
					if (!ownerAttack) {
						if (aiZi.getTarget() == null) {
							return false;
						}else{
							for(BotSkillStrategy skillStrategy : botSkillStrategies)
							{
								if(skillStrategy.getSkillId() == 254 || skillStrategy.getSkillId() == 302){
									// 如果使用了 去拿到上次攻击目标的 id
									GameObject aroundObjectById = World.getAroundObjectById(actor, skillStrategy.getLastTargetObjectId() != 0 ? skillStrategy.getLastTargetObjectId():aiZi.getTarget().getObjectId());
									aiZiMonster = MonsterSelectUtil.getMob((Creature) aroundObjectById);
									if (aiZiMonster !=null && skillStrategy.useMe(aiZi,aiZiMonster)) {
//										aiZi.getListeners().onMagicUse(aiZi.getKnownSkill(254).getTemplate(),aiZiMonster,true);// 这个有啥用？
										skillStrategy.setLastTargetObjectId(actor.getTargetId());
										if (aiZiMonster.isMonster() || !aiZiMonster.isDead()) {
											actor.setTarget(aiZiMonster);
										}else
											return false;

									}// 没对目标使用过 等待
									else
										return false;
									break;
								}
							}
						}
					}
				}
				else {
					// 如果 player 是矮子 技能次略里面 有放花技能 只能使用这个技能 不让他攻击 使用之后 切换目标
					LinkedList<BotSkillStrategy> botSkillStrategies = BotEngine.getInstance().getBotConfig(actor).getAttackStrategy();
					if (botSkillStrategies!=null && botSkillStrategies.size()>0) {
						for (BotSkillStrategy botSkillStrategy : botSkillStrategies) {
							if(botSkillStrategy.getSkillId() == 254 || botSkillStrategy.getSkillId() == 302){

								// 如果使用了这个技能
								SkillEntry skillEntry = actor.getKnownSkill(botSkillStrategy.getSkillId());
								if (skillEntry!=null && skillEntry.isAltUse()) {
									botSkillStrategy.setLastTargetObjectId(actor.getTargetId());
									break;
								}
							}
						}
					}
				}
			}
			if(!GeoEngine.canSeeTarget(actor, monster))
			{
				actor.getMovement().moveToLocation(monster.getLoc(), 0, !actor.getVarBoolean("no_pf"), true, false);
				if(monster.getObjectId() != config.getCurrentTargetObjectId())
				{
					config.setCurrentTargetObjectId(monster.getObjectId());
				}
				config.setTryTimes(config.getTryTimes() + 1);//(config.getTryTimes() + 1) 修改攻擊間隔時間+1
				if(config.getTryTimes() >= 15)
				{
					actor.sendMessage("\u653b\u51fb\u8d85\u65f6\uff0c\u5207\u6362\u76ee\u6807");
					/*\u653b\u51fb\u8d85\u65f6\uff0c\u5207\u6362\u76ee\u6807 攻击超时，切换目标*/
					config.setTryTimes(0);
					config.addBlockTargetId(monster.getObjectId());
					actor.setTarget(null);
				}
				return false;
			}
			if(config.getTryTimes() != 0)
			{
				config.setTryTimes(0);
			}
			boolean useStrategy = false;
			for(BotSkillStrategy s : config.getAttackStrategy())
			{
				useStrategy = s.useMe(actor, monster);
				// 去除后 1秒 内 把所有技能都检测使用 可以达到 增快使用技能的频率 目前测试是 3个技能 会将所有技能都使用完全 而且 没有空闲
//				if(useStrategy)
//					break;
			}
			if(!useStrategy && config.isUsePhysicalAttack())
			{
				actor.getTarget().onAction(actor, false);
			}
		}
		return true;
	}

	private boolean doSummonAttack(Player actor, BotConfigImp config, boolean isSitting, boolean movable, boolean simpleActionDisable)
	{
		if(!config.isSummonAttack())
		{
			return false;
		}
		if(actor.isInPeaceZone())
		{
			return false;
		}
		SummonInstance pet = actor.getSummon();
		if(pet == null || pet.isActionsDisabled())
		{
			return false;
		}
		GameObject target = pet.getTarget();
		if(target != null)
		{
			MonsterInstance mob = null;
			mob = this.getMonster(target);
			if(mob == null || mob.isDead() || !Geometry.calc(actor, mob))
			{
				this.resetTarget(pet, actor, config);
			}
		}
		else
		{
			this.resetTarget(pet, actor, config);
		}
		if((target = pet.getTarget()) != null)
		{
			pet.getAI().Attack(this.getMonster(target), false, false);
			if(config.getBpoidleAction() != BotPetOwnerIdleAction.\u539f\u5730\u4e0d\u52a8 && (!config.isFollowAttack() || config.isAutoAttack() && !config.isUsePhysicalAttack() && config.getAttackStrategy().isEmpty()))
			/*\u539f\u5730\u4e0d\u52a8 原地不动*/
			{
				if(config.getBpoidleAction() == BotPetOwnerIdleAction.\u9760\u8fd1\u53ec\u5524\u517d)
				/*\u9760\u8fd1\u53ec\u5524\u517d 靠近召唤兽*/
				{
					actor.getMovement().moveToLocation(pet.getLoc(), 400, !actor.getVarBoolean("no_pf"));
				}
				else
				{
					double dist;
					Party party = actor.getParty();
					if(party != null && (dist = actor.getDistance(party.getPartyLeader())) > 400.0 && dist < 3500.0)
					{
						actor.getMovement().moveToLocation(party.getPartyLeader().getLoc(), 400, !actor.getVarBoolean("no_pf"));
					}
				}
			}
			return true;
		}
		return false;
	}

	private void resetTarget(SummonInstance pet, Player actor, BotConfigImp config)
	{
		GameObject target;
		if(pet.getTarget() != null)
		{
			pet.setTarget(null);
		}
		if((target = actor.getTarget()) != null && target.isMonster() && !this.getMonster(target).isDead() && Geometry.calc(actor, this.getMonster(target)))
		{
			pet.setTarget(target);
		}
		else if(target == null)
		{
			Optional<MonsterInstance> mob = Optional.empty();
			switch(BotAttack.$SWITCH_TABLE$botscript$PetTargetChoose()[config.getPetTargetChoose().ordinal()])
			{
				case 1:
				{
					mob = MonsterSelectUtil.apply(actor);
					break;
				}
				case 2:
				{
					mob = target == null ? Optional.empty() : Optional.of(this.getMonster(target));
					break;
				}
				case 3:
				{
					Party party = actor.getParty();
					if(party == null)
						break;
					target = party.getPartyLeader().getTarget();
					mob = target == null || !target.isMonster() ? Optional.empty() : Optional.of(this.getMonster(target));
				}
			}
			if(!mob.isPresent())
			{
				if(!actor.isImmobilized())
				{
					this.returnHome(actor);
				}
				return;
			}
			MonsterInstance monster = mob.get();
			pet.setTarget(monster);
		}
	}

	private void returnHome(Player actor)
	{
		BotConfig config = BotEngine.getInstance().getBotConfig(actor);
		if(actor.getDistance(config.getStartX(), config.getStartY(), config.getStartZ()) < 5000.0)
		{
			actor.standUp();
			actor.getMovement().moveToLocation(config.getStartX(), config.getStartY(), config.getStartZ(), 100, !actor.getVarBoolean("no_pf"), true, false);
		}
	}

	private MonsterInstance petTarget(Player actor)
	{
		SummonInstance instance = actor.getSummon();
		if(instance == null)
		{
			return null;
		}
		if(instance.getTarget() == null)
		{
			return null;
		}
		MonsterInstance target = this.getMonster(instance.getTarget());
		if(target == null || target.isDead())
		{
			return null;
		}
		return target;
	}

	static /* synthetic */ int[] $SWITCH_TABLE$botscript$PetTargetChoose()
	{
		if($SWITCH_TABLE$botscript$PetTargetChoose != null)
		{
			//int[] arrn;
			return $SWITCH_TABLE$botscript$PetTargetChoose;
		}
		int[] arrn = new int[PetTargetChoose.values().length];
		try
		{
			arrn[PetTargetChoose.自主选怪.ordinal()] = 1;
		}
		catch(NoSuchFieldError noSuchFieldError)
		{
		}
		try
		{
			arrn[PetTargetChoose.跟随主人.ordinal()] = 2;
		}
		catch(NoSuchFieldError noSuchFieldError)
		{
		}
		try
		{
			arrn[PetTargetChoose.跟随队长.ordinal()] = 3;
		}
		catch(NoSuchFieldError noSuchFieldError)
		{
		}
		$SWITCH_TABLE$botscript$PetTargetChoose = arrn;
		return $SWITCH_TABLE$botscript$PetTargetChoose;
	}
}