package gregtech.common.gui;

import gregtech.api.gui.GT_Container_MultiMachine;
import gregtech.api.gui.GT_GUIContainerMetaTile_Machine;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.util.GT_Utility;
import net.minecraft.entity.player.InventoryPlayer;

import static gregtech.api.enums.GT_Values.RES_PATH_GUI;

public class GT_GUIContainer_Tesseract extends GT_GUIContainerMetaTile_Machine {

    public String mNEI;
    String mName = "";

    public GT_GUIContainer_Tesseract(InventoryPlayer aInventoryPlayer, IGregTechTileEntity aTileEntity, String aName, String aTextureFile, String aNEI) {
        super(new GT_Container_MultiMachine(aInventoryPlayer, aTileEntity, false), RES_PATH_GUI + "multimachines/" + (aTextureFile == null ? "MultiblockDisplay" : aTextureFile));
        mName = aName;
        mNEI = aNEI;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2) {
        fontRendererObj.drawString(mName, 10, 7, 16448255);

        if (mContainer != null) {
            if ((((GT_Container_MultiMachine) mContainer).mDisplayErrorCode & 64) != 0)
                fontRendererObj.drawString("Incomplete Structure.", 9, 16, 16448255);

            if (mContainer != null) {
                if ((((GT_Container_MultiMachine) mContainer).mDisplayErrorCode & 1) != 0)
                    fontRendererObj.drawString("Pipe is loose.", 10, 23, 16448255);
                if ((((GT_Container_MultiMachine) mContainer).mDisplayErrorCode & 2) != 0)
                    fontRendererObj.drawString("Screws are missing.", 10, 31, 16448255);
                if ((((GT_Container_MultiMachine) mContainer).mDisplayErrorCode & 4) != 0)
                    fontRendererObj.drawString("Something is stuck.", 10, 39, 16448255);
                if ((((GT_Container_MultiMachine) mContainer).mDisplayErrorCode & 8) != 0)
                    fontRendererObj.drawString("Platings are dented.", 10, 47, 16448255);
                if ((((GT_Container_MultiMachine) mContainer).mDisplayErrorCode & 16) != 0)
                    fontRendererObj.drawString("Circuitry burned out.", 10, 55, 16448255);
                if ((((GT_Container_MultiMachine) mContainer).mDisplayErrorCode & 32) != 0)
                    fontRendererObj.drawString("That doesn't belong there.", 10, 63, 16448255);
                
                    } else {
                        fontRendererObj.drawString("Running perfectly.", 10, 16, 16448255);
                    }

                }
            }

    @Override
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
        super.drawGuiContainerBackgroundLayer(par1, par2, par3);
        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;
        drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
        if (this.mContainer != null) {
            double tScale = (double) this.mContainer.mProgressTime / (double) this.mContainer.mMaxProgressTime;
            drawTexturedModalRect(x + 5, y + 156, 0, 251, Math.min(147, (int) (tScale * 148)), 5);
        }
    }
}
