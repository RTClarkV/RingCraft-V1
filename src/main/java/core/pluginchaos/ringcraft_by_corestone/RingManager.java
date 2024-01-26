package core.pluginchaos.ringcraft_by_corestone;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class RingManager extends BukkitRunnable implements CommandExecutor, Listener {
    RingCraft_By_CoreStone plugin;
    private BossBar bar;
    private FileConfiguration config;
    //Random
    Random ran = new Random();
    private int inventoryChecpCPS = 0;

    private int oneRingHurt = 0;

    //Strings
    private String oneRingInstance = "One Ring";
    private String menInstance = "Men Ring";
    private String elvenAirInstance = "Elven Air";
    private String naryaInstance = "Narya Ring";
    private String nenyaInstance = "Nenya Ring";
    private String lesserInstance = "Lesser RIng";


    private String dwarvenInstnace = "Dwarven Ring";
    private String ringKey = "RingKey";
    private String ringCodeKey = "ringCodeKey";
    private NamespacedKey breaking;


    //ringLists
    private ArrayList<Ring> ringList = new ArrayList<>();
    private ArrayList<String> ringOwners = new ArrayList<>();
    private HashMap<String, ArrayList<Ring>> ownerRings = new HashMap<>();
    private ArrayList<String> nearOneRing = new ArrayList<>();
    private ArrayList<String> nearBossBar = new ArrayList<>();
    private HashMap<Integer, Ring> codeList = new HashMap<>();

    //Bukkit scheduler
    BukkitScheduler scheduler;
    BukkitTask task;

    public RingManager(RingCraft_By_CoreStone plugin){
        this.plugin = plugin;
        this.config = plugin.getConfig();
        task = runTaskTimer(plugin, 0L, 20L);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getCommand("RingCraftCore").setExecutor(this);
        scheduler = plugin.getServer().getScheduler();
        runTasks();
        this.bar = Bukkit.createBossBar(ChatColor.WHITE + "One Ring Bearer", BarColor.RED, BarStyle.SEGMENTED_20);
        this.bar.setProgress(1.0);
        this.bar.setVisible(true);
        this.oneRingHurt = config.getInt("oneRingStuff");
        oneRingHurt *= 2;
        this.breaking = new NamespacedKey(plugin, "BreakingBad");
    }
    public void addOwnerList(String name, Ring ring){
        if(this.ringOwners.contains(name))return;
        ringOwners.add(name);
        ArrayList<Ring> list = new ArrayList<>();
        if(!ownerRings.containsKey(name)){
            list.add(ring);
            ownerRings.put(name, list);
            return;
        }
        if(ownerRings.containsKey(name) && !ownerRings.get(name).contains(ring)){
            ownerRings.get(name).add(ring);
        }
    }
    public void removeOwnerList(String name, Ring ring){
        if(!this.ringOwners.contains(name))return;
        ringOwners.remove(name);
        if(this.ownerRings.containsKey(name)){
            ownerRings.get(name).remove(ring);
        }
        if(this.ownerRings.get(name).isEmpty()){
            ownerRings.remove(name);
        }
    }
    public void ringDamageAction(Ring ring, EntityDamageByEntityEvent event){
        if(ring.getInstance().equalsIgnoreCase(lesserInstance)){
            lesserRingDamageAction(ring, event);
        }
        if(ring.getInstance().equalsIgnoreCase(oneRingInstance)){
            oneRingDamageAction(ring, event);
        }
    }
    public void oneRingDamageAction(Ring ring, EntityDamageByEntityEvent event){
        event.getEntity().setFireTicks(100);
    }
    public void lesserRingDamageAction(Ring ring, EntityDamageByEntityEvent event){
        if(ring.getSlotName(ring.getSlot()).equalsIgnoreCase("Fire Aspect")){
            event.getEntity().setFireTicks(60);
        }
    }

    public void ringBreakAction(Ring ring, BlockBreakEvent event){
        if(ring.getInstance().equalsIgnoreCase(dwarvenInstnace)){
            if(ring.getOwner().getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH))return;
            dwarvenBlockBreakAction(ring, event);
        }
        if(ring.getInstance().equalsIgnoreCase(lesserInstance)){
            if(ring.getOwner().getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH))return;
            lesserBreakAction(ring, event);
        }
    }
    public void lesserBreakAction(Ring ring, BlockBreakEvent event){
        Block b = event.getBlock();
        if(ring.getSlotName(ring.getSlot()).equalsIgnoreCase("Fortune I")) {
            int random = getRan(0, 2);
            fortuneAction(b, random);
        }
        if(ring.getSlotName(ring.getSlot()).equalsIgnoreCase("Fortune II")) {
            int random = getRan(1, 12);
            if(random <= 9){
                fortuneAction(b, getRan(1, 2));
            }
            if(random > 9)fortuneAction(b, 3);
        }
    }
    public void fortuneAction(Block b, int random){
        //Bukkit.broadcastMessage("r: " + random);
        if(random <= 0)return;
        random -=1;
        if(b.getType().equals(Material.DIAMOND_ORE) || b.getType().equals(Material.DEEPSLATE_DIAMOND_ORE)){
            for(int j = 0; j < random; j++){
                b.getWorld().dropItem(b.getLocation(), new ItemStack(Material.DIAMOND));
            }
        }
        if(b.getType().equals(Material.IRON_ORE) || b.getType().equals(Material.DEEPSLATE_IRON_ORE)){
            for(int j = 0; j < random; j++){
                b.getWorld().dropItem(b.getLocation(), new ItemStack(Material.RAW_IRON));
            }
        }
        if(b.getType().equals(Material.GOLD_ORE) || b.getType().equals(Material.DEEPSLATE_GOLD_ORE)){
            for(int j = 0; j < random; j++){
                b.getWorld().dropItem(b.getLocation(), new ItemStack(Material.RAW_GOLD));
            }
        }
        if(b.getType().equals(Material.COAL_ORE) || b.getType().equals(Material.DEEPSLATE_COAL_ORE)){
            for(int j = 0; j < random; j++){
                b.getWorld().dropItem(b.getLocation(), new ItemStack(Material.COAL));
            }
        }
        if(b.getType().equals(Material.EMERALD_ORE) || b.getType().equals(Material.DEEPSLATE_EMERALD_ORE)){
            for(int j = 0; j < random; j++){
                b.getWorld().dropItem(b.getLocation(), new ItemStack(Material.EMERALD));
            }
        }
        if(b.getType().equals(Material.COPPER_ORE) || b.getType().equals(Material.DEEPSLATE_COPPER_ORE)){
            for(int j = 0; j < random; j++){
                b.getWorld().dropItem(b.getLocation(), new ItemStack(Material.RAW_COPPER));
            }
        }
        if(b.getType().equals(Material.LAPIS_ORE) || b.getType().equals(Material.DEEPSLATE_LAPIS_ORE)){
            for(int j = 0; j < random; j++){
                b.getWorld().dropItem(b.getLocation(), new ItemStack(Material.LAPIS_LAZULI));
            }
        }
        if(b.getType().equals(Material.REDSTONE_ORE) || b.getType().equals(Material.DEEPSLATE_REDSTONE_ORE)){
            for(int j = 0; j < random; j++){
                b.getWorld().dropItem(b.getLocation(), new ItemStack(Material.REDSTONE));
            }
        }
    }
    public void dwarvenBlockBreakAction(Ring ring, BlockBreakEvent event){
        Block b = event.getBlock();
        if(ring.getSlot() == 2) {
            int random = getRan(2, 4);
            fortuneAction(b, random);
//            if(random == 3){
//                random = getRan(0, 1);
//            }
//            if(b.getType().equals(Material.DIAMOND_ORE) || b.getType().equals(Material.DEEPSLATE_DIAMOND_ORE)){
//                for(int j = 0; j < random; j++){
//                    b.getWorld().dropItem(b.getLocation(), new ItemStack(Material.DIAMOND));
//                }
//            }
//            if(b.getType().equals(Material.IRON_ORE) || b.getType().equals(Material.DEEPSLATE_IRON_ORE)){
//                for(int j = 0; j < random; j++){
//                    b.getWorld().dropItem(b.getLocation(), new ItemStack(Material.RAW_IRON));
//                }
//            }
//            if(b.getType().equals(Material.GOLD_ORE) || b.getType().equals(Material.DEEPSLATE_GOLD_ORE)){
//                for(int j = 0; j < random; j++){
//                    b.getWorld().dropItem(b.getLocation(), new ItemStack(Material.RAW_GOLD));
//                }
//            }
//            if(b.getType().equals(Material.COAL_ORE) || b.getType().equals(Material.DEEPSLATE_COAL_ORE)){
//                for(int j = 0; j < random; j++){
//                    b.getWorld().dropItem(b.getLocation(), new ItemStack(Material.COAL));
//                }
//            }
//            if(b.getType().equals(Material.EMERALD_ORE) || b.getType().equals(Material.DEEPSLATE_EMERALD_ORE)){
//                for(int j = 0; j < random; j++){
//                    b.getWorld().dropItem(b.getLocation(), new ItemStack(Material.EMERALD));
//                }
//            }
//            if(b.getType().equals(Material.COPPER_ORE) || b.getType().equals(Material.DEEPSLATE_COPPER_ORE)){
//                for(int j = 0; j < random; j++){
//                    b.getWorld().dropItem(b.getLocation(), new ItemStack(Material.RAW_COPPER));
//                }
//            }
//            if(b.getType().equals(Material.LAPIS_ORE) || b.getType().equals(Material.DEEPSLATE_LAPIS_ORE)){
//                for(int j = 0; j < random; j++){
//                    b.getWorld().dropItem(b.getLocation(), new ItemStack(Material.LAPIS_LAZULI));
//                }
//            }
//            if(b.getType().equals(Material.REDSTONE_ORE) || b.getType().equals(Material.DEEPSLATE_REDSTONE_ORE)){
//                for(int j = 0; j < random; j++){
//                    b.getWorld().dropItem(b.getLocation(), new ItemStack(Material.REDSTONE));
//                }
//            }
//        }
        }
        if(ring.getSlot() == 3){
            if(event.getPlayer().getPersistentDataContainer().has(breaking))return;
            Location loc = b.getLocation().subtract(1, 1, 1);
            ArrayList<Block> blockList = new ArrayList<>();
            ItemStack item = ring.getOwner().getInventory().getItemInMainHand();
            if(item.getType() != Material.DIAMOND_PICKAXE && item.getType() != Material.NETHERITE_PICKAXE) return;
            for(int j = 0; j < 3; j++){
                for(int i = 0; i < 3; i++){
                    for(int l = 0; l < 3; l++){
                        Block block = loc.clone().add(j, i, l).getBlock();
                        if(block.getType() != Material.BEDROCK && block.getType() != Material.OBSIDIAN && block.getType() != Material.END_PORTAL_FRAME){
                            blockList.add(block);
                        }
                    }
                }
            }
            event.getPlayer().getPersistentDataContainer().set(breaking, PersistentDataType.DOUBLE, 1.0);
            blockList.removeIf(block -> !block.getType().isSolid());
            breakBlockSequence(blockList, event.getPlayer());
        }
    }
    public void breakBlockSequence(ArrayList<Block> blockList, Player p){
        if(blockList.isEmpty()){
            p.getPersistentDataContainer().remove(breaking);
            return;
        }
        Block closest = blockList.get(0);
        Block temp;
        for(Block block : blockList){
            temp = block;
            if(closest.getLocation().distance(p.getLocation()) > temp.getLocation().distance(p.getLocation())){
                closest = block;
            }
        }
        blockList.remove(closest);
        //closest.breakNaturally(p.getInventory().getItemInMainHand());
        p.breakBlock(closest);
        playSounds(p.getLocation(), Sound.BLOCK_STONE_BREAK, 8, 1);
        ItemStack item = p.getInventory().getItemInMainHand();
//        if(item.getType().equals(Material.DIAMOND_PICKAXE) || item.getType().equals(Material.NETHERITE_PICKAXE)){
//            Damageable meta = (Damageable) item.getItemMeta();
//            meta.setDamage(meta.getDamage()+1);
//            item.setItemMeta(meta);
//        }
        if(!item.getType().equals(Material.DIAMOND_PICKAXE) && !item.getType().equals(Material.NETHERITE_PICKAXE)){
            p.getPersistentDataContainer().remove(breaking);
            return;
        }
        scheduler.runTaskLater(plugin, ()->{
            breakBlockSequence(blockList, p);
        }, getRan(1, 2));
    }
    @EventHandler
    public void onKickCheck2(PlayerKickEvent e){
        e.getPlayer().getPersistentDataContainer().remove(breaking);
    }
    @EventHandler
    public void onQuitCheck2(PlayerQuitEvent e){
        e.getPlayer().getPersistentDataContainer().remove(breaking);
    }

    public void ringAction(Ring ring, PlayerInteractEvent event){
        if(ring.getSlot() == 0)return;
        if(ring.getInstance().equals(oneRingInstance)){
            oneRingAction(ring, event);
        }
        if(ring.getInstance().equalsIgnoreCase(menInstance)){
            menRingAction(ring, event);
        }
        if(ring.getInstance().equalsIgnoreCase(elvenAirInstance)){
            elvenAirAction(ring, event);
        }
        if(ring.getInstance().equalsIgnoreCase(naryaInstance)){
            naryaAction(ring, event);
        }
        if(ring.getInstance().equalsIgnoreCase(nenyaInstance)){
            nenyaAction(ring, event);
        }
        if(ring.getInstance().equalsIgnoreCase(dwarvenInstnace)){
            dwarvenRingAction(ring, event);
        }
        if(ring.getInstance().equalsIgnoreCase(lesserInstance)){
            lesserRingAction(ring, event);
        }
    }
    public void lesserRingAction(Ring ring, PlayerInteractEvent event){
        String slotName = ring.getSlotName(ring.getSlot());
        Player p = ring.getOwner();
        if(slotName.equalsIgnoreCase("Absorption")){
            playSounds(p.getLocation(), Sound.ENTITY_HORSE_ARMOR, 4, 2);
            p.addPotionEffect(PotionEffectType.ABSORPTION.createEffect(600, 1));
        }
        if(slotName.equalsIgnoreCase("Jump Boost II")){
            playSounds(p.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 2, 2);
            p.addPotionEffect(PotionEffectType.JUMP.createEffect(400, 1));
        }
        if(slotName.equalsIgnoreCase("Jump Boost III")){
            playSounds(p.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 2, 2);
            p.addPotionEffect(PotionEffectType.JUMP.createEffect(400, 2));
        }
        if(slotName.equalsIgnoreCase("Slow Fall")){
            playSounds(p.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 2, 2);
            p.addPotionEffect(PotionEffectType.SLOW_FALLING.createEffect(200, 0));
        }
        if(slotName.equalsIgnoreCase("Levitation")){
            playSounds(p.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 2, 3);
            p.addPotionEffect(PotionEffectType.LEVITATION.createEffect(70, 3));
        }
        if(slotName.equalsIgnoreCase("Speed I")){
            playSounds(p.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 2, 3);
            p.addPotionEffect(PotionEffectType.SPEED.createEffect(500, 0));
        }
        if(slotName.equalsIgnoreCase("Speed II")){
            playSounds(p.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 2, 3);
            p.addPotionEffect(PotionEffectType.SPEED.createEffect(500, 1));
        }
        if(slotName.equalsIgnoreCase("Dolphin's Grace")){
            playSounds(p.getLocation(), Sound.ENTITY_DOLPHIN_AMBIENT, 2, 3);
            p.addPotionEffect(PotionEffectType.DOLPHINS_GRACE.createEffect(500, 0));
        }
        if(slotName.equalsIgnoreCase("Invisibility")){
            playSounds(p.getLocation(), Sound.ENTITY_PHANTOM_AMBIENT, 2, 3);
            p.addPotionEffect(PotionEffectType.INVISIBILITY.createEffect(500, 0));
        }
        if(slotName.equalsIgnoreCase("Small Fireball")){
            SmallFireball fireball = ring.getOwner().launchProjectile(SmallFireball.class);
            playSounds(ring.getOwner().getLocation(), Sound.ENTITY_GHAST_SHOOT, 4, 2);
        }
        if(slotName.equalsIgnoreCase("Push Back")){
            for(LivingEntity e :ring.getOwner().getLocation().getNearbyLivingEntities(4)){
                e.playEffect(EntityEffect.ENTITY_POOF);
                if(e instanceof Player){
                    Player player = (Player) e;
                    playSounds(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 2, 4);
                    playParticles(player.getLocation(), Particle.CLOUD, 10);
                }
                if(!e.getName().equals(ring.getOwnerName())){
                    Vector vec = e.getLocation().toVector().subtract(ring.getOwner().getLocation().toVector());
                    double mag = vec.length();
                    double cosA = Math.acos(vec.getX()/mag);
                    double cosG = Math.acos(vec.getZ()/mag);
                    vec.setX(Math.cos(cosA)*1.2);
                    vec.setY(0.8);
                    vec.setZ(Math.cos(cosG)*1.2);
                    e.setVelocity(vec);
                }
            }
        }
        if(slotName.equalsIgnoreCase("Healing")){
            for(LivingEntity e : ring.getOwner().getLocation().getNearbyLivingEntities(3)){
                e.addPotionEffect(PotionEffectType.REGENERATION.createEffect(300, 1));
                playSounds(p.getLocation(), Sound.BLOCK_BEACON_AMBIENT, 2, 3);
            }
        }
        if(slotName.equalsIgnoreCase("Small Riptide")){
            Vector vec = ring.getOwner().getLocation().getDirection();
            if(!p.isInWater() && !p.isInRain()){
                ring.setCoolDown(ring.getSlot(), -1);
                return;
            }
            ring.getOwner().setVelocity(vec.multiply(1.8));
            playSounds(ring.getOwner().getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_1, 2, 2);
        }
        if(slotName.equalsIgnoreCase("Fire Resistance")){
            p.addPotionEffect(PotionEffectType.FIRE_RESISTANCE.createEffect(1200, 0));
            playSounds(ring.getOwner().getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 4, 2);
        }
        if(slotName.equalsIgnoreCase("Bone Meal")){
            if(event.getClickedBlock() == null)return;
            event.getClickedBlock().applyBoneMeal(event.getBlockFace());
        }
        if(slotName.equalsIgnoreCase("Withering")){
            playSounds(ring.getOwner().getLocation(), Sound.ENTITY_WITHER_AMBIENT, 2, 2);
            for(LivingEntity entity : ring.getOwner().getLocation().getNearbyLivingEntities(4)){
                if(!entity.getName().equalsIgnoreCase(ring.getOwnerName())){
                    entity.addPotionEffect(PotionEffectType.WITHER.createEffect(70, 1));
                    entity.addPotionEffect(PotionEffectType.SLOW.createEffect(70, 0));
                }
            }
        }
        if(slotName.equalsIgnoreCase("Slowness")){
            playSounds(ring.getOwner().getLocation(), Sound.ENTITY_WITHER_AMBIENT, 2, 2);
            for(LivingEntity entity : ring.getOwner().getLocation().getNearbyLivingEntities(4)){
                if(!entity.getName().equalsIgnoreCase(ring.getOwnerName())){
                    entity.addPotionEffect(PotionEffectType.SLOW.createEffect(90, 0));
                }
            }
        }
        if(slotName.equalsIgnoreCase("Blindness")){
            playSounds(ring.getOwner().getLocation(), Sound.ENTITY_WITHER_AMBIENT, 2, 2);
            for(LivingEntity entity : ring.getOwner().getLocation().getNearbyLivingEntities(4)){
                if(!entity.getName().equalsIgnoreCase(ring.getOwnerName())){
                    entity.addPotionEffect(PotionEffectType.BLINDNESS.createEffect(90, 0));
                }

            }
        }
        if(slotName.equalsIgnoreCase("Leap")){
            playSounds(ring.getOwner().getLocation(), Sound.ENTITY_GOAT_LONG_JUMP, 4, 1);
            playParticles(ring.getOwner().getLocation(), Particle.SMOKE_NORMAL, 5);
            Vector vec = ring.getOwner().getVelocity();
            vec.setY(vec.getY()+1);
            ring.getOwner().setVelocity(vec);
        }
        if(slotName.equalsIgnoreCase("boost")){
            playSounds(ring.getOwner().getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 4, 3);
            playParticles(ring.getOwner().getLocation(), Particle.CLOUD, 5);
            Vector vec = ring.getOwner().getLocation().getDirection();;
            ring.getOwner().setVelocity(vec.multiply(1.05));
        }
    }
    public void dwarvenRingAction(Ring ring, PlayerInteractEvent event){
        if(ring.getSlot() == 0 )return;
        if(ring.getSlot() == 1){
            ring.getOwner().addPotionEffect(PotionEffectType.ABSORPTION.createEffect(1230, 2));
            playSounds(ring.getOwner().getLocation(), Sound.ENTITY_HORSE_ARMOR, 8, 1);
        }
    }
    public void nenyaAction(Ring ring, PlayerInteractEvent event){
        if(ring.getSlot() == 0)return;
        if(ring.getSlot() == 1){
            if(event.getClickedBlock() == null)return;
            event.getClickedBlock().applyBoneMeal(event.getBlockFace());
        }
        if(ring.getSlot() == 2){
            for(LivingEntity p :ring.getOwner().getLocation().getNearbyLivingEntities(6)){
                p.playEffect(EntityEffect.LOVE_HEARTS);

                if(p instanceof Player){
                    Player pa = (Player) p;
                    playSounds(p.getLocation(), Sound.BLOCK_BEACON_AMBIENT, 10, 1);
                    for(int j = 0; j < 10; j++){
                        int delay;
                        delay = j*10;
                        scheduler.runTaskLater(plugin, ()->{
                            playParticles(pa.getLocation().add(ran.nextInt(2), ran.nextInt(2), ran.nextInt(2)), Particle.HEART, 1);
                            playParticles(pa.getLocation().add(ran.nextInt(2), ran.nextInt(2), ran.nextInt(2)), Particle.HEART, 1);
                            playParticles(pa.getLocation().add(ran.nextInt(2), ran.nextInt(2), ran.nextInt(2)), Particle.HEART, 1);
                        },delay);
                    }
                }
                p.addPotionEffect(PotionEffectType.REGENERATION.createEffect(300, 2));
            }
        }
        if(ring.getSlot() == 3){
            if(ring.getOwner().getWorld().getEnvironment().equals(World.Environment.NORMAL)) {
                ring.getOwner().getWorld().setStorm(true);
                ring.getOwner().getWorld().setThundering(true);
                scheduler.runTaskLater(plugin, ()->{
                    randomStrom(ring.getOwner().getWorld(), 0);
                }, 100L);
            }
        }

        if(ring.getSlot() == 5){
            if(!ring.getOwner().getWorld().isThundering() && !ring.getOwner().getWorld().hasStorm())return;
            ring.getOwner().addPotionEffect(PotionEffectType.DAMAGE_RESISTANCE.createEffect(10, 5));
            for(LivingEntity e :ring.getOwner().getLocation().getNearbyLivingEntities(5)){
                if(!e.getName().equalsIgnoreCase(ring.getOwnerName())){
                    e.getLocation().getWorld().spawnEntity(e.getLocation(), EntityType.LIGHTNING);
                    if(!e.isDead()){
                        e.damage(getRan(25, 30), ring.getOwner());
                    }
                }
            }
        }
        if(ring.getSlot() == 4){

            if(!event.getPlayer().isInRain() && !event.getPlayer().isInWater()) {
                ring.setCoolDown(ring.getSlot(), -1);
                return;
            }
            playParticles(ring.getOwner().getLocation(), Particle.WATER_WAKE,  50);
            playParticles(ring.getOwner().getLocation(), Particle.WATER_BUBBLE,  50);
            playParticles(ring.getOwner().getLocation(), Particle.FALLING_WATER,  50);
            int random = ran.nextInt(3);
            if(random == 0){
                playSounds(ring.getOwner().getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_1, 10, 1);
            }
            if(random == 1){
                playSounds(ring.getOwner().getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_2, 10, 1);
            }
            if(random == 2){
                playSounds(ring.getOwner().getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_3, 10, 1);
            }
            Vector direction = ring.getOwner().getLocation().getDirection();
            ring.getOwner().setVelocity(direction.multiply(2.8));
            scheduler.runTaskLater(plugin, ()->{
                trackRipTide(ring.getOwner(), 0);
            }, 10L);
        }
    }
    public void trackRipTide(Player p, int safety){
        if(safety > 700)return;
        p.setFallDistance(0);
        for(int j = 0; j<2; j++){
            if(p.getLocation().add(0, -j, 0).getBlock().isSolid()){
                p.setFallDistance(0);
                return;
            }
        }
        int finalsafety = safety +1;
        scheduler.runTaskLater(plugin, ()->{
            trackRipTide(p, finalsafety);
        }, 2L);
    }
    public void randomStrom(World world, int safetyCount){
        if(safetyCount > 10){
            world.setStorm(false);
            world.setThundering(false);
            return;
        }
        int safety = safetyCount + 1;
        scheduler.runTaskLater(plugin, ()->{
            randomStrom(world, safety);
        }, 1200L);
    }
    public void naryaAction(Ring ring, PlayerInteractEvent event){
        if(ring.getSlot() == 0)return;
        if(ring.getSlot() == 1){
            if(event.getClickedBlock() == null)return;
            event.getClickedBlock().applyBoneMeal(event.getBlockFace());
        }
        if(ring.getSlot() == 2){
            for(LivingEntity e : ring.getOwner().getLocation().getNearbyLivingEntities(5)){
                if(e instanceof Player){
                    Player pa = (Player) e;
                    for(int j = 0; j < 10; j++){
                        int delay;
                        delay = j*10;
                        scheduler.runTaskLater(plugin, ()->{
                            playParticles(pa.getLocation().add(ran.nextInt(2), ran.nextInt(2)+0.5, ran.nextInt(2)), Particle.HEART,  1);
                            playParticles(pa.getLocation().add(ran.nextInt(2), ran.nextInt(2)+0.5, ran.nextInt(2)), Particle.HEART,  1);
                            playParticles(pa.getLocation().add(ran.nextInt(2), ran.nextInt(2)+0.5, ran.nextInt(2)), Particle.HEART,  1);
                        },delay);
                    }
                    playSounds(pa.getLocation(), Sound.BLOCK_BEACON_AMBIENT, 10, 1);
                }
                e.playEffect(EntityEffect.LOVE_HEARTS);
                e.addPotionEffect(PotionEffectType.FIRE_RESISTANCE.createEffect(400, 0));
                e.addPotionEffect(PotionEffectType.REGENERATION.createEffect(400, 0));
            }
        }
        if(ring.getSlot() == 3){
            int fireballAmount = ran.nextInt(2);
            fireballAmount +=2;
            for(int j = 0; j < fireballAmount; j++){
                int dellay = j *5;
                scheduler.runTaskLater(plugin, ()->{
                    ring.getOwner().launchProjectile(SmallFireball.class).getVelocity().multiply(4);
                    playParticles(ring.getOwner().getLocation(), Particle.SMALL_FLAME, 10);
                    playSounds(ring.getOwner().getLocation(), Sound.ENTITY_GHAST_SHOOT, 10, 1);
                }, dellay);
            }
        }
        if(ring.getSlot() == 4){
            Fireball fireball = ring.getOwner().launchProjectile(Fireball.class);
            fireball.setVisualFire(false);
            NamespacedKey key = new NamespacedKey(plugin, "Med_Boom");
            PersistentDataContainer data = fireball.getPersistentDataContainer();
            data.set(key, PersistentDataType.INTEGER, 1);
            playParticles(ring.getOwner().getLocation(), Particle.FLAME,  20);
            playSounds(ring.getOwner().getLocation(), Sound.ENTITY_GHAST_SHOOT, 10, 1);
        }
    }
    @EventHandler
    public void naryaExplode(EntityDamageByEntityEvent e){
//        NamespacedKey key = new NamespacedKey(plugin, "Med_Boom");
//        PersistentDataContainer data = e.getDamager().getPersistentDataContainer();
//        if(data.has(key)){
//            data.remove(key);
//            e.setDamage(e.getDamage()*3);
//        }
    }
    public void elvenAirAction(Ring ring, PlayerInteractEvent event){
        if(ring.getSlot() == 0)return;
        if(ring.getSlot() == 1){
            if(event.getClickedBlock() == null)return;
            event.getClickedBlock().applyBoneMeal(event.getBlockFace());
        }
        if(ring.getSlot() == 2){
            ring.getOwner().addPotionEffect(PotionEffectType.REGENERATION.createEffect(60, 3));
            for(LivingEntity entity : ring.getOwner().getLocation().getNearbyLivingEntities(4)){
                entity.addPotionEffect(PotionEffectType.REGENERATION.createEffect(60, 3));
                entity.playEffect(EntityEffect.LOVE_HEARTS);
                if(entity instanceof Player){
                    Player p = (Player) entity;
                    for(int j = 0; j < 10; j++){
                        int delay;
                        delay = j*10;
                        scheduler.runTaskLater(plugin, ()->{
                            playParticles(p.getLocation().add(ran.nextInt(2), ran.nextInt(2)+0.5, ran.nextInt(2)),Particle.HEART,  1);
                            playParticles(p.getLocation().add(ran.nextInt(2), ran.nextInt(2)+0.5, ran.nextInt(2)),Particle.HEART,  1);
                            playParticles(p.getLocation().add(ran.nextInt(2), ran.nextInt(2)+0.5, ran.nextInt(2)),Particle.HEART,  1);
                        },delay);
                    }
                    playSounds(p.getLocation(), Sound.BLOCK_BEACON_AMBIENT, 10, 1);
                }
            }
        }
        if(ring.getSlot() == 3){
            Vector vec = ring.getOwner().getLocation().getDirection();
            playParticles(ring.getOwner().getLocation(), Particle.CLOUD, 20);
            playSounds(ring.getOwner().getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 10, 2);
            ring.getOwner().setVelocity(vec.multiply(1.5));
        }
        if(ring.getSlot() == 4){
            for(LivingEntity p : ring.getOwner().getLocation().getNearbyLivingEntities(7)){
                p.playEffect(EntityEffect.ENTITY_POOF);
                if(p instanceof Player){
                    Player player = (Player) p;
                    playSounds(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 14, 1);
                    playParticles(player.getLocation(),Particle.SWEEP_ATTACK,  5);
                }
                if(!p.getName().equals(ring.getOwnerName())){
                    Vector vec = p.getLocation().toVector().subtract(ring.getOwner().getLocation().toVector());
                    double mag = vec.length();
                    double cosA = Math.acos(vec.getX()/mag);
                    double cosG = Math.acos(vec.getZ()/mag);
                    vec.setX(Math.cos(cosA)*1.9);
                    vec.setY(1);
                    vec.setZ(Math.cos(cosG)*1.9);
                    p.setVelocity(vec);
                }
            }
        }
        if(ring.getSlot() == 5){
            for(LivingEntity p : ring.getOwner().getLocation().getNearbyLivingEntities(2)){
                p.playEffect(EntityEffect.ENTITY_POOF);
                p.addPotionEffect(PotionEffectType.LEVITATION.createEffect(70, 6));
                p.addPotionEffect(PotionEffectType.SLOW_FALLING.createEffect(100000000, 0));
                if(p instanceof Player){
                    Player player = (Player) p;
                    playSounds(p.getLocation(), Sound.ENTITY_PHANTOM_FLAP, 10, 1);
                    playParticles(player.getLocation(),Particle.CLOUD,  100);
                }
                scheduler.runTaskLater(plugin, ()->{
                    airRingBlockChecker(p, 0);
                },  40L);
            }
        }
    }
    public void airRingBlockChecker(LivingEntity e, int safetyCount){
        if(e.getLocation().add(0, -1, 0).getBlock().getType() != Material.AIR){
            scheduler.runTask(plugin, ()->{
                e.removePotionEffect(PotionEffectType.SLOW_FALLING);
            });
            return;
        }
        safetyCount += 1;
        if(safetyCount > 110){
            scheduler.runTask(plugin, ()->{
                e.removePotionEffect(PotionEffectType.SLOW_FALLING);
            });
            return;
        }
        int finalSafetyCount = safetyCount;
        scheduler.runTaskLater(plugin, ()->{
            airRingBlockChecker(e, finalSafetyCount);
        },  4L);
    }

    public void menRingAction(Ring ring, PlayerInteractEvent event){
        if(ring.getSlot()==0)return;
        if(ring.getSlot() == 1){
            playSounds(ring.getOwner().getLocation(), Sound.ENTITY_GHAST_SHOOT, 6, 1);
            DragonFireball ball = ring.getOwner().launchProjectile(DragonFireball.class);
            playParticles(ring.getOwner().getLocation(),Particle.DRAGON_BREATH,  30);
        }
        if(ring.getSlot() == 2){
            playSounds(ring.getOwner().getLocation(), Sound.ENTITY_WITHER_SHOOT, 6, 1);
            playParticles(ring.getOwner().getLocation(), Particle.SMOKE_NORMAL, 10);
            WitherSkull witherSkull = ring.getOwner().launchProjectile(WitherSkull.class);
        }
        if(ring.getSlot() == 3){
            ring.getOwner().setInvisible(true);
            for(Player p : Bukkit.getOnlinePlayers()){
                p.hidePlayer(plugin, ring.getOwner());
            }
            menRingHide(ring.getOwner(), ring.getCode(), 0);
            playSounds(ring.getOwner().getLocation(), Sound.ENTITY_PHANTOM_AMBIENT, 8, 1);
            playParticles(ring.getOwner().getLocation(), Particle.SPELL,  20);
            //ring.getOwner().addPotionEffect(PotionEffectType.DARKNESS.createEffect(10000, 0));
            menRingHiderChecker(ring.getOwner(),ring.getCode(), 0);
            //ring.getOwner().setViewDistance(2);
        }
    }
    @EventHandler
    public void onDamage2(EntityDamageByEntityEvent e){
        if(e.getDamager() instanceof  Player){
            Player p = (Player) e.getDamager();
            if(p.isInvisible()){
                e.setCancelled(true);
            }
        }
    }
    public void menRingHide(Player man, int code, int counter){
        if(counter > 69){
            for(Ring ring : ringList){
                if(ring.getCode() == code){
                    if(!man.isInvisible())return;
                    if(man.isInvisible() && ring.isBeingHeld()){
                        playSounds(ring.getOwner().getLocation(), Sound.ENTITY_PHANTOM_AMBIENT, 8, 1);
                        playParticles(ring.getOwner().getLocation(), Particle.SPELL,  20);
                        man.setInvisible(false);
                        man.removePotionEffect(PotionEffectType.WEAKNESS);
                        man.removePotionEffect(PotionEffectType.DARKNESS);
                        man.removePotionEffect(PotionEffectType.NIGHT_VISION);
                        for(Player p : Bukkit.getOnlinePlayers()){
                            p.showPlayer(plugin, man);
                        }
                    }
                }
            }
            return;
        }
        if(!man.isInvisible()){
            for(Player p : Bukkit.getOnlinePlayers()){
                p.showPlayer(plugin, man);
            }
            return;
        }
        //man.addPotionEffect(PotionEffectType.WEAKNESS.createEffect(50, 10));
        man.addPotionEffect(PotionEffectType.DARKNESS.createEffect(50, 0));
        man.addPotionEffect(PotionEffectType.NIGHT_VISION.createEffect(50, 0));
        int finalCount = counter+1;
        scheduler.runTaskLater(plugin, ()->{
            menRingHide(man, code, finalCount);
        }, 20L);
    }
    @EventHandler
    public void onDeath(PlayerDeathEvent e){
        if(e.getPlayer().isInvisible()){
            e.getPlayer().setInvisible(false);
            for(Player p : Bukkit.getOnlinePlayers()){
                p.showPlayer(plugin, e.getPlayer());
            }
        }
    }
    public void menRingHiderChecker(Player p,int code, int safety){
        if(safety > 800){
            playSounds(p.getLocation(), Sound.ENTITY_PHANTOM_AMBIENT, 8, 1);
            playParticles(p.getLocation(), Particle.SPELL,  20);
            p.setInvisible(false);
            p.removePotionEffect(PotionEffectType.WEAKNESS);
            p.removePotionEffect(PotionEffectType.DARKNESS);
            p.removePotionEffect(PotionEffectType.NIGHT_VISION);
            for(Player p2 : Bukkit.getOnlinePlayers()){
                p2.showPlayer(plugin, p);
            }
            return;
        }
        for(Ring r : ringList){
            if(r.getCode() == code){
                if(!r.getOwner().isInvisible())return;
                if(!r.isBeingHeld() || !r.isOnline()){
                    playSounds(r.getOwner().getLocation(), Sound.ENTITY_PHANTOM_AMBIENT, 8, 1);
                    playParticles(r.getOwner().getLocation(), Particle.SPELL,  20);
                    p.setInvisible(false);
                    p.removePotionEffect(PotionEffectType.WEAKNESS);
                    p.removePotionEffect(PotionEffectType.DARKNESS);
                    p.removePotionEffect(PotionEffectType.NIGHT_VISION);
                    for(Player p2 : Bukkit.getOnlinePlayers()){
                        p2.showPlayer(plugin, p);
                    }
                    return;
                }
            }
        }
        int finalsafety = safety + 1;
        scheduler.runTaskLater(plugin, ()->{
            menRingHiderChecker(p, code, finalsafety);
        }, 10L);
    }
    @EventHandler
    public void menLeaveCheck(PlayerQuitEvent e){
        if(e.getPlayer().isInvisible()){
            e.getPlayer().setInvisible(false);
            e.getPlayer().removePotionEffect(PotionEffectType.WEAKNESS);
            e.getPlayer().removePotionEffect(PotionEffectType.DARKNESS);
            e.getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
            for(Player p : Bukkit.getOnlinePlayers()){
                p.showPlayer(plugin, e.getPlayer());
            }
        }
        for(Player p :Bukkit.getOnlinePlayers()){
            if(p.isInvisible()){
                e.getPlayer().showPlayer(plugin, p);
            }
        }
    }
    @EventHandler
    public void menLeaveCheck(PlayerKickEvent e){
        if(e.getPlayer().isInvisible()){
            e.getPlayer().setInvisible(false);
            e.getPlayer().removePotionEffect(PotionEffectType.WEAKNESS);
            e.getPlayer().removePotionEffect(PotionEffectType.DARKNESS);
            e.getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
            for(Player p : Bukkit.getOnlinePlayers()){
                p.showPlayer(plugin, e.getPlayer());
            }
        }
        for(Player p :Bukkit.getOnlinePlayers()){
            if(!p.isInvisible()){
                e.getPlayer().showPlayer(plugin, p);
            }
        }
    }
    @EventHandler
    public void menJoinCheck(PlayerJoinEvent e){
        for(Player p: Bukkit.getOnlinePlayers()){
            if(!p.isInvisible()){
                e.getPlayer().showPlayer(plugin, p);
            }
            if(p.isInvisible()){
                e.getPlayer().hidePlayer(plugin, p);
            }
        }
    }

    public void oneRingAction(Ring ring, PlayerInteractEvent event){
        int slot = ring.getSlot();
        if(slot == 1){
            ring.getOwner().addPotionEffect(PotionEffectType.ABSORPTION.createEffect(1210, 4));
            playSounds(ring.getOwner().getLocation(), Sound.ENTITY_HORSE_ARMOR, 10, 1);
        }
        if(slot == 2){
            playSounds(ring.getOwner().getLocation(), Sound.ENTITY_WITHER_AMBIENT, 10, 1);
            for(Player p : ring.getOwner().getLocation().getNearbyPlayers(10)){
                if(!p.getName().equals(ring.getOwnerName())){
                    p.addPotionEffect(PotionEffectType.BLINDNESS.createEffect(105, 1));
                    p.addPotionEffect(PotionEffectType.WITHER.createEffect(105, 0));
                }
            }
        }
        if(slot == 3){
            playSounds(ring.getOwner().getLocation(), Sound.ENTITY_WARDEN_SONIC_CHARGE, 15, 1);

            scheduler.runTaskLater(plugin, ()->{
                playSounds(ring.getOwner().getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 20, 1);
                for(LivingEntity p : ring.getOwner().getLocation().getNearbyLivingEntities(10)){
                    p.playEffect(EntityEffect.ENTITY_POOF);
                    if(p instanceof Player){
                        Player player = (Player) p;
                        playParticles(player.getLocation(), Particle.SONIC_BOOM, 10);
                    }
                    int damage = getRan(4, 6);
                    ArmorStand ar = ring.getOwner().getWorld().spawn(ring.getOwner().getLocation().add(0, -10, 0), ArmorStand.class);
                    ar.setVisible(false);
                    ar.setCustomNameVisible(false);
                    ar.setCustomName(ring.getOwnerName());
                    p.damage(damage, ar);
                    ar.remove();
                    if(!p.getName().equals(ring.getOwnerName())){
                        Vector vec = p.getLocation().toVector().subtract(ring.getOwner().getLocation().toVector());
                        double mag = vec.length();
                        double cosA = Math.acos(vec.getX()/mag);
                        double cosG = Math.acos(vec.getZ()/mag);
                        vec.setX(Math.cos(cosA)*2.5);
                        vec.setY(1.5);
                        vec.setZ(Math.cos(cosG)*2.5);
                        p.setVelocity(vec);
                    }
                }
            }, 30L);

        }
        if(slot == 4){
            playSounds(ring.getOwner().getLocation(), Sound.ENTITY_GHAST_SHOOT, 10, 1);
            playParticles(ring.getOwner().getLocation(), Particle.FLAME, 100);
            LargeFireball fireball = ring.getOwner().launchProjectile(LargeFireball.class);
            fireball.setShooter(ring.getOwner());
            NamespacedKey key = new NamespacedKey(plugin, "Big_Boom");
            PersistentDataContainer data = fireball.getPersistentDataContainer();
            data.set(key, PersistentDataType.INTEGER, 1);
        }
        if(slot == 5){
            int amount = getRan(2, 3);
            for(int j = 0; j < amount; j++){
                int dellay = j*getRan(6, 10);
                scheduler.runTaskLater(plugin, ()->{
                    WitherSkull fireball = ring.getOwner().launchProjectile(WitherSkull.class);
                    playParticles(ring.getOwner().getLocation(), Particle.SMOKE_LARGE, 10);
                    fireball.setShooter(ring.getOwner());
                    playSounds(ring.getOwner().getLocation(), Sound.ENTITY_WITHER_SHOOT, 10, 1);
                }, dellay);
            }
        }
        if(slot == 6){
            Vector vector = ring.getOwner().getLocation().getDirection();
            playSounds(ring.getOwner().getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 10, 1);
            playParticles(ring.getOwner().getLocation(), Particle.CLOUD, 50);
            ring.getOwner().setVelocity(vector.multiply(1.6));
        }
        if(slot == 7){
            Location loc = ring.getOwner().getLocation();
            for(Player p : Bukkit.getOnlinePlayers()){
                playSounds(p.getLocation(), Sound.ITEM_GOAT_HORN_SOUND_7, 10, 1);
            }
            for(Ring r : ringList){
                if(r.getInstance().equalsIgnoreCase(menInstance)){
                    r.getOwner().addPotionEffect(PotionEffectType.SPEED.createEffect(1870, 0));
                    r.getOwner().sendMessage(ChatColor.DARK_RED + ""+ChatColor.BOLD + "The One Ring is calling!");
                    r.getOwner().sendMessage(ChatColor.RED  + "The One Ring is at: " + ChatColor.GREEN + (int) loc.getX() + ", "+
                            (int)loc.getY() + ", " + (int) loc.getZ());
                }
            }
        }
        if(slot == 8){
            ring.getOwner().playSound(ring.getOwner().getLocation(), Sound.ENTITY_WITHER_AMBIENT, 10, 1);
            for(Ring r : ringList){
                if(r.getInstance().equalsIgnoreCase(menInstance) && ring.isOnline() && ring.getOwner().getLocation().distance(r.getOwner().getLocation()) <=500){
                    r.getOwner().addPotionEffect(PotionEffectType.POISON.createEffect(400, 1));
                    r.getOwner().addPotionEffect(PotionEffectType.SLOW.createEffect(400, 0));
                    r.getOwner().playSound(r.getOwner(), Sound.ENTITY_WITHER_AMBIENT, 10, 1);
                }
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
    public void killOneRing(PlayerDeathEvent e){
        for(Ring ring :ringList){
            if(ring.isOnline() && ring.getOwnerName().equals(e.getPlayer().getName()) && ring.getInstance().equalsIgnoreCase(oneRingInstance)){
                for(Player p : Bukkit.getOnlinePlayers()){
                    p.playSound(p, Sound.ENTITY_ENDER_DRAGON_GROWL, 20, 1);
                    playParticles(e.getPlayer().getLocation(), Particle.DRAGON_BREATH, 10);
                }
            }
        }
    }
    @EventHandler
    public void oneRingExplode(EntityExplodeEvent e){
        if(!(e.getEntity() instanceof LargeFireball))return;
        LargeFireball fireball = (LargeFireball) e.getEntity();
        NamespacedKey key = new NamespacedKey(plugin, "Big_Boom");
        PersistentDataContainer data = fireball.getPersistentDataContainer();
        if(data.has(key, PersistentDataType.INTEGER)){
            e.setCancelled(true);
            data.remove(key);
            e.getLocation().createExplosion(e.getEntity(), 3, true);
        }

    }
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e){
        NamespacedKey key = new NamespacedKey(plugin, "Med_Boom");
        PersistentDataContainer data = e.getDamager().getPersistentDataContainer();
//        if(e.getDamager() instanceof LargeFireball){
//            int random = ran.nextInt(2);
//            random += 2;
//            e.setDamage(e.getDamage()*);
//        }

        if(data.has(key)){
            int random = ran.nextInt(3);
            random += 3;
            data.remove(key);
            e.setDamage(e.getDamage()*random);
            return;
        }
        if(e.getDamager()instanceof SmallFireball){
            e.setDamage(10);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        scanInventoryForRing(e.getPlayer());
    }
    @EventHandler
    public void onLeave(PlayerQuitEvent e){
        if(e.getPlayer().hasPotionEffect(PotionEffectType.SLOW_FALLING)){
            e.getPlayer().removePotionEffect(PotionEffectType.SLOW_FALLING);
        }
    }
    @EventHandler
    public void onKick(PlayerKickEvent e){
        if(e.getPlayer().hasPotionEffect(PotionEffectType.SLOW_FALLING)){
            e.getPlayer().removePotionEffect(PotionEffectType.SLOW_FALLING);
        }
    }
    @EventHandler
    public void onClose(InventoryCloseEvent e){
        Player p = (Player) e.getPlayer();
        scanInventoryForRing(p);
    }
    @EventHandler
    public void onPickup(PlayerAttemptPickupItemEvent e){
        Player p = e.getPlayer();
        ItemStack item = e.getItem().getItemStack();
        NamespacedKey key = new NamespacedKey(plugin, "ringCode");
        NamespacedKey type = new NamespacedKey(plugin, "ringType");
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        if(data.has(key, PersistentDataType.INTEGER) && data.has(type, PersistentDataType.STRING)){
            if(data.get(type, PersistentDataType.STRING).equalsIgnoreCase(oneRingInstance)){
                int code = data.get(key, PersistentDataType.INTEGER);
                if(!codeList.containsKey(code)){
                    Ring ring = generateNewOneRing(code);
                    ring.setOwner(p.getName());
                }
            }
            if(data.get(type, PersistentDataType.STRING).equalsIgnoreCase(menInstance)){
                int code = data.get(key, PersistentDataType.INTEGER);
                if(!codeList.containsKey(code)){
                    Ring ring = generateNewManRing(code);
                    ring.setOwner(p.getName());
                }
            }
            if(data.get(type, PersistentDataType.STRING).equalsIgnoreCase(elvenAirInstance)){
                int code = data.get(key, PersistentDataType.INTEGER);
                if(!codeList.containsKey(code)){
                    Ring ring = generateNewElvenAIr(code);
                    ring.setOwner(p.getName());
                }
            }
            if(data.get(type, PersistentDataType.STRING).equalsIgnoreCase(naryaInstance)){
                int code = data.get(key, PersistentDataType.INTEGER);
                if(!codeList.containsKey(code)){
                    Ring ring = generateNewElvenRingOfFire(code);
                    ring.setOwner(p.getName());
                }
            }
            if(data.get(type, PersistentDataType.STRING).equalsIgnoreCase(nenyaInstance)){
                int code = data.get(key, PersistentDataType.INTEGER);
                if(!codeList.containsKey(code)){
                    Ring ring = generateNewNenya(code);
                    ring.setOwner(p.getName());
                }
            }
            if(data.get(type, PersistentDataType.STRING).equalsIgnoreCase(dwarvenInstnace)){
                int code = data.get(key, PersistentDataType.INTEGER);
                if(!codeList.containsKey(code)){
                    Ring ring = generateNewDwarven(code);
                    ring.setOwner(p.getName());
                }
            }
            if(data.get(type, PersistentDataType.STRING).equalsIgnoreCase(lesserInstance)){
                int code = data.get(key, PersistentDataType.INTEGER);
                if(!codeList.containsKey(code)){
                    NamespacedKey a1 = new NamespacedKey(plugin, "a1");
                    NamespacedKey a2 = new NamespacedKey(plugin, "a2");
                    NamespacedKey hEff = new NamespacedKey(plugin, "hEff");
                    int x = data.get(a1, PersistentDataType.INTEGER);
                    int y = data.get(a2, PersistentDataType.INTEGER);
                    int heldEffect = data.get(hEff, PersistentDataType.INTEGER);
                    Ring ring = generateLesserRing(code, x, y, heldEffect);
                    ring.setOwner(p.getName());
                }
            }
        }
    }
    public void scanInventoryForRing(Player p){
        NamespacedKey key = new NamespacedKey(plugin, "ringCode");
        NamespacedKey type = new NamespacedKey(plugin, "ringType");
        for(ItemStack item : p.getInventory()){
            if(item != null) {
                ItemMeta meta = item.getItemMeta();
                PersistentDataContainer data = meta.getPersistentDataContainer();
                if(data.has(key, PersistentDataType.INTEGER) && data.has(type, PersistentDataType.STRING)){
                    if(data.get(type, PersistentDataType.STRING).equalsIgnoreCase(oneRingInstance)){
                        int code = data.get(key, PersistentDataType.INTEGER);
                        if(!codeList.containsKey(code)){
                            Ring ring = generateNewOneRing(code);
                            ring.setOwner(p.getName());
                        }
                    }
                    if(data.get(type, PersistentDataType.STRING).equalsIgnoreCase(menInstance)){
                        int code = data.get(key, PersistentDataType.INTEGER);
                        if(!codeList.containsKey(code)){
                            Ring ring = generateNewManRing(code);
                            ring.setOwner(p.getName());
                        }
                    }
                    if(data.get(type, PersistentDataType.STRING).equalsIgnoreCase(elvenAirInstance)){
                        int code = data.get(key, PersistentDataType.INTEGER);
                        if(!codeList.containsKey(code)){
                            Ring ring = generateNewElvenAIr(code);
                            ring.setOwner(p.getName());
                        }
                    }
                    if(data.get(type, PersistentDataType.STRING).equalsIgnoreCase(naryaInstance)){
                        int code = data.get(key, PersistentDataType.INTEGER);
                        if(!codeList.containsKey(code)){
                            Ring ring = generateNewElvenRingOfFire(code);
                            ring.setOwner(p.getName());
                        }
                    }
                    if(data.get(type, PersistentDataType.STRING).equalsIgnoreCase(nenyaInstance)){
                        int code = data.get(key, PersistentDataType.INTEGER);
                        if(!codeList.containsKey(code)){
                            Ring ring = generateNewNenya(code);
                            ring.setOwner(p.getName());
                        }
                    }
                    if(data.get(type, PersistentDataType.STRING).equalsIgnoreCase(dwarvenInstnace)){
                        int code = data.get(key, PersistentDataType.INTEGER);
                        if(!codeList.containsKey(code)){
                            Ring ring = generateNewDwarven(code);
                            ring.setOwner(p.getName());
                        }
                    }
                    if(data.get(type, PersistentDataType.STRING).equalsIgnoreCase(lesserInstance)){
                        int code = data.get(key, PersistentDataType.INTEGER);
                        if(!codeList.containsKey(code)){
                            NamespacedKey a1 = new NamespacedKey(plugin, "a1");
                            NamespacedKey a2 = new NamespacedKey(plugin, "a2");
                            NamespacedKey hEff = new NamespacedKey(plugin, "hEff");
                            int x = data.get(a1, PersistentDataType.INTEGER);
                            int y = data.get(a2, PersistentDataType.INTEGER);
                            int heldEffect = data.get(hEff, PersistentDataType.INTEGER);
                            Ring ring = generateLesserRing(code, x, y, heldEffect);
                            ring.setOwner(p.getName());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void run() {
    }
    public void runTasks(){
        //one ring random stuff
        scheduler.runTaskTimer(plugin, ()->{
            for(Ring ring : ringList){
                if(ring.getInstance().equalsIgnoreCase(oneRingInstance) && ring.isOnline()){
                    int random = getRan(0, oneRingHurt);
                    int target = getRan(0, oneRingHurt);
                    if(random ==target){
                        int r2 = getRan(0, 5);
                        int durration = getRan(80, 700);
                        int amp = getRan(0,1);
                        if(r2 == 0){
                            ring.getOwner().addPotionEffect(PotionEffectType.CONFUSION.createEffect(durration, 0));
                        }
                        if(r2 == 1){
                            ring.getOwner().addPotionEffect(PotionEffectType.BLINDNESS.createEffect(durration, amp));
                        }
                        if(r2 == 2){
                            ring.getOwner().addPotionEffect(PotionEffectType.POISON.createEffect(durration, 0));
                        }
                        if(r2 == 3){
                            ring.getOwner().addPotionEffect(PotionEffectType.SLOW.createEffect(durration, amp));
                        }
                        if(r2 == 4){
                            ring.getOwner().addPotionEffect(PotionEffectType.HUNGER.createEffect(durration, amp));
                        }
                        if(r2 == 5){
                            ring.getOwner().addPotionEffect(PotionEffectType.DARKNESS.createEffect(durration, amp));
                        }
                        if(r2 == random){
                            ring.getOwner().addPotionEffect(PotionEffectType.POISON.createEffect(700, 0));
                            ring.getOwner().addPotionEffect(PotionEffectType.BLINDNESS.createEffect(700, amp));
                            ring.getOwner().addPotionEffect(PotionEffectType.CONFUSION.createEffect(700, 0));
                            ring.getOwner().addPotionEffect(PotionEffectType.HUNGER.createEffect(700, amp));
                            ring.getOwner().addPotionEffect(PotionEffectType.SLOW.createEffect(700, amp));
                            ring.getOwner().addPotionEffect(PotionEffectType.DARKNESS.createEffect(700, amp));
                        }
                    };
                }
            }
        }, 0L, 600L);
        //Boss Bar
        scheduler.runTaskTimer(plugin, ()->{
            nearBossBar.clear();
            for(Ring ring : ringList){
                if(ring.getInstance().equalsIgnoreCase(oneRingInstance) && ring.isOnline()){
                    for(Player p :Bukkit.getOnlinePlayers()){
                        if(Math.abs(ring.getOwner().getLocation().distance(p.getLocation())) <= 240 && !ring.getOwner().equals(p)){
                            nearBossBar.add(p.getName());
                            bar.addPlayer(p);
                        }
                    }
                }
            }
            for(Player p : Bukkit.getOnlinePlayers()){
                if(!nearBossBar.contains(p.getName())){
                    bar.removePlayer(p);
                }
            }
        }, 0L, 20L);
        scheduler.runTaskTimer(plugin, ()->{
            for(Ring ring : ringList){
                if(ring.getInstance().equalsIgnoreCase(oneRingInstance)){
                    if(ring.isOnline()){
                        Player p = ring.getOwner();
                        if(p.getHealth() <= 20.0){
                            bar.setColor(BarColor.RED);
                            double newProgress = p.getHealth()/20;
                            if(newProgress > 1.0) newProgress=1.0;
                            bar.setProgress(newProgress);
                        }
                        if(p.getHealth() >= 20.0){
                            bar.setColor(BarColor.YELLOW);
                            double newProgress = p.getHealth()/40;
                            if(newProgress > 1.0) newProgress=1.0;
                            bar.setProgress(newProgress);
                        }
                    }
                }
            }
        }, 0L, 10L);
    }
    @EventHandler
    public void oneRingDamage(EntityDamageEvent e){
        boolean isOneRing = false;
        for(Ring ring : ringList){
            if(ring.getOwnerName().equalsIgnoreCase(e.getEntity().getName()) && ring.getInstance().equalsIgnoreCase(oneRingInstance)){
                isOneRing = true;
            }
        }
        if(!isOneRing)return;
        Player p = (Player) e.getEntity();
        if(p.getHealth() <= 20.0){
            double newProgress = p.getHealth()/20;
            if(newProgress > 1.0) newProgress=1.0;
            bar.setProgress(newProgress);
        }
        if(p.getHealth() >= 20.0){
            double newProgress = p.getHealth()/40;
            if(newProgress > 1.0) newProgress=1.0;
            bar.setProgress(newProgress);
        }
        for(Player player : p.getLocation().getNearbyPlayers(150)){
            player.playSound(player, Sound.ENTITY_BLAZE_HURT, 6, 1);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player))return true;
        Player p = (Player) sender;
        if(command.getName().equalsIgnoreCase("RingCraftCore")){
            String arg = args[0];
            if(arg.equalsIgnoreCase("ringlist")){
                Bukkit.broadcastMessage("______________");
                for(Ring ring : ringList){
                    Bukkit.broadcastMessage(ring.getInstance());
                }
                Bukkit.broadcastMessage("______________");
            }
            if(arg.equalsIgnoreCase("reload")){
                reloadRings();
            }
            if(arg.equalsIgnoreCase("get")){
                String arg2 = args[1];
                if(arg2.equalsIgnoreCase("onering")){
                    Ring ring = generateNewOneRing(ran.nextInt(10000000));
                    ring.setOwner(p.getName());
                    p.getInventory().addItem(ring.getItem());
                }
                if(arg2.equalsIgnoreCase("menring")){
                    Ring ring = generateNewManRing(ran.nextInt(10000000));
                    ring.setOwner(p.getName());
                    p.getInventory().addItem(ring.getItem());
                }
                if(arg2.equalsIgnoreCase("Vilya")){
                    Ring ring = generateNewElvenAIr(ran.nextInt(10000000));
                    ring.setOwner(p.getName());
                    p.getInventory().addItem(ring.getItem());
                }
                if(arg2.equalsIgnoreCase("narya")){
                    Ring ring = generateNewElvenRingOfFire(ran.nextInt(10000000));
                    ring.setOwner(p.getName());
                    p.getInventory().addItem(ring.getItem());
                }
                if(arg2.equalsIgnoreCase("Nenya")){
                    Ring ring = generateNewNenya(ran.nextInt(10000000));
                    ring.setOwner(p.getName());
                    p.getInventory().addItem(ring.getItem());
                }
                if(arg2.equalsIgnoreCase("dwarven")){
                    Ring ring = generateNewDwarven(ran.nextInt(10000000));
                    ring.setOwner(p.getName());
                    p.getInventory().addItem(ring.getItem());
                }
                if(arg2.equalsIgnoreCase("Lesser")){
                    getRandomLesser(p);
                }
            }
        }
        return true;
    }

    public Ring generateNewOneRing(int code){
        ItemStack item = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta meta = item.getItemMeta();
        //ring code
        NamespacedKey key = new NamespacedKey(plugin, "ringCode");
        PersistentDataContainer data = meta.getPersistentDataContainer();
        if(!data.has(key, PersistentDataType.INTEGER)) data.set(key, PersistentDataType.INTEGER, code);
        //ring instance
        key = new NamespacedKey(plugin, "ringType");
        if(!data.has(key, PersistentDataType.STRING)) data.set(key, PersistentDataType.STRING, oneRingInstance);

        ArrayList<String> loreStuff = new ArrayList<>();
        loreStuff.add("The One Ring to rule them all.");
        meta.setLore(loreStuff);
        meta.setCustomModelData(10);
        meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "One Ring");
        item.setItemMeta(meta);
        Ring ring = new Ring(9, item, this, plugin);
        ring.setCode(code);
        ring.modifySlot(0, ChatColor.DARK_RED, "Disabled", -1);
        ring.modifySlot(1, ChatColor.YELLOW, "Absorption", 130);
        ring.modifySlot(2, ChatColor.RED, "Blind", 25);
        ring.modifySlot(3, ChatColor.RED, "Push Back", 20);
        ring.modifySlot(4, ChatColor.GOLD, "Fireball", 15);
        ring.modifySlot(5, ChatColor.GRAY, "Wither Heads", 10);
        ring.modifySlot(6, ChatColor.WHITE, "Boost", 10);
        ring.modifySlot(7, ChatColor.BLUE, "Call Rings of Men", 300);
        ring.modifySlot(8, ChatColor.BLUE, "Poison Rings of Men", 60);
        ring.addEffect(PotionEffectType.INCREASE_DAMAGE.createEffect(10000000, 0));
        ring.addEffect(PotionEffectType.DAMAGE_RESISTANCE.createEffect(10000000, 1));
        ring.addEffect(PotionEffectType.REGENERATION.createEffect(10000000, 0));
        ring.addEffect(PotionEffectType.CONDUIT_POWER.createEffect(10000000, 0));
        ring.setInstance(oneRingInstance);
        ringList.add(ring);
        codeList.put(code, ring);
        loreStuff.clear();
        return ring;
    }

    public Ring generateNewManRing(int code){
        ItemStack item = new ItemStack(Material.IRON_NUGGET);
        ArrayList<String> loreStuff = new ArrayList<>();
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, "ringCode");
        PersistentDataContainer data = meta.getPersistentDataContainer();
        if(!data.has(key, PersistentDataType.INTEGER)) data.set(key, PersistentDataType.INTEGER, code);
        //ring instance
        key = new NamespacedKey(plugin, "ringType");
        if(!data.has(key, PersistentDataType.STRING)) data.set(key, PersistentDataType.STRING, menInstance);
        loreStuff.add("An ancient artifact created by the Dark Lord Sauron.");
        meta.setLore(loreStuff);
        meta.setCustomModelData(16);
        meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Ring of Man");
        item.setItemMeta(meta);
        Ring ring = new Ring(4, item, this, plugin);
        ring.setCode(code);
        ring.modifySlot(0, ChatColor.DARK_RED, "Disabled", -1);
        ring.modifySlot(1, ChatColor.LIGHT_PURPLE, "Dragon's Breath", 20);
        ring.modifySlot(2, ChatColor.GRAY, "Wither Skull", 12);
        ring.modifySlot(3, ChatColor.WHITE, "Invisibility", 360);
        ring.addEffect(PotionEffectType.REGENERATION.createEffect(10000000, 0));
        ring.addEffect(PotionEffectType.DAMAGE_RESISTANCE.createEffect(10000000, 0));
        ring.setInstance(menInstance);
        ringList.add(ring);
        codeList.put(code, ring);
        loreStuff.clear();
        return ring;
    }

    public Ring generateNewElvenAIr(int code){
        ArrayList<String> loreStuff = new ArrayList<>();
        ItemStack item = new ItemStack(Material.IRON_NUGGET);
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, "ringCode");
        PersistentDataContainer data = meta.getPersistentDataContainer();
        if(!data.has(key, PersistentDataType.INTEGER)) data.set(key, PersistentDataType.INTEGER, code);
        //ring instance
        key = new NamespacedKey(plugin, "ringType");
        if(!data.has(key, PersistentDataType.STRING)) data.set(key, PersistentDataType.STRING, elvenAirInstance);
        loreStuff.add("The Elven Ring of Air.");
        loreStuff.add("Prevent decay through the effect of time.");
        meta.setLore(loreStuff);
        meta.setCustomModelData(15);
        meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Vilya");
        item.setItemMeta(meta);
        Ring ring = new Ring(6, item, this, plugin);
        ring.setCode(code);
        ring.modifySlot(0, ChatColor.DARK_RED, "Disabled", -1);
        ring.modifySlot(1, ChatColor.WHITE, "Bone Meal", -1);
        ring.modifySlot(2, ChatColor.LIGHT_PURPLE, "Healing", 30);
        ring.modifySlot(3, ChatColor.WHITE, "Boost", 10);
        ring.modifySlot(4, ChatColor.RED, "Push Back", 15);
        ring.modifySlot(5, ChatColor.AQUA, "Levitation", 10);
        ring.addEffect(PotionEffectType.REGENERATION.createEffect(10000000, 0));
        ring.addEffect(PotionEffectType.DAMAGE_RESISTANCE.createEffect(10000000, 0));
        ring.setInstance(elvenAirInstance);
        ringList.add(ring);
        codeList.put(code, ring);
        loreStuff.clear();
        return ring;
    }

    public Ring generateNewElvenRingOfFire(int code){
        ArrayList<String> loreStuff = new ArrayList<>();
        ItemStack item = new ItemStack(Material.IRON_NUGGET);
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, "ringCode");
        PersistentDataContainer data = meta.getPersistentDataContainer();
        if(!data.has(key, PersistentDataType.INTEGER)) data.set(key, PersistentDataType.INTEGER, code);
        //ring instance
        key = new NamespacedKey(plugin, "ringType");
        if(!data.has(key, PersistentDataType.STRING)) data.set(key, PersistentDataType.STRING, naryaInstance);
        loreStuff.add("The Elven Ring of Fire.");
        loreStuff.add("Its final bearer was the Wizard Gandalf, now you wear it.");
        meta.setLore(loreStuff);
        meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Narya");
        meta.setCustomModelData(13);
        item.setItemMeta(meta);
        Ring ring = new Ring(5, item, this, plugin);
        ring.addEffect(PotionEffectType.FIRE_RESISTANCE.createEffect(10000000, 0));
        ring.addEffect(PotionEffectType.REGENERATION.createEffect(10000000, 0));
        ring.addEffect(PotionEffectType.DAMAGE_RESISTANCE.createEffect(10000000, 0));
        ring.modifySlot(0, ChatColor.DARK_RED, "Disabled", -1);
        ring.modifySlot(1, ChatColor.WHITE, "Bone Meal", -1);
        ring.modifySlot(2, ChatColor.LIGHT_PURPLE, "Heal & Fire Resistance", 30);
        ring.modifySlot(3, ChatColor.RED, "Small Fireball", 5);
        ring.modifySlot(4, ChatColor.GOLD, "Large Fireball", 12);
        ring.setInstance(naryaInstance);
        ring.setCode(code);
        ringList.add(ring);
        codeList.put(code, ring);
        loreStuff.clear();
        return ring;
    }
    public Ring generateNewNenya(int code){
        ArrayList<String> loreStuff = new ArrayList<>();
        ItemStack item = new ItemStack(Material.IRON_NUGGET);
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, "ringCode");
        PersistentDataContainer data = meta.getPersistentDataContainer();
        if(!data.has(key, PersistentDataType.INTEGER)) data.set(key, PersistentDataType.INTEGER, code);
        //ring instance
        key = new NamespacedKey(plugin, "ringType");
        if(!data.has(key, PersistentDataType.STRING)) data.set(key, PersistentDataType.STRING, nenyaInstance);
        loreStuff.add("Nenya was given by Celebrimbor directly to Galadriel ");
        meta.setLore(loreStuff);
        meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Nenya");
        meta.setCustomModelData(14);
        item.setItemMeta(meta);
        Ring ring = new Ring(6, item, this, plugin);
        ring.addEffect(PotionEffectType.DOLPHINS_GRACE.createEffect(10000000, 1));
        ring.addEffect(PotionEffectType.WATER_BREATHING.createEffect(10000000, 0));
        ring.addEffect(PotionEffectType.REGENERATION.createEffect(10000000, 0));
        ring.addEffect(PotionEffectType.DAMAGE_RESISTANCE.createEffect(10000000, 0));
        ring.addEffect(PotionEffectType.CONDUIT_POWER.createEffect(10000000, 0));
        ring.modifySlot(0, ChatColor.DARK_RED, "Disabled", -1);
        ring.modifySlot(1, ChatColor.WHITE, "Bone Meal", -1);
        ring.modifySlot(2, ChatColor.LIGHT_PURPLE, "Healing", 30);
        ring.modifySlot(3, ChatColor.AQUA, "Summon Rain Storm", 600);
        ring.modifySlot(4, ChatColor.WHITE, "Riptide", 5);
        ring.modifySlot(5, ChatColor.GOLD, "Smite", 10);
        ring.setInstance(nenyaInstance);
        ring.setCode(code);
        ringList.add(ring);
        codeList.put(code, ring);
        loreStuff.clear();
        return ring;
    }

    public Ring generateNewDwarven(int code){
        ArrayList<String> loreStuff = new ArrayList<>();
        ItemStack item = new ItemStack(Material.IRON_NUGGET);
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, "ringCode");
        PersistentDataContainer data = meta.getPersistentDataContainer();
        if(!data.has(key, PersistentDataType.INTEGER)) data.set(key, PersistentDataType.INTEGER, code);
        //ring instance
        key = new NamespacedKey(plugin, "ringType");
        if(!data.has(key, PersistentDataType.STRING)) data.set(key, PersistentDataType.STRING, dwarvenInstnace);
        loreStuff.add("The most redoubtable warriors of all the Speaking Peoples.");
        meta.setLore(loreStuff);
        meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Dwarven Ring");
        meta.setCustomModelData(12);
        item.setItemMeta(meta);

        Ring ring = new Ring(4, item, this, plugin);

        ring.addEffect(PotionEffectType.DAMAGE_RESISTANCE.createEffect(10000000, 0));
        ring.addEffect(PotionEffectType.FAST_DIGGING.createEffect(10000000, 1));
        ring.addEffect(PotionEffectType.INCREASE_DAMAGE.createEffect(10000000, 0));
        ring.modifySlot(0, ChatColor.DARK_RED, "Disabled", -1);
        ring.modifySlot(1, ChatColor.YELLOW, "Absorption", 90);
        ring.modifySlot(2, ChatColor.AQUA, "Fortune IV", -1);
        ring.modifySlot(3, ChatColor.WHITE, "Tunnel", -1);
        ring.setCode(code);
        ring.setInstance(dwarvenInstnace);
        ringList.add(ring);
        codeList.put(code, ring);
        loreStuff.clear();
        return ring;
    }
    public Ring generateLesserRing(int code, int ability1, int ability2, int heldEffect){
        ArrayList<String> loreStuff = new ArrayList<>();
        ArrayList<Integer> abilityList = new ArrayList<>();
        abilityList.add(ability1);
        abilityList.add(ability2);
        abilityList.set(0, ability1);
        abilityList.set(1, ability2);
        ItemStack item = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, "ringCode");
        PersistentDataContainer data = meta.getPersistentDataContainer();
        if(!data.has(key, PersistentDataType.INTEGER)) data.set(key, PersistentDataType.INTEGER, code);
        key = new NamespacedKey(plugin, "a1");
        data = meta.getPersistentDataContainer();
        data.set(key, PersistentDataType.INTEGER, ability1);

        key = new NamespacedKey(plugin, "a2");
        data = meta.getPersistentDataContainer();
        data.set(key, PersistentDataType.INTEGER, ability2);

        key = new NamespacedKey(plugin, "hEff");
        data = meta.getPersistentDataContainer();
        data.set(key, PersistentDataType.INTEGER, heldEffect);
        //ring instance
        key = new NamespacedKey(plugin, "ringType");
        if(!data.has(key, PersistentDataType.STRING)) data.set(key, PersistentDataType.STRING, lesserInstance);
        loreStuff.add("Though the lesser rings were quite useful,");
        loreStuff.add("they did not have the strength, abilities, or power");
        loreStuff.add("as the yet-to-be-forged Rings of Power.");
        meta.setLore(loreStuff);
        meta.setDisplayName(ChatColor.GOLD + "Lesser Ring");
        meta.setCustomModelData(10);
        item.setItemMeta(meta);
        Ring ring = new Ring(3, item, this, plugin);
        if(heldEffect == 1){
            ring.addEffect(PotionEffectType.DAMAGE_RESISTANCE.createEffect(10000000, 0));
        }
        if(heldEffect == 2){
            ring.addEffect(PotionEffectType.REGENERATION.createEffect(10000000, 0));
        }
        if(heldEffect == 3){
            ring.addEffect(PotionEffectType.LUCK.createEffect(10000000, 2));
        }
        if(heldEffect == 4){
            ring.addEffect(PotionEffectType.WATER_BREATHING.createEffect(10000000, 0));
        }
        if(heldEffect == 5){
            ring.addEffect(PotionEffectType.NIGHT_VISION.createEffect(10000000, 0));
        }
        if(heldEffect == 6){
            ring.addEffect(PotionEffectType.LUCK.createEffect(10000000, 1));
        }
        if(heldEffect == 7){
            ring.addEffect(PotionEffectType.FAST_DIGGING.createEffect(10000000, 0));
        }
        ring.modifySlot(0, ChatColor.DARK_RED, "Disabled", -1);
        for(int j = 0; j <abilityList.size(); j++ ){
            if(abilityList.get(j) == 1){
                ring.modifySlot(j+1, ChatColor.YELLOW, "Absorption", 60);
            }
            if(abilityList.get(j) == 2){
                ring.modifySlot(j+1, ChatColor.LIGHT_PURPLE, "Fortune I", -1);
            }
            if(abilityList.get(j) == 3){
                ring.modifySlot(j+1, ChatColor.LIGHT_PURPLE, "Fortune II", -1);
            }
            if(abilityList.get(j) == 4){
                ring.modifySlot(j+1, ChatColor.GREEN, "Jump Boost III", 45);
            }
            if(abilityList.get(j) == 5){
                ring.modifySlot(j+1, ChatColor.WHITE, "Slow Fall", 30);
            }
            if(abilityList.get(j) == 6){
                ring.modifySlot(j+1, ChatColor.AQUA, "Levitation", 30);
            }
            if(abilityList.get(j) == 7){
                ring.modifySlot(j+1, ChatColor.WHITE, "Speed II", 60);
            }
            if(abilityList.get(j) == 8){
                ring.modifySlot(j+1, ChatColor.AQUA, "Dolphin's Grace", 60);
            }
            if(abilityList.get(j) == 9){
                ring.modifySlot(j+1, ChatColor.WHITE, "Invisibility", 40);
            }
            if(abilityList.get(j) == 10){
                ring.modifySlot(j+1, ChatColor.GOLD, "Small Fireball", 10);
            }
            if(abilityList.get(j) == 11){
                ring.modifySlot(j+1, ChatColor.RED, "Push Back", 16);
            }
            if(abilityList.get(j) == 12){
                ring.modifySlot(j+1, ChatColor.LIGHT_PURPLE, "Healing", 35);
            }
            if(abilityList.get(j) == 13){
                ring.modifySlot(j+1, ChatColor.WHITE, "Small Riptide", 10);
            }
            if(abilityList.get(j) == 14){
                ring.modifySlot(j+1, ChatColor.DARK_PURPLE, "Fire Resistance", 90);
            }
            if(abilityList.get(j) == 15){
                ring.modifySlot(j+1, ChatColor.WHITE, "Bone Meal", 1);
            }
            if(abilityList.get(j) == 16){
                ring.modifySlot(j+1, ChatColor.GRAY, "Withering", 35);
            }
            if(abilityList.get(j) == 17){
                ring.modifySlot(j+1, ChatColor.GRAY, "Blindness", 35);
            }
            if(abilityList.get(j) == 18){
                ring.modifySlot(j+1, ChatColor.GREEN, "Leap", 8);
            }
            if(abilityList.get(j) == 19){
                ring.modifySlot(j+1, ChatColor.GOLD, "Fire Aspect", -1);
            }
            if(abilityList.get(j) == 20){
                ring.modifySlot(j+1, ChatColor.WHITE, "Boost", 10);
            }
        }
        ring.setCode(code);
        ring.setInstance(lesserInstance);
        ringList.add(ring);
        codeList.put(code, ring);
        loreStuff.clear();
        return ring;
    }
    public void getRandomLesser(Player p){
        int x = getRan(1, 20);
        Bukkit.broadcastMessage("x: " + x);
        int y = getRan(1, 20);
        Bukkit.broadcastMessage("y: " + y);
        int heldRan = getRan(1, 7);
        Bukkit.broadcastMessage("heldRan: " + heldRan);
        if(x == y){
            getRandomLesser(p);
            return;
        }
        Ring ring = generateLesserRing(ran.nextInt(10000000), x, y, heldRan);
        ring.setOwner(p.getName());
        p.getInventory().addItem(ring.getItem());
    }
    public int getRan(int min, int max){
        int a = ran.nextInt(max+1-min);
        a += min;
        return a;
    }
    @EventHandler
    public void onDamageItem(EntityDamageEvent e){
        if(e.getEntity().getType().equals(EntityType.DROPPED_ITEM)){
            Item item = (Item) e.getEntity();
            NamespacedKey key = new NamespacedKey(plugin, "ringType");
            PersistentDataContainer data = item.getItemStack().getItemMeta().getPersistentDataContainer();
            if(data.has(key)){
                e.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void onDespawn(ItemDespawnEvent e){
        //if(e.getEntity().getType().equals(EntityType.DROPPED_ITEM)){
        Item item = e.getEntity();
        NamespacedKey key = new NamespacedKey(plugin, "ringType");
        PersistentDataContainer data = item.getItemStack().getItemMeta().getPersistentDataContainer();
        if(data.has(key)){
            e.setCancelled(true);
        }
        //}
    }
//    @EventHandler
//    public void onPickUp(EntityPickupItemEvent e){
//        Item item = e.getItem();
//        NamespacedKey key = new NamespacedKey(plugin, "ringType");
//        PersistentDataContainer data = item.getItemStack().getItemMeta().getPersistentDataContainer();
//        if(!e.getEntity().isCustomNameVisible() && data.has(key)){
//            e.setCancelled(true);
//        }
//    }

    public void reloadRings(){
        for(Ring ring : ringList){
            ring.removeOwner();
            ring = null;
        }
        ringList.clear();
        ownerRings.clear();
        ringOwners.clear();
        codeList.clear();
        for(Player p : Bukkit.getOnlinePlayers()){
            scanInventoryForRing(p);
        }
    }
    public boolean checkCanHold(Ring ring){
        if(ring.getOwner() == null)return false;
        ItemStack offhand = ring.getOwner().getInventory().getItemInOffHand();
        if(offhand.getItemMeta() == null){
            return false;
        }
        NamespacedKey key = new NamespacedKey(plugin, "ringType");
        PersistentDataContainer data = offhand.getItemMeta().getPersistentDataContainer();
        return data.has(key);
    }
}
