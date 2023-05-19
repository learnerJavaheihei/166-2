package l2s.gameserver.botscript;

import l2s.gameserver.core.BotConfig;
import l2s.gameserver.core.BotEngine;
import l2s.gameserver.core.BotProperties;
import l2s.gameserver.core.IBotRuntimeChecker;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;

public class BotRuntimeChecker
implements IBotRuntimeChecker {
    @Override
    public boolean test(Player actor) {
        BotConfig config = BotEngine.getInstance().getBotConfig(actor);
        if (!actor.hasPremiumAccount() && BotProperties.ONLY_VIP && actor.getLevel() > 52 && actor.getInventory().getCountOf(70343) < 1) 
		{
            config.setAbort(true, "\u50c5\u9650\u7b49\u7d1a\u5c0f\u65bc52\u7d1a\u7684\u73a9\u5bb6\u6216vip\u7528\u6236\u4f7f\u7528\u3002");
		/*\u4ec5\u9650\u7b49\u7ea7\u5c0f\u4e8e\u0037\u0036\u7ea7\u7684\u73a9\u5bb6\u6216\u0056\u0049\u0050\u4f1a\u5458\u4f7f\u7528 仅限等级小于52级的玩家或VIP会员使用*/
        }
        /* if (actor.getPvpFlag() != 0) 
		{
            config.setAbort(true, "\u65e0\u6cd5\u5728\u7d2b\u540d\u72b6\u6001\u4e0b\u4f7f\u7528\u6302\u673a\u529f\u80fd");
		//\u65e0\u6cd5\u5728\u7d2b\u540d\u72b6\u6001\u4e0b\u4f7f\u7528\u6302\u673a\u529f\u80fd 无法在紫名状态下使用挂机功能
        } */
		if (actor.getKarma() != 0) 
		{
            config.setAbort(true, "\u65e0\u6cd5\u5728\u7ea2\u540d\u72b6\u6001\u4e0b\u4f7f\u7528\u6302\u673a\u529f\u80fd");
		/*\u65e0\u6cd5\u5728\u7ea2\u540d\u72b6\u6001\u4e0b\u4f7f\u7528\u6302\u673a\u529f\u80fd 无法在红名状态下使用挂机功能*/
        }
        /* if (actor.isInSiegeZone() || actor.isInZone(Zone.ZoneType.battle_zone)) {
            config.setAbort(true, "\u65e0\u6cd5\u5728\u653b\u57ce\u3001PvP\u5730\u533a\u4f7f\u7528\u6302\u673a\u529f\u80fd");
		\u65e0\u6cd5\u5728\u653b\u57ce\u3001PvP\u5730\u533a\u4f7f\u7528\u6302\u673a\u529f\u80fd 无法在攻城、PvP地区使用挂机功能
        } */
        if (actor.isInOlympiadMode()) {
            config.setAbort(true, "\u65e0\u6cd5\u5728\u5965\u8d5b\u8fc7\u7a0b\u4e2d\u4f7f\u7528\u6302\u673a\u529f\u80fd");
		/*\u65e0\u6cd5\u5728\u5965\u8d5b\u8fc7\u7a0b\u4e2d\u4f7f\u7528\u6302\u673a\u529f\u80fd 无法在奥赛过程中使用挂机功能*/
        }
		if (actor.getReflectionId() > 0) {
		config.setAbort(true, "\u7121\u6cd5\u5728\u5373\u6642\u5730\u5340\u4f7f\u7528\u639b\u6a5f\u529f\u80fd");
		/*\u65e0\u6cd5\u5728\u5965\u8d5b\u8fc7\u7a0b\u4e2d\u4f7f\u7528\u6302\u673a\u529f\u80fd 我發在即時地區使用掛機功能*/
        }
		/*--PVP活动无法启动*/
        if (actor.isDead()) {
            config.setDeathTime(config.getDeathTime() + 1);
            if (config.getDeathTime() >= 300) {
                config.setAbort(true, "\u4f60\u6302\u4e86");
				/*\u4f60\u6302\u4e86 你挂了*/
            }
            return config.isAbort();
        }
        if (config.getDeathTime() > 0) {
            config.setDeathTime(0);
        }
        if (GameObjectsStorage.getPlayer(actor.getObjectId()) == null) {
            config.setAbort(true, "NONE");
        }
        if (actor.isInOfflineMode()) {
            config.setAbort(true, "NONE");
        }
        return config.isAbort();
    }
}