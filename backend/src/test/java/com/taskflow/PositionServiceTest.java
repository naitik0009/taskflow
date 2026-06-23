package com.taskflow;

import com.taskflow.service.PositionService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PositionServiceTest {

    private final PositionService service = new PositionService();

    @Test
    void emptyListGetsBasePosition() {
        assertThat(service.between(null, null)).isEqualTo(PositionService.STEP);
    }

    @Test
    void appendingToBottomAddsStep() {
        double pos = service.between(1024.0, null);
        assertThat(pos).isEqualTo(1024.0 + PositionService.STEP);
    }

    @Test
    void insertingAtTopSubtractsStep() {
        double pos = service.between(null, 1024.0);
        assertThat(pos).isEqualTo(1024.0 - PositionService.STEP);
    }

    @Test
    void insertingBetweenTwoCardsUsesMidpoint() {
        double pos = service.between(1000.0, 2000.0);
        assertThat(pos).isEqualTo(1500.0);
        assertThat(pos).isGreaterThan(1000.0).isLessThan(2000.0);
    }

    @Test
    void repeatedMidpointInsertionsStayOrdered() {
        double low = 0.0;
        double high = 1024.0;
        // Simulate dragging a card into the same gap many times.
        for (int i = 0; i < 20; i++) {
            double mid = service.between(low, high);
            assertThat(mid).isGreaterThan(low).isLessThan(high);
            high = mid; // keep squeezing toward the top
        }
    }

    @Test
    void rejectsInvalidOrdering() {
        assertThatThrownBy(() -> service.between(2000.0, 1000.0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.between(1000.0, 1000.0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
