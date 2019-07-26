package pw.aru.libs.andeclient.entities.player;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import pw.aru.libs.andeclient.entities.AndePlayer;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletionStage;

/**
 * Control interface of an AndePlayer.
 * Sends control actions to the player's andesite node.
 */
@SuppressWarnings("UnusedReturnValue")
public interface PlayerControls {
    @Nonnull
    AndePlayer player();

    @Nonnull
    @CheckReturnValue
    Play play();

    @Nonnull
    @CheckReturnValue
    Action pause();

    @Nonnull
    @CheckReturnValue
    Action resume();

    @Nonnull
    @CheckReturnValue
    Action volume(int volume);

    @Nonnull
    @CheckReturnValue
    Mixer mixer();

    @Nonnull
    @CheckReturnValue
    Action filters(PlayerFilter... filters);

    @Nonnull
    @CheckReturnValue
    Action seek(long position);

    @Nonnull
    @CheckReturnValue
    Action stop();

    interface Action<T> {
        @Nonnull
        @Deprecated
        PlayerControls execute();

        @Nonnull
        CompletionStage<T> submit();
    }

    interface Play extends Action<Void> {
        @Nonnull
        @CheckReturnValue
        Play track(@Nonnull String trackString);

        @Nonnull
        @CheckReturnValue
        Play track(@Nonnull AudioTrack track);

        @Nonnull
        @CheckReturnValue
        Play start(@Nullable Long timestamp);

        @Nonnull
        @CheckReturnValue
        Play end(@Nullable Long timestamp);

        @Nonnull
        @CheckReturnValue
        Play noReplace();

        @Nonnull
        @CheckReturnValue
        Play replacing();

        @Nonnull
        @CheckReturnValue
        Play pause(@Nullable Boolean isPaused);

        @Nonnull
        @CheckReturnValue
        Play volume(@Nullable Integer volume);
    }

    interface Mixer extends Action<Void> {
        @Nonnull
        @CheckReturnValue
        Mixer enable();

        @Nonnull
        @CheckReturnValue
        Mixer disable();
    }
}
