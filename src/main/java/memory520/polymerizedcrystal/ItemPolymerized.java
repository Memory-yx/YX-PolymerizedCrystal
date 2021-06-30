package memory520.polymerizedcrystal;

import io.lumine.xikage.mythicmobs.MythicMobs;
import jdk.nashorn.internal.runtime.regexp.joni.Config;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.units.qual.C;

import java.util.List;
import java.util.Random;

public class ItemPolymerized implements Listener {

    private List<String> lore;
    private String CrystalStartLore;
    private String CrystalEndLore;
    private ItemStack[] Slot = new ItemStack[5];
    int CrystalSuccessrate;
    int PowderSuccessrate;

    public void LoreSet(ItemStack itemStack, String CrystalID, Configuration Config, Player player,ItemStack[] slot,InventoryClickEvent e){
        //检测物品是否正确
        if(CrystalID != null){

            //判断水晶聚合几率
            if(!e.getInventory().getItem(0).equals(Slot[0])){
                ItemStack CSStack = e.getInventory().getItem(0);
                for (int i=0;i<CSStack.getItemMeta().getLore().size();i++) {
                    if(CSStack.getItemMeta().getLore().get(i).contains(Config.getString("CrystalSuccessrate").replace("&", "§"))){
                        CrystalSuccessrate = Integer.valueOf(CSStack.getItemMeta().getLore().get(i)
                                .replace(Config.getString("CrystalSuccessrate").replace("&", "§"),"")
                                .replace("%",""));
                        break;
                    }else{
                        CrystalSuccessrate = 0;
                    }
                }
            }
            //判断粉末聚合加成几率
            if(!e.getInventory().getItem(1).equals(Slot[1])){
                ItemStack PSStack = e.getInventory().getItem(1);
                for (int i=1;i<PSStack.getItemMeta().getLore().size();i++) {
                    if(PSStack.getItemMeta().getLore().get(i).contains(Config.getString("PowderSuccessrate").replace("&", "§"))){
                        PowderSuccessrate = Integer.valueOf(PSStack.getItemMeta().getLore().get(i)
                                .replace(Config.getString("PowderSuccessrate").replace("&", "§"),"")
                                .replace("%",""));
                        break;
                    }else{
                        PowderSuccessrate = 0;
                    }
                }
            }

            //设置变量
            lore = itemStack.getItemMeta().getLore();
            CrystalStartLore = Config.getString("item."+CrystalID+".startLore").replace("&", "§");
            CrystalEndLore = Config.getString("item."+CrystalID+".endLore").replace("&", "§");

            //声明一个下标变量
            int index = -1;
            //遍历物品上是否存在聚合该水晶的lore
            for (int i=0;i<lore.size();i++){
                if(lore.get(i).equals(CrystalStartLore)){
                    index = i;
                    break;
                }
            }
            //检查下标是否正确
            if (index < 0) {
                player.sendRawMessage(Config.getString("MessagePrefix").replace("&", "§")+"§7该物品没有对应水晶的插槽!");
                sound(player,Config.getString("sound.PolymerizationError"));
                return;
            }

            //消耗物品的检查
            if(Config.getBoolean("item."+CrystalID+".upgradeItem.upgradeOpen")){
                ItemStack upgradeItem = MythicMobs.inst().getItemManager().getItemStack(Config.getString("item."+CrystalID+".upgradeItem.upgradeName"));
                upgradeItem.setAmount(Integer.valueOf(Config.getString("item."+ CrystalID +".upgradeItem.upgradeAmount")));
                if(player.getInventory().containsAtLeast(upgradeItem,upgradeItem.getAmount())){
                    player.getInventory().removeItem(upgradeItem);
                }else{
                    player.sendRawMessage(Config.getString("MessagePrefix").replace("&", "§")+"§7你没有聚合所需要的物品数量!");
                    sound(player,Config.getString("sound.PolymerizationError"));
                    return;
                }
            }

            //判断聚合是否成功,如果失败就返回
            Random r = new Random();
            if(r.nextInt(100)+1>CrystalSuccessrate+PowderSuccessrate){
                Slot = slot;
                Itemtake(e);
                player.sendRawMessage(Config.getString("MessagePrefix").replace("&", "§")+"§7水晶聚合§c失败§7!");
                sound(player,Config.getString("sound.PolymerizationFail"));
                return;
            }

            player.sendRawMessage(Config.getString("MessagePrefix").replace("&", "§")+"§7水晶聚合§b成功§7!");
            sound(player,Config.getString("sound.PolymerizationSuccess"));

            ItemMeta meta = itemStack.getItemMeta();
            //设置内部储存的lore的插槽行数为水晶lore
            lore.set(index++,CrystalEndLore);
            meta.setLore(lore);
            itemStack.setItemMeta(meta);
            //设置Item类中Slot的物品类型
            Slot = slot;
            //使用Itemtake方法
            Itemtake(e);
        }
    }

    //当聚合成功开始后,减少玩家的物品数量
    public void Itemtake(InventoryClickEvent e){
        //减少水晶槽的物品
        if(e.getInventory().getItem(0).getAmount() > 1){
            int ItemAmount = e.getInventory().getItem(0).getAmount()-1;
            e.getInventory().getItem(0).setAmount(ItemAmount);
        }else{
            e.getInventory().setItem(0,Slot[0]);
        }

        //减少粉末槽的物品
        if(e.getInventory().getItem(1).getAmount() > 1){
            int ItemAmount = e.getInventory().getItem(1).getAmount()-1;
            e.getInventory().getItem(1).setAmount(ItemAmount);
        }else{
            e.getInventory().setItem(1,Slot[1]);
        }
    }

    //播放音效
    public void sound(Player player,String sound){
        player.playSound(player.getLocation(), Sound.valueOf(sound), SoundCategory.PLAYERS, 1, 1);
    }
}
