package npc.model;

import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.model.CommandChannel;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Reflection;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.ReflectionUtils;

import instances.BalthusKnightZaken;
import instances.Ignis;
import instances.IgnisExtreme;
import instances.Nebula;
import instances.NebulaExtreme;
import instances.Petram;
import instances.PetramExtreme;
import instances.Procella;
import instances.ProcellaExtreme;

/**
 * @author Bonux (bonuxq@gmail.com)
 * 09.02.2019
 * Developed for L2-Scripts.com
 **/
public class FourBossManagerInstance extends NpcInstance{

    private static final int INSTANTZONE_ID_Ignis = 195;
    private static final int INSTANTZONE_ID_Nebula = 196;
    private static final int INSTANTZONE_ID_Procella = 197;
    private static final int INSTANTZONE_ID_Petram = 198;
    
    private static final int INSTANTZONE_ID_Ignis_Extreme = 201;
    private static final int INSTANTZONE_ID_Nebula_Extreme = 202;
    private static final int INSTANTZONE_ID_Procella_Extreme = 203;
    private static final int INSTANTZONE_ID_Petram_Extreme = 204;
    
    public FourBossManagerInstance(int objectId, NpcTemplate template, MultiValueSet<String> set)
    {
        super(objectId, template, set);
    }

    @Override
    public void onMenuSelect(Player player, int ask, long reply, int state)
    {
    	if(!player.isInParty())
    	{
    		showChatWindow(player, "default/" + getNpcId() + "-2.htm", false);
    		 return;
    	}
        if(ask == -12345)
        {
            if(reply == 195)
            {
                Reflection r = player.getActiveReflection();
                if(r != null)
                {
                    if(player.canReenterInstance(INSTANTZONE_ID_Ignis))
                        player.teleToLocation(r.getTeleportLoc(), r);
                    return;
                }
                if(player.canEnterInstance(INSTANTZONE_ID_Ignis))
                {
                    ReflectionUtils.enterReflection(player, new Ignis(), INSTANTZONE_ID_Ignis);
                    return;
                }
            }
            else if(reply == 196)
            {
                Reflection r = player.getActiveReflection();
                if(r != null)
                {
                    if(player.canReenterInstance(INSTANTZONE_ID_Nebula))
                        player.teleToLocation(r.getTeleportLoc(), r);
                    return;
                }
                if(player.canEnterInstance(INSTANTZONE_ID_Nebula))
                {
                    ReflectionUtils.enterReflection(player, new Nebula(), INSTANTZONE_ID_Nebula);
                    return;
                }
            }
            else if(reply == 197)
            {
                Reflection r = player.getActiveReflection();
                if(r != null)
                {
                    if(player.canReenterInstance(INSTANTZONE_ID_Procella))
                        player.teleToLocation(r.getTeleportLoc(), r);
                    return;
                }
                if(player.canEnterInstance(INSTANTZONE_ID_Procella))
                {
                    ReflectionUtils.enterReflection(player, new Procella(), INSTANTZONE_ID_Procella);
                    return;
                }
            }
            else if(reply == 198)
            {
                Reflection r = player.getActiveReflection();
                if(r != null)
                {
                    if(player.canReenterInstance(INSTANTZONE_ID_Petram))
                        player.teleToLocation(r.getTeleportLoc(), r);
                    return;
                }
                if(player.canEnterInstance(INSTANTZONE_ID_Petram))
                {
                    ReflectionUtils.enterReflection(player, new Petram(), INSTANTZONE_ID_Petram);
                    return;
                }
            }
            else if(reply == 201)
            {
                Reflection r = player.getActiveReflection();
                if(r != null)
                {
                    if(player.canReenterInstance(INSTANTZONE_ID_Ignis_Extreme))
                        player.teleToLocation(r.getTeleportLoc(), r);
                    return;
                }
                if(player.canEnterInstance(INSTANTZONE_ID_Ignis_Extreme))
                {
                    ReflectionUtils.enterReflection(player, new IgnisExtreme(), INSTANTZONE_ID_Ignis_Extreme);
                    return;
                }
            }
            else if(reply == 202)
            {
                Reflection r = player.getActiveReflection();
                if(r != null)
                {
                    if(player.canReenterInstance(INSTANTZONE_ID_Nebula_Extreme))
                        player.teleToLocation(r.getTeleportLoc(), r);
                    return;
                }
                if(player.canEnterInstance(INSTANTZONE_ID_Nebula_Extreme))
                {
                    ReflectionUtils.enterReflection(player, new NebulaExtreme(), INSTANTZONE_ID_Nebula_Extreme);
                    return;
                }
            }
            else if(reply == 203)
            {
                Reflection r = player.getActiveReflection();
                if(r != null)
                {
                    if(player.canReenterInstance(INSTANTZONE_ID_Procella_Extreme))
                        player.teleToLocation(r.getTeleportLoc(), r);
                    return;
                }
                if(player.canEnterInstance(INSTANTZONE_ID_Procella_Extreme))
                {
                    ReflectionUtils.enterReflection(player, new ProcellaExtreme(), INSTANTZONE_ID_Procella_Extreme);
                    return;
                }
            }
            else if(reply == 204)
            {
                Reflection r = player.getActiveReflection();
                if(r != null)
                {
                    if(player.canReenterInstance(INSTANTZONE_ID_Petram_Extreme))
                        player.teleToLocation(r.getTeleportLoc(), r);
                    return;
                }
                if(player.canEnterInstance(INSTANTZONE_ID_Petram_Extreme))
                {
                    ReflectionUtils.enterReflection(player, new PetramExtreme(), INSTANTZONE_ID_Petram_Extreme);
                    return;
                }
            }
        }
        else
            super.onMenuSelect(player, ask, reply, state);
    }
}
