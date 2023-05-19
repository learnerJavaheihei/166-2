package l2s.gameserver.utils;


import l2s.gameserver.botscript.BotConfigImp;
import l2s.gameserver.core.BotEngine;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;

import java.math.BigDecimal;

public class BotTransactionBank {

    @Bypass(value = "bot.transactionBank")
    public void transactionBank(Player player, NpcInstance npc, String[] param)
    {
        BotConfigImp config = (BotConfigImp) BotEngine.getInstance().getBotConfig(player);
        if(param.length > 0)
        {
            String charName = param[0];
            config.setLeaderName(charName);
        }
//        // 每次打开交易行初始化time 为10 计时
//        TimerManager.getInstance().rateRenewTime(false,null,player);
//        TimerManager.getInstance().time =10;
//        TimerManager.getInstance().rateRenewTime(true ,null,player);
        // 点开 显示汇率 并且10S后刷新
        MyUtilsFunction.transactionBank(player);
    }
    @Bypass(value = "bot.chooseExchange")
    public void chooseExchange(Player player, NpcInstance npc, String[] param)
    {
//        TimerManager.getInstance().rateRenewTime(false,null,player);
//        TimerManager.getInstance().time =10;
//        TimerManager.getInstance().rateRenewTime(true,param[0],player);
        if(param.length > 0)
        {
            MyUtilsFunction.chooseExchange(player,param[0]);
        }else {
            MyUtilsFunction.transactionBank(player);
        }
    }
    @Bypass(value = "bot.defineExchange")
    public void defineExchange(Player player, NpcInstance npc, String[] param)
    {
//        TimerManager.getInstance().rateRenewTime(false,null,player);
//        TimerManager.getInstance().time =10;
//        TimerManager.getInstance().rateRenewTime(true,"chooseGoldExchange",player);
        // 如果是兑换金币 则去判断赞助币是否充足
        if(param.length > 0 && param[0].equals("\u91d1\u5e01")){
            long countOfVipGold = player.getInventory().getCountOf(29520);
            if(countOfVipGold >=0 && countOfVipGold<30){
                // 跳至 请充值页面
                MyUtilsFunction.defineExchange(player,"chooseGoldExchange");
            }else {
                // 充足 扣除赞助币 并计算汇率
                player.getInventory().destroyItemByItemId(29520, 30L);
                String replace = param[1].replace(",", "");
                player.getInventory().addItem(57,Long.parseLong(replace));
                TransactionBankCount.getInstance().update("chooseGoldExchange",replace);
                player.sendMessage("成功兑换"+MyUtilsFunction.numFormat(Long.parseLong(replace))+"金币");

                MyUtilsFunction.chooseExchange(player,"chooseGoldExchange");
            }
        }else if(param.length > 0 && param[0].equals("\u8d5e\u52a9\u5e01")){
            // 如果是兑换赞助币 判断金币是否满足
            long countOfGold = player.getInventory().getCountOf(57);
            // 不足 跳转
            if (countOfGold >=0 &&   BigDecimal.valueOf(countOfGold).compareTo(TransactionBankCount.exchangeRate) == -1) {
                MyUtilsFunction.defineExchange(player,"chooseVipGoldExchange");
            }else {
                // 充足 扣除金币 并计算汇率
                String replace = param[2].replace(",", "");
                player.getInventory().destroyItemByItemId(57, Long.parseLong(replace));
                player.getInventory().addItem(29520,30L);
                TransactionBankCount.getInstance().update("chooseVipGoldExchange",replace);
                player.sendMessage("成功兑换30赞助币");
//                TimerManager.getInstance().rateRenewTime(true,"chooseVipGoldExchange",player);
                MyUtilsFunction.chooseExchange(player,"chooseVipGoldExchange");
            }
        }
    }
}
