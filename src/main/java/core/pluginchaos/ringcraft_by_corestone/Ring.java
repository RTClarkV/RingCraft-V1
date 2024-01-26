package core.pluginchaos.ringcraft_by_corestone;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;

public class Ring extends BukkitRunnable implements Listener {
    //plugin
    private RingCraft_By_CoreStone plugin;
    private RingManager manager;
    //runable
    BukkitTask task;
    private final BukkitScheduler scheduler;
    //integer info
    private int slot;
    private int cps = 0;
    private int maxSlots;
    private int otherRingsInInv;
    private int code;

    //Stringy strings
    private String owner;
    private String instance = "null";

    //boolean
    private boolean hasOwner;
    private boolean ownerOnline;
    private boolean disabled;
    private boolean isHeld;

    //Arrays goofy
    private ArrayList<PotionEffect> potionEffects = new ArrayList<>();
    private ArrayList<Integer> defCoolDowns = new ArrayList<>();
    private ArrayList<Integer> coolDowns = new ArrayList<>();

    //hashmap stuff
    private HashMap<Integer, String> slotNames = new HashMap<>();
    private HashMap<Integer, ChatColor> slotColors = new HashMap<>();

    //item info
    private ItemStack item;
    private ItemMeta itemMeta;

    public Ring(int slots, ItemStack item, RingManager manager, RingCraft_By_CoreStone plugin){
        this.manager = manager;
        this.hasOwner = false;
        this.isHeld = false;
        this.owner = "Null _ _";
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.scheduler = this.plugin.getServer().getScheduler();
        maxSlots = slots;
        this.slot = 0;
        for(int j = 0; j < slots; j++){
            coolDowns.add(-1);
            defCoolDowns.add(-1);
            slotNames.put(j, "");
            slotColors.put(j, ChatColor.WHITE);
        }
        this.item = item;
        this.code = 0;
//        this.itemMeta = itemMeta;
//        this.item.setItemMeta(itemMeta);
        this.task = runTaskTimer(plugin, 0L, 20L);
        runTasks();
    }
    public void runTasks(){
        scheduler.runTaskTimer(plugin, ()->{
            if(isOnline() && isHeld){
                for(int j = 0; j < potionEffects.size(); j++){
                    getOwner().addPotionEffect(potionEffects.get(j));
                }
            }
        }, 0L, 10L);
    }
    public void setCode(int code){
        this.code = code;
    }
    public int getCode(){
        return code;
    }
    public void setInstance(String instance){
        this.instance = instance;
    }
    public String getInstance(){
        return instance;
    }
    public void modifySlot(int index, ChatColor color, String name, int coolDown){
        this.slotColors.replace(index, color);
        this.slotNames.replace(index, name);
        this.defCoolDowns.set(index, coolDown);
    }
    public String getSlotName(int index){
        return slotNames.get(index);
    }
    public ItemStack getItem(){
        return item;
    }
    public ItemMeta getItemMeta(){
        return itemMeta;
    }
    public void setCoolDown(int slot, int cooldown){
        coolDowns.set(slot, cooldown);
    }
    public ArrayList<Integer> getCoolDowns(){
        return coolDowns;
    }
    public ArrayList<Integer> getDefCoolDowns(){
        return defCoolDowns;
    }

