package gregtech.api.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_BasicMachine;
import gregtech.api.util.GT_Utility;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidTank;

import java.util.Iterator;

import static gregtech.api.enums.GT_Values.NI;
import static gregtech.api.metatileentity.implementations.GT_MetaTileEntity_BasicMachine.OTHER_SLOT_COUNT;
/**
 * NEVER INCLUDE THIS FILE IN YOUR MOD!!!
 * <p/>
 * The Container I use for all my Basic Machines
 */
public class GT_Container_BasicMachine extends GT_Container_BasicTank {

    public boolean mFluidTransfer = false, mItemTransfer = false, mStuttering = false;

    public GT_Container_BasicMachine(InventoryPlayer aInventoryPlayer, IGregTechTileEntity aTileEntity) {
        super(aInventoryPlayer, aTileEntity);
    }

    @Override
    public void addSlots(InventoryPlayer aInventoryPlayer) {
        addSlotToContainer(new GT_Slot_Holo(mTileEntity, 0, 8, 63, false, true, 1));
        addSlotToContainer(new GT_Slot_Holo(mTileEntity, 0, 26, 63, false, true, 1));
        addSlotToContainer(new GT_Slot_Render(mTileEntity, 2, 107, 63));

        int tStartIndex = ((GT_MetaTileEntity_BasicMachine) mTileEntity.getMetaTileEntity()).getInputSlot();

        switch (((GT_MetaTileEntity_BasicMachine) mTileEntity.getMetaTileEntity()).mInputSlotCount) {
            case 0:
                break;
            case 1:
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 53, 25));
                break;
            case 2:
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 35, 25));
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 53, 25));
                break;
            case 3:
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 17, 25));
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 35, 25));
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 53, 25));
                break;
            case 4:
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 35, 16));
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 53, 16));
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 35, 34));
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 53, 34));
                break;
            case 5:
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 17, 16));
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 35, 16));
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 53, 16));
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 35, 34));
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 53, 34));
                break;
            case 6:
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 17, 16));
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 35, 16));
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 53, 16));
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 17, 34));
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 35, 34));
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 53, 34));
                break;
            case 7:
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 17, 7));
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 35, 7));
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 53, 7));
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 17, 25));
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 35, 25));
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 53, 25));
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 17, 43));
                break;
            case 8:
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 17, 7));
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 35, 7));
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 53, 7));
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 17, 25));
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 35, 25));
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 53, 25));
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 17, 43));
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 35, 43));
                break;
            default:
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 17, 7));
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 35, 7));
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 53, 7));
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 17, 25));
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 35, 25));
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 53, 25));
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 17, 43));
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 35, 43));
                addSlotToContainer(new Slot(mTileEntity, tStartIndex++, 53, 43));
                break;
        }

        tStartIndex = ((GT_MetaTileEntity_BasicMachine) mTileEntity.getMetaTileEntity()).getOutputSlot();

        switch (((GT_MetaTileEntity_BasicMachine) mTileEntity.getMetaTileEntity()).mOutputItems.length) {
            case 0:
                break;
            case 1:
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 107, 25));
                break;
            case 2:
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 107, 25));
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 125, 25));
                break;
            case 3:
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 107, 25));
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 125, 25));
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 143, 25));
                break;
            case 4:
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 107, 16));
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 125, 16));
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 107, 34));
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 125, 34));
                break;
            case 5:
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 107, 16));
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 125, 16));
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 143, 16));
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 107, 34));
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 125, 34));
                break;
            case 6:
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 107, 16));
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 125, 16));
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 143, 16));
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 107, 34));
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 125, 34));
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 143, 34));
                break;
            case 7:
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 107, 7));
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 125, 7));
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 143, 7));
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 107, 25));
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 125, 25));
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 143, 25));
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 107, 43));
                break;
            case 8:
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 107, 7));
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 125, 7));
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 143, 7));
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 107, 25));
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 125, 25));
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 143, 25));
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 107, 43));
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 125, 43));
                break;
            default:
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 107, 7));
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 125, 7));
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 143, 7));
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 107, 25));
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 125, 25));
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 143, 25));
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 107, 43));
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 125, 43));
                addSlotToContainer(new GT_Slot_Output(mTileEntity, tStartIndex++, 143, 43));
                break;
        }

        addSlotToContainer(new Slot(mTileEntity, 1, 80, 63));
        addSlotToContainer(new Slot(mTileEntity, 3, 125, 63));
        addSlotToContainer(new GT_Slot_Render(mTileEntity, tStartIndex++, 53, 63));
    }

    @Override
    public ItemStack slotClick(int aSlotIndex, int aMouseclick, int aShifthold, EntityPlayer aPlayer) {
        GT_MetaTileEntity_BasicMachine machine = (GT_MetaTileEntity_BasicMachine) mTileEntity.getMetaTileEntity();
        if (machine == null) return null;
        switch (aSlotIndex) {
            case 0:
                if (mTileEntity.getMetaTileEntity() == null) return null;
                ((GT_MetaTileEntity_BasicMachine) mTileEntity.getMetaTileEntity()).mFluidTransfer = !((GT_MetaTileEntity_BasicMachine) mTileEntity.getMetaTileEntity()).mFluidTransfer;
                return null;
            case 1:
                if (mTileEntity.getMetaTileEntity() == null) return null;
                ((GT_MetaTileEntity_BasicMachine) mTileEntity.getMetaTileEntity()).mItemTransfer = !((GT_MetaTileEntity_BasicMachine) mTileEntity.getMetaTileEntity()).mItemTransfer;
                return null;
            case 2:
                return pickupFluid(machine.getDrainableStack(), aPlayer);
            default:
                if (aSlotIndex == OTHER_SLOT_COUNT + 1 + machine.mInputSlotCount + machine.mOutputItems.length) {
                    // input fluid slot
                    ItemStack tStackHeld = aPlayer.inventory.getItemStack();
                    ItemStack tStackSizedOne = GT_Utility.copyAmount(1, tStackHeld);
                    if (tStackSizedOne == null) return null;
                    FluidStack tInputFluid = machine.getFillableStack();
                    FluidStack tFluidHeld = GT_Utility.getFluidForFilledItem(tStackSizedOne, true);
                    if (tInputFluid == null) {
                        if (tFluidHeld == null)
                            // both null -> no op
                            return null;
                        return fillFluid(machine, aPlayer, tFluidHeld);
                    } else {
                        if (tFluidHeld != null) {
                            // both nonnull. actually both pickup and fill is reasonable, but I'll go with fill here
                            return fillFluid(machine, aPlayer, tFluidHeld);
                        } else {
                            return pickupFluid(tInputFluid, aPlayer);
                        }
                    }
                } else {
                    return super.slotClick(aSlotIndex, aMouseclick, aShifthold, aPlayer);
                }
        }
    }

    private ItemStack pickupFluid(FluidStack aTankStack, EntityPlayer aPlayer) {
        if (aTankStack == null) return null;
        ItemStack tStackHeld = aPlayer.inventory.getItemStack();
        ItemStack tStackSizedOne = GT_Utility.copyAmount(1, tStackHeld);
        if (tStackSizedOne == null) return null;
        ItemStack tFilled = GT_Utility.fillFluidContainer(aTankStack, tStackSizedOne, true, false);
        if (tFilled == null && tStackSizedOne.getItem() instanceof IFluidContainerItem) {
            IFluidContainerItem tContainerItem = (IFluidContainerItem) tStackSizedOne.getItem();
            int tFilledAmount = tContainerItem.fill(tStackSizedOne, aTankStack, true);
            if (tFilledAmount > 0) {
                tFilled = tStackSizedOne;
                aTankStack.amount -= tFilledAmount;
            }
        }
        if (tFilled != null) {
            reduceStackSizeInHandByOne(aPlayer);
            GT_Utility.addItemToPlayerInventory(aPlayer, tFilled);
        }
        return tFilled;
    }

    private ItemStack fillFluid(IFluidTank aTank, EntityPlayer aPlayer, FluidStack aFluidHeld) {
        ItemStack tStackHeld = aPlayer.inventory.getItemStack();
        ItemStack tStackSizedOne = GT_Utility.copyAmount(1, tStackHeld);
        if (tStackSizedOne == null)
            return null;

        int tFilled = aTank.fill(aFluidHeld, false);
        if (tFilled == 0)  // filled nothing
            return null;
        ItemStack tStackEmptied = null;
        if (tFilled == aFluidHeld.amount)
            // fully accepted - try take it from item now
            // IFluidContainerItem is intentionally not checked here. it will be checked later
            tStackEmptied = GT_Utility.getContainerForFilledItem(tStackSizedOne, false);
        if (tStackEmptied == null && tStackHeld.getItem() instanceof IFluidContainerItem) {
            IFluidContainerItem container = (IFluidContainerItem) tStackHeld.getItem();
            FluidStack tDrained = container.drain(tStackSizedOne, tFilled, true);
            if (tDrained != null && tDrained.amount > 0)
                // something is actually drained - change the cell and drop it to player
                tStackEmptied = tStackSizedOne;
        }
        if (tStackEmptied == null)
            // somehow the cell refuse to take that amount of fluid, no op then
            return null;
        aTank.fill(aFluidHeld, true);
        GT_Utility.addItemToPlayerInventory(aPlayer, tStackEmptied);
        reduceStackSizeInHandByOne(aPlayer);
        return tStackEmptied;
    }

    private void reduceStackSizeInHandByOne(EntityPlayer aPlayer) {
        ItemStack tStackHeld = aPlayer.inventory.getItemStack();
        tStackHeld.stackSize -= 1;
        if (tStackHeld.stackSize == 0)
            aPlayer.inventory.setItemStack(NI);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (mTileEntity.isClientSide() || mTileEntity.getMetaTileEntity() == null) return;

        mFluidTransfer = ((GT_MetaTileEntity_BasicMachine) mTileEntity.getMetaTileEntity()).mFluidTransfer;
        mItemTransfer = ((GT_MetaTileEntity_BasicMachine) mTileEntity.getMetaTileEntity()).mItemTransfer;
        mStuttering = ((GT_MetaTileEntity_BasicMachine) mTileEntity.getMetaTileEntity()).mStuttering;

        Iterator var2 = this.crafters.iterator();
        while (var2.hasNext()) {
            ICrafting var1 = (ICrafting) var2.next();
            var1.sendProgressBarUpdate(this, 102, mFluidTransfer ? 1 : 0);
            var1.sendProgressBarUpdate(this, 103, mItemTransfer ? 1 : 0);
            var1.sendProgressBarUpdate(this, 104, mStuttering ? 1 : 0);
        }
    }

    @Override
    public void addCraftingToCrafters(ICrafting par1ICrafting) {
        super.addCraftingToCrafters(par1ICrafting);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int par1, int par2) {
        super.updateProgressBar(par1, par2);
        switch (par1) {
            case 102:
                mFluidTransfer = (par2 != 0);
                break;
            case 103:
                mItemTransfer = (par2 != 0);
                break;
            case 104:
                mStuttering = (par2 != 0);
                break;
        }
    }

    @Override
    public int getSlotStartIndex() {
        return 3;
    }

    @Override
    public int getShiftClickStartIndex() {
        return 3;
    }

    @Override
    public int getSlotCount() {
        return getShiftClickSlotCount() + ((GT_MetaTileEntity_BasicMachine) mTileEntity.getMetaTileEntity()).mOutputItems.length + 2;
    }

    @Override
    public int getShiftClickSlotCount() {
        return ((GT_MetaTileEntity_BasicMachine) mTileEntity.getMetaTileEntity()).mInputSlotCount;
    }
}
