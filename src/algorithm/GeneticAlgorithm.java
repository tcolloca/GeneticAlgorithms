package algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import model.Couple;
import model.Individual;
import model.IndividualFactory;
import model.MutationMethod;
import model.Population;
import model.ReplacementMethod;
import model.ReproductionMethod;
import model.SelectionMethod;

public class GeneticAlgorithm<T> {

	// TODO
	private static final SelectionMethod SELECTION_METHOD = SelectionMethod.ELITE;
	private static final ReproductionMethod REPRODUCTION_METHOD = ReproductionMethod.ONE_POINT;
	private static final MutationMethod MUTATION_METHOD = MutationMethod.SINGLE_GENE;
	private static final ReplacementMethod REPLACEMENT_METHOD = ReplacementMethod.METHOD_1;
	private static final int N = 0;
	private static final int GENERATIONS = 0;
	
	private int generation = 0;
	
	private Population<T> population;
	
	public void run(IndividualFactory<T> individualFactory) {
		createPopulation(individualFactory, N);
		for (int i = 0; i < GENERATIONS; i++) {
			replacePopulation(REPLACEMENT_METHOD);
		}
	}
	
	private void createPopulation(IndividualFactory<T> individualFactory, int N) {
		this.population = new Population<>(individualFactory, N, generation++);
	}
	
	private void replacePopulation(ReplacementMethod replacementMethod) {
		switch (replacementMethod) {
		case METHOD_1:
			this.population = firstReplacementMethod(population);
		case METHOD_2:
			this.population = secondReplacementMethod(population);
		case METHOD_3:
			this.population = thirdReplacementMethod(population);
		default:
			throw new UnsupportedOperationException(
					"Unknown replacement method: " + replacementMethod);
		}
	}
	
	private Population<T> firstReplacementMethod(Population<T> population) {
		List<Individual<T>> newIndividuals = new ArrayList<>();
		for (int i = 0; i < population.size() / 2; i++) {
			Couple<T> parents = new Couple<>(selectIndividuals(SELECTION_METHOD, 2));
			List<Individual<T>> children = crossIndividuals(REPRODUCTION_METHOD, parents).toList();
			children = mutateIndividuals(MUTATION_METHOD, children);
			newIndividuals.addAll(children);
		}
		return new Population<>(newIndividuals, generation++);
	}
	
	private Population<T> secondReplacementMethod(Population<T> population) {
		List<Individual<T>> newIndividuals = new ArrayList<>();
		List<Individual<T>> children = getChildren(REPRODUCTION_METHOD, population);
		newIndividuals.addAll(children);
		List<Individual<T>> oldIndividuals = population.getIndividuals();
		Collections.shuffle(oldIndividuals);
		newIndividuals.addAll(oldIndividuals.subList(0, population.size() - children.size()));
		return new Population<>(newIndividuals, generation++);
	}
	
	private Population<T> thirdReplacementMethod(Population<T> population) {
		List<Individual<T>> newIndividuals = new ArrayList<>();		
		List<Individual<T>> children = getChildren(REPRODUCTION_METHOD, population);
		List<Individual<T>> oldIndividuals = population.getIndividuals();
		Collections.shuffle(oldIndividuals);
		newIndividuals.addAll(oldIndividuals.subList(0, population.size() - children.size()));
		oldIndividuals.addAll(newIndividuals);
		Collections.shuffle(oldIndividuals);
		newIndividuals.addAll(oldIndividuals.subList(0, children.size()));
		return new Population<>(newIndividuals, generation++);
	}
	
	private List<Individual<T>> getChildren(ReproductionMethod reproductionMethod, Population<T> population) {
		List<Couple<T>> parents = makeCouples(population.getIndividuals());
		return parents.stream()
				.map(couple -> crossIndividuals(reproductionMethod, couple).toList())
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}
	
	private List<Individual<T>> selectIndividuals(SelectionMethod selectionMethod, int K) {	
		switch (selectionMethod) {
		case ELITE:
			return SelectionAlgorithms.elite(population, K);	
		case BOLTZMANN:
			return SelectionAlgorithms.boltzmann(population, K);
		case RANDOM:
			return SelectionAlgorithms.random(population, K);
		case RANKING:
			return SelectionAlgorithms.ranking(population, K);
		case ROULETTE:
			return SelectionAlgorithms.roulette(population, K);
		case TOURNAMENT_DET:
			return SelectionAlgorithms.detTournament(population, K);
		case TOURNAMENT_PROB:
			return SelectionAlgorithms.probTournament(population, K);
		case UNIVERSAL:
			return SelectionAlgorithms.universal(population, K);
		default:
			throw new UnsupportedOperationException(
					"Unknown selection method: " + selectionMethod);
		}
	}
	
	private List<Couple<T>> makeCouples(List<Individual<T>> individuals) {
		Collections.shuffle(individuals);
		List<Couple<T>> couples = new ArrayList<>();
		for (int i = 0; i < individuals.size(); i += 2) {
			couples.add(new Couple<>(individuals.get(0), individuals.get(1)));
		}
		return couples;
	}
	
	private Couple<T> crossIndividuals(ReproductionMethod reproductionMethod, Couple<T> couple) {
		switch (reproductionMethod) {
		case ONE_POINT:
			return ReproductionAlgorithms.onePoint(couple);
		case DOUBLE_POINT:
			return ReproductionAlgorithms.twoPoints(couple);
		case RING:
			return ReproductionAlgorithms.ring(couple);
		case UNIFORM:
			return ReproductionAlgorithms.uniform(couple);
		default:
			throw new UnsupportedOperationException(
					"Unknown reproduction method: " + reproductionMethod);
		}
	}
	
	private List<Individual<T>> mutateIndividuals(MutationMethod mutationMethod, List<Individual<T>> individuals) {
		return individuals.stream()
				.map(individual -> this.mutateIndividual(mutationMethod, individual))
				.collect(Collectors.toList());
	}
	
	private Individual<T> mutateIndividual(MutationMethod mutationMethod, Individual<T> individual) {
		switch (mutationMethod) {
		case SINGLE_GENE:
			return MutationAlgorithms.singleGene(individual);
		default:
			throw new UnsupportedOperationException(
					"Unknown mutation method: " + mutationMethod);
		}
	}
}
