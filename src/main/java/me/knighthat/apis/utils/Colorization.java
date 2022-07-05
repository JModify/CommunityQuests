package me.knighthat.apis.utils;

import lombok.NonNull;
import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Colorization {

    default @NonNull String color(@NonNull String message) {
        Pattern HEX_PATTERN = Pattern.compile("&#(\\w{5}[0-9a-f])");
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while(matcher.find()) {
            matcher.appendReplacement(buffer, ChatColor.of("#" + matcher.group(1)).toString());
        }

        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }

    default @NonNull String strip(@NonNull String a) {
        return ChatColor.stripColor(color(a));
    }
}
