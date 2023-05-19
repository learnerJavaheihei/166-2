package l2s.gameserver.network.l2.s2c;


public class ExShowUpgradeSystemNormal
  extends L2GameServerPacket
{
  private final int type;
  
  public ExShowUpgradeSystemNormal(int type)
  {
    this.type = type;
  }
  
  protected void writeImpl()
  {
    writeH(1);
    writeH(this.type);
    writeH(100);
    writeD(0);
    writeD(0);
  }
}
