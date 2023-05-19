package l2s.gameserver.utils.Transaction;


import l2s.gameserver.botscript.BotConfigImp;
import l2s.gameserver.core.BotEngine;
import l2s.gameserver.core.TransactionBankExchangeLog;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.MyUtilsFunction;
import l2s.gameserver.utils.Transaction.TransactionBankCount;

import java.io.IOException;
import java.math.BigDecimal;

public class TransactionBank {

    @Bypass(value = "bot.transactionBank")
    public void transactionBank(Player player, NpcInstance npc, String[] param)
    {
        BotConfigImp config = (BotConfigImp) BotEngine.getInstance().getBotConfig(player);
        if(param.length > 0)
        {
            if(param.length>1 && param[1].equals("refresh")){
                TransactionBankDao.getInstance().selectProcedureRate();
                TransactionBankCount.exchangeRate = TransactionBankDao.getInstance().selectExchangeRate();
            }
        }
//        // 每次打开交易行初始化time 为10 计时
//        TimerManager.getInstance().rateRenewTime(false,null,player);
//        TimerManager.getInstance().time =10;
//        TimerManager.getInstance().rateRenewTime(true ,null,player);
        // 每个玩家点开交易行都要去查询一次手续费率 并更新  新增
        TransactionBankDao.getInstance().selectProcedureRate();
        TransactionBankCount.exchangeRate = TransactionBankDao.getInstance().selectExchangeRate();
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
            if(param.length>1 && param[1].equals("refresh")){
                TransactionBankDao.getInstance().selectProcedureRate();
                TransactionBankCount.exchangeRate = TransactionBankDao.getInstance().selectExchangeRate();
            }
			MyUtilsFunction.chooseExchange(player,param[0]);
        }else {
            MyUtilsFunction.transactionBank(player);
        }
    }
    @Bypass(value = "bot.defineExchange")
    public void defineExchange(Player player, NpcInstance npc, String[] param) throws IOException {
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
				boolean b = player.getInventory().destroyItemByItemId(29520, 30L);
                if (b) {
				String replace =null;
                // 充足 扣除金币 并计算汇率
                if(!param[2].contains(",")){
                    replace = param[2];
                }
                replace = param[1].replace(",", "");
                // 扣除手续费 后应该增加的金币  新增
                double reduceGold = Double.parseDouble(replace) * (1 - TransactionBankDao.procedureRate);
                player.getInventory().addItem(57,Long.parseLong(String.valueOf(Math.round(reduceGold))));
                TransactionBankCount.getInstance().update("chooseGoldExchange",replace);
                player.sendMessage("成功兑换"+MyUtilsFunction.numFormat(Long.parseLong(replace))+"金币");
                TransactionBankExchangeLog.getInstance().printLog(player,MyUtilsFunction.numFormat(Long.parseLong(replace)),"chooseGoldExchange");
                MyUtilsFunction.chooseExchange(player,"chooseGoldExchange");
				}else {
                    player.sendMessage("兑换失败");
                }
            }
        }else if(param.length > 0 && param[0].equals("新魔力币")){
            // 如果是兑换赞助币 判断金币是否满足
            long countOfGold = player.getInventory().getCountOf(57);
            // 不足 跳转
            double addGold = TransactionBankCount.exchangeRate.longValue() * (1 + TransactionBankDao.procedureRate);
            // 不足 跳转  新增
            if (Math.round(countOfGold) >=0 && countOfGold < Math.round(addGold)){
                MyUtilsFunction.defineExchange(player,"chooseVipGoldExchange");
            }else {
                String replace =null;
                // 充足 扣除金币 并计算汇率
                if(!param[2].contains(",")){
                    replace = param[2];
                }
                replace = param[2].replace(",", "");
                 boolean b = player.getInventory().destroyItemByItemId(57, Math.round(addGold));
                if (b) {
                player.getInventory().addItem(29520,30L);
                TransactionBankCount.getInstance().update("chooseVipGoldExchange",replace);
                player.sendMessage("成功兑换30新魔力币");
                TransactionBankExchangeLog.getInstance().printLog(player, "30赞助币","");
//                TimerManager.getInstance().rateRenewTime(true,"chooseVipGoldExchange",player);
                MyUtilsFunction.chooseExchange(player,"chooseVipGoldExchange");
				}else {
                    player.sendMessage("兑换失败");
                }
            }
        }
    }
}
