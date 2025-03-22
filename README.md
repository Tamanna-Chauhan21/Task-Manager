# Todo List Manager (Java Swing + DSA) 📝✅  
A priority-based task manager with undo functionality, built using Java Swing and core data structures.  

## 📌 Description  
A desktop application that combines Java Swing with Data Structures & Algorithms to manage tasks with:  
- Priority-based scheduling (using a custom TreeSet priority queue)  
- Undo/redo functionality (doubly linked list implementation)  
- Category-based filtering (HashMap + HashSet)  
- Persistent task history  

## ✨ Key Features  
- Priority Queue: Tasks automatically sorted by priority (1-5) using a TreeSet with custom comparator.  
- Undo/Redo System: History manager using a **doubly linked list to track actions (add/remove/complete).  
- Category Filtering: View tasks by category (e.g., "Work", "Personal") with O(1) lookup via HashMaps.  
- GUI Controls:  
  - Add/remove tasks with input validation  
  - Real-time task listing with scrollable view  
  - Instant priority updates  

## 🛠 Tech Stack  
- Core DSA:  
  - TreeSet (Priority Queue) for task prioritization  
  - HashMap + HashSet for category management  
  - Custom doubly linked list for undo/redo history  
- GUI: Java Swing (JFrame, JOptionPane, event listeners)  
- OOP Principles: Encapsulation, abstraction, and polymorphism
