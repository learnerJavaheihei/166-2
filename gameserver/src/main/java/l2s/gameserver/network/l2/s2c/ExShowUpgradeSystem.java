package l2s.gameserver.network.l2.s2c;


public class ExShowUpgradeSystem
  extends L2GameServerPacket
{
  private final int unk;
  
  public ExShowUpgradeSystem(int unk)
  {
    this.unk = unk;
  }
  
  protected void writeImpl()
  {
    writeH(this.unk);
    writeH(0);
  }
}
