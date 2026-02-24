package me.kaylunasa.tahanapp.data;

import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Assessment {
    private static final String TAG = Assessment.class.getSimpleName();

    private final Date timestamp;

    private final int faceScore;
    private final int legsScore;
    private final int activityScore;
    private final int cryScore;
    private final int consolabilityScore;

    private final List<String> painLocations;
    private final String comments;

    private Assessment(
            Date timestamp,
            int faceScore, int legsScore, int activityScore,
            int cryScore, int consolabilityScore,
            @NotNull List<String> painLocations, @NotNull String comments
    ) {
        this.timestamp = timestamp;
        this.faceScore = faceScore;
        this.legsScore = legsScore;
        this.activityScore = activityScore;
        this.cryScore = cryScore;
        this.consolabilityScore = consolabilityScore;

        this.painLocations = List.copyOf(painLocations);
        this.comments = comments;
    }

    private Assessment(Draft draft) {
        this(Calendar.getInstance().getTime(), draft.faceScore, draft.legsScore, draft.activityScore,
                draft.cryScore, draft.consolabilityScore,
                draft.painLocations, draft.comments);

        Date timestamp = Calendar.getInstance().getTime();
        Log.d(TAG, "Assessment: Timestamp is " + timestamp.getTime());
    }

    public static class Draft {
        private int faceScore;
        private int legsScore;
        private int activityScore;
        private int cryScore;
        private int consolabilityScore;

        private final List<String> painLocations;
        private String comments;

        public Draft() {
            this.faceScore = 0;
            this.legsScore = 0;
            this.activityScore = 0;
            this.cryScore = 0;
            this.consolabilityScore = 0;

            this.painLocations = new ArrayList<>();
            this.comments = "";
        }

        public int getFaceScore() {
            return this.faceScore;
        }

        public int getLegsScore() {
            return this.legsScore;
        }

        public int getActivityScore() {
            return this.activityScore;
        }

        public int getCryScore() {
            return this.cryScore;
        }

        public int getConsolabilityScore() {
            return this.consolabilityScore;
        }

        public List<String> getPainLocations() {
            return this.painLocations;
        }

        public String getComments() {
            return this.comments;
        }

        public void setFaceScore(int faceScore){
            this.faceScore = faceScore;
        }

        public void setLegsScore(int legsScore) {
            this.legsScore = legsScore;
        }

        public void setActivityScore(int activityScore) {
            this.activityScore = activityScore;
        }

        public void setCryScore(int cryScore) {
            this.cryScore = cryScore;
        }

        public void setConsolabilityScore(int consolabilityScore) {
            this.consolabilityScore = consolabilityScore;
        }

        public boolean addPainLocation(String painLocation) {
            return this.painLocations.add(painLocation);
        }

        public boolean removePainLocation(String painLocation) {
            return this.painLocations.remove(painLocation);
        }

        public void resetPainLocations() {
            this.painLocations.clear();
        }

        public void setComments(String comments) {
            this.comments = comments;
        }

        public Assessment finalizeDraft() {
            return new Assessment(this);
        }
    }

    public Date getTimestamp() {
        return this.timestamp;
    }
    public int getFaceScore() {
        return this.faceScore;
    }

    public int getLegsScore() {
        return this.legsScore;
    }

    public int getActivityScore() {
        return this.activityScore;
    }

    public int getCryScore() {
        return this.cryScore;
    }

    public int getConsolabilityScore() {
        return this.consolabilityScore;
    }

    public int getTotal() {
        return this.faceScore + this.legsScore + this.activityScore + this.cryScore + this.consolabilityScore;
    }

    public int getPainLevel() {
        final int total = this.getTotal();
        if (total == 0)
            return 0;
        if (total < 4)
            return 1;
        if (total < 7)
            return 2;
        return 3;
    }

    public List<String> getPainLocations() {
        return this.painLocations;
    }

    public String getComments() {
        return this.comments;
    }

    public static Assessment fromJsonData(JSONObject data) {
        List<String> painLocations = new ArrayList<>();
        JSONArray painLocationsJsonArr = data.optJSONArray("painLocations");
        if (painLocationsJsonArr != null) for (int i = 0; i < painLocationsJsonArr.length(); i++){
            Object obj = painLocationsJsonArr.opt(i);
            if (!(obj instanceof String))
                continue;
            painLocations.add(obj.toString());
        }

        return new Assessment(
                new Date(data.optLong("timestamp", 0)),
                data.optInt("faceScore", 0), data.optInt("legsScore", 0),
                data.optInt("activityScore", 0), data.optInt("cryScore", 0),
                data.optInt("consolabilityScore", 0),
                painLocations, data.optString("comments")
        );
    }

    public JSONObject toJsonData() {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("timestamp", this.timestamp.getTime());
            jsonObject.put("faceScore", this.faceScore);
            jsonObject.put("legsScore", this.legsScore);
            jsonObject.put("activityScore", this.activityScore);
            jsonObject.put("cryScore", this.cryScore);
            jsonObject.put("consolabilityScore", this.consolabilityScore);

            JSONArray painLocationsJsonArr = new JSONArray();
            this.painLocations.forEach(painLocationsJsonArr::put);
            jsonObject.put("painLocations", painLocationsJsonArr);

            jsonObject.put("comments", this.comments);
        }
        catch (JSONException ignored) {}

        return jsonObject;
    }
}
