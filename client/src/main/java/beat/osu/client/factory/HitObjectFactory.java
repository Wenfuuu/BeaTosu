package beat.osu.client.factory;

import beat.osu.client.model.Beatmap;
import beat.osu.client.model.HitCircle;
import beat.osu.client.model.HitObject;
import beat.osu.client.model.HitSlider;
import beat.osu.client.utils.OsuParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class HitObjectFactory {

    public static int getComboSkipCount(String data) {
        String[] parts = data.split(",");
        int type = Integer.parseInt(parts[3]);
        return (type >> 4) & 7;
    }

    public static boolean checkNewCombo(String data) {
        String[] parts = data.split(",");
        int type = Integer.parseInt(parts[3]);
        return (type & 4) != 0;
    }

    private static String decodeType(int type){
        boolean isHitCircle = (type & 1) != 0;
        boolean isSlider = (type & 2) != 0;
//        boolean isNewCombo = (type & 4) != 0;
        boolean isSpinner = (type & 8) != 0;
        boolean isHold = (type & 128) != 0;

        if(isHitCircle) return "circle";
        else if(isSlider) return "slider";
        return "spinner";
    }

    private static ArrayList<String> generateCircleSfxFilenames(int hitSound, String hitSample) {
        String[] parts = hitSample.split(":");
        int normalSetId = parts.length > 0 && !parts[0].isEmpty() ? Integer.parseInt(parts[0]) : 0;
        int additionSetId = parts.length > 1 && !parts[1].isEmpty() ? Integer.parseInt(parts[1]) : 0;
        int index = parts.length > 2 && !parts[2].isEmpty() ? Integer.parseInt(parts[2]) : 0;
        String filename = parts.length > 4 ? parts[4] : "";

        ArrayList<String> sounds = new ArrayList<>();
        if (!filename.isEmpty()) {
            sounds.add(filename);
            return sounds;
        }

        String normalSet = getSampleSetName(normalSetId);
        String additionSet = getSampleSetName(additionSetId);

        System.out.println(normalSet);
        if(normalSet.equals("normal")) {
            if (hitSound == 0 || (hitSound & 1) != 0) {
                sounds.add(buildCircleFilename(normalSet, "hitnormal", index));
            }
            if ((hitSound & 2) != 0 || (hitSound & 4) != 0 || (hitSound & 8) != 0) {
                sounds.add(buildCircleFilename(additionSet, "hitnormal", index));
            }
        }else {
            if (hitSound == 0 || (hitSound & 1) != 0) {
                sounds.add(buildCircleFilename(normalSet, "hitnormal", index));
            }
            if ((hitSound & 2) != 0) {
                sounds.add(buildCircleFilename(additionSet, "hitwhistle", index));
            }
            if ((hitSound & 4) != 0) {
                sounds.add(buildCircleFilename(additionSet, "hitfinish", index));
            }
            if ((hitSound & 8) != 0) {
                sounds.add(buildCircleFilename(additionSet, "hitclap", index));
            }
        }

        System.out.println("total sounds: " + sounds.size());
        return sounds;
    }

    private static String getSampleSetName(int id) {
        switch (id) {
            case 2: return "soft";
            case 3: return "drum";
            default: return "normal";
        }
    }

    private static String buildCircleFilename(String setName, String type, int index) {
        if (index <= 1) {
            return setName + "-" + type + ".wav";
        } else {
            return setName + "-" + type + index + ".wav";
        }
    }

    public static HitObject createHitObject(String data, Beatmap selectedBeatmap,
                                            int comboNumber, int comboSetIndex,
                                            boolean comboEnd){
        // Circle (length 6) => 382,305,6867,1,2,3:2:0:0:
        // Slider => 59,124,2279,6,0,P|116:91|220:132,1,171.73332756836,2|0,0:2|0:2,0:0:0:0:
        Map<String, String> colours = OsuParser.getColours();
        String key = "Combo" + (comboSetIndex + 1); // +1 because osu uses 1-based keys
        String colorString = colours.getOrDefault(key, "255,255,255"); // Default to white if not found
//        System.out.println("key: " + key);
//        System.out.println("color: " + colorString);

        String[] parts = data.split(",");
//        System.out.println(parts.length);

        int x = Integer.parseInt(parts[0]);
        int y = Integer.parseInt(parts[1]);
        int time = Integer.parseInt(parts[2]);
        int type = Integer.parseInt(parts[3]);
        int hitSound = Integer.parseInt(parts[4]);
        int spinnerEndTime = time;

        String objectParams = "";
        String hitSample = parts[parts.length-1];

        String hitType = decodeType(type);

        if(parts.length == 6){// circle
            hitSample = parts[5];
        }else if(parts.length == 7){// spinner
            spinnerEndTime = Integer.parseInt(parts[5]);
        }else{// slider
            objectParams = String.join(",", Arrays.copyOfRange(parts, 5, parts.length-1));
//            hitSample = parts[parts.length-1];
        }

        // check if hitSample is invalid
        if(!hitSample.contains(":")){
            hitSample = "0:0:0:0:";
            objectParams = String.join(",", Arrays.copyOfRange(parts, 5, parts.length));
        }
//        System.out.println(objectParams);
//        for (String sfx : generateCircleSfxFilenames(hitSound, hitSample)) {
//            System.out.println("sfx: " + sfx);
//        }

        if(hitType.equals("slider")) {
            // get edge sounds using object params => edgeSounds & edgeSets

        }

        double approachRate = selectedBeatmap.getApproachRate();
        double circleSize = selectedBeatmap.getCircleSize();
//        return new HitCircle(x, y, time, type, hitSound, hitSample, approachRate);
        if(hitType.equals("circle")){
            return new HitCircle(x, y, time, type, hitSound, hitSample, approachRate, circleSize,
                    comboNumber, comboSetIndex, colorString, comboEnd,
                    generateCircleSfxFilenames(hitSound, hitSample));
        }else if(hitType.equals("slider")){
            return new HitSlider(x, y, time, type, hitSound, objectParams, hitSample,
                    approachRate, circleSize, selectedBeatmap.getSliderMultiplier(),
                    comboNumber, comboSetIndex, colorString, comboEnd,
                    generateCircleSfxFilenames(hitSound, hitSample));
        }else{
            return new HitCircle(x, y, time, type, hitSound, hitSample, approachRate, circleSize,
                    comboNumber, comboSetIndex, colorString, comboEnd,
                    generateCircleSfxFilenames(hitSound, hitSample));
        }
    }

}
