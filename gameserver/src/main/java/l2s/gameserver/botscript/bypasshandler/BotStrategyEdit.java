package l2s.gameserver.botscript.bypasshandler;

import l2s.gameserver.botscript.BotControlPage;
import l2s.gameserver.core.BotConfig;
import l2s.gameserver.core.BotEngine;
import l2s.gameserver.core.BotResType;
import l2s.gameserver.core.BotSkillStrategy;
import java.util.LinkedList;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.skills.SkillEntry;

public class BotStrategyEdit
{
	@Bypass(value = "bot.setSkillCondition")
	public void botSetSkillCondition(Player player, NpcInstance npc, String[] param)
	{
		if(BotEngine.getInstance().getBotConfig(player).getAttackStrategy().size() == 5)
		{
			player.sendMessage("\u6700\u591a\u53ea\u80fd\u6dfb\u52a05\u6761\u6280\u80fd\u7b56\u7565");
			/*\u6700\u591a\u53ea\u80fd\u6dfb\u52a05\u6761\u6280\u80fd\u7b56\u7565 最多只能添加5条技能策略*/
		}
		else
		{
			int skillId = Integer.parseInt(param[0]);
			boolean targetHpCheck = Boolean.parseBoolean(param[1]);
			int hpPercent = Integer.parseInt(param[2].substring(0, param[2].length() - 1));
			boolean oneTime = Boolean.parseBoolean(param[3]);
			if(player.getKnownSkill(skillId) != null)
			{
				BotSkillStrategy skillStrategy = new BotSkillStrategy(skillId, targetHpCheck, oneTime, hpPercent);
				if(param.length == 5)
				{
					skillStrategy.setBaseSelfMpCheck(Boolean.parseBoolean(param[4]));
				}
				BotEngine.getInstance().getBotConfig(player).getAttackStrategy().addLast(skillStrategy);
			}
		}
		BotControlPage.fightPage(player);
	}

	@Bypass(value = "bot.resOrder")
	public void resOrder(Player player, NpcInstance npc, String[] param)
	{
		LinkedList<BotResType> config = BotEngine.getInstance().getBotConfig(player).getResType();
		int index = Integer.parseInt(param[0]);
		BotResType botResType = config.remove(index);
		config.add(++index, botResType);
		BotControlPage.protectPage(player);
	}

	@Bypass(value = "bot.skillOrderUp")
	public void botSkillOrderUp(Player player, NpcInstance npc, String[] param)
	{
		LinkedList<BotSkillStrategy> config = BotEngine.getInstance().getBotConfig(player).getAttackStrategy();
		if(config.size() <= 1)
		{
			return;
		}
		int index = Integer.parseInt(param[0]);
		if(index != 0)
		{
			BotSkillStrategy skillStrategy = config.remove(index);
			config.add(--index, skillStrategy);
		}
		BotControlPage.fightPage(player);
	}

	@Bypass(value = "bot.skillOrderDown")
	public void botSkillOrderDown(Player player, NpcInstance npc, String[] param)
	{
		LinkedList<BotSkillStrategy> config = BotEngine.getInstance().getBotConfig(player).getAttackStrategy();
		if(config.size() <= 1)
		{
			return;
		}
		int index = Integer.parseInt(param[0]);
		if(index != config.size() - 1)
		{
			BotSkillStrategy skillStrategy = config.remove(index);
			config.add(++index, skillStrategy);
		}
		BotControlPage.fightPage(player);
	}

	@Bypass(value = "bot.skillRemove")
	public void botSkillRemove(Player player, NpcInstance npc, String[] param)
	{
		LinkedList<BotSkillStrategy> config = BotEngine.getInstance().getBotConfig(player).getAttackStrategy();
		int index = Integer.parseInt(param[0]);
		config.remove(index);
		BotControlPage.fightPage(player);
	}
}