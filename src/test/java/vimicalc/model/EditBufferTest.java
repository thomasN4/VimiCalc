package vimicalc.model;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link EditBuffer}: caret-aware insertion, deletion, kill
 * operations, and movement, including clamping at both ends of the text.
 */
class EditBufferTest {

    @Nested
    class Construction {

        @Test
        void emptyBufferStartsAtZero() {
            EditBuffer buffer = new EditBuffer();
            assertEquals("", buffer.text());
            assertEquals(0, buffer.caret());
        }

        @Test
        void initialTextPutsCaretAtEnd() {
            EditBuffer buffer = new EditBuffer("write");
            assertEquals("write", buffer.text());
            assertEquals(5, buffer.caret());
        }
    }

    @Nested
    class Insertion {

        @Test
        void insertAtEndAppends() {
            EditBuffer buffer = new EditBuffer("ab");
            buffer.insert("c");
            assertEquals("abc", buffer.text());
            assertEquals(3, buffer.caret());
        }

        @Test
        void insertMidTextSplitsAtCaret() {
            EditBuffer buffer = new EditBuffer("wq");
            buffer.left();
            buffer.insert("rite");
            assertEquals("writeq", buffer.text());
            assertEquals(5, buffer.caret());
        }

        @Test
        void insertAtStartPrepends() {
            EditBuffer buffer = new EditBuffer("q");
            buffer.home();
            buffer.insert("w");
            assertEquals("wq", buffer.text());
            assertEquals(1, buffer.caret());
        }
    }

    @Nested
    class Deletion {

        @Test
        void backspaceRemovesCharBeforeCaret() {
            EditBuffer buffer = new EditBuffer("abc");
            buffer.left();
            buffer.backspace();
            assertEquals("ac", buffer.text());
            assertEquals(1, buffer.caret());
        }

        @Test
        void backspaceAtStartIsANoOp() {
            EditBuffer buffer = new EditBuffer("abc");
            buffer.home();
            buffer.backspace();
            assertEquals("abc", buffer.text());
            assertEquals(0, buffer.caret());
        }

        @Test
        void deleteAtCaretRemovesCharUnderCaret() {
            EditBuffer buffer = new EditBuffer("abc");
            buffer.home();
            buffer.deleteAtCaret();
            assertEquals("bc", buffer.text());
            assertEquals(0, buffer.caret());
        }

        @Test
        void deleteAtCaretAtEndIsANoOp() {
            EditBuffer buffer = new EditBuffer("abc");
            buffer.deleteAtCaret();
            assertEquals("abc", buffer.text());
            assertEquals(3, buffer.caret());
        }
    }

    @Nested
    class Kills {

        @Test
        void killToEndDropsEverythingAfterCaret() {
            EditBuffer buffer = new EditBuffer("cellColor red");
            buffer.home();
            for (int i = 0; i < 9; i++) buffer.right();
            buffer.killToEnd();
            assertEquals("cellColor", buffer.text());
            assertEquals(9, buffer.caret());
        }

        @Test
        void killToEndAtEndIsANoOp() {
            EditBuffer buffer = new EditBuffer("abc");
            buffer.killToEnd();
            assertEquals("abc", buffer.text());
        }

        @Test
        void killWordRemovesWhitespaceThenWord() {
            EditBuffer buffer = new EditBuffer("write  file.json");
            buffer.home();
            buffer.wordRight();
            buffer.killWord();
            assertEquals("write", buffer.text());
            assertEquals(5, buffer.caret());
        }

        @Test
        void killWordMidWordRemovesRestOfWord() {
            EditBuffer buffer = new EditBuffer("write file");
            buffer.home();
            buffer.right();
            buffer.right();
            buffer.killWord();
            assertEquals("wr file", buffer.text());
            assertEquals(2, buffer.caret());
        }

        @Test
        void killWordAtEndIsANoOp() {
            EditBuffer buffer = new EditBuffer("abc");
            buffer.killWord();
            assertEquals("abc", buffer.text());
        }
    }

    @Nested
    class Movement {

        @Test
        void leftAndRightMoveByOneChar() {
            EditBuffer buffer = new EditBuffer("ab");
            buffer.left();
            assertEquals(1, buffer.caret());
            buffer.right();
            assertEquals(2, buffer.caret());
        }

        @Test
        void leftClampsAtStart() {
            EditBuffer buffer = new EditBuffer("a");
            buffer.home();
            buffer.left();
            assertEquals(0, buffer.caret());
        }

        @Test
        void rightClampsAtEnd() {
            EditBuffer buffer = new EditBuffer("a");
            buffer.right();
            assertEquals(1, buffer.caret());
        }

        @Test
        void homeAndEndJumpToBounds() {
            EditBuffer buffer = new EditBuffer("abc");
            buffer.home();
            assertEquals(0, buffer.caret());
            buffer.end();
            assertEquals(3, buffer.caret());
        }
    }

    @Nested
    class WordMovement {

        @Test
        void wordLeftSkipsTrailingWhitespaceThenWord() {
            EditBuffer buffer = new EditBuffer("write  file.json");
            buffer.wordLeft();
            assertEquals(7, buffer.caret());
            buffer.wordLeft();
            assertEquals(0, buffer.caret());
        }

        @Test
        void wordRightSkipsLeadingWhitespaceThenWord() {
            EditBuffer buffer = new EditBuffer("write  file.json");
            buffer.home();
            buffer.wordRight();
            assertEquals(5, buffer.caret());
            buffer.wordRight();
            assertEquals(16, buffer.caret());
        }

        @Test
        void wordMovesClampAtBounds() {
            EditBuffer buffer = new EditBuffer("abc");
            buffer.wordRight();
            assertEquals(3, buffer.caret());
            buffer.wordLeft();
            buffer.wordLeft();
            assertEquals(0, buffer.caret());
        }
    }

    @Nested
    class SetText {

        @Test
        void setTextReplacesContentAndMovesCaretToEnd() {
            EditBuffer buffer = new EditBuffer("re");
            buffer.home();
            buffer.setText("resizeColumn");
            assertEquals("resizeColumn", buffer.text());
            assertEquals(12, buffer.caret());
        }
    }
}
