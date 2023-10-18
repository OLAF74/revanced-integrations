package app.revanced.integrations.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

public final class ByteTrieSearch extends TrieSearch<byte[]> {

    public ByteTrieSearch() {
        super(new ByteTrieNode());
    }

    @Override
    public void addPattern(@NonNull byte[] pattern) {
        super.addPattern(pattern, pattern.length, null);
    }

    @Override
    public void addPattern(@NonNull byte[] pattern, @NonNull TriePatternMatchedCallback<byte[]> callback) {
        super.addPattern(pattern, pattern.length, Objects.requireNonNull(callback));
    }

    @Override
    public boolean matches(@NonNull byte[] textToSearch, int startIndex, int endIndex, @Nullable Object callbackParameter) {
        return super.matches(textToSearch, textToSearch.length, startIndex, endIndex, callbackParameter);
    }

    @Override
    public boolean matches(@NonNull byte[] textToSearch, int startIndex) {
        return matches(textToSearch, startIndex, textToSearch.length, null);
    }

    @Override
    public boolean matches(@NonNull byte[] textToSearch, @Nullable Object callbackParameter) {
        return matches(textToSearch, 0, textToSearch.length, callbackParameter);
    }

    private static final class ByteTrieNode extends TrieNode<byte[]> {
        ByteTrieNode() {
            super();
        }

        ByteTrieNode(char nodeCharacterValue) {
            super(nodeCharacterValue);
        }

        @Override
        TrieNode<byte[]> createNode(char nodeCharacterValue) {
            return new ByteTrieNode(nodeCharacterValue);
        }

        @Override
        char getCharValue(byte[] text, int index) {
            return (char) text[index];
        }
    }

}
