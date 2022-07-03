package me.knighthat.apis.files;

import lombok.NonNull;
import me.wonka01.ServerQuests.ServerQuests;
import me.wonka01.ServerQuests.questcomponents.QuestData;

import java.util.List;

public final class Messages extends Getters {

    public Messages(ServerQuests plugin) {
        super(plugin);
    }

    public @NonNull String getPrefix() {
        return color(string("prefix"));
    }

    public @NonNull String message(@NonNull String path) {
        return !get().contains(path) ? "" : color(getPrefix() + string(path));
    }

    public @NonNull String message(@NonNull String path, @NonNull QuestData quest) {

        String result = message(path).replace("questName", quest.getDisplayName());

        List<String> questDescription = quest.getDescription();
        StringBuilder builder = new StringBuilder();
        for (String line : questDescription) {
            builder.append(line).append('\n').append(" ");
        }
        String descString = builder.substring(0, builder.length() - 2);
        result = result.replace("questDescription", descString);
        return result;
    }

    public @NonNull String string(@NonNull String path, @NonNull QuestData quest) {

        String result = string(path).replace("questName", quest.getDisplayName());

        List<String> questDescription = quest.getDescription();
        StringBuilder builder = new StringBuilder();
        for (String line : questDescription) {
            builder.append(line).append('\n').append(" ");
        }
        String descString = builder.substring(0, builder.length() - 2);
        result = result.replace("questDescription", descString);

        return result;
    }

    @Override
    public @NonNull String string(@NonNull String path) {
        return color(super.string(path));
    }
}
