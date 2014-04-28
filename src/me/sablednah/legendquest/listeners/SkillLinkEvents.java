package me.sablednah.legendquest.listeners;

import me.sablednah.legendquest.Main;
import me.sablednah.legendquest.playercharacters.PC;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class SkillLinkEvents implements Listener {

    public Main lq;

    public SkillLinkEvents(final Main p) {
        this.lq = p;
    }

    // catch "use" actions
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onInteract(final PlayerInteractEvent event) {
        final Player p = event.getPlayer();
        final PC pc = lq.players.getPC(p);
        final Material itemUsed = p.getItemInHand().getType();
        final Action act = event.getAction();
        if (itemUsed != null) {
            if (act == Action.RIGHT_CLICK_AIR || act == Action.RIGHT_CLICK_BLOCK) {
                String linkedSkill = pc.getLink(itemUsed);
                if (linkedSkill!=null) {
                	pc.useSkill(linkedSkill);
                }
            }
        }
    }
}