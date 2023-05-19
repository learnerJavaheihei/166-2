package l2s.gameserver.skills.effects.instant;

import java.util.ArrayList;//修復混亂術
import java.util.List;
import java.util.Set;//修復混亂術
import java.util.TreeSet;//修復混亂術

import l2s.commons.util.Rnd;
import l2s.gameserver.model.AggroList;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.templates.skill.EffectTemplate;

/**
 * @author VISTALL
 * @date 12:01/29.01.2011
 */
public class i_randomize_hate extends i_abstract_effect
{
	public i_randomize_hate(EffectTemplate template)
	{
		super(template);
	}

	@Override
	protected boolean checkCondition(Creature effector, Creature effected)
	{
		if(effected.isRaid())
			return false;
		return effected.isMonster();
	}

	@Override
	public void instantUse(Creature effector, Creature effected, boolean reflected)
	{
		MonsterInstance monster = (MonsterInstance) effected;
		Creature mostHated = monster.getAggroList().getMostHated(monster.getAI().getMaxHateRange());
		if(mostHated == null)
			return;

		AggroList.AggroInfo mostAggroInfo = monster.getAggroList().get(mostHated);
		if(mostAggroInfo == null)
			return;
		List<Creature> hateList = new ArrayList<Creature>() ;//修復混亂術--
		
		Set<AggroList.HateInfo> set = new TreeSet<AggroList.HateInfo>(AggroList.HateComparator.getInstance());
		set.addAll(monster.getAggroList().getCharMap().values());
		for(AggroList.HateInfo aggroInfo : set)
			hateList.add(aggroInfo.attacker);//--修復混亂術
		hateList.remove(mostHated);

		if(!hateList.isEmpty())
		{
			AggroList.AggroInfo newAggroInfo = monster.getAggroList().get(hateList.get(Rnd.get(hateList.size())));
			if(newAggroInfo == null)
				return;

			final int oldHate = newAggroInfo.hate;

			newAggroInfo.hate = mostAggroInfo.hate;
			mostAggroInfo.hate = oldHate;
		}
	}
}
