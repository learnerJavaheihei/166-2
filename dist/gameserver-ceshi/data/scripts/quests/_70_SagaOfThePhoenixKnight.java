
import l2s.commons.util.Rnd;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.listener.actor.ai.OnAiEventListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ClassLevel;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;

//SanyaDC

public class _70_SagaOfThePhoenixKnight extends Quest {
    private class ArhontListener implements OnAiEventListener {
        private final QuestState _qs;

        public ArhontListener(QuestState qs) {
            _qs = qs;
        }

        @Override
        public void onAiEvent(Creature actor, CtrlEvent evt, Object[] args) {
            if (actor.getNpcId() == ARHONT_HALIWI && evt == CtrlEvent.EVT_DESPAWN) {
                if (_qs.getCond() == 13) {
                    _qs.setCond(12);
                    long consumeItemsCount = _qs.getQuestItemsCount(ZNAK_HALIWI) - 690; // Откатываем до 690 итемов.
                    if (consumeItemsCount > 0)
                        _qs.takeItems(ZNAK_HALIWI, consumeItemsCount);
                }
            }
        }
    }
    public final int SEDRIK = 30849;
    public final int FELIX = 31277;
    public final int RIFKEN = 34268;
    public final int ERIKRAMSHART = 31631;
    public final int KAMEN_POZNANIA1 = 31646;
    public final int KAMEN_POZNANIA2 = 31647;
    public final int KAMEN_POZNANIA3 = 31651;
    public final int KAMEN_POZNANIA4 = 31654;
    //mobs
    public final int LEDANOI_MONSTR = 27316;
    public final int DUH_UTOPL = 27317;
    public final int DUWA_HOLODA = 27318;
    public final int PRIZ_ODINOCHESTVA = 27319;
    public final int CHUDIWE_HOLODA = 27320;
    public final int DUH_HOLODA = 27321;
    public final int SMOTRITEL_TOPI = 21650;
    public final int PULAYWIY_DREIK = 21651;
    public final int PLAMENNIY_IFRIT = 21652;
    public final int IKEDIT = 21653;
    public final int HRANZAPRZNAN = 27214;
    public final int PADWRICADHIL = 27286;
    public final int ARHONT_HALIWI = 27219;
    public final int TENBELEFA = 27278;
    public static final String A_LIST = "a_list";
    public static final String B_LIST = "a_list";
    public static final String C_LIST = "a_list";
    public static final String D_LIST = "a_list";
    //items
    public final int BOOKGOLDLION = 90038;
    public final int OSKOLOK_KRI_HOLODA = 49804;
    public final int ZNAK_HALIWI = 7485;
    public final int AMULET_REZONANSA_PERVIY = 7268;
    public final int AMULET_REZONANSA_VTOROI = 7299;
    public final int AMULET_REZONANSA_TRETIY = 7330;
    public final int AMULET_REZONANSA_CHETVERTIY = 7361;

    //	# [MOB_ID, REQUIRED, ITEM, NEED_COUNT, CHANCE]
    public final int[][] DROPLIST =
            {
                    {LEDANOI_MONSTR, OSKOLOK_KRI_HOLODA, 50, 100},
                    {DUH_UTOPL, OSKOLOK_KRI_HOLODA, 50, 100},
                    {DUWA_HOLODA, OSKOLOK_KRI_HOLODA, 50, 100},
                    {PRIZ_ODINOCHESTVA, OSKOLOK_KRI_HOLODA, 50, 100},
                    {CHUDIWE_HOLODA, OSKOLOK_KRI_HOLODA, 50, 100},
                    {DUH_HOLODA, OSKOLOK_KRI_HOLODA, 50, 100},
                    {SMOTRITEL_TOPI, ZNAK_HALIWI, 700, 100},
                    {PULAYWIY_DREIK, ZNAK_HALIWI, 700, 100},
                    {IKEDIT, ZNAK_HALIWI, 700, 100},
                    {PLAMENNIY_IFRIT, ZNAK_HALIWI, 700, 100}};

