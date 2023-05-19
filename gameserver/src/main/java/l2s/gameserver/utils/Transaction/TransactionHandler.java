package l2s.gameserver.utils.Transaction;


import l2s.gameserver.botscript.voicehandler.BotCallOut;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.handler.bypass.BypassHolder;
import l2s.gameserver.handler.voicecommands.IVoicedCommandHandler;
import l2s.gameserver.handler.voicecommands.VoicedCommandHandler;


import java.lang.reflect.Method;

public class TransactionHandler {

   public static Class<?>[] BYPASS_CLAZ = new Class[] {TransactionBank.class};

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
        VoicedCommandHandler.getInstance().registerVoicedCommandHandler((IVoicedCommandHandler)new BotCallOut());
    }

}
