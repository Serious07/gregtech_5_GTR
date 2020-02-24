package gregtech.api.metatileentity.implementations;

import cofh.api.energy.IEnergyReceiver;
import com.google.common.collect.Sets;
import gregtech.GT_Mod;
import gregtech.api.GregTech_API;
import gregtech.api.enums.Dyes;
import gregtech.api.enums.Materials;
import gregtech.api.enums.TextureSet;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.metatileentity.IMetaTileEntityCable;
import gregtech.api.interfaces.tileentity.ICoverable;
import gregtech.api.interfaces.tileentity.IEnergyConnected;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.BaseMetaPipeEntity;
import gregtech.api.metatileentity.MetaPipeEntity;
import gregtech.api.objects.GT_RenderedTexture;
import gregtech.api.util.GT_CoverBehavior;
import gregtech.api.util.GT_ModHandler;
import gregtech.api.util.GT_Utility;
import gregtech.common.GT_Client;
import gregtech.common.covers.GT_Cover_SolarPanel;
import gregtech.loaders.materialprocessing.ProcessingModSupport;
import gregtech.loaders.postload.PartP2PGTPower;
import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.reactor.IReactorChamber;
import micdoodle8.mods.galacticraft.api.power.EnergySource;
import micdoodle8.mods.galacticraft.api.power.EnergySource.EnergySourceAdjacent;
import micdoodle8.mods.galacticraft.api.power.IEnergyHandlerGC;
import micdoodle8.mods.galacticraft.api.transmission.NetworkType;
import micdoodle8.mods.galacticraft.api.transmission.tile.IConnector;
import micdoodle8.mods.galacticraft.core.energy.EnergyConfigHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import appeng.api.parts.IPartHost;
import codechicken.multipart.asm.StackAnalyser.Const;

import static gregtech.api.enums.GT_Values.VN;

public class GT_MetaPipeEntity_Cable extends MetaPipeEntity implements IMetaTileEntityCable {
	public final float mThickNess;
	public final Materials mMaterial;
	public final long mCableLossPerMeter, mAmperage, mVoltage;
	public final boolean mInsulated, mCanShock;
	public long mTransferredAmperage = 0, mTransferredAmperageLast20 = 0, mTransferredVoltageLast20 = 0;
	public long mRestRF;
	public short mOverheat;
	private boolean mCheckConnections = !GT_Mod.gregtechproxy.gt6Cable;

	private boolean isPlaced = true;

	public static final int CONNECTION_DELAY = 1;

	public static HashMap<IMetaTileEntityCable, GT_MetaPipeEntity_CableChain> startCableCash = new HashMap<IMetaTileEntityCable, GT_MetaPipeEntity_CableChain>();

	public static HashSet<TileEntity> energyEmmiters = new HashSet<>();
	
	public ArrayList<GT_MetaPipeEntity_CableEnergyEmmitor> cableEnergyEmmiters = new ArrayList<GT_MetaPipeEntity_CableEnergyEmmitor>();
	public boolean isEnrgyEmmitersCashed = false;
	
	public GT_MetaPipeEntity_Cable(int aID, String aName, String aNameRegional, float aThickNess, Materials aMaterial,
			long aCableLossPerMeter, long aAmperage, long aVoltage, boolean aInsulated, boolean aCanShock) {
		super(aID, aName, aNameRegional, 0);
		mThickNess = aThickNess;
		mMaterial = aMaterial;
		mAmperage = aAmperage;
		mVoltage = aVoltage;
		mInsulated = aInsulated;
		mCanShock = aCanShock;
		mCableLossPerMeter = aCableLossPerMeter;
	}

	public GT_MetaPipeEntity_Cable(String aName, float aThickNess, Materials aMaterial, long aCableLossPerMeter,
			long aAmperage, long aVoltage, boolean aInsulated, boolean aCanShock) {
		super(aName, 0);
		mThickNess = aThickNess;
		mMaterial = aMaterial;
		mAmperage = aAmperage;
		mVoltage = aVoltage;
		mInsulated = aInsulated;
		mCanShock = aCanShock;
		mCableLossPerMeter = aCableLossPerMeter;
	}

	@Override
	public byte getTileEntityBaseType() {
		return (byte) (mInsulated ? 9 : 8);
	}

