/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quickchatcoherencemlambo1;

/**
 *
 * @author lab_services_student
 */

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class AccountManager {
    private static final String ACCOUNTS_FILE = "accounts.json";
    // Map username -> hashedPasswordHex
    private final Map<String, String> accounts = new HashMap<>();

    public AccountManager() {
        loadAccountsFromFile();
    }

    @SuppressWarnings("unchecked")
    private void loadAccountsFromFile() {
        File file = new File(ACCOUNTS_FILE);
        if (!file.exists()) return;
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(file)) {
            JSONArray array = (JSONArray) parser.parse(reader);
            for (Object obj : array) {
                JSONObject json = (JSONObject) obj;
                String username = (String) json.get("username");
                String hash = (String) json.get("passwordHash");
                if (username != null && hash != null) {
                    accounts.put(username, hash);
                }
            }
        } catch (IOException | ParseException e) {
            JOptionPane.showMessageDialog(null, "Error loading accounts: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void saveAccountsToFile() {
        JSONArray array = new JSONArray();
        for (Map.Entry<String, String> entry : accounts.entrySet()) {
            JSONObject json = new JSONObject();
            json.put("username", entry.getKey());
            json.put("passwordHash", entry.getValue());
            array.add(json);
        }
        try (FileWriter writer = new FileWriter(ACCOUNTS_FILE)) {
            writer.write(array.toJSONString());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving accounts: " + e.getMessage());
        }
    }

    public boolean registerNewUser() {
        while (true) {
            String username = JOptionPane.showInputDialog(null, "Register - Enter new username:");
            if (username == null) {
                return false; // user cancelled
            }
            username = username.trim();
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Username cannot be empty.");
                continue;
            }
            if (accounts.containsKey(username)) {
                JOptionPane.showMessageDialog(null, "Username already exists. Choose another.");
                continue;
            }
            // Ask password
            JPasswordField pf1 = new JPasswordField();
            JPasswordField pf2 = new JPasswordField();
            int ok = JOptionPane.showConfirmDialog(null, pf1, "Enter password:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (ok != JOptionPane.OK_OPTION) return false;
            String pw1 = new String(pf1.getPassword());
            if (pw1.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Password cannot be empty.");
                continue;
            }
            ok = JOptionPane.showConfirmDialog(null, pf2, "Confirm password:", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (ok != JOptionPane.OK_OPTION) return false;
            String pw2 = new String(pf2.getPassword());
            if (!pw1.equals(pw2)) {
                JOptionPane.showMessageDialog(null, "Passwords do not match.");
                continue;
            }
            // Hash password
            String hashed = hashPassword(pw1);
            if (hashed == null) {
                JOptionPane.showMessageDialog(null, "Error in password hashing.");
                return false;
            }
            accounts.put(username, hashed);
            saveAccountsToFile();
            JOptionPane.showMessageDialog(null, "Registration successful. You can now login.");
            return true;
        }
    }

    public String loginUser() {
        for (int attempts = 0; attempts < 3; attempts++) {
            String username = JOptionPane.showInputDialog(null, "Login - Enter username:");
            if (username == null) {
                return null;
            }
            username = username.trim();
            if (!accounts.containsKey(username)) {
                JOptionPane.showMessageDialog(null, "Username not found.");
                continue;
            }
            JPasswordField pf = new JPasswordField();
            int ok = JOptionPane.showConfirmDialog(null, pf, "Enter password for " + username + ":", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (ok != JOptionPane.OK_OPTION) {
                return null;
            }
            String pw = new String(pf.getPassword());
            String hashed = hashPassword(pw);
            if (hashed != null && hashed.equals(accounts.get(username))) {
                JOptionPane.showMessageDialog(null, "Login successful. Welcome, " + username + "!");
                return username;
            } else {
                JOptionPane.showMessageDialog(null, "Incorrect password.");
            }
        }
        JOptionPane.showMessageDialog(null, "Too many failed attempts. Exiting.");
        return null;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(password.getBytes());
            // convert to hex
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public Set<String> getAllUsernames() {
        return Collections.unmodifiableSet(accounts.keySet());
    }
}