    public void resetCoolDown(){
        coolDowns.set(slot, defCoolDowns.get(slot));
    }
    public void nextSlot(){
        slot += 1;
        if(slot >= maxSlots){
            slot = 0;
        }
        showOwnerSlot();
    }
    public int getSlot(){
        return slot;
    }
    public void showOwnerSlot(){
        //getOwner().sendTitle("", slotColors.get(slot) + slotNames.get(slot), 1, 15, 5);
        getOwner().sendActionBar(slotColors.get(slot) + slotNames.get(slot));
    }
    public void setOwner(String name){
        this.hasOwner = true;
        this.setOnline(true);
        this.owner = name;
        manager.addOwnerList(getOwnerName(), this);
        scheduler.runTaskLater(plugin, ()->{
            if(getOwner().getInventory().getItemInMainHand().equals(getItem()) || getOwner().getInventory().getItemInOffHand().equals(getItem())){
                setHeld(true);
            }
        }, 1L);
    }
    public void removeOwner(){
        manager.removeOwnerList(getOwnerName(), this);
        for(int j = 0; j < potionEffects.size(); j++){
            getOwner().removePotionEffect(potionEffects.get(j).getType());
        }
        owner = "Null _ _";
        this.hasOwner = false;
        this.setOnline(false);
    }
    public String getOwnerName(){
        return owner;
    }
    public Player getOwner(){
        return Bukkit.getPlayer(owner);
    }
    public void setOnline(boolean online){
        if(online){
            this.ownerOnline = true;
            this.hasOwner = true;
        }

        if(!online){
            this.ownerOnline = false;
            scheduler.runTaskLaterAsynchronously(plugin, ()->{
                this.hasOwner = false;
            }, 2L);

        }
    }
    public boolean isOnline(){
        return ownerOnline;
    }
    public boolean hasAnOwner(){
        return hasOwner;
    }
    public void coolDown(){
        for(int j = 0; j < coolDowns.size(); j++){
            if(coolDowns.get(j) > -1){
                coolDowns.set(j, coolDowns.get(j) -1 );
            }
        }
    }
    public void addEffect(PotionEffect effect){
        this.potionEffects.add(effect);
    }
    public void setHeld(boolean isHeld){
        this.isHeld = isHeld;
        if(isHeld){
            scheduler.runTaskLater(plugin, ()->{
                boolean localOnline = false;
                for(Player p : Bukkit.getOnlinePlayers()){
                    if(p.getName().equalsIgnoreCase(owner)){
                        localOnline = true;
                    }
                }
                if(localOnline){
                    for(int j = 0; j < potionEffects.size(); j++){
                        getOwner().addPotionEffect(potionEffects.get(j));
                    }
                }
            }, 1L);
        }
        if(!isHeld){
            for(int j = 0; j < potionEffects.size(); j++){
                getOwner().removePotionEffect(potionEffects.get(j).getType());
            }
        }
    }
    public boolean isBeingHeld(){
        return isHeld;
    }
    public void setDisabled(boolean disabled){
        this.disabled = disabled;
    }
    public boolean isDisabled(){
        return this.disabled;
    }
    private void addOtherRingInInv(){
        otherRingsInInv += 1;
    }
    private int getOtherRingsInInv(){
        return otherRingsInInv;
    }
    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        if(e.getPlayer().getInventory().contains(item)){
            setOwner(e.getPlayer().getName());
            setOnline(true);
            if(e.getPlayer().getInventory().getItemInMainHand().equals(item) || e.getPlayer().getInventory().getItemInOffHand().equals(item)){
                setHeld(true);
            }
        }
    }
    @EventHandler
    public void onLeave(PlayerQuitEvent e){
        if(e.getPlayer().getName().equalsIgnoreCase(owner)){
            setOnline(false);
        }
    }
    @EventHandler
    public void onPickup(PlayerAttemptPickupItemEvent e){
        if(e.getItem().getItemStack().equals(item)){
            setOwner(e.getPlayer().getName());
        }
    }
    @EventHandler
    public void onSwitchItem(PlayerItemHeldEvent e){
        if(!e.getPlayer().getName().equals(getOwnerName()))return;
        ItemStack newItem = e.getPlayer().getInventory().getItem(e.getNewSlot());
        ItemStack oldItem = e.getPlayer().getInventory().getItem(e.getPreviousSlot());
        //if(manager.checkCanHold(this))return;
        if(newItem != null && newItem.equals(item)) {
            if(manager.checkCanHold(this))return;
            setHeld(true);
            showOwnerSlot();
        }
        if(oldItem != null && oldItem.equals(item)){
            setHeld(false);
        }
    }
    @EventHandler
    public void onOffHand(PlayerSwapHandItemsEvent e){
        scheduler.runTaskLater(plugin, ()->{
            if(getOwner() == null)return;
            ItemStack offhand = getOwner().getInventory().getItemInOffHand();
            NamespacedKey key = new NamespacedKey(plugin, "ringType");
            if(offhand.getItemMeta() == null)return;
            PersistentDataContainer offdata = offhand.getItemMeta().getPersistentDataContainer();
            if(offdata.has(key) && !offhand.equals(item) && isHeld){
                setHeld(false);
                playSounds(e.getPlayer().getLocation(), Sound.ITEM_ARMOR_EQUIP_GENERIC, 1, 1);
            }
            if(offdata.has(key) && offhand.equals(item) && !isHeld){
                setHeld(true);
            }
        }, 1L);
    }
    @EventHandler
    public void setOwnerOnClose(InventoryCloseEvent e){
        if(e.getPlayer().getName().equals(owner))return;
        if(e.getPlayer().getInventory().contains(item)){
            setOwner(e.getPlayer().getName());
        }
    }
    @EventHandler
    public void removeOwnerIfNotInInv(InventoryCloseEvent e){
        if(!e.getEventName().equals(owner))return;
        if(!getOwner().getInventory().contains(item)){
            removeOwner();
        }
    }
    @EventHandler
    public void setHeldOnClose(InventoryCloseEvent e){
        if(!isOnline())return;
        if(!e.getPlayer().getName().equals(owner))return;
        if(e.getPlayer().getInventory().getItemInMainHand().equals(getItem()) || e.getPlayer().getInventory().getItemInOffHand().equals(getItem())){
            setHeld(true);
        }
        scheduler.runTaskLater(plugin, ()->{
            ItemStack offhand = getOwner().getInventory().getItemInOffHand();
            NamespacedKey key = new NamespacedKey(plugin, "ringType");
            if(offhand.getItemMeta() == null)return;
            PersistentDataContainer offdata = offhand.getItemMeta().getPersistentDataContainer();
            if(offdata.has(key) && !offhand.equals(item)){
                setHeld(false);
            }
            if(offdata.has(key) && offhand.equals(item)){
                setHeld(true);
            }
        }, 1L);
        if(!e.getPlayer().getInventory().getItemInMainHand().equals(getItem()) && !e.getPlayer().getInventory().getItemInOffHand().equals(getItem())){
            setHeld(false);
        }

    }
    @EventHandler
    public void onDropItem(PlayerDropItemEvent e){
        if(!getOwnerName().equalsIgnoreCase(e.getPlayer().getName()))return;
        if(e.getItemDrop().getItemStack().equals(item)){
            removeOwner();
        }
    }
    @EventHandler
    public void onShiftLeftClick(PlayerInteractEvent e){
        if(!e.getPlayer().getName().equals(getOwnerName()))return;
        if(!e.getAction().isLeftClick())return;
        if(!e.getPlayer().getPose().equals(Pose.SNEAKING))return;
        if(!isHeld)return;
        if(cps > 0)return;
        clicked();
        nextSlot();
    }
    @EventHandler
    public void onDeath(PlayerDeathEvent e){
        if(!e.getPlayer().getName().equals(getOwnerName()))return;
        if(e.getDrops().contains(getItem())){
            removeOwner();
        }
    }
    @EventHandler
    public void onRightClick(PlayerInteractEvent e){
        if(!e.getPlayer().equals(getOwner()))return;
        if(!e.getAction().isRightClick())return;
        if(cps > 0)return;
        clicked();
        if(!isHeld)return;
        if(getOwner().getInventory().getItemInMainHand().getType().isEdible())return;
        if(e.getClickedBlock() !=null){
            if(e.getClickedBlock().getType().isInteractable())return;
        }
        if(disabled){
            getOwner().sendMessage(ChatColor.RED + "You're too close to the One Ring bearer!");
            return;
        }
        if(coolDowns.get(slot) > -1) {
            getOwner().sendMessage(ChatColor.RED + "You can use " + slotColors.get(slot)
                    + "" + ChatColor.BOLD + slotNames.get(slot) + "" + ChatColor.RESET+ "" + ChatColor.RED + " in "+ ChatColor.GREEN + coolDowns.get(slot) + ChatColor.RED+"s.");
            return;
        }
        coolDowns.set(slot, defCoolDowns.get(slot));
        manager.ringAction(this, e);
    }
    @EventHandler
    public void onBreak(BlockBreakEvent e){
        if(!e.getPlayer().equals(getOwner()))return;
        if(!isHeld)return;
        manager.ringBreakAction(this, e);
    }
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e){
        if(!e.getDamager().equals(getOwner()))return;
        if(!isHeld)return;
        manager.ringDamageAction(this, e);
    }
    public void clicked(){
        cps =1;
        scheduler.runTaskLater(plugin, ()->{
            cps = 0;
        }, 2L);
    }
    @EventHandler
    public void onDespawn(ItemDespawnEvent e){
        if(e.getEntity().getItemStack().equals(item)){
            e.setCancelled(true);
        }
    }
