package pw.aru.libs.andeclient.entities.player;

import org.immutables.value.Value;
import org.json.JSONArray;
import org.json.JSONObject;
import pw.aru.libs.andeclient.annotations.Filter;
import pw.aru.libs.andeclient.entities.player.internal.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Map;

/**
 * This class contains all Andesite player filters.
 */
public class DefaultFilters {
    private DefaultFilters() {}

    public static Equalizer equalizer() {
        return new Equalizer();
    }

    public static KaraokeFilter.Builder karaoke() {
        return KaraokeFilter.builder();
    }

    public static TimescaleFilter.Builder timescale() {
        return TimescaleFilter.builder();
    }

    public static TremoloFilter.Builder tremolo() {
        return TremoloFilter.builder();
    }

    public static VibratoFilter.Builder vibrato() {
        return VibratoFilter.builder();
    }

    public static PlayerFilter volume(Float volume) {
        return VolumeFilter.of(volume);
    }

    public static PlayerFilter volume(int volume) {
        return VolumeFilter.of(((float) volume));
    }

    @Filter
    public static class Equalizer extends PlayerFilter {
        private Float[] bands = new Float[15];

        public Equalizer withBand(int band, Float gain) {
            if (band < 0 || band > 14) throw new IllegalArgumentException("band out of range [0, 14]");
            if (gain != null && (gain < -0.25f || gain > 1.0f)) throw new IllegalArgumentException("band out of range [-0.25, 1.0]");
            bands[band] = gain;
            return this;
        }

        public Float[] bands() {
            return bands.clone();
        }

        @Override
        public int hashCode() {
            return Arrays.deepHashCode(bands);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            return obj instanceof Equalizer && Arrays.equals(bands, ((Equalizer) obj).bands);
        }

        @Nonnull
        @Override
        public Map.Entry<String, JSONObject> updatePayload() {
            final JSONArray array = new JSONArray();
            for (int band = 0; band < bands.length; band++) {
                final Float gain = bands[band];
                if (gain != null) {
                    array.put(new JSONObject().put("band", band).put("gain", gain));
                }
            }

            return Map.entry("equalizer", new JSONObject().put("bands", array));
        }
    }

    @Value.Immutable
    @Filter
    public abstract static class Karaoke extends PlayerFilter {
        @Nullable
        @Value.Default
        public Float level() {
            return 1f;
        }

        @Nullable
        @Value.Default
        public Float monoLevel() {
            return 1f;
        }

        @Nullable
        @Value.Default
        public Float filterBand() {
            return 220f;
        }

        @Nullable
        @Value.Default
        public Float filterWidth() {
            return 110f;
        }

        @Nonnull
        @Override
        public Map.Entry<String, JSONObject> updatePayload() {
            return Map.entry(
                "karaoke", new JSONObject()
                    .put("level", level())
                    .put("monoLevel", monoLevel())
                    .put("filterBand", filterBand())
                    .put("filterWidth", filterWidth())
            );
        }
    }


    @Value.Immutable
    @Filter
    public abstract static class Timescale extends PlayerFilter {
        @Nullable
        @Value.Default
        public Float speed() {
            return 1f;
        }

        @Nullable
        @Value.Default
        public Float pitch() {
            return 1f;
        }

        @Nullable
        @Value.Default
        public Float rate() {
            return 1f;
        }

        @Value.Check
        protected void check() {
            final var speed = speed();
            if (speed != null && speed <= 0)
                throw new IllegalArgumentException("speed out of range (> 0)");

            final var pitch = pitch();
            if (pitch != null && pitch <= 0)
                throw new IllegalArgumentException("pitch out of range (> 0)");

            final var rate = rate();
            if (rate != null && rate <= 0)
                throw new IllegalArgumentException("rate out of range (> 0)");

        }

        @Nonnull
        @Override
        public Map.Entry<String, JSONObject> updatePayload() {
            return Map.entry(
                "timescale", new JSONObject()
                    .put("speed", speed())
                    .put("pitch", pitch())
                    .put("rate", rate())
            );
        }
    }

    @Value.Immutable
    @Filter
    public abstract static class Tremolo extends PlayerFilter {
        @Nullable
        @Value.Default
        public Float frequency() {
            return 2f;
        }

        @Nullable
        @Value.Default
        public Float depth() {
            return 0.5f;
        }

        @Value.Check
        protected void check() {
            final var frequency = frequency();
            if (frequency != null && frequency <= 0) throw new IllegalArgumentException("frequency out of range (> 0)");

            final var depth = depth();
            if (depth != null && (depth <= 0 || depth > 1)) throw new IllegalArgumentException("depth out of range (0 < depth <= 1)");
        }

        @Nonnull
        @Override
        public Map.Entry<String, JSONObject> updatePayload() {
            return Map.entry(
                "tremolo", new JSONObject()
                    .put("frequency", frequency())
                    .put("depth", depth())
            );
        }
    }

    @Value.Immutable
    @Filter
    public abstract static class Vibrato extends PlayerFilter {
        @Nullable
        @Value.Default
        public Float frequency() {
            return 2f;
        }

        @Nullable
        @Value.Default
        public Float depth() {
            return 0.5f;
        }

        @Value.Check
        protected void check() {
            final var frequency = frequency();
            if (frequency != null && (frequency <= 0 || frequency > 14)) throw new IllegalArgumentException("frequency out of range (0 < depth <= 14)");

            final var depth = depth();
            if (depth != null && (depth <= 0 || depth > 1)) throw new IllegalArgumentException("depth out of range (0 < depth <= 1)");
        }

        @Nonnull
        @Override
        public Map.Entry<String, JSONObject> updatePayload() {
            return Map.entry(
                "vibrato", new JSONObject()
                    .put("frequency", frequency())
                    .put("depth", depth())
            );
        }
    }

    @Value.Immutable
    @Filter
    public abstract static class Volume extends PlayerFilter {
        @Nullable
        public abstract Float value();

        @Nonnull
        @Override
        public Map.Entry<String, JSONObject> updatePayload() {
            return Map.entry("volume", new JSONObject().put("volume", value()));
        }
    }
}
