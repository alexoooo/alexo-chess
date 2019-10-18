package ao.chess.v2.engine.mcts.player;



public interface BanditNode {
    BanditNode childMatching(int action);

    int maxDepth();
    int minDepth();
    int nodeCount();
}
