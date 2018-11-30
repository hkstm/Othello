package AI.Genetic_Algorithm;

import AI.EvaluationFunction;
import AI.AI;
import Core.Board;
import Core.QuickSort;

import java.util.Random;

public class Population {

    private int popSize;
    private int boardSize;
    private static Random rand;
    private double weightPolyBound;
    private double territoryBound;
    private AI[] AIs;

    private int mutationCount;

    public static final int GA_GAMES_TO_BE_SIMMED = 2;
    public static final int GA_BOARD_SIZE = 4;
    public static final int DEPTH = 3;
    public static final int GA_POP_SIZE = 10;
    public static final double GA_WEIGHT_POLY_BOUND = 10;
    public static final double GA_TERRITORY_BOUND = 10;

    public Population(int popSize, int boardSize, double weightPolyBound, double territoryBound) {
        this.popSize = popSize;
        this.boardSize = boardSize;
        this.rand = new Random();
        this.weightPolyBound = weightPolyBound;
        this.territoryBound = territoryBound;
        this.mutationCount = 0;
        AIs = new AI[popSize];
        initMiniMaxAlphPopulation();
    }

    public Population() {
        this(GA_POP_SIZE, GA_BOARD_SIZE, GA_WEIGHT_POLY_BOUND, GA_TERRITORY_BOUND);
    }

    public void initMiniMaxAlphPopulation() {
        for(int i = 0; i < popSize; i++) {
            EvaluationFunction cEvalFunc = new EvaluationFunction(new Board(boardSize));
            cEvalFunc.setWeightPoly(initWeightPoly(16, weightPolyBound)); //size of weightpoly in evaluationfunction
            cEvalFunc.setTerritory(initTerritory(territoryBound));
            this.AIs[i] = new GA_MiniMaxAlph(DEPTH, new Board(boardSize), cEvalFunc); //idk what the depth should be
        }
    }

    public double[] initWeightPoly(int weightPolySize, double bound) {
        double[] weightPoly = new double[weightPolySize];
        for(int i = 0; i < weightPolySize; i++) {
            weightPoly[i] = rand.nextDouble()*bound;
        }
        return weightPoly;
    }

    public double[][] initTerritory(double bound) {
        return combineTerritoryPart(createTerritoryPart(bound));
    }

    public double[][] createTerritoryPart(double bound) {
        double[][] territoryPart = new double[boardSize/2][boardSize/2]; //force symmetry
        for(int i = 0; i < boardSize/2; i++) {
            for(int j = 0; j < boardSize/2; j++) {
                territoryPart[i][j] = rand.nextDouble()*bound;
            }
        }
        return territoryPart;
    }

    /**
     * Territory parts should be symmetric so trying to save calculations
     * by only generating a part and then constructing whole terr
     * @param territoryPart assuming part is topleft
     * @return full territory based on part
     */

    public double[][] combineTerritoryPart(double[][] territoryPart) {
        double[][] territory = new double[boardSize][boardSize];
        for(int i = 0; i < territoryPart.length; i++) {
            for(int j = 0; j < territoryPart.length; j++) {
                territory[i][j] = territoryPart[i][j]; //topleft
                territory[i][(boardSize - 1) - j] = territoryPart[i][j]; //topright
                territory[(boardSize - 1) - i][j] = territoryPart[i][j]; //bottomleft
                territory[(boardSize - 1) - i][(boardSize - 1) - j] = territoryPart[i][j]; //bottomright
            }
        }
        return territory;
    }

    /**
     * Calculates and sort AIs
     * @param gamesToBeSimmed
     * @param boardSize
     */

    public void calculateFitness(int gamesToBeSimmed, int boardSize) {
        for(int i = 0; i < AIs.length; i++) {
            AIs[i].evaluateFitness(gamesToBeSimmed, boardSize);
        }
//        AI[] AIsTemp = new QuickSort(AIs).getAIs();
//        for(int i = 0; i < AIs.length; i++) {
//            AIs[i] = AIsTemp[i];
//        }
    }

