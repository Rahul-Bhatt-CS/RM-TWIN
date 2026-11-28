
import java.util.*;

public class Main {

    static class Tab {
        int id;
        String current;
        Stack<String> back = new Stack<>();
        Stack<String> forward = new Stack<>();

        Tab(int id) {
            this.id = id;
            current = "HOME";
        }

        void visit(String x) {
            back.push(current);
            current = x;
            forward.clear();
        }

        void goBack(int k) {
            while (k-- > 0 && !back.isEmpty()) {
                forward.push(current);
                current = back.pop();
            }
        }

        void goForward(int k) {
            while (k-- > 0 && !forward.isEmpty()) {
                back.push(current);
                current = forward.pop();
            }
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        int n = sc.nextInt();
        sc.nextLine();

        TreeMap<Integer, Tab> tabs = new TreeMap<>();
        Stack<Tab> closed = new Stack<>();

        int nextId = 1;
        int activeId = 1;

        tabs.put(1, new Tab(1));

        for (int i = 0; i < n; i++) {
            String[] op = sc.nextLine().trim().split("\\s+");

            switch (op[0]) {
                case "VISIT":
                    tabs.get(activeId).visit(op[1]);
                    break;

                case "BACK":
                    tabs.get(activeId).goBack(Integer.parseInt(op[1]));
                    break;

                case "FORWARD":
                    tabs.get(activeId).goForward(Integer.parseInt(op[1]));
                    break;

                case "NEW":
                    Tab t = new Tab(++nextId);
                    t.visit(op[1]);
                    tabs.put(nextId, t);
                    activeId = nextId;
                    break;

                case "SWITCH":
                    int id = Integer.parseInt(op[1]);
                    if (tabs.containsKey(id)) {
                        activeId = id;
                    }
                    break;

                case "CLOSE":
                    if (tabs.containsKey(activeId)) {
                        closed.push(tabs.remove(activeId));

                        if (!tabs.isEmpty()) {
                            activeId = tabs.lastKey();
                        }
                    }
                    break;

                case "RESTORE":
                    if (!closed.isEmpty()) {
                        Tab restored = closed.pop();
                        tabs.put(restored.id, restored);
                        activeId = restored.id;
                    }
                    break;
            }
        }

        if (tabs.isEmpty()) {
            System.out.println("EMPTY");
        } else {
            System.out.println(tabs.size() + " " + activeId);

            boolean first = true;
            for (int id : tabs.keySet()) {
                if (!first) System.out.print(" ");
                System.out.print(id);
                first = false;
            }
            System.out.println();
        }

        sc.close();
    }
}
