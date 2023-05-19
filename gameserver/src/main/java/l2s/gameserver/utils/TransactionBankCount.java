package l2s.gameserver.utils;

import l2s.gameserver.GameServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TransactionBankCount {

    private static TransactionBankCount _instance = new TransactionBankCount();

    public static TransactionBankCount getInstance()
    {
        return _instance;
    }

    private static final Logger _log = LoggerFactory.getLogger(GameServer.class);

    public static ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock(true);

    public static final ReentrantReadWriteLock.ReadLock readLock = reentrantReadWriteLock.readLock();

    public static final ReentrantReadWriteLock.WriteLock writeLock = reentrantReadWriteLock.writeLock();

    public static BigDecimal exchangeRate;

    public BigDecimal read(){
        readLock.lock();
        try {
            exchangeRate = TransactionBankDao.getInstance().selectExchangeRate();
//            _log.info("player"+Thread.currentThread().getId()+"select=exchangeRate");
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            readLock.unlock();
        }
        return exchangeRate;
    }

    public void update(String chooseExchange,String exchangeRate){
        writeLock.lock();
        try {
            BigDecimal bigDecimal = new BigDecimal(Long.parseLong(exchangeRate));
            double v = bigDecimal.doubleValue();
            if (chooseExchange.equals("chooseGoldExchange")) {
                //如果是兑换金币 汇率= 交易前的汇率值 *0.99
                v *= 0.99;
            }else if(chooseExchange.equals("chooseVipGoldExchange")){
                //如果是兑换会员币 汇率= 交易前的汇率值 *1.01
                v *= 1.01;
            }
            BigDecimal rate = BigDecimal.valueOf(Math.round(v));
            TransactionBankDao.getInstance().updateExchangeRate(rate);
            TransactionBankCount.exchangeRate = rate;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            writeLock.unlock();
        }
    }
}