	@Override
	public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
		return new GT_MetaPipeEntity_Cable(mName, mThickNess, mMaterial, mCableLossPerMeter, mAmperage, mVoltage,
				mInsulated, mCanShock);
	}

	private int delay = -1;
	private boolean needUpdateNearestCables = false;

	public void UpdateNearestCables(int delayInSeconds) {
		needUpdateNearestCables = true;
		delay = delayInSeconds * 20;
	}

	public void UpdateNearestCables() {
		// System.out.println("this != null: " + (this != null));
		// System.out.println("this instanceof IMetaTileEntityCable: " + (this
		// instanceof IMetaTileEntityCable));

		if (this != null && this instanceof IMetaTileEntityCable) {
			UpdateCablesChain((IMetaTileEntityCable) this);
		}

		// �������� �������� ������� ���� �����
		for (byte i = 0; i < 6; i++) {
			TileEntity entity = getBaseMetaTileEntity().getTileEntityAtSide(i);
			// System.out.println("Check tile entity " + i + " " + entity);
			if (entity != null && entity instanceof BaseMetaPipeEntity) {
				BaseMetaPipeEntity baseMetaPipeEntityTmp = (BaseMetaPipeEntity) entity;
				if(baseMetaPipeEntityTmp.getMetaTileEntity() instanceof GT_MetaPipeEntity_Cable) {
					GT_MetaPipeEntity_Cable cable = (GT_MetaPipeEntity_Cable) (baseMetaPipeEntityTmp.getMetaTileEntity());
					// System.out.println("UpdateNearestCables() entity instanceof
					// IMetaTileEntityCable");
					UpdateCablesChain(cable);
				}
			}
		}
	}

	@Override
	public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, byte aSide, byte aConnections,
			byte aColorIndex, boolean aConnected, boolean aRedstone) {
		if (!mInsulated)
			return new ITexture[] { new GT_RenderedTexture(mMaterial.mIconSet.mTextures[TextureSet.INDEX_wire],
					Dyes.getModulation(aColorIndex, mMaterial.mRGBa)) };
		if (aConnected) {
			float tThickNess = getThickNess();
			if (tThickNess < 0.124F)
				return new ITexture[] { new GT_RenderedTexture(Textures.BlockIcons.INSULATION_FULL,
						Dyes.getModulation(aColorIndex, Dyes.CABLE_INSULATION.mRGBa)) };
			if (tThickNess < 0.374F)// 0.375 x1
				return new ITexture[] {
						new GT_RenderedTexture(mMaterial.mIconSet.mTextures[TextureSet.INDEX_wire], mMaterial.mRGBa),
						new GT_RenderedTexture(Textures.BlockIcons.INSULATION_TINY,
								Dyes.getModulation(aColorIndex, Dyes.CABLE_INSULATION.mRGBa)) };
			if (tThickNess < 0.499F)// 0.500 x2
				return new ITexture[] {
						new GT_RenderedTexture(mMaterial.mIconSet.mTextures[TextureSet.INDEX_wire], mMaterial.mRGBa),
						new GT_RenderedTexture(Textures.BlockIcons.INSULATION_SMALL,
								Dyes.getModulation(aColorIndex, Dyes.CABLE_INSULATION.mRGBa)) };
			if (tThickNess < 0.624F)// 0.625 x4
				return new ITexture[] {
						new GT_RenderedTexture(mMaterial.mIconSet.mTextures[TextureSet.INDEX_wire], mMaterial.mRGBa),
						new GT_RenderedTexture(Textures.BlockIcons.INSULATION_MEDIUM,
								Dyes.getModulation(aColorIndex, Dyes.CABLE_INSULATION.mRGBa)) };
			if (tThickNess < 0.749F)// 0.750 x8
				return new ITexture[] {
						new GT_RenderedTexture(mMaterial.mIconSet.mTextures[TextureSet.INDEX_wire], mMaterial.mRGBa),
						new GT_RenderedTexture(Textures.BlockIcons.INSULATION_MEDIUM_PLUS,
								Dyes.getModulation(aColorIndex, Dyes.CABLE_INSULATION.mRGBa)) };
			if (tThickNess < 0.874F)// 0.825 x12
				return new ITexture[] {
						new GT_RenderedTexture(mMaterial.mIconSet.mTextures[TextureSet.INDEX_wire], mMaterial.mRGBa),
						new GT_RenderedTexture(Textures.BlockIcons.INSULATION_LARGE,
								Dyes.getModulation(aColorIndex, Dyes.CABLE_INSULATION.mRGBa)) };
			return new ITexture[] {
					new GT_RenderedTexture(mMaterial.mIconSet.mTextures[TextureSet.INDEX_wire], mMaterial.mRGBa),
					new GT_RenderedTexture(Textures.BlockIcons.INSULATION_HUGE,
							Dyes.getModulation(aColorIndex, Dyes.CABLE_INSULATION.mRGBa)) };
		}
		return new ITexture[] { new GT_RenderedTexture(Textures.BlockIcons.INSULATION_FULL,
				Dyes.getModulation(aColorIndex, Dyes.CABLE_INSULATION.mRGBa)) };
	}

	@Override
	public void onEntityCollidedWithBlock(World aWorld, int aX, int aY, int aZ, Entity aEntity) {
		if (mCanShock && (((BaseMetaPipeEntity) getBaseMetaTileEntity()).mConnections & -128) == 0
				&& aEntity instanceof EntityLivingBase
				&& !isCoverOnSide((BaseMetaPipeEntity) getBaseMetaTileEntity(), (EntityLivingBase) aEntity))
			GT_Utility.applyElectricityDamage((EntityLivingBase) aEntity, mTransferredVoltageLast20,
					mTransferredAmperageLast20);
	}

	@Override
	public boolean isSimpleMachine() {
		return true;
	}

	@Override
	public boolean isFacingValid(byte aFacing) {
		return false;
	}

	@Override
	public boolean isValidSlot(int aIndex) {
		return true;
	}

	@Override
	public final boolean renderInside(byte aSide) {
		return false;
	}

	@Override
	public int getProgresstime() {
		return (int) mTransferredAmperage * 64;
	}

	@Override
	public int maxProgresstime() {
		return (int) mAmperage * 64;
	}

	private void pullFromIc2EnergySources(IGregTechTileEntity aBaseMetaTileEntity) {
		if (!GT_Mod.gregtechproxy.ic2EnergySourceCompat)
			return;
		
		if(isEnrgyEmmitersCashed == false) {
			for (byte aSide = 0; aSide < 6; aSide++) {
				if (isConnectedAtSide(aSide)) {
					final TileEntity tTileEntity = aBaseMetaTileEntity.getTileEntityAtSide(aSide);
					final TileEntity tEmitter;
	
					if (tTileEntity instanceof IReactorChamber) {
						tEmitter = (TileEntity) ((IReactorChamber) tTileEntity).getReactor();
					} else {
						tEmitter = (tTileEntity == null || tTileEntity instanceof IEnergyTile || EnergyNet.instance == null)
								? tTileEntity
								: EnergyNet.instance.getTileEntity(tTileEntity.getWorldObj(), tTileEntity.xCoord,
										tTileEntity.yCoord, tTileEntity.zCoord);
					}
	
					if (tEmitter instanceof IEnergySource) {
						final GT_CoverBehavior coverBehavior = aBaseMetaTileEntity.getCoverBehaviorAtSide(aSide);
						final int coverId = aBaseMetaTileEntity.getCoverIDAtSide(aSide),
								coverData = aBaseMetaTileEntity.getCoverDataAtSide(aSide);
						final ForgeDirection tDirection = ForgeDirection.getOrientation(GT_Utility.getOppositeSide(aSide));
	
						if (((IEnergySource) tEmitter).emitsEnergyTo((TileEntity) aBaseMetaTileEntity, tDirection)
								&& coverBehavior.letsEnergyIn(aSide, coverId, coverData, aBaseMetaTileEntity)) {
							
							TryDrawEnergy(aBaseMetaTileEntity, (IEnergySource) tEmitter, aSide);
						}
	
						if (tEmitter != null) {
							energyEmmiters.add(tEmitter);
							cableEnergyEmmiters.add(new GT_MetaPipeEntity_CableEnergyEmmitor((IEnergySource)tEmitter, aSide));
						}
					}
				}
			}
		} else {
			for(GT_MetaPipeEntity_CableEnergyEmmitor energySource : cableEnergyEmmiters) {
				TryDrawEnergy(aBaseMetaTileEntity, energySource.energySource, energySource.aSide);
			}
		}
		
		isEnrgyEmmitersCashed = true;
	}
	
	private void TryDrawEnergy(IGregTechTileEntity aBaseMetaTileEntity, IEnergySource tEmitter, byte aSide) {
		final long tEU = (long) tEmitter.getOfferedEnergy();
		long transferedAmerage = transferElectricity(aSide, tEU, 1, Sets.newHashSet((TileEntity) aBaseMetaTileEntity));
		if (transferedAmerage > 0) tEmitter.drawEnergy(tEU);
	}
	
	@Override
	public long injectEnergyUnits(byte aSide, long aVoltage, long aAmperage) {
		if (!isConnectedAtSide(aSide) && aSide != 6) {
			return 0;
		}

		if (!getBaseMetaTileEntity().getCoverBehaviorAtSide(aSide).letsEnergyIn(aSide,
				getBaseMetaTileEntity().getCoverIDAtSide(aSide), getBaseMetaTileEntity().getCoverDataAtSide(aSide),
				getBaseMetaTileEntity())) {
			return 0;
		}
		
		long transferedAmperes = transferElectricity(aSide, aVoltage, aAmperage,
				Sets.newHashSet((TileEntity) getBaseMetaTileEntity()));

		return transferedAmperes;
	}

	@Override
	@Deprecated
	public long transferElectricity(byte aSide, long aVoltage, long aAmperage,
			ArrayList<TileEntity> aAlreadyPassedTileEntityList) {
		return transferElectricity(aSide, aVoltage, aAmperage, new HashSet<>(aAlreadyPassedTileEntityList));
	}

	private boolean needToFireCable(GT_MetaPipeEntity_Cable cable, long aVoltage, long amperege) {
		if (amperege == 0)
			return false;

		boolean needToFire = aVoltage * amperege > cable.mVoltage * cable.mAmperage;

		if (needToFire) {
			System.out.println("================needToFireCable================");
			System.out.println("aVoltage: " + aVoltage);
			System.out.println("aAmperege: " + amperege);
			System.out.println("cable.mVoltage: " + cable.mVoltage);
			System.out.println("cable.mAmperage: " + cable.mAmperage);
		}

		return needToFire;

		// return aVoltage * amperege > cable.mVoltage * cable.mAmperage;
	}

	private boolean cableInFire = false;

	// Cashed method of electrisity transport
	@Override
	public long transferElectricity(IMetaTileEntityCable startCable, byte aSide, long aVoltage, long aAmperage,
			HashSet<TileEntity> aAlreadyPassedSet) {
		if (!isConnectedAtSide(aSide) && aSide != 6)
			return 0;

		long rUsedAmperes = 0;

		/*
		 * // Стартовый провод в огне
		 * if(needToFireCable((GT_MetaPipeEntity_Cable)startCable, aVoltage,
		 * rUsedAmperes)) { startCable.getBaseMetaTileEntity().setToFire(); return 0; }
		 * 
		 * // Сегмент проводов после стартового cableInFire = false;
		 * 
		 * if(startCableCash.get(startCable).cableSegments != null &&
		 * startCableCash.get(startCable).cableSegments.containsKey((
		 * GT_MetaPipeEntity_Cable)startCable)) { for(GT_MetaPipeEntity_Cable
		 * segmentCable :
		 * startCableCash.get(startCable).cableSegments.get((GT_MetaPipeEntity_Cable)
		 * startCable)) { if(segmentCable != null && needToFireCable(segmentCable,
		 * aVoltage, rUsedAmperes)) { segmentCable.getBaseMetaTileEntity().setToFire();
		 * cableInFire = true; break; } }
		 * 
		 * if(cableInFire) { return 0; } }
		 */

		if (startCableCash.containsKey(startCable)) {
			cableInFire = false;

			HashMap<GT_MetaPipeEntity_Cable, Long> usedAmperageWithCosumers = new HashMap<GT_MetaPipeEntity_Cable, Long>();

			// Подсчитать количество переданных ампер, ампер на потребитель и передать
			// энергию машинам

			for (GT_MetaPipeEntity_CableCash cableCash : startCableCash.get(startCable).consumers) {
				GT_MetaPipeEntity_Cable cable = cableCash.cable;

				newAmperage = aAmperage - rUsedAmperes;

				rUsedAmperes += insertEnergyInto(cableCash.tTileEntity, cableCash.tSide, cableCash.voltage,
						newAmperage);

				usedAmperageWithCosumers.put(cable, new Long(rUsedAmperes));

				if (rUsedAmperes >= aAmperage)
					break;
			}

			// Процесс расчёта горения проводов

			Iterator it = usedAmperageWithCosumers.entrySet().iterator();

			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				long usedAmpres = (long) pair.getValue();

				GT_MetaPipeEntity_Cable cable = (GT_MetaPipeEntity_Cable) pair.getKey();

				if (needToFireCable(cable, aVoltage, rUsedAmperes - usedAmpres)) {
					FireCable(cable, (GT_MetaPipeEntity_Cable) startCable);
					break;
				}

				if (startCable != null && cable != null && startCableCash != null
						&& startCableCash.get(startCable).cableSegments != null
						&& startCableCash.get(startCable).cableSegments.containsKey(cable)) {
					for (GT_MetaPipeEntity_Cable segmentCable : startCableCash.get(startCable).cableSegments
							.get(cable)) {
						if (segmentCable != null
								&& needToFireCable(segmentCable, aVoltage, rUsedAmperes - usedAmpres)) {
							FireCable(segmentCable, ((GT_MetaPipeEntity_Cable) startCable));

							cableInFire = true;
							break;
						}
					}

					if (cableInFire)
						break;
				}
			}

		} else {
			final IGregTechTileEntity baseMetaTile = getBaseMetaTileEntity();

			GT_MetaPipeEntity_CableChain cableCashList = recalculateCables(startCable, this,
					new GT_MetaPipeEntity_CableChain(), aAlreadyPassedSet, aAmperage, rUsedAmperes, aVoltage, aSide,
					baseMetaTile);
			
			if(cableCashList != null) {
				cableCashList.consumers.sort(Comparator.comparing(GT_MetaPipeEntity_CableCash::getDistance));
	
				rUsedAmperes = cableCashList.rUsedAmperes;
	
				startCableCash.put(startCable, cableCashList);
			}
		}

		return rUsedAmperes;
	}

	private void FireCable(GT_MetaPipeEntity_Cable cableToFire, GT_MetaPipeEntity_Cable startCable) {
		cableToFire.getBaseMetaTileEntity().setToFire();

		startCable.UpdateNearestCables();
		startCable.checkConnection(GT_MetaPipeEntity_Cable.CONNECTION_DELAY);
	}

	private float getDistance(int x1, int y1, int z1, int x2, int y2, int z2) {
		return (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y1 - y1) + (z2 - z1) * (z2 - z1));
	}

	public static boolean isCableInChain(IMetaTileEntityCable cable) {
		Iterator it = GT_MetaPipeEntity_Cable.startCableCash.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			if (((GT_MetaPipeEntity_CableChain) pair.getValue()).isCableInChain(cable)) {
				return true;
			}

			it.remove(); // avoids a ConcurrentModificationException
		}

		return false;
	}

	public static GT_MetaPipeEntity_CableChain getNetworkInfo(IMetaTileEntityCable cable) {
		Iterator it = GT_MetaPipeEntity_Cable.startCableCash.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			if (((GT_MetaPipeEntity_CableChain) pair.getValue()).isCableInChain(cable)) {
				return (GT_MetaPipeEntity_CableChain) pair.getValue();
			}

			it.remove(); // avoids a ConcurrentModificationException
		}

		return null;
	}

	public static ArrayList<IMetaTileEntityCable> UpdateCablesChainQueue = new ArrayList<IMetaTileEntityCable>();
	public static boolean isUpdateCablesChainAtWork = false;

	public static void UpdateCablesChain(IMetaTileEntityCable cable) {
		if (isUpdateCablesChainAtWork == false) {
			isUpdateCablesChainAtWork = true;

			if (cable != null) {
				if(cable instanceof GT_MetaPipeEntity_Cable) {
					((GT_MetaPipeEntity_Cable)cable).isEnrgyEmmitersCashed = false;
					((GT_MetaPipeEntity_Cable)cable).cableEnergyEmmiters.clear();
				}
				
				ArrayList<IMetaTileEntityCable> keysToRemove = new ArrayList<IMetaTileEntityCable>();

				Iterator it = GT_MetaPipeEntity_Cable.startCableCash.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry pair = (Map.Entry) it.next();
					if (((GT_MetaPipeEntity_CableChain) pair.getValue()).isCableInChain(cable)) {
						keysToRemove.add((IMetaTileEntityCable) pair.getKey());
					}

					it.remove(); // avoids a ConcurrentModificationException
				}

				for (IMetaTileEntityCable c : keysToRemove) {
					// System.out.println("Remove key from cash");

					GT_MetaPipeEntity_Cable.startCableCash.remove(c);
				}

				keysToRemove.clear();
			}

			// Создать очередь обработки проводов чтобы не создавать конфликтов
			if (UpdateCablesChainQueue.contains(cable)) {
				UpdateCablesChainQueue.remove(cable);
			}

			if (UpdateCablesChainQueue.size() == 0) {
				isUpdateCablesChainAtWork = false;
			} else {
				isUpdateCablesChainAtWork = false;
				UpdateCablesChain(UpdateCablesChainQueue.get(0));
			}
		} else {
			UpdateCablesChainQueue.add(cable);
		}
	}

	private int x1, y1, z1, x2, y2, z2;
	private long newAmperage;
	private float distance;

	public GT_MetaPipeEntity_CableChain recalculateCables(IMetaTileEntityCable startCable,
			GT_MetaPipeEntity_Cable lastConsumerCable, GT_MetaPipeEntity_CableChain result,
			HashSet<TileEntity> aAlreadyPassedSet, long aAmperage, long rUsedAmperes, long aVoltage, byte aSide,
			IGregTechTileEntity baseMetaTile) {
		/*
		 * if (!isConnectedAtSide(aSide) && aSide != 6) return result;
		 */

		/*
		 * if(this instanceof IMetaTileEntityCable) {
		 * result.cablesChain.add((IMetaTileEntityCable)this); }
		 */
		
		byte i = (byte) ((((aSide / 2) * 2) + 2) % 6);

		long tmpRUsedAmpers = 0;

		aVoltage -= mCableLossPerMeter;
		for (byte j = 0; j < 6 && aAmperage > rUsedAmperes; j++, i = (byte) ((i + 1) % 6)) {
			if (/* i != aSide && isConnectedAtSide(i) && */
			baseMetaTile.getCoverBehaviorAtSide(i).letsEnergyOut(i, baseMetaTile.getCoverIDAtSide(i),
					baseMetaTile.getCoverDataAtSide(i), baseMetaTile)) {
				final TileEntity tTileEntity = baseMetaTile.getTileEntityAtSide(i);

				if (tTileEntity != null && aAlreadyPassedSet.add(tTileEntity)) {
					final byte tSide = GT_Utility.getOppositeSide(i);
					final IGregTechTileEntity tBaseMetaTile = tTileEntity instanceof IGregTechTileEntity
							? ((IGregTechTileEntity) tTileEntity)
							: null;
					final IMetaTileEntity tMeta = tBaseMetaTile != null ? tBaseMetaTile.getMetaTileEntity() : null;

					if (tMeta instanceof IMetaTileEntityCable) {
						if (tBaseMetaTile.getCoverBehaviorAtSide(tSide).letsEnergyIn(tSide,
								tBaseMetaTile.getCoverIDAtSide(tSide), tBaseMetaTile.getCoverDataAtSide(tSide),
								tBaseMetaTile) /*
												 * && ((IGregTechTileEntity) tTileEntity).getTimer() > 50
												 */) {
							// rUsedAmperes += ((IMetaTileEntityCable) ((IGregTechTileEntity) tTileEntity)
							// .getMetaTileEntity()).transferElectricity(tSide, aVoltage, aAmperage -
							// rUsedAmperes, aAlreadyPassedSet);

							// Задать сегменты сети
							if (result.isConsumerCable(this)) {
								lastConsumerCable = this;
							}

							if (result.cableSegments.containsKey(lastConsumerCable)) {
								if (result.cableSegments.get(lastConsumerCable) == null) {
									result.cableSegments.put(lastConsumerCable,
											new ArrayList<GT_MetaPipeEntity_Cable>());
								}
							} else {
								result.cableSegments.put(lastConsumerCable, new ArrayList<GT_MetaPipeEntity_Cable>());
							}

							ArrayList<GT_MetaPipeEntity_Cable> cablesSegment = result.cableSegments
									.get(lastConsumerCable);
							cablesSegment.add(this);
							result.cableSegments.put(lastConsumerCable, cablesSegment);

							// Добавить провода в цепь проводов
							result.cablesChain.add((IMetaTileEntityCable) tMeta);
							result = ((GT_MetaPipeEntity_Cable) tMeta).recalculateCables(startCable, lastConsumerCable,
									result, aAlreadyPassedSet, aAmperage, rUsedAmperes, aVoltage, aSide, tBaseMetaTile);
							tmpRUsedAmpers += result.rUsedAmperes;
						}
					} else {
						/*if (energyEmmiters.contains(tTileEntity) == false 
														  && ((tTileEntity instanceof IEnergyConnected &&
														  ((IEnergyConnected)tTileEntity).inputEnergyFrom(tSide)
														  ) || tTileEntity instanceof appeng.tile.powersink.IC2)) {*/
							x1 = startCable.getBaseMetaTileEntity().getXCoord();
							y1 = startCable.getBaseMetaTileEntity().getYCoord();
							z1 = startCable.getBaseMetaTileEntity().getZCoord();

							x2 = tTileEntity.xCoord;
							y2 = tTileEntity.yCoord;
							z2 = tTileEntity.zCoord;

							distance = getDistance(x1, y1, z1, x2, y2, z2);

							newAmperage = aAmperage - rUsedAmperes;

							result.consumers.add(new GT_MetaPipeEntity_CableCash(tTileEntity, tSide, aVoltage,
									newAmperage, distance, this));

							tmpRUsedAmpers += insertEnergyInto(tTileEntity, tSide, aVoltage, newAmperage);
						//}
					}

				}
			}
		}

		mTransferredAmperage += tmpRUsedAmpers;
		mTransferredVoltageLast20 = Math.max(mTransferredVoltageLast20, aVoltage);
		mTransferredAmperageLast20 = Math.max(mTransferredAmperageLast20, mTransferredAmperage);

		if (aVoltage > mVoltage || mTransferredAmperage > mAmperage) {
			if (mOverheat > GT_Mod.gregtechproxy.mWireHeatingTicks * 100) {
				getBaseMetaTileEntity().setToFire();
			} else {
				mOverheat += 100;
			}
			result.rUsedAmperes += aAmperage;
		}
		result.rUsedAmperes += rUsedAmperes;
		
		if(result.cablesChain.contains(this) == false && this == startCable) {
			result.cablesChain.add(this);
		}
		
		return result;
	}

	@Override
	public long transferElectricity(byte aSide, long aVoltage, long aAmperage, HashSet<TileEntity> aAlreadyPassedSet) {
		if (!isConnectedAtSide(aSide) && aSide != 6)
			return 0;

		long rUsedAmperes = 0;
		final IGregTechTileEntity baseMetaTile = getBaseMetaTileEntity();

		byte i = (byte) ((((aSide / 2) * 2) + 2) % 6); // this bit of trickery makes sure a direction goes to the next
														// cardinal pair. IE, NS goes to E, EW goes to U, UD goes to N.
														// It's a lame way to make sure locally connected machines on a
														// wire get EU first.

		aVoltage -= mCableLossPerMeter;
		if (aVoltage > 0)
			for (byte j = 0; j < 6 && aAmperage > rUsedAmperes; j++, i = (byte) ((i + 1) % 6))
				if (i != aSide && isConnectedAtSide(i) && baseMetaTile.getCoverBehaviorAtSide(i).letsEnergyOut(i,
						baseMetaTile.getCoverIDAtSide(i), baseMetaTile.getCoverDataAtSide(i), baseMetaTile)) {
					final TileEntity tTileEntity = baseMetaTile.getTileEntityAtSide(i);

					if (tTileEntity != null && aAlreadyPassedSet.add(tTileEntity)) {
						final byte tSide = GT_Utility.getOppositeSide(i);
						final IGregTechTileEntity tBaseMetaTile = tTileEntity instanceof IGregTechTileEntity
								? ((IGregTechTileEntity) tTileEntity)
								: null;
						final IMetaTileEntity tMeta = tBaseMetaTile != null ? tBaseMetaTile.getMetaTileEntity() : null;

						if (tMeta instanceof IMetaTileEntityCable) {
							if (tBaseMetaTile.getCoverBehaviorAtSide(tSide).letsEnergyIn(tSide,
									tBaseMetaTile.getCoverIDAtSide(tSide), tBaseMetaTile.getCoverDataAtSide(tSide),
									tBaseMetaTile) && ((IGregTechTileEntity) tTileEntity).getTimer() > 50) {
								rUsedAmperes += ((IMetaTileEntityCable) ((IGregTechTileEntity) tTileEntity)
										.getMetaTileEntity()).transferElectricity(tSide, aVoltage,
												aAmperage - rUsedAmperes, aAlreadyPassedSet);
							}
						} else {
							rUsedAmperes += insertEnergyInto(tTileEntity, tSide, aVoltage, aAmperage - rUsedAmperes);
						}

					}
				}
		mTransferredAmperage += rUsedAmperes;
		mTransferredVoltageLast20 = Math.max(mTransferredVoltageLast20, aVoltage);
		mTransferredAmperageLast20 = Math.max(mTransferredAmperageLast20, mTransferredAmperage);
		if (aVoltage > mVoltage || mTransferredAmperage > mAmperage) {
			if (mOverheat > GT_Mod.gregtechproxy.mWireHeatingTicks * 100) {
				getBaseMetaTileEntity().setToFire();
			} else {
				mOverheat += 100;
			}
			return aAmperage;
		}
		return rUsedAmperes;
	}

	public long insertEnergyInto(TileEntity tTileEntity, byte tSide, long aVoltage, long aAmperage) {
		if (aAmperage == 0 || tTileEntity == null)
			return 0;

		final IGregTechTileEntity baseMetaTile = getBaseMetaTileEntity();
		final ForgeDirection tDirection = ForgeDirection.getOrientation(tSide);

		if (tTileEntity instanceof IEnergyConnected) {
			return ((IEnergyConnected) tTileEntity).injectEnergyUnits(tSide, aVoltage, aAmperage);
		}

		// AE2 Compat
		if (GT_Mod.gregtechproxy.mAE2Integration && tTileEntity instanceof appeng.tile.powersink.IC2) {
			if (((appeng.tile.powersink.IC2) tTileEntity).acceptsEnergyFrom((TileEntity) baseMetaTile, tDirection)) {
				long rUsedAmperes = 0;
				while (aAmperage > rUsedAmperes && ((appeng.tile.powersink.IC2) tTileEntity).getDemandedEnergy() > 0
						&& ((appeng.tile.powersink.IC2) tTileEntity).injectEnergy(tDirection, aVoltage,
								aVoltage) <= aVoltage)
					rUsedAmperes++;

				return rUsedAmperes;
			}
			return 0;
		}

		// GC Compat
		if (GregTech_API.mGalacticraft) {
			if (tTileEntity instanceof IEnergyHandlerGC) {
				if (!(tTileEntity instanceof IConnector)
						|| ((IConnector) tTileEntity).canConnect(tDirection, NetworkType.POWER)) {
					EnergySource eSource = (EnergySource) GT_Utility.callConstructor(
							"micdoodle8.mods.galacticraft.api.power.EnergySource.EnergySourceAdjacent", 0, null, false,
							new Object[] { tDirection });

					float tSizeToReceive = aVoltage * EnergyConfigHandler.IC2_RATIO,
							tStored = ((IEnergyHandlerGC) tTileEntity).getEnergyStoredGC(eSource);
					if (tSizeToReceive >= tStored
							|| tSizeToReceive <= ((IEnergyHandlerGC) tTileEntity).getMaxEnergyStoredGC(eSource)
									- tStored) {
						float tReceived = ((IEnergyHandlerGC) tTileEntity).receiveEnergyGC(eSource, tSizeToReceive,
								false);
						if (tReceived > 0) {
							tSizeToReceive -= tReceived;
							while (tSizeToReceive > 0) {
								tReceived = ((IEnergyHandlerGC) tTileEntity).receiveEnergyGC(eSource, tSizeToReceive,
										false);
								if (tReceived < 1)
									break;
								tSizeToReceive -= tReceived;
							}
							return 1;
						}
					}
				}
				return 0;
			}
		}

		// IC2 Compat
		{
			final TileEntity tIc2Acceptor = (tTileEntity instanceof IEnergyTile || EnergyNet.instance == null)
					? tTileEntity
					: EnergyNet.instance.getTileEntity(tTileEntity.getWorldObj(), tTileEntity.xCoord,
							tTileEntity.yCoord, tTileEntity.zCoord);

			if (tIc2Acceptor instanceof IEnergySink
					&& ((IEnergySink) tIc2Acceptor).acceptsEnergyFrom((TileEntity) baseMetaTile, tDirection)) {
				long rUsedAmperes = 0;
				while (aAmperage > rUsedAmperes && ((IEnergySink) tIc2Acceptor).getDemandedEnergy() > 0
						&& ((IEnergySink) tIc2Acceptor).injectEnergy(tDirection, aVoltage, aVoltage) <= aVoltage)
					rUsedAmperes++;
				return rUsedAmperes;
			}
		}

		// RF Compat
		if (GregTech_API.mOutputRF && tTileEntity instanceof IEnergyReceiver) {
			final IEnergyReceiver rfReceiver = (IEnergyReceiver) tTileEntity;
			long rfOUT = aVoltage * GregTech_API.mEUtoRF / 100, rUsedAmperes = 0;
			int rfOut = rfOUT > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) rfOUT;

			if (rfReceiver.receiveEnergy(tDirection, rfOut, true) == rfOut) {
				rfReceiver.receiveEnergy(tDirection, rfOut, false);
				rUsedAmperes++;
			} else if (rfReceiver.receiveEnergy(tDirection, rfOut, true) > 0) {
				if (mRestRF == 0) {
					int RFtrans = rfReceiver.receiveEnergy(tDirection, (int) rfOut, false);
					rUsedAmperes++;
					mRestRF = rfOut - RFtrans;
				} else {
					int RFtrans = rfReceiver.receiveEnergy(tDirection, (int) mRestRF, false);
					mRestRF = mRestRF - RFtrans;
				}
			}
			if (GregTech_API.mRFExplosions && rfReceiver.getMaxEnergyStored(tDirection) < rfOut * 600) {
				if (rfOut > 32 * GregTech_API.mEUtoRF / 100)
					this.doExplosion(rfOut);
			}
			return rUsedAmperes;
		}

		return 0;
	}

	@Override
	public void onCreated(ItemStack aStack, World aWorld, EntityPlayer aPlayer) {
		super.onCreated(aStack, aWorld, aPlayer);

		// System.out.println("onCreated");

		UpdateNearestCables();
		checkConnection(CONNECTION_DELAY);
	}

	@Override
	public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {
		// TODO Auto-generated method stub
		super.onFirstTick(aBaseMetaTileEntity);

		// System.out.println("onFirstTick");

		UpdateNearestCables();
		checkConnection(CONNECTION_DELAY);
	}

	public void checkConnection() {
		if (!GT_Mod.gregtechproxy.gt6Cable || mCheckConnections)
			checkConnections();
	}

	private boolean needToCheck;
	private int checkDealy = -1;

	public void checkConnection(int delay) {
		checkDealy = 20 * delay;
		needToCheck = true;
	}

	@Override
	public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
		if (aBaseMetaTileEntity.isServerSide()) {
			if (aTick > 50 && GT_Mod.gregtechproxy.ic2EnergySourceCompat)
				pullFromIc2EnergySources(aBaseMetaTileEntity);

			mTransferredAmperage = 0;
			if (mOverheat > 0)
				mOverheat--;

			if (aTick % 20 == 0) {
				mTransferredVoltageLast20 = 0;
				mTransferredAmperageLast20 = 0;

				if (needToCheck) {
					if (checkDealy > 0) {
						checkDealy -= 20;

						if (checkDealy <= 0) {
							needToCheck = false;
							checkDealy = -1;

							checkConnection();
						}
					}
				}

				// checkConnection();

				// if (!GT_Mod.gregtechproxy.gt6Cable || mCheckConnections) checkConnections();
			}

			// TODO: Обновленние с задержкой нигде не используется поэтому закомментирован
			// чтобы не вызывать лищний раз
			// NeedUpdateCableWithDelay();

			// ClearListOfEmmiters();
		} else if (aBaseMetaTileEntity.isClientSide() && GT_Client.changeDetected == 4)
			aBaseMetaTileEntity.issueTextureUpdate();
	}

	private void NeedUpdateCableWithDelay() {
		if (needUpdateNearestCables) {
			if (delay > 0) {
				delay--;
			} else {
				needUpdateNearestCables = false;
				delay = -1;
				UpdateNearestCables();
			}
		}
	}

	private void ClearListOfEmmiters() {
		HashSet<TileEntity> entityToRemove = new HashSet<TileEntity>();

		for (TileEntity entity : energyEmmiters) {
			if (entity == null) {
				entityToRemove.add(entity);
			}
		}

		for (TileEntity entity : entityToRemove) {
			energyEmmiters.remove(entity);
		}

		entityToRemove.clear();
	}

	/*
	 * @Override public void onRemoval() { super.onRemoval();
	 * 
	 * System.out.println("onRemoval");
	 * 
	 * UpdateNearestCables(); }
	 */

	// @Override
	// public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity,
	// EntityPlayer aPlayer, byte aSide, float aX,
	// float aY, float aZ) {
	//
	// // System.out.println("onRightclick");
	//
	// UpdateNearestCables();
	//
	// return super.onRightclick(aBaseMetaTileEntity, aPlayer, aSide, aX, aY, aZ);
	// }
	//
	// @Override
	// public void onLeftclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer
	// aPlayer) {
	// // System.out.println("onLeftclick");
	//
	// UpdateNearestCables();
	//
	// super.onLeftclick(aBaseMetaTileEntity, aPlayer);
	// }

	@Override
	public boolean onWireCutterRightClick(byte aSide, byte aWrenchingSide, EntityPlayer aPlayer, float aX, float aY,
			float aZ) {
		if (GT_Mod.gregtechproxy.gt6Cable
				&& GT_ModHandler.damageOrDechargeItem(aPlayer.inventory.getCurrentItem(), 1, 500, aPlayer)) {
			if (isConnectedAtSide(aWrenchingSide)) {
				disconnect(aWrenchingSide);
				GT_Utility.sendChatToPlayer(aPlayer, trans("215", "Disconnected"));
			} else if (!GT_Mod.gregtechproxy.costlyCableConnection) {
				if (connect(aWrenchingSide) > 0)
					GT_Utility.sendChatToPlayer(aPlayer, trans("214", "Connected"));
			}
			return true;
		}
		return false;
	}

	public boolean onSolderingToolRightClick(byte aSide, byte aWrenchingSide, EntityPlayer aPlayer, float aX, float aY,
			float aZ) {
		if (GT_Mod.gregtechproxy.gt6Cable
				&& GT_ModHandler.damageOrDechargeItem(aPlayer.inventory.getCurrentItem(), 1, 500, aPlayer)) {
			if (isConnectedAtSide(aWrenchingSide)) {
				disconnect(aWrenchingSide);
				GT_Utility.sendChatToPlayer(aPlayer, trans("215", "Disconnected"));
			} else if (!GT_Mod.gregtechproxy.costlyCableConnection || GT_ModHandler.consumeSolderingMaterial(aPlayer)) {
				if (connect(aWrenchingSide) > 0)
					GT_Utility.sendChatToPlayer(aPlayer, trans("214", "Connected"));
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean letsIn(GT_CoverBehavior coverBehavior, byte aSide, int aCoverID, int aCoverVariable,
			ICoverable aTileEntity) {
		return coverBehavior.letsEnergyIn(aSide, aCoverID, aCoverVariable, aTileEntity);
	}

	@Override
	public boolean letsOut(GT_CoverBehavior coverBehavior, byte aSide, int aCoverID, int aCoverVariable,
			ICoverable aTileEntity) {
		return coverBehavior.letsEnergyOut(aSide, aCoverID, aCoverVariable, aTileEntity);
	}

	@Override
	public boolean canConnect(byte aSide, TileEntity tTileEntity) {
		final IGregTechTileEntity baseMetaTile = getBaseMetaTileEntity();
		final GT_CoverBehavior coverBehavior = baseMetaTile.getCoverBehaviorAtSide(aSide);
		final byte tSide = GT_Utility.getOppositeSide(aSide);
		final ForgeDirection tDir = ForgeDirection.getOrientation(tSide);

		// GT Machine handling
		if ((tTileEntity instanceof IEnergyConnected) && (((IEnergyConnected) tTileEntity).inputEnergyFrom(tSide, false)
				|| ((IEnergyConnected) tTileEntity).outputsEnergyTo(tSide, false)))
			return true;

		// Solar Panel Compat
		if (coverBehavior instanceof GT_Cover_SolarPanel)
			return true;

		// ((tIsGregTechTileEntity && tIsTileEntityCable) && (tAlwaysLookConnected ||
		// tLetEnergyIn || tLetEnergyOut) ) --> Not needed

		// GC Compat
		if (GregTech_API.mGalacticraft) {
			if (tTileEntity instanceof IEnergyHandlerGC && (!(tTileEntity instanceof IConnector)
					|| ((IConnector) tTileEntity).canConnect(tDir, NetworkType.POWER)))
				return true;
		}

		// AE2-p2p Compat
		if (GT_Mod.gregtechproxy.mAE2Integration) {
			if (tTileEntity instanceof IEnergySource && tTileEntity instanceof IPartHost
					&& ((IPartHost) tTileEntity).getPart(tDir) instanceof PartP2PGTPower
					&& ((IEnergySource) tTileEntity).emitsEnergyTo((TileEntity) baseMetaTile, tDir))
				return true;
			if (tTileEntity instanceof appeng.tile.powersink.IC2
					&& ((appeng.tile.powersink.IC2) tTileEntity).acceptsEnergyFrom((TileEntity) baseMetaTile, tDir))
				return true;
		}

		// IC2 Compat
		{
			final TileEntity ic2Energy;

			if (tTileEntity instanceof IReactorChamber)
				ic2Energy = (TileEntity) ((IReactorChamber) tTileEntity).getReactor();
			else
				ic2Energy = (tTileEntity == null || tTileEntity instanceof IEnergyTile || EnergyNet.instance == null)
						? tTileEntity
						: EnergyNet.instance.getTileEntity(tTileEntity.getWorldObj(), tTileEntity.xCoord,
								tTileEntity.yCoord, tTileEntity.zCoord);

			// IC2 Sink Compat
			if ((ic2Energy instanceof IEnergySink)
					&& ((IEnergySink) ic2Energy).acceptsEnergyFrom((TileEntity) baseMetaTile, tDir))
				return true;

			// IC2 Source Compat
			if (GT_Mod.gregtechproxy.ic2EnergySourceCompat && (ic2Energy instanceof IEnergySource)) {
				if (((IEnergySource) ic2Energy).emitsEnergyTo((TileEntity) baseMetaTile, tDir)) {
					return true;
				}
			}
		}
		// RF Output Compat
		if (GregTech_API.mOutputRF && tTileEntity instanceof IEnergyReceiver
				&& ((IEnergyReceiver) tTileEntity).canConnectEnergy(tDir))
			return true;

		// RF Input Compat
		if (GregTech_API.mInputRF && (tTileEntity instanceof IEnergyEmitter
				&& ((IEnergyEmitter) tTileEntity).emitsEnergyTo((TileEntity) baseMetaTile, tDir)))
			return true;

		return false;
	}

	@Override
	public void onExplosion() {
		// TODO Auto-generated method stub
		super.onExplosion();
		checkConnection(CONNECTION_DELAY);
	}

	@Override
	public boolean getGT6StyleConnection() {
		// Yes if GT6 Cables are enabled
		return GT_Mod.gregtechproxy.gt6Cable;
	}

	@Override
	public boolean allowPullStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, byte aSide, ItemStack aStack) {
		return false;
	}

	@Override
	public boolean allowPutStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, byte aSide, ItemStack aStack) {
		return false;
	}

	@Override
	public String[] getDescription() {
		return new String[] {
				"Max Voltage: %%%" + EnumChatFormatting.GREEN + mVoltage + " (" + VN[GT_Utility.getTier(mVoltage)] + ")"
						+ EnumChatFormatting.GRAY,
				"Max Amperage: %%%" + EnumChatFormatting.YELLOW + mAmperage + EnumChatFormatting.GRAY,
				"Loss/Meter/Ampere: %%%" + EnumChatFormatting.RED + mCableLossPerMeter + EnumChatFormatting.GRAY
						+ "%%% EU-Volt" };
	}

	@Override
	public float getThickNess() {
		if (GT_Mod.instance.isClientSide() && (GT_Client.hideValue & 0x1) != 0)
			return 0.0625F;
		return mThickNess;
	}

	@Override
	public void saveNBTData(NBTTagCompound aNBT) {
		if (GT_Mod.gregtechproxy.gt6Cable)
			aNBT.setByte("mConnections", mConnections);
	}

	@Override
	public void loadNBTData(NBTTagCompound aNBT) {
		if (GT_Mod.gregtechproxy.gt6Cable) {
			mConnections = aNBT.getByte("mConnections");
		}
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World aWorld, int aX, int aY, int aZ) {
		if (GT_Mod.instance.isClientSide() && (GT_Client.hideValue & 0x2) != 0)
			return AxisAlignedBB.getBoundingBox(aX, aY, aZ, aX + 1, aY + 1, aZ + 1);
		else
			return getActualCollisionBoundingBoxFromPool(aWorld, aX, aY, aZ);
	}

	private AxisAlignedBB getActualCollisionBoundingBoxFromPool(World aWorld, int aX, int aY, int aZ) {
		float tSpace = (1f - mThickNess) / 2;
		float tSide0 = tSpace;
		float tSide1 = 1f - tSpace;
		float tSide2 = tSpace;
		float tSide3 = 1f - tSpace;
		float tSide4 = tSpace;
		float tSide5 = 1f - tSpace;

		if (getBaseMetaTileEntity().getCoverIDAtSide((byte) 0) != 0) {
			tSide0 = tSide2 = tSide4 = 0;
			tSide3 = tSide5 = 1;
		}
		if (getBaseMetaTileEntity().getCoverIDAtSide((byte) 1) != 0) {
			tSide2 = tSide4 = 0;
			tSide1 = tSide3 = tSide5 = 1;
		}
		if (getBaseMetaTileEntity().getCoverIDAtSide((byte) 2) != 0) {
			tSide0 = tSide2 = tSide4 = 0;
			tSide1 = tSide5 = 1;
		}
		if (getBaseMetaTileEntity().getCoverIDAtSide((byte) 3) != 0) {
			tSide0 = tSide4 = 0;
			tSide1 = tSide3 = tSide5 = 1;
		}
		if (getBaseMetaTileEntity().getCoverIDAtSide((byte) 4) != 0) {
			tSide0 = tSide2 = tSide4 = 0;
			tSide1 = tSide3 = 1;
		}
		if (getBaseMetaTileEntity().getCoverIDAtSide((byte) 5) != 0) {
			tSide0 = tSide2 = 0;
			tSide1 = tSide3 = tSide5 = 1;
		}

		byte tConn = ((BaseMetaPipeEntity) getBaseMetaTileEntity()).mConnections;
		if ((tConn & (1 << ForgeDirection.DOWN.ordinal())) != 0)
			tSide0 = 0f;
		if ((tConn & (1 << ForgeDirection.UP.ordinal())) != 0)
			tSide1 = 1f;
		if ((tConn & (1 << ForgeDirection.NORTH.ordinal())) != 0)
			tSide2 = 0f;
		if ((tConn & (1 << ForgeDirection.SOUTH.ordinal())) != 0)
			tSide3 = 1f;
		if ((tConn & (1 << ForgeDirection.WEST.ordinal())) != 0)
			tSide4 = 0f;
		if ((tConn & (1 << ForgeDirection.EAST.ordinal())) != 0)
			tSide5 = 1f;

		return AxisAlignedBB.getBoundingBox(aX + tSide4, aY + tSide0, aZ + tSide2, aX + tSide5, aY + tSide1,
				aZ + tSide3);
	}

	@Override
	public void addCollisionBoxesToList(World aWorld, int aX, int aY, int aZ, AxisAlignedBB inputAABB,
			List<AxisAlignedBB> outputAABB, Entity collider) {
		super.addCollisionBoxesToList(aWorld, aX, aY, aZ, inputAABB, outputAABB, collider);
		if (GT_Mod.instance.isClientSide() && (GT_Client.hideValue & 0x2) != 0) {
			AxisAlignedBB aabb = getActualCollisionBoundingBoxFromPool(aWorld, aX, aY, aZ);
			if (inputAABB.intersectsWith(aabb))
				outputAABB.add(aabb);
		}
	}
}