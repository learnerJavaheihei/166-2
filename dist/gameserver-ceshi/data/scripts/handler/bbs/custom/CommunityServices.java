package handler.bbs.custom;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.StringTokenizer;

import l2s.gameserver.data.htm.HtmTemplates;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.dao.CharacterDAO;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.data.xml.holder.PremiumAccountHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.handler.bbs.BbsHandlerHolder;
import l2s.gameserver.handler.bbs.IBbsHandler;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.handler.voicecommands.VoicedCommandHandler;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.impl.PvPEvent;
import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.authcomm.AuthServerCommunication;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExBuySellListPacket;
import l2s.gameserver.network.l2.s2c.ExShowVariationCancelWindow;
import l2s.gameserver.network.l2.s2c.ExShowVariationMakeWindow;
import l2s.gameserver.network.l2.s2c.ExStorageMaxCountPacket;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.network.l2.s2c.ShowBoardPacket;
import l2s.gameserver.network.l2.s2c.ShowPCCafeCouponShowUI;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.templates.item.data.ItemData;
import l2s.gameserver.templates.PremiumAccountTemplate;
import l2s.gameserver.utils.HtmlUtils;
import l2s.gameserver.utils.ItemFunctions;
import l2s.gameserver.utils.Language;
import l2s.gameserver.utils.Log;
import l2s.gameserver.utils.TimeUtils;
import l2s.gameserver.utils.WarehouseFunctions;
import l2s.gameserver.utils.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Bonux
**/
public class CommunityServices extends CustomCommunityHandler
{
	private static final Logger _log = LoggerFactory.getLogger(CommunityServices.class);

	@Override
	public String[] getBypassCommands()
	{
		return new String[]
		{
			"_cbbsservices"
		};
	}

