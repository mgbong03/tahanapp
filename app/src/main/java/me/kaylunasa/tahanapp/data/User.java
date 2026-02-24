package me.kaylunasa.tahanapp.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import me.kaylunasa.tahanapp.util.Sha256Kt;

public class User {
    private final String username;
    private final String passwordHash;

    private final List<ChildProfile> childProfiles;

    private User (
            String username, String passwordHash,
            List<ChildProfile> childProfiles
    ) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.childProfiles = List.copyOf(childProfiles);
    }

    private User(Draft draft) {
        this(draft.username, draft.passwordHash, draft.childProfiles);
    }

    public static class Draft {
        private String username;
        private String passwordHash;

        private final List<ChildProfile> childProfiles;

        public Draft() {
            this.username = "";
            this.passwordHash = "";
            this.childProfiles = new ArrayList<>();
        }

        public Draft(User user) {
            this.username = user.username;
            this.passwordHash = user.passwordHash;
            this.childProfiles = new ArrayList<>(user.childProfiles);
        }

        public String getUsername() {
            return this.username;
        }

        public String getPasswordHash() {
            return this.passwordHash;
        }

        public List<ChildProfile> getChildProfiles() {
            return this.childProfiles;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setPassword(String password) {
            this.passwordHash = Sha256Kt.sha256(password);
        }

        public boolean addChildProfile(ChildProfile childProfile) {
            for (ChildProfile existingChildProfile : this.childProfiles) if (existingChildProfile.getName().equals(childProfile.getName()))
                return false;
            this.childProfiles.add(0, childProfile);
            return true;
        }

        public boolean removeChildProfile(String name) {
            Iterator<ChildProfile> iter = this.childProfiles.iterator();
            while (iter.hasNext()) {
                ChildProfile assessment = iter.next();
                if (assessment.getName().equals(name)) {
                    iter.remove();
                    return true;
                }
            }
            return false;
        }

        public User finalizeDraft() {
            return new User(this);
        }
    }

    public String getUsername() {
        return this.username;
    }

    public String getPasswordHash() {
        return this.passwordHash;
    }

    public List<ChildProfile> getChildProfiles() {
        return this.childProfiles;
    }

    public ChildProfile getChildProfile(String profileName) {
        for (ChildProfile childProfile : this.childProfiles) if (childProfile.getName().equals(profileName))
            return childProfile;
        return null;
    }

    public static User fromJsonData(JSONObject data) {
        List<ChildProfile> childProfiles = new ArrayList<>();
        JSONArray childProfilesJsonArray = data.optJSONArray("childProfiles");
        if (childProfilesJsonArray != null) for (int i = 0; i < childProfilesJsonArray.length(); i++) {
            Object obj = childProfilesJsonArray.opt(i);
            if (!(obj instanceof JSONObject) || obj == JSONObject.NULL)
                continue;
            childProfiles.add(ChildProfile.fromJsonData((JSONObject) obj));
        }

        return new User(
                data.optString("username"), data.optString("passwordHash"),
                childProfiles
        );
    }

    public JSONObject toJsonData() {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("username", this.username);
            jsonObject.put("passwordHash", this.passwordHash);

            JSONArray childProfilesJsonArray = new JSONArray();
            this.childProfiles.forEach(childProfile -> childProfilesJsonArray.put(childProfile.toJsonData()));
            jsonObject.put("childProfiles", childProfilesJsonArray);
        }
        catch (JSONException ignored) {}

        return jsonObject;
    }
}
