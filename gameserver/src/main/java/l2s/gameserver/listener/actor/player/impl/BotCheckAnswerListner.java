package l2s.gameserver.listener.actor.player.impl;

import l2s.commons.lang.reference.HardReference;

import l2s.gameserver.instancemanager.BotCheckManager;
import l2s.gameserver.listener.actor.player.OnAnswerListener;
import l2s.gameserver.model.Player;

/**
 * @author Iqman
 * @date 11:35/21.0.2013
 */
public class BotCheckAnswerListner implements OnAnswerListener
{
	private HardReference<Player> _playerRef;
	private int _qId;

	public BotCheckAnswerListner(Player player, int qId)
	{
		_playerRef = player.getRef();
		_qId = qId;
	}

	@Override
	public void sayYes()
	{
		Player player = _playerRef.get();
		if(player == null)
			return;
		boolean rightAnswer = BotCheckManager.checkAnswer(_qId, true);	
		if(rightAnswer)
		{
			player.increaseBotRating();
			sendFeedBack(player, true, player.isLangRus());
		}	
		else
		{
			sendFeedBack(player, false, player.isLangRus());
			player.decreaseBotRating();
		}	
	}

	@Override
	public void sayNo()
	{
		Player player = _playerRef.get();
		if(player == null)
			return;
		boolean rightAnswer = BotCheckManager.checkAnswer(_qId, false);
		if(rightAnswer)
		{
			player.increaseBotRating();
			sendFeedBack(player, true, player.isLangRus());
		}	
		else
		{
			player.decreaseBotRating();
			sendFeedBack(player, false, player.isLangRus());
		}	
	}
	
	private void sendFeedBack(Player player, boolean rightAnswer, boolean isLangRus)
	{
		if(rightAnswer)
		{
			if(isLangRus)
				player.sendMessage("您的回答正確！");
			else
				player.sendMessage("您的回答正确！");
		}
		else
		{
			if(isLangRus)
				player.sendMessage("您的回答錯誤！萬一您不正確回答了幾次，將會被投入監獄進行教導。");
			else
				player.sendMessage("您的回答错误！万一您不正确回答了几次，将会被投入监狱进行教导。");
		}
	}
}