	@Override
	protected void doBypassCommand(Player player, String bypass)
	{
		StringTokenizer st = new StringTokenizer(bypass, "_");
		String cmd = st.nextToken();
		String html = "";

		if("cbbsservices".equals(cmd))
		{
			if(!st.hasMoreTokens())
			{
				player.sendMessage(player.isLangRus() ? "此功能尚未實裝。" : "此功能尚未实装。");
				player.sendPacket(ShowBoardPacket.CLOSE);
				return;
			}

			String cmd2 = st.nextToken();
			//bypass _cbbsservices_compensateACCOUNT_20200116
			if("compensateACCOUNT".equals(cmd2))//這一區是要給玩家補償用的區域 依據玩家的帳號只發一次
			{
				boolean ok = false;
				String cmd3 = st.nextToken();//取得發放日期
				//需記錄玩家objId 補償時間 領取當下時間
				if(!CheckCompensateAccount(player, cmd3)) //判斷補償玩家及 補償帳號 及日期
				{
					int ItemId = 0;
					int count = 0;
					
					switch (cmd3)
					{
						case "20200121": //這一個是補償日期設置
							ItemId = 70267;//補償ID
							count = 1;//補償數量
							break;
						case "20200101": //這一個是補償日期設置 我亂設置的，參考用的。
							ItemId = 57;//補償ID
							count = 1;//補償數量
							break;
					}
					if(ItemId > 0)
					{
						if (AddCompensateAccount(player, cmd3))
						{
							ItemFunctions.addItem(player, ItemId, count, true);
							ok = true;
						}
					}
				}
				if(ok)
				{
					player.sendMessage("领取成功。");
				}
				else
				{
					if(player.getLanguage() == Language.CHINESE_TW)
					{
						player.sendMessage("领取失敗或是已領取了。");
					}
					else
					{
						player.sendMessage("领取失败或是已领取了。");
					}
				}
				return;
				//先確定玩家帳號是否有領取過			
			}
			//bypass _cbbsservices_compensateID_2020xxxx
			else if("compensateID".equals(cmd2))//這一區是要給玩家補償用的區域 依據玩家的ID只發放一次
			{

			}
			if("pccoupon".equals(cmd2))
			{
				player.sendPacket(ShowBoardPacket.CLOSE);
				player.sendPacket(ShowPCCafeCouponShowUI.STATIC);
				return;
			}
			else if("sell".equals(cmd2))
			{
				if(!Config.BBS_SELL_ITEMS_ENABLED)
				{
					player.sendMessage(player.isLangRus() ? "功能禁用。" : "功能禁用。");
					player.sendPacket(ShowBoardPacket.CLOSE);
					return;
				}

				if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player))
				{
					onWrongCondition(player);
					return;
				}

				IBbsHandler handler = BbsHandlerHolder.getInstance().getCommunityHandler("_bbspage:shop");
				if(handler != null)
					handler.onBypassCommand(player, "_bbspage:shop");

				player.sendPacket(new ExBuySellListPacket.BuyList(null, player, 0.), new ExBuySellListPacket.SellRefundList(player, false, 0.));
				return;
			}
			else if("augment".equals(cmd2))
			{
				if(!st.hasMoreTokens())
					return;

				if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player))
				{
					onWrongCondition(player);
					return;
				}

				String cmd3 = st.nextToken();
				if("add".equals(cmd3))
				{
					player.sendPacket(ShowBoardPacket.CLOSE);
					player.sendPacket(SystemMsg.SELECT_THE_ITEM_TO_BE_AUGMENTED, ExShowVariationMakeWindow.STATIC);
				}
				else if("remove".equals(cmd3))
				{
					player.sendPacket(ShowBoardPacket.CLOSE);
					player.sendPacket(SystemMsg.SELECT_THE_ITEM_FROM_WHICH_YOU_WISH_TO_REMOVE_AUGMENTATION, ExShowVariationCancelWindow.STATIC);
				}

				return;
			}
			else if("warehouse".equals(cmd2))
			{
				if(!st.hasMoreTokens())
					return;

				if(!Config.BBS_WAREHOUSE_ENABLED)
				{
					player.sendMessage(player.isLangRus() ? "功能禁用。" : "功能禁用。");
					player.sendPacket(ShowBoardPacket.CLOSE);
					return;
				}

				if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player))
				{
					onWrongCondition(player);
					return;
				}

				IBbsHandler handler = BbsHandlerHolder.getInstance().getCommunityHandler("_bbspage:warehouse");
				if(handler != null)
					handler.onBypassCommand(player, "_bbspage:warehouse");

				String cmd3 = st.nextToken();
				if("withdraw".equals(cmd3))
				{
					if(!st.hasMoreTokens())
						return;

					String cmd4 = st.nextToken();
					if("personal".equals(cmd4))
						WarehouseFunctions.showRetrieveWindow(player);
					else if("clan".equals(cmd4))
						WarehouseFunctions.showWithdrawWindowClan(player);
				}
				else if("deposit".equals(cmd3))
				{
					if(!st.hasMoreTokens())
						return;

					String cmd4 = st.nextToken();
					if("personal".equals(cmd4))
						WarehouseFunctions.showDepositWindow(player);
					else if("clan".equals(cmd4))
						WarehouseFunctions.showDepositWindowClan(player);
				}
				return;
			}
			else if("cabinet".equals(cmd2))
			{
				if(st.hasMoreTokens())
				{
					onBypassCommand(player, "_cbbsservices_cabinet");

					String cmd3 = st.nextToken();
					if("repair".equals(cmd3))
					{
						player.sendMessage(player.isLangRus() ? "修復角色，請使用命令「.repair 角色名稱」。" : "修复角色，请使用命令「.repair [角色名称]」。");
						return;
					}
					else if("acp".equals(cmd3))
					{
						IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler("acp");
						if(vch != null)
							vch.useVoicedCommand("acp", player, "");
						return;
					}
					else if("config".equals(cmd3))
					{
						IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler("cfg");
						if(vch != null)
							vch.useVoicedCommand("cfg", player, "");
						return;
					}
					else if("stats".equals(cmd3))
					{
						IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler("whoiam");
						if(vch != null)
							vch.useVoicedCommand("whoiam", player, "");
						return;
					}
					else if("auto".equals(cmd3))
					{
						String command = null;
						String percent = "";

						String cmd4 = st.nextToken();
						if("hp".equals(cmd4))
						{
							command = "autohp";
							if(st.hasMoreTokens())
							{
								player.unsetVar("autohp");
								percent = st.nextToken().replace(" ", "");
							}
							else
								player.setVar("autohp", 1, -1);
						}
						else if("mp".equals(cmd4))
						{
							command = "automp";
							if(st.hasMoreTokens())
							{
								player.unsetVar("automp");
								percent = st.nextToken().replace(" ", "");
							}
							else
								player.setVar("automp", 1, -1);
						}
						else if("cp".equals(cmd4))
						{
							command = "autocp";
							if(st.hasMoreTokens())
							{
								player.unsetVar("autocp");
								percent = st.nextToken().replace(" ", "");
							}
							else
								player.setVar("autocp", 1, -1);
						}

						if(command != null)
						{
							IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
							if(vch != null)
								vch.useVoicedCommand(command, player, percent);
						}
						return;
					}
				}
				else
				{
					html = HtmCache.getInstance().getHtml("scripts/handler/bbs/pages/cabinet.htm", player);
					html = html.replace("<?account?>", player.getAccountName());
					html = html.replace("<?ip?>", player.getIP());

					String type;
					if(player.hasPremiumAccount() && (player.getPremiumAccount().getType() != 4) )//新註冊用戶贈送3小時會員
						type = player.isLangRus() ? "高級会员" : "高级帐号";
					else if(player.hasVIPAccount())
						type = "VIP " + player.getVIP().getLevel();
					else
						type = player.isLangRus() ? "共用" : "共用";

					html = html.replace("<?type?>", type);
					html = html.replace("<?char_name?>", player.getName());
					html = html.replace("<?clan_name?>", player.getClan() != null ? player.getClan().getName() : (player.isLangRus() ? "無" : "无"));
					html = html.replace("<?ally_name?>", player.getAlliance() != null ? player.getAlliance().getAllyName() : (player.isLangRus() ? "無" : "无"));

					int playedTime = player.getOnlineTime() / 60;
					int minutes = playedTime % 60;
					int hours = ((playedTime - minutes) / 60) % 24;
					int days = (((playedTime - minutes) / 60) - hours) / 24;
					html = html.replace("<?played_day?>", String.valueOf(days));
					html = html.replace("<?played_hour?>", String.valueOf(hours));
					html = html.replace("<?played_minute?>", String.valueOf(minutes));

					html = html.replace("<?rate_xp?>", doubleToString(player.getRateExp()));
					html = html.replace("<?rate_sp?>", doubleToString(player.getRateSp()));
					html = html.replace("<?rate_adena?>", doubleToString(player.getRateAdena()));
					html = html.replace("<?rate_drop?>", doubleToString(player.getRateItems()));
					html = html.replace("<?rate_spoil?>", doubleToString(player.getRateSpoil()));
					html = html.replace("<?rate_quest_reward?>", doubleToString(player.getRateQuestsReward()));
					html = html.replace("<?rate_quest_drop?>", doubleToString(player.getRateQuestsDrop()));
				}
			}
			else if("pa".equals(cmd2))
			{
				HtmTemplates tpls = HtmCache.getInstance().getTemplates("scripts/handler/bbs/pages/pa.htm", player);
				html = tpls.get(0);

				StringBuilder content = new StringBuilder();

				if(Config.PREMIUM_ACCOUNT_ENABLED)
				{
					if(st.hasMoreTokens())
					{
						String cmd3 = st.nextToken();
						if("info".equals(cmd3))
						{
							if(!st.hasMoreTokens())
								return;

							final int schemeId = Integer.parseInt(st.nextToken());

							final PremiumAccountTemplate paTemplate = PremiumAccountHolder.getInstance().getPremiumAccount(schemeId);
							if(paTemplate == null)
							{
								_log.warn(getClass().getSimpleName() + ": Error while open info about premium account scheme ID[" + schemeId + "]! Scheme is null.");
								return;
							}

							final int schemeDelay = Integer.parseInt(st.nextToken());

							List<ItemData> feeItems = paTemplate.getFeeItems(schemeDelay);
							if(feeItems == null)
								return;

							String delayName = "";
							if(schemeDelay > 0)
							{
								int days = schemeDelay / 24;
								int hours = schemeDelay % 24;
								if(days > 0 && hours > 0)
								{
									delayName = tpls.get(11);
									delayName = delayName.replace("<?days?>", String.valueOf(days));
									delayName = delayName.replace("<?hours?>", String.valueOf(hours));
								}
								else if(days > 0)
								{
									delayName = tpls.get(10);
									delayName = delayName.replace("<?days?>", String.valueOf(days));
								}
								else if(hours > 0)
								{
									delayName = tpls.get(9);
									delayName = delayName.replace("<?hours?>", String.valueOf(hours));
								}
							}
							else
								delayName = tpls.get(12);

							String infoBlock = tpls.get(6);
							infoBlock = infoBlock.replace("<?scheme_id?>", String.valueOf(paTemplate.getType()));
							infoBlock = infoBlock.replace("<?scheme_delay?>", String.valueOf(schemeDelay));
							infoBlock = infoBlock.replace("<?scheme_name?>", paTemplate.getName(player.getLanguage()));
							infoBlock = infoBlock.replace("<?period?>", delayName);
							infoBlock = infoBlock.replace("<?exp_rate?>", doubleToString(paTemplate.getExpRate()));
							infoBlock = infoBlock.replace("<?sp_rate?>", doubleToString(paTemplate.getSpRate()));
							infoBlock = infoBlock.replace("<?adena_drop_rate?>", doubleToString(paTemplate.getAdenaRate()));
							infoBlock = infoBlock.replace("<?items_drop_rate?>", doubleToString(paTemplate.getDropRate()));
							infoBlock = infoBlock.replace("<?spoil_rate?>", doubleToString(paTemplate.getSpoilRate()));
							infoBlock = infoBlock.replace("<?quest_drop_rate?>", doubleToString(paTemplate.getQuestDropRate()));
							infoBlock = infoBlock.replace("<?quest_reward_rate?>", doubleToString(paTemplate.getQuestRewardRate()));
							infoBlock = infoBlock.replace("<?enchant_chance?>", doubleToString(paTemplate.getEnchantChanceBonus()));
							infoBlock = infoBlock.replace("<?craft_chance?>", doubleToString(paTemplate.getCraftChanceBonus()));

							String feeItemBlock = "";
							if(!feeItems.isEmpty())
							{
								feeItemBlock = tpls.get(13);
								feeItemBlock = feeItemBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItems.get(0).getId()));
								feeItemBlock = feeItemBlock.replace("<?fee_count?>", Util.formatAdena(feeItems.get(0).getCount()));

								final String feeItemsBlockStr = tpls.get(14);
								StringBuilder feeItemsBlock = new StringBuilder();
								for(int i = 1; i < feeItems.size(); i++)
								{
									ItemData feeItem = feeItems.get(i);

									String tempBlock = feeItemsBlockStr;
									tempBlock = tempBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItem.getId()));
									tempBlock = tempBlock.replace("<?fee_count?>", Util.formatAdena(feeItem.getCount()));

									feeItemsBlock.append(tempBlock);
								}
								feeItemBlock = feeItemBlock.replace("<?fee_items?>", feeItemsBlock.toString());
							}
							infoBlock = infoBlock.replace("<?fees?>", feeItemBlock);

							content.append(infoBlock);
						}
						else if("buy".equals(cmd3))
						{
							if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player))
							{
								onWrongCondition(player);
								return;
							}

							if(!Config.PREMIUM_ACCOUNT_BASED_ON_GAMESERVER && AuthServerCommunication.getInstance().isShutdown())
								content.append(tpls.get(4));
							else
							{
								if(!st.hasMoreTokens())
									return;

								final int schemeId = Integer.parseInt(st.nextToken());
								final PremiumAccountTemplate paTemplate = PremiumAccountHolder.getInstance().getPremiumAccount(schemeId);
								if(paTemplate == null)
								{
									_log.warn(getClass().getSimpleName() + ": Error while open info about premium account scheme ID[" + schemeId + "]! Scheme is null.");
									return;
								}

								final int schemeDelay = Integer.parseInt(st.nextToken());

								List<ItemData> feeItems = paTemplate.getFeeItems(schemeDelay);
								if(feeItems == null)
									return;

								if(player.hasPremiumAccount() && player.getPremiumAccount() != paTemplate)
								{
									int premiumAccountExpire = player.getNetConnection().getPremiumAccountExpire();
									if(premiumAccountExpire != Integer.MAX_VALUE)
									{
										String expireBlock = tpls.get(5);
										expireBlock = expireBlock.replace("<?date_expire?>", TimeUtils.toSimpleFormat(premiumAccountExpire * 1000L));
										content.append(expireBlock);
									}
									else
										content.append(tpls.get(8));
								}
								else
								{
									boolean success = true;

									if(!feeItems.isEmpty())
									{
										for(ItemData feeItem : feeItems)
										{
											if(!ItemFunctions.haveItem(player, feeItem.getId(), feeItem.getCount()))
											{
												success = false;
												break;
											}
										}

										if(success)
										{
											for(ItemData feeItem : feeItems)
												ItemFunctions.deleteItem(player, feeItem.getId(), feeItem.getCount());
										}
										else
											content.append(tpls.get(7));
									}
		
									if(success)
									{
										if(player.givePremiumAccount(paTemplate, schemeDelay))
											player.broadcastPacket(new MagicSkillUse(player, player, 23128, 1, 1, 0));
										else
										{
											for(ItemData feeItem : feeItems)
												ItemFunctions.addItem(player, feeItem.getId(), feeItem.getCount());
										}

										IBbsHandler handler = BbsHandlerHolder.getInstance().getCommunityHandler("_cbbsservices_pa");
										if(handler != null)
											onBypassCommand(player, "_cbbsservices_pa");
										return;
									}
								}
							}
						}
					}
					else
					{
						if(player.hasPremiumAccount())
						{
							PremiumAccountTemplate paTemplate = player.getPremiumAccount();
							int premiumAccountExpire = player.getNetConnection().getPremiumAccountExpire();
							if(premiumAccountExpire != Integer.MAX_VALUE)
							{
								String expireBlock = tpls.get(15);
								expireBlock = expireBlock.replace("<?scheme_name?>", paTemplate.getName(player.getLanguage()));
								expireBlock = expireBlock.replace("<?date_expire?>", TimeUtils.toSimpleFormat(premiumAccountExpire * 1000L));
								content.append(expireBlock);
							}
							else
							{
								String expireBlock = tpls.get(16);
								expireBlock = expireBlock.replace("<?scheme_name?>", paTemplate.getName(player.getLanguage()));
								content.append(expireBlock);
							}
						}

						content.append(tpls.get(2));

						final String schemeButton = tpls.get(3);
						for(PremiumAccountTemplate paTemplate : PremiumAccountHolder.getInstance().getPremiumAccounts())
						{
							int type = paTemplate.getType();
							//會員類型4不顯示
							if (type == 4)
								continue;
							String name = paTemplate.getName(player.getLanguage());
							for(int delay : paTemplate.getFeeDelays())
							{
								String delayName = "";
								if(delay > 0)
								{
									int days = delay / 24;
									int hours = delay % 24;
									if(days > 0 && hours > 0)
									{
										delayName = tpls.get(11);
										delayName = delayName.replace("<?days?>", String.valueOf(days));
										delayName = delayName.replace("<?hours?>", String.valueOf(hours));
									}
									else if(days > 0)
									{
										delayName = tpls.get(10);
										delayName = delayName.replace("<?days?>", String.valueOf(days));
									}
									else if(hours > 0)
									{
										delayName = tpls.get(9);
										delayName = delayName.replace("<?hours?>", String.valueOf(hours));
									}
								}
								else
									delayName = tpls.get(12);

								String tempButton = schemeButton.replace("<?scheme_name?>", name);
								tempButton = tempButton.replace("<?delay_name?>", delayName);
								tempButton = tempButton.replace("<?scheme_id?>", String.valueOf(type));
								tempButton = tempButton.replace("<?scheme_delay?>", String.valueOf(delay));
								content.append(tempButton);
							}
						}
					}
				}
				else
					content.append(tpls.get(1));

				html = html.replace("<?content?>", content.toString());
			}
			else if("changename".equals(cmd2))
			{
				if(!st.hasMoreTokens())
					return;

				String cmd3 = st.nextToken();
				if("player".equals(cmd3))
				{
					final int feeItemId = BBSConfig.CHANGE_PLAYER_NAME_SERVICE_COST_ITEM_ID;
					final long feeItemCount = BBSConfig.CHANGE_PLAYER_NAME_SERVICE_COST_ITEM_COUNT;
					if(feeItemId == 0)
					{
						player.sendMessage(player.isLangRus() ? "功能禁用。" : "功能禁用。");
						player.sendPacket(ShowBoardPacket.CLOSE);
						return;
					}

					HtmTemplates tpls = HtmCache.getInstance().getTemplates("scripts/handler/bbs/pages/change_player_name.htm", player);
					html = tpls.get(0);

					StringBuilder content = new StringBuilder();
					if(!st.hasMoreTokens())
					{
						if(feeItemCount > 0)
						{
							String feeBlock = tpls.get(1);
							feeBlock = feeBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
							feeBlock = feeBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

							content.append(feeBlock);
						}
						else
							content.append(tpls.get(2));

						content.append(tpls.get(3));
					}
					else
					{
						if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player))
						{
							onWrongCondition(player);
							return;
						}

						String newPlayerName = st.nextToken();
						if(newPlayerName.charAt(0) == ' ')
							newPlayerName = newPlayerName.substring(1);

						if(player.getName().equals(newPlayerName))
							content.append(tpls.get(7));
						if(!Util.isMatchingRegexp(newPlayerName, Config.CNAME_TEMPLATE))
							content.append(tpls.get(5));
						else if(CharacterDAO.getInstance().getObjectIdByName(newPlayerName) > 0)
							content.append(tpls.get(6));
						else if(feeItemCount > 0 && !ItemFunctions.deleteItem(player, feeItemId, feeItemCount, true))
						{
							String noHaveItemBlock = tpls.get(4);
							noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
							noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

							content.append(noHaveItemBlock);
						}
						else
						{
							content.append(tpls.get(8).replace("<?player_name?>", newPlayerName));

							String oldName = player.getName();

							player.reName(newPlayerName, true);
							Log.add("Character " + oldName + " renamed to " + newPlayerName, "renames");
						}
					}
					html = html.replace("<?content?>", content.toString());
				}
				else if("pet".equals(cmd3))
				{
					final int feeItemId = BBSConfig.CHANGE_PET_NAME_SERVICE_COST_ITEM_ID;
					final long feeItemCount = BBSConfig.CHANGE_PET_NAME_SERVICE_COST_ITEM_COUNT;
					if(feeItemId == 0)
					{
						player.sendMessage(player.isLangRus() ? "功能禁用。" : "功能禁用。");
						player.sendPacket(ShowBoardPacket.CLOSE);
						return;
					}

					HtmTemplates tpls = HtmCache.getInstance().getTemplates("scripts/handler/bbs/pages/change_pet_name.htm", player);
					html = tpls.get(0);

					StringBuilder content = new StringBuilder();
					if(!st.hasMoreTokens())
					{
						if(feeItemCount > 0)
						{
							String feeBlock = tpls.get(1);
							feeBlock = feeBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
							feeBlock = feeBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

							content.append(feeBlock);
						}
						else
							content.append(tpls.get(2));

						content.append(tpls.get(3));
					}
					else
					{
						if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player))
						{
							onWrongCondition(player);
							return;
						}

						String newPetName = st.nextToken();
						if(newPetName.charAt(0) == ' ')
							newPetName = newPetName.substring(1);

						PetInstance pet = player.getPet();
						if(pet == null)
							content.append(tpls.get(8));
						else if(feeItemCount > 0 && pet.isDefaultName())
							content.append(tpls.get(7));
						else if(pet.getName().equals(newPetName))
							content.append(tpls.get(6));
						else if(newPetName.length() < 1 || newPetName.length() > 8)
							content.append(tpls.get(5));
						else if(feeItemCount > 0 && !ItemFunctions.deleteItem(player, feeItemId, feeItemCount, true))
						{
							String noHaveItemBlock = tpls.get(4);
							noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
							noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

							content.append(noHaveItemBlock);
						}
						else
						{
							content.append(tpls.get(9).replace("<?pet_name?>", newPetName));

							String oldName = pet.getName();

							pet.setName(newPetName);
							pet.broadcastCharInfo();
							pet.updateControlItem();
							Log.add("Pet " + oldName + " renamed to " + newPetName, "renames");
						}
					}
					html = html.replace("<?content?>", content.toString());
				}
				else if("clan".equals(cmd3))
				{
					final int feeItemId = BBSConfig.CHANGE_CLAN_NAME_SERVICE_COST_ITEM_ID;
					final long feeItemCount = BBSConfig.CHANGE_CLAN_NAME_SERVICE_COST_ITEM_COUNT;
					if(feeItemId == 0)
					{
						player.sendMessage(player.isLangRus() ? "功能禁用。" : "功能禁用。");
						player.sendPacket(ShowBoardPacket.CLOSE);
						return;
					}

					HtmTemplates tpls = HtmCache.getInstance().getTemplates("scripts/handler/bbs/pages/change_clan_name.htm", player);
					html = tpls.get(0);

					StringBuilder content = new StringBuilder();
					if(!st.hasMoreTokens())
					{
						if(feeItemCount > 0)
						{
							String feeBlock = tpls.get(1);
							feeBlock = feeBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
							feeBlock = feeBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

							content.append(feeBlock);
						}
						else
							content.append(tpls.get(2));

						content.append(tpls.get(3));
					}
					else
					{
						if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player))
						{
							onWrongCondition(player);
							return;
						}

						String newClanName = st.nextToken();
						if(newClanName.charAt(0) == ' ')
							newClanName = newClanName.substring(1);

						final Clan clan = player.getClan();
						if(clan == null)
							content.append(tpls.get(8));
						else if(!player.isClanLeader())
							content.append(tpls.get(9));
						else if(clan.getSubUnit(Clan.SUBUNIT_MAIN_CLAN).getName().equals(newClanName))
							content.append(tpls.get(6));
						else if(!Util.isMatchingRegexp(newClanName, Config.CLAN_NAME_TEMPLATE))
							content.append(tpls.get(5));
						else if(ClanTable.getInstance().getClanByName(newClanName) != null)
							content.append(tpls.get(7));
						else if(feeItemCount > 0 && !ItemFunctions.deleteItem(player, feeItemId, feeItemCount, true))
						{
							String noHaveItemBlock = tpls.get(4);
							noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
							noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

							content.append(noHaveItemBlock);
						}
						else
						{
							content.append(tpls.get(10).replace("<?clan_name?>", newClanName));

							String oldName = clan.getSubUnit(Clan.SUBUNIT_MAIN_CLAN).getName();

							clan.getSubUnit(Clan.SUBUNIT_MAIN_CLAN).setName(newClanName, true);
							clan.updateClanInDB();
							clan.broadcastClanStatus(true, true, true);
							player.broadcastUserInfo(true);
							Log.add("Clan " + oldName + " renamed to " + newClanName, "renames");
						}
					}
					html = html.replace("<?content?>", content.toString());
				}
			}
			else if("color".equals(cmd2))
			{
				if(!st.hasMoreTokens())
					return;

				String cmd3 = st.nextToken();
				if("name".equals(cmd3))
				{
					final int feeItemId = BBSConfig.COLOR_NAME_SERVICE_COST_ITEM_ID;
					final long feeItemCount = BBSConfig.COLOR_NAME_SERVICE_COST_ITEM_COUNT;
					final String[] availableColors = BBSConfig.COLOR_NAME_SERVICE_COLORS;
					if(feeItemId == 0 || availableColors.length == 0)
					{
						player.sendMessage(player.isLangRus() ? "功能禁用。" : "功能禁用。");
						player.sendPacket(ShowBoardPacket.CLOSE);
						return;
					}

					HtmTemplates tpls = HtmCache.getInstance().getTemplates("scripts/handler/bbs/pages/color_name_change.htm", player);
					html = tpls.get(0);

					StringBuilder content = new StringBuilder();
					if(!st.hasMoreTokens())
					{
						if(feeItemCount > 0)
						{
							String feeBlock = tpls.get(1);
							feeBlock = feeBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
							feeBlock = feeBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

							content.append(feeBlock);
						}
						else
							content.append(tpls.get(2));

						final String colorBlock = tpls.get(3).replace("<?player_name?>", player.getName());

						if(player.getNameColor() != Integer.decode("0xFFFFFF"))
							content.append(colorBlock.replace("<?color?>", "FFFFFF"));

						for(String color : availableColors)
						{
							String bgrColor = color.substring(4, 6) + color.substring(2, 4) + color.substring(0, 2);
							if(player.getNameColor() != Integer.decode("0x" + bgrColor))
								content.append(colorBlock.replace("<?color?>", color));
						}
					}
					else
					{
						if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player))
						{
							onWrongCondition(player);
							return;
						}

						final String newColor = st.nextToken().replace(" ", "");

						if(!newColor.equalsIgnoreCase("FFFFFF"))
						{
							boolean available = false;
							for(String color : availableColors)
							{
								if(color.equalsIgnoreCase(newColor))
								{
									available = true;
									break;
								}
							}

							if(!available)
							{
								player.sendPacket(ShowBoardPacket.CLOSE);
								return;
							}
						}

						final String bgrNewColor = newColor.substring(4, 6) + newColor.substring(2, 4) + newColor.substring(0, 2);
						final int newColorInt = Integer.decode("0x" + bgrNewColor);
						if(player.getNameColor() == newColorInt)
						{
							player.sendPacket(ShowBoardPacket.CLOSE);
							return;
						}

						if(feeItemCount > 0 && !ItemFunctions.deleteItem(player, feeItemId, feeItemCount, true))
						{
							String noHaveItemBlock = tpls.get(4);
							noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
							noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

							content.append(noHaveItemBlock);
						}
						else
						{
							content.append(tpls.get(5).replace("<?color?>", newColor).replace("<?player_name?>", player.getName()));

							player.setNameColor(newColorInt);
							player.broadcastUserInfo(true);
							Log.add("Player " + player.getName() + " changed name color to " + newColor, "changecolor");
						}
					}
					html = html.replace("<?content?>", content.toString());
				}
				else if("title".equals(cmd3))
				{
					final int feeItemId = BBSConfig.COLOR_TITLE_SERVICE_COST_ITEM_ID;
					final long feeItemCount = BBSConfig.COLOR_TITLE_SERVICE_COST_ITEM_COUNT;
					final String[] availableColors = BBSConfig.COLOR_TITLE_SERVICE_COLORS;
					if(feeItemId == 0 || availableColors.length == 0)
					{
						player.sendMessage(player.isLangRus() ? "功能禁用。" : "功能禁用。");
						player.sendPacket(ShowBoardPacket.CLOSE);
						return;
					}

					HtmTemplates tpls = HtmCache.getInstance().getTemplates("scripts/handler/bbs/pages/color_title_change.htm", player);
					html = tpls.get(0);

					StringBuilder content = new StringBuilder();
					if(!st.hasMoreTokens())
					{
						if(feeItemCount > 0)
						{
							String feeBlock = tpls.get(1);
							feeBlock = feeBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
							feeBlock = feeBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

							content.append(feeBlock);
						}
						else
							content.append(tpls.get(2));

						final String colorBlock = tpls.get(3).replace("<?player_name?>", player.getName());

						if(player.getTitleColor() != Integer.decode("0xFFFF77"))
							content.append(colorBlock.replace("<?color?>", "77FFFF"));

						for(String color : availableColors)
						{
							String bgrColor = color.substring(4, 6) + color.substring(2, 4) + color.substring(0, 2);
							if(player.getTitleColor() != Integer.decode("0x" + bgrColor))
								content.append(colorBlock.replace("<?color?>", color));
						}
					}
					else
					{
						if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player))
						{
							onWrongCondition(player);
							return;
						}

						final String newColor = st.nextToken().replace(" ", "");

						if(!newColor.equalsIgnoreCase("77FFFF"))
						{
							boolean available = false;
							for(String color : availableColors)
							{
								if(color.equalsIgnoreCase(newColor))
								{
									available = true;
									break;
								}
							}

							if(!available)
							{
								player.sendPacket(ShowBoardPacket.CLOSE);
								return;
							}
						}

						final String bgrNewColor = newColor.substring(4, 6) + newColor.substring(2, 4) + newColor.substring(0, 2);
						final int newColorInt = Integer.decode("0x" + bgrNewColor);
						if(player.getTitleColor() == newColorInt)
						{
							player.sendPacket(ShowBoardPacket.CLOSE);
							return;
						}

						if(feeItemCount > 0 && !ItemFunctions.deleteItem(player, feeItemId, feeItemCount, true))
						{
							String noHaveItemBlock = tpls.get(4);
							noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
							noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

							content.append(noHaveItemBlock);
						}
						else
						{
							content.append(tpls.get(5).replace("<?color?>", newColor).replace("<?player_name?>", player.getName()));

							player.setTitleColor(newColorInt);
							player.broadcastUserInfo(true);
							Log.add("Player " + player.getName() + " changed title color to " + newColor, "changecolor");
						}
					}
					html = html.replace("<?content?>", content.toString());
				}
			}
			else if("sex".equals(cmd2))
			{
				final int feeItemId = BBSConfig.CHANGE_SEX_SERVICE_COST_ITEM_ID;
				final long feeItemCount = BBSConfig.CHANGE_SEX_SERVICE_COST_ITEM_COUNT;
				if(feeItemId == 0)
				{
					player.sendMessage(player.isLangRus() ? "功能禁用。" : "功能禁用。");
					player.sendPacket(ShowBoardPacket.CLOSE);
					return;
				}

				HtmTemplates tpls = HtmCache.getInstance().getTemplates("scripts/handler/bbs/pages/change_sex.htm", player);
				html = tpls.get(0);

				StringBuilder content = new StringBuilder();
				if(!st.hasMoreTokens())
				{
					if(feeItemCount > 0)
					{
						String feeBlock = tpls.get(1);
						feeBlock = feeBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
						feeBlock = feeBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

						content.append(feeBlock);
					}
					else
						content.append(tpls.get(2));

					content.append(tpls.get(3));
				}
				else
				{
					String cmd3 = st.nextToken();
					if("change".equals(cmd3))
					{
						if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player))
						{
							onWrongCondition(player);
							return;
						}

						if(feeItemCount > 0 && !ItemFunctions.deleteItem(player, feeItemId, feeItemCount, true))
						{
							String noHaveItemBlock = tpls.get(4);
							noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
							noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

							content.append(noHaveItemBlock);
						}
						else
						{
							content.append(tpls.get(5).replace("<?player_name?>", player.getName()));

							player.changeSex();
							player.broadcastUserInfo(true);
							player.broadcastPacket(new MagicSkillUse(player, player, 23128, 1, 1, 0));
							Log.add("Player " + player.getName() + " changed sex to : " + player.getSex(), "changesex");
						}
					}
				}
				html = html.replace("<?content?>", content.toString());
			}
			else if("expand".equals(cmd2))
			{
				if(!st.hasMoreTokens())
					return;

				String cmd3 = st.nextToken();
				if("inventory".equals(cmd3))
				{
					final int feeItemId = BBSConfig.EXPAND_INVENTORY_SERVICE_COST_ITEM_ID;
					final long feeItemCount = BBSConfig.EXPAND_INVENTORY_SERVICE_COST_ITEM_COUNT;
					if(feeItemId == 0)
					{
						player.sendMessage(player.isLangRus() ? "功能禁用。" : "功能禁用。");
						player.sendPacket(ShowBoardPacket.CLOSE);
						return;
					}

					// TODO: Вынести конфиг в конфиги коммунити.
					if(Config.SERVICES_EXPAND_INVENTORY_MAX <= player.getExpandInventory())//修復擴充問題
					{
						player.sendMessage(player.isLangRus() ? "擴充到最大值，無法再擴充。" : "扩充到最大值，无法再扩充。");
						player.sendPacket(ShowBoardPacket.CLOSE);
						return;
					}

					HtmTemplates tpls = HtmCache.getInstance().getTemplates("scripts/handler/bbs/pages/expand_inventory.htm", player);
					html = tpls.get(0);

					StringBuilder content = new StringBuilder();
					if(!st.hasMoreTokens())
					{
						if(feeItemCount > 0)
						{
							String feeBlock = tpls.get(1);
							feeBlock = feeBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
							feeBlock = feeBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

							content.append(feeBlock);
						}
						else
							content.append(tpls.get(2));

						content.append(tpls.get(3));
					}
					else
					{
						if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player))
						{
							onWrongCondition(player);
							return;
						}

						int count = Integer.parseInt(st.nextToken());
						if(count == 0)
							return;

						long price = feeItemCount * count;
						if(price > 0 && !ItemFunctions.deleteItem(player, feeItemId, price, true))
						{
							String noHaveItemBlock = tpls.get(4);
							noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_count?>", Util.formatAdena(price));
							noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

							content.append(noHaveItemBlock);
						}
						else
						{
							content.append(tpls.get(5).replace("<?player_name?>", player.getName()).replace("<?expand_count?>", String.valueOf(count)));

							player.setExpandInventory(player.getExpandInventory() + count);
							player.setVar("ExpandInventory", String.valueOf(player.getExpandInventory()), -1);
							player.sendPacket(new ExStorageMaxCountPacket(player));
							Log.add("Player " + player.getName() + " expand inventory", "expandinventory");
						}
					}
					html = html.replace("<?content?>", content.toString());
				}
				else if("warehouse".equals(cmd3))
				{
					final int feeItemId = BBSConfig.EXPAND_WAREHOUSE_SERVICE_COST_ITEM_ID;
					final long feeItemCount = BBSConfig.EXPAND_WAREHOUSE_SERVICE_COST_ITEM_COUNT;
					if(feeItemId == 0)
					{
						player.sendMessage(player.isLangRus() ? "功能禁用。" : "功能禁用。");
						player.sendPacket(ShowBoardPacket.CLOSE);
						return;
					}

					HtmTemplates tpls = HtmCache.getInstance().getTemplates("scripts/handler/bbs/pages/expand_warehouse.htm", player);
					html = tpls.get(0);

					StringBuilder content = new StringBuilder();
					if(!st.hasMoreTokens())
					{
						if(feeItemCount > 0)
						{
							String feeBlock = tpls.get(1);
							feeBlock = feeBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
							feeBlock = feeBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

							content.append(feeBlock);
						}
						else
							content.append(tpls.get(2));

						content.append(tpls.get(3));
					}
					else
					{
						if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player))
						{
							onWrongCondition(player);
							return;
						}

						int count = Integer.parseInt(st.nextToken());
						if(count == 0)
							return;

						long price = feeItemCount * count;
						if(price > 0 && !ItemFunctions.deleteItem(player, feeItemId, price, true))
						{
							String noHaveItemBlock = tpls.get(4);
							noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_count?>", Util.formatAdena(price));
							noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

							content.append(noHaveItemBlock);
						}
						else
						{
							content.append(tpls.get(5).replace("<?player_name?>", player.getName()).replace("<?expand_count?>", String.valueOf(count)));

							player.setExpandWarehouse(player.getExpandWarehouse() + count);
							player.setVar("ExpandWarehouse", String.valueOf(player.getExpandWarehouse()), -1);
							player.sendPacket(new ExStorageMaxCountPacket(player));
							Log.add("Player " + player.getName() + " expand warehouse", "expandwarehouse");
						}
					}
					html = html.replace("<?content?>", content.toString());
				}
				else if("clanwarehouse".equals(cmd3))
				{
					final int feeItemId = BBSConfig.EXPAND_CLANWAREHOUSE_SERVICE_COST_ITEM_ID;
					final long feeItemCount = BBSConfig.EXPAND_CLANWAREHOUSE_SERVICE_COST_ITEM_COUNT;
					if(feeItemId == 0)
					{
						player.sendMessage(player.isLangRus() ? "功能禁用。" : "功能禁用。");
						player.sendPacket(ShowBoardPacket.CLOSE);
						return;
					}

					Clan clan = player.getClan();
					if(clan == null)
					{
						player.sendMessage(player.isLangRus() ? "您不是血盟成員。" : "您不是血盟成员。");
						player.sendPacket(ShowBoardPacket.CLOSE);
						return;
					}

					HtmTemplates tpls = HtmCache.getInstance().getTemplates("scripts/handler/bbs/pages/expand_clanwarehouse.htm", player);
					html = tpls.get(0);

					StringBuilder content = new StringBuilder();
					if(!st.hasMoreTokens())
					{
						if(feeItemCount > 0)
						{
							String feeBlock = tpls.get(1);
							feeBlock = feeBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
							feeBlock = feeBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

							content.append(feeBlock);
						}
						else
							content.append(tpls.get(2));

						content.append(tpls.get(3));
					}
					else
					{
						if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player))
						{
							onWrongCondition(player);
							return;
						}

						int count = Integer.parseInt(st.nextToken());
						if(count == 0)
							return;

						long price = feeItemCount * count;
						if(price > 0 && !ItemFunctions.deleteItem(player, feeItemId, price, true))
						{
							String noHaveItemBlock = tpls.get(4);
							noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_count?>", Util.formatAdena(price));
							noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

							content.append(noHaveItemBlock);
						}
						else
						{
							content.append(tpls.get(5).replace("<?player_name?>", player.getName()).replace("<?expand_count?>", String.valueOf(count)));

							clan.setWhBonus(player.getClan().getWhBonus() + count);
							player.sendPacket(new ExStorageMaxCountPacket(player));
							Log.add("Player " + player.getName() + " expand clan warehouse", "expandclanwarehouse");
						}
					}
					html = html.replace("<?content?>", content.toString());
				}
			}
			else if("clanrep".equals(cmd2))
			{
				final long[][] pricesList = BBSConfig.CLAN_REPUTATION_SERVICE_PRICES_LIST;
				if(pricesList.length == 0)
				{
					player.sendMessage(player.isLangRus() ? "功能禁用。" : "功能禁用。");
					player.sendPacket(ShowBoardPacket.CLOSE);
					return;
				}

				Clan clan = player.getClan();
				if(clan == null)
				{
					player.sendMessage(player.isLangRus() ? "您不是血盟成員。" : "您不是血盟成员。");
					player.sendPacket(ShowBoardPacket.CLOSE);
					return;
				}

				if(clan.getLevel() < 3)//修改 佈告欄-服務-血盟聲望 只需要3級就可以提升(血盟技能3級開始學習)
				{
					player.sendMessage(player.isLangRus() ? "您的血盟等級不符合，此功能需要血盟等級「3」。" : "您的血盟等级不符合，此功能需要血盟等级「3」。");
					player.sendPacket(ShowBoardPacket.CLOSE);
					return;
				}

				HtmTemplates tpls = HtmCache.getInstance().getTemplates("scripts/handler/bbs/pages/clanrep.htm", player);
				html = tpls.get(0);

				StringBuilder content = new StringBuilder();
				if(!st.hasMoreTokens())
				{
					for(int i = 0; i < pricesList.length; i++)
					{
						int feeItemId = (int) pricesList[i][0];
						long feeItemCount = pricesList[i][1];
						int reputationCount = (int) pricesList[i][2];
						if(feeItemCount > 0)
						{
							String feeBlock = tpls.get(1);
							feeBlock = feeBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
							feeBlock = feeBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

							content.append(feeBlock);
						}
						else
							content.append(tpls.get(2));

						String buttonBlock = tpls.get(3);
						buttonBlock = buttonBlock.replace("<?reputation_count?>", String.valueOf(reputationCount));
						buttonBlock = buttonBlock.replace("<?reputation_id?>", String.valueOf(i));

						content.append(buttonBlock);
					}
				}
				else
				{
					if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player))
					{
						onWrongCondition(player);
						return;
					}

					int id = Integer.parseInt(st.nextToken());
					if(id < 0 || id >= pricesList.length)
						return;

					int feeItemId = (int) pricesList[id][0];
					long feeItemCount = pricesList[id][1];
					int reputationCount = (int) pricesList[id][2];
					if(feeItemCount > 0 && !ItemFunctions.deleteItem(player, feeItemId, feeItemCount, true))
					{
						String noHaveItemBlock = tpls.get(4);
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

						content.append(noHaveItemBlock);
					}
					else
					{
						content.append(tpls.get(5).replace("<?player_name?>", player.getName()).replace("<?reputation_count?>", String.valueOf(reputationCount)));

						clan.incReputation(reputationCount, false, "CustomBBBServices");
						Log.add("Player " + player.getName() + " purchase clan reputation", "clanreputation");
					}
				}
				html = html.replace("<?content?>", content.toString());
			}
			else if("tvtrelax".equals(cmd2))//一般的
			{
				PvPEvent event = EventHolder.getInstance().getEvent(EventType.CUSTOM_PVP_EVENT, 2);
				if(event != null && event.isRegActive())
				{
					event.reg(player);
				}
			}
			else if("tvthard".equals(cmd2))//難的
			{
				PvPEvent event = EventHolder.getInstance().getEvent(EventType.CUSTOM_PVP_EVENT, 3);
				if(event != null && event.isRegActive())
				{
					event.reg(player);
				}
			}
			else if("hasherrelax".equals(cmd2))//死战一般的
			{
				PvPEvent event = EventHolder.getInstance().getEvent(EventType.CUSTOM_PVP_EVENT, 4);
				if(event != null && event.isRegActive())
				{
					event.reg(player);
				}
			}
			else if("hasherhard".equals(cmd2))//死战難的
			{
				PvPEvent event = EventHolder.getInstance().getEvent(EventType.CUSTOM_PVP_EVENT, 5);
				if(event != null && event.isRegActive())
				{
					event.reg(player);
				}
			}
			else if("karma".equals(cmd2))
			{
				final int feeItemId = BBSConfig.KARMA_PK_SERVICE_COST_ITEM_ID;
				final long feeItemCount = BBSConfig.KARMA_PK_SERVICE_COST_ITEM_COUNT;
				if(feeItemId == 0)
				{
					player.sendMessage(player.isLangRus() ? "功能禁用。" : "功能禁用。");
					player.sendPacket(ShowBoardPacket.CLOSE);
					return;
				}

				if(player.getKarma() >= 0 && player.getPkKills() == 0)
				{
					player.sendMessage(player.isLangRus() ? "您的性向為0，請保持。" : "您的性向为0，请保持。");
					player.sendPacket(ShowBoardPacket.CLOSE);
					return;
				}

				HtmTemplates tpls = HtmCache.getInstance().getTemplates("scripts/handler/bbs/pages/karma_pk.htm", player);
				html = tpls.get(0);

				StringBuilder content = new StringBuilder();
				if(!st.hasMoreTokens())
				{
					if(player.getKarma() < 0)
					{
						if(feeItemCount > 0)
						{
							String feeBlock = tpls.get(1);
							feeBlock = feeBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
							feeBlock = feeBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

							content.append(feeBlock);
						}
						else
							content.append(tpls.get(2));

						content.append(tpls.get(5));
					}
					else if(player.getPkKills() > 0)
					{
						if(feeItemCount > 0)
						{
							String feeBlock = tpls.get(3);
							feeBlock = feeBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
							feeBlock = feeBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

							content.append(feeBlock);
						}
						else
							content.append(tpls.get(4));

						content.append(tpls.get(6));
					}
				}
				else
				{
					String cmd3 = st.nextToken();
					if("clear".equals(cmd3))
					{
						if(!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player))
						{
							onWrongCondition(player);
							return;
						}

						if(feeItemCount > 0 && !ItemFunctions.deleteItem(player, feeItemId, feeItemCount, true))
						{
							String noHaveItemBlock = tpls.get(7);
							noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
							noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

							content.append(noHaveItemBlock);
						}
						else
						{
							if(player.getKarma() < 0)
							{
								content.append(tpls.get(8).replace("<?player_name?>", player.getName()));
								player.setKarma(0);
							}
							else if(player.getPkKills() > 0)
							{
								content.append(tpls.get(9).replace("<?player_name?>", player.getName()));
								player.setPkKills(0);
							}

							player.broadcastUserInfo(true);
							player.broadcastPacket(new MagicSkillUse(player, player, 23128, 1, 1, 0));
						}
					}
				}
				html = html.replace("<?content?>", content.toString());
			}
		}
		ShowBoardPacket.separateAndSend(html, player);
	}

	
	private boolean CheckCompensateAccount(Player player,String eventName)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		boolean go = false ;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT obj_name FROM _give_item_by_account WHERE `account` = ? and eventName = ? ");
			statement.setString(1, player.getAccountName());
			statement.setString(2, eventName);
			rset = statement.executeQuery();
			if(rset.next())
				go = true;
			statement.close();
		}
		catch(Exception e)
		{
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return go;
	}
	
	private boolean AddCompensateAccount(Player player,String eventName)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		boolean go = true;//1表示成功
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO _give_item_by_account(account, obj_name, eventName, post_date) VALUES (?,?,?,?)");
			statement.setString(1, player.getAccountName());
			statement.setString(2, player.getName());
			statement.setString(3, eventName);
			statement.setInt(4, (int) (System.currentTimeMillis() / 1000));
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			go = false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return go;
	}
	
	
	
	
	
	@Override
	protected void doWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{
		//
	}

	private static String doubleToString(double value)
	{
		int intValue = (int) value;
		if(intValue == value)
			return String.valueOf(intValue);
		return String.valueOf(value);
	}
}