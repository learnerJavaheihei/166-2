package ai.seedofdestruction;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.ai.DefaultAI;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.components.SceneMovie;

/**
 * @author pchayka
 */
public class TiatCamera extends DefaultAI
{
	private List<Player> _players = new ArrayList<Player>();

	public TiatCamera(NpcInstance actor)
	{
		super(actor);
		actor.getFlags().getImmobilized().start();
		actor.getFlags().getDamageBlocked().start();
	}

	@Override
	protected boolean thinkActive()
	{
		NpcInstance actor = getActor();
		for(Player p : World.getAroundPlayers(actor, 300, 300))
			if(!_players.contains(p))
			{
				//p.startScenePlayer(SceneMovie.TIAT_OPENING);
				_players.add(p);
			}
		return true;
	}
}