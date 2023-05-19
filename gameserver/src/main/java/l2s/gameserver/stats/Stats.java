package l2s.gameserver.stats;

import java.util.NoSuchElementException;
import l2s.gameserver.Config;

public enum Stats
{
	MAX_HP("maxHp", 0., Double.POSITIVE_INFINITY, 1.),//Hp最大值
	MAX_MP("maxMp", 0., Double.POSITIVE_INFINITY, 1.),//Mp最大值
	MAX_CP("maxCp", 0., Double.POSITIVE_INFINITY, 1.),//Cp最大值

	PLAYER_MAX_HP_LIMIT("max_hp_limit", 0., Double.POSITIVE_INFINITY, 1.),
	PLAYER_MAX_MP_LIMIT("max_mp_limit", 0., Double.POSITIVE_INFINITY, 1.),
	PLAYER_MAX_CP_LIMIT("max_cp_limit", 0., Double.POSITIVE_INFINITY, 1.),

	REGENERATE_HP_RATE("regHp"),//Hp額外恢復量
	REGENERATE_CP_RATE("regCp"),//Cp額外恢復量
	REGENERATE_MP_RATE("regMp"),//Mp額外恢復量

	// Для эффектов типа Seal of Limit
	HP_LIMIT("hpLimit", 1., 100., 100.),//恢復最多hp
	MP_LIMIT("mpLimit", 1., 100., 100.),//恢復最多mp
	CP_LIMIT("cpLimit", 1., 100., 100.),//恢復最多cp

	RUN_SPEED("runSpd"),//移動速度

	POWER_DEFENCE("pDef"),//p.防御力
	MAGIC_DEFENCE("mDef"),//m.防御力
	POWER_ATTACK("pAtk"),//p.攻擊力
	MAGIC_ATTACK("mAtk"),//m.攻擊力
	POWER_ATTACK_SPEED("pAtkSpd"),//攻擊速度
	MAGIC_ATTACK_SPEED("mAtkSpd"),//施法速度

	MAGIC_REUSE_RATE("mReuse"),//魔法技能冷卻時間
	PHYSIC_REUSE_RATE("pReuse"),//物理技能冷卻時間
	MUSIC_REUSE_RATE("musicReuse"),//詩歌/劍舞技能冷卻時間
	ATK_REUSE("atkReuse"),//攻擊速度冷卻時間
	BASE_P_ATK_SPD("basePAtkSpd"),//弓箭攻擊速度
	BASE_M_ATK_SPD("baseMAtkSpd"),

	P_EVASION_RATE("pEvasRate"),//p.迴避
	M_EVASION_RATE("mEvasRate"),//m.迴避
	P_ACCURACY_COMBAT("pAccCombat"),//p.命中
	M_ACCURACY_COMBAT("mAccCombat"),//m.命中

	BASE_P_CRITICAL_RATE("basePCritRate", 0., Double.POSITIVE_INFINITY), // 靜態致命機率 用於增加致命機率 例子: <add order="0x40" stat="baseCrit" val="27.4" />
	BASE_M_CRITICAL_RATE("baseMCritRate", 0., Double.POSITIVE_INFINITY), // 靜態魔法致命機率 用於增加魔法致命機率 例子: <add order="0x40" stat="baseMCritRate" val="27.4" />

	P_CRITICAL_RATE("pCritRate", 0., Double.POSITIVE_INFINITY, 100), // dynamic crit rate. Use it to MULTIPLE crit for 1.3, 1.5 etc. Sample: <add order="0x40" stat="rCrit" val="50" /> = (x1.5)
	M_CRITICAL_RATE("mCritRate", 0., Double.POSITIVE_INFINITY, 100),

	INFLICTS_P_DAMAGE_POWER("inflicts_p_damage_power"),//增加物理傷害
	INFLICTS_M_DAMAGE_POWER("inflicts_m_damage_power"),//增加魔法傷害
	RECEIVE_P_DAMAGE_POWER("receive_p_damage_power"),//固定物理傷害抗性
	RECEIVE_M_DAMAGE_POWER("receive_m_damage_power"),//固定魔法傷害抗性

