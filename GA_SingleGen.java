import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

public class GA_SingleGen {
	static DecimalFormat df = new DecimalFormat("#.#####"); 

	public static void main(String[] args) {
		// Get input for size of population
		System.out.println("Minimizing: (x-2)^2");
		int p_size = getInput("Enter size of population: ",2);
		
		// Create initial population
		double[] population = createPopulation(p_size);
		System.out.println("\nSTEP #1: GENERATE INITIAL POPULATION");
		System.out.println("(Range from 1-10)");
		for (double d:population)
			System.out.println(d);
		
		// Calculate fitness values
		double[] probability = calcFitness(population);
		System.out.println("\nSTEP #2: CALCULATE FITNESS VALUE");
		System.out.println("Total Fitness Value will sum to 1");
		System.out.println("Fitness Values: ");
		for (double d:probability)
			System.out.println(d);
		
		// Parent Selection using 'Roulette Wheel' method
		System.out.println("\nSTEP #3: CONSTRUCT ROULETTE WHEEL & PARENT SELECTION");
		System.out.println("Roulette wheel: ");
		double[] parents = selectParents(population,probability);
		System.out.println("Parents chosen: ");
		for (double d:parents)
			System.out.println(d);
		
		// Crossover among parents
		System.out.println("\nSTEP #4: CROSSOVER AMONGST SELECTED PARENTS");
		double[] nextGen = performCrossover(parents,0.9);
		System.out.println("Next gen after crossover with 0.9 chance: ");
		for (double d:nextGen)
			System.out.println(d);
		
		// Mutation
		nextGen = performMutation(parents, 0.01);
		System.out.println("\nSTEP #5: MUTATION");
		System.out.println("Next gen after mutation with 0.01 chance: ");
		for (double d:nextGen)
			System.out.println(d);
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
		
		double prev_sum = 0;
		for (int i=0; i<roulette_wheel.length; i++) {
			System.out.println(df.format(prev_sum)+" to "+df.format(roulette_wheel[i])+" ("+(i+1)+")");
			prev_sum += roulette_wheel[i];
		}
		System.out.println();
		
		// Select N chromosome as parent from the roulette wheel
		Random r = new Random();
		double[] parents = new double[pop.length];	// Used to store selected chromosomes based on their probability
		
		// On every selection iteration, generate a random number between 0-1 
		for (int i=0; i<pop.length; i++) {
			double num = r.nextDouble();
			for (int j=0; j<roulette_wheel.length; j++) 
				if (num<roulette_wheel[j]) {
					System.out.print("num: "+df.format(num)+" landed on: ("+(j+1)+")");
					break;
				}
			
			// if the random num is smaller than roulette wheel[n], select pop[n] as the parent for this iteration
			for (int j=0; j<pop.length; j++) {
				if (num<roulette_wheel[j]) {
					parents[i] = pop[j];
					System.out.println(", parent chosen: "+pop[j]);
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