package ovh.not.javamusicbot;

import org.json.JSONArray;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

abstract class GenericUserManager {
    private final File file;
    protected List<String> list;

    GenericUserManager(String path) {
        file = new File(path);
        try {
            list = load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private synchronized List<String> load() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            if (builder.length() == 0) {
                return new ArrayList<>();
            }
            JSONArray array = new JSONArray(builder.toString());
            List<String> list = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                list.add(array.getString(i));
            }
            return list;
        } catch (FileNotFoundException e) {
            file.createNewFile();
            return load();
        }
    }

    synchronized void save() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(new JSONArray(list).toString());
        }
    }
}
