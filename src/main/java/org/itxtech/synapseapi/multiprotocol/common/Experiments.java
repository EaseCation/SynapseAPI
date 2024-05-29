package org.itxtech.synapseapi.multiprotocol.common;

public class Experiments {
    public static final Experiment[] EMPTY = new Experiment[0];
    public static final Experiments NONE = new Experiments(EMPTY);

    public final Experiment[] experiments;
    public final boolean hasPreviouslyUsedExperiments;

    public Experiments(Experiment... experiments) {
        this.experiments = experiments;
        hasPreviouslyUsedExperiments = experiments.length != 0;
    }

    public record Experiment(String name, boolean enable) {
    }
}
