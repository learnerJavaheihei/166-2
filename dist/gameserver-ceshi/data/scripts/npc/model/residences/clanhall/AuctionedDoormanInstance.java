package npc.model.residences.clanhall;

import java.util.StringTokenizer;

import org.apache.commons.lang3.ArrayUtils;
import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.entity.residence.ClanHall;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.Privilege;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.HtmlUtils;
import l2s.gameserver.utils.ReflectionUtils;

/**
 * @author VISTALL
 * @date 10:50/20.06.2011
 */
public class AuctionedDoormanInstance extends NpcInstance
{
	private int[] _doors;
	private boolean _elite;

	public AuctionedDoormanInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);

		_doors = template.getAIParams().getIntegerArray("doors", ArrayUtils.EMPTY_INT_ARRAY);
		_elite = template.getAIParams().getBool("elite", false);
	}

	@Override
	public void onBypassFeedback(Player player, String command)
	{
		ClanHall clanHall = getClanHall();
		if(command.equalsIgnoreCase("openDoors"))//打開大門
		{
			if(player.hasPrivilege(Privilege.CH_ENTER_EXIT) && player.getClan().getHasHideout() == clanHall.getId())
			{
				for(int d : _doors)
					ReflectionUtils.getDoor(d).openMe();
				showChatWindow(player, "residence2/clanhall/agitafterdooropen.htm", false);//關閉大門
			}
			else
				showChatWindow(player, "residence2/clanhall/noAuthority.htm", false);//沒有權限
		}
		else if(command.equalsIgnoreCase("closeDoors"))
		{
			if(player.hasPrivilege(Privilege.CH_ENTER_EXIT) && player.getClan().getHasHideout() == clanHall.getId())
			{
				for(int d : _doors)
					ReflectionUtils.getDoor(d).closeMe(player, true);
				showChatWindow(player, "residence2/clanhall/agitafterdoorclose.htm", false);//已關閉大門
			}
			else
				showChatWindow(player, "residence2/clanhall/noAuthority.htm", false);//沒有權限
		}
		else if(command.equalsIgnoreCase("banish"))
		{
			if(player.hasPrivilege(Privilege.CH_DISMISS))
			{
				clanHall.banishForeigner(player.getClan().getClanId());
				showChatWindow(player, "residence2/clanhall/agitafterbanish.htm", false);//驅逐外來者
			}
			else
				showChatWindow(player, "residence2/clanhall/noAuthority.htm", false);//沒有權限
		}
		else
			super.onBypassFeedback(player, command);
	}

	@Override
	public void showChatWindow(Player player, int val, boolean firstTalk, Object... arg)
	{
		ClanHall clanHall = getClanHall();
		if(clanHall != null)
		{
			Clan playerClan = player.getClan();
			if(playerClan != null && playerClan.getHasHideout() == clanHall.getId())
			{
				if(!player.isClanLeader())
				{
					showChatWindow(player,  "residence2/clanhall/WyvernAgitJanitorHi.htm" , firstTalk, "%owner%", playerClan.getName());//修復根據地守門人
				}
				else
				{
					showChatWindow(player, "residence2/clanhall/AgitJanitorHi.htm", firstTalk, "%owner%", playerClan.getName());//修復根據地守門人
				}				
			}
			else
			{
				/**if(playerClan != null && playerClan.getCastle() != 0)
				* {
				* 	Castle castle = ResidenceHolder.getInstance().getResidence(playerClan.getCastle());
				* 	HtmlMessage html = new HtmlMessage(this);
				* 	html.setFile("merchant/territorystatus.htm");
				* 	html.replace("%castlename%", HtmlUtils.htmlResidenceName(castle.getId()));
				* 	html.replace("%taxpercent%", String.valueOf(castle.getSellTaxPercent()));
				* 	html.replace("%clanname%", playerClan.getName());
				* 	html.replace("%clanleadername%", playerClan.getLeaderName());
				* 	player.sendPacket(html);
				* }
				* else
				* 	showChatWindow(player, "residence2/clanhall/yesAgitInfo.htm", firstTalk);
				*/
				if(clanHall.getOwner() != null)
				{
					HtmlMessage adminReply = new HtmlMessage(5);
					StringBuffer replyMSG = new StringBuffer("<html><title>對話</title><body>");
					replyMSG.append("<td>" + "您好！<br>這根據地的主人是<font color=\"55FFFF\">" + clanHall.getOwner().getName() + "</font>血盟之血盟主" + "</td>");
					replyMSG.append("<td>" + "<font color=\"55FFFF\">" + clanHall.getOwner().getLeaderName() + "</font>陛下。<br>" + "</td>");
					replyMSG.append("<td>" + "很抱歉，非<font color=\"55FFFF\">" + clanHall.getOwner().getName() + "</font>血盟之血盟成員不得進入。" + "</td>");
					replyMSG.append("</table><br><br>");
					replyMSG.append("</body></html>");
					adminReply.setHtml(replyMSG.toString());
					player.sendPacket(adminReply);
				}
				else
				{
					showChatWindow(player, "residence2/clanhall/noAgitInfo.htm", firstTalk);//如果沒有擁有者 將顯示還在拍賣中
				}
			}
		}
		else
			showChatWindow(player, "residence2/clanhall/noAgitInfo.htm", firstTalk);//如果沒有擁有者 將顯示還在拍賣中
	}
}
