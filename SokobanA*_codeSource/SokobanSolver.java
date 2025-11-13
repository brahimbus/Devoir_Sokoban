import java.util.*;

/**
 * Résolveur Sokoban avec algorithme A*
 * État du jeu + moteur de recherche combinés
 * 
 * Symboles: ■=mur, □=vide, @=joueur, $=caisse, T=cible, *=caisse sur cible
 */
public class SokobanSolver {
    
    // ========== CLASSE INTERNE: État du jeu ==========
    
    /**
     * Représente un état du puzzle Sokoban
     */
    public static class State {
        public char[][] grid;       // Configuration de la grille
        public int g;               // Coût réel (nombre de poussées)
        public int f;               // Coût estimé total (g + h)
        public State parent;        // État précédent
        public String move;         // Direction du mouvement (U/D/L/R)
        
        public int playerX, playerY;
        public List<int[]> boxes;
        
        public State(char[][] grid, int g, int f, State parent, String move) {
            this.grid = deepCopy(grid);
            this.g = g;
            this.f = f;
            this.parent = parent;
            this.move = move;
            extractPositions();
        }
        
        // Extrait positions du joueur et des caisses de la grille
        private void extractPositions() {
            boxes = new ArrayList<>();
            for (int i = 0; i < grid.length; i++) {
                for (int j = 0; j < grid[i].length; j++) {
                    char c = grid[i][j];
                    if (c == '@' || c == '+') {
                        playerX = j;
                        playerY = i;
                    }
                    if (c == '$' || c == '*') {
                        boxes.add(new int[]{j, i});
                    }
                }
            }
        }
        
        // Vérifie si toutes les caisses sont sur des cibles
        public boolean isGoal() {
            for (int[] box : boxes) {
                if (grid[box[1]][box[0]] != '*') {
                    return false;
                }
            }
            return boxes.size() > 0;
        }
        
        // Calcule l'heuristique h(n) = distance optimale caisses→cibles + joueur→caisse
        public int heuristic(List<int[]> targets) {
            if (boxes.isEmpty() || targets.isEmpty()) return 0;
            if (hasDeadlock()) return Integer.MAX_VALUE;
            return computeMatching(targets) + playerToBoxDistance();
        }
        
        // Détecte si une caisse est bloquée dans un coin
        private boolean hasDeadlock() {
            for (int[] box : boxes) {
                int x = box[0];
                int y = box[1];
                if (grid[y][x] == '*') continue;
                if (isCornerDeadlock(x, y)) return true;
            }
            return false;
        }
        
        // Vérifie les 4 patterns de coin deadlock
        private boolean isCornerDeadlock(int x, int y) {
            boolean wL = isWall(x - 1, y);
            boolean wR = isWall(x + 1, y);
            boolean wU = isWall(x, y - 1);
            boolean wD = isWall(x, y + 1);
            
            if (wL && wU) return isBlocked(x + 1, y) && isBlocked(x, y + 1);
            if (wR && wU) return isBlocked(x - 1, y) && isBlocked(x, y + 1);
            if (wL && wD) return isBlocked(x + 1, y) && isBlocked(x, y - 1);
            if (wR && wD) return isBlocked(x - 1, y) && isBlocked(x, y - 1);
            return false;
        }
        
        private boolean isWall(int x, int y) {
            if (y < 0 || y >= grid.length || x < 0 || x >= grid[y].length) return true;
            return grid[y][x] == '■';
        }
        
        private boolean isBlocked(int x, int y) {
            if (y < 0 || y >= grid.length || x < 0 || x >= grid[y].length) return true;
            char c = grid[y][x];
            return c == '■' || c == '$' || c == '*';
        }
        
        // Calcule l'assignation optimale caisses → cibles (brute force)
        private int computeMatching(List<int[]> targets) {
            int n = boxes.size();
            if (n == 0) return 0;
            return findOptimalAssignment(targets, new boolean[targets.size()], 0, 0);
        }
        
        // Trouve la meilleure assignation récursivement
        private int findOptimalAssignment(List<int[]> targets, boolean[] used, int boxIdx, int cost) {
            if (boxIdx >= boxes.size()) return cost;
            
            int minCost = Integer.MAX_VALUE;
            int[] box = boxes.get(boxIdx);
            
            for (int i = 0; i < targets.size(); i++) {
                if (used[i]) continue;
                
                int[] target = targets.get(i);
                int dist = Math.abs(box[0] - target[0]) + Math.abs(box[1] - target[1]);
                
                used[i] = true;
                int result = findOptimalAssignment(targets, used, boxIdx + 1, cost + dist);
                used[i] = false;
                
                minCost = Math.min(minCost, result);
            }
            return minCost;
        }
        