	CAST_INTERRUPT("concentration", 0., 100.),//施法被中斷的傷害
	SHIELD_DEFENCE("sDef"),//盾牌防禦力
	SHIELD_RATE("rShld", 0., 100.),//盾牌防禦機率 skill id="153" levels="4" name="精通盾技
	SHIELD_ANGLE("shldAngle", 0., 360., 60.),//盾牌防禦方位

	POWER_ATTACK_RANGE("pAtkRange", 0., 1500.),//攻擊範圍
	MAGIC_ATTACK_RANGE("mAtkRange", 0., 1500.),//魔法攻擊範圍
	P_ATTACK_RADIUS("p_attack_radius", 0., 1500.),//額外增加一般攻擊距離
	POLE_ATTACK_ANGLE("poleAngle", 0., 180.),
	ATTACK_TARGETS_COUNT("attack_targets_count"),//攻擊目標數量
	POLE_TARGET_COUNT("poleTargetCount"),//槍攻擊目標數量

	STAT_STR("STR", 1., 100.),//STR基本能力
	STAT_CON("CON", 1., 100.),//CON基本能力
	STAT_DEX("DEX", 1., 100.),//DEX基本能力
	STAT_INT("INT", 1., 100.),//INT基本能力
	STAT_WIT("WIT", 1., 100.),//WIT基本能力
	STAT_MEN("MEN", 1., 100.),//MEN基本能力

	BREATH("breath"),//肺活量
	FALL("fall"),//高處墜落
	EXP_LOST("expLost"),//死亡EXP減少
	ITEMS_LOST_CHANCE("items_lost_chance", 0., 100.),//死亡物品掉落機率

