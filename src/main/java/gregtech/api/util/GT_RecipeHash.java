package gregtech.api.util;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class GT_RecipeHash {
	public static ArrayList<GT_RecipeHash> cachedResipes = new ArrayList<GT_RecipeHash>();
	
	public static GT_RecipeHash getCashedResipe(boolean aOptimize, ItemStack[] aInputs, ItemStack[] aOutputs, Object aSpecialItems, int[] aChances, 
    		FluidStack[] aFluidInputs, FluidStack[] aFluidOutputs, int aDuration, int aEUt, int aSpecialValue) {
		
		return null;
	}
	
	// Inputed args
	private boolean aOptimize;
	private ItemStack[] aInputs;
	private ItemStack[] aOutputs;
	private Object aSpecialItems;
	private int[] aChances; 
	private FluidStack[] aFluidInputs;
	private FluidStack[] aFluidOutputs;
	private int aDuration;
	private int aEUt;
	private int aSpecialValue;
	
	// Outputed args
	public ItemStack[] mInputs;
	public ItemStack[] mOutputs;
    public Object mSpecialItems;
    public int[] mChances;
    public FluidStack[] mFluidInputs;
    public FluidStack[] mFluidOutputs;
    public int mDuration;
    public int mSpecialValue;        	
    public int mEUt;
    
    public GT_RecipeHash(boolean aOptimize, ItemStack[] aInputs, ItemStack[] aOutputs, Object aSpecialItems, int[] aChances, 
    		FluidStack[] aFluidInputs, FluidStack[] aFluidOutputs, int aDuration, int aEUt, int aSpecialValue) {
    	this.aOptimize = aOptimize;
    	this.aInputs = aInputs;
    	this.aOutputs = aOutputs;
    	this.aSpecialItems = aSpecialItems;
    	this.aChances = aChances;
    	this.aFluidInputs = aFluidInputs;
    	this.aFluidOutputs = aFluidOutputs;
    	this.aDuration = aDuration;
    	this.aEUt = aEUt;
    	this.aSpecialValue = aSpecialValue;
    }
}