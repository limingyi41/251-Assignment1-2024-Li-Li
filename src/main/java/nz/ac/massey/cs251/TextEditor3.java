package nz.ac.massey.cs251;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.*;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterException;
import java.io.*;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

public class TextEditor3 extends JFrame implements ActionListener {
    private JTextPane textPane;
    private JMenuBar menuBar;
    private JToolBar toolBar;
    private JMenu fileMenu, editMenu, formatMenu, helpMenu;
    private JMenuItem newItem, openItem, saveItem, saveAsItem, printItem, exitItem;
    private JMenuItem copyItem, cutItem, pasteItem, searchItem, undoItem, redoItem;
    private JMenuItem boldItem, italicItem, underlineItem, fontColorItem;
    private JMenuItem wordCountItem, aboutItem, pdfConvertItem;
    private JFileChooser fileChooser;
    private String currentFileName = null;
    private UndoManager undoManager;
    private Timer autoSaveTimer;

    public TextEditor3() {
        // 初始化窗口设置
        setTitle("Advanced Text Editor");
        textPane = new JTextPane();
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text Documents", "txt", "html", "md"));
        undoManager = new UndoManager();
        autoSaveTimer = new Timer();
        textPane.setContentType("text/plain");

        // 将文本面板添加到滚动面板中
        add(new JScrollPane(textPane), BorderLayout.CENTER);
        setupMenu();
        setupToolBar();
        setupShortcuts();

        // 设置自动保存功能，每分钟自动保存一次
        autoSaveTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                autoSave();
            }
        }, 60000, 60000);

        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        textPane.getDocument().addUndoableEditListener(e -> undoManager.addEdit(e.getEdit()));
    }

    private void setupMenu() {
        // 创建菜单栏和各个菜单
        menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        editMenu = new JMenu("Edit");
        formatMenu = new JMenu("Format");
        helpMenu = new JMenu("Help");

        // 文件菜单项
        newItem = createMenuItem("New", fileMenu);
        openItem = createMenuItem("Open", fileMenu);
        saveItem = createMenuItem("Save", fileMenu);
        saveAsItem = createMenuItem("Save As", fileMenu);
        printItem = createMenuItem("Print", fileMenu);
        exitItem = createMenuItem("Exit", fileMenu);

        // 编辑菜单项
        undoItem = createMenuItem("Undo", editMenu);
        redoItem = createMenuItem("Redo", editMenu);
        copyItem = createMenuItem(new DefaultEditorKit.CopyAction(), "Copy", editMenu);
        cutItem = createMenuItem(new DefaultEditorKit.CutAction(), "Cut", editMenu);
        pasteItem = createMenuItem(new DefaultEditorKit.PasteAction(), "Paste", editMenu);
        searchItem = createMenuItem("Search", editMenu);
        wordCountItem = createMenuItem("Word Count", editMenu);

        // 格式菜单项
        boldItem = createMenuItem("Bold", formatMenu);
        italicItem = createMenuItem("Italic", formatMenu);
        underlineItem = createMenuItem("Underline", formatMenu);
        fontColorItem = createMenuItem("Font Color", formatMenu);
        pdfConvertItem = createMenuItem("Convert to PDF", formatMenu);

        // 帮助菜单项
        aboutItem = createMenuItem("About", helpMenu);

        // 将菜单栏添加到窗口
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(formatMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
    }

    private void setupToolBar() {
        toolBar = new JToolBar();
        addButtonToToolBar("New", newItem);
        addButtonToToolBar("Open", openItem);
        addButtonToToolBar("Save", saveItem);
        addButtonToToolBar("Undo", undoItem);
        addButtonToToolBar("Redo", redoItem);
        addButtonToToolBar("Print", printItem);
        addButtonToToolBar("Bold", boldItem);
        addButtonToToolBar("Italic", italicItem);
        addButtonToToolBar("Underline", underlineItem);
        addButtonToToolBar("Font Color", fontColorItem);
        add(toolBar, BorderLayout.NORTH);
    }

    private void addButtonToToolBar(String name, JMenuItem menuItem) {
        JButton button = new JButton(name);
        button.addActionListener(menuItem.getActionListeners()[0]);
        toolBar.add(button);
    }

    private JMenuItem createMenuItem(String name, JMenu menu) {
        JMenuItem item = new JMenuItem(name);
        item.addActionListener(this);
        menu.add(item);
        return item;
    }

    private JMenuItem createMenuItem(Action action, String name, JMenu menu) {
        JMenuItem item = new JMenuItem(action);
        item.setText(name);
        menu.add(item);
        return item;
    }

    private void setupShortcuts() {
        newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK));
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK));
        copyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK));
        cutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK));
        pasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK));
        undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK));
        redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK));
        printItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK));
        searchItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, KeyEvent.CTRL_DOWN_MASK));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            Object source = e.getSource();
            if (source == newItem) {
                newFile();
            } else if (source == openItem) {
                openFile();
            } else if (source == saveItem) {
                saveFile(false);
            } else if (source == saveAsItem) {
                saveFile(true);
            } else if (source == printItem) {
                printFile();
            } else if (source == exitItem) {
                System.exit(0);
            } else if (source == searchItem) {
                searchWord();
            } else if (source == wordCountItem) {
                wordCount();
            } else if (source == boldItem) {
                setTextStyle((Integer) StyleConstants.Bold, true);
            } else if (source == italicItem) {
                setTextStyle((Integer) StyleConstants.Italic, true);
            } else if (source == underlineItem) {
                setTextStyle((Integer) StyleConstants.Underline, true);
            } else if (source == fontColorItem) {
                setFontColor();
            } else if (source == undoItem) {
                undoAction();
            } else if (source == redoItem) {
                redoAction();
            } else if (source == aboutItem) {
                showAboutDialog();
            } else if (source == pdfConvertItem) {
                convertToPDF();
            }
        } catch (Exception ex) {
            showErrorDialog("Error: " + ex.getMessage());
        }
    }

    private void setTextStyle(Integer bold, boolean value) {
    }

    private void newFile() {
        textPane.setText("");
        currentFileName = null;
    }

    private void openFile() throws IOException {
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            currentFileName = file.getName();
            FileReader reader = null;
            try {
                if (file.getName().endsWith(".html") || file.getName().endsWith(".htm")) {
                    // 读取HTML文件
                    textPane.setContentType("text/html");
                    textPane.read(new FileReader(file), null);
                } else if (file.getName().endsWith(".md")) {
                    // 读取Markdown文件
                    textPane.setContentType("text/plain");
                    reader = new FileReader(file);
                    textPane.read(reader, null);
                } else {
                    // 读取纯文本文件
                    textPane.setContentType("text/plain");
                    reader = new FileReader(file);
                    textPane.read(reader, null);
                }
            } catch (Exception ex) {
                showErrorDialog("Error opening file: " + ex.getMessage());
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        }
    }

    private void saveFile(boolean saveAs) throws IOException {
        if (currentFileName == null || saveAs) {
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                currentFileName = file.getName();
            }
        }
        if (currentFileName != null) {
            Writer writer = null;
            try {
                if (currentFileName.endsWith(".html") || currentFileName.endsWith(".htm")) {
                    // 保存为HTML格式
                    textPane.setContentType("text/html");
                    writer = new FileWriter(new File(fileChooser.getCurrentDirectory(), currentFileName));
                    textPane.write(writer);
                } else if (currentFileName.endsWith(".md")) {
                    // 保存为Markdown格式
                    textPane.setContentType("text/plain");
                    writer = new FileWriter(new File(fileChooser.getCurrentDirectory(), currentFileName));
                    textPane.write(writer);
                } else {
                    // 保存为纯文本格式
                    textPane.setContentType("text/plain");
                    writer = new FileWriter(new File(fileChooser.getCurrentDirectory(), currentFileName));
                    textPane.write(writer);
                }
            } catch (Exception ex) {
                showErrorDialog("Error saving file: " + ex.getMessage());
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
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
        String word = JOptionPane.showInputDialog(this, "Enter word to search:");
        if (word != null) {
            String content = textPane.getText().toLowerCase();
            int index = content.indexOf(word.toLowerCase());
            if (index != -1) {
                textPane.setSelectionStart(index);
                textPane.setSelectionEnd(index + word.length());
                textPane.requestFocus();
            } else {
                showErrorDialog("Word not found!");
            }
        }
    }

    private void wordCount() {
        String content = textPane.getText().trim();
        String[] words = content.split("\\s+");
        showInfoDialog("Word Count: " + words.length);
    }

    public enum TextStyle {
        BOLD, ITALIC, UNDERLINE;
    }

    private void setTextStyle(TextStyle style, boolean value) {
        SimpleAttributeSet attr = new SimpleAttributeSet();

        // 设置默认样式（全部关闭）
        StyleConstants.setBold(attr, false);
        StyleConstants.setItalic(attr, false);
        StyleConstants.setUnderline(attr, false);

        // 根据传入的枚举值设置具体的文本样式
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

        // 应用样式到 textPane
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

    private void undoAction() {
        try {
            if (undoManager.canUndo()) {
                undoManager.undo();
            }
        } catch (CannotUndoException e) {
            showErrorDialog("Cannot undo: " + e.getMessage());
        }
    }

    private void redoAction() {
        try {
            if (undoManager.canRedo()) {
                undoManager.redo();
            }
        } catch (CannotRedoException e) {
            showErrorDialog("Cannot redo: " + e.getMessage());
        }
    }

    private void convertToPDF() {
        PDDocument document = new PDDocument();
        try {
            PDPage page = new PDPage();
            document.addPage(page);
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // 设置字体和文本位置
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.beginText();
            contentStream.setLeading(14.5f);
            contentStream.newLineAtOffset(25, 725);

            // 分行处理文本
            String[] lines = textPane.getText().split("\n");
            for (String line : lines) {
                contentStream.showText(line);
                contentStream.newLine();
            }
            contentStream.endText();
            contentStream.close();

            // 保存PDF文件
            String pdfFileName = fileChooser.getSelectedFile().getName().replace(".txt", ".pdf");
            document.save(new File(fileChooser.getCurrentDirectory(), pdfFileName));
            showInfoDialog("PDF file saved successfully!");
        } catch (IOException ex) {
            showErrorDialog("Error converting to PDF: " + ex.getMessage());
        } finally {
            try {
                document.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void autoSave() {
        if (currentFileName != null) {
            try {
                saveFile(false);
                System.out.println("Auto saved at: " + System.currentTimeMillis());
            } catch (IOException ex) {
                System.err.println("Auto save failed: " + ex.getMessage());
            }
        }
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this, "Advanced Text Editor\nCreated by: [Your Name]");
    }

    private void showInfoDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdvancedTextEditor().setVisible(true));
    }
}
