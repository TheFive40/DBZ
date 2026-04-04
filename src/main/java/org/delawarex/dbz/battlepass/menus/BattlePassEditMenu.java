package org.delawarex.dbz.battlepass.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.delawarex.dbz.battlepass.manager.BattlePassManager;
import org.delawarex.dbz.battlepass.models.BattlePass;
import org.delawarex.dbz.customitems.events.ChatInput;
import org.delawarex.dbz.customitems.menus.Menu;
import org.delawarex.service.CC;

public class BattlePassEditMenu extends Menu {

    private final String passId;

    public BattlePassEditMenu(String passId) { this.passId = passId; }

    @Override
    protected String getTitle() { return "&c&l⭐ Editar Pase"; }

    @Override
    protected int getRows() { return 5; }

    @Override
    protected void buildContents(Player player) {
        fillBorder();
        BattlePassManager mgr = BattlePassManager.getInstance();
        BattlePass pass = mgr.getPass(passId);
        if (pass == null) { player.closeInventory(); return; }

        set(4, item(Material.PAPER,
                "&7ID: &f" + pass.getId(),
                "&7Nombre: &f" + CC.strip(pass.getDisplayName()),
                "&7Descripción: &f" + (pass.getDescription().isEmpty() ? "N/A" : pass.getDescription()),
                "&7Niveles: &f" + pass.getLevels().size(),
                "&7Permiso: &f" + (pass.getPermission().isEmpty() ? "Ninguno" : pass.getPermission()),
                "&7Estado: " + (pass.isEnabled() ? "&aActivo" : "&cDesactivado")));

        set(10, item(Material.NAME_TAG,
                        "&eRenombrar",
                        "&7Actual: &f" + CC.strip(pass.getDisplayName()),
                        "", "&a[CLICK]"),
                e -> ChatInput.await(player, "Nuevo nombre del pase (soporta &colores):", (p, txt) -> {
                    pass.setDisplayName(txt);
                    mgr.savePass(pass);
                    p.sendMessage(CC.translate("&a✓ Nombre actualizado."));
                    new BattlePassEditMenu(passId).open(p);
                }));

        set(11, item(Material.WRITABLE_BOOK,
                        "&bDescripción",
                        "&7Actual: &f" + (pass.getDescription().isEmpty() ? "N/A" : pass.getDescription()),
                        "", "&a[CLICK]"),
                e -> ChatInput.await(player, "Nueva descripción del pase:", (p, txt) -> {
                    pass.setDescription(txt);
                    mgr.savePass(pass);
                    p.sendMessage(CC.translate("&a✓ Descripción actualizada."));
                    new BattlePassEditMenu(passId).open(p);
                }));

        set(12, item(Material.TRIPWIRE_HOOK,
                        "&dPermiso de Acceso",
                        "&7Actual: &f" + (pass.getPermission().isEmpty() ? "Ninguno (todos)" : pass.getPermission()),
                        "&7Escribe 'ninguno' para acceso libre",
                        "", "&a[CLICK]"),
                e -> ChatInput.await(player, "Permiso requerido (o 'ninguno' para acceso libre):", (p, txt) -> {
                    String perm = txt.trim().equalsIgnoreCase("ninguno") || txt.trim().isEmpty() ? "" : txt.trim();
                    pass.setPermission(perm);
                    mgr.savePass(pass);
                    p.sendMessage(CC.translate("&a✓ Permiso " + (perm.isEmpty() ? "removido (acceso libre)." : "establecido: &f" + perm)));
                    new BattlePassEditMenu(passId).open(p);
                }));

        set(13, item(Material.ITEM_FRAME,
                        "&aÍtem Visual (Material)",
                        "&7Actual: &f" + pass.getMaterial(),
                        "&7Sostén el ítem en la mano y haz clic",
                        "", player.getInventory().getItemInMainHand().getType() != Material.AIR
                                ? "&a[CLICK para establecer]" : "&c[Sin ítem en mano]"),
                e -> {
                    Material hMat = player.getInventory().getItemInMainHand().getType();
                    if (hMat == Material.AIR) {
                        player.sendMessage(CC.translate("&cSostén un ítem en la mano."));
                        return;
                    }
                    pass.setMaterial(hMat.name());
                    mgr.savePass(pass);
                    player.sendMessage(CC.translate("&a✓ Material visual: &f" + hMat.name()));
                    new BattlePassEditMenu(passId).open(player);
                });

        set(14, item(pass.isEnabled() ? Material.LIME_DYE : Material.GRAY_DYE,
                        "&fEstado del Pase",
                        pass.isEnabled() ? "&a✔ ACTIVO &8[CLICK desactivar]" : "&c✘ DESACTIVADO &8[CLICK activar]"),
                e -> {
                    pass.setEnabled(!pass.isEnabled());
                    mgr.savePass(pass);
                    new BattlePassEditMenu(passId).open(player);
                });

        set(22, item(Material.ENDER_CHEST,
                        "&6&lGestionar Niveles",
                        "&7Niveles configurados: &f" + pass.getLevels().size(),
                        "&7Añade niveles, puntos y recompensas",
                        "", "&6[CLICK]"),
                e -> new BattlePassLevelListMenu(passId, 1).open(player));

        set(31, item(Material.TNT,
                        "&c&lEliminar Pase",
                        "&cEsta acción es irreversible",
                        "", "&c[CLICK]"),
                e -> {
                    mgr.deletePass(passId);
                    player.sendMessage(CC.translate("&c✗ Pase &f" + passId + " &celiminado."));
                    new BattlePassAdminMenu(1).open(player);
                });

        set(36, back(), e -> new BattlePassAdminMenu(1).open(player));
    }
}