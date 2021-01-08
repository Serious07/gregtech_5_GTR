package gregtech.common.tileentities.machines.basic;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import gregtech.api.util.*;

public class GT_Components {
	public ItemStack[] mItemStack;
	public FluidStack[] mFluidStack;
	
	public HashMap<ItemStack, Integer> cashedItemStack = new HashMap<ItemStack, Integer>();
	public HashMap<FluidStack, Integer> cashedFluidStack = new HashMap<FluidStack, Integer>();
	
	public GT_Components(ItemStack[] mInputStack, FluidStack[] mFluidStack) {
		this.mItemStack = mInputStack;
		this.mFluidStack = mFluidStack;
		
		if(mInputStack != null) {
			for (ItemStack itemStack : mInputStack) {
				if(itemStack != null) {
					cashedItemStack.put(itemStack, itemStack.stackSize);
				}
			}
		}
		
		if(mFluidStack != null) {
			for (FluidStack fluidStack : mFluidStack) {
				if(fluidStack != null) {
					cashedFluidStack.put(fluidStack, fluidStack.amount);
				}
			}
		}
	}
	

	
    public boolean equalsComponents(GT_Components other) {
    	System.out.println("Biba 1");
    	
    	if(cashedItemStack.size() == 0 && cashedFluidStack.size() == 0) return false;
    	
    	int itemsCount = 0;
    	int fluidCount = 0;
		
		ItemStack[] otherItems = other.mItemStack;
		FluidStack[] otherFluids = other.mFluidStack;
		
		System.out.println("Biba 2");
		
		if(cashedItemStack.size() > 0) {
			for (ItemStack otherItem : otherItems) {
				Iterator it = ((Map<ItemStack, Integer>) cashedItemStack).entrySet().iterator();
				
				while(it.hasNext()) {
					Map.Entry<ItemStack, Integer> pair = (Map.Entry<ItemStack, Integer>)it.next();
					ItemStack key = pair.getKey();
					
					System.out.println("Biba 3");
					
					if(otherItem.getDisplayName().equals(((ItemStack)key).getDisplayName())) {
						System.out.println("OtherItemName: " + otherItem.getDisplayName() + " key: " + ((ItemStack)key).getDisplayName());
					}
					
					if(GT_Utility.areUnificationsEqual(otherItem, (ItemStack)key, true) || 
						    GT_Utility.areUnificationsEqual(GT_OreDictUnificator.get(false, otherItem), (ItemStack)key, true)){
				    	itemsCount += 1;
				    }
					
					System.out.println("Biba 4");
					
					if(itemsCount == cashedItemStack.size()) break;
				}
				
				System.out.println("Biba 5");
				
				if(itemsCount == cashedItemStack.size()) break;
			}
			
			System.out.println("ItemCount: " + itemsCount + " cashedItemStackSize " + cashedItemStack.size());
			
			if (itemsCount < cashedItemStack.size()) return false;
		}
		
		System.out.println("Biba 7");
		
		if(cashedFluidStack.size() > 0) {
			for (FluidStack otherFluid : otherFluids) {
				Iterator it = ((Map<FluidStack, Integer>) cashedFluidStack).entrySet().iterator();
				
				while(it.hasNext()) {
					Map.Entry<FluidStack, Integer> pair = (Map.Entry<FluidStack, Integer>)it.next();
					FluidStack key = pair.getKey();
					
					System.out.println("Biba 8");
					
					if(key.isFluidEqual(otherFluid)) {
						fluidCount++;
					}
					
					System.out.println("Biba 9");
					
					if(fluidCount == cashedFluidStack.size()) break;
				}
				
				System.out.println("Biba 10");
				
				if(fluidCount == cashedFluidStack.size()) break;
			}
			
			System.out.println("Biba 11");
			
			if (fluidCount < cashedFluidStack.size()) return false;
		}
		
		System.out.println("Biba 12");
		
		System.out.println("fluidCount: " + fluidCount + " itemsCount: " + itemsCount);
		
		return (fluidCount >= cashedFluidStack.size() && itemsCount >= cashedItemStack.size());
		
		/*for (int i = 0; i < mItemStack.length; i++) {
			if((GT_Utility.areUnificationsEqual(mItemStack[i], otherItems[i], true) || 
			    GT_Utility.areUnificationsEqual(GT_OreDictUnificator.get(false, mItemStack[i]), otherItems[i], true)) &&
				mItemStack[i].stackSize <= otherItems[i].stackSize) {
				System.out.println("3");
			} else {
				System.out.println("4");
				return false;
			}
		}
		
		for (int i=0; i < mFluidStack.length; i++) {
			if(mFluidStack[i].isFluidEqual(otherFluids[i]) && mFluidStack[i].amount <= otherFluids[i].amount) {
				System.out.println("6");
			} else {
				System.out.println("7");
				return false;
			}
		}*/
	}
}