//    @EventHandler
//    public void deathMessages(PlayerDeathEvent e){
//        if(!isHeld)return;
//        if(e.getEntity().getLastDamageCause() instanceof Player){
//            Player p = (Player) e.getEntity().getLastDamageCause().getEntity();
//            if(!p.getName().equalsIgnoreCase(getOwnerName()))return;
//            playSounds(e.getPlayer().getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2, 1);
//            playParticles(e.getPlayer().getLocation(), Particle.FLASH, 10);
//            e.setDeathMessage(ChatColor.WHITE + e.getPlayer().getName() + ChatColor.RED +" was slain by " + ChatColor.WHITE +p.getName()+
//                    ChatColor.RED + " using " + ChatColor.GOLD + getItem().displayName());
//        }
//    }
    @EventHandler
    public void stopAnvil(PrepareAnvilEvent e){
        if(e.getResult() == null)return;
        if(e.getResult().getLore() == null)return;
        if(e.getResult().getLore().equals(getItem().getLore())){
            e.setResult(null);
        }
    }
    @EventHandler
    public void stopCraft(CraftItemEvent e){
        for(ItemStack item : e.getInventory()){
            if(item != null &&item.equals(getItem())){
                e.setCancelled(true);
            }
        }
    }

    public void playParticles(Location loc, Particle particle, int amount){
        for(Player viewer : loc.getNearbyPlayers(60)){
            viewer.spawnParticle(particle, loc, amount);
        }
    }
    public void playSounds(Location loc, Sound sound, int volume, int pitch){
        for(Player viewer : loc.getNearbyPlayers(60)){
            viewer.playSound(loc, sound, volume, pitch);
        }
    }
    @EventHandler
    public void onConsume(PlayerItemConsumeEvent e){
        if(!e.getPlayer().getName().equals(owner))return;
        if(e.getItem().getType().equals(Material.GOLDEN_APPLE)){
            scheduler.runTaskLater(plugin, ()->{
                e.getPlayer().removePotionEffect(PotionEffectType.ABSORPTION);
                e.getPlayer().removePotionEffect(PotionEffectType.REGENERATION);
                e.getPlayer().addPotionEffect(PotionEffectType.REGENERATION.createEffect(100, 0));
                e.getPlayer().addPotionEffect(PotionEffectType.ABSORPTION.createEffect(300, 0));
            }, 1L);
            if(isHeld){
                setHeld(true);
            }
        }
    }
    @Override
    public void run() {
        coolDown();
    }
}