        // Distance du joueur à la caisse la plus proche
        private int playerToBoxDistance() {
            if (boxes.isEmpty()) return 0;
            int minDist = Integer.MAX_VALUE;
            for (int[] box : boxes) {
                int dist = Math.abs(playerX - box[0]) + Math.abs(playerY - box[1]);
                minDist = Math.min(minDist, dist);
            }
            return minDist;
        }
        
        // Génère tous les mouvements possibles (4 directions)
        public List<State> getPossibleMoves() {
            List<State> moves = new ArrayList<>();
            int[] dx = {0, 0, -1, 1};
            int[] dy = {-1, 1, 0, 0};
            String[] dirs = {"U", "D", "L", "R"};
            
            for (int i = 0; i < 4; i++) {
                State next = tryMove(dx[i], dy[i], dirs[i]);
                if (next != null) moves.add(next);
            }
            return moves;
        }
        
        // Essaie un mouvement dans une direction
        private State tryMove(int dx, int dy, String dir) {
            int newX = playerX + dx;
            int newY = playerY + dy;
            
            if (newY < 0 || newY >= grid.length || newX < 0 || newX >= grid[newY].length) {
                return null;
            }
            
            char nextCell = grid[newY][newX];
            
            if (nextCell == '■') return null;
            
            // Cas: pousser une caisse
            if (nextCell == '$' || nextCell == '*') {
                int boxX = newX + dx;
                int boxY = newY + dy;
                
                if (boxY < 0 || boxY >= grid.length || boxX < 0 || boxX >= grid[boxY].length) {
                    return null;
                }
                
                char behind = grid[boxY][boxX];
                if (behind != '□' && behind != 'T') return null;
                
                return createStateAfterPush(newX, newY, boxX, boxY, dx, dy, dir);
            }
            
            // Cas: mouvement simple
            if (nextCell == '□' || nextCell == 'T') {
                return createStateAfterMove(newX, newY, dir);
            }
            
            return null;
        }
        
        // Crée nouvel état après mouvement simple (sans poussée)
        private State createStateAfterMove(int newX, int newY, String dir) {
            char[][] newGrid = deepCopy(grid);
            
            char oldCell = grid[playerY][playerX];
            if (oldCell == '@') {
                newGrid[playerY][playerX] = '□';
            } else if (oldCell == '+') {
                newGrid[playerY][playerX] = 'T';
            }
            
            char newCell = grid[newY][newX];
            if (newCell == '□') {
                newGrid[newY][newX] = '@';
            } else if (newCell == 'T') {
                newGrid[newY][newX] = '+';
            }
            
            return new State(newGrid, g, 0, this, dir);
        }
        
        // Crée nouvel état après poussée (g augmente de 1)
        private State createStateAfterPush(int bx, int by, int nbx, int nby, int dx, int dy, String dir) {
            char[][] newGrid = deepCopy(grid);
            
            char oldCell = grid[playerY][playerX];
            if (oldCell == '@') {
                newGrid[playerY][playerX] = '□';
            } else if (oldCell == '+') {
                newGrid[playerY][playerX] = 'T';
            }
            
            char boxCell = grid[by][bx];
            if (boxCell == '$') {
                newGrid[by][bx] = '@';
            } else if (boxCell == '*') {
                newGrid[by][bx] = '+';
            }
            
            char behindCell = grid[nby][nbx];
            if (behindCell == '□') {
                newGrid[nby][nbx] = '$';
            } else if (behindCell == 'T') {
                newGrid[nby][nbx] = '*';
            }
            
            return new State(newGrid, g + 1, 0, this, dir);
        }
        
        private static char[][] deepCopy(char[][] original) {
            char[][] copy = new char[original.length][];
            for (int i = 0; i < original.length; i++) {
                copy[i] = Arrays.copyOf(original[i], original[i].length);
            }
            return copy;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof State)) return false;
            State other = (State) obj;
            
