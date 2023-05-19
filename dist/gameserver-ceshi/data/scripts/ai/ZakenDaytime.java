package ai;

import java.util.concurrent.TimeUnit;

import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.Fighter;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.ExSendUIEventPacket;
import l2s.gameserver.network.l2.s2c.PlaySoundPacket;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.geometry.Location;

/**
 * Daytime Zaken.
 * - иногда телепортируется в случайную комнату
 *
 * @author pchayka
 */
public class ZakenDaytime extends Fighter
{
	SkillEntry skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 4222, 1);//瞬間移動
	private static final Location[] _locations = new Location[]{
			new Location(55256, 219096, -3232),
			new Location(56280, 220104, -3232),
			new Location(54216, 220136, -3232),
			new Location(56280, 218120, -3232),
			new Location(54232, 218120, -3232),
			new Location(56280, 218056, -2960),
			new Location(56280, 220104, -2960),
			new Location(54264, 220136, -2960),
			new Location(54216, 218088, -2960),
			new Location(55256, 219080, -2960)
	};

	private long _teleportSelfTimer = 0L;
	private long _teleportSelfReuse = 120000L;		  // 120秒
	private NpcInstance actor = getActor();

	public ZakenDaytime(NpcInstance actor)
	{
		super(actor);
		setMaxPursueRange(Integer.MAX_VALUE / 2);
	}

	@Override
	protected void thinkAttack()
	{
		if(_teleportSelfTimer + _teleportSelfReuse < System.currentTimeMillis())
		{
			_teleportSelfTimer = System.currentTimeMillis();
			if(Rnd.chance(20))
			{
				actor.doCast(skillEntry, actor, false);
				ThreadPoolManager.getInstance().schedule(() ->
				{
					actor.teleToLocation(_locations[Rnd.get(_locations.length)]);
					actor.getAggroList().clear(true);
				}, 500);
			}
		}
		super.thinkAttack();
	}

	@Override
	protected void onEvtDead(Creature killer)
	{
		Reflection r = actor.getReflection();
		r.setReenterTime(System.currentTimeMillis(),true);
		for(Player p : r.getPlayers())
			p.sendPacket(new ExSendUIEventPacket(p, 1, 1, 0, 0));
		actor.broadcastPacket(new PlaySoundPacket(PlaySoundPacket.Type.MUSIC, "BS02_D", 1, actor.getObjectId(), actor.getLoc()));
		super.onEvtDead(killer);
	}

	@Override
	protected boolean teleportHome()
	{
		return false;
	}
}