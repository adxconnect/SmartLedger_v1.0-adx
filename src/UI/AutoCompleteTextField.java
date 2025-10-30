package src.UI;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.function.Supplier;

/**
 * Text field with autocomplete dropdown based on cached values
 */
public class AutoCompleteTextField extends JTextField {
    
    private JPopupMenu popup;
    private Supplier<List<String>> suggestionProvider;
    private boolean isAdjusting = false;
    
    public AutoCompleteTextField(int columns, Supplier<List<String>> suggestionProvider) {
        super(columns);
        this.suggestionProvider = suggestionProvider;
        initAutocomplete();
    }
    
    private void initAutocomplete() {
        popup = new JPopupMenu();
        
        getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!isAdjusting) {
                    SwingUtilities.invokeLater(() -> showSuggestions());
                }
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!isAdjusting) {
                    SwingUtilities.invokeLater(() -> showSuggestions());
                }
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) {
                if (!isAdjusting) {
                    SwingUtilities.invokeLater(() -> showSuggestions());
                }
            }
        });
        
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                popup.setVisible(false);
            }
        });
        
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    popup.setVisible(false);
                }
            }
        });
    }
    
    private void showSuggestions() {
        String text = getText().trim();
        if (text.isEmpty()) {
            popup.setVisible(false);
            return;
        }
        
        List<String> allSuggestions = suggestionProvider.get();
        if (allSuggestions == null || allSuggestions.isEmpty()) {
            popup.setVisible(false);
            return;
        }
        
        // Filter suggestions based on current text
        List<String> filtered = allSuggestions.stream()
            .filter(s -> s.toLowerCase().contains(text.toLowerCase()))
            .limit(5) // Show max 5 suggestions
            .toList();
        
        if (filtered.isEmpty()) {
            popup.setVisible(false);
            return;
        }
        
        popup.removeAll();
        
        for (String suggestion : filtered) {
            JMenuItem item = new JMenuItem(suggestion);
            item.addActionListener(e -> {
                isAdjusting = true;
                setText(suggestion);
                isAdjusting = false;
                popup.setVisible(false);
                // Move cursor to end
                setCaretPosition(suggestion.length());
            });
            popup.add(item);
        }
        
        if (popup.getComponentCount() > 0) {
            popup.show(this, 0, getHeight());
            popup.setPopupSize(getWidth(), popup.getPreferredSize().height);
        }
    }
    
    /**
     * Sets initial text without triggering autocomplete
     */
    public void setTextQuietly(String text) {
        isAdjusting = true;
        setText(text);
        isAdjusting = false;
    }
}
