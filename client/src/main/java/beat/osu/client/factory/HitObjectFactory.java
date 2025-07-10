package beat.osu.client.factory;

import beat.osu.client.interfaces.game.HitObjectListener;
import beat.osu.client.model.*;
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

    private static String decodeType(int type) {
        boolean isHitCircle = (type & 1) != 0;
        boolean isSlider = (type & 2) != 0;
        // boolean isNewCombo = (type & 4) != 0;
        boolean isSpinner = (type & 8) != 0;
        boolean isHold = (type & 128) != 0;

        if (isHitCircle)
            return "circle";
        else if (isSlider)
            return "slider";
        return "spinner";
    }

    private static ArrayList<String> generateCircleSfxFilenames(int hitSound, String hitSample, int hitTime) {
        String[] parts = hitSample.split(":");
        int normalSetId = parts.length > 0 && !parts[0].isEmpty() ? Integer.parseInt(parts[0]) : 0;
        int additionSetId = parts.length > 1 && !parts[1].isEmpty() ? Integer.parseInt(parts[1]) : 0;
        int index = parts.length > 2 && !parts[2].isEmpty() ? Integer.parseInt(parts[2]) : 0;
        String filename = parts.length > 4 ? parts[4] : "";

        if (index == 0) {
            TimingPoint activeTP = getActiveTimingPointAt(hitTime);
            if (activeTP != null) {
                index = activeTP.getSampleIndex();
            }
        }

        ArrayList<String> sounds = new ArrayList<>();
        if (!filename.isEmpty()) {
            sounds.add(filename);
            return sounds;
        }

        String normalSet = getSampleSetNameForNormal(normalSetId, hitTime);
        String additionSet = getSampleSetNameForAddition(additionSetId, normalSet);

        // System.out.println(normalSet);
        // Normal sound (bit 1 or hitSound 0)
        if (hitSound == 0 || (hitSound & 1) != 0) {
            sounds.add(buildCircleFilename(normalSet, "hitnormal", index));
        }
        // Addition sounds use additionSet regardless of normalSet
        if ((hitSound & 2) != 0) {
            sounds.add(buildCircleFilename(additionSet, "hitwhistle", index));
        }
        if ((hitSound & 4) != 0) {
            sounds.add(buildCircleFilename(additionSet, "hitfinish", index));
        }
        if ((hitSound & 8) != 0) {
            sounds.add(buildCircleFilename(additionSet, "hitclap", index));
        }

        // System.out.println("total sounds: " + sounds.size());
        return sounds;
    }

    public static ArrayList<ArrayList<String>> generateSliderEdgeSfxFilenames(
            String edgeSounds, String edgeSets, int hitTime) {
        String[] soundsArray = edgeSounds.isEmpty() ? new String[0] : edgeSounds.split("\\|");
        String[] setsArray = edgeSets.isEmpty() ? new String[0] : edgeSets.split("\\|");

        ArrayList<ArrayList<String>> allEdgeSounds = new ArrayList<>();

        // Process each edge
        int edgeCount = Math.max(soundsArray.length, setsArray.length);
        for (int i = 0; i < edgeCount; i++) {
            // Get hitsound for this edge (default to 0 if not specified)
            int hitSound = 0;
            if (i < soundsArray.length && !soundsArray[i].isEmpty()) {
                hitSound = Integer.parseInt(soundsArray[i]);
            }

            // Get sample sets for this edge (default to "0:0" if not specified)
            String sampleSet = "0:0";
            if (i < setsArray.length && !setsArray[i].isEmpty()) {
                sampleSet = setsArray[i];
            }

            // Generate sounds for this edge using similar logic to circle hitsounds
            ArrayList<String> edgeSfx = generateEdgeSfxForSingleEdge(hitSound, sampleSet, hitTime);
            allEdgeSounds.add(edgeSfx);
        }

        return allEdgeSounds;
    }

    private static ArrayList<String> generateEdgeSfxForSingleEdge(
            int hitSound, String sampleSet, int hitTime) {
        String[] parts = sampleSet.split(":");
        int normalSetId = parts.length > 0 && !parts[0].isEmpty() ? Integer.parseInt(parts[0]) : 0;
        int additionSetId = parts.length > 1 && !parts[1].isEmpty() ? Integer.parseInt(parts[1]) : 0;
        int index = 0;

        if (index == 0) {
            TimingPoint activeTP = getActiveTimingPointAt(hitTime);
            if (activeTP != null) {
                index = activeTP.getSampleIndex();
            }
        }

        ArrayList<String> sounds = new ArrayList<>();
        String normalSet = getSampleSetNameForNormal(normalSetId, hitTime);
        String additionSet = getSampleSetNameForAddition(additionSetId, normalSet);

        // Normal sound (bit 1 or hitSound 0)
        if (hitSound == 0 || (hitSound & 1) != 0) {
            sounds.add(buildCircleFilename(normalSet, "hitnormal", index));
        }
        // Addition sounds use additionSet regardless of normalSet
        if ((hitSound & 2) != 0) {
            sounds.add(buildCircleFilename(additionSet, "hitwhistle", index));
        }
        if ((hitSound & 4) != 0) {
            sounds.add(buildCircleFilename(additionSet, "hitfinish", index));
        }
        if ((hitSound & 8) != 0) {
            sounds.add(buildCircleFilename(additionSet, "hitclap", index));
        }

        return sounds;
    }

    private static String getSampleSetName(int id) {
        switch (id) {
            case 1:
                return "normal";
            case 2:
                return "soft";
            case 3:
                return "drum";
            default:
                return OsuParser.getGeneralSampleSet();
        }
    }

    private static String getSampleSetNameForNormal(int id, int hitTime) {
        switch (id) {
            case 1:
                return "normal";
            case 2:
                return "soft";
            case 3:
                return "drum";
            case 0:
                // For normal sounds, use the timing point's sample set
                TimingPoint activeTP = getActiveTimingPointAt(hitTime);
                if (activeTP != null) {
                    System.out.println("using active timing point sample set: " + activeTP.getSampleSet());
                    return getSampleSetName(activeTP.getSampleSet());
                }
                return OsuParser.getGeneralSampleSet();
            default:
                return OsuParser.getGeneralSampleSet();
        }
    }

    private static String getSampleSetNameForAddition(int id, String normalSet) {
        switch (id) {
            case 1:
                return "normal";
            case 2:
                return "soft";
            case 3:
                return "drum";
            case 0:
                // For additions, use the normal sound's sample set
                return normalSet;
            default:
                return OsuParser.getGeneralSampleSet();
        }
    }

    private static TimingPoint getActiveTimingPointAt(int time) {
        TimingPoint activeTP = null;
        for (TimingPoint tp : OsuParser.getTimingPointsList()) {
            if (tp.getTime() <= time) {
                activeTP = tp;
            } else {
                break;
            }
        }
        return activeTP;
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
            boolean comboEnd, HitObjectListener listener) {
        // Circle (length 6) => 382,305,6867,1,2,3:2:0:0:
        // Slider =>
        // 59,124,2279,6,0,P|116:91|220:132,1,171.73332756836,2|0,0:2|0:2,0:0:0:0:
        Map<String, String> colours = OsuParser.getColours();
        String key = "Combo" + (comboSetIndex + 1); // +1 because osu uses 1-based keys
        String colorString = colours.getOrDefault(key, "64,64,64"); // Default to white if not found
        // System.out.println("key: " + key);
        // System.out.println("color: " + colorString);

        String[] parts = data.split(",");
        // System.out.println(parts.length);

        int x = Integer.parseInt(parts[0]);
        int y = Integer.parseInt(parts[1]);
        int time = Integer.parseInt(parts[2]);
        int type = Integer.parseInt(parts[3]);
        int hitSound = Integer.parseInt(parts[4]);
        int spinnerEndTime = time;

        String objectParams = "";
        String hitSample = parts[parts.length - 1];
        String hitType = decodeType(type);

        if (parts.length == 6) {// circle
            hitSample = parts[5];
        } else if (parts.length == 7) {// spinner
            spinnerEndTime = Integer.parseInt(parts[5]);
        } else {// slider
            objectParams = String.join(",", Arrays.copyOfRange(parts, 5, parts.length - 1));
        }

        // check if hitSample is invalid
        if (!hitSample.contains(":")) {
            hitSample = "0:0:0:0:";
            objectParams = String.join(",", Arrays.copyOfRange(parts, 5, parts.length));
        }
        // System.out.println(objectParams);
        // for (String sfx : generateCircleSfxFilenames(hitSound, hitSample)) {
        // System.out.println("sfx: " + sfx);
        // }

        double approachRate = selectedBeatmap.getApproachRate();
        double circleSize = selectedBeatmap.getCircleSize();
        double sliderMultiplier = selectedBeatmap.getSliderMultiplier();
        double overallDifficulty = selectedBeatmap.getOverallDifficulty();
        double sliderTickRate = selectedBeatmap.getSliderTickRate();
        // return new HitCircle(x, y, time, type, hitSound, hitSample, approachRate);
        if (hitType.equals("circle")) {
            return new HitCircle(x, y, time, type, hitSound, hitSample, approachRate, circleSize,
                    comboNumber, comboSetIndex, colorString, comboEnd,
                    generateCircleSfxFilenames(hitSound, hitSample, time));
        } else if (hitType.equals("slider")) {
            return new HitSlider(x, y, time, type, hitSound, objectParams, hitSample,
                    approachRate, circleSize, sliderMultiplier, sliderTickRate,
                    comboNumber, comboSetIndex, colorString, comboEnd,
                    generateCircleSfxFilenames(hitSound, hitSample, time), listener);
        } else {
            return new HitSpinner(x, y, time, type, hitSound, hitSample,
                    spinnerEndTime, approachRate, circleSize, overallDifficulty,
                    comboNumber, comboSetIndex, colorString, comboEnd,
                    generateCircleSfxFilenames(hitSound, hitSample, time), listener);
        }
    }

}
