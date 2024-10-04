package nz.ac.massey.cs251;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyleConstants;
import javax.swing.text.SimpleAttributeSet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.io.*;

public class TextEditor2 extends JFrame implements ActionListener {
    private JTextPane textPane; // 使用 JTextPane 以支持丰富文本编辑功能
    private JMenuBar menuBar;
    private JMenu fileMenu, editMenu, formatMenu, helpMenu;
    private JMenuItem newItem, openItem, saveItem, printItem, exitItem;
    private JMenuItem copyItem, cutItem, pasteItem, searchItem;
    private JMenuItem boldItem, italicItem, underlineItem, fontColorItem;
    private JMenuItem aboutItem;
    private JFileChooser fileChooser;
    private String fileName;

    public TextEditor2() {
        // 设置窗口标题
        setTitle("Advanced Text Editor");

        // 初始化文本区域和文件选择器
        textPane = new JTextPane();
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Documents", "txt"));

        // 将文本区域添加到滚动面板中
        add(new JScrollPane(textPane), BorderLayout.CENTER);

        // 创建菜单栏
        menuBar = new JMenuBar();

        // 创建文件菜单
        fileMenu = new JMenu("File");
        newItem = new JMenuItem("New");
        openItem = new JMenuItem("Open");
        saveItem = new JMenuItem("Save");
        printItem = new JMenuItem("Print");
        exitItem = new JMenuItem("Exit");
        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(printItem);
        fileMenu.add(exitItem);

        newItem.addActionListener(this);
        openItem.addActionListener(this);
        saveItem.addActionListener(this);
        printItem.addActionListener(this);
        exitItem.addActionListener(this);

        // 创建编辑菜单
        editMenu = new JMenu("Edit");
        copyItem = new JMenuItem(new DefaultEditorKit.CopyAction());
        copyItem.setText("Copy");
        cutItem = new JMenuItem(new DefaultEditorKit.CutAction());
        cutItem.setText("Cut");
        pasteItem = new JMenuItem(new DefaultEditorKit.PasteAction());
        pasteItem.setText("Paste");
        searchItem = new JMenuItem("Search");
        editMenu.add(copyItem);
        editMenu.add(cutItem);
        editMenu.add(pasteItem);
        editMenu.add(searchItem);

        searchItem.addActionListener(this);

        // 创建格式菜单
        formatMenu = new JMenu("Format");
        boldItem = new JMenuItem("Bold");
        italicItem = new JMenuItem("Italic");
        underlineItem = new JMenuItem("Underline");
        fontColorItem = new JMenuItem("Font Color");
        formatMenu.add(boldItem);
        formatMenu.add(italicItem);
        formatMenu.add(underlineItem);
        formatMenu.add(fontColorItem);

        boldItem.addActionListener(this);
        italicItem.addActionListener(this);
        underlineItem.addActionListener(this);
        fontColorItem.addActionListener(this);

        // 创建帮助菜单
        helpMenu = new JMenu("Help");
        aboutItem = new JMenuItem("About");
        helpMenu.add(aboutItem);
        aboutItem.addActionListener(this);

        // 将所有菜单添加到菜单栏
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(formatMenu);
        menuBar.add(helpMenu);

        // 设置菜单栏到窗口
        setJMenuBar(menuBar);

        // 设置窗口大小和关闭操作
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == newItem) {
            newFile();
        } else if (e.getSource() == openItem) {
            openFile();
        } else if (e.getSource() == saveItem) {
            saveFile();
        } else if (e.getSource() == printItem) {
            printFile();
        } else if (e.getSource() == exitItem) {
            System.exit(0);
        } else if (e.getSource() == searchItem) {
            searchWord();
        } else if (e.getSource() == boldItem) {
            setTextStyle(StyleConstants.Bold, true);
        } else if (e.getSource() == italicItem) {
            setTextStyle(StyleConstants.Italic, true);
        } else if (e.getSource() == underlineItem) {
            setTextStyle(StyleConstants.Underline, true);
        } else if (e.getSource() == fontColorItem) {
            setFontColor();
        } else if (e.getSource() == aboutItem) {
            showAboutDialog();
        }
    }

    private void setTextStyle(Object bold, boolean value) {
    }

    private void newFile() {
        textPane.setText("");
        fileName = null;
    }

    private void openFile() {
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            fileName = file.getName();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                textPane.read(reader, null);
            } catch (IOException ex) {
                showErrorDialog("Error opening file: " + ex.getMessage());
            }
        }
    }

    private void saveFile() {
        if (fileName == null) {
            int option = fileChooser.showSaveDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                fileName = file.getName();
                writeFile(file);
            }
        } else {
            File file = new File(fileChooser.getCurrentDirectory(), fileName);
            writeFile(file);
        }
    }

    private void writeFile(File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            textPane.write(writer);
        } catch (IOException ex) {
            showErrorDialog("Error saving file: " + ex.getMessage());
        }
    }

    private void printFile() {
        try {
            textPane.print();
        } catch (PrinterException ex) {
            showErrorDialog("Error printing file: " + ex.getMessage());
        }
    }

    private void searchWord() {
        String searchWord = JOptionPane.showInputDialog(this, "Enter word to search:");
        if (searchWord != null && !searchWord.isEmpty()) {
            String content = textPane.getText().toLowerCase();
            int index = content.indexOf(searchWord.toLowerCase());
            if (index != -1) {
                textPane.setSelectionStart(index);
                textPane.setSelectionEnd(index + searchWord.length());
                textPane.requestFocus();
            } else {
                showErrorDialog("Word not found!");
            }
        }
    }

    public enum TextStyle {
        BOLD, ITALIC, UNDERLINE;
    }

    private void setTextStyle(TextStyle style, boolean value) {
        SimpleAttributeSet attr = new SimpleAttributeSet();

        switch (style) {
            case BOLD:
                StyleConstants.setBold(attr, value);
                break;
            case ITALIC:
                StyleConstants.setItalic(attr, value);
                break;
            case UNDERLINE:
                StyleConstants.setUnderline(attr, value);
                break;
        }

        textPane.setCharacterAttributes(attr, false);
    }


    private void setFontColor() {
        Color color = JColorChooser.showDialog(this, "Choose Font Color", Color.BLACK);
        if (color != null) {
            SimpleAttributeSet attr = new SimpleAttributeSet();
            StyleConstants.setForeground(attr, color);
            textPane.setCharacterAttributes(attr, false);
        }
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this, "Advanced Text Editor\nCreated by: [Your Name]");
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TextEditor().setVisible(true));
    }
}
