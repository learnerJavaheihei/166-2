package l2s.gameserver.core;

import l2s.gameserver.model.Player;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.logging.SimpleFormatter;

public class TransactionBankExchangeLog {

    private static TransactionBankExchangeLog _instance = new TransactionBankExchangeLog();

    public static TransactionBankExchangeLog getInstance()
    {
        return _instance;
    }

    public void printLog(Player player, String num, String kind) throws IOException {
        String filePath = "C:/transactionBank";
        File dir = new File(filePath);
        // 一、检查放置文件的文件夹路径是否存在，不存在则创建
        if (!dir.exists()) {
            dir.mkdirs();// mkdirs创建多级目录
        }
        File checkFile = new File(filePath + "/transactionBank.txt");
        FileWriter writer = null;
        try {
            // 二、检查目标文件是否存在，不存在则创建
            if (!checkFile.exists()) {
                checkFile.createNewFile();// 创建目标文件
            }
            // 三、向目标文件中写入内容
            // FileWriter(File file, boolean append)，append为true时为追加模式，false或缺省则为覆盖模式
//            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(checkFile));
//            bos.write();
            writer = new FileWriter(checkFile, true);
            long l = System.currentTimeMillis();
            SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String format = sd.format(l);
            writer.append(format+"  ");
            writer.append(player.getName()+"  ");
            if (kind.equals("chooseGoldExchange")) {
                writer.append("金币:-"+num+"  ");
                writer.append("剩余金币："+String.valueOf(player.getInventory().getCountOf(57))+"  ");
                writer.append("赞助币：-"+30+"  ");
                writer.append("剩余赞助币："+String.valueOf(player.getInventory().getCountOf(29520))+"  ");
            }else {
                writer.append("金币:+"+num+"  ");
                writer.append("剩余金币："+String.valueOf(player.getInventory().getCountOf(57))+"  ");
                writer.append("赞助币：+"+30+"  ");
                writer.append("剩余赞助币："+String.valueOf(player.getInventory().getCountOf(29520))+"  ");
            }
            writer.append("\r\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != writer)
                writer.close();
        }
    }

}
