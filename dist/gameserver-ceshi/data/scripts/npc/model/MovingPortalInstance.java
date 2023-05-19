package npc.model;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.data.xml.holder.SkillHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.templates.npc.NpcTemplate;

/**
 * 即時傳送門
 */
public class MovingPortalInstance extends NpcInstance
{
	private static final long serialVersionUID = 1L;

	private final SkillEntry _returnSkill;

	public MovingPortalInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
		_returnSkill = SkillHolder.getInstance().getSkillEntry(getParameter("return_skill_id", 0), getParameter("return_skill_level", 1));
	}

	@Override
	public void showChatWindow(Player player, int val, boolean firstTalk, Object... replace)
	{
		forceUseSkill(_returnSkill, player);
	}
}