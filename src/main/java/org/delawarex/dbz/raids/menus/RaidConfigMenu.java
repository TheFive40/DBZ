package org.delawarex.dbz.raids.menus;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.delawarex.dbz.customitems.menus.Menu;
import org.delawarex.dbz.raids.managers.RaidChatInputManager;
import org.delawarex.dbz.raids.managers.RaidManager;
import org.delawarex.dbz.raids.models.Raid;
import org.delawarex.service.CC;

public class RaidConfigMenu extends Menu {

    private final String raidId;

    public RaidConfigMenu(String raidId) { this.raidId = raidId; }

    @Override
    protected String getTitle() { return "&6&lConfigurar Raid"; }

    @Override
    protected int getRows() { return 4; }

    @Override
    protected void buildContents(Player player) {
        fillBorder();
        Raid raid = RaidManager.getInstance().getById(raidId);
        if (raid == null) { player.closeInventory(); return; }

        set(4, item(Material.PAPER,
                "&6&l" + raid.getRaidName(),
                "&7ID: &f" + raid.getRaidId(),
                "&7Oleadas: &f" + raid.getTotalWaves(),
                "&7Config: " + (raid.isConfigured() ? "&a✓ Lista" : "&c✗ Incompleta"),
                "&7Estado: " + (raid.isEnabled() ? "&aActiva" : "&cDesactivada")));

        set(10, item(Material.NAME_TAG,
                        "&eRenombrar",
                        "&7Actual: &f" + raid.getRaidName(),
                        "", "&a[CLICK]"),
                e -> {
                    player.closeInventory();
                    RaidChatInputManager.startRenameInput(player, raidId);
                });

        set(11, item(Material.BOOK,
                        "&bDescripción",
                        "&7Actual: &f" + (raid.getDescription().isEmpty() ? "Sin descripción" : raid.getDescription()),
                        "", "&a[CLICK]"),
                e -> {
                    player.closeInventory();
                    RaidChatInputManager.startDescriptionInput(player, raidId);
                });

        set(12, item(Material.CLOCK,
                        "&3Cooldown",
                        "&7Actual: &f" + (raid.getCooldownSeconds() / 60) + " minutos",
                        "", "&a[CLICK]"),
                e -> {
                    player.closeInventory();
                    RaidChatInputManager.startCooldownInput(player, raidId);
                });

        set(13, item(Material.PLAYER_HEAD,
                        "&bJugadores",
                        "&7Mín: &f" + raid.getMinPlayers(),
                        "&7Máx: &f" + raid.getMaxPlayers(),
                        "", "&a[CLICK]"),
                e -> {
                    player.closeInventory();
                    RaidChatInputManager.startPlayersInput(player, raidId);
                });

        Location pSpawn = raid.getPlayerSpawnPoint();
        set(14, item(pSpawn != null ? Material.RED_BED : Material.BARRIER,
                        "&aPlayer Spawn",
                        pSpawn != null
                                ? "&7Pos: &f" + pSpawn.getBlockX() + ", " + pSpawn.getBlockY() + ", " + pSpawn.getBlockZ()
                                : "&c✗ Sin configurar",
                        "&7(Se usa tu posición actual)",
                        "", "&a[CLICK para establecer]"),
                e -> {
                    raid.setPlayerSpawnPoint(player.getLocation().clone());
                    RaidManager.getInstance().saveRaid(raid);
                    player.sendMessage(CC.translate("&a✓ Player spawn establecido."));
                    new RaidConfigMenu(raidId).open(player);
                });

        set(15, item(Material.BLAZE_POWDER,
                        "&6Oleadas",
                        "&7Configuradas: &f" + raid.getTotalWaves(),
                        "", "&6[CLICK]"),
                e -> new RaidWavesMenu(raidId).open(player));

        boolean enabled = raid.isEnabled();
        set(19, item(enabled ? Material.LIME_DYE : Material.GRAY_DYE,
                        enabled ? "&a&lActiva" : "&c&lDesactivada",
                        enabled ? "&7La raid está disponible" : "&7La raid está oculta",
                        "", "&e[CLICK para cambiar]"),
                e -> {
                    raid.setEnabled(!raid.isEnabled());
                    RaidManager.getInstance().saveRaid(raid);
                    new RaidConfigMenu(raidId).open(player);
                });

        set(25, item(Material.TNT,
                        "&c&lEliminar Raid",
                        "&cEsta acción es irreversible",
                        "", "&c[CLICK]"),
                e -> {
                    RaidManager.getInstance().deleteRaid(raidId);
                    player.sendMessage(CC.translate("&c✗ Raid &f" + raid.getRaidName() + " &celiminada."));
                    new RaidListMenu(1).open(player);
                });

        set(27, back(), e -> new RaidListMenu(1).open(player));
    }
}