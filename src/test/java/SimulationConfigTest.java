 import org.junit.jupiter.api.Test;
    import simu.model.SimulationConfig;

    import static org.junit.jupiter.api.Assertions.*;

    class SimulationConfigTest {

        @Test
        void simulationConfigObjectCanBeCreated() {
            SimulationConfig config = new SimulationConfig();

            assertNotNull(config);
        }
    }

