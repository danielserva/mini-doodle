package minidoodle;

import io.gatling.app.Gatling;

/**
 * IDE entry point for running Gatling simulations without Maven.
 * Pass -Dgatling.simulationClass=minidoodle.simulation.SchedulingSimulation
 * as a VM option to target a specific simulation, or omit it to be prompted.
 */
public final class Engine {
    private Engine() {}

    public static void main(String[] args) {
        Gatling.main(args);
    }
}
