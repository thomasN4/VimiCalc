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

    // ── Zoom ──

    @Test
    void zoomScalesDefaultCellSizes() {
        positions.setZoom(2.0);

        int[] xs = positions.getCellAbsXs(), ys = positions.getCellAbsYs();
        assertEquals(192, xs[3] - xs[2], "column widths double at 200% zoom");
        assertEquals(48, ys[3] - ys[2], "row heights double at 200% zoom");
    }

    @Test
    void zoomKeepsTheHomeOriginUnscaled() {
        positions.setZoom(2.0);

        assertEquals(48, positions.getCellAbsXs()[1],
            "cellAbsXs[1] must stay GUTTER_W so the grid stays flush with the fixed chrome");
        assertEquals(24, positions.getCellAbsYs()[1],
            "cellAbsYs[1] must stay HEADER_H so the grid stays flush with the fixed chrome");
    }

    @Test
    void zoomScalesResizedColumnsProportionallyWithoutMutatingOffsets() {
        positions.applyOffset(new int[]{3, 31}, true);

        positions.setZoom(2.0);

        int[] xs = positions.getCellAbsXs();
        assertEquals(2 * (96 + 31), xs[4] - xs[3], "the resized column scales as (default + offset) * zoom");
        assertEquals(192, xs[3] - xs[2], "other columns scale from the default width");
        assertEquals(31, positions.getxOffsets().get(3), "the stored offset map is not mutated by zoom");
    }

    @Test
    void zoomClampsToTheAllowedRange() {
        positions.setZoom(10.0);
        assertEquals(4.0, positions.getZoom(), "zoom clamps to the maximum");

        positions.setZoom(0.01);
        assertEquals(0.25, positions.getZoom(), "zoom clamps to the minimum");
    }

    @Test
    void zoomStepsProduceUniformColumnWidths() {
        positions.setZoom(1.1);

        int[] xs = positions.getCellAbsXs();
        for (int xC = 2; xC <= 5; xC++)
            assertEquals(106, xs[xC + 1] - xs[xC],
                "per-increment rounding must not accumulate drift across columns");
    }

    @Test
    void resetZoomRestoresTheExactOriginalLayout() {
        int[] xs = positions.getCellAbsXs().clone(), ys = positions.getCellAbsYs().clone();
        int firstXC = positions.getFirstXC(), lastXC = positions.getLastXC();

        positions.setZoom(1.7);
        positions.setZoom(1.0);

        assertArrayEquals(xs, positions.getCellAbsXs());
        assertArrayEquals(ys, positions.getCellAbsYs());
        assertEquals(firstXC, positions.getFirstXC());
        assertEquals(lastXC, positions.getLastXC());
    }
}
