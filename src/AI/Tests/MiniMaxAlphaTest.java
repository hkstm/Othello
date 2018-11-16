package AI.Tests;

import AI.*;
import Core.Board;

public class MiniMaxAlphaTest {

    final static int DEPTH = 4;

    public static void main(String[] args) {

        EvaluationFunction evaluator = new EvaluationFunction();
        Board board = new Board();
        MiniMaxAlph minimax = new MiniMaxAlph(evaluator, board);
        GameTree gameTree = new GameTree(DEPTH);
        Node<Board> root = gameTree.createTree();
        minimax.search(root, 1);                        //1 is AI
        minimax.getBestNode().getData().displayBoardGrid();

    }
}
