package org.delawarex.dbz.bank.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.delawarex.dbz.DbzMain;
import org.delawarex.dbz.bank.model.LoanRange;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class LoanRangeStorage {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LIST_TYPE = new TypeToken<List<LoanRange>>(){}.getType();

    private final File file;

    public LoanRangeStorage() {
        File folder = new File(DbzMain.instance.getDataFolder(), "bank");
        folder.mkdirs();
        file = new File(folder, "loan_ranges.json");
        if (!file.exists()) saveDefaults();
    }

    private void saveDefaults() {
        List<LoanRange> defaults = new ArrayList<>();
        defaults.add(new LoanRange(1,   50,  50_000L,     100_000, 0.05, 5,  24, 0.30));
        defaults.add(new LoanRange(51,  100, 300_000L,    500_000, 0.07, 8,  24, 0.30));
        defaults.add(new LoanRange(101, 200, 1_000_000L,  2_000_000, 0.10, 12, 24, 0.30));
        save(defaults);
    }

    public List<LoanRange> load() {
        if (!file.exists()) return new ArrayList<>();
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            List<LoanRange> list = GSON.fromJson(reader, LIST_TYPE);
            return list != null ? list : new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void save(List<LoanRange> ranges) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            GSON.toJson(ranges, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
