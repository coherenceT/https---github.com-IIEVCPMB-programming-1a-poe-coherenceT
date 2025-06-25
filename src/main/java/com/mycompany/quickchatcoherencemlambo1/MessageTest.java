/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quickchatcoherencemlambo1;

/**
 *
 * @author lab_services_student
 */
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.regex.Pattern;
import org.json.simple.JSONObject;

class MessageTest {

    @Test
    void testValidRecipient() {
        assertTrue(Message.isValidRecipient("+27123456789"));
        assertTrue(Message.isValidRecipient("1234567890"));
        assertTrue(Message.isValidRecipient("+441234567890"));
    }

    @Test
    void testInvalidRecipient() {
        assertFalse(Message.isValidRecipient("abc"));
        assertFalse(Message.isValidRecipient("123"));
        assertFalse(Message.isValidRecipient("+1234567890123")); // too long (>13 digits)
        assertFalse(Message.isValidRecipient(null));
    }

    @Test
    void testMessageLengthValidation() {
        assertEquals("Success", Message.checkMessageLength("Hello"));
        
        StringBuilder longMsg = new StringBuilder();
        for (int i = 0; i < 300; i++) longMsg.append("a");
        
        String result = Message.checkMessageLength(longMsg.toString());
        assertTrue(result.contains("too long"));
        assertTrue(result.contains("300"));
    }

    @Test
    void testMessageIDGeneration() {
        String id = Message.generateMessageID();
        assertNotNull(id);
        // UUID length is 36 characters
        assertEquals(36, id.length());
    }

    @Test
    void testMessageHashing() {
        Message msg = new Message("1234567890", 1, "+27123456789", "Test message", "user1");
        String hash = msg.createMessageHash();
        assertNotNull(hash);
        // SHA-256 produces 64 hex characters
        assertEquals(64, hash.length());
        assertTrue(Pattern.matches("[a-f0-9]{64}", hash));
    }

    @Test
    void testJSONConversion() {
        Message msg = new Message("9876543210", 2, "+441234567890", "JSON test", "user2");
        JSONObject json = msg.toJSON();
        
        assertEquals("9876543210", json.get("messageID"));
        // JSON-simple stores integers as Long
        assertEquals(2L, json.get("messageNumber"));
        assertEquals("+441234567890", json.get("recipient"));
        assertEquals("JSON test", json.get("content"));
        assertEquals("user2", json.get("sender"));
        assertNotNull(json.get("timestamp"));
    }
}