    public _70_SagaOfThePhoenixKnight() {
        super(PARTY_NONE, ONETIME);

        addStartNpc(SEDRIK);
        addTalkId(FELIX);
        addTalkId(RIFKEN);
        addTalkId(ERIKRAMSHART);
        addTalkId(KAMEN_POZNANIA1);
        addTalkId(KAMEN_POZNANIA2);
        addTalkId(KAMEN_POZNANIA3);
        addTalkId(KAMEN_POZNANIA4);
        addQuestItem(OSKOLOK_KRI_HOLODA);
        addQuestItem(ZNAK_HALIWI);
        addKillNpcWithLog(7, A_LIST, 20, 27214);
        addKillNpcWithLog(9, B_LIST, 1, 27286);
        addKillNpcWithLog(13, C_LIST, 1, ARHONT_HALIWI);
        addKillNpcWithLog(16, D_LIST, 1, 27278);


        for (int[] element : DROPLIST)
            addKillId(element[0]);

        addQuestItem(new int[]
                {OSKOLOK_KRI_HOLODA, ZNAK_HALIWI});


        addLevelCheck("sedrik_q70_02.htm", 76);
        addClassIdCheck("sedrik_q70_03.htm", 5);
    }

    @Override
    public String onEvent(String event, QuestState st, NpcInstance npc) {
        String htmltext = event;
        if (event.equalsIgnoreCase("sedrik_q70_02a.htm")) {
            htmltext = "sedrik_q70_5.htm";
        } else if (event.equalsIgnoreCase("sedrik_q70_01s.htm")) {
            if (st.getCond() == 0)
                st.setCond(1);
        } else if (event.equalsIgnoreCase("felix_q70_2.htm")) {
            if (st.getCond() == 1)
                st.setCond(2);
        } else if (event.equalsIgnoreCase("rifken_q70_2.htm")) {
            if (st.getCond() == 2)
                st.setCond(3);
        } else if (event.equalsIgnoreCase("rifken_q70_4.htm")) {
            if (st.getCond() == 4)
                st.setCond(5);
            st.takeItems(OSKOLOK_KRI_HOLODA, -1);
        } else if (event.equalsIgnoreCase("felix_q70_4.htm")) {
            if (st.getCond() == 5)
                st.setCond(6);
            st.giveItems(AMULET_REZONANSA_PERVIY, 1);
        } else if (event.equalsIgnoreCase("stone12.htm")) {
            if (st.getCond() == 6)
                st.setCond(7);
        } else if (event.equalsIgnoreCase("stone22.htm")) {
            if (st.getCond() == 8) {
                st.addSpawn(PADWRICADHIL);
                st.setCond(9);
            }
        } else if (event.equalsIgnoreCase("stone25.htm")) {
            if (st.getCond() == 10)
                st.setCond(11);
        } else if (event.equalsIgnoreCase("felix_q70_6.htm")) {
            if (st.getCond() == 11)
                st.setCond(12);
        } else if (event.equalsIgnoreCase("stone32.htm")) {
            if (st.getCond() == 14)
                st.setCond(15);
        } else if (event.equalsIgnoreCase("stone41.htm")) {
            if (st.getCond() == 15)
                st.setCond(16);
            st.addSpawn(TENBELEFA);
        } else if (event.equalsIgnoreCase("erikrams2.htm")) {
            if (st.getCond() == 16)
                st.setCond(17);
            st.giveItems(AMULET_REZONANSA_CHETVERTIY, 1);

        } else if (event.equalsIgnoreCase("stone43.htm")) {
            if (st.getCond() == 17)
                st.setCond(18);


        } else if (event.equalsIgnoreCase("sedrik_q70_7.htm")) {
            if (st.getCond() == 18) {
                st.addExpAndSp(3100000, 103000);
                st.giveItems(BOOKGOLDLION, 1);
                st.takeItems(AMULET_REZONANSA_PERVIY, -1);
                st.takeItems(AMULET_REZONANSA_VTOROI, -1);
                st.takeItems(AMULET_REZONANSA_TRETIY, -1);
                st.takeItems(AMULET_REZONANSA_CHETVERTIY, -1);
                st.takeItems(ZNAK_HALIWI, -1);
                st.finishQuest();

                Player player = st.getPlayer();
                player.setClassId(ClassId.PHOENIX_KNIGHT.getId(), false);
                if (player.getBaseClassId() == ClassId.PALADIN.getId())
                    player.setClassId(ClassId.PHOENIX_KNIGHT.getId(), false);
                player.broadcastCharInfo();
                npc.broadcastPacket(new MagicSkillUse(npc, player, 5103, 1, 1000, 0));
            }
        }

        return htmltext;
    }

