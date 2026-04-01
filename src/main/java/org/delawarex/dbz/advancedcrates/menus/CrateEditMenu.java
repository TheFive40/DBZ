package org.delawarex.dbz.advancedcrates.menus;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.delawarex.dbz.DbzMain;
import org.delawarex.service.CC;
import org.delawarex.dbz.advancedcrates.managers.ChatInputManager;
import org.delawarex.dbz.advancedcrates.managers.CrateManager;
import org.delawarex.dbz.advancedcrates.models.Crate;
import org.delawarex.dbz.advancedcrates.models.Rarity;

public class CrateEditMenu extends Menu {

    private final String crateId;

    public CrateEditMenu(String crateId) { this.crateId = crateId; }

    @Override protected String getTitle() { return "&c&l\u2699 Editar Crate"; }
    @Override protected int getRows()     { return 5; }

    @Override
    protected void buildContents(Player player) {
        fillBorder(0);

        CrateManager mgr = DbzMain.get().getCrateManager();
        Crate crate = mgr.getCrate(crateId);
        if (crate == null) { player.closeInventory(); return; }

        set(4, item(Material.PAPER,
                "&7ID: &f" + crate.getId(),
                "&7Nombre: &f" + CC.strip(crate.getDisplayName()),
                "&7Rareza: " + crate.getRarity().getDisplay(),
                "&7Recompensas: &f" + crate.getRewards().size(),
                "&7Estado: " + (crate.isEnabled() ? "&aActiva" : "&cDesactivada"),
                "&7Loc. física: " + (crate.getPhysicalLocation() != null ? "&aSí" : "&cNo")));

        set(10, item(Material.NAME_TAG, "&eRenombrar",
                "&7Actual: &f" + CC.strip(crate.getDisplayName()), "", "&a[CLICK]"),
                e -> ChatInputManager.await(player, "Nuevo nombre (soporta &colores):", (p, txt) -> {
                    crate.setDisplayName(txt);
                    mgr.saveCrate(crate);
                    p.sendMessage(CC.translate("&a\u2713 Nombre actualizado."));
                    new CrateEditMenu(crateId).open(p);
                }));

        set(11, item(Material.WRITABLE_BOOK, "&bLore",
                "&7Líneas: &f" + crate.getLore().size(),
                "&7Escribe líneas separadas por |",
                "", "&a[CLICK]"),
                e -> ChatInputManager.await(player, "Lore (separa líneas con |):", (p, txt) -> {
                    crate.getLore().clear();
                    for (String line : txt.split("\\|")) crate.getLore().add(line.trim());
                    mgr.saveCrate(crate);
                    p.sendMessage(CC.translate("&a\u2713 Lore actualizado."));
                    new CrateEditMenu(crateId).open(p);
                }));

        set(12, item(crate.getRarity().getIcon(), "&5Rareza",
                "&7Actual: " + crate.getRarity().getDisplay(),
                "&7Disponibles: COMMON, RARE, EPIC, LEGENDARY, MYTHIC",
                "", "&a[CLICK]"),
                e -> ChatInputManager.await(player, "Nueva rareza (COMMON/RARE/EPIC/LEGENDARY/MYTHIC):", (p, txt) -> {
                    Rarity r = Rarity.fromString(txt.trim());
                    crate.setRarity(r);
                    mgr.saveCrate(crate);
                    p.sendMessage(CC.translate("&a\u2713 Rareza: " + r.getDisplay()));
                    new CrateEditMenu(crateId).open(p);
                }));

        ItemStack hand = player.getInventory().getItemInMainHand();
        set(13, item(Material.ITEM_FRAME, "&dÍtem Visual",
                crate.getVisualItem() != null ? "&7Material: &f" + crate.getVisualItem().getType().name() : "&7Sin configurar",
                "&7Sostén el ítem en la mano",
                "", hand.getType() != Material.AIR ? "&a[CLICK para establecer]" : "&c[Sin ítem en mano]"),
                e -> {
                    ItemStack h = player.getInventory().getItemInMainHand();
                    if (h.getType() == Material.AIR) {
                        player.sendMessage(CC.translate("&c\u2717 Sostén un ítem en la mano."));
                        return;
                    }
                    crate.setVisualItem(h.clone());
                    crate.setMaterial(h.getType().name());
                    mgr.saveCrate(crate);
                    player.sendMessage(CC.translate("&a\u2713 Ítem visual establecido: &f" + h.getType().name()));
                    new CrateEditMenu(crateId).open(player);
                });

        set(14, item(Material.TRIPWIRE_HOOK, "&eLlave",
                "&7ID: &f" + crate.getKeyId(),
                "&7Nombre: &f" + CC.strip(crate.getKeyDisplayName()),
                "&7Material: &f" + crate.getKeyMaterial(),
                "", "&a[CLICK]"),
                e -> new CrateKeyEditMenu(crateId).open(player));

        set(15, item(Material.ENDER_CHEST, "&aRecompensas",
                "&7Total: &f" + crate.getRewards().size(),
                "", "&a[CLICK para gestionar]"),
                e -> new CrateRewardListMenu(crateId, 1).open(player));

        Location loc = crate.getPhysicalLocation();
        set(19, item(loc != null ? Material.FILLED_MAP : Material.MAP, "&3Localización Física",
                loc != null ? "&7Mundo: &f" + loc.getWorld().getName() : "&7Sin configurar",
                loc != null ? "&7X,Y,Z: &f" + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ() : "",
                "&7Establece tu posición como ubicación",
                "", "&a[CLICK para establecer] &c[SHIFT para quitar]"),
                e -> {
                    if (e.isShiftClick()) {
                        crate.setPhysicalLocation(null);
                        mgr.saveCrate(crate);
                        player.sendMessage(CC.translate("&a\u2713 Localización eliminada."));
                    } else {
                        crate.setPhysicalLocation(player.getLocation().clone());
                        mgr.saveCrate(crate);
                        player.sendMessage(CC.translate("&a\u2713 Localización establecida."));
                    }
                    new CrateEditMenu(crateId).open(player);
                });

        set(20, item(crate.isEnabled() ? Material.LIME_DYE : Material.GRAY_DYE,
                "&fEstado",
                crate.isEnabled() ? "&a\u2714 ACTIVA &8[CLICK desactivar]" : "&c\u2718 DESACTIVADA &8[CLICK activar]"),
                e -> {
                    crate.setEnabled(!crate.isEnabled());
                    mgr.saveCrate(crate);
                    new CrateEditMenu(crateId).open(player);
                });

        set(24, item(Material.CHEST, "&a\u25BA Dar Ítem a mí",
                "&7Recibe el ítem visual de la crate",
                "", "&a[CLICK]"),
                e -> {
                    ItemStack di = mgr.buildCrateDisplayItem(crate);
                    if (player.getInventory().firstEmpty() == -1)
                        player.getWorld().dropItem(player.getLocation(), di);
                    else
                        player.getInventory().addItem(di);
                    player.sendMessage(CC.translate("&a\u2713 Ítem de crate entregado."));
                });

        set(33, item(Material.TNT, "&c&lEliminar Crate",
                "&cIrreversible", "", "&c[CLICK]"),
                e -> {
                    mgr.deleteCrate(crateId);
                    player.sendMessage(CC.translate("&c\u2717 Crate &f" + crateId + " &celiminada."));
                    new CrateAdminMenu(1).open(player);
                });

        set(36, back(), e -> new CrateAdminMenu(1).open(player));
    }
}
