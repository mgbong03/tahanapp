package me.kaylunasa.tahanapp.data;

import android.graphics.Bitmap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import me.kaylunasa.tahanapp.util.BitmapBase64Kt;

public class ChildProfile {
    private final String name;
    private final int age;
    private final String gender;
    private final String diagnosis;
    private final Bitmap image;

    private final List<Assessment> assessmentHistory;

    private ChildProfile(
            String name, int age, String gender, String diagnosis,
            Bitmap image, List<Assessment> assessmentHistory
    ) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.diagnosis = diagnosis;
        this.image = image;

        this.assessmentHistory = List.copyOf(assessmentHistory);
    }

    private ChildProfile(Draft draft) {
        this(draft.name, draft.age, draft.gender,
                draft.diagnosis, draft.image, draft.assessmentHistory);
    }

    public static class Draft {
        private String name;
        private int age;
        private String gender;
        private String diagnosis;
        private Bitmap image;

        private final List<Assessment> assessmentHistory;

        public Draft() {
            this.name = "";
            this.age = 0;
            this.gender = "Unspecified";
            this.diagnosis = "";
            this.assessmentHistory = new ArrayList<>();
        }

        public Draft(ChildProfile profile) {
            this.name = profile.name;
            this.age = profile.age;
            this.gender = profile.gender;
            this.diagnosis = profile.diagnosis;
            this.image = profile.image;
            this.assessmentHistory = new ArrayList<>(profile.assessmentHistory);
        }

        public String getName() {
            return this.name;
        }

        public int getAge() {
            return this.age;
        }

        public String getGender() {
            return this.gender;
        }

        public String getDiagnosis() {
            return this.diagnosis;
        }

        public Bitmap getImage() {
            return this.image;
        }

        public List<Assessment> getAssessmentHistory() {
            return this.assessmentHistory;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public void setDiagnosis(String diagnosis) {
            this.diagnosis = diagnosis;
        }

        public void setImage(Bitmap image) {
            this.image = image;
        }

        public boolean addAssessment(Assessment assessment) {
            Date timestamp = assessment.getTimestamp();
            for (Assessment prevAssessment : this.assessmentHistory) if (prevAssessment.getTimestamp() == timestamp)
                return false;
            return this.assessmentHistory.add(assessment);
        }

        public boolean removeAssessment(Date timestamp) {
            Iterator<Assessment> iter = this.assessmentHistory.iterator();
            while (iter.hasNext()) {
                Assessment assessment = iter.next();
                if (assessment.getTimestamp().equals(timestamp)) {
                    iter.remove();
                    return true;
                }
            }
            return false;
        }

        public ChildProfile finalizeDraft() {
            return new ChildProfile(this);
        }
    }

    public String getName() {
        return this.name;
    }

    public int getAge() {
        return this.age;
    }

    public String getGender() {
        return this.gender;
    }

    public String getDiagnosis() {
        return this.diagnosis;
    }

    public Bitmap getImage() {
        return this.image;
    }

    public List<Assessment> getAssessmentHistory() {
        return this.assessmentHistory;
    }

    public Assessment getAssessment(long timestamp) {
        for (Assessment assessment : this.assessmentHistory) if (assessment.getTimestamp().getTime() == timestamp)
            return assessment;
        return null;
    }

    public static ChildProfile fromJsonData(JSONObject data) {
        String base64 = data.optString("image");
        Bitmap image = null;
        if (!base64.isEmpty())
            image = BitmapBase64Kt.base64ToBitmap(base64);

        List<Assessment> assessmentHistory = new ArrayList<>();
        JSONArray assessmentHistoryJsonArray = data.optJSONArray("assessmentHistory");
        if (assessmentHistoryJsonArray != null) for (int i = 0; i < assessmentHistoryJsonArray.length(); i++) {
            Object obj = assessmentHistoryJsonArray.opt(i);
            if (!(obj instanceof JSONObject) || obj == JSONObject.NULL)
                continue;
            assessmentHistory.add(Assessment.fromJsonData((JSONObject) obj));
        }

        return new ChildProfile(
                data.optString("name"), data.optInt("age"), data.optString("gender"),
                data.optString("diagnosis"), image, assessmentHistory
        );
    }

    public JSONObject toJsonData() {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("name", this.name);
            jsonObject.put("age", this.age);
            jsonObject.put("gender", this.gender);
            jsonObject.put("diagnosis", this.diagnosis);
            if (image != null)
                jsonObject.put("image", BitmapBase64Kt.toBase64(image, Bitmap.CompressFormat.PNG, 100));

            JSONArray assessmentHistoryJsonArray = new JSONArray();
            this.assessmentHistory.forEach(assessment -> assessmentHistoryJsonArray.put(assessment.toJsonData()));
            jsonObject.put("assessmentHistory", assessmentHistoryJsonArray);
        }
        catch (JSONException ignored){}

        return jsonObject;
    }
}
