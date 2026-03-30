package org.delawarex.dbz.boosters.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BoosterParser {

    public static double parsePercentageToMultiplier(String percentage) throws IllegalArgumentException {
        if (percentage == null || percentage.trim().isEmpty())
            throw new IllegalArgumentException("El porcentaje no puede estar vacío");

        String cleaned = percentage.trim().replace("%", "");

        try {
            double percent = Double.parseDouble(cleaned);
            if (percent < 0) throw new IllegalArgumentException("El porcentaje no puede ser negativo");
            if (percent > 10000) throw new IllegalArgumentException("El porcentaje no puede ser mayor a 10000%");
            return 1.0 + (percent / 100.0);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Porcentaje inválido: " + percentage);
        }
    }

    public static long parseTimeToSeconds(String timeStr) throws IllegalArgumentException {
        if (timeStr == null || timeStr.trim().isEmpty())
            throw new IllegalArgumentException("El tiempo no puede estar vacío");

        String cleaned = timeStr.trim().toLowerCase();
        long totalSeconds = 0;
        Pattern pattern = Pattern.compile("(\\d+)([dhms])");
        Matcher matcher = pattern.matcher(cleaned);
        boolean foundMatch = false;

        while (matcher.find()) {
            foundMatch = true;
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);
            switch (unit) {
                case "d": totalSeconds += value * 86400L; break;
                case "h": totalSeconds += value * 3600L; break;
                case "m": totalSeconds += value * 60L; break;
                case "s": totalSeconds += value; break;
            }
        }

        if (!foundMatch) throw new IllegalArgumentException("Formato de tiempo inválido. Usa: 1d 2h 30m 45s");
        if (totalSeconds <= 0) throw new IllegalArgumentException("El tiempo debe ser mayor a 0");
        if (totalSeconds > 31536000) throw new IllegalArgumentException("El tiempo no puede ser mayor a 1 año");

        return totalSeconds;
    }

    public static String formatMultiplierAsPercentage(double multiplier) {
        double percentage = (multiplier - 1.0) * 100.0;
        if (percentage % 1.0 == 0) return String.format("%.0f%%", percentage);
        return String.format("%.2f%%", percentage);
    }

    public static String formatSecondsToTime(long seconds) {
        if (seconds <= 0) return "0s";
        StringBuilder sb = new StringBuilder();
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        if (secs > 0 || sb.length() == 0) sb.append(secs).append("s");
        return sb.toString().trim();
    }

    public static boolean isValidPercentage(String percentage) {
        try { parsePercentageToMultiplier(percentage); return true; }
        catch (IllegalArgumentException e) { return false; }
    }

    public static boolean isValidTime(String time) {
        try { parseTimeToSeconds(time); return true; }
        catch (IllegalArgumentException e) { return false; }
    }
}