	CANCEL_RESIST("cancelResist", -200., 300.),//輔助魔法消除攻擊的抗性
	MAGIC_RESIST("magicResist", -200., 300.),//魔法攻擊的抗性
	BLOW_RESIST("blow_resist", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//要害攻擊的防禦

	CANCEL_POWER("cancelPower", -200., 200.),
	MAGIC_POWER("magicPower", -200., 200.),
	BLOW_POWER("blow_power", -200., 200.),

	RESIST_ABNORMAL_BUFF("resist_abnormal_buff", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	RESIST_ABNORMAL_DEBUFF("resist_abnormal_debuff", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//詛咒魔法抗性

	FATALBLOW_RATE("blowRate", 0., 10., 1.),//要害攻擊成功率
	P_SKILL_CRITICAL_RATE("p_skill_critical_rate"),//感覺是物理技能致命機率？
	DEATH_VULNERABILITY("deathVuln", 10., 190., 100.),//要害攻擊的防禦能力

	P_CRIT_DAMAGE_RECEPTIVE("pCritDamRcpt", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 100.),//減少所受物理致命攻擊傷害
	M_CRIT_DAMAGE_RECEPTIVE("mCritDamRcpt", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//減少所受魔法致命攻擊傷害
	P_CRIT_CHANCE_RECEPTIVE("pCritChanceRcpt", 10., 190., 100.),//減少所受物理致命攻擊機率
	M_CRIT_CHANCE_RECEPTIVE("mCritChanceRcpt", 10., 190., 100.),//減少所受魔法致命攻擊機率

	DEFENCE_FIRE("defenceFire", -600, 600), //Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	DEFENCE_WATER("defenceWater", -600, 600), // Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	DEFENCE_WIND("defenceWind", -600, 600), // Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	DEFENCE_EARTH("defenceEarth", -600, 600), // Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	DEFENCE_HOLY("defenceHoly", -600, 600), // Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	DEFENCE_UNHOLY("defenceUnholy", -600, 600), // Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	BASE_ELEMENTS_DEFENCE("elements_defence", -600, 600), // Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),

	ATTACK_FIRE("attackFire", 0., Config.ELEMENT_ATTACK_LIMIT), // Double.POSITIVE_INFINITY),
	ATTACK_WATER("attackWater", 0., Config.ELEMENT_ATTACK_LIMIT), // Double.POSITIVE_INFINITY),
	ATTACK_WIND("attackWind", 0., Config.ELEMENT_ATTACK_LIMIT), // Double.POSITIVE_INFINITY),
	ATTACK_EARTH("attackEarth", 0., Config.ELEMENT_ATTACK_LIMIT), // Double.POSITIVE_INFINITY),
	ATTACK_HOLY("attackHoly", 0., Config.ELEMENT_ATTACK_LIMIT), // Double.POSITIVE_INFINITY),
	ATTACK_UNHOLY("attackUnholy", 0., Config.ELEMENT_ATTACK_LIMIT), // Double.POSITIVE_INFINITY),

	ABSORB_DAMAGE_PERCENT("absorbDam", 0., 100., 0.),
	ABSORB_BOW_DAMAGE_PERCENT("absorbBowDam", 0., 100., 0.),
	ABSORB_PSKILL_DAMAGE_PERCENT("absorbPSkillDam", 0., 100., 0.),
	ABSORB_MSKILL_DAMAGE_PERCENT("absorbMSkillDam", 0., 100., 0.),
	ABSORB_DAMAGEMP_PERCENT("absorbDamMp", 0., 100., 0.),
	ABSORB_DAMAGEMP_PERCENT_CHANCE("absorbDamMpChance", 0., 100., 0.),//受到傷害轉換傷害的%增加MP 技能ID250
	ABSORB_DAMAGEMP_PERCENT_TARGET("absorbDamMpTarget", 0., 100., 0.),//受到傷害轉換傷害的%增加MP 技能ID250
	TRANSFER_TO_SUMMON_DAMAGE_PERCENT("transferPetDam", 0., 100.),//部分傷害轉移給使魔
	TRANSFER_TO_EFFECTOR_DAMAGE_PERCENT("transferToEffectorDam", 0., 100.),//代受隊伍成員所遭受的傷害
	TRANSFER_TO_MP_DAMAGE_PERCENT("p_mp_shield", 0., 100.),//傷害轉移到消耗MP

	// Отражение урона с шансом. Урон получает только атакующий.
	REFLECT_AND_BLOCK_DAMAGE_CHANCE("reflectAndBlockDam", 0., Config.REFLECT_AND_BLOCK_DAMAGE_CHANCE_CAP), // 將所受的傷害XX%反射給攻擊者
	REFLECT_AND_BLOCK_PSKILL_DAMAGE_CHANCE("reflectAndBlockPSkillDam", 0., Config.REFLECT_AND_BLOCK_PSKILL_DAMAGE_CHANCE_CAP), // Ближний урон скиллами 反射物理技能傷害
	REFLECT_AND_BLOCK_MSKILL_DAMAGE_CHANCE("reflectAndBlockMSkillDam", 0., Config.REFLECT_AND_BLOCK_MSKILL_DAMAGE_CHANCE_CAP), // Любой урон магией 反射魔法技能傷害

	// Отражение урона в процентах. Урон получает и атакующий и цель
	REFLECT_DAMAGE_PERCENT("reflectDam", 0., Config.REFLECT_DAMAGE_PERCENT_CAP), // Ближний урон без скиллов 反射物理傷害
	REFLECT_BOW_DAMAGE_PERCENT("reflectBowDam", 0., Config.REFLECT_BOW_DAMAGE_PERCENT_CAP), // Урон луком без скиллов 反射弓傷害
	REFLECT_PSKILL_DAMAGE_PERCENT("reflectPSkillDam", 0., Config.REFLECT_PSKILL_DAMAGE_PERCENT_CAP), // Ближний урон скиллами 反射物理技能傷害
	REFLECT_MSKILL_DAMAGE_PERCENT("reflectMSkillDam", 0., Config.REFLECT_MSKILL_DAMAGE_PERCENT_CAP), // Любой урон магией 反射魔法技能傷害

	REFLECT_PHYSIC_SKILL("reflectPhysicSkill", 0., 60.),//反射物理技能？
	REFLECT_MAGIC_SKILL("reflectMagicSkill", 0., 60.),//反射魔法技能？

	REFLECT_PHYSIC_DEBUFF("reflectPhysicDebuff", 0., 60.),//反射物理型輔助魔法/詛咒魔法
	REFLECT_MAGIC_DEBUFF("reflectMagicDebuff", 0., 60.),//反射魔法型輔助魔法/詛咒魔法

	P_SKILL_EVASION("pSkillEvasion", 100., 200.),//物理技能的迴避率
	M_SKILL_EVASION("mSkillEvasion", 100., 200.),//魔法技能的迴避率
	COUNTER_ATTACK("counterAttack", 0., 100.),//受到的近距離物理攻擊技能傷害反射給對方

	P_SKILL_POWER("p_skill_power"),//物理技能威力
	P_SKILL_POWER_STATIC("pSkillPowerStatic"),
	M_SKILL_POWER("mSkillPower"),//魔法技能威力
	CHARGED_P_SKILL_POWER("charged_p_skill_power"),//增加消耗鬥力技能的威力

	// PvP Dmg bonus
	PVP_PHYS_DMG_BONUS("pvpPhysDmgBonus"),
	PVP_PHYS_SKILL_DMG_BONUS("pvpPhysSkillDmgBonus"),
	PVP_MAGIC_SKILL_DMG_BONUS("pvpMagicSkillDmgBonus"),
	// PvP Def bonus
	PVP_PHYS_DEFENCE_BONUS("pvpPhysDefenceBonus"),
	PVP_PHYS_SKILL_DEFENCE_BONUS("pvpPhysSkillDefenceBonus"),
	PVP_MAGIC_SKILL_DEFENCE_BONUS("pvpMagicSkillDefenceBonus"),

	// PvE Dmg bonus
	PVE_PHYS_DMG_BONUS("pvePhysDmgBonus"),
	PVE_PHYS_SKILL_DMG_BONUS("pvePhysSkillDmgBonus"),
	PVE_MAGIC_SKILL_DMG_BONUS("pveMagicSkillDmgBonus"),
	// PvE Def bonus
	PVE_PHYS_DEFENCE_BONUS("pvePhysDefenceBonus"),
	PVE_PHYS_SKILL_DEFENCE_BONUS("pvePhysSkillDefenceBonus"),
	PVE_MAGIC_SKILL_DEFENCE_BONUS("pveMagicSkillDefenceBonus"),

	MANAHEAL_EFFECTIVNESS("mpEff", 0., 1000.),
	CPHEAL_EFFECTIVNESS("cpEff", 0., 1000.),

	MP_MAGIC_SKILL_CONSUME("mpConsum"),//魔法技能的MP消耗量
	MP_PHYSICAL_SKILL_CONSUME("mpConsumePhysical"),//物理技能的MP消耗量
	MP_DANCE_SKILL_CONSUME("mpDanceConsume"),//劍舞技能的MP消耗量

	CHEAP_SHOT("cheap_shot"),//不消耗MP的狀態下，也能使用弓箭

	MAX_LOAD("maxLoad"),
	MAX_NO_PENALTY_LOAD("maxNoPenaltyLoad"),//負重懲罰上限
	INVENTORY_LIMIT("inventoryLimit"),//擴充道具
	STORAGE_LIMIT("storageLimit"),//擴充個人倉庫
	TRADE_LIMIT("tradeLimit"),//擴充個人商店
	COMMON_RECIPE_LIMIT("CommonRecipeLimit"),
	DWARVEN_RECIPE_LIMIT("DwarvenRecipeLimit"),
	BUFF_LIMIT("buffLimit"),//提升個人所能接受的輔助魔法狀態上限
	TALISMANS_LIMIT("talismansLimit", 0., 6.),//可裝備X個護身符
	JEWELS_LIMIT("jewels_limit", 0., 6.),//可裝備X個寶石
	ACTIVE_AGATHION_MAIN_SLOT("active_agathion_main_slot", 0., 1.),//精代表欄位開放
	SUB_AGATHIONS_LIMIT("sub_agathions_limit", 0., 4.),//壺精輔助欄位擴增
	CUBICS_LIMIT("cubicsLimit", 0., 3., 1.),//可同時召喚X個晶體
	MAX_INCREASED_FORCE("max_increased_force"),//最大力量數值最高可達階段 鬥力

	GRADE_EXPERTISE_LEVEL("gradeExpertiseLevel"),//自己的等級懲罰減少X級。
	EXP_RATE_MULTIPLIER("exp_rate_multiplier"),
	FISHING_EXP_MULTIPLIER("fishing_exp_multiplier"),//釣魚技能EXP/SP實裝
	FISHING_SP_MULTIPLIER("fishing_sp_multiplier"),//釣魚技能EXP/SP實裝
	SP_RATE_MULTIPLIER("sp_rate_multiplier"),
	ADENA_RATE_MULTIPLIER("adena_rate_multiplier"),//金幣掉落倍數
	DROP_RATE_MULTIPLIER("drop_rate_multiplier"),//物品掉落倍數
	SPOIL_RATE_MULTIPLIER("spoil_rate_multiplier"),//回收倍數

	DROP_CHANCE_MODIFIER("drop_chance_modifier"),//掉落機率
	DROP_COUNT_MODIFIER("drop_count_modifier"),//掉落數量
	SPOIL_CHANCE_MODIFIER("spoil_chance_modifier"),//回收機率
	SPOIL_COUNT_MODIFIER("spoil_count_modifier"),//回收數量

	SKILLS_ELEMENT_ID("skills_element_id", -1., 100., -1.),
	DAMAGE_AGGRO_PERCENT("damageAggroPercent", 0., 300., 0.),
	RECIEVE_DAMAGE_LIMIT("recieveDamageLimit", -1, Double.POSITIVE_INFINITY, -1),//只會受到多少普通攻擊傷害
	RECIEVE_DAMAGE_LIMIT_P_SKILL("recieveDamageLimitPSkill", -1, Double.POSITIVE_INFINITY, -1),//只會受到多少物理技能攻擊傷害
	RECIEVE_DAMAGE_LIMIT_M_SKILL("recieveDamageLimitMSkill", -1, Double.POSITIVE_INFINITY, -1),//只會受到多少魔法技能攻擊傷害
	KILL_AND_RESTORE_HP("killAndRestoreHp", 0., 100., 0.),//殺敵恢復HP%？
	RESIST_REFLECT_DAM("resistRelectDam", 0., 100., 0.),//傷害反射抗性
	
	BUFF_TIME_MODIFIER("buff_time_modifier", 1., Double.POSITIVE_INFINITY, 1.),//增加良性魔法的持續時間
	DEBUFF_TIME_MODIFIER("debuff_time_modifier", 1., Double.POSITIVE_INFINITY, 1.),//增加負面魔法的持續時間

	P_SKILL_CRIT_RATE_DEX_DEPENDENCE("p_skill_crit_rate_dex_dependence", 0., 1., 0.),//根據DEX，將會變更物理技能的致命攻擊率
	SPEED_ON_DEX_DEPENDENCE("speed_on_dex_dependence", 0., 1., 0.),//根據DEX，將會變更移動速度

	ENCHANT_CHANCE_MODIFIER("enchant_chance_modifier", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.),//增加強化成功機率

	SOULSHOT_POWER("soulshot_power", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	SPIRITSHOT_POWER("spiritshot_power", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),

	DAMAGE_BLOCK_RADIUS("damage_block_radius", -1., Double.POSITIVE_INFINITY, -1.),//阻攔傷害範圍

	DAMAGE_BLOCK_COUNT("damage_block_count", 0., Double.POSITIVE_INFINITY),//產生可吸收傷害XXX的保護膜

	DAMAGE_HATE_BONUS("DAMAGE_HATE_BONUS"),//激起攻擊慾望的狀態

	ShillienProtection("shillienProtection", 0, 1, 0),//為選擇的隊員佈下保護幕。在受到致命攻擊的情況下恢復CP/HP100%。發動效果時會刪除輔助魔法。
	SacrificialSoul("sacrificialSoul", 0, 1, 0),
	RestoreHPGiveDamage("restoreHPGiveDamage", 0, 1, 0),//受到一定程度以上的攻擊時，會機率性的恢復HP最大值的10%
	MarkOfTrick("MarkOfTrick", 0, 1, 0),//妨礙目標施展技能
	DivinityOfEinhasad("DivinityOfEinhasad", 0, 1, 0),//在30秒內減少魔法技能消耗MP30%，「療癒之光」、「再生之光」、「治癒光輝」技能使用時，100%發動致命攻擊。消耗5個魔精石。
	BlockFly("blockFly", 0, 1, 0),//阻絕使其無法使用移動技術

	P_CRIT_RATE_LIMIT("p_crit_rate_limit"),//提升物理致命攻擊最大值  好像是可以突破極限？

	ADDITIONAL_EXPERTISE_INDEX("additional_expertise_index"),//在道具清單持有期間擁有可裝備X級道具而不受等級懲罰的能力。1、D 2、C 3、B 4、A 5、S 6、S80 7、S84 8、R 9、R95 10、R99

	PHYSICAL_ABNORMAL_RESIST("p_physical_abnormal_resist", -100., 100.),//增加肉體系抗性
	MAGIC_ABNORMAL_RESIST("p_magic_abnormal_resist", -100., 100.),//增加精神系抗性

	WORLD_CHAT_POINTS("world_chat_points"),//世界聊天點數
	CRAFT_CHANCE_BONUS("craft_chance_bonus", 0., 100., 0.),//53002製作裝備加成
	CRAFT_CRITICAL_POWER("craft_critical_power", 0., 100., 0.),//53001製作裝備 爆擊的機率
	FIRE_ELEMENTAL_EXP_RATE("fire_elemental_exp_rate"),
	WATER_ELEMENTAL_EXP_RATE("water_elemental_exp_rate"),
	WIND_ELEMENTAL_EXP_RATE("wind_elemental_exp_rate"),
	EARTH_ELEMENTAL_EXP_RATE("earth_elemental_exp_rate"),

	FIRE_ELEMENTAL_ATTACK("fire_elemental_attack"),
	WATER_ELEMENTAL_ATTACK("water_elemental_attack"),
	WIND_ELEMENTAL_ATTACK("wind_elemental_attack"),
	EARTH_ELEMENTAL_ATTACK("earth_elemental_attack"),

	FIRE_ELEMENTAL_DEFENCE("fire_elemental_defence"),
	WATER_ELEMENTAL_DEFENCE("water_elemental_defence"),
	WIND_ELEMENTAL_DEFENCE("wind_elemental_defence"),
	EARTH_ELEMENTAL_DEFENCE("earth_elemental_defence"),

	FIRE_ELEMENTAL_CRIT_RATE("fire_elemental_crit_rate"),
	WATER_ELEMENTAL_CRIT_RATE("water_elemental_crit_rate"),
	WIND_ELEMENTAL_CRIT_RATE("wind_elemental_crit_rate"),
	EARTH_ELEMENTAL_CRIT_RATE("earth_elemental_crit_rate"),

	FIRE_ELEMENTAL_CRIT_ATTACK("fire_elemental_crit_attack"),
	WATER_ELEMENTAL_CRIT_ATTACK("water_elemental_crit_attack"),
	WIND_ELEMENTAL_CRIT_ATTACK("wind_elemental_crit_attack"),
	EARTH_ELEMENTAL_CRIT_ATTACK("earth_elemental_crit_attack"),

	// Stats from PTS server-pack
	CRITICAL_DAMAGE("critical_damage", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//致命傷害
	MAGIC_CRITICAL_DMG("magic_critical_dmg", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//魔法致命傷害
	SKILL_CRITICAL_DAMAGE("skill_critical_damage", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//技能致命傷害？
	HEAL_EFFECT("heal_effect", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),

	ATTACK_TRAIT_SWORD("attack_trait_sword", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//劍
	DEFENCE_TRAIT_SWORD("defence_trait_sword", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_BLUNT("attack_trait_blunt", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//鈍
	DEFENCE_TRAIT_BLUNT("defence_trait_blunt", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_DAGGER("attack_trait_dagger", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//匕首
	DEFENCE_TRAIT_DAGGER("defence_trait_dagger", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_POLE("attack_trait_pole", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//槍
	DEFENCE_TRAIT_POLE("defence_trait_pole", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_FIST("attack_trait_fist", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//魔劍
	DEFENCE_TRAIT_FIST("defence_trait_fist", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_BOW("attack_trait_bow", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//弓
	DEFENCE_TRAIT_BOW("defence_trait_bow", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_ETC("attack_trait_etc", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//大頭？
	DEFENCE_TRAIT_ETC("defence_trait_etc", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_POISON("attack_trait_poison", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//毒
	DEFENCE_TRAIT_POISON("defence_trait_poison", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_HOLD("attack_trait_hold", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//束縛
	DEFENCE_TRAIT_HOLD("defence_trait_hold", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_BLEED("attack_trait_bleed", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//出血
	DEFENCE_TRAIT_BLEED("defence_trait_bleed", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_SLEEP("attack_trait_sleep", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//睡眠
	DEFENCE_TRAIT_SLEEP("defence_trait_sleep", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_SHOCK("attack_trait_shock", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//衝擊
	DEFENCE_TRAIT_SHOCK("defence_trait_shock", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_DERANGEMENT("attack_trait_derangement", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//精神
	DEFENCE_TRAIT_DERANGEMENT("defence_trait_derangement", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_BUG_WEAKNESS("attack_trait_bug_weakness", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//蟲族
	DEFENCE_TRAIT_BUG_WEAKNESS("defence_trait_bug_weakness", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_ANIMAL_WEAKNESS("attack_trait_animal_weakness", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//動物
	DEFENCE_TRAIT_ANIMAL_WEAKNESS("defence_trait_animal_weakness", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_PLANT_WEAKNESS("attack_trait_plant_weakness", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//植物
	DEFENCE_TRAIT_PLANT_WEAKNESS("defence_trait_plant_weakness", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_BEAST_WEAKNESS("attack_trait_beast_weakness", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//怪獸
	DEFENCE_TRAIT_BEAST_WEAKNESS("defence_trait_beast_weakness", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_DRAGON_WEAKNESS("attack_trait_dragon_weakness", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//龍族
	DEFENCE_TRAIT_DRAGON_WEAKNESS("defence_trait_dragon_weakness", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_PARALYZE("attack_trait_paralyze", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//麻痺
	DEFENCE_TRAIT_PARALYZE("defence_trait_paralyze", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_DUAL("attack_trait_dual", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//雙刀
	DEFENCE_TRAIT_DUAL("defence_trait_dual", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_DUALFIST("attack_trait_dualfist", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//拳套
	DEFENCE_TRAIT_DUALFIST("defence_trait_dualfist", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_BOSS("attack_trait_boss", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//
	DEFENCE_TRAIT_BOSS("defence_trait_boss", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_GIANT_WEAKNESS("attack_trait_giant_weakness", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//巨人
	DEFENCE_TRAIT_GIANT_WEAKNESS("defence_trait_giant_weakness", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_CONSTRUCT_WEAKNESS("attack_trait_construct_weakness", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//創造物
	DEFENCE_TRAIT_CONSTRUCT_WEAKNESS("defence_trait_construct_weakness", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_DEATH("attack_trait_death", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//
	DEFENCE_TRAIT_DEATH("defence_trait_death", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_VALAKAS("attack_trait_valakas", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//
	DEFENCE_TRAIT_VALAKAS("defence_trait_valakas", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_ROOT_PHYSICALLY("attack_trait_root_physically", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//物理型束縛
	DEFENCE_TRAIT_ROOT_PHYSICALLY("defence_trait_root_physically", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_RAPIER("attack_trait_rapier", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//細劍
	DEFENCE_TRAIT_RAPIER("defence_trait_rapier", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_CROSSBOW("attack_trait_crossbow", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//弩弓
	DEFENCE_TRAIT_CROSSBOW("defence_trait_crossbow", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_ANCIENTSWORD("attack_trait_ancientsword", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//古代劍
	DEFENCE_TRAIT_ANCIENTSWORD("defence_trait_ancientsword", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_TURN_STONE("attack_trait_turn_stone", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//變身石頭
	DEFENCE_TRAIT_TURN_STONE("defence_trait_turn_stone", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_GUST("attack_trait_gust", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//
	DEFENCE_TRAIT_GUST("defence_trait_gust", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_PHYSICAL_BLOCKADE("attack_trait_physical_blockade", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//
	DEFENCE_TRAIT_PHYSICAL_BLOCKADE("defence_trait_physical_blockade", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_TARGET("attack_trait_target", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//
	DEFENCE_TRAIT_TARGET("defence_trait_target", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_PHYSICAL_WEAKNESS("attack_trait_physical_weakness", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//
	DEFENCE_TRAIT_PHYSICAL_WEAKNESS("defence_trait_physical_weakness", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_MAGICAL_WEAKNESS("attack_trait_magical_weakness", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//
	DEFENCE_TRAIT_MAGICAL_WEAKNESS("defence_trait_magical_weakness", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_DUALDAGGER("attack_trait_dualdagger", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//雙匕首
	DEFENCE_TRAIT_DUALDAGGER("defence_trait_dualdagger", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_DUALBLUNT("attack_trait_dualblunt", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//雙鈍器
	DEFENCE_TRAIT_DUALBLUNT("defence_trait_dualblunt", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_KNOCKBACK("attack_trait_knockback", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//擊退
	DEFENCE_TRAIT_KNOCKBACK("defence_trait_knockback", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_KNOCKDOWN("attack_trait_knockdown", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//擊倒
	DEFENCE_TRAIT_KNOCKDOWN("defence_trait_knockdown", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_PULL("attack_trait_pull", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//捕獲
	DEFENCE_TRAIT_PULL("defence_trait_pull", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_HATE("attack_trait_hate", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//
	DEFENCE_TRAIT_HATE("defence_trait_hate", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_AGGRESSION("attack_trait_aggression", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//
	DEFENCE_TRAIT_AGGRESSION("defence_trait_aggression", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_AIRBIND("attack_trait_airbind", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//
	DEFENCE_TRAIT_AIRBIND("defence_trait_airbind", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_DISARM("attack_trait_disarm", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//
	DEFENCE_TRAIT_DISARM("defence_trait_disarm", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_DEPORT("attack_trait_deport", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//
	DEFENCE_TRAIT_DEPORT("defence_trait_deport", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_CHANGEBODY("attack_trait_changebody", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//
	DEFENCE_TRAIT_CHANGEBODY("defence_trait_changebody", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ATTACK_TRAIT_TWOHANDCROSSBOW("attack_trait_twohandcrossbow", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),//
	DEFENCE_TRAIT_TWOHANDCROSSBOW("defence_trait_twohandcrossbow", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

	public static final Stats[] VALUES = values();
	public static final int NUM_STATS = VALUES.length;

	private final String _value;
	private double _min;
	private double _max;
	private double _init;

	public String getValue()
	{
		return _value;
	}

	public double getInit()
	{
		return _init;
	}

	private Stats(String s)
	{
		this(s, 0., Double.POSITIVE_INFINITY, 0.);
	}

	private Stats(String s, double min, double max)
	{
		this(s, min, max, 0.);
	}

	private Stats(String s, double min, double max, double init)
	{
		_value = s.toUpperCase();
		_min = min;
		_max = max;
		_init = init;
	}

	public double validate(double val)
	{
		if(val < _min)
			return _min;
		if(val > _max)
			return _max;
		return val;
	}

	public static Stats valueOfXml(String name)
	{
		String upperCaseName = name.toUpperCase();
		for(Stats s : VALUES)
		{
			if(s.getValue().equals(upperCaseName))
				return s;
		}
		throw new NoSuchElementException("Unknown name '" + name + "' for enum Stats");
	}

	@Override
	public String toString()
	{
		return _value;
	}
}