    @Override
    public String onTalk(NpcInstance npc, QuestState st) {
        String htmltext = "noquest";
        int npcId = npc.getNpcId();
        int cond = st.getCond();
        long squire = st.getQuestItemsCount(OSKOLOK_KRI_HOLODA);


        if (npcId == SEDRIK) {
            if (cond == 0) {
                if (st.getPlayer().getLevel() >= 76 && st.getPlayer().getClassId().getId() == 5)

                    htmltext = "sedrik_q70_01.htm";
                else
                    htmltext = "sedrik_q70_03.htm";
            }
            if (cond == 1) {
                htmltext = "sedrik_q70_01s.htm";
            }
            if (cond == 18) {
                htmltext = "sedrik_q70_6.htm";
            }

        }
        if (npcId == FELIX) {
            if (cond == 1) {
                htmltext = "felix_q70_1.htm";
            }
            if (cond == 2) {
                htmltext = "felix_q70_2.htm";
            }
            if (cond == 5) {
                htmltext = "felix_q70_3.htm";
            }
            if (cond == 6) {
                htmltext = "felix_q70_4.htm";
            }
            if (cond == 11) {
                htmltext = "felix_q70_5.htm";
            }
            if (cond == 12) {
                htmltext = "felix_q70_6.htm";
            }
        }
        if (npcId == RIFKEN) {
            if (cond == 2) {
                htmltext = "rifken_q70_1.htm";
            }
            if (cond == 3) {
                htmltext = "rifken_q70_2.htm";
            }
            if (cond == 4) {
                htmltext = "rifken_q70_3.htm";
            }
        }
        if (npcId == KAMEN_POZNANIA1) {
            if (cond == 6) {
                htmltext = "stone11.htm";
            }
            if (cond == 7) {
                htmltext = "stone12.htm";
            }
        }
        if (npcId == KAMEN_POZNANIA2) {
            if (cond == 8) {
                htmltext = "stone21.htm";
            }
            if (cond == 9) {
                htmltext = "stone23.htm";
            }
            if (cond == 10) {
                htmltext = "stone24.htm";
            }
            if (cond == 11) {
                htmltext = "stone25.htm";
            }
        }
        if (npcId == KAMEN_POZNANIA3) {
            if (cond == 14) {
                htmltext = "stone31.htm";
            }
            if (cond == 15) {
                htmltext = "stone32.htm";
            }
        }
        if (npcId == KAMEN_POZNANIA4) {
            if (cond == 15) {
                htmltext = "stone40.htm";
            }
            if (cond == 17) {
                htmltext = "stone42.htm";
            }
            if (cond == 18) {
                htmltext = "stone43.htm";
            }

        }
        if (npcId == ERIKRAMSHART) {
            if (cond == 16) {
                htmltext = "erikrams1.htm";
            }
            if (cond == 17) {
                htmltext = "erikrams2.htm";
            }

        }
        return htmltext;
    }

    @Override
    public String onKill(NpcInstance npc, QuestState qs) {
        int cond = qs.getCond();
        if (cond == 3) {
            if (qs.rollAndGive(OSKOLOK_KRI_HOLODA, 1, 1, 50, 100))
                qs.setCond(4);
        } else if (cond == 7) {
            if (updateKill(npc, qs)) {
                qs.unset(A_LIST);
                qs.giveItems(AMULET_REZONANSA_VTOROI, 1);
                qs.setCond(8);
            }
        } else if (cond == 9) {
            if (updateKill(npc, qs)) {
                qs.unset(B_LIST);
                qs.setCond(10);

            }
        } else if (cond == 12) {
            qs.rollAndGive(ZNAK_HALIWI, 1, 1, 700, 100);
            if (qs.getQuestItemsCount(ZNAK_HALIWI) >= 700) {
                qs.setCond(13);

                NpcInstance arhont = qs.addSpawn(ARHONT_HALIWI, 300000);
                if (arhont != null)
                    arhont.addListener(new ArhontListener(qs));
            }
        } else if (cond == 13) {
            if (updateKill(npc, qs)) {
                qs.unset(C_LIST);
                qs.giveItems(AMULET_REZONANSA_TRETIY, 1);
                qs.setCond(14);

            }
        } else if (cond == 16) {
            if (updateKill(npc, qs)) {
                qs.unset(D_LIST);
                qs.addSpawn(ERIKRAMSHART);

            }
        }


        return null;
    }
}