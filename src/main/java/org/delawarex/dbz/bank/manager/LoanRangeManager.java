package org.delawarex.dbz.bank.manager;

import org.delawarex.dbz.bank.model.LoanRange;
import org.delawarex.dbz.bank.storage.LoanRangeStorage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class LoanRangeManager {

    private final LoanRangeStorage storage;
    private List<LoanRange> ranges;

    public LoanRangeManager() {
        storage = new LoanRangeStorage();
        ranges  = new ArrayList<>(storage.load());
        sort();
    }

    public Optional<LoanRange> getRangeForLevel(int level) {
        return ranges.stream().filter(r -> r.contains(level)).findFirst();
    }

    public void addRange(LoanRange range) {
        ranges.removeIf(r -> r.getMinLevel() == range.getMinLevel() && r.getMaxLevel() == range.getMaxLevel());
        ranges.add(range);
        sort();
        save();
    }

    public boolean removeRange(int index) {
        if (index < 0 || index >= ranges.size()) return false;
        ranges.remove(index);
        save();
        return true;
    }

    public void updateRange(int index, LoanRange updated) {
        if (index < 0 || index >= ranges.size()) return;
        ranges.set(index, updated);
        sort();
        save();
    }

    public boolean overlaps(LoanRange candidate, int excludeIndex) {
        for (int i = 0; i < ranges.size(); i++) {
            if (i == excludeIndex) continue;
            LoanRange r = ranges.get(i);
            if (candidate.getMinLevel() <= r.getMaxLevel() && candidate.getMaxLevel() >= r.getMinLevel()) return true;
        }
        return false;
    }

    public void save() {
        storage.save(ranges);
    }

    public void reload() {
        ranges = new ArrayList<>(storage.load());
        sort();
    }

    private void sort() {
        ranges.sort(Comparator.comparingInt(LoanRange::getMinLevel));
    }

    public List<LoanRange> getRanges() { return ranges; }
}
