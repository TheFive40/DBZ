package org.delawarex.dbz.battlepass.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.delawarex.dbz.DbzMain;
import org.delawarex.dbz.battlepass.manager.BattlePassManager;
import org.delawarex.dbz.battlepass.models.BattlePass;
import org.delawarex.dbz.battlepass.models.BattlePassLevel;
import org.delawarex.dbz.customitems.events.ChatInput;
import org.delawarex.dbz.customitems.menus.Menu;
import org.delawarex.service.CC;

import java.util.ArrayList;
import java.util.List;

public class BattlePassLevelEditMenu extends Menu {

    private final String passId;
    private final int levelIndex;

    public BattlePassLevelEditMenu(String passId, int levelIndex) {
        this.passId = passId;
        this.levelIndex = levelIndex;
    }

    @Override
    protected String getTitle() { return "&6&l⭐ Editar Nivel"; }

    @Override
    protected int getRows() { return 6; }

    @Override
    protected void buildContents(Player player) {
        fillBorder();
        BattlePassManager mgr = BattlePassManager.getInstance();
        BattlePass pass = mgr.getPass(passId);
        if (pass == null || levelIndex < 0 || levelIndex >= pass.getLevels().size()) {
            player.closeInventory();
            return;
        }
        BattlePassLevel level = pass.getLevels().get(levelIndex);

        set(4, item(Material.GOLD_INGOT,
                "&e&lNivel " + level.getLevelNumber() + ": " + CC.strip(level.getDisplayName()),
                "&7Puntos requeridos: &f" + level.getRequiredPoints(),
                "&7Ítems de recompensa: &f" + level.getItems().size(),
                "&7Comandos de recompensa: &f" + level.getCommands().size()));

        set(10, item(Material.NAME_TAG,
                        "&eRenombrar Nivel",
                        "&7Actual: &f" + CC.strip(level.getDisplayName()),
                        "", "&a[CLICK]"),
                e -> ChatInput.await(player, "Nuevo nombre para el nivel (soporta &colores):", (p, txt) -> {
                    level.setDisplayName(txt);
                    mgr.savePass(pass);
                    p.sendMessage(CC.translate("&a✓ Nombre del nivel actualizado."));
                    new BattlePassLevelEditMenu(passId, levelIndex).open(p);
                }));

        set(11, item(Material.EXPERIENCE_BOTTLE,
                        "&aPuntos Requeridos",
                        "&7Actual: &f" + level.getRequiredPoints() + " puntos",
                        "&7Puntos necesarios para desbloquear",
                        "", "&a[CLICK]"),
                e -> ChatInput.await(player, "Puntos requeridos para este nivel (ej: 500):", (p, txt) -> {
                    try {
                        int pts = Integer.parseInt(txt.trim());
                        if (pts < 0) throw new NumberFormatException();
                        level.setRequiredPoints(pts);
                        mgr.savePass(pass);
                        p.sendMessage(CC.translate("&a✓ Puntos requeridos: &f" + pts));
                    } catch (NumberFormatException ex) {
                        p.sendMessage(CC.translate("&cNúmero inválido (debe ser ≥ 0)."));
                    }
                    new BattlePassLevelEditMenu(passId, levelIndex).open(p);
                }));

        set(12, item(Material.CHEST,
                        "&bAgregar Ítem a Recompensa",
                        "&7Sostén el ítem en la mano",
                        "&7Se añadirá como recompensa del nivel",
                        "",
                        player.getInventory().getItemInMainHand().getType() != Material.AIR
                                ? "&a[CLICK para agregar]" : "&c[Sostén un ítem en mano]"),
                e -> {
                    ItemStack hand = player.getInventory().getItemInMainHand();
                    if (hand.getType() == Material.AIR) {
                        player.sendMessage(CC.translate("&cSostén un ítem en la mano primero."));
                        return;
                    }
                    level.getItems().add(hand.clone());
                    mgr.savePass(pass);
                    player.sendMessage(CC.translate("&a✓ Ítem agregado: &f" + hand.getType().name()
                            + (hand.getAmount() > 1 ? " x" + hand.getAmount() : "")));
                    new BattlePassLevelEditMenu(passId, levelIndex).open(player);
                });

        set(13, item(Material.COMMAND_BLOCK,
                        "&dAgregar Comando a Recompensa",
                        "&7@p y {player} = nombre del jugador",
                        "&7Sin incluir /",
                        "", "&a[CLICK]"),
                e -> ChatInput.await(player, "Comando a ejecutar al reclamar (sin /) — @p = jugador:", (p, txt) -> {
                    String cmd = txt.startsWith("/") ? txt.substring(1) : txt;
                    if (cmd.trim().isEmpty()) {
                        p.sendMessage(CC.translate("&cComando inválido."));
                        new BattlePassLevelEditMenu(passId, levelIndex).open(p);
                        return;
                    }
                    level.getCommands().add(cmd.trim());
                    mgr.savePass(pass);
                    p.sendMessage(CC.translate("&a✓ Comando agregado: &f/" + cmd.trim()));
                    new BattlePassLevelEditMenu(passId, levelIndex).open(p);
                }));

        set(14, item(Material.BARRIER,
                        "&cLimpiar Todo",
                        "&7Elimina todos los ítems y comandos",
                        "&cIrreversible", "", "&c[CLICK]"),
                e -> {
                    level.getItems().clear();
                    level.getCommands().clear();
                    mgr.savePass(pass);
                    player.sendMessage(CC.translate("&c✗ Todas las recompensas del nivel eliminadas."));
                    new BattlePassLevelEditMenu(passId, levelIndex).open(player);
                });

        List<String> itemsHeader = new ArrayList<>();
        itemsHeader.add(CC.translate("&b— Ítems de Recompensa (" + level.getItems().size() + ") —"));
        set(18, item(Material.CHEST, "&b&lÍtems de Recompensa",
                "&7Total: &f" + level.getItems().size(),
                "&7Clic en un ítem para eliminarlo"));

        int itemDisplaySlot = 19;
        for (int i = 0; i < Math.min(level.getItems().size(), 7); i++) {
            final int idx = i;
            ItemStack rewardItem = level.getItems().get(i);
            if (rewardItem == null) continue;
            ItemStack display = rewardItem.clone();
            ItemMeta dMeta = display.hasItemMeta() ? display.getItemMeta()
                    : DbzMain.instance.getServer().getItemFactory().getItemMeta(display.getType());
            if (dMeta != null) {
                List<String> lore = dMeta.hasLore() && dMeta.getLore() != null
                        ? new ArrayList<>(dMeta.getLore()) : new ArrayList<>();
                lore.add("");
                lore.add(CC.translate("&c[CLICK] Eliminar ítem"));
                dMeta.setLore(lore);
                display.setItemMeta(dMeta);
            }
            set(itemDisplaySlot + i, display, e -> {
                if (idx < level.getItems().size()) {
                    level.getItems().remove(idx);
                    mgr.savePass(pass);
                    player.sendMessage(CC.translate("&c✗ Ítem eliminado de la recompensa."));
                }
                new BattlePassLevelEditMenu(passId, levelIndex).open(player);
            });
        }
        if (level.getItems().size() > 7) {
            set(26, item(Material.PAPER, "&7+" + (level.getItems().size() - 7) + " ítems más",
                    "&7Solo se muestran los primeros 7"));
        }

        set(27, item(Material.COMMAND_BLOCK, "&d&lComandos de Recompensa",
                "&7Total: &f" + level.getCommands().size(),
                "&7Clic en un comando para eliminarlo"));

        int cmdDisplaySlot = 28;
        for (int i = 0; i < Math.min(level.getCommands().size(), 7); i++) {
            final int idx = i;
            String cmd = level.getCommands().get(i);
            String display = cmd.length() > 28 ? cmd.substring(0, 28) + "..." : cmd;
            set(cmdDisplaySlot + i, item(Material.PAPER,
                            "&f/" + display,
                            "&7Cmd completo: &f/" + cmd,
                            "", "&c[CLICK] Eliminar comando"),
                    e -> {
                        if (idx < level.getCommands().size()) {
                            level.getCommands().remove(idx);
                            mgr.savePass(pass);
                            player.sendMessage(CC.translate("&c✗ Comando eliminado."));
                        }
                        new BattlePassLevelEditMenu(passId, levelIndex).open(player);
                    });
        }
        if (level.getCommands().size() > 7) {
            set(35, item(Material.PAPER, "&7+" + (level.getCommands().size() - 7) + " comandos más",
                    "&7Solo se muestran los primeros 7"));
        }

        set(45, back(), e -> new BattlePassLevelListMenu(passId, 1).open(player));
    }
}