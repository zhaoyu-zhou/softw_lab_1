package org.example;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;
import org.graphstream.ui.view.Viewer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class TextProcessor {

    public static void main(String[] args) {
        String filePath = "..//double_01//text.txt";

        try {
            String processedText = processTextFile(filePath);
            System.out.println("Processed Text:");
            System.out.println(processedText);

            Map<String, Map<String, Integer>> directedGraphData = buildDirectedGraph(processedText);
            Graph graph = createGraph(directedGraphData);

            // Console menu
            Scanner scanner = new Scanner(System.in);
            boolean exit = false;
            while (!exit) {
                System.out.println("\nMenu:");
                System.out.println("1. 查看有向图");
                System.out.println("2. 查询桥接词");
                System.out.println("3. 根据桥接词生成新文本");
                System.out.println("4. 计算最短路径");
                System.out.println("5. 随机游走");
                System.out.println("6. 退出");
                System.out.print("请选择操作（1-6）: ");
                int choice = scanner.nextInt();

                switch (choice) {
                    case 1:
                        showDirectedGraph(graph);
                        break;
                    case 2:
                        queryBridgeWords(scanner, graph);
                        break;
                    case 3:
                        generateNewText(scanner, graph);
                        break;
                    case 4:
                        calcShortestPath(scanner, graph);
                        break;
                    case 5:
                        randomWalk(graph);
                        break;
                    case 6:
                        exit = true;
                        break;
                    default:
                        System.out.println("无效的选项，请重新选择。");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String processTextFile(String filePath) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;

        while ((line = reader.readLine()) != null) {
            sb.append(processLine(line)).append(" ");
        }
        reader.close();
        return sb.toString().trim();
    }

    public static String processLine(String line) {
        // Replace punctuation with spaces and remove non-English characters
        return line.replaceAll("[\\p{Punct}]", " ").replaceAll("[^a-zA-Z\\s]", "").toLowerCase();
    }

    public static Map<String, Map<String, Integer>> buildDirectedGraph(String text) {
        String[] words = text.split("\\s+");
        Map<String, Map<String, Integer>> graph = new HashMap<>();

        for (int i = 0; i < words.length - 1; i++) {
            String wordA = words[i];
            String wordB = words[i + 1];

            if (!graph.containsKey(wordA)) {
                graph.put(wordA, new HashMap<>());
            }

            Map<String, Integer> edges = graph.get(wordA);
            edges.put(wordB, edges.getOrDefault(wordB, 0) + 1);
        }

        return graph;
    }

    public static Graph createGraph(Map<String, Map<String, Integer>> graphData) {
        Graph graph = new SingleGraph("Text Graph");

        for (String from : graphData.keySet()) {
            if (graph.getNode(from) == null) {
                Node node = graph.addNode(from);
                node.addAttribute("ui.label", from);
            }
            for (String to : graphData.get(from).keySet()) {
                if (graph.getNode(to) == null) {
                    Node node = graph.addNode(to);
                    node.addAttribute("ui.label", to);
                }
                String edgeId = from + "->" + to;
                if (graph.getEdge(edgeId) == null) {
                    Edge edge = graph.addEdge(edgeId, from, to, true);
                    edge.setAttribute("weight", graphData.get(from).get(to));
                    edge.addAttribute("ui.label", graphData.get(from).get(to));
                }
            }
        }
        return graph;
    }

    public static void showDirectedGraph(Graph graph) {
        graph.addAttribute("ui.stylesheet", "node { fill-color: red; size: 20px; text-alignment: center; text-size: 14px; } edge { text-size: 14px; }");
        graph.addAttribute("ui.quality");
        graph.addAttribute("ui.antialias");

        Viewer viewer = graph.display();
        viewer.enableAutoLayout(new SpringBox());
    }

    public static void queryBridgeWords(Scanner scanner, Graph graph) {
        System.out.print("请输入第一个单词：");
        String word1 = scanner.next().toLowerCase();
        System.out.print("请输入第二个单词： ");
        String word2 = scanner.next().toLowerCase();

        Node startNode = graph.getNode(word1);
        Node targetNode = graph.getNode(word2);

        if (startNode == null || targetNode == null) {
            System.out.println("起始单词或目标单词不在图中。");
            return;
        }

        List<String> bridgeWords = findBridgeWords(graph, word1, word2);
        if (bridgeWords.isEmpty()) {
            System.out.println("没有找到桥接词。");
        } else {
            System.out.println("桥接词： " + String.join(", ", bridgeWords));
        }
    }

    public static void generateNewText(Scanner scanner, Graph graph) {
        System.out.print("请输入新文本：");
        scanner.nextLine(); // Clear the input buffer
        String newText = scanner.nextLine().toLowerCase();

        String result = generateNewText(graph, newText);
        System.out.println("生成的新文本：");
        System.out.println(result);
    }

    public static void calcShortestPath(Scanner scanner, Graph graph) {
        System.out.print("请输入起始单词：");
        String startWord = scanner.next().toLowerCase();
        System.out.print("请输入目标单词：");
        String targetWord = scanner.next().toLowerCase();

        Node startNode = graph.getNode(startWord);
        Node targetNode = graph.getNode(targetWord);

        if (startNode == null || targetNode == null) {
            System.out.println("起始单词或目标单词不在图中。");
            return;
        }

        List<Node> shortestPath = dijkstraShortestPath(graph, startNode, targetNode);
        if (shortestPath.isEmpty()) {
            System.out.println("起始单词到目标单词之间无最短路径。");
            return;
        }

        System.out.println("最短路径：");
        for (Node node : shortestPath) {
            System.out.print(node.getId() + " ");
        }
        System.out.println();

        // 高亮最短路径
        highlightPath(graph, shortestPath);
    }

    public static List<String> findBridgeWords(Graph graph, String word1, String word2) {
        Node node1 = graph.getNode(word1);
        Node node2 = graph.getNode(word2);

        if (node1 == null || node2 == null) {
            return Collections.emptyList();
        }

        List<String> bridgeWords = new ArrayList<>();

        for (Edge edge1 : node1.getLeavingEdgeSet()) {
            Node intermediate = edge1.getTargetNode();
            for (Edge edge2 : intermediate.getLeavingEdgeSet()) {
                if (edge2.getTargetNode().equals(node2)) {
                    bridgeWords.add(intermediate.getId());
                }
            }
        }

        return bridgeWords;
    }

    public static String generateNewText(Graph graph, String newText) {
        String[] words = newText.split("\\s+");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < words.length - 1; i++) {
            result.append(words[i]).append(" ");

            String wordA = words[i];
            String wordB = words[i + 1];

            List<String> bridgeWords = findBridgeWords(graph, wordA, wordB);
            if (!bridgeWords.isEmpty()) {
                Random random = new Random();
                String bridgeWord = bridgeWords.get(random.nextInt(bridgeWords.size()));
                result.append(bridgeWord).append(" ");
            }
        }
        result.append(words[words.length - 1]);

        return result.toString();
    }

    public static List<Node> dijkstraShortestPath(Graph graph, Node startNode, Node targetNode) {
        Map<Node, Integer> distances = new HashMap<>();
        Map<Node, Node> previousNodes = new HashMap<>();
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(distances::get));

        for (Node node : graph) {
            distances.put(node, Integer.MAX_VALUE);
            previousNodes.put(node, null);
        }
        distances.put(startNode, 0);
        priorityQueue.add(startNode);

        while (!priorityQueue.isEmpty()) {
            Node currentNode = priorityQueue.poll();

            if (currentNode.equals(targetNode)) {
                break;
            }

            for (Edge edge : currentNode.getLeavingEdgeSet()) {
                Node neighbor = edge.getTargetNode();
                Integer edgeWeight = edge.getAttribute("weight");
                if (edgeWeight == null) {
                    continue;  // Skip if edge weight is null
                }

                int newDist = distances.get(currentNode) + edgeWeight;
                if (newDist < distances.get(neighbor)) {
                    distances.put(neighbor, newDist);
                    previousNodes.put(neighbor, currentNode);
                    priorityQueue.add(neighbor);
                }
            }
        }

        List<Node> path = new ArrayList<>();
        for (Node at = targetNode; at != null; at = previousNodes.get(at)) {
            path.add(at);
        }
        Collections.reverse(path);

        if (path.get(0).equals(startNode)) {
            return path;
        } else {
            return Collections.emptyList();
        }
    }

    public static void highlightPath(Graph graph, List<Node> path) {
        for (Edge edge : graph.getEdgeSet()) {
            edge.removeAttribute("ui.style");
        }

        for (int i = 0; i < path.size() - 1; i++) {
            Node from = path.get(i);
            Node to = path.get(i + 1);
            Edge edge = graph.getEdge(from.getId() + "->" + to.getId());
            if (edge != null) {
                edge.addAttribute("ui.style", "fill-color: green;");
            }
        }
    }

    public static void randomWalk(Graph graph) {
        List<Node> walkPath = new ArrayList<>();
        Set<Edge> visitedEdges = new HashSet<>();
        Random random = new Random();

        List<Node> nodes = new ArrayList<>();
        for (Node node : graph) {
            nodes.add(node);
        }

        Node currentNode = nodes.get(random.nextInt(nodes.size()));
        walkPath.add(currentNode);

        System.out.println("开始: " + currentNode.getId());

        while (true) {
            System.out.print("按下Enter 继续, 或输入'stop'停止: ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("stop")) {
                break;
            }

            List<Edge> leavingEdges = new ArrayList<>(currentNode.getLeavingEdgeSet());
            if (leavingEdges.isEmpty()) {
                System.out.println("No further nodes to walk to from " + currentNode.getId());
                break;
            }

            Edge randomEdge = leavingEdges.get(random.nextInt(leavingEdges.size()));
            if (visitedEdges.contains(randomEdge)) {
                System.out.println("Encountered a previously visited edge. Stopping walk.");
                break;
            }

            visitedEdges.add(randomEdge);
            currentNode = randomEdge.getTargetNode();
            walkPath.add(currentNode);


            System.out.println("走到: " + currentNode.getId());
        }

        System.out.println("随机游走路径:");
        for (Node node : walkPath) {
            System.out.print(node.getId() + " ");
        }
        System.out.println();

        try (FileWriter writer = new FileWriter("random_walk.txt")) {
            for (Node node : walkPath) {
                writer.write(node.getId() + " ");
            }
            System.out.println("路径保存到random_walk.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}