import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

class Task {
    private int id;
    private String title;
    private int priority;
    private String category;
    private boolean completed;

    public Task(int id, String title, int priority, String category) {
        this.id = id;
        this.title = title;
        this.priority = priority;
        this.category = category;
        this.completed = false;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public int getPriority() { return priority; }
    public String getCategory() { return category; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean status) { completed = status; }

    @Override
    public String toString() {
        return String.format("Task [ID=%d, Title=%s, Priority=%d, Category=%s, Completed=%s]",
                id, title, priority, category, completed);
    }
}

class HistoryManager {
    public enum Action { ADD, REMOVE, COMPLETE }
    
    private static class Node {
        Action action;
        Task task;
        Node prev, next;

        Node(Action action, Task task) {
            this.action = action;
            this.task = task;
        }
    }

    private Node current;

    public void recordAction(Action action, Task task) {
        Node newNode = new Node(action, task);
        if (current != null) {
            current.next = newNode;
            newNode.prev = current;
        }
        current = newNode;
    }

    public Action undo() {
        if (current == null) return null;
        Action action = current.action;
        current = current.prev;
        return action;
    }

    public Task getUndoneTask() {
        return current != null && current.next != null ? current.next.task : null;
    }
}

class PriorityQueue {
    private final TreeSet<Task> queue;

    public PriorityQueue() {
        queue = new TreeSet<>((a, b) -> {
            int priorityCompare = Integer.compare(b.getPriority(), a.getPriority());
            return priorityCompare != 0 ? priorityCompare : Integer.compare(a.getId(), b.getId());
        });
    }

    public void add(Task task) { queue.add(task); }
    public Task poll() { return queue.pollFirst(); }
    public Task peek() { return queue.isEmpty() ? null : queue.first(); }
    public boolean remove(Task task) { return queue.remove(task); }
    public boolean isEmpty() { return queue.isEmpty(); }
}

class TodoList {
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<String, Set<Integer>> categories = new HashMap<>();
    private final PriorityQueue priorityQueue = new PriorityQueue();
    private final HistoryManager history = new HistoryManager();
    private int nextId = 1;

    public void addTask(String title, int priority, String category) {
        Task task = new Task(nextId++, title, priority, category);
        tasks.put(task.getId(), task);
        categories.computeIfAbsent(category, k -> new HashSet<>()).add(task.getId());
        priorityQueue.add(task);
        history.recordAction(HistoryManager.Action.ADD, task);
    }

    public boolean removeTask(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            Set<Integer> categoryTasks = categories.get(task.getCategory());
            if (categoryTasks != null) categoryTasks.remove(id);
            priorityQueue.remove(task);
            history.recordAction(HistoryManager.Action.REMOVE, task);
            return true;
        }
        return false;
    }

    public String undo() {
        HistoryManager.Action action = history.undo();
        Task task = history.getUndoneTask();
        
        if (action == null || task == null) return "Nothing to undo";
        
        switch (action) {
            case ADD: internalRemove(task); break;
            case REMOVE: internalAdd(task); break;
            case COMPLETE: task.setCompleted(false); break;
        }
        return "Undid action: " + action + " on Task " + task.getId();
    }

    private void internalAdd(Task task) {
        tasks.put(task.getId(), task);
        categories.computeIfAbsent(task.getCategory(), k -> new HashSet<>()).add(task.getId());
        priorityQueue.add(task);
    }

    private void internalRemove(Task task) {
        tasks.remove(task.getId());
        Set<Integer> categoryTasks = categories.get(task.getCategory());
        if (categoryTasks != null) categoryTasks.remove(task.getId());
        priorityQueue.remove(task);
    }

    public List<Task> getTasksByCategory(String category) {
        List<Task> result = new ArrayList<>();
        if (category.isEmpty()) {
            result.addAll(tasks.values());
        } else {
            for (Integer id : categories.getOrDefault(category, new HashSet<>())) {
                Task task = tasks.get(id);
                if (task != null) result.add(task);
            }
        }
        return result;
    }

    public Task getHighestPriorityTask() {
        return priorityQueue.peek();
    }
}

public class TodoListGUI extends JFrame {
    private TodoList todo = new TodoList();

    public TodoListGUI() {
        setTitle("Todo List Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        initUI();
        setLocationRelativeTo(null);
    }

    private void initUI() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JButton addButton = new JButton("Add Task");
        JButton removeButton = new JButton("Remove Task");
        JButton undoButton = new JButton("Undo");
        JButton showButton = new JButton("Show Tasks");
        JButton exitButton = new JButton("Exit");

        addButton.addActionListener(e -> addTask());
        removeButton.addActionListener(e -> removeTask());
        undoButton.addActionListener(e -> undo());
        showButton.addActionListener(e -> showTasks());
        exitButton.addActionListener(e -> System.exit(0));

        panel.add(addButton);
        panel.add(removeButton);
        panel.add(undoButton);
        panel.add(showButton);
        panel.add(exitButton);

        add(panel);
    }

    private void addTask() {
        JTextField titleField = new JTextField();
        JComboBox<Integer> priorityCombo = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        JTextField categoryField = new JTextField();

        JPanel inputPanel = new JPanel(new GridLayout(0, 1));
        inputPanel.add(new JLabel("Title:"));
        inputPanel.add(titleField);
        inputPanel.add(new JLabel("Priority:"));
        inputPanel.add(priorityCombo);
        inputPanel.add(new JLabel("Category:"));
        inputPanel.add(categoryField);

        int result = JOptionPane.showConfirmDialog(this, inputPanel, "Add Task", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String title = titleField.getText().trim();
            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Title cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            int priority = (Integer) priorityCombo.getSelectedItem();
            String category = categoryField.getText().trim();
            todo.addTask(title, priority, category);
            JOptionPane.showMessageDialog(this, "Task added successfully!");
        }
    }

    private void removeTask() {
        String input = JOptionPane.showInputDialog(this, "Enter Task ID to remove:");
        if (input == null || input.trim().isEmpty()) return;
        
        try {
            int taskId = Integer.parseInt(input.trim());
            if (todo.removeTask(taskId)) {
                JOptionPane.showMessageDialog(this, "Task removed successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Task not found!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid Task ID!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void undo() {
        String result = todo.undo();
        JOptionPane.showMessageDialog(this, result);
    }

    private void showTasks() {
        String category = JOptionPane.showInputDialog(this, "Enter category (leave empty for all):");
        if (category == null) return; // User canceled
        
        List<Task> tasks = todo.getTasksByCategory(category.trim());
        StringBuilder sb = new StringBuilder();
        
        if (tasks.isEmpty()) {
            sb.append("No tasks found.");
        } else {
            for (Task task : tasks) {
                sb.append(task).append("\n");
            }
        }

        JTextArea textArea = new JTextArea(sb.toString(), 20, 50);
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        JOptionPane.showMessageDialog(this, scrollPane, "Tasks", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TodoListGUI().setVisible(true));
    }
}