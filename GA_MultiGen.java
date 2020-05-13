import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.Scanner;

public class GA_MultiGen {
	static DecimalFormat df = new DecimalFormat("#.#####");

	public static void main(String[] args) {
		// Get input for size of population
		System.out.println("Minimizing: (x-2)^2");
		int p_size = getInput("Enter size of population: ",2);
		
		// Create initial population
		double[] population = createPopulation(p_size);
		System.out.println("\nRandomly generated population: ");
		for (double d:population)
			System.out.println(d);
		System.out.println();
		
		// Get input for number of generations & run for n generations
		int gen_num = getInput("How many generations would you like to simulate: ",1);
		for (int i=1; i<=gen_num; i++) {
			population = performSingleGeneration(population);
			
			System.out.println("Generation "+i+":");
			for (double d:population)
				System.out.println(d);
			System.out.println();
		}
		System.out.println("-----END-----");
	}
	
	
	// Handles user input
	public static int getInput(String q, int min) {
		Scanner sc = new Scanner(System.in);
		
		int ans = 0;
		while (true) {
			System.out.print(q);
			String s = sc.nextLine();
			try {
				ans = Integer.parseInt(s);
				if (ans >= min)
					break;
				else {
					System.out.println("Please enter a number larger than "+min+"\n");
					continue;
				}
			}
			catch (Exception e) {
				System.out.println("Please enter a number\n");
			}
		}
		return ans;
	}
	
	
	// A single generation of steps for GA algorithm
	public static double[] performSingleGeneration(double[] population) {
		double[] probability = calcFitness(population);
		double[] parents = selectParents(population,probability);
		double[] nextGen = performCrossover(parents,0.9);
		nextGen = performMutation(parents, 0.01);
		return nextGen;
	}
	
	
	// Generate random initial population
	public static double[] createPopulation(int size) {
		Random r = new Random();
		double[] population = new double[size];
		
		for (int i=0; i<size; i++) {
			double d = r.nextDouble()*8;
			d = Double.parseDouble(df.format(d));
			population[i] = d;
		}
		
		return population;
	}
	
	
	// Calculate the fitness function for each chromosome
	public static double[] calcFitness(double[] pop) {
		
		// Calculate each chromosome's objective function value
		double[] val = new double[pop.length];		
		for (int i=0; i<pop.length; i++) 
			val[i] = Math.pow(pop[i]-2,2);
		
		// Calculate each chromosome's fitness values & total fitness value
		double fitness_total = 0;
		double[] fitness = new double[pop.length];
		double[] probability = new double[pop.length];
		for (int i=0; i<pop.length; i++) {
			fitness[i] = 1/val[i];	
			fitness_total+=fitness[i];
		}
		for (int i=0; i<pop.length; i++)
			probability[i] = Double.parseDouble(df.format(fitness[i]/fitness_total));
		
		return probability;
	}
	
	
	// Creates selection pool(roulette wheel) & picks N chromosomes as parent
	// High fitness value, high probability to get picked
	public static double[] selectParents(double[] pop, double[] probability) {
		
		// Construct the roulette wheel(Selection Pool) based on the probabilities of each chromosome
		double[] roulette_wheel = new double[pop.length];
		double sum = 0;
		for (int i=0; i<pop.length; i++) {
			sum+=probability[i];
			roulette_wheel[i] = sum;
		}
		
		// Select N chromosome as parent from the roulette wheel
		Random r = new Random();
		double[] parents = new double[pop.length];	// Used to store selected chromosomes based on their probability
		
		// On every selection iteration, generate a random number between 0-1 
		for (int i=0; i<pop.length; i++) {
			double num = r.nextDouble();
			
			// if the random num is smaller than roulette wheel[n], select pop[n] as the parent for this iteration
			for (int j=0; j<pop.length; j++) {
				if (num<roulette_wheel[j]) {
					parents[i] = pop[j];
					break;
				}
			}
		}
		return parents;
	}
	
	
	// 'cross_rate' specifies probability for crossover to occur for each parent
	public static double[] performCrossover(double[] parents, double cross_rate) {
		
		// Store index of selected parents
		ArrayList<Integer> selected_parents_index = new ArrayList<Integer>();	// ArrayList is used due to size being unknown upon initializaton
		ArrayList<Integer> crossover_index = new ArrayList<Integer>();	
		Random r = new Random();	
		for (int i=0; i<parents.length; i++) {
			double num = r.nextDouble();
			if (num<cross_rate) {
				selected_parents_index.add(i);
				int cross_index = r.nextInt(4)+1;
				crossover_index.add(cross_index);
			}
		}
		
		// Computing crossover index and performs crossover
		double[] childrens = new double[selected_parents_index.size()];
		int index_parent1 = 0;
		int index_parent2 = 0;
		for (int i=0; i<selected_parents_index.size(); i++) {
			index_parent1 = selected_parents_index.get(i);
			if (i == selected_parents_index.size()-1)
				index_parent2 = selected_parents_index.get(0);
			else
				index_parent2 = selected_parents_index.get(i+1);
			
			// Remove the decimal point '.' and start performing crossover
			String children = "";
			int current_crossover_index = crossover_index.get(i);
			String parent1 = Double.toString(parents[index_parent1]);
			String parent2 = Double.toString(parents[index_parent2]);
			parent1 = parent1.replace(".", "");
			parent2 = parent2.replace(".", "");
			children = children.concat(parent1.substring(0, current_crossover_index));
			children = children.concat(parent2.substring(current_crossover_index));
			childrens[i] = Double.parseDouble(children.substring(0,1)+"."+children.substring(1, children.length()));
		}
		
		// Overwrite parents(population) with childrens
		for (int i=0; i<selected_parents_index.size(); i++) 
			parents[selected_parents_index.get(i)] = childrens[i];
				
		return parents;
	}
	
	
	// 'mutation_rate' specifies probability for mutation
	public static double[] performMutation(double[] parents, double mutation_rate) {
		Random r = new Random();

		for (int i=0; i<parents.length; i++) {
			String children = Double.toString(parents[i]);
			children = children.replace(".", "");
			for (int j=0; j<children.length(); j++) {
				double num = r.nextDouble();
				if (num<mutation_rate) {
					char charToReplace = children.charAt(j);
					char newChar = (char)(r.nextInt(10)+'0');
					children = children.replace(charToReplace, newChar);
				}
			}
			children = children.substring(0, 1)+"."+children.substring(1, children.length());
			parents[i] = Double.parseDouble(children);
		}
		return parents;
	}

}