package org.delawarex.dbz.market.engine;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.delawarex.dbz.bank.manager.BankManager;
import org.delawarex.dbz.bank.model.BankAccount;
import org.delawarex.dbz.bank.model.LoanType;
import org.delawarex.dbz.market.model.MarketEvent;
import org.delawarex.dbz.market.model.MarketItem;
import org.delawarex.dbz.market.model.Transaction;
import org.delawarex.dbz.market.storage.EconomyManager;
import org.delawarex.dbz.market.storage.MarketDataManager;
import org.delawarex.service.CC;

public class TransactionEngine {

    private final PriceEngine      priceEngine;
    private final StockEngine      stockEngine;
    private final AntiExploitEngine exploit;
    private final EconomyManager   economy;
    private final MarketDataManager data;

    public TransactionEngine(PriceEngine p, StockEngine s, AntiExploitEngine a, EconomyManager e, MarketDataManager d) {
        this.priceEngine = p;
        this.stockEngine = s;
        this.exploit     = a;
        this.economy     = e;
        this.data        = d;
    }

    public TransactionResult buy(Player player, MarketItem item, int quantity, MarketEvent event) {
        String exploitMsg = exploit.checkBuy(player, item, quantity);
        if (exploitMsg != null) return TransactionResult.fail(exploitMsg);

        if (!stockEngine.canBuy(item, quantity))
            return TransactionResult.fail("Stock insuficiente. Disponible: &f" + item.getStock());

        double total = priceEngine.calcOrderBuyTotal(item, quantity, event);
        double balance = economy.getBalance(player);

        if (balance < total)
            return TransactionResult.fail("Fondos insuficientes. Necesitas &f" + String.format("%.2f", total) + " &cZeni. Tienes &f" + String.format("%.2f", balance));

        economy.withdraw(player, total);
        giveItems(player, item, quantity);
        stockEngine.consumeStock(item, quantity);
        item.recordBuy(quantity);

        double perUnit = Math.round((total / quantity) * 100.0) / 100.0;
        exploit.recordAction(player, item);
        exploit.recordBuyPrice(player, item, perUnit);
        priceEngine.updateTrend(item);

        Transaction tx = new Transaction(player.getUniqueId(), player.getName(), item.getId(),
                Transaction.Type.BUY, quantity, perUnit, total);
        item.addToHistory(tx);
        data.saveItem(item);

        applyZeniPenaltyOnPurchase(player, total);

        return TransactionResult.ok(total, quantity, perUnit);
    }

    public TransactionResult sell(Player player, MarketItem item, int quantity, MarketEvent event) {
        String exploitMsg = exploit.checkSell(player, item, quantity);
        if (exploitMsg != null) return TransactionResult.fail(exploitMsg);

        int found = countItemsInInventory(player, item);
        if (found < quantity)
            return TransactionResult.fail("No tienes suficientes ítems. Tienes &f" + found);

        double total = priceEngine.calcOrderSellTotal(item, quantity, event);

        removeItemsFromInventory(player, item, quantity);
        economy.deposit(player, total);
        stockEngine.addStock(item, quantity);
        item.recordSell(quantity);

        double perUnit = Math.round((total / quantity) * 100.0) / 100.0;
        exploit.recordAction(player, item);
        priceEngine.updateTrend(item);

        Transaction tx = new Transaction(player.getUniqueId(), player.getName(), item.getId(),
                Transaction.Type.SELL, quantity, perUnit, total);
        item.addToHistory(tx);
        data.saveItem(item);

        applyZeniPenaltyOnSale(player, total);

        return TransactionResult.ok(total, quantity, perUnit);
    }

    private void applyZeniPenaltyOnPurchase(Player player, double amount) {
        try {
            BankAccount acc = BankManager.getInstance().getAccount(player.getUniqueId());
            if (acc == null || !acc.hasZeniPenalty()) return;
            double penaltyAmount = amount * acc.getZeniPenaltyRate();
            if (penaltyAmount <= 0) return;
            if (economy.withdraw(player, penaltyAmount)) {
                boolean anyPaid = BankManager.getInstance().getPenaltyManager().applyZeniDebtPayment(acc, penaltyAmount);
                BankManager.getInstance().save(acc);
                player.sendMessage(CC.translate("&c⚠ Penalización: &f" + String.format("%.2f", penaltyAmount) + " Zenis descontados de tu compra para cubrir tu deuda."));
                if (anyPaid) BankManager.getInstance().notifyLoanPaidOff(acc, LoanType.ZENIS);
            }
        } catch (Exception ignored) {}
    }

    private void applyZeniPenaltyOnSale(Player player, double amount) {
        try {
            BankAccount acc = BankManager.getInstance().getAccount(player.getUniqueId());
            if (acc == null || !acc.hasZeniPenalty()) return;
            double penaltyAmount = amount * acc.getZeniPenaltyRate();
            if (penaltyAmount <= 0) return;
            if (economy.withdraw(player, penaltyAmount)) {
                boolean anyPaid = BankManager.getInstance().getPenaltyManager().applyZeniDebtPayment(acc, penaltyAmount);
                BankManager.getInstance().save(acc);
                player.sendMessage(CC.translate("&c⚠ Penalización: &f" + String.format("%.2f", penaltyAmount) + " Zenis de tu venta aplicados a tu deuda bancaria."));
                if (anyPaid) BankManager.getInstance().notifyLoanPaidOff(acc, LoanType.ZENIS);
            }
        } catch (Exception ignored) {}
    }

    private void giveItems(Player player, MarketItem item, int quantity) {
        Material mat;
        try {
            mat = Material.valueOf(item.getMaterial().toUpperCase());
        } catch (IllegalArgumentException e) {
            mat = Material.STONE;
        }
        int remaining = quantity;
        while (remaining > 0) {
            int stackSize = Math.min(64, remaining);
            ItemStack stack = new ItemStack(mat, stackSize);
            if (player.getInventory().firstEmpty() == -1) {
                player.getWorld().dropItem(player.getLocation(), stack);
            } else {
                player.getInventory().addItem(stack);
            }
            remaining -= stackSize;
        }
    }

    private int countItemsInInventory(Player player, MarketItem item) {
        Material mat;
        try {
            mat = Material.valueOf(item.getMaterial().toUpperCase());
        } catch (IllegalArgumentException e) {
            return 0;
        }
        int count = 0;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack != null && stack.getType() == mat) count += stack.getAmount();
        }
        return count;
    }

    private void removeItemsFromInventory(Player player, MarketItem item, int quantity) {
        Material mat;
        try {
            mat = Material.valueOf(item.getMaterial().toUpperCase());
        } catch (IllegalArgumentException e) {
            return;
        }
        int remaining = quantity;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (remaining <= 0) break;
            if (stack == null || stack.getType() != mat) continue;
            int take = Math.min(stack.getAmount(), remaining);
            stack.setAmount(stack.getAmount() - take);
            remaining -= take;
        }
    }

    public record TransactionResult(boolean success, String message, double total, int quantity, double perUnit) {
        static TransactionResult ok(double total, int qty, double per) {
            return new TransactionResult(true, null, total, qty, per);
        }
        public static TransactionResult fail(String msg) {
            return new TransactionResult(false, msg, 0, 0, 0);
        }
    }
}
