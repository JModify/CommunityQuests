package me.wonka01.ServerQuests.commands;

import lombok.NonNull;
import me.knighthat.apis.commands.PluginCommand;
import me.modify.townyquests.autoquest.AutoQuestTimer;
import me.wonka01.ServerQuests.ServerQuests;
import me.wonka01.ServerQuests.configuration.QuestModel;
import me.wonka01.ServerQuests.enums.EventType;
import me.wonka01.ServerQuests.questcomponents.ActiveQuests;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class StartCommand extends PluginCommand {

    public StartCommand(ServerQuests plugin) {
        super(plugin, true);
    }

    @Override
    public @NonNull String getName() {
        return "start";
    }

    @Override
    public @NonNull String getPermission() {
        return "townyquests.start";
    }

    @Override
    public void execute(@NonNull CommandSender sender, @NotNull @NonNull String[] args) {
        if (!getPlugin().getAutoQuest().isEnabled()) {
            if (args.length >= 3) {
                QuestModel model = getPlugin().questLibrary.getQuestModelById(args[1]);
                if (model == null) {
                    String invalidName = getPlugin().getMessages().message("invalidQuestName");
                    sender.sendMessage(invalidName);
                    return;
                }

                EventType type;
                switch (args[2]) {
                    case "coop":
                        type = EventType.COLLAB;
                        if (model.getQuestGoal() <= 0) {
                            String noGoal = getPlugin().getMessages().message("cooperativeQuestMustHaveAGoal");
                            sender.sendMessage(noGoal);
                            return;
                        }
                        break;
                    case "comp":
                        type = EventType.COMPETITIVE;
                        break;
                    default:
                        String invalidQuestType = getPlugin().getMessages().message("invalidQuestType");
                        sender.sendMessage(invalidQuestType);
                        return;
                }

                if (!ActiveQuests.getActiveQuestsInstance().beginNewQuest(model, type)) {
                    String reachLimit = getPlugin().getMessages().message("questLimitReached");
                    sender.sendMessage(reachLimit);
                }
            } else if (args.length == 2) {
                String invalidQuestType = getPlugin().getMessages().message("invalidQuestType");
                sender.sendMessage(invalidQuestType);
            } else if (sender instanceof Player) {
                getPlugin().getStartGui().openInventory((Player) sender);
            }
        } else {
            String commandDisabledMessage = getPlugin().getMessages().message("commandDisabled");
            sender.sendMessage(commandDisabledMessage);
        }
    }
}
