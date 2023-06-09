package npc.model;


import handler.bbs.custom.BBSConfig;
import handler.bbs.custom.communitybuffer.BuffSkill;
import l2s.commons.collections.MultiValueSet;
import l2s.commons.dbutils.DbUtils;
import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.dao.CharacterDAO;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.data.htm.HtmTemplates;
import l2s.gameserver.data.xml.holder.PremiumAccountHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.actor.instances.player.Henna;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.authcomm.AuthServerCommunication;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.components.HtmlMessage;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExStorageMaxCountPacket;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.network.l2.s2c.ShowBoardPacket;
import l2s.gameserver.network.l2.s2c.ShowPCCafeCouponShowUI;
import l2s.gameserver.skills.SkillEntry;
import l2s.gameserver.skills.SkillEntryType;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.templates.PremiumAccountTemplate;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.data.ItemData;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;
/**
 * NPC服务
 **/
public class MemberNPCInstance extends NpcInstance
{
	private static final Logger _log = LoggerFactory.getLogger(MemberNPCInstance.class);
	
	static final int[][] GIveEveryDayItemId = 
		{
			// 第一個是幣數量  第二個是物品及數量  第三個是等級限制 第四個是購買次數 輸入999 表示可買999 也等於無限次數了
			{29001,1},
		};
	private static Long[] Slot= {
			ItemTemplate.SLOT_R_EAR,//右耳
			ItemTemplate.SLOT_L_EAR,//左耳
			ItemTemplate.SLOT_NECK,//项练
			ItemTemplate.SLOT_R_FINGER,//右手戒
			ItemTemplate.SLOT_L_FINGER,//左手戒
			ItemTemplate.SLOT_HEAD,//头
			ItemTemplate.SLOT_R_HAND,//右手
			ItemTemplate.SLOT_L_HAND,//左手
			ItemTemplate.SLOT_GLOVES,//手套
			ItemTemplate.SLOT_CHEST,//胸
			ItemTemplate.SLOT_LEGS,//下护具
			ItemTemplate.SLOT_FEET,//脚
			ItemTemplate.SLOT_BACK,//披风
			ItemTemplate.SLOT_R_BRACELET,//右手镯
			ItemTemplate.SLOT_L_BRACELET,//左手镯
			ItemTemplate.SLOT_HAIR,//头饰
			ItemTemplate.SLOT_HAIRALL,//头饰
			ItemTemplate.SLOT_BROOCH,//胸针
			ItemTemplate.SLOT_PENDANT,//坠饰
			ItemTemplate.SLOT_BELT,//腰带
	};
	public static int[] skillReserve = {
			11400,11401,11402,11403,11404,51275,51276,51277,51278,51279,51280,51281,51271,51272,51273,1405,51271,51252,51251,51250,51249,51248,5124
	};
	public MemberNPCInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
	{
		super(objectId, template, set);
	}
	@Override
	public void showChatWindow(Player player, int val, boolean firstTalk, Object... replace)
	{
		//這一區是第一次對話時該出現的對話框區域
		if(val == 0)
		{
			showChatWindow(player, "member/" + getNpcId() + ".htm", firstTalk);
		}
		else
		{
			showChatWindow(player, "member/" + getNpcId() + "-" + val + ".htm", firstTalk);
		}
		
	}
	@Override
	public void onBypassFeedback(Player player, String command) {
		final String[] buypassOptions = command.split(" ");
		String html = "";
		//Announcements.announceToAll("command:" + command);
		if (command.startsWith("Chat")) {
			//bypass -h npc_%objectId%_Chat 1
			showChatWindow(player, "member/" + getNpcId() + "-" + buypassOptions[1] + ".htm", false);
		}
		//bypass -h npc?BuffMagic
		else if (buypassOptions[0].equals("BuffMagic")) {
			int Pages = 1;
			if (buypassOptions.length == 2) {
				Pages = Integer.parseInt(buypassOptions[1]);
			}
		} else if (buypassOptions[0].equals("GetClanGift")) {
			showPage(player, "4362-3.htm");
		}
		//bypass -h npc?GiveBuffToPlayer gm deletetime
		else if (buypassOptions[0].equals("GiveBuffToPlayer")) {
			String name = buypassOptions[1];
			int times = Integer.parseInt(buypassOptions[2]);
			if (!checkBuffAndGive(player, name, times)) {
				player.sendMessage("出了一些問題。");
			}
			showPage(player, "4362-3.htm");
		} else if (buypassOptions[0].equals("UpgradeLoad")) {
			final int feeItemId = 88888;
			final long feeItemCount = 1000;
			final String[] availableColors = BBSConfig.COLOR_TITLE_SERVICE_COLORS;
			if (feeItemId == 0 || availableColors.length == 0) {
				player.sendMessage(player.isLangRus() ? "功能禁用。" : "功能禁用。");
				player.sendPacket(ShowBoardPacket.CLOSE);
				return;
			}

			HtmTemplates tpls = HtmCache.getInstance().getTemplates("member/upgrade_load.htm", player);
			html = tpls.get(0);

			StringBuilder content = new StringBuilder();
			if (buypassOptions.length == 1) {
				if (feeItemCount > 0) {
					String feeBlock = tpls.get(1);
					feeBlock = feeBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
					feeBlock = feeBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

					content.append(feeBlock);
				} else
					content.append(tpls.get(2));

				content.append(tpls.get(3));
			} else {
				if (!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player)) {
					onWrongCondition(player);
					return;
				}

				// 這裡需要判定玩家目前等級
				int level = Math.max(player.getSkillLevel(46003), 0);
				if (level < 10) {
					if (feeItemCount > 0 && !ItemFunctions.deleteItem(player, feeItemId, feeItemCount, true)) {
						String noHaveItemBlock = tpls.get(4);
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

						content.append(noHaveItemBlock);
					} else {
						//這裡要提升下一個等級寫法
						content.append(tpls.get(5));
						if (level > 0) {
							player.removeSkill(46003, true);//先刪除技能等級
						}

						SkillEntry skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, 46003, level + 1);

						player.addSkill(skillEntry, true);
						player.sendSkillList();
						//player.getPlayer().sendPacket(new SystemMessagePacket(SystemMsg.YOU_HAVE_EARNED_S1_SKILL).addSkillName(skillEntry.getId(), skillEntry.getLevel()));
						player.broadcastUserInfo(true);
						Log.add("玩家 " + player.getName() + " 增加負重成功  " + level + " >> " + (level + 1), "負重");
					}

				} else {
					content.append(tpls.get(6));
				}
			}
			html = html.replace("<?content?>", content.toString());
			sendHtmlMessage(player, html);
		} else if (buypassOptions[0].equals("pccoupon")) {
			player.sendPacket(ShowBoardPacket.CLOSE);
			player.sendPacket(ShowPCCafeCouponShowUI.STATIC);
			return;
		} else if (buypassOptions[0].equals("GetMemberCoin")) {
			int memberCoins = CheckMemberHaveConis(player);
			HtmTemplates tpls = HtmCache.getInstance().getTemplates("member/GetMemberCoin.htm", player);
			html = tpls.get(0);

			if (buypassOptions.length == 2) {
				int getCoins = Integer.parseInt(buypassOptions[1]);
				;
				if (getCoins > memberCoins) {
					String tmp = tpls.get(3);//輸入超出數額
					html = html.replace("<?content?>", tmp);
					sendHtmlMessage(player, html);
					return;
				}
				if (UpdateMemberConis(player, -getCoins)) {
					ItemFunctions.addItem(player, 88888, getCoins, true);
				}
				memberCoins = memberCoins - getCoins;
			}

			if (memberCoins == 0)//顯示沒有贊助或領取完成
			{
				String tmp = tpls.get(2);
				html = html.replace("<?content?>", tmp);
				sendHtmlMessage(player, html);
				return;
			}
			String tmp = tpls.get(1);//顯示會員幣數量
			tmp = tmp.replace("<$memberCoins$>", memberCoins + "");
			html = html.replace("<?content?>", tmp);
			sendHtmlMessage(player, html);
		} else if (buypassOptions[0].equals("getEveryDay")) {
			if (player.hasPremiumAccount()) {
				if (getToday(player)) {
					for (int i = 0; i < GIveEveryDayItemId.length; i++) {
						ItemFunctions.addItem(player, GIveEveryDayItemId[i][0], GIveEveryDayItemId[i][1], true);
					}
					updateGiveOk(player);
				} else {
					player.sendMessage("尊敬的會員，您今日已領取過啦。");
				}
			}
		} else if (buypassOptions[0].equals("premiumAccount")) {
			HtmTemplates tpls = HtmCache.getInstance().getTemplates("member/premiumAccount.htm", player);
			html = tpls.get(0);

			StringBuilder content = new StringBuilder();

			if (Config.PREMIUM_ACCOUNT_ENABLED) {
				if (buypassOptions.length > 1) {
					String cmd3 = buypassOptions[1];
					if ("info".equals(cmd3)) {
						if (buypassOptions.length < 2)
							return;

						final int schemeId = Integer.parseInt(buypassOptions[2]);

						final PremiumAccountTemplate paTemplate = PremiumAccountHolder.getInstance().getPremiumAccount(schemeId);
						if (paTemplate == null) {
							_log.warn(getClass().getSimpleName() + ": Error while open info about premium account scheme ID[" + schemeId + "]! Scheme is null.");
							return;
						}

						final int schemeDelay = Integer.parseInt(buypassOptions[3]);

						List<ItemData> feeItems = paTemplate.getFeeItems(schemeDelay);
						if (feeItems == null)
							return;

						String delayName = "";
						if (schemeDelay > 0) {
							int days = schemeDelay / 24;
							int hours = schemeDelay % 24;
							if (days > 0 && hours > 0) {
								delayName = tpls.get(11);
								delayName = delayName.replace("<?days?>", String.valueOf(days));
								delayName = delayName.replace("<?hours?>", String.valueOf(hours));
							} else if (days > 0) {
								delayName = tpls.get(10);
								delayName = delayName.replace("<?days?>", String.valueOf(days));
							} else if (hours > 0) {
								delayName = tpls.get(9);
								delayName = delayName.replace("<?hours?>", String.valueOf(hours));
							}
						} else
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
						if (!feeItems.isEmpty()) {
							feeItemBlock = tpls.get(13);
							feeItemBlock = feeItemBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItems.get(0).getId()));
							feeItemBlock = feeItemBlock.replace("<?fee_count?>", Util.formatAdena(feeItems.get(0).getCount()));

							final String feeItemsBlockStr = tpls.get(14);
							StringBuilder feeItemsBlock = new StringBuilder();
							for (int i = 1; i < feeItems.size(); i++) {
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
					} else if ("buy".equals(cmd3)) {
						if (!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player)) {
							onWrongCondition(player);
							return;
						}

						if (!Config.PREMIUM_ACCOUNT_BASED_ON_GAMESERVER && AuthServerCommunication.getInstance().isShutdown())
							content.append(tpls.get(4));
						else {
							if (buypassOptions.length < 2)
								return;

							final int schemeId = Integer.parseInt(buypassOptions[2]);
							final PremiumAccountTemplate paTemplate = PremiumAccountHolder.getInstance().getPremiumAccount(schemeId);
							if (paTemplate == null) {
								_log.warn(getClass().getSimpleName() + ": Error while open info about premium account scheme ID[" + schemeId + "]! Scheme is null.");
								return;
							}

							final int schemeDelay = Integer.parseInt(buypassOptions[3]);

							List<ItemData> feeItems = paTemplate.getFeeItems(schemeDelay);
							if (feeItems == null)
								return;

							if (player.hasPremiumAccount() && player.getPremiumAccount() != paTemplate) {
								int premiumAccountExpire = player.getNetConnection().getPremiumAccountExpire();
								if (premiumAccountExpire != Integer.MAX_VALUE) {
									String expireBlock = tpls.get(5);
									expireBlock = expireBlock.replace("<?date_expire?>", TimeUtils.toSimpleFormat(premiumAccountExpire * 1000L));
									content.append(expireBlock);
								} else
									content.append(tpls.get(8));
							} else {
								boolean success = true;

								if (!feeItems.isEmpty()) {
									for (ItemData feeItem : feeItems) {
										if (!ItemFunctions.haveItem(player, feeItem.getId(), feeItem.getCount())) {
											success = false;
											break;
										}
									}

									if (success) {
										for (ItemData feeItem : feeItems)
											ItemFunctions.deleteItem(player, feeItem.getId(), feeItem.getCount());
									} else
										content.append(tpls.get(7));
								}

								if (success) {
									if (player.givePremiumAccount(paTemplate, schemeDelay)) {
										player.broadcastPacket(new MagicSkillUse(player, player, 23128, 1, 1, 0));
									} else {
										for (ItemData feeItem : feeItems)
											ItemFunctions.addItem(player, feeItem.getId(), feeItem.getCount());
									}

									//IBbsHandler handler = BbsHandlerHolder.getInstance().getCommunityHandler("_cbbsservices_pa");
									//if(handler != null)
									//onBypassCommand(player, "_cbbsservices_pa");
									return;
								}
							}
						}
					}
				} else {
					if (player.hasPremiumAccount()) {
						PremiumAccountTemplate paTemplate = player.getPremiumAccount();
						int premiumAccountExpire = player.getNetConnection().getPremiumAccountExpire();
						if (premiumAccountExpire != Integer.MAX_VALUE) {
							String expireBlock = tpls.get(15);
							expireBlock = expireBlock.replace("<?scheme_name?>", paTemplate.getName(player.getLanguage()));
							expireBlock = expireBlock.replace("<?date_expire?>", TimeUtils.toSimpleFormat(premiumAccountExpire * 1000L));
							content.append(expireBlock);
						} else {
							String expireBlock = tpls.get(16);
							expireBlock = expireBlock.replace("<?scheme_name?>", paTemplate.getName(player.getLanguage()));
							content.append(expireBlock);
						}
					}

					content.append(tpls.get(2));

					String schemeButton = tpls.get(3);
					//領取每日獎勵<?S1?>         bypass -h npc?getEveryDay <?S2?>
					if (player.hasPremiumAccount()) {
						if (getToday(player)) {
							schemeButton = schemeButton.replace("<?S1?>", "领取每日宝箱");
							schemeButton = schemeButton.replace("<?S2?>", "bypass -h npc?getEveryDay");
						} else {
							schemeButton = schemeButton.replace("<?S1?>", "今日已领取");
							schemeButton = schemeButton.replace("<?S2?>", "");
						}
					} else {
						schemeButton = schemeButton.replace("<?S1?>", "会员可领取每日宝箱");
						schemeButton = schemeButton.replace("<?S2?>", "");
					}
					for (PremiumAccountTemplate paTemplate : PremiumAccountHolder.getInstance().getPremiumAccounts()) {
						int type = paTemplate.getType();
						if (type == 4)//會員類型4不顯示
							continue;
						String name = paTemplate.getName(player.getLanguage());
						for (int delay : paTemplate.getFeeDelays()) {
							String delayName = "";
							if (delay > 0) {
								int days = delay / 24;
								int hours = delay % 24;
								if (days > 0 && hours > 0) {
									delayName = tpls.get(11);
									delayName = delayName.replace("<?days?>", String.valueOf(days));
									delayName = delayName.replace("<?hours?>", String.valueOf(hours));
								} else if (days > 0) {
									delayName = tpls.get(10);
									delayName = delayName.replace("<?days?>", String.valueOf(days));
								} else if (hours > 0) {
									delayName = tpls.get(9);
									delayName = delayName.replace("<?hours?>", String.valueOf(hours));
								}
							} else
								delayName = tpls.get(12);

							String tempButton = schemeButton.replace("<?scheme_name?>", name);
							tempButton = tempButton.replace("<?delay_name?>", delayName);
							tempButton = tempButton.replace("<?scheme_id?>", String.valueOf(type));
							tempButton = tempButton.replace("<?scheme_delay?>", String.valueOf(delay));
							content.append(tempButton);
						}
					}
				}
			} else
				content.append(tpls.get(1));

			html = html.replace("<?content?>", content.toString());
			sendHtmlMessage(player, html);
		}
		//ChangeHtml
		else if (buypassOptions[0].equals("ChangeHtml")) {
			showPage(player, buypassOptions[1]);
		} else if (buypassOptions[0].equals("changename")) {
			if ("player".equals(buypassOptions[1])) {
				final int feeItemId = BBSConfig.CHANGE_PLAYER_NAME_SERVICE_COST_ITEM_ID;
				final long feeItemCount = BBSConfig.CHANGE_PLAYER_NAME_SERVICE_COST_ITEM_COUNT;
				if (feeItemId == 0) {
					player.sendMessage(player.isLangRus() ? "功能禁用。" : "功能禁用。");
					player.sendPacket(ShowBoardPacket.CLOSE);
					return;
				}

				HtmTemplates tpls = HtmCache.getInstance().getTemplates("member/change_player_name.htm", player);
				html = tpls.get(0);

				StringBuilder content = new StringBuilder();
				if (buypassOptions.length == 2)//if(!st.hasMoreTokens())
				{
					if (feeItemCount > 0) {
						String feeBlock = tpls.get(1);
						feeBlock = feeBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
						feeBlock = feeBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

						content.append(feeBlock);
					} else
						content.append(tpls.get(2));

					content.append(tpls.get(3));
				} else {
					if (!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player)) {
						onWrongCondition(player);
						return;
					}

					String newPlayerName = buypassOptions[2];//st.nextToken();
					if (newPlayerName.charAt(0) == ' ')
						newPlayerName = newPlayerName.substring(1);

					if (player.getName().equals(newPlayerName))
						content.append(tpls.get(7));
					if (!Util.isMatchingRegexp(newPlayerName, Config.CNAME_TEMPLATE))
						content.append(tpls.get(5));
					else if (CharacterDAO.getInstance().getObjectIdByName(newPlayerName) > 0)
						content.append(tpls.get(6));
					else if (feeItemCount > 0 && !ItemFunctions.deleteItem(player, feeItemId, feeItemCount, true)) {
						String noHaveItemBlock = tpls.get(4);
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

						content.append(noHaveItemBlock);
					} else {
						content.append(tpls.get(8).replace("<?player_name?>", newPlayerName));

						String oldName = player.getName();

						player.reName(newPlayerName, true);
						Log.add("Character " + oldName + " renamed to " + newPlayerName, "renames");
					}
				}
				html = html.replace("<?content?>", content.toString());
				sendHtmlMessage(player, html);
			} else if ("pet".equals(buypassOptions[1])) {
				final int feeItemId = BBSConfig.CHANGE_PET_NAME_SERVICE_COST_ITEM_ID;
				final long feeItemCount = BBSConfig.CHANGE_PET_NAME_SERVICE_COST_ITEM_COUNT;
				if (feeItemId == 0) {
					player.sendMessage(player.isLangRus() ? "功能禁用。" : "功能禁用。");
					player.sendPacket(ShowBoardPacket.CLOSE);
					return;
				}

				HtmTemplates tpls = HtmCache.getInstance().getTemplates("member/change_pet_name.htm", player);
				html = tpls.get(0);

				StringBuilder content = new StringBuilder();
				if (buypassOptions.length == 2)//if(!st.hasMoreTokens())
				{
					if (feeItemCount > 0) {
						String feeBlock = tpls.get(1);
						feeBlock = feeBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
						feeBlock = feeBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

						content.append(feeBlock);
					} else
						content.append(tpls.get(2));

					content.append(tpls.get(3));
				} else {
					if (!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player)) {
						onWrongCondition(player);
						return;
					}

					String newPetName = buypassOptions[2];//if(!st.hasMoreTokens())"";//st.nextToken();
					if (newPetName.charAt(0) == ' ')
						newPetName = newPetName.substring(1);

					PetInstance pet = player.getPet();
					if (pet == null)
						content.append(tpls.get(8));
					else if (feeItemCount > 0 && pet.isDefaultName())
						content.append(tpls.get(7));
					else if (pet.getName().equals(newPetName))
						content.append(tpls.get(6));
					else if (newPetName.length() < 1 || newPetName.length() > 8)
						content.append(tpls.get(5));
					else if (feeItemCount > 0 && !ItemFunctions.deleteItem(player, feeItemId, feeItemCount, true)) {
						String noHaveItemBlock = tpls.get(4);
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

						content.append(noHaveItemBlock);
					} else {
						content.append(tpls.get(9).replace("<?pet_name?>", newPetName));

						String oldName = pet.getName();

						pet.setName(newPetName);
						pet.broadcastCharInfo();
						pet.updateControlItem();
						Log.add("Pet " + oldName + " renamed to " + newPetName, "renames");
					}
				}
				html = html.replace("<?content?>", content.toString());
				sendHtmlMessage(player, html);
			} else if ("clan".equals(buypassOptions[1])) {
				final int feeItemId = BBSConfig.CHANGE_CLAN_NAME_SERVICE_COST_ITEM_ID;
				final long feeItemCount = BBSConfig.CHANGE_CLAN_NAME_SERVICE_COST_ITEM_COUNT;
				if (feeItemId == 0) {
					player.sendMessage(player.isLangRus() ? "功能禁用。" : "功能禁用。");
					player.sendPacket(ShowBoardPacket.CLOSE);
					return;
				}

				HtmTemplates tpls = HtmCache.getInstance().getTemplates("member/change_clan_name.htm", player);
				html = tpls.get(0);

				StringBuilder content = new StringBuilder();
				if (buypassOptions.length == 2)//if(!st.hasMoreTokens())
				{
					if (feeItemCount > 0) {
						String feeBlock = tpls.get(1);
						feeBlock = feeBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
						feeBlock = feeBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

						content.append(feeBlock);
					} else
						content.append(tpls.get(2));

					content.append(tpls.get(3));
				} else {
					if (!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player)) {
						onWrongCondition(player);
						return;
					}

					String newClanName = buypassOptions[2];//st.nextToken();
					if (newClanName.charAt(0) == ' ')
						newClanName = newClanName.substring(1);

					final Clan clan = player.getClan();
					if (clan == null)
						content.append(tpls.get(8));
					else if (!player.isClanLeader())
						content.append(tpls.get(9));
					else if (clan.getSubUnit(Clan.SUBUNIT_MAIN_CLAN).getName().equals(newClanName))
						content.append(tpls.get(6));
					else if (!Util.isMatchingRegexp(newClanName, Config.CLAN_NAME_TEMPLATE))
						content.append(tpls.get(5));
					else if (ClanTable.getInstance().getClanByName(newClanName) != null)
						content.append(tpls.get(7));
					else if (feeItemCount > 0 && !ItemFunctions.deleteItem(player, feeItemId, feeItemCount, true)) {
						String noHaveItemBlock = tpls.get(4);
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

						content.append(noHaveItemBlock);
					} else {
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
				sendHtmlMessage(player, html);
			}
		} else if (buypassOptions[0].equals("sex")) {
			final int feeItemId = BBSConfig.CHANGE_SEX_SERVICE_COST_ITEM_ID;
			final long feeItemCount = BBSConfig.CHANGE_SEX_SERVICE_COST_ITEM_COUNT;
			if (feeItemId == 0) {
				player.sendMessage(player.isLangRus() ? "功能禁用。" : "功能禁用。");
				player.sendPacket(ShowBoardPacket.CLOSE);
				return;
			}

			HtmTemplates tpls = HtmCache.getInstance().getTemplates("member/change_sex.htm", player);
			html = tpls.get(0);

			StringBuilder content = new StringBuilder();
			if (buypassOptions.length == 1)//if(!st.hasMoreTokens())
			{
				if (feeItemCount > 0) {
					String feeBlock = tpls.get(1);
					feeBlock = feeBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
					feeBlock = feeBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

					content.append(feeBlock);
				} else
					content.append(tpls.get(2));

				content.append(tpls.get(3));
			} else {
				String cmd3 = buypassOptions[1];
				if ("change".equals(cmd3)) {
					if (!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player)) {
						onWrongCondition(player);
						return;
					}

					if (feeItemCount > 0 && !ItemFunctions.deleteItem(player, feeItemId, feeItemCount, true)) {
						String noHaveItemBlock = tpls.get(4);
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));
						content.append(noHaveItemBlock);
					} else {
						content.append(tpls.get(5).replace("<?player_name?>", player.getName()));

						player.changeSex();
						player.broadcastUserInfo(true);
						player.broadcastPacket(new MagicSkillUse(player, player, 23128, 1, 1, 0));
						Log.add("Player " + player.getName() + " changed sex to : " + player.getSex(), "changesex");
					}
				}
			}
			html = html.replace("<?content?>", content.toString());
			sendHtmlMessage(player, html);
		} else if (buypassOptions[0].equals("expand")) {
			String cmd3 = buypassOptions[1];
			if ("inventory".equals(cmd3)) {
				final int feeItemId = BBSConfig.EXPAND_INVENTORY_SERVICE_COST_ITEM_ID;
				final long feeItemCount = BBSConfig.EXPAND_INVENTORY_SERVICE_COST_ITEM_COUNT;
				if (feeItemId == 0) {
					player.sendMessage(player.isLangRus() ? "功能禁用。" : "功能禁用。");
					player.sendPacket(ShowBoardPacket.CLOSE);
					return;
				}

				// TODO: Вынести конфиг в конфиги коммунити.
				if (Config.SERVICES_EXPAND_INVENTORY_MAX <= player.getExpandInventory())//修復擴充問題
				{
					player.sendMessage(player.isLangRus() ? "擴充到最大值，無法再擴充。" : "扩充到最大值，无法再扩充。");
					player.sendPacket(ShowBoardPacket.CLOSE);
					return;
				}

				HtmTemplates tpls = HtmCache.getInstance().getTemplates("member/expand_inventory.htm", player);
				html = tpls.get(0);

				StringBuilder content = new StringBuilder();
				if (buypassOptions.length == 2)//if(!st.hasMoreTokens())
				{
					if (feeItemCount > 0) {
						String feeBlock = tpls.get(1);
						feeBlock = feeBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
						feeBlock = feeBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

						content.append(feeBlock);
					} else
						content.append(tpls.get(2));

					content.append(tpls.get(3));
				} else {
					if (!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player)) {
						onWrongCondition(player);
						return;
					}

					int count = Integer.parseInt(buypassOptions[2]);
					if (count == 0)
						return;

					long price = feeItemCount * count;
					if (price > 0 && !ItemFunctions.deleteItem(player, feeItemId, price, true)) {
						String noHaveItemBlock = tpls.get(4);
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_count?>", Util.formatAdena(price));
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

						content.append(noHaveItemBlock);
					} else {
						content.append(tpls.get(5).replace("<?player_name?>", player.getName()).replace("<?expand_count?>", String.valueOf(count)));

						player.setExpandInventory(player.getExpandInventory() + count);
						player.setVar("ExpandInventory", String.valueOf(player.getExpandInventory()), -1);
						player.sendPacket(new ExStorageMaxCountPacket(player));
						Log.add("Player " + player.getName() + " expand inventory", "expandinventory");
					}
				}
				html = html.replace("<?content?>", content.toString());
				sendHtmlMessage(player, html);
			} else if ("warehouse".equals(cmd3)) {
				final int feeItemId = BBSConfig.EXPAND_WAREHOUSE_SERVICE_COST_ITEM_ID;
				final long feeItemCount = BBSConfig.EXPAND_WAREHOUSE_SERVICE_COST_ITEM_COUNT;
				if (feeItemId == 0) {
					player.sendMessage(player.isLangRus() ? "功能禁用。" : "功能禁用。");
					player.sendPacket(ShowBoardPacket.CLOSE);
					return;
				}

				HtmTemplates tpls = HtmCache.getInstance().getTemplates("member/expand_warehouse.htm", player);
				html = tpls.get(0);

				StringBuilder content = new StringBuilder();
				if (buypassOptions.length == 2) {
					if (feeItemCount > 0) {
						String feeBlock = tpls.get(1);
						feeBlock = feeBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
						feeBlock = feeBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

						content.append(feeBlock);
					} else
						content.append(tpls.get(2));

					content.append(tpls.get(3));
				} else {
					if (!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player)) {
						onWrongCondition(player);
						return;
					}

					int count = Integer.parseInt(buypassOptions[2]);
					if (count == 0)
						return;

					long price = feeItemCount * count;
					if (price > 0 && !ItemFunctions.deleteItem(player, feeItemId, price, true)) {
						String noHaveItemBlock = tpls.get(4);
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_count?>", Util.formatAdena(price));
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

						content.append(noHaveItemBlock);
					} else {
						content.append(tpls.get(5).replace("<?player_name?>", player.getName()).replace("<?expand_count?>", String.valueOf(count)));

						player.setExpandWarehouse(player.getExpandWarehouse() + count);
						player.setVar("ExpandWarehouse", String.valueOf(player.getExpandWarehouse()), -1);
						player.sendPacket(new ExStorageMaxCountPacket(player));
						Log.add("Player " + player.getName() + " expand warehouse", "expandwarehouse");
					}
				}
				html = html.replace("<?content?>", content.toString());
				sendHtmlMessage(player, html);
			} else if ("clanwarehouse".equals(cmd3)) {
				final int feeItemId = BBSConfig.EXPAND_CLANWAREHOUSE_SERVICE_COST_ITEM_ID;
				final long feeItemCount = BBSConfig.EXPAND_CLANWAREHOUSE_SERVICE_COST_ITEM_COUNT;
				if (feeItemId == 0) {
					player.sendMessage(player.isLangRus() ? "功能禁用。" : "功能禁用。");
					player.sendPacket(ShowBoardPacket.CLOSE);
					return;
				}

				Clan clan = player.getClan();
				if (clan == null) {
					player.sendMessage(player.isLangRus() ? "您不是血盟成員。" : "您不是血盟成员。");
					player.sendPacket(ShowBoardPacket.CLOSE);
					return;
				}

				HtmTemplates tpls = HtmCache.getInstance().getTemplates("member/expand_clanwarehouse.htm", player);
				html = tpls.get(0);

				StringBuilder content = new StringBuilder();
				if (buypassOptions.length == 2) {
					if (feeItemCount > 0) {
						String feeBlock = tpls.get(1);
						feeBlock = feeBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
						feeBlock = feeBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

						content.append(feeBlock);
					} else
						content.append(tpls.get(2));

					content.append(tpls.get(3));
				} else {
					if (!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player)) {
						onWrongCondition(player);
						return;
					}

					int count = Integer.parseInt(buypassOptions[2]);
					if (count == 0)
						return;

					long price = feeItemCount * count;
					if (price > 0 && !ItemFunctions.deleteItem(player, feeItemId, price, true)) {
						String noHaveItemBlock = tpls.get(4);
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_count?>", Util.formatAdena(price));
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

						content.append(noHaveItemBlock);
					} else {
						content.append(tpls.get(5).replace("<?player_name?>", player.getName()).replace("<?expand_count?>", String.valueOf(count)));

						clan.setWhBonus(player.getClan().getWhBonus() + count);
						player.sendPacket(new ExStorageMaxCountPacket(player));
						Log.add("Player " + player.getName() + " expand clan warehouse", "expandclanwarehouse");
					}
				}
				html = html.replace("<?content?>", content.toString());
				sendHtmlMessage(player, html);
			}
		} else if (buypassOptions[0].equals("color")) {
			String cmd3 = buypassOptions[1];
			if ("name".equals(cmd3)) {
				final int feeItemId = BBSConfig.COLOR_NAME_SERVICE_COST_ITEM_ID;
				final long feeItemCount = BBSConfig.COLOR_NAME_SERVICE_COST_ITEM_COUNT;
				final String[] availableColors = BBSConfig.COLOR_NAME_SERVICE_COLORS;
				if (feeItemId == 0 || availableColors.length == 0) {
					player.sendMessage(player.isLangRus() ? "功能禁用。" : "功能禁用。");
					player.sendPacket(ShowBoardPacket.CLOSE);
					return;
				}

				HtmTemplates tpls = HtmCache.getInstance().getTemplates("member/color_name_change.htm", player);
				html = tpls.get(0);

				StringBuilder content = new StringBuilder();
				if (buypassOptions.length == 2) {
					if (feeItemCount > 0) {
						String feeBlock = tpls.get(1);
						feeBlock = feeBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
						feeBlock = feeBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

						content.append(feeBlock);
					} else
						content.append(tpls.get(2));

					final String colorBlock = tpls.get(3).replace("<?player_name?>", player.getName());

					if (player.getNameColor() != Integer.decode("0xFFFFFF"))
						content.append(colorBlock.replace("<?color?>", "FFFFFF"));

					for (String color : availableColors) {
						String bgrColor = color.substring(4, 6) + color.substring(2, 4) + color.substring(0, 2);
						if (player.getNameColor() != Integer.decode("0x" + bgrColor))
							content.append(colorBlock.replace("<?color?>", color));
					}
				} else {
					if (!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player)) {
						onWrongCondition(player);
						return;
					}

					final String newColor = buypassOptions[2].replace(" ", "");

					if (!newColor.equalsIgnoreCase("FFFFFF")) {
						boolean available = false;
						for (String color : availableColors) {
							if (color.equalsIgnoreCase(newColor)) {
								available = true;
								break;
							}
						}

						if (!available) {
							player.sendPacket(ShowBoardPacket.CLOSE);
							return;
						}
					}

					final String bgrNewColor = newColor.substring(4, 6) + newColor.substring(2, 4) + newColor.substring(0, 2);
					final int newColorInt = Integer.decode("0x" + bgrNewColor);
					if (player.getNameColor() == newColorInt) {
						player.sendPacket(ShowBoardPacket.CLOSE);
						return;
					}

					if (feeItemCount > 0 && !ItemFunctions.deleteItem(player, feeItemId, feeItemCount, true)) {
						String noHaveItemBlock = tpls.get(4);
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

						content.append(noHaveItemBlock);
					} else {
						content.append(tpls.get(5).replace("<?color?>", newColor).replace("<?player_name?>", player.getName()));

						player.setNameColor(newColorInt);
						player.broadcastUserInfo(true);
						Log.add("Player " + player.getName() + " changed name color to " + newColor, "changecolor");
					}
				}
				html = html.replace("<?content?>", content.toString());
				sendHtmlMessage(player, html);
			} else if ("title".equals(cmd3)) {
				final int feeItemId = BBSConfig.COLOR_TITLE_SERVICE_COST_ITEM_ID;
				final long feeItemCount = BBSConfig.COLOR_TITLE_SERVICE_COST_ITEM_COUNT;
				final String[] availableColors = BBSConfig.COLOR_TITLE_SERVICE_COLORS;
				if (feeItemId == 0 || availableColors.length == 0) {
					player.sendMessage(player.isLangRus() ? "功能禁用。" : "功能禁用。");
					player.sendPacket(ShowBoardPacket.CLOSE);
					return;
				}

				HtmTemplates tpls = HtmCache.getInstance().getTemplates("member/color_title_change.htm", player);
				html = tpls.get(0);

				StringBuilder content = new StringBuilder();
				if (buypassOptions.length == 2) {
					if (feeItemCount > 0) {
						String feeBlock = tpls.get(1);
						feeBlock = feeBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
						feeBlock = feeBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

						content.append(feeBlock);
					} else
						content.append(tpls.get(2));

					final String colorBlock = tpls.get(3).replace("<?player_name?>", player.getName());

					if (player.getTitleColor() != Integer.decode("0xFFFF77"))
						content.append(colorBlock.replace("<?color?>", "77FFFF"));

					for (String color : availableColors) {
						String bgrColor = color.substring(4, 6) + color.substring(2, 4) + color.substring(0, 2);
						if (player.getTitleColor() != Integer.decode("0x" + bgrColor))
							content.append(colorBlock.replace("<?color?>", color));
					}
				} else {
					if (!BBSConfig.GLOBAL_USE_FUNCTIONS_CONFIGS && !checkUseCondition(player)) {
						onWrongCondition(player);
						return;
					}

					final String newColor = buypassOptions[2].replace(" ", "");

					if (!newColor.equalsIgnoreCase("77FFFF")) {
						boolean available = false;
						for (String color : availableColors) {
							if (color.equalsIgnoreCase(newColor)) {
								available = true;
								break;
							}
						}

						if (!available) {
							player.sendPacket(ShowBoardPacket.CLOSE);
							return;
						}
					}

					final String bgrNewColor = newColor.substring(4, 6) + newColor.substring(2, 4) + newColor.substring(0, 2);
					final int newColorInt = Integer.decode("0x" + bgrNewColor);
					if (player.getTitleColor() == newColorInt) {
						player.sendPacket(ShowBoardPacket.CLOSE);
						return;
					}

					if (feeItemCount > 0 && !ItemFunctions.deleteItem(player, feeItemId, feeItemCount, true)) {
						String noHaveItemBlock = tpls.get(4);
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_count?>", Util.formatAdena(feeItemCount));
						noHaveItemBlock = noHaveItemBlock.replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId));

						content.append(noHaveItemBlock);
					} else {
						content.append(tpls.get(5).replace("<?color?>", newColor).replace("<?player_name?>", player.getName()));

						player.setTitleColor(newColorInt);
						player.broadcastUserInfo(true);
						Log.add("Player " + player.getName() + " changed title color to " + newColor, "changecolor");
					}
				}
				html = html.replace("<?content?>", content.toString());
				sendHtmlMessage(player, html);
			}
		} else if (buypassOptions[0].equals("SetCareer")) {
			StringTokenizer st = new StringTokenizer(command, " ");
			String cmd = st.nextToken();
			String cmd2 = st.nextToken();
			if ("profession".equals(cmd2)) {
				HtmTemplates tpls = HtmCache.getInstance().getTemplates("member/4362-Career.htm", player);
				html = tpls.get(0);
				StringBuilder content = new StringBuilder();
				final int feeItemId = getFeeItemIdForChangeClass(player);
				final long feeItemCount = getFeeItemCountForChangeClass(player);
				final int nextClassMinLevel = getNextClassMinLevel(player);
				if (!st.hasMoreTokens()) {
					if (nextClassMinLevel == -1)
						content.append(tpls.get(1));
					else if (feeItemId == 0)
						content.append(tpls.get(8));
					else {
						if (nextClassMinLevel > player.getLevel())
							content.append(tpls.get(5).replace("<?level?>", String.valueOf(nextClassMinLevel)));
						else {
							List<ClassId> availClasses = getAvailClasses(player.getClassId());
							if (availClasses.isEmpty())
								content.append(tpls.get(6));
							else {
								ClassId classId = availClasses.get(0);
								content.append(tpls.get(2));
								if (feeItemId > 0 && feeItemCount > 0) {
									content.append("<br1>");
									content.append(tpls.get(3).replace("<?fee_item_count?>", String.valueOf(feeItemCount)).replace("<?fee_item_name?>", HtmlUtils.htmlItemName(feeItemId)));
								}
								for (ClassId cls : availClasses) {
									content.append("<br>");
									String classHtm = tpls.get(4);
									classHtm = classHtm.replace("<?class_name?>", cls.getName(player));
									classHtm = classHtm.replace("<?class_id?>", String.valueOf(cls.getId()));
									content.append(classHtm);
								}
							}
						}
					}
				} else {
					if (nextClassMinLevel == -1 || feeItemId == 0 || nextClassMinLevel > player.getLevel()) {
						player.sendMessage("沒有下一級可以升上去了。");
						return;
					}
					List<ClassId> availClasses = getAvailClasses(player.getClassId());
					if (availClasses.isEmpty()) {
						player.sendMessage("沒有下一級可以升上去了。");
						return;
					}
					boolean avail = false;
					ClassId classId = ClassId.VALUES[Integer.parseInt(st.nextToken())];
					for (ClassId cls : availClasses) {
						if (cls == classId) {
							avail = true;
							break;
						}
					}
					if (!avail) {
						player.sendMessage("沒有下一級可以升上去了。");
						return;
					}
					player.sendPacket(SystemMsg.CONGRATULATIONS__YOUVE_COMPLETED_A_CLASS_TRANSFER);
					player.setClassId(classId.getId(), false);
					player.broadcastUserInfo(true);
					//player.sendMessage("轉職成功了。");
					return;
				}
				html = html.replace("<?content?>", content.toString());
				sendHtmlMessage(player, html);
			}
		} else if (buypassOptions[0].equals("ShowChangeClassId")) {
			ShowChangeClassId(player);
		} else if (buypassOptions[0].equals("ShowMyLevelCanChange")) {
			if (Hero.getInstance().isHero(player.getObjectId())) {
				player.sendMessage("英雄不能转换！");
				return;
			}
			ShowMyLevelCanChange(player);
		}else if(buypassOptions[0].equals("ChangeMyClassId"))
		{
			int myClassId = Integer.parseInt(buypassOptions[1]);
			if(myClassId > (ClassId.VALUES.length - 1))
			{
				player.sendMessage("輸入的參數錯誤");// There are no classes over 136 id.
				return;
			}
			int money = GetLevelMoney(player);
			if(player.getInventory().getCountOf(88888) < money)
			{
				player.sendMessage("支付費用不足 " + money + " 個赞助币.");// There are no classes over 136 id.
				return;
			}
			for(Servitor servitor : player.getServitors())//包结宠物
				servitor.unSummon(false);//移除招唤的

			if(player.getCubics().size() > 0)
			{
				player.deleteCubics(); //移除晶体
			}
			if(player.isTransformed())//变身
			{
				player.setTransform(null);
			}
			for(SkillEntry skillEntry : player.getAllSkills())//删除全部技能 保留 skillReserve 中的技能
			{
				boolean flag = false;
				if(skillEntry != null){
					for (int i = 0; i < skillReserve.length; i++) {
						if (skillReserve[i] == skillEntry.getId()) {
							flag = true;
							break;
						}
					}
					if (!flag) {
						player.removeSkill(skillEntry, true);
					}
				}
			}
			Henna[] hennas = player.getHennaList().values(false);
			for(Henna henna : hennas)//删除纹身
			{
				player.getHennaList().remove(henna);
			}
			for(long party : Slot)//脱掉所有装备
			{
				player.getInventory().unEquipItemInBodySlot(party);
			}

			//player.sendSkillList();
			ItemFunctions.deleteItem(player, 88888, money, true); //这样子删除物品有讯息出来
			int oldId = player.getActiveClassId();
			String name = "「" + player.getName() + "」通過喬安轉換了自己的職業為: " + player.getClassId().getName(player) + " -> ";
			player.setClassId(myClassId, true);
			player.broadcastCharInfo();
			name += player.getClassId().getName(player);
			UpdateChangeClassId(player,oldId,myClassId);
			Announcements.announceToAll(name);

//			//這裡應該要把武器轉換學習的技能給完全載入給玩家。 20210409 這部份需要去實測，很重要。
//			int[] learnSkills = GetLearnedSkillList(player);
//			for (int i = 0; i < learnSkills.length; i++)
//			{
//				SkillEntry skillEntry = SkillEntry.makeSkillEntry(SkillEntryType.NONE, learnSkills[i], 1);
//				player.addSkill(skillEntry, true);
//			}
		}else if(buypassOptions[0].equals("showChangeClassLog"))
		{
			int index = Integer.parseInt(buypassOptions[1]);
			showChangeClassLog(player, index);
		}
	}
	private static void ShowChangeClassId(Player player)
	{
		String html = HtmCache.getInstance().getHtml("member/41001-3.htm", player);
		int money = GetLevelMoney(player);
		html = html.replace("<$money$>", String.valueOf(money));
		HtmlMessage msg = new HtmlMessage(5);
		msg.setHtml(html);
		player.sendPacket(msg);
		player.sendActionFailed();
	}
	private static int GetLevelMoney(Player player)
	{
//		1 - 76级 = 每次 50赞助币<br1>
//		76-80 级 = 每次 100赞助币<br1>
//		80级以上  = 每次 100赞助币加上每级增加50赞助币

		int money = 50;
		int level = player.getLevel();
		if(level <= 76)
			money = 50;
		else if(level <= 80)
			money = 100;
		else
			money = 100 + (level - 80) * 50;//这一个区域应该不存在写一个超大值(如果超过85级才会出现的区域)
		return money;
	}
	private static void ShowMyLevelCanChange(Player player)
	{
		String html = "";
		if(player.getClassLevel() == ClassLevel.NONE)
		{
			html = HtmCache.getInstance().getHtml("member/41001-NONE.htm", player); //这一个是出生的职业，应该要出现的讯息为不支持
			HtmlMessage msg = new HtmlMessage(5);
			msg.setHtml(html);
			player.sendPacket(msg);
			player.sendActionFailed();
			return;
		}
		html = HtmCache.getInstance().getHtml("member/41001-ClassId.htm", player); //这一个是出生的职业，应该要出现的讯息为不支持
		ClassLevel playerClassLevel = player.getClassLevel();
		int classId = player.getActiveClassId();
		StringBuilder content = new StringBuilder();
		//<Button ALIGN=LEFT ICON="NORMAL" action="bypass -h npc?ShowMyLevelCanChange">确认转换职业 (需求 <$money$> 玫瑰币)</Button><br>
		for(ClassId _class : ClassId.values())
		{
			if(_class.getClassLevel() == playerClassLevel)
			{
				if(_class.getId() != classId)
					content.append("<Button ALIGN=LEFT ICON=\"NORMAL\" action=\"bypass -h npc?ChangeMyClassId " + _class.getId() + "\">確認轉換職業 " + _class.getName(player) + "</Button><br>");
			}
		}
		html = html.replace("<$content$>", content.toString());

		HtmlMessage msg = new HtmlMessage(5);
		msg.setHtml(html);
		player.sendPacket(msg);
		player.sendActionFailed();
	}
	public static void UpdateChangeClassId(Player player, int oldId, int newId)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();

			statement = con.prepareStatement("INSERT INTO _change_class (char_name, oldClassId ,newClassId ,changeTime) VALUES (?,?,?,?)");
			statement.setString(1, player.getName());
			statement.setInt(2, oldId);
			statement.setInt(3, newId);
			statement.setInt(4, (int) (System.currentTimeMillis() / 1000));
			statement.execute();
			statement.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}
	private static int[] GetLearnedSkillList(Player player)//TODO
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;

		List<Integer> values = new ArrayList<Integer>();
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT skill_id from _skill_collect where obj_id =?");
			statement.setInt(1, player.getObjectId());
			rset = statement.executeQuery();
			while (rset.next())
			{
				values.add(rset.getInt("skill_id"));
			}
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		//int[] array = list.stream().mapToInt(i->i).toArray();
		if(values.size() > 0)
		{
			return values.stream().mapToInt(i -> i).toArray();
		}
		return new int[0];
	}
	public static void showChangeClassLog(Player player,int index)
	{
		int pageLimite = 16;//一頁二筆記錄  如要改成 20筆 這裡改就行了
		String number = pageLimite *index  + "," + pageLimite;
		String html = HtmCache.getInstance().getHtml("member/41001-ChangeLog.htm", player); //这一个是出生的职业，应该要出现的讯息为不支持
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		StringBuilder content = new StringBuilder();
		int Allpages =0;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT count(*) as ct FROM _change_class");
			rset = statement.executeQuery();
			if (rset.next())
			{
				Allpages = rset.getInt("ct");
			}
			else
			{
				html = html.replace("<$content$>", "");
				html = html.replace("<$pages$>", "");
				HtmlMessage msg = new HtmlMessage(5);
				msg.setHtml(html);
				player.sendPacket(msg);
				player.sendActionFailed();
				statement.close();
				return;
			}

			statement.close();



			statement = con.prepareStatement("SELECT char_name,oldClassId,newClassId,changeTime FROM _change_class ORDER BY changeTime desc LIMIT "+ number);
			rset = statement.executeQuery();
			content.append("<table>");
			boolean first = true;
			while (rset.next())
			{
				if(first)
				{
					content.append("<tr><td align=center width=86><font color=33ff00>玩家</font></td><td align=center width=86><font color=33ff00>原職業</font></td><td align=center width=86><font color=33ff00>現職業</font></td></tr>");
					first=false;
				}
				content.append("<tr>");
				content.append("<td align=center>" + rset.getString("char_name") + "</td>");
				content.append("<td align=center>" + ClassIdToName(player, rset.getInt("oldClassId")) + "</td>");
				content.append("<td align=center>" + ClassIdToName(player, rset.getInt("newClassId")) + "</td>");
				/*时间记录不需要，位置不够long t = rset.getInt("changeTime") * 1000L;
				content.append("<td>" + dateFormat.format(t) + "</td>");*/
				content.append("</tr>");
			}
			content.append("</table>");
			statement.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}

		int minPage = 1;
		int maxPage = (int) Math.ceil((double) Allpages / pageLimite);
		int currentPage = Math.max(Math.min(maxPage, index+1), minPage);
		StringBuilder pageContent = new StringBuilder();
		pageContent.append("<center><table><tr>");
		for(int i = minPage; i <= maxPage; i++)
		{
			if(i == currentPage)
			{
				pageContent.append("<td><font color=FFFFFF>"+ i + "<font></td>");
			}
			else
			{
				//<font color=FF00FF><a action="bypass _bbsmoney:ShowMainhtml">xxx</a></font>
				pageContent.append("<td><font color=FF00FF><a action=\"bypass -h npc?showChangeClassLog " + (i - 1) + "\" >" + i + "</a></font></td>");
			}
			//<font color=FF00FF><button value=\"本人\""
		}
		pageContent.append("</tr></table></center>");

		html = html.replace("<$content$>", content.toString());
		html = html.replace("<$pages$>", pageContent.toString());
		HtmlMessage msg = new HtmlMessage(5);
		msg.setHtml(html);
		player.sendPacket(msg);
		player.sendActionFailed();
	}
	public static String ClassIdToName(Player player, int Id)
	{
		return new CustomMessage("l2s.gameserver.model.base.ClassId.name." + Id).toString(player);
	}
	private static void showPage(Player player, String page)
	{
		String html = HtmCache.getInstance().getHtml("member/" + page, player);
		String value = getClanBuff(player);
		//String tmp = getHtmlByBuff(buypassOptions[2], player);
		if(value.length() > 0)
		{
			html = html.replace("<$content$>", value);
		}
		else
		{
			html = html.replace("<$content$>", "目前没有东西");
		}
		HtmlMessage msg = new HtmlMessage(5);
		msg.setHtml(html);
		player.sendPacket(msg);
		player.sendActionFailed();	
	}
	private static String doubleToString(double value)
	{
		int intValue = (int) value;
		if(intValue == value)
			return String.valueOf(intValue);
		return String.valueOf(value);
	}
	
	private static int getNextClassMinLevel(Player player)
	{
		final ClassId classId = player.getClassId();
		if(classId.isLast())
			return -1;

		return classId.getClassMinLevel(true);
	}
	private static int getFeeItemIdForChangeClass(Player player)
	{
		switch(player.getClassId().getClassLevel())
		{
			case NONE:
				return BBSConfig.OCCUPATION_SERVICE_COST_ITEM_ID_1;
			case FIRST:
				return BBSConfig.OCCUPATION_SERVICE_COST_ITEM_ID_2;
			case SECOND:
				return BBSConfig.OCCUPATION_SERVICE_COST_ITEM_ID_3;
		}
		return 0;
	}

	private static long getFeeItemCountForChangeClass(Player player)
	{
		switch(player.getClassId().getClassLevel())
		{
			case NONE:
				return BBSConfig.OCCUPATION_SERVICE_COST_ITEM_COUNT_1;
			case FIRST:
				return BBSConfig.OCCUPATION_SERVICE_COST_ITEM_COUNT_2;
			case SECOND:
				return BBSConfig.OCCUPATION_SERVICE_COST_ITEM_COUNT_3;
		}
		return 0L;
	}
	private static List<ClassId> getAvailClasses(ClassId playerClass)
	{
		List<ClassId> result = new ArrayList<ClassId>();
		for(ClassId _class : ClassId.values())
		{
			if(!_class.isDummy() && _class.getClassLevel().ordinal() == playerClass.getClassLevel().ordinal() + 1 && _class.childOf(playerClass))
				result.add(_class);
		}		
		return result;
	}
	
	protected boolean checkUseCondition(Player player)
	{
		if(player.getVar("jailed") != null)	// Если в тюрьме
			return false;

		if(player.isInTrainingCamp())
			return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_WHEN_DEAD)	// Если мертв, или притворяется мертвым
			if(player.isAlikeDead())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_IN_A_BATTLE)	// В состоянии битвы
			if(player.isCastingNow() || player.isInCombat() || player.isAttackingNow())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_IN_PVP)	// В PvP
			if(player.getPvpFlag() > 0)
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_IN_INVISIBLE)
			if(player.isInvisible(null))
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_ON_OLLYMPIAD)	// На олимпиаде
			if(player.isInOlympiadMode() || player.isInArenaObserverMode())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_IF_FLIGHT)	// В состоянии полета
			if(player.isFlying() || player.isInFlyingTransform())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_IF_IN_VEHICLE)	// На корабле
			if(player.isInBoat())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_IF_MOUNTED)	// На ездовом животном
			if(player.isMounted())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_IF_CANNOT_MOVE)	// В состоянии обизвдижения
			if(player.isMovementDisabled())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_WHEN_IN_TRADE)	// В состоянии торговли
			if(player.isInStoreMode() || player.isInTrade() || player.isInOfflineMode())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_IF_TELEPORTING)	// Во время телепортации
			if(player.isLogoutStarted() || player.isTeleporting())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_IN_DUEL)	// На дуели
			if(player.isInDuel())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_WHEN_IS_PK)	// Когда PK
			if(player.isPK())
				return false;

		if(BBSConfig.CAN_USE_FUNCTIONS_CLAN_LEADERS_ONLY)	// Если клан лидер
			if(!player.isClanLeader())
				return false;

		if(!BBSConfig.CAN_USE_FUNCTIONS_ON_SIEGE)	// На осаждаемой территории
			if(player.isInSiegeZone())
				return false;

		if(BBSConfig.CAN_USE_FUNCTIONS_IN_PEACE_ZONE_ONLY)	// В мирной зоне
			if(!player.isInPeaceZone())
				return false;

		return true;
	}
	protected void onWrongCondition(Player player)
	{
		player.sendMessage(player.isLangRus() ? "您目前的立場不允許您使用此操作。" : "您目前的立场不允许您使用此操作");
		player.sendPacket(ShowBoardPacket.CLOSE);
	}
	private void sendHtmlMessage(Player player,String html)
	{
		HtmlMessage msg = new HtmlMessage(5);
		msg.setHtml(html);
		player.sendPacket(msg);
		player.sendActionFailed();	
	}
	//23小時52分11秒
	static	String buttontimes ="<tr><td align=\"center\" width=265><table width=265><tr><td><img src=\"icon.bm_nebit_box\" width=32 height=32></td><td fixwidth=\"150\" >\r\n" + 
				"小型联盟礼物 <font color=\"8474E2\">($name$)</font><br1><font color=\"LEVEL\">剩余领取时间:$time$</font></td><td fixwidth=\"30\" ><button value=\"领取\" action=\"$action$\" width=66 height=25 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\"></td>\r\n" + 
				"</tr></table></td></tr>";
		
	private static String getClanBuff(Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		String value = "";
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT  deletetime,name  FROM _player_give_clan_buff where obj_Id = ? and deletetime > ?");
			statement.setInt(1, player.getObjectId());
			statement.setLong(2, System.currentTimeMillis() / 1000);
			rset = statement.executeQuery();
			while (rset.next())
			{
				int t = rset.getInt("deletetime");
				String name = rset.getString("name");
				String cmd = "bypass -h npc?GiveBuffToPlayer " + name + " " + t;
				int between = t - (int) (System.currentTimeMillis() / 1000);
				int hour1 = between / 3600;
				int minute1 = between % 3600 / 60;
				int second1 = between % 60;
				String joinstr = buttontimes.replace("$name$", name);
				joinstr = joinstr.replace("$time$", hour1 + "小时" + minute1 + "分" + second1 + "秒");
				joinstr = joinstr.replace("$action$", cmd);
				value += joinstr;
			}
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return value;
	}
	//player,name,times
	private static boolean checkBuffAndGive(Player player, String name , int times)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		boolean giveBuff = true;
		BuffSkill buff ;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT skill_id,skill_level FROM _player_give_clan_buff where obj_Id = ? and name = ? and deletetime=? ");
			statement.setInt(1, player.getObjectId());
			statement.setString(2, name);
			statement.setLong(3,times);
			rset = statement.executeQuery();
			if(rset.next())
			{
				int skill_id = rset.getInt("skill_id");
				int skill_level = rset.getInt("skill_level");
				buff = BuffSkill.makeBuffSkill(skill_id, skill_level, 1, -1, false);
				player.callSkill(player, SkillEntry.makeSkillEntry(SkillEntryType.NONE, buff.getSkill()), new HashSet<Creature>(Arrays.asList(player)), false, false);
				statement.close();
				statement = con.prepareStatement("DELETE FROM _player_give_clan_buff where obj_Id = ? and name = ? and deletetime=? ");
				statement.setInt(1, player.getObjectId());
				statement.setString(2, name);
				statement.setLong(3, times);
				statement.execute();
			}
			else
			{
				giveBuff = false;
			}
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		if(!giveBuff)
		{
			return false;
		}
		return true;
	}
	private static int CheckMemberHaveConis(Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		int Counts = 0;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT sum(point) as pt FROM _game_acc where account = ?");
			statement.setString(1, player.getAccountName());
			rset = statement.executeQuery();
			if (rset.next())
			{
				Counts = rset.getInt("pt");
			}
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return Counts;
	}
	private static boolean UpdateMemberConis(Player player,int Counts)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			
			statement = con.prepareStatement("INSERT INTO _game_acc (point, account ,createtime) VALUES (?,?, unix_timestamp(now()))");
			statement.setInt(1, Counts);
			statement.setString(2, player.getAccountName());
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}
	static SimpleDateFormat dateFmt = new SimpleDateFormat("yyyyMMdd");
	//activeChar.sendMessage("申訴成功:當前時間為「 " + Todays + "」 請記得抓圖給GM解封。");
	private static boolean getToday(Player player)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		boolean go=false;
		try
		{
			int Todays = Integer.parseInt(dateFmt.format(new Date()));
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM _give_every_day_item where account = ? and createtime = ?");
			statement.setString(1, player.getAccountName());
			statement.setInt(2, Todays);
			rset = statement.executeQuery();
			if(rset.next())
			{
				go = false;//今日已取
			}
			else
			{
				go = true;//今日沒取
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return go;
	}

	private void updateGiveOk(Player player)
	{

		Connection con = null;
		PreparedStatement statement = null;
		int Todays = Integer.parseInt(dateFmt.format(new Date()));
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO _give_every_day_item (account ,createtime) VALUES (?,?)");
			statement.setString(1, player.getAccountName());
			statement.setInt(2, Todays);
			statement.execute();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}

	}
	
	
}
