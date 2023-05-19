package npc.model;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.CommandChannel;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ReflectionUtils;



/**
 * @author Bonux (bonuxq@gmail.com)
 * 09.02.2019
 * Developed for L2-Scripts.com
 **/
public class FreyaClearBuffInstance extends NpcInstance{

	protected final SkillEntry skills = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 50052, 1);
    public FreyaClearBuffInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
    {
        super(objectId, template, set);
    }

    @Override
    public void onMenuSelect(Player player, int ask, long reply, int state)
    {
        if(ask == -666)
        {
			int t = player.getAbnormalList().getCount(50050);
			if (t == 1)
			{
				player.getAbnormalList().stop(50050);
				player.doCast(skills, player, true);
				player.sendMessage("移除技能了。");
			}
			else
			{
				player.sendMessage("當你中了技能才可以解除。");
			}
        }
        else
            super.onMenuSelect(player, ask, reply, state);
    }
}
