package WayofTime.alchemicalWizardry.common.items;

import java.util.List;

import WayofTime.alchemicalWizardry.AlchemicalWizardry;
import WayofTime.alchemicalWizardry.common.entity.projectile.EntityEnergyBazookaMainProjectile;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class EnergyBazooka extends EnergyItems
{
    @SideOnly(Side.CLIENT)
    private IIcon activeIcon;
    @SideOnly(Side.CLIENT)
    private IIcon activeIconTier2;
    @SideOnly(Side.CLIENT)
    private IIcon passiveIcon;
    private int tier;
    private int damage;

    public EnergyBazooka(int tier)
    {
        super();
        setMaxStackSize(1);
        setCreativeTab(AlchemicalWizardry.tabBloodMagic);
        setFull3D();
        setMaxDamage(250);
        this.tier = tier;
        this.setEnergyUsed(tier == 1 ? AlchemicalWizardry.energyBazookaLPPerShot : AlchemicalWizardry.energyBazookaSecondTierLPPerShot);
        this.damage = tier == 1 ? AlchemicalWizardry.energyBazookaDamage : AlchemicalWizardry.energyBazookaSecondTierDamage;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister)
    {
        this.itemIcon = iconRegister.registerIcon("AlchemicalWizardry:EnergyBazooka_activated");
        this.activeIcon = iconRegister.registerIcon("AlchemicalWizardry:EnergyBazooka_activated");
        this.activeIconTier2 = iconRegister.registerIcon("AlchemicalWizardry:EnergyBazooka_activated");
        this.passiveIcon = iconRegister.registerIcon("AlchemicalWizardry:SheathedItem");
    }

    @Override
    public IIcon getIcon(ItemStack stack, int renderPass, EntityPlayer player, ItemStack usingItem, int useRemaining)
    {
        if (stack.getTagCompound() == null)
        {
            stack.setTagCompound(new NBTTagCompound());
        }

        NBTTagCompound tag = stack.getTagCompound();

        if (tag.getBoolean("isActive"))
        {
            return tier == 1 ? this.activeIcon : this.activeIconTier2;
        }
        else
        {
            return this.passiveIcon;
        }
    }

    @Override
    public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
    {
        final int maxDelay = tier == 1 ? AlchemicalWizardry.energyBazookaMaxDelay : AlchemicalWizardry.energyBazookaSecondTierMaxDelay;

        if (!EnergyItems.checkAndSetItemOwner(par1ItemStack, par3EntityPlayer) || par3EntityPlayer.isSneaking())
        {
            this.setActivated(par1ItemStack, !getActivated(par1ItemStack));
            par1ItemStack.getTagCompound().setInteger("worldTimeDelay", (int) (par2World.getWorldTime() - 1) %
                                                                        (tier == 1 ?
                                                                        AlchemicalWizardry.energyBazookaMaxDelayAfterActivation + 1 :
                                                                        AlchemicalWizardry.energyBazookaSecondTierMaxDelayAfterActivation + 1));
            return par1ItemStack;
        }

        if (!getActivated(par1ItemStack))
        {
            return par1ItemStack;
        }

        if (this.getDelay(par1ItemStack) > 0)
        {
            return par1ItemStack;
        }

        if (!par3EntityPlayer.capabilities.isCreativeMode)
        {
            if(!syphonBatteries(par1ItemStack, par3EntityPlayer, this.getEnergyUsed()))
            {
            	return par1ItemStack;
            }
        }

        par2World.playSoundAtEntity(par3EntityPlayer, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

        if (!par2World.isRemote)
        {
            par2World.spawnEntityInWorld(new EntityEnergyBazookaMainProjectile(par2World, par3EntityPlayer, damage));
            this.setDelay(par1ItemStack, maxDelay);
        }

        Vec3 vec = par3EntityPlayer.getLookVec();
        double wantedVelocity = 3.0f;
        par3EntityPlayer.motionX = -vec.xCoord * wantedVelocity;
        par3EntityPlayer.motionY = -vec.yCoord * wantedVelocity;
        par3EntityPlayer.motionZ = -vec.zCoord * wantedVelocity;
        par2World.playSoundEffect((double) ((float) par3EntityPlayer.posX + 0.5F), (double) ((float) par3EntityPlayer.posY + 0.5F), (double) ((float) par3EntityPlayer.posZ + 0.5F), "random.fizz", 0.5F, 2.6F + (par2World.rand.nextFloat() - par2World.rand.nextFloat()) * 0.8F);
        par3EntityPlayer.fallDistance = 0;
        return par1ItemStack;
    }

    @Override
    public void onUpdate(ItemStack par1ItemStack, World par2World, Entity par3Entity, int par4, boolean par5)
    {
        if (!(par3Entity instanceof EntityPlayer))
        {
            return;
        }

        EntityPlayer par3EntityPlayer = (EntityPlayer) par3Entity;

        if (par1ItemStack.getTagCompound() == null)
        {
            par1ItemStack.setTagCompound(new NBTTagCompound());
        }
        int delay = this.getDelay(par1ItemStack);

        if (!par2World.isRemote && delay > 0)
        {
            this.setDelay(par1ItemStack, delay - 1);
        }

        if (par2World.getWorldTime() % (tier == 1 ?
                                        AlchemicalWizardry.energyBazookaMaxDelayAfterActivation + 1 :
                                        AlchemicalWizardry.energyBazookaSecondTierMaxDelayAfterActivation + 1) == par1ItemStack.getTagCompound().getInteger("worldTimeDelay") &&
            par1ItemStack.getTagCompound().getBoolean("isActive"))
        {
            if (!par3EntityPlayer.capabilities.isCreativeMode)
            {
                if(!EnergyItems.syphonBatteries(par1ItemStack, par3EntityPlayer, tier == 1 ? AlchemicalWizardry.energyBazookaLPPerActivation : AlchemicalWizardry.energyBazookaSecondTierLPPerActivation))
                {
                	this.setActivated(par1ItemStack, false);
                }
            }
        }

        par1ItemStack.setItemDamage(0);
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4)
    {
        par3List.add(StatCollector.translateToLocal("tooltip.energybazooka.desc"));

        if (!(par1ItemStack.getTagCompound() == null))
        {
            if (par1ItemStack.getTagCompound().getBoolean("isActive"))
            {
                par3List.add(StatCollector.translateToLocal("tooltip.sigil.state.activated"));
            } else
            {
                par3List.add(StatCollector.translateToLocal("tooltip.sigil.state.deactivated"));
            }

            if (!par1ItemStack.getTagCompound().getString("ownerName").equals(""))
            {
                par3List.add(StatCollector.translateToLocal("tooltip.owner.currentowner") + " " + par1ItemStack.getTagCompound().getString("ownerName"));
            }
        }
    }

    public void setActivated(ItemStack par1ItemStack, boolean newActivated)
    {
        if (par1ItemStack.getTagCompound() == null)
        {
            par1ItemStack.setTagCompound(new NBTTagCompound());
        }
        par1ItemStack.getTagCompound().setBoolean("isActive", newActivated);
    }

    public boolean getActivated(ItemStack par1ItemStack)
    {
        if (par1ItemStack.getTagCompound() == null)
        {
            par1ItemStack.setTagCompound(new NBTTagCompound());
        }
        return par1ItemStack.getTagCompound().getBoolean("isActive");
    }

    public void setDelay(ItemStack par1ItemStack, int newDelay)
    {
        if (par1ItemStack.getTagCompound() == null)
        {
            par1ItemStack.setTagCompound(new NBTTagCompound());
        }
        par1ItemStack.getTagCompound().setInteger("delay", newDelay);
    }

    public int getDelay(ItemStack par1ItemStack)
    {
        if (par1ItemStack.getTagCompound() == null)
        {
            par1ItemStack.setTagCompound(new NBTTagCompound());
        }
        return par1ItemStack.getTagCompound().getInteger("delay");
    }
}
