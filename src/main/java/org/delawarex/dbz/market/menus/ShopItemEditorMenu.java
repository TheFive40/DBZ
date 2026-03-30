package org.delawarex.dbz.market.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.delawarex.dbz.customitems.events.ChatInput;
import org.delawarex.dbz.customitems.menus.Menu;
import org.delawarex.dbz.market.ShopManager;
import org.delawarex.dbz.market.model.MarketItem;
import org.delawarex.service.CC;

public class ShopItemEditorMenu extends Menu {

    private final String itemId;

    public ShopItemEditorMenu(String itemId) { this.itemId = itemId; }

    @Override protected String getTitle() { return "&c&lEditor: &f"; }
    @Override protected int getRows()     { return 5; }

    @Override
    protected void buildContents(Player player) {
        fillBorder();
        ShopManager mgr = ShopManager.getInstance();
        MarketItem  mi  = mgr.getItem(itemId);
        if (mi == null) { player.closeInventory(); return; }

        String sym = mgr.getConfig().currencySymbol;
        double buy  = mgr.getPriceEngine().getBuyPrice(mi);
        double sell = mgr.getPriceEngine().getSellPrice(mi);

        set(4, item(Material.PAPER,
                "&7ID: &f" + mi.getId(),
                "&7Material: &f" + mi.getMaterial(),
                "&7Nombre: &f" + mi.getDisplayName(),
                "",
                "&7Compra actual: &c" + String.format("%.2f", buy) + " " + sym,
                "&7Venta actual:  &a" + String.format("%.2f", sell) + " " + sym,
                "&7Stock: &f" + mi.getStock() + " / " + mi.getTargetStock(),
                "&7Tendencia: " + mi.getTrend().getDisplay()));

        set(10, item(Material.GOLD_INGOT,
                        "&6Precio Base",
                        "&7Actual: &f" + mi.getBasePrice() + " " + sym,
                        "", "&a[CLICK]"),
                e -> ChatInput.await(player, "Nuevo precio base:", (p, txt) -> {
                    try {
                        double val = Double.parseDouble(txt.trim());
                        if (val <= 0) throw new NumberFormatException();
                        mi.setBasePrice(val);
                        mgr.saveItem(mi);
                        p.sendMessage(CC.translate("&a✓ Precio base: &f" + val));
                    } catch (NumberFormatException ex) {
                        p.sendMessage(CC.translate("&cValor inválido."));
                    }
                    new ShopItemEditorMenu(itemId).open(p);
                }));

        set(11, item(Material.RED_STAINED_GLASS,
                        "&cPrecio Mínimo",
                        "&7Actual: &f" + mi.getMinPrice() + " " + sym,
                        "", "&a[CLICK]"),
                e -> ChatInput.await(player, "Nuevo precio mínimo:", (p, txt) -> {
                    try {
                        double val = Double.parseDouble(txt.trim());
                        if (val <= 0) throw new NumberFormatException();
                        mi.setMinPrice(val);
                        mgr.saveItem(mi);
                        p.sendMessage(CC.translate("&a✓ Precio mínimo: &f" + val));
                    } catch (NumberFormatException ex) {
                        p.sendMessage(CC.translate("&cValor inválido."));
                    }
                    new ShopItemEditorMenu(itemId).open(p);
                }));

        set(12, item(Material.LIME_STAINED_GLASS,
                        "&aPrecio Máximo",
                        "&7Actual: &f" + mi.getMaxPrice() + " " + sym,
                        "", "&a[CLICK]"),
                e -> ChatInput.await(player, "Nuevo precio máximo:", (p, txt) -> {
                    try {
                        double val = Double.parseDouble(txt.trim());
                        if (val <= 0) throw new NumberFormatException();
                        mi.setMaxPrice(val);
                        mgr.saveItem(mi);
                        p.sendMessage(CC.translate("&a✓ Precio máximo: &f" + val));
                    } catch (NumberFormatException ex) {
                        p.sendMessage(CC.translate("&cValor inválido."));
                    }
                    new ShopItemEditorMenu(itemId).open(p);
                }));

        set(13, item(Material.CHEST,
                        "&bStock Actual",
                        "&7Actual: &f" + mi.getStock(),
                        "", "&a[CLICK para cambiar]"),
                e -> ChatInput.await(player, "Nuevo stock:", (p, txt) -> {
                    try {
                        int val = Integer.parseInt(txt.trim());
                        if (val < 0) throw new NumberFormatException();
                        mi.setStock(val);
                        mgr.saveItem(mi);
                        p.sendMessage(CC.translate("&a✓ Stock: &f" + val));
                    } catch (NumberFormatException ex) {
                        p.sendMessage(CC.translate("&cValor inválido."));
                    }
                    new ShopItemEditorMenu(itemId).open(p);
                }));

        set(14, item(Material.HOPPER,
                        "&3Stock Objetivo",
                        "&7Actual: &f" + mi.getTargetStock(),
                        "&7Afecta al cálculo de precio.",
                        "", "&a[CLICK]"),
                e -> ChatInput.await(player, "Nuevo stock objetivo:", (p, txt) -> {
                    try {
                        int val = Integer.parseInt(txt.trim());
                        if (val <= 0) throw new NumberFormatException();
                        mi.setTargetStock(val);
                        mgr.saveItem(mi);
                        p.sendMessage(CC.translate("&a✓ Stock objetivo: &f" + val));
                    } catch (NumberFormatException ex) {
                        p.sendMessage(CC.translate("&cValor inválido."));
                    }
                    new ShopItemEditorMenu(itemId).open(p);
                }));

        set(19, item(Material.EMERALD,
                        "&aTasa de Impuesto",
                        "&7Actual: &f" + String.format("%.0f", mi.getTaxRate() * 100) + "%",
                        "&7Valor entre 0.0 y 1.0",
                        "", "&a[CLICK]"),
                e -> ChatInput.await(player, "Tasa de impuesto (0.0-1.0):", (p, txt) -> {
                    try {
                        double val = Double.parseDouble(txt.trim());
                        if (val < 0 || val > 1) throw new NumberFormatException();
                        mi.setTaxRate(val);
                        mgr.saveItem(mi);
                        p.sendMessage(CC.translate("&a✓ Impuesto: &f" + String.format("%.0f", val * 100) + "%"));
                    } catch (NumberFormatException ex) {
                        p.sendMessage(CC.translate("&cValor inválido (0.0 - 1.0)."));
                    }
                    new ShopItemEditorMenu(itemId).open(p);
                }));

        set(20, item(Material.COMPARATOR,
                        "&eSpread Compra/Venta",
                        "&7Actual: &f" + String.format("%.0f", mi.getSpread() * 100) + "%",
                        "&7Diferencia entre compra y venta.",
                        "&7Valor entre 0.0 y 0.9",
                        "", "&a[CLICK]"),
                e -> ChatInput.await(player, "Spread (0.0-0.9):", (p, txt) -> {
                    try {
                        double val = Double.parseDouble(txt.trim());
                        if (val < 0 || val > 0.9) throw new NumberFormatException();
                        mi.setSpread(val);
                        mgr.saveItem(mi);
                        p.sendMessage(CC.translate("&a✓ Spread: &f" + String.format("%.0f", val * 100) + "%"));
                    } catch (NumberFormatException ex) {
                        p.sendMessage(CC.translate("&cValor inválido (0.0 - 0.9)."));
                    }
                    new ShopItemEditorMenu(itemId).open(p);
                }));

        set(21, item(Material.REDSTONE,
                        "&cSensibilidad Volumen",
                        "&7Actual: &f" + mi.getVolumeSensitivity(),
                        "&7Mayor = más impacto en precio",
                        "&7al comprar/vender en cantidad.",
                        "", "&a[CLICK]"),
                e -> ChatInput.await(player, "Sensibilidad volumen (ej: 0.02):", (p, txt) -> {
                    try {
                        double val = Double.parseDouble(txt.trim());
                        if (val < 0) throw new NumberFormatException();
                        mi.setVolumeSensitivity(val);
                        mgr.saveItem(mi);
                        p.sendMessage(CC.translate("&a✓ Vol. sensibilidad: &f" + val));
                    } catch (NumberFormatException ex) {
                        p.sendMessage(CC.translate("&cValor inválido."));
                    }
                    new ShopItemEditorMenu(itemId).open(p);
                }));

        set(22, item(Material.LAPIS_LAZULI,
                        "&9Sensibilidad Demanda",
                        "&7Actual: &f" + mi.getDemandSensitivity(),
                        "&7Qué tanto afecta la demanda.",
                        "", "&a[CLICK]"),
                e -> ChatInput.await(player, "Sensibilidad demanda (ej: 0.5):", (p, txt) -> {
                    try {
                        double val = Double.parseDouble(txt.trim());
                        if (val < 0) throw new NumberFormatException();
                        mi.setDemandSensitivity(val);
                        mgr.saveItem(mi);
                        p.sendMessage(CC.translate("&a✓ Dem. sensibilidad: &f" + val));
                    } catch (NumberFormatException ex) {
                        p.sendMessage(CC.translate("&cValor inválido."));
                    }
                    new ShopItemEditorMenu(itemId).open(p);
                }));

        set(23, item(Material.NETHER_STAR,
                        "&dSensibilidad Stock",
                        "&7Actual: &f" + mi.getStockSensitivity(),
                        "&7Qué tanto afecta el stock al precio.",
                        "", "&a[CLICK]"),
                e -> ChatInput.await(player, "Sensibilidad stock (ej: 1.0):", (p, txt) -> {
                    try {
                        double val = Double.parseDouble(txt.trim());
                        if (val < 0) throw new NumberFormatException();
                        mi.setStockSensitivity(val);
                        mgr.saveItem(mi);
                        p.sendMessage(CC.translate("&a✓ Stock sensibilidad: &f" + val));
                    } catch (NumberFormatException ex) {
                        p.sendMessage(CC.translate("&cValor inválido."));
                    }
                    new ShopItemEditorMenu(itemId).open(p);
                }));

        set(31, item(mi.isEnabled() ? Material.LIME_DYE : Material.GRAY_DYE,
                        "&f&lEstado",
                        mi.isEnabled() ? "&a✔ ACTIVO &8[CLICK desactivar]" : "&c✘ DESACTIVADO &8[CLICK activar]"),
                e -> {
                    mi.setEnabled(!mi.isEnabled());
                    mgr.saveItem(mi);
                    new ShopItemEditorMenu(itemId).open(player);
                });

        set(33, item(Material.TNT,
                        "&c&lEliminar ítem",
                        "&cElimina del mercado permanentemente.",
                        "", "&c[CLICK]"),
                e -> {
                    mgr.removeItem(itemId);
                    player.sendMessage(CC.translate("&c✗ Ítem &f" + itemId + " &celiminado del mercado."));
                    new ShopEditorMenu(1).open(player);
                });

        set(36, back(), e -> new ShopEditorMenu(1).open(player));
    }
}
