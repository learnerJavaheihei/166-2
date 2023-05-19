package l2s.gameserver.botscript;

import l2s.gameserver.botscript.actionhandler.BotAbsorbBody;
import l2s.gameserver.botscript.actionhandler.BotAttack;
import l2s.gameserver.botscript.actionhandler.BotFollow;
import l2s.gameserver.botscript.actionhandler.BotFollowAttack;
import l2s.gameserver.botscript.actionhandler.BotHeal;
import l2s.gameserver.botscript.actionhandler.BotHpMpShift;
import l2s.gameserver.botscript.actionhandler.BotInvitePartner;
import l2s.gameserver.botscript.actionhandler.BotPickUpItem;
import l2s.gameserver.botscript.actionhandler.BotRes;
import l2s.gameserver.botscript.actionhandler.BotRest;
import l2s.gameserver.botscript.actionhandler.BotSpoilMob;
import l2s.gameserver.botscript.actionhandler.BotSummon;
import l2s.gameserver.botscript.actionhandler.BotSupport;
import l2s.gameserver.botscript.actionhandler.BotSweepMob;
import l2s.gameserver.botscript.bypasshandler.BotBuffManager;
import l2s.gameserver.botscript.bypasshandler.BotConfigSet;
import l2s.gameserver.botscript.bypasshandler.BotPage;
import l2s.gameserver.botscript.bypasshandler.BotPartyManager;
import l2s.gameserver.botscript.bypasshandler.BotPathVisualize;
import l2s.gameserver.botscript.bypasshandler.BotStrategyEdit;
import l2s.gameserver.botscript.bypasshandler.BotSwitchBypass;
import l2s.gameserver.botscript.voicehandler.BotCallOut;
import l2s.gameserver.core.BotActionHandler;
import l2s.gameserver.core.IBotActionHandler;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.handler.bypass.BypassHolder;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.handler.voicecommands.VoicedCommandHandler;

public class BotScriptsLoader {
    public static Class<? extends IBotActionHandler>[] ACTION_CLAZ = new Class[]{BotInvitePartner.class, BotPickUpItem.class, BotHpMpShift.class, BotAbsorbBody.class, BotSpoilMob.class, BotSweepMob.class, BotRes.class, BotHeal.class, BotSupport.class, BotSummon.class, BotRest.class, BotFollowAttack.class, BotAttack.class, BotFollow.class};
    public static Class<?>[] BYPASS_CLAZ = new Class[]{BotSwitchBypass.class, BotConfigSet.class, BotPage.class, BotStrategyEdit.class, BotPathVisualize.class, BotBuffManager.class, BotPartyManager.class,};

    public static void load() {
        for (Class<?> claz : BYPASS_CLAZ) {
            Method[] methods = claz.getDeclaredMethods();
            Object o = null;
            for (Method method : methods) {
                if (o == null) {
                    try {
                        o = claz.newInstance();
                    }
                    catch (IllegalAccessException | InstantiationException e) {
                        e.printStackTrace();
                    }
                }
                if (!method.isAnnotationPresent(Bypass.class)) continue;
                Bypass bypass = method.getAnnotation(Bypass.class);
                BypassHolder.getInstance().registerBypass(bypass.value(), o, method);
            }
        }
        for (int i = 0; i < ACTION_CLAZ.length; ++i) {
            try {
                BotActionHandler.getInstance().regHandler(i, ACTION_CLAZ[i].newInstance());
                continue;
            }
            catch (IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        }
        VoicedCommandHandler.getInstance().registerVoicedCommandHandler((IVoicedCommandHandler)new BotCallOut());
    }
}