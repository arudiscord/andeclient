package pw.aru.libs.andeclient.util;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pw.aru.libs.andeclient.entities.AndesiteNode;
import pw.aru.libs.andeclient.entities.AudioLoadResult;
import pw.aru.libs.andeclient.entities.internal.*;
import pw.aru.libs.andeclient.entities.player.DefaultFilters;
import pw.aru.libs.andeclient.entities.player.PlayerFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AndesiteUtil {
    private static final Logger logger = LoggerFactory.getLogger(AndesiteUtil.class);

    public static AndesiteNode.Info nodeInfo(JsonObject json) {
        return ActualInfo.builder()
            .version(json.getString("version"))
            .versionMajor(json.getString("versionMajor"))
            .versionMinor(json.getString("versionMinor"))
            .versionRevision(json.getString("versionRevision"))
            .versionCommit(json.getString("versionCommit"))
            .versionBuild(json.getLong("versionBuild"))
            .nodeRegion(json.getString("nodeRegion"))
            .nodeId(json.getString("nodeId"))
            .enabledSources(toStringList(json.getArray("enabledSources")))
            .loadedPlugins(toStringList(json.getArray("loadedPlugins")))
            .build();
    }

    public static AndesiteNode.Stats nodeStats(AndesiteNode node, JsonObject json) {
        final JsonObject jsonPlayers = json.getObject("players");
        final JsonObject jsonCpu = json.getObject("cpu");
        final JsonObject jsonFrames = json.getObject("frameStats");

        return ActualStats.builder()
            .node(node)
            .raw(json)
            .players(jsonPlayers.getInt("total"))
            .playingPlayers(jsonPlayers.getInt("playing"))
            .uptime(json.getObject("runtime").getLong("uptime"))
            .systemLoad(jsonCpu == null ? 0 : jsonCpu.getDouble("system"))
            .andesiteLoad(jsonCpu == null ? 0 : jsonCpu.getDouble("andesite"))
            .sentFrames(jsonFrames == null ? 0 : jsonFrames.getLong("sent"))
            .nulledFrames(jsonFrames == null ? 0 : jsonFrames.getLong("nulled"))
            .deficitFrames(jsonFrames == null ? 0 : jsonFrames.getLong("deficit"))
            .build();
    }

    public static AudioLoadResult audioLoadResult(JsonObject json) {
        switch (json.getString("loadType")) {
            case "TRACK_LOADED": {
                return ActualTrack.builder()
                    .track(
                        AudioTrackUtil.fromString(json.getArray("tracks").getObject(0).getString("track"))
                    )
                    .build();
            }
            case "PLAYLIST_LOADED":
            case "SEARCH_RESULT": {
                List<AudioTrack> tracks = json.getArray("tracks").stream()
                    .filter(track -> track instanceof JsonObject)
                    .map(track -> (JsonObject) track)
                    .map(jsonTrack -> AudioTrackUtil.fromString(jsonTrack.getString("track")))
                    .collect(Collectors.toUnmodifiableList());

                final JsonObject info = json.getObject("playlistInfo");
                final String name = info.getString("name");
                final int selected = info.getInt("selectedTrack", -1);

                return ActualPlaylist.builder()
                    .searchResults(json.getString("loadType").equals("SEARCH_RESULT"))
                    .tracks(tracks)
                    .playlistName(name)
                    .selectedIndex(selected)
                    .selectedTrack(selected < 0 ? null : tracks.get(selected))
                    .build();
            }
            case "LOAD_FAILED": {
                return ActualFailed.builder()
                    .cause(json.getObject("cause").getString("message"))
                    .severity(FriendlyException.Severity.valueOf(json.getString("severity")))
                    .build();
            }
            case "NO_MATCHES": {
                return AudioLoadResult.NO_MATCHES;
            }
            default: {
                logger.warn("unknown loadType {} | raw json is {}", json.getString("loadType"), json);
                return AudioLoadResult.UNKNOWN;
            }
        }
    }

    private static List<String> toStringList(JsonArray array) {
        return array.stream()
            .map(Object::toString)
            .collect(Collectors.toUnmodifiableList());
    }

    public static Set<PlayerFilter> playerFilters(JsonObject jsonFilters) {
        ArrayList<PlayerFilter> filters = new ArrayList<>();

        for (String filter : jsonFilters.keySet()) {
            final JsonObject jsonFilter = jsonFilters.getObject(filter);
            if (jsonFilter.getBoolean("enabled")) {
                switch (filter) {
                    case "equalizer": {
                        final DefaultFilters.Equalizer equalizer = DefaultFilters.equalizer();
                        final JsonArray bands = jsonFilter.getArray("bands");
                        for (int band = 0; band < bands.size(); band++) {
                            final float gain = bands.getFloat(band);
                            if (gain != 0f) equalizer.withBand(band, gain);
                        }
                        filters.add(equalizer);
                        break;
                    }
                    case "karaoke": {
                        filters.add(
                            DefaultFilters.karaoke()
                                .level(jsonFilter.getFloat("level"))
                                .monoLevel(jsonFilter.getFloat("monoLevel"))
                                .filterBand(jsonFilter.getFloat("filterBand"))
                                .filterWidth(jsonFilter.getFloat("filterWidth"))
                                .create()
                        );
                        break;
                    }
                    case "timescale": {
                        filters.add(
                            DefaultFilters.timescale()
                                .speed(jsonFilter.getFloat("speed"))
                                .pitch(jsonFilter.getFloat("pitch"))
                                .rate(jsonFilter.getFloat("rate"))
                                .create()
                        );
                        break;
                    }
                    case "tremolo": {
                        filters.add(
                            DefaultFilters.tremolo()
                                .frequency(jsonFilter.getFloat("frequency"))
                                .depth(jsonFilter.getFloat("depth"))
                                .create()
                        );
                        break;
                    }
                    case "vibrato": {
                        filters.add(
                            DefaultFilters.vibrato()
                                .frequency(jsonFilter.getFloat("frequency"))
                                .depth(jsonFilter.getFloat("depth"))
                                .create()
                        );
                        break;
                    }
                    case "volume": {
                        filters.add(DefaultFilters.volume(jsonFilter.getFloat("volume")));
                        break;
                    }
                    default: {
                        filters.add(new PlayerFilter.Raw(filter, jsonFilter));
                        break;
                    }
                }
            }
        }

        return Set.copyOf(filters);
    }
}
