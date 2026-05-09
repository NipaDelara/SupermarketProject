import org.junit.jupiter.api.Test;
import simu.model.Statistics;

import static org.junit.jupiter.api.Assertions.*;

    class StatisticsTest {

        @Test
        void statisticsObjectCanBeCreated() {
            Statistics statistics = new Statistics();

            assertNotNull(statistics);
        }
    }

