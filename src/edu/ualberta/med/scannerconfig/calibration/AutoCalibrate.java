package edu.ualberta.med.scannerconfig.calibration;

import java.util.Date;
import java.util.Random;

import org.jgap.*;
import org.jgap.impl.DefaultConfiguration;
import org.jgap.impl.IntegerGene;

public class AutoCalibrate {
	private Random rand;
	private Configuration conf;
	private FitnessFunction fitnessFunct;
	private Gene[] sampleGenes;
	private Chromosome sampleChromosome;
	private Genotype population;

	boolean Initialized;

	public boolean isInitialized() {
		return Initialized;
	}

	public void iterateEvolution() { // Perform ~20 for success;
		population.evolve();
	}

	public IChromosome getBestChromosome() {
		return population.getFittestChromosome();
	}

	// / initialPopulation should be 10-15
	public AutoCalibrate(boolean TwainDriver, int initialPopulation) {
		try {

			rand = new Random((new Date()).getTime());
			conf = new DefaultConfiguration();
			fitnessFunct = new FitnessFunct();
			conf.setFitnessFunction(fitnessFunct);

			// Setup Genes
			if (TwainDriver) {
				sampleGenes = new Gene[7];
			} else {
				sampleGenes = new Gene[5];
			}

			sampleGenes[0] = new IntegerGene(conf, 0,
					FitnessFunct.SQUAREDEV_LIST.length - 1);
			sampleGenes[1] = new IntegerGene(conf, 0,
					FitnessFunct.THRESHOLD_LIST.length - 1);
			sampleGenes[2] = new IntegerGene(conf, 0,
					FitnessFunct.GAPS_LIST.length - 1);
			sampleGenes[3] = new IntegerGene(conf, 0,
					FitnessFunct.CORRECTION_LIST.length - 1);
			sampleGenes[4] = new IntegerGene(conf, 0,
					FitnessFunct.CELLDIST_LIST.length - 1);

			if (TwainDriver) {
				sampleGenes[5] = new IntegerGene(conf, 0,
						FitnessFunct.BRIGHTNESS_LIST.length - 1);
				sampleGenes[6] = new IntegerGene(conf, 0,
						FitnessFunct.CONTRAST_LIST.length - 1);
			}

			// Setup Chromosome
			sampleChromosome = new Chromosome(conf, sampleGenes);
			conf.setSampleChromosome(sampleChromosome);
			conf.setPopulationSize(initialPopulation);

			// Time Used as Seed
			RandomGenerator rg = new RandomGenerator() {

				private static final long serialVersionUID = 1L;

				@Override
				public boolean nextBoolean() {
					return (rand).nextBoolean();
				}

				@Override
				public double nextDouble() {
					return (rand).nextDouble();
				}

				@Override
				public float nextFloat() {
					return (rand).nextFloat();
				}

				@Override
				public int nextInt() {
					return (rand).nextInt();
				}

				@Override
				public int nextInt(int arg0) {
					return (rand).nextInt(arg0);
				}

				@Override
				public long nextLong() {
					return (rand).nextLong();
				}

			};
			conf.setRandomGenerator(rg);

			// Setup Population
			population = Genotype.randomInitialGenotype(conf);

		} catch (InvalidConfigurationException e) {
			Initialized = false;
			return;
		}
		Initialized = true;
	}
}
