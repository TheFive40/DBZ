package org.delawarex.dbz.advancedcrates.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.delawarex.dbz.DbzMain;
import org.delawarex.service.CC;
import org.delawarex.dbz.advancedcrates.managers.ChatInputManager;
import org.delawarex.dbz.advancedcrates.managers.CrateManager;
import org.delawarex.dbz.advancedcrates.models.Crate;

public class CrateKeyEditMenu extends Menu {

    private final String crateId;

    public CrateKeyEditMenu(String crateId) { this.crateId = crateId; }

    @Override protected String getTitle() { return "&e&l\uD83D\uDDDD Configurar Llave"; }
    @Override protected int getRows()     { return 3; }

    @Override
    protected void buildContents(Player player) {
        fillBorder(3);

        CrateManager mgr = DbzMain.get().getCrateManager();
        Crate crate = mgr.getCrate(crateId);
        if (crate == null) { player.closeInventory(); return; }

        set(11, item(Material.NAME_TAG, "&eNombre de la Llave",
                "&7Actual: &f" + CC.strip(crate.getKeyDisplayName()),
                "&7Soporta &ccolores &7con &",
                "", "&a[CLICK]"),
                e -> ChatInputManager.await(player, "Nuevo nombre de la llave (soporta &ccolores):", (p, txt) -> {
                    crate.setKeyDisplayName(txt);
                    mgr.saveCrate(crate);
                    p.sendMessage(CC.translate("&a\u2713 Nombre de llave actualizado."));
                    new CrateKeyEditMenu(crateId).open(p);
                }));

        Material keyMat;
        try { keyMat = Material.valueOf(crate.getKeyMaterial().toUpperCase()); }
        catch (Exception ex) { keyMat = Material.TRIPWIRE_HOOK; }

        set(13, item(keyMat, "&bMaterial de la Llave",
                "&7Actual: &f" + crate.getKeyMaterial(),
                "&7Usa nombres de Material de Bukkit",
                "", "&a[CLICK]"),
                e -> ChatInputManager.await(player, "Material (ej: TRIPWIRE_HOOK, GOLDEN_KEY...):", (p, txt) -> {
                    try {
                        Material.valueOf(txt.trim().toUpperCase());
                        crate.setKeyMaterial(txt.trim().toUpperCase());
                        mgr.saveCrate(crate);
                        p.sendMessage(CC.translate("&a\u2713 Material actualizado: &f" + txt.trim().toUpperCase()));
                    } catch (IllegalArgumentException ex) {
                        p.sendMessage(CC.translate("&c\u2717 Material inválido: &f" + txt));
                    }
                    new CrateKeyEditMenu(crateId).open(p);
                }));

        set(15, item(Material.TRIPWIRE_HOOK, "&aPrevisualizar Llave",
                "&7Ver cómo quedará la llave",
                "", "&a[CLICK para recibir]"),
                e -> {
                    if (player.getInventory().firstEmpty() == -1) {
                        player.sendMessage(CC.translate("&c\u2717 Inventario lleno."));
                        return;
                    }
                    player.getInventory().addItem(mgr.buildKeyItem(crate));
                    player.sendMessage(CC.translate("&a\u2713 Llave de previsualización entregada."));
                });

        set(18, back(), e -> new CrateEditMenu(crateId).open(player));
    }
}
