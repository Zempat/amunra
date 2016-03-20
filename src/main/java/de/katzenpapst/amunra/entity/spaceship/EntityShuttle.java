package de.katzenpapst.amunra.entity.spaceship;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import de.katzenpapst.amunra.AmunRa;
import de.katzenpapst.amunra.item.ARItems;
import de.katzenpapst.amunra.network.packet.PacketSimpleAR;
import de.katzenpapst.amunra.network.packet.PacketSimpleAR.EnumSimplePacket;
import micdoodle8.mods.galacticraft.api.entity.IRocketType.EnumRocketType;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntityTieredRocket;
import micdoodle8.mods.galacticraft.api.prefab.entity.EntitySpaceshipBase.EnumLaunchPhase;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import micdoodle8.mods.galacticraft.api.world.IGalacticraftWorldProvider;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.entities.EntityCelestialFake;
import micdoodle8.mods.galacticraft.core.entities.player.GCPlayerStats;
import micdoodle8.mods.galacticraft.core.network.PacketDynamic;
import micdoodle8.mods.galacticraft.core.network.PacketEntityUpdate;
import micdoodle8.mods.galacticraft.core.util.ConfigManagerCore;
import micdoodle8.mods.galacticraft.core.util.WorldUtil;
import micdoodle8.mods.galacticraft.planets.asteroids.items.AsteroidsItems;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntityShuttle extends EntityTieredRocket {
    /* landeable shuttle feature is scrapped for now...
    public static enum EnumShuttleMode
    {
        // regular rocket mode
        ROCKET,
        // hovering in orbit while the player selects a destination
        HOVERING,
        // lander mode
        LANDER
    }
     * /
    // STUFF FROM LANDER START
    private Boolean shouldMoveClient;
    private Boolean shouldMoveServer;

    private boolean lastShouldMove;

    // advancedmotion
    public float currentDamage;
    public int timeSinceHit;
    public int rockDirection;
    protected boolean lastOnGround;

    public double advancedPositionX;
    public double advancedPositionY;
    public double advancedPositionZ;
    public double advancedYaw;
    public double advancedPitch;
    public int posRotIncrements;
     */
    // STUFF FROM LANDER END

    /*public boolean shouldMove()
    {
        if (this.shouldMoveClient == null || this.shouldMoveServer == null)
        {
            return false;
        }

        if (this.ticks < 40)
        {
            return false;
        }

        return this.riddenByEntity != null && !this.onGround;
    }

    public ArrayList<Object> getNetworkedData()
    {
        final ArrayList<Object> objList = new ArrayList<Object>();

        if (!this.worldObj.isRemote)
        {
            Integer cargoLength = this.cargoItems != null ? this.cargoItems.length : 0;
            objList.add(cargoLength);
            objList.add(this.fuelTank.getFluid() == null ? 0 : this.fuelTank.getFluid().amount);
        }

        if (this.worldObj.isRemote)
        {
            this.shouldMoveClient = this.shouldMove();
            objList.add(this.shouldMoveClient);
        }
        else
        {
            this.shouldMoveServer = this.shouldMove();
            objList.add(this.shouldMoveServer);
            //Server send rider information for client to check
            objList.add(this.riddenByEntity == null ? -1 : this.riddenByEntity.getEntityId());
        }

        return objList;
    }

    protected EnumShuttleMode shuttleMode = EnumShuttleMode.ROCKET;
     */
    public EntityShuttle(World par1World) {
        super(par1World);

        this.setSize(1.2F, 5.5F);
        this.yOffset = 1.5F;
    }

    public EntityShuttle(World world, double posX, double posY, double posZ, EnumRocketType type) {
        super(world, posX, posY, posZ);
        //  this.rocketType = rocketType;
        this.cargoItems = new ItemStack[this.getSizeInventory()];
        this.setSize(1.2F, 3.5F);
        this.yOffset = 1.5F;
        this.rocketType = type;
    }

    public EntityShuttle(World par1World, double par2, double par4, double par6, boolean reversed, EnumRocketType rocketType, ItemStack[] inv)
    {
        this(par1World, par2, par4, par6, rocketType);
        this.cargoItems = inv;
    }
    /*
    public void setShuttleMode(EnumShuttleMode newMode) {
        this.ticks = 0;
        switch(newMode) {
        case ROCKET:
            this.launchPhase = EnumLaunchPhase.UNIGNITED.ordinal();
            this.landing = false;
            break;
        case LANDER:
            this.launchPhase = EnumLaunchPhase.LAUNCHED.ordinal();
            this.landing = true;
            break;
        case HOVERING:
            this.landing = false;
        }
        System.out.println("Setting shuttle mode: "+newMode.toString());
        this.shuttleMode = newMode;
    }
     */
    @Override
    public ItemStack getPickedResult(MovingObjectPosition target)
    {
        return new ItemStack(ARItems.shuttleItem, 1, this.rocketType.getIndex());
    }

    @Override
    public int getRocketTier() {
        // Keep it at 0, the shuttle can't reach most stuff
        return 0;
    }

    @Override
    public float getCameraZoom() {
        return 15.0F;
    }

    @Override
    public boolean defaultThirdPerson() {
        return true;
    }

    @Override
    public int getFuelTankCapacity() {
        return 1000;
    }

    @Override
    public int getPreLaunchWait() {
        return 400;
    }

    @Override
    public double getOnPadYOffset()
    {
        return 1.40D;
    }

    @Override
    public double getMountedYOffset()
    {
        return 0.0D;
    }

    private void makeFlame(double x2, double y2, double z2, Vector3 motionVec, boolean getLaunched)
    {
        if (getLaunched)
        {
            GalacticraftCore.proxy.spawnParticle("launchFlameLaunched", new Vector3(x2 + 0.4 - this.rand.nextDouble() / 10, y2, z2 + 0.4 - this.rand.nextDouble() / 10), motionVec, new Object[] { riddenByEntity });
            GalacticraftCore.proxy.spawnParticle("launchFlameLaunched", new Vector3(x2 - 0.4 + this.rand.nextDouble() / 10, y2, z2 + 0.4 - this.rand.nextDouble() / 10), motionVec, new Object[] { riddenByEntity });
            GalacticraftCore.proxy.spawnParticle("launchFlameLaunched", new Vector3(x2 - 0.4 + this.rand.nextDouble() / 10, y2, z2 - 0.4 + this.rand.nextDouble() / 10), motionVec, new Object[] { riddenByEntity });
            GalacticraftCore.proxy.spawnParticle("launchFlameLaunched", new Vector3(x2 + 0.4 - this.rand.nextDouble() / 10, y2, z2 - 0.4 + this.rand.nextDouble() / 10), motionVec, new Object[] { riddenByEntity });
            GalacticraftCore.proxy.spawnParticle("launchFlameLaunched", new Vector3(x2, y2, z2), motionVec, new Object[] { riddenByEntity });
            GalacticraftCore.proxy.spawnParticle("launchFlameLaunched", new Vector3(x2 + 0.4, y2, z2), motionVec, new Object[] { riddenByEntity });
            GalacticraftCore.proxy.spawnParticle("launchFlameLaunched", new Vector3(x2 - 0.4, y2, z2), motionVec, new Object[] { riddenByEntity });
            GalacticraftCore.proxy.spawnParticle("launchFlameLaunched", new Vector3(x2, y2, z2 + 0.4D), motionVec, new Object[] { riddenByEntity });
            GalacticraftCore.proxy.spawnParticle("launchFlameLaunched", new Vector3(x2, y2, z2 - 0.4D), motionVec, new Object[] { riddenByEntity });
            return;
        }

        double x1 = motionVec.x;
        double y1 = motionVec.y;
        double z1 = motionVec.z;
        GalacticraftCore.proxy.spawnParticle("launchFlameIdle", new Vector3(x2 + 0.4 - this.rand.nextDouble() / 10, y2, z2 + 0.4 - this.rand.nextDouble() / 10), new Vector3(x1 + 0.5D, y1 - 0.3D, z1 + 0.5D), new Object[] { riddenByEntity });
        GalacticraftCore.proxy.spawnParticle("launchFlameIdle", new Vector3(x2 - 0.4 + this.rand.nextDouble() / 10, y2, z2 + 0.4 - this.rand.nextDouble() / 10), new Vector3(x1 - 0.5D, y1 - 0.3D, z1 + 0.5D), new Object[] { riddenByEntity });
        GalacticraftCore.proxy.spawnParticle("launchFlameIdle", new Vector3(x2 - 0.4 + this.rand.nextDouble() / 10, y2, z2 - 0.4 + this.rand.nextDouble() / 10), new Vector3(x1 - 0.5D, y1 - 0.3D, z1 - 0.5D), new Object[] { riddenByEntity });
        GalacticraftCore.proxy.spawnParticle("launchFlameIdle", new Vector3(x2 + 0.4 - this.rand.nextDouble() / 10, y2, z2 - 0.4 + this.rand.nextDouble() / 10), new Vector3(x1 + 0.5D, y1 - 0.3D, z1 - 0.5D), new Object[] { riddenByEntity });
        GalacticraftCore.proxy.spawnParticle("launchFlameIdle", new Vector3(x2 + 0.4, y2, z2), new Vector3(x1 + 0.8D, y1 - 0.3D, z1), new Object[] { riddenByEntity });
        GalacticraftCore.proxy.spawnParticle("launchFlameIdle", new Vector3(x2 - 0.4, y2, z2), new Vector3(x1 - 0.8D, y1 - 0.3D, z1), new Object[] { riddenByEntity });
        GalacticraftCore.proxy.spawnParticle("launchFlameIdle", new Vector3(x2, y2, z2 + 0.4D), new Vector3(x1, y1 - 0.3D, z1 + 0.8D), new Object[] { riddenByEntity });
        GalacticraftCore.proxy.spawnParticle("launchFlameIdle", new Vector3(x2, y2, z2 - 0.4D), new Vector3(x1, y1 - 0.3D, z1 - 0.8D), new Object[] { riddenByEntity });
    }

    protected void spawnParticles(boolean launched)
    {
        if (!this.isDead)
        {
            double x1 = 3.2 * Math.cos(this.rotationYaw / 57.2957795D) * Math.sin(this.rotationPitch / 57.2957795D);
            double z1 = 3.2 * Math.sin(this.rotationYaw / 57.2957795D) * Math.sin(this.rotationPitch / 57.2957795D);
            double y1 = 3.2 * Math.cos((this.rotationPitch - 180) / 57.2957795D);
            if (this.landing && this.targetVec != null)
            {
                double modifier = this.posY - this.targetVec.y;
                modifier = Math.max(modifier, 1.0);
                x1 *= modifier / 60.0D;
                y1 *= modifier / 60.0D;
                z1 *= modifier / 60.0D;
            }

            final double y2 = this.prevPosY + (this.posY - this.prevPosY) + y1;

            final double x2 = this.posX + x1;
            final double z2 = this.posZ + z1;
            Vector3 motionVec = new Vector3(x1, y1, z1);
            Vector3 d1 = new Vector3(y1 * 0.1D, -x1 * 0.1D, z1 * 0.1D).rotate(315 - this.rotationYaw, motionVec);
            Vector3 d2 = new Vector3(x1 * 0.1D, -z1 * 0.1D, y1 * 0.1D).rotate(315 - this.rotationYaw, motionVec);
            Vector3 d3 = new Vector3(-y1 * 0.1D, x1 * 0.1D, z1 * 0.1D).rotate(315 - this.rotationYaw, motionVec);
            Vector3 d4 = new Vector3(x1 * 0.1D, z1 * 0.1D, -y1 * 0.1D).rotate(315 - this.rotationYaw, motionVec);
            Vector3 mv1 = motionVec.clone().translate(d1);
            Vector3 mv2 = motionVec.clone().translate(d2);
            Vector3 mv3 = motionVec.clone().translate(d3);
            Vector3 mv4 = motionVec.clone().translate(d4);
            //T3 - Four flameballs which spread
            makeFlame(x2 + d1.x, y2 + d1.y, z2 + d1.z, mv1, this.getLaunched());
            makeFlame(x2 + d2.x, y2 + d2.y, z2 + d2.z, mv2, this.getLaunched());
            makeFlame(x2 + d3.x, y2 + d3.y, z2 + d3.z, mv3, this.getLaunched());
            makeFlame(x2 + d4.x, y2 + d4.y, z2 + d4.z, mv4, this.getLaunched());
        }
    }

    protected void updateRocket() {
        super.onUpdate();

        int i; // this has something to do with particles...

        if (this.timeUntilLaunch >= 100)
        {
            i = Math.abs(this.timeUntilLaunch / 100);
        }
        else
        {
            i = 1;
        }

        if ((this.getLaunched() || this.launchPhase == EnumLaunchPhase.IGNITED.ordinal() && this.rand.nextInt(i) == 0) && !ConfigManagerCore.disableSpaceshipParticles && this.hasValidFuel())
        {
            if (this.worldObj.isRemote)
            {
                this.spawnParticles(this.getLaunched());
            }
        }

        if(this.getLaunched()) {


            if (this.hasValidFuel())
            {
                // acceleration?
                double d = this.timeSinceLaunch / 150;

                d = Math.min(d, 1);

                if (d != 0.0)
                {
                    this.motionY = -d * 2.5D * Math.cos((this.rotationPitch - 180) / 57.2957795D);
                }

                // fuel usage multiplier
                double multiplier = 1.0D;

                if (this.worldObj.provider instanceof IGalacticraftWorldProvider)
                {
                    multiplier = ((IGalacticraftWorldProvider) this.worldObj.provider).getFuelUsageMultiplier();

                    if (multiplier <= 0)
                    {
                        multiplier = 1;
                    }
                }

                if (this.timeSinceLaunch % MathHelper.floor_double(2 * (1 / multiplier)) == 0)
                {
                    this.removeFuel(1);
                    if (!this.hasValidFuel())
                        this.stopRocketSound();
                }
            }
            else
            {
                if(!this.worldObj.isRemote) { // why? why not remote world? might be a bug here

                    if (Math.abs(Math.sin(this.timeSinceLaunch / 1000)) / 10 != 0.0)
                    {
                        // shouldn't this be affected by gravity?
                        this.motionY -= Math.abs(Math.sin(this.timeSinceLaunch / 1000)) / 20;
                    }
                }
            }
        }
    }
    /*
    protected void updateLander() {
        super.onUpdate();

        if(this.onGround) {
            // go to rocket mode
            this.setShuttleMode(EnumShuttleMode.ROCKET);
            return;
        }

        // acceleration?
        double d = this.timeSinceLaunch / 150;

        d = Math.min(d, 1);

        if (d != 0.0)
        {
            this.motionY = +d * 2.5D * Math.cos((this.rotationPitch - 180) / 57.2957795D);
        }

    }

    protected void updateHovering() {
        super.onUpdate();
        // this.onUpdateAdvancedMotion();
        // ensure it stays where it is
        this.motionX = this.motionY = this.motionZ = 0;

        if (this.ticks >= Long.MAX_VALUE)
        {
            this.ticks = 1;
        }

        this.ticks++;

        if(this.ticks > 256) {
            System.out.print("going down again");
            this.setShuttleMode(EnumShuttleMode.LANDER);
        }

    }

     */





    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void onUpdate()
    {
        updateRocket();
    }

    @Override
    public void onReachAtmosphere()
    {
        /*
        if(this.shuttleMode != EnumShuttleMode.ROCKET) {
            return;
        }*/
        //Not launch controlled
        if (this.riddenByEntity != null && !this.worldObj.isRemote)
        {
            if (this.riddenByEntity instanceof EntityPlayerMP)
            {
                EntityPlayerMP player = (EntityPlayerMP) this.riddenByEntity;

                this.onTeleport(player);
                GCPlayerStats stats = GCPlayerStats.get(player);

                if (this.cargoItems == null || this.cargoItems.length == 0)
                {
                    stats.rocketStacks = new ItemStack[2];
                }
                else
                {
                    stats.rocketStacks = this.cargoItems;
                }

                stats.rocketType = this.rocketType.getIndex();
                stats.rocketItem = ARItems.shuttleItem;
                stats.fuelLevel = this.fuelTank.getFluidAmount();

                // this is the part which activates the celestial gui
                toCelestialSelection(player, stats, this.getRocketTier());
                // setShuttleMode(EnumShuttleMode.HOVERING);
            }
        }

        //Destroy any rocket which reached the top of the atmosphere and is not controlled by a Launch Controller
        // or maybe not
        // this.setDead();
    }

    public static void toCelestialSelection(EntityPlayerMP player, GCPlayerStats stats, int tier)
    {
        player.mountEntity(null);
        stats.spaceshipTier = tier;
        // replace this with my own stuff
        HashMap<String, Integer> map = WorldUtil.getArrayOfPossibleDimensions(tier, player);
        String dimensionList = "";
        int count = 0;
        for (Entry<String, Integer> entry : map.entrySet())
        {
            dimensionList = dimensionList.concat(entry.getKey() + (count < map.entrySet().size() - 1 ? "?" : ""));
            count++;
        }


        AmunRa.packetPipeline.sendTo(new PacketSimpleAR(EnumSimplePacket.C_OPEN_SHUTTLE_GUI, new Object[] { player.getGameProfile().getName(), dimensionList }), player);
        stats.usingPlanetSelectionGui = true;
        stats.savedPlanetList = new String(dimensionList);


        //Entity fakeEntity = new EntityCelestialFake(player.worldObj, player.posX, player.posY, player.posZ, 0.0F);
        //player.worldObj.spawnEntityInWorld(fakeEntity);
        //player.mountEntity(fakeEntity);
    }

    @Override
    public List<ItemStack> getItemsDropped(List<ItemStack> droppedItems)
    {
        super.getItemsDropped(droppedItems);
        ItemStack rocket = new ItemStack(ARItems.shuttleItem, 1, this.rocketType.getIndex());
        rocket.setTagCompound(new NBTTagCompound());
        rocket.getTagCompound().setInteger("RocketFuel", this.fuelTank.getFluidAmount());
        droppedItems.add(rocket);
        return droppedItems;
    }


    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt)
    {
        //nbt.setInteger("ShuttleMode", this.shuttleMode.ordinal());
        super.writeEntityToNBT(nbt);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt)
    {
        //EnumShuttleMode.
        //this.shuttleMode = EnumShuttleMode.values()[nbt.getInteger("ShuttleMode")];
        //this.setShuttleMode(shuttleMode);
        super.readEntityFromNBT(nbt);
    }

}
