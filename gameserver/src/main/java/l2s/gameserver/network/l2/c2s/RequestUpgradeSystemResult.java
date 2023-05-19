package l2s.gameserver.network.l2.c2s;

import java.util.Iterator;
import java.util.List;

import l2s.gameserver.data.xml.holder.UpgradeSystemHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.PcInventory;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.components.SystemMsg;
import l2s.gameserver.network.l2.s2c.ExUpgradeSystemResult;
import l2s.gameserver.network.l2.s2c.SystemMessagePacket;
import l2s.gameserver.templates.item.upgrade.UpgradeItemData;
import l2s.gameserver.templates.item.upgrade.rare.RareUpgradeData;
import l2s.gameserver.utils.ItemFunctions;

public class RequestUpgradeSystemResult extends L2GameClientPacket
{
	private int targetItemObjectId;
	private int upgradeId;

	protected boolean readImpl()
	{
		this.targetItemObjectId = readD();
		this.upgradeId = readD();
		return true;
	}

	protected void runImpl()
	{
		Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
		{
			return;
		}
		if(activeChar.isActionsDisabled())
		{
			activeChar.sendPacket(new ExUpgradeSystemResult(0));
			activeChar.sendPacket(SystemMsg.FAILED_THE_OPERATION);
			return;
		}
		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(new ExUpgradeSystemResult(0));
			activeChar.sendPacket(SystemMsg.FAILED_THE_OPERATION);
			return;
		}
		if(activeChar.isProcessingRequest())
		{
			activeChar.sendPacket(new ExUpgradeSystemResult(0));
			activeChar.sendPacket(SystemMsg.FAILED_THE_OPERATION);
			return;
		}
		if(activeChar.isFishing())
		{
			activeChar.sendPacket(new ExUpgradeSystemResult(0));
			activeChar.sendPacket(SystemMsg.FAILED_THE_OPERATION);
			return;
		}
		if(activeChar.isInTrainingCamp())
		{
			activeChar.sendPacket(new ExUpgradeSystemResult(0));
			activeChar.sendPacket(SystemMsg.FAILED_THE_OPERATION);
			return;
		}
		RareUpgradeData upgradeData = UpgradeSystemHolder.getInstance().getRareUpgradeData(this.upgradeId);
		if(upgradeData == null)
		{
			activeChar.sendPacket(new ExUpgradeSystemResult(0));
			activeChar.sendPacket(SystemMsg.FAILED_THE_OPERATION);
			return;
		}
		activeChar.getInventory().writeLock();
		try
		{
			ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(this.targetItemObjectId);
			if(targetItem == null)
			{
				activeChar.sendPacket(new ExUpgradeSystemResult(0));
				activeChar.sendPacket(SystemMsg.FAILED_BECAUSE_THE_TARGET_ITEM_DOES_NOT_EXIST);
				return;
			}
			if(targetItem.getEnchantLevel() != upgradeData.getEnchantLevel())
			{
				activeChar.sendPacket(new ExUpgradeSystemResult(0));
				activeChar.sendPacket(SystemMsg.FAILED_BECAUSE_THE_TARGET_ITEM_DOES_NOT_EXIST);
				return;
			}
			if(activeChar.getAdena() < upgradeData.getPrice())
			{
				activeChar.sendPacket(new ExUpgradeSystemResult(0));
				activeChar.sendPacket(SystemMsg.FAILED_BECAUSE_THERES_NOT_ENOUGH_ADENA);
				return;
			}
			for(UpgradeItemData requiredItem : upgradeData.getRequiredItems())
			{
				if(requiredItem.getCount() != 0L)
				{
					List<ItemInstance> items = activeChar.getInventory().getItemsByItemId(requiredItem.getId());
					Iterator<ItemInstance> localIterator2 = items.iterator();
					if(!localIterator2.hasNext())
					{
						break;
					}
					ItemInstance item =localIterator2.next();
					if((item != null) && (item.getCount() >= requiredItem.getCount()))
					{
						if(item.getEnchantLevel() == requiredItem.getEnchantLevel())
						{
							break;
						}
					}
				}
				activeChar.sendPacket(new ExUpgradeSystemResult(0));
				activeChar.sendPacket(SystemMsg.FAILED_BECAUSE_THERE_ARE_NOT_ENOUGH_INGREDIENTS);
				return;
			}

			activeChar.getInventory().destroyItem(targetItem, 1L);
			activeChar.sendPacket(SystemMessagePacket.removeItems(targetItem.getItemId(), 1L));
			activeChar.reduceAdena(upgradeData.getPrice(), true);
			for(UpgradeItemData requiredItem : upgradeData.getRequiredItems())
			{
				if(requiredItem.getCount() != 0L)
				{
					List<ItemInstance> items = activeChar.getInventory().getItemsByItemId(requiredItem.getId());
					//Iterator<ItemInstance> localIterator2 = items.iterator();
					for(ItemInstance item : items)
					{
						// ItemInstance item = (ItemInstance)its);
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
			//Iterator localIterator2;
			activeChar.getInventory().writeUnlock();
		}
		ItemFunctions.addItem(activeChar, upgradeData.getResultItemId(), upgradeData.getResultItemCount(), upgradeData.getResultItemEnchant(), true);
		activeChar.sendPacket(new ExUpgradeSystemResult(1));
	}
}
