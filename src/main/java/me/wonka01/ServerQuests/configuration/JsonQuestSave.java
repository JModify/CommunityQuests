package me.wonka01.ServerQuests.configuration;

import me.modify.townyquests.autoquest.AutoQuest;
import me.wonka01.ServerQuests.ServerQuests;
import me.wonka01.ServerQuests.questcomponents.ActiveQuests;
import me.wonka01.ServerQuests.questcomponents.CompetitiveQuestData;
import me.wonka01.ServerQuests.questcomponents.QuestController;
import me.wonka01.ServerQuests.questcomponents.players.PlayerData;
import me.wonka01.ServerQuests.util.EventTypeHandler;
import org.bukkit.Bukkit;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class JsonQuestSave {
    private File path;
    private ServerQuests plugin;

    public JsonQuestSave(File path, ServerQuests plugin) {
        this.path = new File(path + "/questSave.json");
        this.plugin = plugin;
    }

    public boolean getOrCreateQuestFile() {
        if (path.exists()) {
            return true;
        } else {
            try {
                path.createNewFile();
            } catch (IOException e) {
                Bukkit.getServer().getConsoleSender().sendMessage(e.getMessage());
            }
        }
        return false;
    }

    public void saveQuestsInProgress() {
        // Save manually activated quests to file
        JSONArray activeQuests = new JSONArray();
        for (QuestController questController : ActiveQuests.getActiveQuestsInstance().getActiveQuestsList()) {
            if (questController.getQuestData().isGoalComplete()) {
                continue;
            }

            JSONObject jObject = new JSONObject();
            jObject.put("id", questController.getQuestType());
            jObject.put("playerMap", questController.getPlayerComponent().getPlayerDataInJson());
            jObject.put("amountComplete", questController.getQuestData().getAmountCompleted());
            jObject.put("timeLeft", questController.getQuestData().getQuestDuration());
            jObject.put("autoQuest", questController.getQuestData().isAutoQuest());
            if (questController.getQuestData() instanceof CompetitiveQuestData) {
                jObject.put("type", "comp");
            } else {
                jObject.put("type", "coop");
            }
            activeQuests.add(jObject);
        }

        try {
            FileWriter fileWriter = new FileWriter(path, false); // overwrite the existing file
            JSONObject object = new JSONObject();
            object.put("activeQuests", activeQuests);
            fileWriter.write(object.toJSONString());
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readAndInitializeQuests() {
        if (!path.exists()) {
            return;
        }

        JSONParser parser = new JSONParser();
        try {
            JSONObject object = (JSONObject) parser.parse(new FileReader(path.getPath()));

            JSONArray activeQuests = (JSONArray) object.get("activeQuests");
            Iterator activeQuestsIterator = activeQuests.iterator();
            while (activeQuestsIterator.hasNext()) {
                JSONObject questObject = (JSONObject) activeQuestsIterator.next();
                String questId = (String) questObject.get("id");
                String questType = (String) questObject.get("type");
                double amountComplete = (double) questObject.get("amountComplete");
                long questDuration = (Long) questObject.getOrDefault("timeLeft", 0);
                boolean autoQuest = (Boolean) questObject.get("autoQuest");

                JSONArray playerObject = (JSONArray) questObject.get("playerMap");

                Iterator<JSONObject> pIterator = playerObject.iterator();
                Map<UUID, PlayerData> playerMap = new TreeMap<>();

                while (pIterator.hasNext()) {
                    JSONObject obj = pIterator.next();
                    UUID uuid = UUID.fromString((String) obj.keySet().iterator().next());
                    String playerName = Bukkit.getServer().getOfflinePlayer(uuid).getName();
                    double pContributed = (double) obj.get(uuid.toString());
                    playerMap.put(uuid, new PlayerData(playerName, (int) pContributed));
                }

                EventTypeHandler handler = new EventTypeHandler(questType);
                QuestModel model = plugin.getQuestLibrary().getQuestModelById(questId);

                if (model == null || (amountComplete >= model.getQuestGoal() && model.getQuestGoal() > 0)) {
                    Bukkit.getLogger().info("The quest in the save file has expired and will not be initialized.");
                    continue;
                }

                QuestController controller = handler.createControllerFromSave(model, playerMap, (int) amountComplete, (int) questDuration, autoQuest);
                ActiveQuests.getActiveQuestsInstance().beginQuestFromSave(controller);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
