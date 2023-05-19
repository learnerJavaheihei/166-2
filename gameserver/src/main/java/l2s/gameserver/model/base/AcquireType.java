package l2s.gameserver.model.base;

/**
 * Author: VISTALL
 * Date:  11:53/01.12.2010
 */
public enum AcquireType
{
	/*0*/NORMAL(0),
	/*1*/FISHING(1),
	/*2*/CLAN(2),
	/*3*/SUB_UNIT(3),
	/*4*/GENERAL(11),
	/*5*/HERO(13),
	/*6*/GM(14),
	/*7*/MULTICLASS(20),
	/*8*/CUSTOM,
	/*9*/ADVENTURER,//認證技能--
	/*10*/FORGOTTENPOWER;//--遺忘之力

	public static final AcquireType[] VALUES = AcquireType.values();

	private final int _id;

	private AcquireType(int id)
	{
		_id = id;
	}

	private AcquireType()
	{
		_id = ordinal();
	}

	public int getId()
	{
		return _id;
	}

	public static AcquireType getById(int id)
	{
		for(AcquireType at : VALUES)
		{
			if(at.getId() == id)
				return at;
		}
		return null;
	}
}
