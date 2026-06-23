package com.taskflow.service;

import org.springframework.stereotype.Service;

/**
 * Computes fractional positions for ordered items (lists, cards). Inserting an
 * item between two neighbours uses the midpoint of their positions, which lets a
 * move touch a single row instead of renumbering the whole column.
 *
 * <ul>
 *   <li>insert at top    → {@code before = null}</li>
 *   <li>insert at bottom → {@code after  = null}</li>
 *   <li>empty list       → both null</li>
 * </ul>
 */
@Service
public class PositionService {

    /** Spacing applied when appending to an end so positions stay well-separated. */
    public static final double STEP = 1024.0;

    /**
     * @param before position of the neighbour above the target slot, or null
     * @param after  position of the neighbour below the target slot, or null
     * @return a position strictly between the neighbours
     */
    public double between(Double before, Double after) {
        if (before == null && after == null) {
            return STEP;
        }
        if (before == null) {
            return after - STEP;
        }
        if (after == null) {
            return before + STEP;
        }
        if (before >= after) {
            throw new IllegalArgumentException(
                    "Invalid ordering: before (" + before + ") must be < after (" + after + ")");
        }
        return before + (after - before) / 2.0;
    }
}
