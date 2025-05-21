package beat.osu.beatosu.model;

import lombok.Data;

@Data
public class TimingPoint {
    private int time;                // In milliseconds
    private double beatLength;      // In ms (positive for BPM, negative for SV)
    private int meter;              // Beats per measure
    private int sampleSet;          // 0 = default, 1 = normal, 2 = soft, 3 = drum
    private int sampleIndex;        // Custom sample index (0 = default)
    private int volume;             // Volume percentage (0–100)
    private boolean inherited;      // true = green (SV), false = red (BPM)
    private int effects;

    public TimingPoint(String line) {
        String[] parts = line.split(",");
        this.time = Integer.parseInt(parts[0].trim());
        this.beatLength = Double.parseDouble(parts[1].trim());
        this.meter = Integer.parseInt(parts[2].trim());
        this.sampleSet = Integer.parseInt(parts[3].trim());
        this.sampleIndex = Integer.parseInt(parts[4].trim());
        this.volume = Integer.parseInt(parts[5].trim());
        this.inherited = Integer.parseInt(parts[6].trim()) == 0; // 0 = inherited (green)
        this.effects = Integer.parseInt(parts[7].trim());
    }
}
