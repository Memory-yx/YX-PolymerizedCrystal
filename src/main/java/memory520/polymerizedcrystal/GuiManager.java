package memory520.polymerizedcrystal;

import io.lumine.xikage.mythicmobs.MythicMobs;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class GuiManager implements Listener {

    ItemStack[] Slot = new ItemStack[5];
    List guilist;
    List equiplist;
    Configuration Config;
    ItemPolymerized itemPolymerized = new ItemPolymerized();
    String name;

    //创建一个GUI类
    public static class PolymerizedCrystalInv implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    public void Itemlist(Configuration config){
        for (int i=0;i<=4;i++){
            //设置物品属性
            Slot[i] = new ItemStack(Material.valueOf(config.getString("gui.Slot"+(i+1)+".type")));
            ItemMeta itemMeta = Slot[i].getItemMeta();
            itemMeta.setDisplayName(config.getString("gui.Slot"+(i+1)+".name").replace("&", "§"));
            guilist = config.getList("gui.Slot"+(i+1)+".lore");
            for (int j=0;j<guilist.size();j++) {
                guilist.set(j,guilist.get(j).toString().replace("&", "§"));
            }
            itemMeta.setLore(guilist);
            itemMeta.setCustomModelData(Integer.valueOf(config.getString("gui.Slot"+(i+1)+".CustomModelData")));
            Slot[i].setItemMeta(itemMeta);
        }

        equiplist = config.getList("slot.Equip");
        Config = config;
    }

    public void Invopen(Player player){
        Inventory inv = Bukkit.createInventory(new PolymerizedCrystalInv(), InventoryType.valueOf("BREWING"),Config.getString("gui.GUIName").replace("&", "§"));

        inv.setItem(0,Slot[0]);
        inv.setItem(1,Slot[1]);
        inv.setItem(2,Slot[2]);
        inv.setItem(3,Slot[3]);
        inv.setItem(4,Slot[4]);

        name = Slot[3].getItemMeta().getDisplayName();

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e){
        if(e.getInventory().getHolder() instanceof PolymerizedCrystalInv){
            Player player = (Player)e.getWhoClicked();
            //声明一个变量为玩家手上物品
            ItemStack CursorHand = e.getCursor();
            //声明一个变量为玩家点击的格子
            ItemStack CurrentSlot = e.getCurrentItem();

            //如果玩家点击GUI中的格子就取消事件
            if(e.getRawSlot() <= 4 && e.getRawSlot() >= 0){ e.setCancelled(true); }

            //判断玩家点击ID3的槽位(进行聚合)
            if(e.getRawSlot() == 3){
                if(e.getInventory().getItem(4).equals(Slot[4]) || e.getInventory().getItem(0).equals(Slot[0])){
                    player.sendRawMessage(Config.getString("MessagePrefix").replace("&", "§")+"§7你没有放置物品,无法进行聚合");
                    itemPolymerized.sound(player,Config.getString("sound.PolymerizationError"));
                    return;
                }
                String CrystalName = null;
                for (String s:Config.getConfigurationSection("item").getKeys(false)) {
                    if(e.getInventory().getItem(0).getItemMeta().getDisplayName().equals(Config.getString("item."+s+".name").replace("&", "§"))){
                        CrystalName = s;
                        break;
                    }
                }
                //调用Item类的算法
                itemPolymerized.LoreSet(e.getInventory().getItem(4),CrystalName,Config,player,Slot,e);
            }

            //判断玩家点击ID4的槽位(武器槽)
            if(e.getRawSlot() == 4){
                //查找玩家是否用config配置中的武器类型点击格子
                for (int i = 0; i< equiplist.size(); i++){
                    if(e.getCursor().getType() == Material.valueOf((String) equiplist.get(i))){
                        //检查是否为配置中的格子物品
                        if(!e.getCurrentItem().equals(Slot[4])){
                            e.setCursor(CurrentSlot);
                        }else{
                            e.setCursor(null);
                        }
                        e.setCurrentItem(CursorHand);
                        break;
                    }
                }
                //如果不是配置中的格子物品并且手持物品为null,就替换GUI与鼠标物品
                if(!e.getCurrentItem().equals(Slot[4]) && CursorHand.getAmount() < 1){
                    e.setCurrentItem(Slot[4]);
                    e.setCursor(CurrentSlot);
                }
            }

            //判断玩家点击ID0的槽位(水晶槽)
            if(e.getRawSlot() == 0){
                //查找玩家是否用config配置中的武器类型点击格子
                for (String s:Config.getConfigurationSection("item").getKeys(false)) {
                    if(e.getCursor().getItemMeta() == null){break;}
                    if(e.getCursor().getItemMeta().getDisplayName().equals(Config.getString("item."+s+".name").replace("&", "§"))){
                        //检查是否为配置中的格子物品
                        if(!e.getCurrentItem().equals(Slot[0])){
                            e.setCursor(CurrentSlot);
                        }else{
                            e.setCursor(null);
                        }
                        e.setCurrentItem(CursorHand);

                        //检查物品消耗系统是否开启
                        if(Config.getBoolean("item."+s+".upgradeItem.upgradeOpen")){
                            //声明一个物品数据并赋值
                            ItemStack upgradeItem;
                            upgradeItem = MythicMobs.inst().getItemManager().getItemStack(Config.getString("item."+s+".upgradeItem.upgradeName"));
                            upgradeItem.setAmount(Integer.valueOf(Config.getString("item."+s+".upgradeItem.upgradeAmount")));
                            ItemMeta meta = upgradeItem.getItemMeta();
                            meta.setDisplayName(upgradeItem.getItemMeta().getDisplayName()+"§7 x"+upgradeItem.getAmount()+" §7消耗");
                            upgradeItem.setItemMeta(meta);
                            //改变GUI上的物品
                            e.getInventory().setItem(2,upgradeItem);
                        }else{
                            e.getInventory().setItem(2,Slot[2]);
                        }

                        break;
                    }
                }
                //如果不是配置中的格子物品并且手持物品为null,就替换GUI与鼠标物品
                if(!e.getCurrentItem().equals(Slot[0]) && CursorHand.getAmount() < 1){
                    e.setCurrentItem(Slot[0]);
                    e.setCursor(CurrentSlot);
                    e.getInventory().setItem(2,Slot[2]);
                }
            }

            //判断玩家点击ID1的槽位(粉末槽)
            if(e.getRawSlot() == 1){
                //查找玩家是否用config配置中的武器类型点击格子
                for (String s:Config.getConfigurationSection("derivation").getKeys(false)) {
                    if(e.getCursor().getItemMeta() == null){break;}
                    if(e.getCursor().getItemMeta().getDisplayName().equals(Config.getString("derivation."+s+".name").replace("&", "§"))){
                        //检查是否为配置中的格子物品
                        if(!e.getCurrentItem().equals(Slot[1])){
                            e.setCursor(CurrentSlot);
                        }else{
                            e.setCursor(null);
                        }
                        e.setCurrentItem(CursorHand);
                        break;
                    }
                }
                //如果不是配置中的格子物品并且手持物品为null,就替换GUI与鼠标物品
                if(!e.getCurrentItem().equals(Slot[1]) && CursorHand.getAmount() < 1){
                    e.setCurrentItem(Slot[1]);
                    e.setCursor(CurrentSlot);
                }
            }

            itemPolymerized.sound(player,Config.getString("sound.GuiClick"));
        }
    }

    //关闭GUI后返还物品到玩家背包
    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Inventory inventory = e.getInventory();
        HumanEntity player = e.getPlayer();

        if(inventory == null){ return; }
        if(inventory.getHolder() instanceof PolymerizedCrystalInv){

            ItemStack itemStack;

            for (int i=0;i<5;i++){
                if(i==2 || i==3){continue;}
                itemStack = inventory.getItem(i);;
                if (!inventory.getItem(i).equals(Slot[i])) {
                    player.getInventory().addItem(itemStack);
                }
            }
        }
    }
}
