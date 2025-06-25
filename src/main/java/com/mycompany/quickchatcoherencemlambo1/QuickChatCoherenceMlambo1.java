package com.mycompany.quickchatcoherencemlambo1;


import javax.swing.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class QuickChatCoherenceMlambo1 {
    private static final List<Message> sentMessages = new ArrayList<>();
    private static final List<Message> storedMessages = new ArrayList<>();
    private static final List<Message> disregardedMessages = new ArrayList<>();
    private static final Set<String> messageHashes = new HashSet<>();
    private static final Set<String> messageIDs = new HashSet<>();
    private static int totalMessagesSent = 0;
    private static String currentUser;
    private static AccountManager accountManager;

    public static void main(String[] args) {
        accountManager = new AccountManager();
        // Initial prompt: Login or Register
        String[] options = {"Login", "Register", "Exit"};
        int choice = JOptionPane.showOptionDialog(null,
                "Welcome to QuickChat by Coherence Tasimba Mlambo.\nChoose an option:",
                "QuickChat",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);
        if (choice == 2 || choice == JOptionPane.CLOSED_OPTION) {
            System.exit(0);
        }
        if (choice == 1) { // Register
            boolean registered = accountManager.registerNewUser();
            if (!registered) System.exit(0);
            // After registration, go to login
        }
        // Login flow
        currentUser = accountManager.loginUser();
        if (currentUser == null) {
            System.exit(0);
        }
        // After successful login
        loadMessagesFromFile();

        // Main application loop
        while (true) {
            String menu = "QuickChat Menu:\n"
                    + "1) Send Messages\n"
                    + "2) View Recent Messages\n"
                    + "3) Advanced Features\n"
                    + "4) Logout and Exit";
            
            String input = JOptionPane.showInputDialog(null, menu);
            if (input == null) System.exit(0);
            switch (input) {
                case "1": sendMessages(); break;
                case "2": displayRecentMessages(); break;
                case "3": showAdvancedMenu(); break;
                case "4": exitApplication(); break;
                default: JOptionPane.showMessageDialog(null, "Invalid choice!");
            }
        }
    }

    private static void sendMessages() {
        int numMessages = getMessageCount();
        if (numMessages <= 0) return;
        
        for (int i = 0; i < numMessages; i++) {
            // Get recipient
            String recipient = getValidRecipient();
            if (recipient == null) return;
            
            // Get content
            String content = getValidContent();
            if (content == null) return;
            
            // Create message
            String messageId = generateUniqueID();
            int messageNum = totalMessagesSent + i + 1;
            Message msg = new Message(messageId, messageNum, recipient, content, currentUser);
            
            // Process message
            String action = msg.selectMessageAction();
            if (action == null) continue;
            
            switch (action) {
                case "Send Message":
                    sentMessages.add(msg);
                    messageHashes.add(msg.createMessageHash());
                    messageIDs.add(messageId);
                    totalMessagesSent++;
                    JOptionPane.showMessageDialog(null, "Message sent!\n" + msg.shortDetails());
                    break;
                case "Store Message":
                    storedMessages.add(msg);
                    messageHashes.add(msg.createMessageHash());
                    messageIDs.add(messageId);
                    JOptionPane.showMessageDialog(null, "Message stored!");
                    break;
                case "Disregard Message":
                    disregardedMessages.add(msg);
                    JOptionPane.showMessageDialog(null, "Message disregarded.");
                    break;
            }
        }
        saveMessagesToFile();
    }

    private static int getMessageCount() {
        while (true) {
            String input = JOptionPane.showInputDialog("How many messages do you want to send?");
            if (input == null) return 0;
            
            try {
                int count = Integer.parseInt(input);
                if (count > 0) return count;
                JOptionPane.showMessageDialog(null, "Please enter a positive number");
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid number format");
            }
        }
    }

    private static String getValidRecipient() {
        Set<String> usernames = accountManager.getAllUsernames();
        while (true) {
            String recipient = JOptionPane.showInputDialog("Enter recipient username:");
            if (recipient == null) return null;
            recipient = recipient.trim();
            if (recipient.equals(currentUser)) {
                JOptionPane.showMessageDialog(null, "Cannot send to yourself. Choose another user.");
                continue;
            }
            if (usernames.contains(recipient)) {
                return recipient;
            }
            JOptionPane.showMessageDialog(null, "Recipient not found. Make sure the user is registered.");
        }
    }

    private static String getValidContent() {
        while (true) {
            String content = JOptionPane.showInputDialog("Enter message (max 250 characters):");
            if (content == null) return null;
            
            String validation = Message.checkMessageLength(content);
            if (validation.equals("Success")) {
                return content;
            }
            JOptionPane.showMessageDialog(null, validation);
        }
    }

    private static String generateUniqueID() {
        String id;
        do {
            id = Message.generateMessageID();
        } while (messageIDs.contains(id));
        return id;
    }

    // File handling methods
    @SuppressWarnings("unchecked")
    private static void loadMessagesFromFile() {
        File file = new File("chat_history.json");
        if (!file.exists()) return;
        
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(file)) {
            JSONArray jsonArray = (JSONArray) parser.parse(reader);
            // Clear current lists
            sentMessages.clear();
            storedMessages.clear();
            disregardedMessages.clear();
            messageIDs.clear();
            messageHashes.clear();
            totalMessagesSent = 0;
            
            for (Object obj : jsonArray) {
                JSONObject json = (JSONObject) obj;
                String id = (String) json.get("messageID");
                int num = ((Long) json.get("messageNumber")).intValue();
                String recipient = (String) json.get("recipient");
                String content = (String) json.get("content");
                String sender = (String) json.get("sender");
                String status = (String) json.get("status");
                String timestamp = (String) json.get("timestamp");
                
                // Only load messages where sender or recipient is currentUser
                if (!currentUser.equals(sender) && !currentUser.equals(recipient)) {
                    continue;
                }
                
                Message msg = new Message(id, num, recipient, content, sender, timestamp);
                switch (status) {
                    case "sent": sentMessages.add(msg); totalMessagesSent++; break;
                    case "stored": storedMessages.add(msg); break;
                    case "disregarded": disregardedMessages.add(msg); break;
                }
                
                messageIDs.add(id);
                messageHashes.add(msg.createMessageHash());
            }
        } catch (IOException | ParseException e) {
            JOptionPane.showMessageDialog(null, "Error loading messages: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static void saveMessagesToFile() {
        // Read existing file, but preserve entries for other users
        JSONArray allArray = new JSONArray();
        File file = new File("chat_history.json");
        JSONParser parser = new JSONParser();
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                JSONArray existing = (JSONArray) parser.parse(reader);
                // Keep entries not related to currentUser
                for (Object obj : existing) {
                    JSONObject json = (JSONObject) obj;
                    String sender = (String) json.get("sender");
                    String recipient = (String) json.get("recipient");
                    if (currentUser.equals(sender) || currentUser.equals(recipient)) {
                        continue; // we will re-add updated ones
                    }
                    allArray.add(json);
                }
            } catch (IOException | ParseException e) {
                JOptionPane.showMessageDialog(null, "Error reading existing history: " + e.getMessage());
            }
        }
        // Add current user's messages
        addMessagesToArray(allArray, sentMessages, "sent");
        addMessagesToArray(allArray, storedMessages, "stored");
        addMessagesToArray(allArray, disregardedMessages, "disregarded");
        
        try (FileWriter fw = new FileWriter("chat_history.json")) {
            fw.write(allArray.toJSONString());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving messages: " + e.getMessage());
        }
    }

    private static void addMessagesToArray(JSONArray array, List<Message> messages, String status) {
        for (Message msg : messages) {
            JSONObject json = msg.toJSON();
            json.put("status", status);
            array.add(json);
        }
    }

    private static void displayRecentMessages() {
        StringBuilder sb = new StringBuilder("=== MESSAGE HISTORY for " + currentUser + " ===\n\n");
        
        if (sentMessages.isEmpty() && storedMessages.isEmpty() && disregardedMessages.isEmpty()) {
            sb.append("No messages found");
        } else {
            sb.append("SENT MESSAGES:\n");
            for (Message msg : sentMessages) {
                sb.append(msg.shortDetails()).append("\n\n");
            }
            
            sb.append("\nSTORED MESSAGES:\n");
            for (Message msg : storedMessages) {
                sb.append(msg.shortDetails()).append("\n\n");
            }
            
            sb.append("\nDISREGARDED MESSAGES:\n");
            for (Message msg : disregardedMessages) {
                sb.append(msg.shortDetails()).append("\n\n");
            }
        }
        
        JTextArea textArea = new JTextArea(sb.toString());
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setEditable(false);
        JOptionPane.showMessageDialog(null, scrollPane, "Message History", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void showAdvancedMenu() {
        while (true) {
            String menu = "ADVANCED FEATURES:\n"
                    + "1) Sender/Recipient Pairs\n"
                    + "2) Longest Message\n"
                    + "3) Search by Message ID\n"
                    + "4) Search by Recipient\n"
                    + "5) Delete by Hash\n"
                    + "6) Full Message Report\n"
                    + "7) Back to Main Menu";
            
            String choice = JOptionPane.showInputDialog(null, menu);
            if (choice == null || choice.equals("7")) return;
            
            switch (choice) {
                case "1": showSenderRecipients(); break;
                case "2": showLongestMessage(); break;
                case "3": searchByID(); break;
                case "4": searchByRecipient(); break;
                case "5": deleteByHash(); break;
                case "6": showFullReport(); break;
                default: JOptionPane.showMessageDialog(null, "Invalid choice!");
            }
        }
    }

    private static void showSenderRecipients() {
        StringBuilder sb = new StringBuilder("SENDER/RECIPIENT PAIRS (for sent messages):\n\n");
        for (Message msg : sentMessages) {
            sb.append(msg.getSender()).append(" → ").append(msg.getRecipient()).append("\n");
        }
        showResult(sb.toString(), "Sender/Recipient Pairs");
    }

    private static void showLongestMessage() {
        if (sentMessages.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No sent messages found!");
            return;
        }
        
        Message longest = Collections.max(sentMessages, 
            Comparator.comparingInt(m -> m.getContent().length()));
            
        String result = "LONGEST MESSAGE (" + longest.getContent().length() + " chars):\n" +
                        "From: " + longest.getSender() + "\n" +
                        "To: " + longest.getRecipient() + "\n" +
                        "Content: " + longest.getContent();
        showResult(result, "Longest Message");
    }

    private static void searchByID() {
        String id = JOptionPane.showInputDialog("Enter Message ID:");
        if (id == null || id.trim().isEmpty()) return;
        
        for (Message msg : sentMessages) {
            if (msg.getMessageID().equals(id)) {
                showResult(msg.fullMessageDetails(), "Message Found");
                return;
            }
        }
        JOptionPane.showMessageDialog(null, "Message not found");
    }

    private static void searchByRecipient() {
        String recipient = JOptionPane.showInputDialog("Enter recipient username:");
        if (recipient == null) return;
        
        StringBuilder sb = new StringBuilder("MESSAGES TO " + recipient + ":\n\n");
        for (Message msg : sentMessages) {
            if (msg.getRecipient().equals(recipient)) {
                sb.append("From: ").append(msg.getSender()).append("\n")
                  .append("Message: ").append(msg.getContent()).append("\n\n");
            }
        }
        showResult(sb.toString(), "Messages to " + recipient);
    }

    private static void deleteByHash() {
        String hash = JOptionPane.showInputDialog("Enter message hash:");
        if (hash == null) return;
        
        Iterator<Message> iterator = sentMessages.iterator();
        while (iterator.hasNext()) {
            Message msg = iterator.next();
            if (msg.createMessageHash().equals(hash)) {
                iterator.remove();
                messageHashes.remove(hash);
                messageIDs.remove(msg.getMessageID());
                saveMessagesToFile();
                JOptionPane.showMessageDialog(null, "Message deleted");
                return;
            }
        }
        JOptionPane.showMessageDialog(null, "Message not found");
    }

    private static void showFullReport() {
        StringBuilder sb = new StringBuilder("FULL MESSAGE REPORT (sent messages):\n\n");
        for (Message msg : sentMessages) {
            sb.append("Hash: ").append(msg.createMessageHash()).append("\n")
              .append("Recipient: ").append(msg.getRecipient()).append("\n")
              .append("Content: ").append(msg.getContent()).append("\n\n");
        }
        showResult(sb.toString(), "Full Message Report");
    }

    private static void showResult(String content, String title) {
        JTextArea textArea = new JTextArea(content);
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setEditable(false);
        JOptionPane.showMessageDialog(null, scrollPane, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private static void exitApplication() {
        JOptionPane.showMessageDialog(null, 
            "Goodbye, " + currentUser + "!\nTotal messages sent this session: " + totalMessagesSent);
        System.exit(0);
    }
}