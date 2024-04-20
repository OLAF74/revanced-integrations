package app.revanced.integrations.youtube.patches.components;

import static app.revanced.integrations.youtube.shared.NavigationBar.NavigationButton;

import androidx.annotation.Nullable;

import app.revanced.integrations.shared.patches.components.ByteArrayFilterGroup;
import app.revanced.integrations.shared.patches.components.Filter;
import app.revanced.integrations.shared.patches.components.StringFilterGroup;
import app.revanced.integrations.shared.settings.BooleanSetting;
import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.youtube.shared.RootView;

@SuppressWarnings("unused")
public final class ShortsShelfFilter extends Filter {
    private static final String SHORTS_SHELF_HEADER_CONVERSION_CONTEXT = "horizontalCollectionSwipeProtector=null";
    private static final String SHELF_HEADER_PATH = "shelf_header.eml";
    private final StringFilterGroup shortsCompactFeedVideoPath;
    private final ByteArrayFilterGroup shortsCompactFeedVideoBuffer;
    private final StringFilterGroup shelfHeader;

    public ShortsShelfFilter() {
        BooleanSetting hideShortsShelf = Settings.HIDE_SHORTS_SHELF;

        // Feed Shorts shelf header.
        // Use a different filter group for this pattern, as it requires an additional check after matching.
        shelfHeader = new StringFilterGroup(
                hideShortsShelf,
                SHELF_HEADER_PATH
        );

        final StringFilterGroup shorts = new StringFilterGroup(
                hideShortsShelf,
                "shorts_shelf",
                "inline_shorts",
                "shorts_grid",
                "shorts_video_cell"
        );

        addIdentifierCallbacks(shelfHeader, shorts);

        // Shorts that appear in the feed/search when the device is using tablet layout.
        shortsCompactFeedVideoPath = new StringFilterGroup(
                hideShortsShelf,
                "compact_video.eml"
        );

        // Filter out items that use the 'frame0' thumbnail.
        // This is a valid thumbnail for both regular videos and Shorts,
        // but it appears these thumbnails are used only for Shorts.
        shortsCompactFeedVideoBuffer = new ByteArrayFilterGroup(
                hideShortsShelf,
                "/frame0.jpg"
        );

        addPathCallbacks(shortsCompactFeedVideoPath);
    }

    @Override
    public boolean isFiltered(String path, @Nullable String identifier, String allValue, byte[] protobufBufferArray,
                       StringFilterGroup matchedGroup, FilterContentType contentType, int contentIndex) {
        if (!shouldHideShortsFeedItems())
            return false;

        if (matchedGroup == shortsCompactFeedVideoPath) {
            if (contentIndex == 0 && shortsCompactFeedVideoBuffer.check(protobufBufferArray).isFiltered())
                return super.isFiltered(path, identifier, allValue, protobufBufferArray, matchedGroup, contentType, contentIndex);
            return false;
        } else if (matchedGroup == shelfHeader) {
            // Check ConversationContext to not hide shelf header in channel profile
            // This value does not exist in the shelf header in the channel profile
            if (!allValue.contains(SHORTS_SHELF_HEADER_CONVERSION_CONTEXT))
                return false;
        }

        // Super class handles logging.
        return super.isFiltered(path, identifier, allValue, protobufBufferArray, matchedGroup, contentType, contentIndex);
    }

    private static boolean shouldHideShortsFeedItems() {
        final boolean hideHomeAndRelatedVideos = Settings.HIDE_SHORTS_SHELF_HOME_RELATED_VIDEOS.get();
        final boolean hideSubscriptions = Settings.HIDE_SHORTS_SHELF_SUBSCRIPTIONS.get();
        final boolean hideSearch = Settings.HIDE_SHORTS_SHELF_SEARCH.get();
        final boolean hideHistory = Settings.HIDE_SHORTS_SHELF_HISTORY.get();

        if (hideHomeAndRelatedVideos && hideSubscriptions && hideSearch && hideHistory) {
            // Shorts suggestions can load in the background if a video is opened and
            // then immediately minimized before any suggestions are loaded.
            // In this state the player type will show minimized, which makes it not possible to
            // distinguish between Shorts suggestions loading in the player and between
            // scrolling thru search/home/subscription tabs while a player is minimized.
            //
            // To avoid this situation for users that never want to show Shorts (all hide Shorts options are enabled)
            // then hide all Shorts everywhere including the Library history and Library playlists.
            return true;
        }

        // Must check player type first, as search bar can be active behind the player.
        if (RootView.isPlayerActive()) {
            // For now, consider the under video results the same as the home feed.
            return hideHomeAndRelatedVideos;
        }

        // Must check second, as search can be from any tab.
        if (RootView.isSearchBarActive()) {
            return hideSearch;
        }

        // Avoid checking navigation button status if all other Shorts should show.
        if (!hideHomeAndRelatedVideos && !hideSubscriptions && !hideHistory) {
            return false;
        }

        NavigationButton selectedNavButton = NavigationButton.getSelectedNavigationButton();
        if (selectedNavButton == null) {
            return hideHomeAndRelatedVideos; // Unknown tab, treat the same as home.
        }
        if (selectedNavButton == NavigationButton.HOME) {
            return hideHomeAndRelatedVideos;
        }
        if (selectedNavButton == NavigationButton.SUBSCRIPTIONS) {
            return hideSubscriptions;
        }
        if (selectedNavButton.isLibraryOrYouTab()) {
            return hideHistory;
        }

        return false;
    }
}