package memory520.polymerizedcrystal;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class PolymerizedCrystal extends JavaPlugin implements Listener {

    private GuiManager GuiManager;
    private ItemPolymerized itemPolymerized;

    @Override
    public void onEnable() {
        // Plugin startup logic
        System.out.println("# +----------------------------+");
        System.out.println("# |");
        System.out.println("# |   §bYX-PolymerizedCrystal");
        System.out.println("# |   #载入成功");
        System.out.println("# |");
        System.out.println("# |   载入前置插件");
        if(Bukkit.getPluginManager().isPluginEnabled("MythicMobs")){
            System.out.println("# |   | "+Bukkit.getPluginManager().getPlugin("MythicMobs")+" §6完成");
        }else{
            System.out.println("# |   | MythicMobs §c失败");
        }
        System.out.println("# |");
        System.out.println("# |   | §eby.Memory520");
        System.out.println("# |   | §eQQ:3332397782");
        System.out.println("# |");
        System.out.println("# +-------");
        System.out.println("");

        GuiManager = new GuiManager();
        Bukkit.getServer().getPluginManager().registerEvents(GuiManager, this);

        load();

        super.onEnable();
    }

    public void load(){
        saveDefaultConfig();
        reloadConfig();
        GuiManager.Itemlist(getConfig());
        FileConfiguration config = getConfig();

        //关闭玩家的界面
        for (Player player : Bukkit.getOnlinePlayers()) {
            InventoryView view = player.getOpenInventory();
            if (view == null) {
                continue;
            }
            Inventory inventory = view.getTopInventory();
            if (inventory == null) {
                continue;
            }
            if (inventory.getHolder() instanceof memory520.polymerizedcrystal.GuiManager.PolymerizedCrystalInv) {
                player.closeInventory();
            }
        }
    }

    //返还GUI里的物品给玩家
    public void DisableItemReturn(){
        for (Player player : Bukkit.getOnlinePlayers()) {
            InventoryView view = player.getOpenInventory();
            if (view == null) {
                continue;
            }
            Inventory inventory = view.getTopInventory();
            if (inventory == null) {
                continue;
            }
            if (inventory.getHolder() instanceof memory520.polymerizedcrystal.GuiManager.PolymerizedCrystalInv) {

                ItemStack itemStack;

                for (int i = 0; i < 5; i++) {
                    if(i==2 || i==3){continue;}
                    itemStack = inventory.getItem(i);
                    if (!inventory.getItem(i).equals(GuiManager.Slot[i])) {
                        player.getInventory().addItem(itemStack);
                    }
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("open")) {
            if (sender instanceof Player) {
                if (!sender.hasPermission("pc.open.use")) {
                    sender.sendMessage("§c你没有这个权限!");
                    return true;
                }
                GuiManager.Invopen(((Player) sender));
                return true;
            }else {
                sender.sendMessage("§c只有玩家能使用这个命令!");
                return true;
            }
        }else if(args.length > 0 && args[0].equalsIgnoreCase("reload")){
            if (!sender.hasPermission("pc.reload.admin")) {
                sender.sendMessage("§c你没有这个权限!");
                return true;
            }
            load();
            sender.sendMessage("§aYX-PolymerizedCrystal 重载完成!");
            return true;
        }else{
            sender.sendMessage("§9§l[YX-PolymerizedCrystal] §7->");
            sender.sendMessage(" > §7/pc open §9聚合界面");
            sender.sendMessage(" > §7/pc reload §9重载插件");
            return true;
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        //在插件卸载时返还GUI的物品给玩家
        DisableItemReturn();

        Bukkit.getScheduler().cancelTasks(this);
        super.onDisable();
    }
}
