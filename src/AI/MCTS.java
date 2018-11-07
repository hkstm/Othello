package AI;

import Core.Board;
import Core.Logic;

import java.util.LinkedList;
import java.util.Random;

import static Core.Board.BLACK;
import static Core.Board.EMPTY;
import static Core.Board.WHITE;
import static java.lang.Integer.MIN_VALUE;

public class MCTS {

    private Board board;
    private GameTree gt;
    private int mctsMode;
    private int maxTurnsSimulated;
    private int treeDepth;
    private LinkedList<MCTSNode> nodeQueue = new LinkedList<MCTSNode>();
    public static final int DUMB = 0;
    public static final int WIN = 100;
    public static final int DRAW = 50;
    public static final int LOSS = 0;


    public MCTS(int treeDepth) {
        this.treeDepth = treeDepth;
    }

    public Board findMove(Board board) {
        MCTSNode root = new MCTSNode(board);
        nodeQueue.addFirst(root);
        do{
            MCTSNode toBeChecked = nodeSelection(nodeQueue);
            playoutSimulation(toBeChecked);
        } while(nodeQueue.size() > 0);
        Board mctsBoard = new Board(board);
        int maxScore = MIN_VALUE;
        for (MCTSNode node : root.getChildren()) {
            if(node.getScoreTotal()/node.getSimulations() > maxScore) {
                maxScore = node.getScoreTotal()/node.getSimulations();
                mctsBoard = node.getBoard();
            }
        }
        return mctsBoard;
    }

    public void createChildren(MCTSNode parentNode) {
        Board board = parentNode.getBoard();
        for(int r = 0; r < board.getSize(); r++) {
            for(int c = 0; c < board.getSize(); c++) {
                if(Logic.checkSquareAllowed(r, c, board)) {
                    Board tmpCopyBoard =  new Board(board);
                    tmpCopyBoard.applyMove(r, c);
                    MCTSNode tmpNode = new MCTSNode(tmpCopyBoard);
                    parentNode.addChild(tmpNode);
                    nodeQueue.add(tmpNode);
                }
            }
        }
    }

    public MCTSNode nodeSelection(LinkedList nodeQueue) {
        MCTSNode toBeChecked = (MCTSNode)nodeQueue.poll(); //retrieves and removes the first element of this list or returns null if this list is empty
        if(toBeChecked.getDepth() < treeDepth) createChildren(toBeChecked);
        return toBeChecked;
    }

    public void playoutBackpropogation(MCTSNode node, int winner, int currentPlayer) {
        while(node.getParent() != null) {
            Board board = node.getBoard();
            if(winner == EMPTY) node.setScoreTotal(node.getScoreTotal() + DRAW);
            if(board.getCurrentPlayer() == currentPlayer) {
                if(currentPlayer == winner) node.setScoreTotal(node.getScoreTotal() + WIN);
            }
            node.setSimulations(node.getSimulations()+1);
            node = (MCTSNode)node.getParent();
        }
    }

    public void playoutSimulation(MCTSNode node) {
        Board board = node.getBoard();
        boolean gameFinished = false;
        Random rand = new Random();
        int currentPlayer = board.getCurrentPlayer();
        do{
            if(Logic.isMovePossible(board)) {
                int x = rand.nextInt(board.getSize());
                int y = rand.nextInt(board.getSize());
                if(Logic.checkSquareAllowed(x, y, board)) {
                    board.applyMove(x, y);
                    board.incrementTurn();
                    board.changePlayer();
                }
            }
            if(!Logic.isMovePossible(board)) {
                board.changePlayer();
                if(!Logic.isMovePossible(board)) { // No moves possible for either player so game has ended
                    if(board.getNrBlackSquares() > board.getNrWhiteSquares()) {
                        playoutBackpropogation(node, BLACK, currentPlayer);
                    } else if(board.getNrBlackSquares() < board.getNrWhiteSquares()) {
                        playoutBackpropogation(node, WHITE, currentPlayer);
                    } else {
                        playoutBackpropogation(node, EMPTY, currentPlayer);
                    }
                    gameFinished = true;
                }
            }
        } while(!gameFinished);
    }
}