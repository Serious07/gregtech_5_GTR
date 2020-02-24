package gregtech.api.items;

import gregtech.api.util.GT_ModHandler;

public class GT_FarmScheme_Item extends GT_Generic_Item {
	public GT_FarmScheme_Item(String aUnlocalized, String aEnglish, String aEnglishTooltip) {
		super(aUnlocalized, aEnglish, aEnglishTooltip);
		
		long bits = GT_ModHandler.RecipeBits.BUFFERED | GT_ModHandler.RecipeBits.NOT_REMOVABLE;
	}
}