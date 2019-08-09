/**
 * @author Creepinson
 * 
 */
package me.creepinson.mod.tile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import me.creepinson.mod.block.BlockEntityDetector;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class TileEntityEntityDetector extends TileEntity implements ITickable {

	public boolean entityFound;
	public List<Entity> detectedEntities = new ArrayList<Entity>();
	public int radiusRange = 1;
	public List<Class<? extends Entity>> entityWhiteList = new ArrayList<Class<? extends Entity>>();
	public int maxRadiusRange = 20; // implement range upgrade item for more range.

	public static List<Entity> getEntitiesInAABB(World world, List<Class<? extends Entity>> whitelist,
			AxisAlignedBB aabb) {

		int j2 = MathHelper.floor((aabb.minX - World.MAX_ENTITY_RADIUS) / 16.0D);
		int k2 = MathHelper.ceil((aabb.maxX + World.MAX_ENTITY_RADIUS) / 16.0D);
		int l2 = MathHelper.floor((aabb.minZ - World.MAX_ENTITY_RADIUS) / 16.0D);
		int i3 = MathHelper.ceil((aabb.maxZ + World.MAX_ENTITY_RADIUS) / 16.0D);
		List<Entity> list = Lists.<Entity>newArrayList();

		for (int j3 = j2; j3 < k2; ++j3) {
			for (int k3 = l2; k3 < i3; ++k3) {
				if (world.isChunkGeneratedAt(j3, k3)) {
					Chunk chunk = world.getChunkFromChunkCoords(j3, k3);
					int i = MathHelper.floor((aabb.minY - World.MAX_ENTITY_RADIUS) / 16.0D);
					int j = MathHelper.floor((aabb.maxY + World.MAX_ENTITY_RADIUS) / 16.0D);
					i = MathHelper.clamp(i, 0, chunk.getEntityLists().length - 1);
					j = MathHelper.clamp(j, 0, chunk.getEntityLists().length - 1);

					for (int k = i; k <= j; ++k) {
						for (Class<? extends Entity> entityClass : whitelist) {
							for (Entity t : chunk.getEntityLists()[k].getByClass(entityClass)) {
								if (t.getEntityBoundingBox().intersects(aabb)) {
									list.add(t);
								}
							}
						}
					}
				}
			}
		}
		return list;
	}

	@Override
	public void update() {

		if (!entityWhiteList.isEmpty()) {
			this.detectedEntities = getEntitiesInAABB(world, entityWhiteList,
					new AxisAlignedBB(pos.subtract(new Vec3i(radiusRange + 1, radiusRange + 1, radiusRange + 1)),
							pos.add(new Vec3i(radiusRange + 1, radiusRange + 1, radiusRange + 1))));

		}
		this.entityFound = this.detectedEntities.size() > 0;
		IBlockState state = world.getBlockState(pos);
		if (this.detectedEntities.size() > 0 && !state.getValue(BlockEntityDetector.EMIT)) {
			world.setBlockState(pos, state.withProperty(BlockEntityDetector.EMIT, true));
		} else if (this.detectedEntities.size() == 0 && state.getValue(BlockEntityDetector.EMIT)) {
			world.setBlockState(pos, state.withProperty(BlockEntityDetector.EMIT, false));
		}

	}

	public TileEntityEntityDetector() {

	}

}