package l2s.gameserver.network.l2.c2s;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import l2s.commons.util.Rnd;
import l2s.gameserver.data.xml.holder.UpgradeSystemHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage.ScreenMessageAlign;
import l2s.gameserver.network.l2.s2c.ExUpgradeSystemNormalResult;
import l2s.gameserver.network.l2.s2c.ExUpgradeSystemResult;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.item.upgrade.UpgradeItemData;
import l2s.gameserver.templates.item.upgrade.normal.NormalUpgradeData;
import l2s.gameserver.templates.item.upgrade.normal.NormalUpgradeResult;
import l2s.gameserver.templates.item.upgrade.normal.NormalUpgradeResultType;
import l2s.gameserver.utils.ItemFunctions;

public class ExUpgradeSystemNormalRequest extends L2GameClientPacket
{
	private int targetItemObjectId;
	private int upgradeType;
	private int upgradeId;

	protected boolean readImpl() throws Exception
	{
		this.targetItemObjectId = readD();
		this.upgradeType = readD();
		this.upgradeId = readD();
		return true;
	}

	protected void runImpl()
	{
		Player activeChar =getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		if(activeChar.isActionsDisabled())
		{
			activeChar.sendPacket(new ExUpgradeSystemNormalResult(0));
			activeChar.sendPacket(SystemMsg.FAILED_THE_OPERATION);
			return;
		}
		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(new ExUpgradeSystemNormalResult(0));
			activeChar.sendPacket(SystemMsg.FAILED_THE_OPERATION);
			return;
		}
		if(activeChar.isProcessingRequest())
		{
			activeChar.sendPacket(new ExUpgradeSystemNormalResult(0));
			activeChar.sendPacket(SystemMsg.FAILED_THE_OPERATION);
			return;
		}
		if(activeChar.isFishing())
		{
			activeChar.sendPacket(new ExUpgradeSystemNormalResult(0));
			activeChar.sendPacket(SystemMsg.FAILED_THE_OPERATION);
			return;
		}
		if(activeChar.isInTrainingCamp())
		{
			activeChar.sendPacket(new ExUpgradeSystemNormalResult(0));
			activeChar.sendPacket(SystemMsg.FAILED_THE_OPERATION);
			return;
		}
		NormalUpgradeData upgradeData = UpgradeSystemHolder.getInstance().getNormalUpgradeData(upgradeId);
		if((upgradeData == null) || (upgradeData.getType() != upgradeType))
		{
			activeChar.sendPacket(new ExUpgradeSystemNormalResult(0));
			activeChar.sendPacket(SystemMsg.FAILED_THE_OPERATION);
			return;
		}
		NormalUpgradeResult successResult = upgradeData.getSuccessResult();
		NormalUpgradeResult failResult = upgradeData.getFailResult();
		if((successResult == null) && (failResult == null))
		{
			activeChar.sendPacket(new ExUpgradeSystemNormalResult(0));
			activeChar.sendPacket(SystemMsg.FAILED_THE_OPERATION);
			return;
		}
		double totalChance = 0.0D;
		if(successResult != null)
		{
			totalChance += successResult.getChance();
		}
		if(failResult != null)
		{
			totalChance += failResult.getChance();
		}
		double totalFailChance = 100.0D - totalChance;
		if((totalFailChance > 0.0D) && (Rnd.chance(totalFailChance)))
		{
			activeChar.sendPacket(new ExUpgradeSystemNormalResult(0));
			activeChar.sendPacket(SystemMsg.FAILED_THE_OPERATION);
			return;
		}
		List<NormalUpgradeResult> rolledResults = new ArrayList<NormalUpgradeResult>();
		int rollCount = 1;
		while(rolledResults.isEmpty())
		{
			if((successResult != null) && (Rnd.chance(successResult.getChance() * rollCount)))
			{
				rolledResults.add(successResult);
			}
			if((failResult != null) && (Rnd.chance(failResult.getChance() * rollCount)))
			{
				rolledResults.add(failResult);
			}
			rollCount += 10;
		}
		NormalUpgradeResult result =Rnd.get(rolledResults);
		if(result == null)
		{
			activeChar.sendPacket(new ExUpgradeSystemNormalResult(0));
			activeChar.sendPacket(SystemMsg.FAILED_THE_OPERATION);
			return;
		}
		activeChar.getInventory().writeLock();
		try
		{
			ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(this.targetItemObjectId);
			if(targetItem == null)
			{
				activeChar.sendPacket(new ExUpgradeSystemNormalResult(0));
				activeChar.sendPacket(SystemMsg.FAILED_BECAUSE_THE_TARGET_ITEM_DOES_NOT_EXIST);
				return;
			}

			if(targetItem.getEnchantLevel() != upgradeData.getEnchantLevel())
			{
				activeChar.sendPacket(new ExUpgradeSystemNormalResult(0));
				activeChar.sendPacket(SystemMsg.FAILED_BECAUSE_THE_TARGET_ITEM_DOES_NOT_EXIST);
				return;
			}

			if(activeChar.getAdena() < upgradeData.getPrice())
			{
				activeChar.sendPacket(new ExUpgradeSystemNormalResult(0));
				activeChar.sendPacket(SystemMsg.FAILED_BECAUSE_THERES_NOT_ENOUGH_ADENA);
				return;
			}
			label687: for(UpgradeItemData requiredItem : upgradeData.getRequiredItems())
			{
				if(requiredItem.getCount() != 0L)
				{
					List<ItemInstance> items = activeChar.getInventory().getItemsByItemId(requiredItem.getId());
					Iterator<ItemInstance> localIterator2 = items.iterator();
					while(true)
					{
						if(!localIterator2.hasNext())
							break label687;
						ItemInstance item =localIterator2.next();
						if((item != null) && (item.getCount() < requiredItem.getCount()))
						{
							//if (item.getEnchantLevel() == requiredItem.getEnchantLevel()) {
							break;
							//}
						}
					}
					activeChar.sendPacket(new ExUpgradeSystemNormalResult(0));
					activeChar.sendPacket(SystemMsg.FAILED_BECAUSE_THERE_ARE_NOT_ENOUGH_INGREDIENTS);
					return;
				}
			}
			activeChar.getInventory().destroyItem(targetItem, 1L);
			activeChar.sendPacket(SystemMessagePacket.removeItems(targetItem.getItemId(), 1L));
			activeChar.reduceAdena(upgradeData.getPrice(), true);

			for(UpgradeItemData requiredItem : upgradeData.getRequiredItems())
				if(requiredItem.getCount() != 0L)
				{

					List<ItemInstance> items = activeChar.getInventory().getItemsByItemId(requiredItem.getId());
					Iterator<ItemInstance> localIterator2 = items.iterator();
					while(true)
					{
						if(localIterator2.hasNext())
						{
							ItemInstance item =localIterator2.next();
							if((item != null) && (item.getCount() >= requiredItem.getCount()) && (item.getEnchantLevel() == requiredItem.getEnchantLevel()) && (activeChar.getInventory().destroyItemByObjectId(item.getObjectId(), requiredItem.getCount())))
							{
								activeChar.sendPacket(SystemMessagePacket.removeItems(requiredItem.getId(), requiredItem.getCount()));
								break;
							}
						}
					}
				}
		}
		finally
		{
			// Iterator localIterator2;
			activeChar.getInventory().writeUnlock();
		}
		if(result.getType() == NormalUpgradeResultType.SUCCESS)
		{
			for(Iterator<UpgradeItemData> targetItem = result.getItems().iterator(); targetItem.hasNext();)
			{
				UpgradeItemData resultItem = targetItem.next();
				ItemFunctions.addItem(activeChar, resultItem.getId(), resultItem.getCount(), resultItem.getEnchantLevel(), true);
			}
			NormalUpgradeResult bonusResult = upgradeData.getBonusResult();
			if((bonusResult != null) && (Rnd.chance(bonusResult.getChance())))
			{
				for(Iterator<UpgradeItemData> resultItem = bonusResult.getItems().iterator(); resultItem.hasNext();)
				{
					UpgradeItemData resultItem1 = resultItem.next();
					ItemFunctions.addItem(activeChar, resultItem1.getId(), resultItem1.getCount(), resultItem1.getEnchantLevel(), true);
				}
			}
			activeChar.sendMessage("恭喜升級成功。");
			activeChar.sendPacket(new ExUpgradeSystemResult(1));
			activeChar.sendPacket(new ExShowScreenMessage("恭喜升級成功。", 5000, ScreenMessageAlign.TOP_CENTER, true));
			return;
		}
		if(result.getType() == NormalUpgradeResultType.FAIL)
		{
			NormalUpgradeResult FailResult = upgradeData.getFailResult();
			if(FailResult != null) 
			{
				for(Iterator<UpgradeItemData> resultItem = FailResult.getItems().iterator();  resultItem.hasNext();)
				{
					UpgradeItemData resultItem1 = resultItem.next();
					ItemFunctions.addItem(activeChar, resultItem1.getId(), resultItem1.getCount(), resultItem1.getEnchantLevel(), true);
				}
			}
			activeChar.sendMessage("升級失敗了。");
			activeChar.sendPacket(new ExShowScreenMessage("升級失敗了。", 5000, ScreenMessageAlign.TOP_CENTER, true));
			activeChar.sendPacket(new ExUpgradeSystemResult(0));
			return;
		}
	}
}