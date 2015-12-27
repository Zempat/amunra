package de.katzenpapst.amunra.world.mapgen.volcano;

import java.util.Random;

import cpw.mods.fml.common.FMLLog;
import de.katzenpapst.amunra.world.CoordHelper;
import de.katzenpapst.amunra.world.mapgen.BaseStructureStart;
import micdoodle8.mods.galacticraft.api.prefab.core.BlockMetaPair;
import micdoodle8.mods.galacticraft.core.perlin.generator.Gradient;
import net.minecraft.block.Block;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;

public class Volcano extends BaseStructureStart {



	protected BlockMetaPair fluid;
	protected BlockMetaPair mountainMaterial;
	protected BlockMetaPair shaftMaterial;
	protected int maxDepth = 2;
	protected int maxHeight = 50; // over ground

	// radius*2+1 will be the circumference
	protected int radius = 50;
	protected int shaftRadius = 1;

	protected Gradient testGrad;


	public Volcano(World world, int chunkX, int chunkZ, Random rand) {
		super(world, chunkX, chunkZ, rand);
		int startX = CoordHelper.chunkToMinBlock(chunkX)+MathHelper.getRandomIntegerInRange(rand, 0, 15);
		int startZ = CoordHelper.chunkToMinBlock(chunkZ)+MathHelper.getRandomIntegerInRange(rand, 0, 15);
		StructureBoundingBox bb = new StructureBoundingBox(
				startX-radius,
				startZ-radius,
				startX+radius,
				startZ+radius
			);
		this.setStructureBoundingBox(bb);
		FMLLog.info("Generating Volcano at "+startX+"/"+startZ);

		testGrad = new Gradient(this.rand.nextLong(), 4, 0.25F);
		testGrad.setFrequency(0.02F);

	}


	@Override
	public boolean generateChunk(int chunkX, int chunkZ, Block[] blocks, byte[] metas) {
		super.generateChunk(chunkX, chunkZ, blocks, metas);

		// test first
		StructureBoundingBox chunkBB = CoordHelper.getChunkBB(chunkX, chunkZ);
		StructureBoundingBox myBB = this.getStructureBoundingBox();

		if(!chunkBB.intersectsWith(myBB)) {
			return false;
		}

		int fallbackGround = this.getWorldGroundLevel();
		if(groundLevel == -1) {
			groundLevel = getAverageGroundLevel(blocks, metas, getStructureBoundingBox(), chunkBB, fallbackGround);
			if(groundLevel == -1) {
				groundLevel = fallbackGround; // but this shouldn't even happen...
			}
		}

		int xCenter = myBB.getCenterX();
		int zCenter = myBB.getCenterZ();

		double sqRadius = Math.pow(this.radius, 2);


		int maxVolcanoHeight = (maxHeight+groundLevel);


		for(int x = myBB.minX; x <= myBB.maxX; x++) {
			for(int z = myBB.minZ; z <= myBB.maxZ; z++) {

				if(!chunkBB.isVecInside(x, 64, z)) {
					continue;
				}

				int lowestBlock = this.getHighestSpecificBlock(
						blocks,
						metas,
						CoordHelper.abs2rel(x, chunkX),
						CoordHelper.abs2rel(z, chunkZ),
						this.mountainMaterial.getBlock(),
						this.mountainMaterial.getMetadata()
					);
				if(lowestBlock == -1) {
					lowestBlock = maxDepth;
				}

				int xRel = x-xCenter;
				int zRel = z-zCenter;

				int sqDistance = xRel*xRel + zRel*zRel;

				if(sqDistance <= sqRadius) {
					double distance = Math.sqrt(sqDistance);

					int height = (int)( maxHeight*((this.radius-distance)/this.radius) );
					// variate a little
					// TEST
					//double rad = Math.atan2(xRel, zRel)*180/Math.PI;

					double noise = testGrad.getNoise(x, z)*32-16;
					//double noise = testGrad.getNoise((float)distance, (float)rad)*16-8;
					// noise has less effect the closer to the shaft we come
					noise *= (distance)/this.radius;
					height += noise;
					// height += MathHelper.getRandomIntegerInRange(rand, -1, 1);
					if(height > 255) {
						height = 255;
					}

					//int height = (int)((1-sqDistance/sqRadius)*maxVolcanoHeight);

					if(distance < this.shaftRadius+2) {
						for(int y = maxDepth; y < groundLevel+height; y++) {

							if(distance < this.shaftRadius+1) {
								this.placeBlockAbs(blocks, metas, x, y, z, chunkX, chunkZ, fluid);
							} else {
								if(y == groundLevel+height-1) {
									this.placeBlockAbs(blocks, metas, x, y, z, chunkX, chunkZ, fluid);
								} else {
									this.placeBlockAbs(blocks, metas, x, y, z, chunkX, chunkZ, this.shaftMaterial);
								}
							}
						}

					} else {
						for(int y = lowestBlock; y < groundLevel+height; y++) {

							this.placeBlockAbs(blocks, metas, x, y, z, chunkX, chunkZ, mountainMaterial);

						}
					}
				}
			}

		}

		return true;
	}

	public BlockMetaPair getFluid() {
		return fluid;
	}


	public void setFluid(BlockMetaPair fluid) {
		this.fluid = fluid;
	}


	public BlockMetaPair getMountainMaterial() {
		return mountainMaterial;
	}


	public void setMountainMaterial(BlockMetaPair mountainMaterial) {
		this.mountainMaterial = mountainMaterial;
	}


	public BlockMetaPair getShaftMaterial() {
		return shaftMaterial;
	}


	public void setShaftMaterial(BlockMetaPair shaftMaterial) {
		this.shaftMaterial = shaftMaterial;
	}


	public int getMaxDepth() {
		return maxDepth;
	}


	public void setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
	}


	public int getRadius() {
		return radius;
	}


	public void setRadius(int radius) {
		this.radius = radius;
	}


}
