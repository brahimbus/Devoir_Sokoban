/**
 * Programme principal pour résoudre des puzzles Sokoban avec A*
 */
public class SokobanAStarSearch {
    
    private static String[] grid1 = {
        "■■■■■■■■■■",
        "■□□□□□□□□■",
        "■□■■□■■□□■",
        "■□$□T□$□□■",
        "■□■□@□■□□■",
        "■□$□T□$□□■",
        "■□■■□■■□□■",
        "■□□T□□T□□■",
        "■□□□□□□□□■",
        "■■■■■■■■■■"
    };
    
    private static String[] grid2 = {
        "■■■■■■■■■■",
        "■T□■□□■□T■",
        "■□■$□□$■□■",
        "■□■□□□□■□■",
        "■□□□@□□□□■",
        "■□■□□□□■□■",
        "■□■$□□$■□■",
        "■T□■□□■□T■",
        "■□□□□□□□□■",
        "■■■■■■■■■■"
    };
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("Solveur Sokoban avec A*");
        System.out.println("========================================\n");
        
        System.out.println("--- Grille 1 ---");
        System.out.println("Grille initiale:");
        printGrid(grid1);
        System.out.println();
        
        SokobanSolver solver1 = new SokobanSolver(grid1);
        
        System.out.println("\n========================================\n");
        
        System.out.println("--- Grille 2 ---");
        System.out.println("Grille initiale:");
        printGrid(grid2);
        System.out.println();
        
        SokobanSolver solver2 = new SokobanSolver(grid2);
        
        System.out.println("\n========================================");
        System.out.println("Résumé des résultats:");
        System.out.println("========================================");
        System.out.println("Grille 1:");
        System.out.println("  - Poussées: " + solver1.getPushCount());
        System.out.println("  - Temps: " + solver1.getSolveTime() + " ms");
        System.out.println("  - Nœuds explorés: " + solver1.getNodesExplored());
        System.out.println("\nGrille 2:");
        System.out.println("  - Poussées: " + solver2.getPushCount());
        System.out.println("  - Temps: " + solver2.getSolveTime() + " ms");
        System.out.println("  - Nœuds explorés: " + solver2.getNodesExplored());
    }
    
    private static void printGrid(String[] grid) {
        for (String line : grid) {
            System.out.println(line);
        }
    }
}
