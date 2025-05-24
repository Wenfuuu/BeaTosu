package beat.osu.beatosu.factory;

import beat.osu.beatosu.model.Beatmap;
import beat.osu.beatosu.model.HitCircle;
import beat.osu.beatosu.model.HitObject;
import beat.osu.beatosu.model.HitSlider;
import beat.osu.beatosu.utils.OsuParser;

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

//        int comboSkip = (type >> 4) & 7;

//        System.out.println("Type: " + type);
//        System.out.println(" - Hit Circle: " + isHitCircle);
//        System.out.println(" - Slider: " + isSlider);
//        System.out.println(" - Spinner: " + isSpinner);
//        System.out.println(" - Hold Note (osu!mania): " + isHold);
//        System.out.println(" - Starts New Combo: " + isNewCombo);
//        System.out.println(" - Combo Color Skip Count: " + comboSkip);
//        System.out.println();

        if(isHitCircle) return "circle";
        else if(isSlider) return "slider";
        return "spinner";
    }

    public static HitObject createHitObject(String data, Beatmap selectedBeatmap,
                                            int comboNumber, int comboSetIndex){
        // Circle (length 6) => 382,305,6867,1,2,3:2:0:0:
        // Slider => 59,124,2279,6,0,P|116:91|220:132,1,171.73332756836,2|0,0:2|0:2,0:0:0:0:
        Map<String, String> colours = OsuParser.getColours();
        String key = "Combo" + (comboSetIndex + 1); // +1 because osu uses 1-based keys
        String color = colours.getOrDefault(key, "255,255,255"); // Default to white if not found
        System.out.println("key: " + key);
        System.out.println("color: " + color);

        String[] parts = data.split(",");
        System.out.println(parts.length);

        int x = Integer.parseInt(parts[0]);
        int y = Integer.parseInt(parts[1]);
        int time = Integer.parseInt(parts[2]);
        int type = Integer.parseInt(parts[3]);
        int hitSound = Integer.parseInt(parts[4]);

        String objectParams = "";
        String hitSample = "0:0:0:0:";

        String hitType = decodeType(type);

        if(parts.length == 6){// circle
            hitSample = parts[5];
        }else if(parts.length == 7){// spinner

        }else{// slider
            objectParams = String.join(",", Arrays.copyOfRange(parts, 5, parts.length-1));
            hitSample = parts[parts.length-1];
        }

//        System.out.println(objectParams);
//        System.out.println(hitSample);
        double approachRate = selectedBeatmap.getApproachRate();
        double circleSize = selectedBeatmap.getCircleSize();
//        return new HitCircle(x, y, time, type, hitSound, hitSample, approachRate);
        if(hitType.equals("circle")){
            return new HitCircle(x, y, time, type, hitSound, hitSample, approachRate, circleSize,
                    comboNumber, comboSetIndex);
        }else if(hitType.equals("slider")){
            return new HitSlider(x, y, time, type, hitSound, objectParams, hitSample,
                    approachRate, circleSize, selectedBeatmap.getSlideMultiplier(),
                    comboNumber, comboSetIndex);
        }
        else{
            return new HitCircle(x, y, time, type, hitSound, hitSample, approachRate, circleSize,
                    comboNumber, comboSetIndex);
        }
    }

}
