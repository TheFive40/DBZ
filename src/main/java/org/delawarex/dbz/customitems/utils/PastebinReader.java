package org.delawarex.dbz.customitems.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class PastebinReader {

    /**
     * Downloads content from the given Pastebin URL and returns each line as a list entry.
     * Returns null if the download fails.
     */
    public static List<String> download(String input) {
        String rawUrl = toRawUrl(input.trim());
        if (rawUrl == null) return null;

        List<String> lines = new ArrayList<>();
        try {
            URL url = new URL(rawUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) return null;

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            }
            return lines;

        } catch (Exception e) {
            return null;
        }
    }

    /** Converts any Pastebin URL format to the raw download URL. */
    private static String toRawUrl(String input) {
        String clean = input
                .replace("https://", "")
                .replace("http://", "")
                .replace("www.", "");

        if (clean.startsWith("pastebin.com/raw/")) {
            return "https://" + clean;
        }

        if (clean.startsWith("pastebin.com/")) {
            String code = clean.replace("pastebin.com/", "");
            if (code.isEmpty()) return null;
            return "https://pastebin.com/raw/" + code;
        }

        if (!clean.contains(".") && !clean.isEmpty()) {
            return "https://pastebin.com/raw/" + clean;
        }

        return null;
    }
}