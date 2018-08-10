import edu.princeton.cs.algs4.Bag;
import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.FordFulkerson;
import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.RedBlackBST;
import edu.princeton.cs.algs4.StdOut;

public class BaseballElimination {
    
    private final RedBlackBST<String, Integer> teams;
    private final String[] teamName;
    private final Integer[] wins;
    private final Integer[] loss;
    private final Integer[] left;
    private final Integer[][] games;
    private final int totalTeams;
    private int maxCapacity;
    
    public BaseballElimination(String filename) {// create a baseball division from given filename in format specified below
        In in = new In(filename);
        
        totalTeams = in.readInt();
        teams = new RedBlackBST<>();
        teamName = new String[totalTeams];
        wins = new Integer[totalTeams];
        loss = new Integer[totalTeams];
        left = new Integer[totalTeams];
        games = new Integer[totalTeams][totalTeams];
              
        int index = 0;
        while(!in.isEmpty()) {           
            String temp = in.readString();
            teams.put(temp, index);
            teamName[index] = temp;
            wins[index] = in.readInt();
            loss[index] = in.readInt();
            left[index] = in.readInt();
            for(int i = 0; i < totalTeams; i++) {
                games[index][i] = in.readInt();
            }
            index++;
        }

    }
    public int numberOfTeams() {// number of teams
        return totalTeams;
    }
    public Iterable<String> teams(){// all teams
        return teams.keys();
    }
    public int wins(String team) {// number of wins for given team
        int index = teams.get(team);
        return wins[index];
    }
    public int losses(String team) {// number of losses for given team
        int index = teams.get(team);
        return loss[index];
    }
    public int remaining(String team) {// number of remaining games for given team
        int index = teams.get(team);
        return left[index];
    }
    public int against(String team1, String team2) {// number of remaining games between team1 and team2
        int index1 = teams.get(team1);
        int index2 = teams.get(team2);
        return games[index1][index2];
    }
    public boolean isEliminated(String team) {// is given team eliminated?        
        int index = teams.get(team);
        for(int i = 0; i < totalTeams; i++) {
           if(wins[index] + left[index] < wins[i]) {//trivial elimination
               return true;
           } 
        }
        FordFulkerson temp = drawFF(team);
        return (maxCapacity != temp.value());
    }
    
    private int ijToVnum(int i, int j) {
        return (i * totalTeams + j + totalTeams);
    }
    
    private FordFulkerson drawFF(String team) {
        int index = teams.get(team);
        int vNum = totalTeams + totalTeams * totalTeams + 2;
        //vertex s = vNum-1, t = vNum - 2
        //team vertices = 0 to totalTeams-1
        //game vertices = totalTeams to totalTeams * totalTeams + totalTeams
        FlowNetwork flowNet = new FlowNetwork(vNum);
        maxCapacity = 0;
        for(int i = 0; i < totalTeams; i++) {
            if(i == index) continue;            
            for(int j = i+1; j < totalTeams; j++) {
                if(j == index) continue;
                FlowEdge temp1 = new FlowEdge(vNum-1, ijToVnum(i,j), games[i][j], 0);
                flowNet.addEdge(temp1);
                maxCapacity += games[i][j];
                FlowEdge temp2 = new FlowEdge(ijToVnum(i,j), i, Double.POSITIVE_INFINITY, 0);
                flowNet.addEdge(temp2);
                FlowEdge temp3 = new FlowEdge(ijToVnum(i,j), j, Double.POSITIVE_INFINITY, 0);
                flowNet.addEdge(temp3);
            }
            FlowEdge temp4 = new FlowEdge(i, vNum-2, Math.max(0, (wins[index] + left[index] - wins[i])), 0);
            flowNet.addEdge(temp4);
        }
        //System.out.print(flowNet.toString());
        FordFulkerson findMaxFlow = new FordFulkerson(flowNet, vNum-1, vNum-2);
        return findMaxFlow;
    }
    
    public Iterable<String> certificateOfElimination(String team){// subset R of teams that eliminates given team; null if not eliminated
        int index = teams.get(team);
        Bag<String> bag = new Bag<>();
        for(int i = 0; i < totalTeams; i++) {
           if(wins[index] + left[index] < wins[i]) {//trivial elimination
               bag.add(teamName[i]);
           }
           if(!bag.isEmpty()) {
               return bag;
           }
        }
        FordFulkerson fordFul = drawFF(team);
        for(int i = 0; i < totalTeams; i++) {
            if(fordFul.inCut(i) == true) bag.add(teamName[i]);
        }            
        return bag;
    }
    
    public static void main(String[] args) {
        BaseballElimination division = new BaseballElimination("baseball/teams5.txt");
        for (String team : division.teams()) {
            if (division.isEliminated(team)) {
                StdOut.print(team + " is eliminated by the subset R = { ");
                for (String t : division.certificateOfElimination(team)) {
                    StdOut.print(t + " ");
                }
                StdOut.println("}");
            }
            else {
                StdOut.println(team + " is not eliminated");
            }
        }
    }
}