            if (grid.length != other.grid.length) return false;
            for (int i = 0; i < grid.length; i++) {
                if (!Arrays.equals(grid[i], other.grid[i])) return false;
            }
            return true;
        }
        
        @Override
        public int hashCode() {
            int hash = 0;
            for (int i = 0; i < grid.length; i++) {
                hash = 31 * hash + Arrays.hashCode(grid[i]);
            }
            return hash;
        }
    }
    
    // ========== CLASSE: Moteur de recherche A* ==========
    
    private char[][] grid;
    private State startState;
    private List<int[]> targets;
    private State goalState;
    private int nodesExplored;
    private long startTime, endTime;
    
    // Initialise et lance la recherche A*
    public SokobanSolver(String[] gridLines) {
        parseGrid(gridLines);
        extractTargets();
        startState = new State(grid, 0, 0, null, null);
        nodesExplored = 0;
        search();
    }
    
    // Parse la grille depuis les chaînes d'entrée
    private void parseGrid(String[] gridLines) {
        if (gridLines == null || gridLines.length == 0) {
            throw new IllegalArgumentException("Grille vide");
        }
        
        int height = gridLines.length;
        int width = 0;
        for (String line : gridLines) {
            if (line.length() > width) width = line.length();
        }
        
        grid = new char[height][width];
        for (int i = 0; i < height; i++) {
            String line = gridLines[i];
            for (int j = 0; j < width; j++) {
                grid[i][j] = (j < line.length()) ? line.charAt(j) : '□';
            }
        }
    }
    
    // Extrait les positions des cibles (T, *, +)
    private void extractTargets() {
        targets = new ArrayList<>();
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                char c = grid[i][j];
                if (c == 'T' || c == '*' || c == '+') {
                    targets.add(new int[]{j, i});
                }
            }
        }
    }
    
    // Algorithme A*: Open = états à explorer, Closed = états explorés
    private void search() {
        startTime = System.currentTimeMillis();
        
        // File de priorité triée par f = g + h
        PriorityQueue<State> open = new PriorityQueue<>(64, (a, b) -> {
            if (a.f != b.f) return Integer.compare(a.f, b.f);
            return Integer.compare(a.g, b.g);
        });
        
        Set<State> closed = new HashSet<>();
        Set<State> inOpen = new HashSet<>();
        Map<State, Integer> bestG = new HashMap<>();
        
        int h0 = startState.heuristic(targets);
        startState.f = h0;
        open.add(startState);
        inOpen.add(startState);
        bestG.put(startState, 0);
        
        final int MAX_NODES = 500000;
        
        while (!open.isEmpty() && nodesExplored < MAX_NODES) {
            State current = open.poll();
            
            if (closed.contains(current)) continue;
            
            inOpen.remove(current);
            closed.add(current);
            nodesExplored++;
            
            if (current.isGoal()) {
                goalState = current;
                break;
            }
            
            // Générer successeurs et ajouter à Open
            for (State neighbor : current.getPossibleMoves()) {
                if (closed.contains(neighbor)) continue;
                
                int tentativeG = neighbor.g;
                Integer prevG = bestG.get(neighbor);
                
                if (prevG == null || tentativeG < prevG) {
                    bestG.put(neighbor, tentativeG);
                    int h = neighbor.heuristic(targets);
                    
                    if (h == Integer.MAX_VALUE) continue;
                    
                    neighbor.f = tentativeG + h;
                    
                    if (!inOpen.contains(neighbor)) {
                        open.add(neighbor);
                        inOpen.add(neighbor);
                    }
                }
            }
        }
        
        endTime = System.currentTimeMillis();
        
        if (goalState != null) {
            System.out.println("But trouvé.");
            printSolution();
        } else {
            System.out.println("Aucune solution trouvée.");
        }
    }
    
    // Affiche la solution trouvée
    private void printSolution() {
        List<String> path = new ArrayList<>();
        State current = goalState;
        
        while (current != null && current.move != null) {
            path.add(0, current.move);
            current = current.parent;
        }
        
        System.out.println("Nombre de poussées: " + goalState.g);
        System.out.print("Chemin optimal: [");
        for (int i = 0; i < path.size(); i++) {
            System.out.print(path.get(i));
            if (i < path.size() - 1) System.out.print(", ");
        }
        System.out.println("]");
        System.out.println("Temps: " + (endTime - startTime) + " ms");
        System.out.println("Nœuds explorés: " + nodesExplored);
        
        System.out.println("\nGrille finale:");
        printGrid(goalState.grid);
    }
    
    public void printGrid(char[][] gridToPrint) {
        for (char[] row : gridToPrint) {
            System.out.println(new String(row));
        }
    }
    
    public int getPushCount() {
        return goalState != null ? goalState.g : 0;
    }
    
    public long getSolveTime() {
        return endTime - startTime;
    }
    
    public int getNodesExplored() {
        return nodesExplored;
    }
}
