package vimicalc.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Positions} layout generation, in particular the
 * camera-less re-layout entry points ({@link Positions#regenerate()} and
 * {@link Positions#applyOffset(int[], boolean)}) that the model layer and
 * {@code :resCol}/{@code :resRow} rely on (issue #30).
 */
class PositionsTest {

    private Positions positions;

    @BeforeEach
    void setUp() {
        positions = new Positions(852, 552, 96, 24, new HashMap<>(), new HashMap<>());
        positions.generate(48, 24);
    }

    @Test
    void regenerateReproducesTheLastGeneratedLayout() {
        int[] xs = positions.getCellAbsXs().clone(), ys = positions.getCellAbsYs().clone();
        int firstXC = positions.getFirstXC(), lastXC = positions.getLastXC();
        int firstYC = positions.getFirstYC(), lastYC = positions.getLastYC();

        positions.regenerate();

        assertArrayEquals(xs, positions.getCellAbsXs());
        assertArrayEquals(ys, positions.getCellAbsYs());
        assertEquals(firstXC, positions.getFirstXC());
        assertEquals(lastXC, positions.getLastXC());
        assertEquals(firstYC, positions.getFirstYC());
        assertEquals(lastYC, positions.getLastYC());
    }

    @Test
    void regenerateTracksTheEdgesOfTheLastGenerate() {
        positions.generate(144, 72);
        int firstXC = positions.getFirstXC(), firstYC = positions.getFirstYC();

        positions.regenerate();

        assertEquals(firstXC, positions.getFirstXC(),
            "regenerate must re-run layout at the most recent viewport edges");
        assertEquals(firstYC, positions.getFirstYC());
    }

    @Test
    void applyOffsetWidensOnlyTheTargetColumn() {
        int firstXC = positions.getFirstXC();

        positions.applyOffset(new int[]{3, 31}, true);

        int[] xs = positions.getCellAbsXs();
        assertEquals(96, xs[3] - xs[2], "columns before the target keep the default width");
        assertEquals(96 + 31, xs[4] - xs[3], "the target column gains exactly the offset");
        assertEquals(96, xs[5] - xs[4], "columns after the target keep the default width");
        assertEquals(firstXC, positions.getFirstXC(),
            "resizing must relayout at the previously generated edges");
    }

    @Test
    void applyOffsetGrowsOnlyTheTargetRow() {
        positions.applyOffset(new int[]{2, 13}, false);

        int[] ys = positions.getCellAbsYs();
        assertEquals(24, ys[2] - ys[1], "rows before the target keep the default height");
        assertEquals(24 + 13, ys[3] - ys[2], "the target row gains exactly the offset");
        assertEquals(24, ys[4] - ys[3], "rows after the target keep the default height");
    }
}
