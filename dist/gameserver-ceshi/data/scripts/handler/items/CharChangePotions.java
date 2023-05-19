package handler.items;

import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;

//TODO: [Bonux] Переделать через скиллы.
public class CharChangePotions extends ScriptItemHandler
{
	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		if(playable == null || !playable.isPlayer())
			return false;
		Player player = (Player) playable;

		int itemId = item.getItemId();

		if(!player.getInventory().destroyItem(item, 1))
		{
			player.sendActionFailed();
			return false;
		}

		int face = player.getFace();
		int hairColor = player.getHairColor();
		int hairStyle = player.getHairStyle();
		switch(itemId)
		{
			case 5235://整形藥水-A
				player.setFace(0);
				break;
			case 49014://整形藥水-A
				player.setFace(0);
				break;
			case 70863://整形藥水-A
				player.setFace(0);
				break;
			case 5236://整形藥水-B
				player.setFace(1);
				break;
			case 49015://整形藥水-B
				player.setFace(1);
				break;
			case 70864://整形藥水-B
				player.setFace(1);
				break;
			case 5237://整形藥水-C
				player.setFace(2);
				break;
			case 49016://整形藥水-C
				player.setFace(2);
				break;
			case 70865://整形藥水-C
				player.setFace(2);
				break;
			case 5238://染髮藥水-A
				player.setHairColor(0);
				break;
			case 49017://染髮藥水-A
				player.setHairColor(0);
				break;
			case 70866://染髮藥水-A
				player.setHairColor(0);
				break;
			case 5239://染髮藥水-B
				player.setHairColor(1);
				break;
			case 49018://染髮藥水-B
				player.setHairColor(1);
				break;
			case 70867://染髮藥水-B
				player.setHairColor(1);
				break;
			case 5240://染髮藥水-C
				player.setHairColor(2);
				break;
			case 49019://染髮藥水-C
				player.setHairColor(2);
				break;
			case 70868://染髮藥水-C
				player.setHairColor(2);
				break;
			case 5241://染髮藥水-D
				player.setHairColor(3);
				break;
			case 49020://染髮藥水-D
				player.setHairColor(3);
				break;
			case 70869://染髮藥水-D
				player.setHairColor(3);
				break;
			case 5242://造型藥水-A
				player.setHairStyle(0);
				break;
			case 70870://造型藥水-A
				player.setHairStyle(0);
				break;
			case 5243://造型藥水-B
				player.setHairStyle(1);
				break;
			case 70871://造型藥水-B
				player.setHairStyle(1);
				break;
			case 5244://造型藥水-C
				player.setHairStyle(2);
				break;
			case 70872://造型藥水-C
				player.setHairStyle(2);
				break;
			case 5245://造型藥水-D
				player.setHairStyle(3);
				break;
			case 70873://造型藥水-D
				player.setHairStyle(3);
				break;
			case 5246://造型藥水-E
				player.setHairStyle(4);
				break;
			case 70874://造型藥水-E
				player.setHairStyle(4);
				break;
			case 5247://造型藥水-F
				player.setHairStyle(5);
				break;
			case 70875://造型藥水-F
				player.setHairStyle(5);
				break;
			case 5248://造型藥水-G
				player.setHairStyle(6);
				break;
			case 70876://造型藥水-G
				player.setHairStyle(6);
				break;
		}

		player.broadcastPacket(new MagicSkillUse(player, player, 2003, 1, 1, 0));
		if(face != player.getFace() || hairColor != player.getHairColor() || hairStyle != player.getHairStyle())
			player.broadcastUserInfo(true);
		return true;
	}
}