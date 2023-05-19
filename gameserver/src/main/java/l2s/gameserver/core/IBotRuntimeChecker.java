package l2s.gameserver.core;

import java.util.function.Predicate;
import l2s.gameserver.model.Player;

public interface IBotRuntimeChecker extends Predicate<Player>
{
}