    public AI randomCrossover(AI ai_parent1, AI ai_parent2) {
        EvaluationFunction parent1 = ai_parent1.getEvaluator();
        EvaluationFunction parent2 = ai_parent2.getEvaluator();
        double[] parent1WeightPoly = parent1.getWeightPoly();
        double[] parent2WeightPoly = parent1.getWeightPoly();
        double[][] parent1CellValues = parent2.getCellValues();
        double[][] parent2CellValues = parent2.getCellValues();
        double[] childWeightPoly = new double[parent1WeightPoly.length];
        double[][] childCellValues = new double[parent1CellValues.length][parent1CellValues[0].length];

        for(int i = 0; i < parent1WeightPoly.length; i++) {
            childWeightPoly[i] = (rand.nextInt(2) == 0) ? parent1WeightPoly[i] : parent2WeightPoly[i];
        }
        for(int i = 0; i < parent1CellValues.length; i++) {
            for (int j = 0; j < parent1CellValues[0].length; j++) {
                childCellValues[i][j] = (rand.nextInt(2) == 0) ? parent1CellValues[i][j] : parent2CellValues[i][j];
            }
        }
        EvaluationFunction tmpEvalFunc = new EvaluationFunction(new Board(boardSize));
        tmpEvalFunc.setWeightPoly(initWeightPoly(16, weightPolyBound)); //size of weightpoly in evaluationfunction
        tmpEvalFunc.setTerritory(initTerritory(territoryBound));
        return new GA_MiniMaxAlph(DEPTH, new Board(boardSize), tmpEvalFunc);
    }

    public AI randomWeightedCrossover(AI ai_parent1, AI ai_parent2) {
        EvaluationFunction parent1 = ai_parent1.getEvaluator();
        EvaluationFunction parent2 = ai_parent2.getEvaluator();
        double[] parent1WeightPoly = parent1.getWeightPoly();
        double[] parent2WeightPoly = parent1.getWeightPoly();
        double[][] parent1CellValues = parent2.getCellValues();
        double[][] parent2CellValues = parent2.getCellValues();
        double[] childWeightPoly = new double[parent1WeightPoly.length];
        double[][] childCellValues = new double[parent1CellValues.length][parent1CellValues[0].length];
        double proportion;

        for(int i = 0; i < parent1WeightPoly.length; i++) {
            proportion = rand.nextDouble();
            childWeightPoly[i] = parent1WeightPoly[i] * proportion + parent2WeightPoly[i] * (1 - proportion);
        }
        for(int i = 0; i < parent1CellValues.length; i++) {
            for (int j = 0; j < parent1CellValues[0].length; j++) {
                proportion = rand.nextDouble();
                childCellValues[i][j] = parent1CellValues[i][j] * proportion + parent2CellValues[i][j] * (1 - proportion);
            }
        }
        EvaluationFunction tmpEvalFunc = new EvaluationFunction(new Board(boardSize));
        tmpEvalFunc.setWeightPoly(initWeightPoly(16, weightPolyBound)); //size of weightpoly in evaluationfunction
        tmpEvalFunc.setTerritory(initTerritory(territoryBound));
        return new GA_MiniMaxAlph(DEPTH, new Board(boardSize), tmpEvalFunc);
    }

    /**
     * Mutates EvaluationFunction chromosomes, severity of mutations decreases each iteration of population
     * @param percentPopAff percentage of individuals affected
     * @param percentChromoAff percentage of genes in chromosomes of individuals affected
     */

    public void nonUniformBitMutate(double percentPopAff, double percentChromoAff) {
        for(int i = 0; i < AIs.length; i++) {
            if(rand.nextDouble() < percentPopAff) {
                double[] chromosome = AIs[i].getEvaluator().getChromosome();
                for(int j = 0; j < chromosome.length; j++ ) {
                    if(rand.nextDouble() < percentChromoAff) {
                        chromosome[j] = (rand.nextDouble() < 0.5) ? (1/mutationCount) * chromosome[j] + chromosome[j] : (1/mutationCount) * chromosome[j] - chromosome[j];
                    }
                }
            }
        }
        mutationCount++;
    }

    public AI[] selection(double selectionRatio) {
        double totalFitness = 0;
        AI[] selectedIndividuals = new AI[AIs.length*2];
        this.calculateFitness(GA_GAMES_TO_BE_SIMMED, GA_BOARD_SIZE);
        for (int i = 0; i < AIs.length; i++) {
            totalFitness += AIs[i].getFitness();
        }
        for(int i = 0; i < AIs.length*2; i++) {
            int individualIndex = -1; //makes sure its gets updated
            do {
                individualIndex = rand.nextInt(AIs.length);
                selectedIndividuals[i] = AIs[individualIndex];
            } while(AIs[individualIndex].getFitness() > rand.nextDouble() * selectionRatio * totalFitness);
        }
       return selectedIndividuals;
    }

    public AI[] getAIs() {
        return AIs;
    }

    public void setAIs(AI[] AIs) {
        this.AIs = AIs;
    }

    public int getPopSize() {
        return popSize;
    }

    public int getBoardSize() {
        return boardSize;
    }
}