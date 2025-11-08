package src.UI;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.regex.Pattern;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicScrollBarUI;

import src.auth.Account;
import src.auth.AuthManager;
import src.auth.PasswordHasher;
import src.auth.SessionContext;
import src.db.DBHelper;
import src.FinanceManager;

/**
 * Modern dialog for editing user profile information with PAN card, password change, and profile picture
 */
public class EditProfileDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
        Pattern.CASE_INSENSITIVE);
    private static final Pattern PAN_PATTERN = Pattern.compile(
        "^[A-Z]{5}[0-9]{4}[A-Z]{1}$");

    private JTextField nameField;
    private JComboBox<Account.AccountType> accountTypeField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField panCardField;
    private JLabel emailValidLabel;
    private JLabel panValidLabel;
    private JLabel profilePictureLabel;
    private JButton saveButton;
    private JButton changePasswordButton;
    private JButton uploadPictureButton;
    
    private String newProfilePicturePath = null;
    private boolean succeeded = false;
    private Account currentAccount;

    public EditProfileDialog(Frame owner) {
        super(owner, "Edit Profile", true);
        this.currentAccount = SessionContext.getCurrentAccount();
        buildUI();
        setResizable(false);
        setSize(600, 750);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(ModernTheme.BACKGROUND);
    }

    private void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
        verticalBar.setOpaque(false);
        verticalBar.setBackground(ModernTheme.BACKGROUND);
        verticalBar.setBorder(BorderFactory.createEmptyBorder());
        verticalBar.setPreferredSize(new Dimension(12, Integer.MAX_VALUE));
        verticalBar.setUnitIncrement(16);
        verticalBar.setUI(new ModernScrollBarUI());
    }

    private static class ModernScrollBarUI extends BasicScrollBarUI {
        private static final Color TRACK_COLOR = new Color(242, 243, 247);
        private static final Color THUMB_COLOR = new Color(110, 159, 219);
        private static final Color THUMB_HOVER = new Color(80, 129, 189);
        private boolean hover;

        @Override
        protected void configureScrollBarColors() {
            super.configureScrollBarColors();
            this.trackColor = TRACK_COLOR;
            this.thumbColor = THUMB_COLOR;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            JButton button = super.createDecreaseButton(orientation);
            button.setPreferredSize(new Dimension(0, 0));
            button.setOpaque(false);
            button.setVisible(false);
            return button;
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            JButton button = super.createIncreaseButton(orientation);
            button.setPreferredSize(new Dimension(0, 0));
            button.setOpaque(false);
            button.setVisible(false);
            return button;
        }

        @Override
        protected void installListeners() {
            super.installListeners();
            if (scrollbar != null) {
                scrollbar.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        hover = true;
                        scrollbar.repaint();
                    }

                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        hover = false;
                        scrollbar.repaint();
                    }
                });
            }
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(TRACK_COLOR);
            g2.fillRoundRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height, 12, 12);
            g2.dispose();
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (!scrollbar.isEnabled() || thumbBounds.width > thumbBounds.height) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color color = hover ? THUMB_HOVER : THUMB_COLOR;
            g2.setColor(color);
            int arc = 12;
            g2.fillRoundRect(thumbBounds.x + 2, thumbBounds.y + 2,
                thumbBounds.width - 4, thumbBounds.height - 4, arc, arc);
            g2.dispose();
        }

        @Override
        protected void paintDecreaseHighlight(Graphics g) {
            // No highlight
        }

        @Override
        protected void paintIncreaseHighlight(Graphics g) {
            // No highlight
        }
    }

    private static class ProfilePictureDialog extends JDialog {
        private static final long serialVersionUID = 1L;

    private final ImageCropperPanel cropperPanel;
    private final FileSelectionPanel fileSelectionPanel;
        private final JSlider sizeSlider;
        private final JComboBox<RatioPreset> ratioCombo;
        private final JPanel customRatioPanel;
        private final JSpinner customWidthSpinner;
        private final JSpinner customHeightSpinner;
        private final JButton applyCustomButton;
        private final JButton usePhotoButton;
        private boolean approved = false;
        private BufferedImage croppedImage;
        private double activeRatio = 1.0d;

        ProfilePictureDialog(Window owner) {
            super(owner, "Update Profile Picture", ModalityType.APPLICATION_MODAL);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setSize(900, 600);
            setLocationRelativeTo(owner);
            setLayout(new BorderLayout());
            getContentPane().setBackground(ModernTheme.BACKGROUND);

            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(true);
            header.setBackground(ModernTheme.SURFACE);
            header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ModernTheme.BORDER),
                BorderFactory.createEmptyBorder(18, 24, 18, 24)
            ));
            JPanel headerContent = new JPanel();
            headerContent.setOpaque(false);
            headerContent.setLayout(new BoxLayout(headerContent, BoxLayout.Y_AXIS));

            JLabel titleLabel = new JLabel("Upload Profile Picture");
            titleLabel.setFont(new Font(ModernTheme.FONT_BODY.getFamily(), Font.BOLD, 22));
            titleLabel.setForeground(ModernTheme.TEXT_PRIMARY);

            JLabel subtitleLabel = new JLabel("Choose an image, adjust the crop, and save your new avatar");
            subtitleLabel.setFont(new Font(ModernTheme.FONT_BODY.getFamily(), Font.PLAIN, 13));
            subtitleLabel.setForeground(ModernTheme.TEXT_SECONDARY);

            headerContent.add(titleLabel);
            headerContent.add(Box.createVerticalStrut(6));
            headerContent.add(subtitleLabel);
            header.add(headerContent, BorderLayout.CENTER);

            add(header, BorderLayout.NORTH);

            cropperPanel = new ImageCropperPanel();

            fileSelectionPanel = new FileSelectionPanel(this::loadSelectedImage);

            JPanel previewWrapper = new JPanel(new BorderLayout(0, 8));
            previewWrapper.setOpaque(false);
            previewWrapper.add(cropperPanel, BorderLayout.CENTER);

            JLabel previewHint = new JLabel("Drag to reposition. Adjust aspect ratio and frame size as needed.");
            previewHint.setFont(new Font(ModernTheme.FONT_BODY.getFamily(), Font.PLAIN, 12));
            previewHint.setForeground(ModernTheme.TEXT_SECONDARY);
            previewHint.setHorizontalAlignment(SwingConstants.CENTER);
            previewWrapper.add(previewHint, BorderLayout.SOUTH);

            JPanel centerPanel = new JPanel(new BorderLayout(20, 0));
            centerPanel.setOpaque(false);
            centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));
            centerPanel.add(fileSelectionPanel, BorderLayout.WEST);
            centerPanel.add(previewWrapper, BorderLayout.CENTER);

            add(centerPanel, BorderLayout.CENTER);

            JPanel footerPanel = new JPanel(new BorderLayout());
            footerPanel.setBackground(ModernTheme.SURFACE);
            footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ModernTheme.BORDER));

            JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 14));
            controlPanel.setOpaque(false);

            JLabel ratioLabel = new JLabel("Aspect Ratio:");
            ratioLabel.setFont(new Font(ModernTheme.FONT_BODY.getFamily(), Font.BOLD, 12));
            ratioLabel.setForeground(ModernTheme.TEXT_PRIMARY);
            controlPanel.add(ratioLabel);

            ratioCombo = new JComboBox<>(RatioPreset.values());
            ratioCombo.setSelectedItem(RatioPreset.SQUARE);
            ratioCombo.setPreferredSize(new Dimension(160, 32));
            ModernTheme.styleComboBox(ratioCombo);
            controlPanel.add(ratioCombo);

            customRatioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            customRatioPanel.setOpaque(false);
            customRatioPanel.add(new JLabel("W"));
            customWidthSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
            customWidthSpinner.setPreferredSize(new Dimension(60, 28));
            customRatioPanel.add(customWidthSpinner);
            customRatioPanel.add(new JLabel(":"));
            customHeightSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
            customHeightSpinner.setPreferredSize(new Dimension(60, 28));
            customRatioPanel.add(customHeightSpinner);
            applyCustomButton = ModernTheme.createSecondaryButton("Apply");
            applyCustomButton.setPreferredSize(new Dimension(80, 30));
            customRatioPanel.add(applyCustomButton);
            customRatioPanel.setVisible(false);
            controlPanel.add(customRatioPanel);

            JSeparator verticalSeparator = new JSeparator(SwingConstants.VERTICAL);
            verticalSeparator.setPreferredSize(new Dimension(1, 32));
            controlPanel.add(verticalSeparator);

            JLabel sizeLabel = new JLabel("Frame Size:");
            sizeLabel.setFont(new Font(ModernTheme.FONT_BODY.getFamily(), Font.BOLD, 12));
            sizeLabel.setForeground(ModernTheme.TEXT_PRIMARY);
            controlPanel.add(sizeLabel);

            sizeSlider = new JSlider(35, 100, 80);
            sizeSlider.setPreferredSize(new Dimension(220, 40));
            sizeSlider.setOpaque(false);
            sizeSlider.setEnabled(false);
            controlPanel.add(sizeSlider);

            footerPanel.add(controlPanel, BorderLayout.WEST);

            JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
            buttonsPanel.setOpaque(false);

            JButton cancelButton = ModernTheme.createSecondaryButton("Cancel");
            cancelButton.setPreferredSize(new Dimension(110, 36));
            cancelButton.addActionListener(e -> dispose());

            usePhotoButton = ModernTheme.createPrimaryButton("Use Photo");
            usePhotoButton.setPreferredSize(new Dimension(140, 36));
            usePhotoButton.setEnabled(false);
            usePhotoButton.addActionListener(e -> {
                if (!cropperPanel.hasImage()) {
                    JOptionPane.showMessageDialog(this,
                        "Please choose an image first.",
                        "No Image",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                BufferedImage result = cropperPanel.getCroppedImage();
                if (result == null) {
                    JOptionPane.showMessageDialog(this,
                        "Unable to crop the image. Please try a different selection.",
                        "Crop Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                croppedImage = result;
                approved = true;
                dispose();
            });

            buttonsPanel.add(cancelButton);
            buttonsPanel.add(usePhotoButton);

            footerPanel.add(buttonsPanel, BorderLayout.EAST);

            add(footerPanel, BorderLayout.SOUTH);

            ratioCombo.addActionListener(e -> handleRatioSelection());
            applyCustomButton.addActionListener(e -> applyCustomRatio());
            sizeSlider.addChangeListener(e -> {
                double factor = sizeSlider.getValue() / 100.0;
                cropperPanel.setSizeFactor(factor);
            });

            cropperPanel.setAspectRatio(activeRatio);
            handleRatioSelection();
        }

        private void handleRatioSelection() {
            RatioPreset preset = (RatioPreset) ratioCombo.getSelectedItem();
            if (preset == null) {
                return;
            }
            if (preset == RatioPreset.CUSTOM) {
                customRatioPanel.setVisible(true);
                customRatioPanel.revalidate();
                customRatioPanel.repaint();
            } else {
                customRatioPanel.setVisible(false);
                activeRatio = preset.getRatio();
                cropperPanel.setAspectRatio(activeRatio);
                cropperPanel.centerSelection();
            }
        }

        private void applyCustomRatio() {
            int widthValue = (Integer) customWidthSpinner.getValue();
            int heightValue = (Integer) customHeightSpinner.getValue();
            if (heightValue <= 0) {
                JOptionPane.showMessageDialog(this,
                    "Ratio height must be greater than zero.",
                    "Invalid Ratio",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            activeRatio = widthValue / (double) heightValue;
            cropperPanel.setAspectRatio(activeRatio);
            cropperPanel.centerSelection();
        }

        private void loadSelectedImage(File file) {
            if (file == null) {
                return;
            }
            fileSelectionPanel.showLoading(file);
            try {
                BufferedImage image = ImageIO.read(file);
                if (image == null) {
                    throw new IOException("Unsupported image format");
                }
                cropperPanel.setImage(image);
                cropperPanel.setAspectRatio(activeRatio);
                cropperPanel.centerSelection();
                sizeSlider.setEnabled(true);
                usePhotoButton.setEnabled(true);
                fileSelectionPanel.showSuccess(file);
            } catch (IOException ex) {
                fileSelectionPanel.showError("Unable to open image: " + ex.getMessage());
                sizeSlider.setEnabled(false);
                usePhotoButton.setEnabled(false);
                JOptionPane.showMessageDialog(this,
                    "Unable to open image: " + ex.getMessage(),
                    "Image Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }

        boolean isApproved() {
            return approved;
        }

        BufferedImage getCroppedImage() {
            return croppedImage;
        }

        private enum RatioPreset {
            SQUARE("Square 1:1", 1, 1),
            PORTRAIT_4_5("Portrait 4:5", 4, 5),
            PORTRAIT_3_4("Portrait 3:4", 3, 4),
            LANDSCAPE_3_2("Landscape 3:2", 3, 2),
            LANDSCAPE_16_9("Landscape 16:9", 16, 9),
            CUSTOM("Custom", -1, -1);

            private final String label;
            private final int widthUnits;
            private final int heightUnits;

            RatioPreset(String label, int widthUnits, int heightUnits) {
                this.label = label;
                this.widthUnits = widthUnits;
                this.heightUnits = heightUnits;
            }

            @Override
            public String toString() {
                return label;
            }

            double getRatio() {
                if (widthUnits <= 0 || heightUnits <= 0) {
                    return 1.0d;
                }
                return widthUnits / (double) heightUnits;
            }
        }

        private static class FileSelectionPanel extends JPanel {
            private static final long serialVersionUID = 1L;
            private final Consumer<File> fileConsumer;
            private final JLabel fileNameLabel;
            private final JLabel statusLabel;
            private final JButton browseButton;
            private File lastDirectory;

            FileSelectionPanel(Consumer<File> fileConsumer) {
                this.fileConsumer = fileConsumer;
                setOpaque(false);
                setPreferredSize(new Dimension(260, 0));
                setLayout(new BorderLayout());

                JPanel card = new JPanel();
                card.setBackground(ModernTheme.SURFACE);
                card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
                card.setBorder(BorderFactory.createEmptyBorder(28, 20, 28, 20));

                JLabel iconLabel = new JLabel(ModernIcons.create(ModernIcons.IconType.MAGIC, ModernTheme.PRIMARY, 56));
                iconLabel.setAlignmentX(CENTER_ALIGNMENT);
                card.add(iconLabel);

                card.add(Box.createVerticalStrut(12));

                JLabel titleLabel = new JLabel("Select a Photo");
                titleLabel.setAlignmentX(CENTER_ALIGNMENT);
                titleLabel.setFont(new Font(ModernTheme.FONT_BODY.getFamily(), Font.BOLD, 16));
                titleLabel.setForeground(ModernTheme.TEXT_PRIMARY);
                card.add(titleLabel);

                card.add(Box.createVerticalStrut(6));

                statusLabel = new JLabel("Supported formats: JPG, PNG, GIF, BMP");
                statusLabel.setAlignmentX(CENTER_ALIGNMENT);
                statusLabel.setFont(new Font(ModernTheme.FONT_SMALL.getFamily(), Font.PLAIN, 12));
                statusLabel.setForeground(ModernTheme.TEXT_SECONDARY);
                card.add(statusLabel);

                card.add(Box.createVerticalStrut(12));

                fileNameLabel = new JLabel("No file selected");
                fileNameLabel.setAlignmentX(CENTER_ALIGNMENT);
                fileNameLabel.setFont(new Font(ModernTheme.FONT_SMALL.getFamily(), Font.PLAIN, 12));
                fileNameLabel.setForeground(ModernTheme.TEXT_SECONDARY);
                card.add(fileNameLabel);

                card.add(Box.createVerticalStrut(18));

                browseButton = ModernTheme.createPrimaryButton("Browse Files");
                browseButton.setAlignmentX(CENTER_ALIGNMENT);
                browseButton.setPreferredSize(new Dimension(160, 36));
                browseButton.addActionListener(e -> openFileChooser());
                card.add(browseButton);

                card.add(Box.createVerticalGlue());

                add(card, BorderLayout.CENTER);
            }

            private void openFileChooser() {
                JFileChooser chooser = (lastDirectory != null)
                    ? new JFileChooser(lastDirectory)
                    : new JFileChooser();
                chooser.setDialogTitle("Choose Profile Photo");
                chooser.setAcceptAllFileFilterUsed(false);
                chooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png", "gif", "bmp"));
                int result = chooser.showOpenDialog(SwingUtilities.getWindowAncestor(this));
                if (result == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    lastDirectory = chooser.getCurrentDirectory();
                    showLoading(file);
                    fileConsumer.accept(file);
                }
            }

            void showLoading(File file) {
                if (file != null) {
                    fileNameLabel.setText(file.getName());
                }
                statusLabel.setText("Loading preview...");
                statusLabel.setForeground(ModernTheme.TEXT_SECONDARY);
            }

            void showSuccess(File file) {
                if (file != null) {
                    fileNameLabel.setText(file.getName());
                }
                statusLabel.setText("Preview ready. Adjust the crop on the right.");
                statusLabel.setForeground(ModernTheme.SUCCESS);
            }

            void showError(String message) {
                statusLabel.setText(message);
                statusLabel.setForeground(ModernTheme.DANGER);
            }
        }
    }

    private static class ImageCropperPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        private static final double MIN_FACTOR = 0.35d;
        private static final double MAX_FACTOR = 1.0d;
        private static final int PADDING = 32;

        private BufferedImage image;
        private Rectangle2D.Double selection;
        private double aspectRatio = 1.0d;
        private double sizeFactor = 0.8d;
        private Point dragOrigin;

        ImageCropperPanel() {
            setPreferredSize(new Dimension(480, 480));
            setBackground(ModernTheme.SURFACE);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ModernTheme.BORDER),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

            MouseAdapter adapter = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (!hasImage()) {
                        return;
                    }
                    if (isInsideSelection(e.getPoint())) {
                        dragOrigin = e.getPoint();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    dragOrigin = null;
                }
            };

            MouseMotionAdapter motionAdapter = new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (!hasImage() || dragOrigin == null) {
                        return;
                    }
                    translateSelection(dragOrigin, e.getPoint());
                    dragOrigin = e.getPoint();
                }
            };

            addMouseListener(adapter);
            addMouseMotionListener(motionAdapter);
        }

        void setImage(BufferedImage image) {
            this.image = image;
            this.selection = null;
            applyAspectRatio(true);
            repaint();
        }

        void setAspectRatio(double ratio) {
            if (ratio <= 0) {
                ratio = 1.0d;
            }
            this.aspectRatio = ratio;
            applyAspectRatio(false);
            repaint();
        }

        void setSizeFactor(double factor) {
            if (!hasImage()) {
                return;
            }
            this.sizeFactor = clamp(factor, MIN_FACTOR, MAX_FACTOR);
            applyAspectRatio(false);
            repaint();
        }

        void centerSelection() {
            if (!hasImage() || selection == null) {
                return;
            }
            double iw = image.getWidth();
            double ih = image.getHeight();
            selection.x = (iw - selection.width) / 2.0;
            selection.y = (ih - selection.height) / 2.0;
            clampSelection();
            repaint();
        }

        boolean hasImage() {
            return image != null;
        }

        BufferedImage getCroppedImage() {
            if (!hasImage() || selection == null) {
                return null;
            }
            Rectangle cropRect = toIntegerRectangle(selection);
            if (cropRect.width <= 0 || cropRect.height <= 0) {
                return null;
            }
            BufferedImage sub = image.getSubimage(cropRect.x, cropRect.y, cropRect.width, cropRect.height);
            BufferedImage copy = new BufferedImage(cropRect.width, cropRect.height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = copy.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(sub, 0, 0, null);
            g2.dispose();
            return copy;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            if (!hasImage()) {
                g2.setColor(ModernTheme.TEXT_SECONDARY);
                g2.setFont(new Font(ModernTheme.FONT_BODY.getFamily(), Font.PLAIN, 14));
                String message = "Select an image to begin";
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(message)) / 2;
                int y = (getHeight() + fm.getAscent()) / 2;
                g2.drawString(message, x, y);
                g2.dispose();
                return;
            }

            double scale = computeScale();
            double offsetX = computeOffsetX(scale);
            double offsetY = computeOffsetY(scale);

            int drawX = (int) Math.round(offsetX);
            int drawY = (int) Math.round(offsetY);
            int drawW = (int) Math.round(image.getWidth() * scale);
            int drawH = (int) Math.round(image.getHeight() * scale);

            g2.drawImage(image, drawX, drawY, drawW, drawH, null);

            if (selection != null) {
                Rectangle2D selectionView = new Rectangle2D.Double(
                    offsetX + selection.x * scale,
                    offsetY + selection.y * scale,
                    selection.width * scale,
                    selection.height * scale);

                Area shade = new Area(new Rectangle2D.Double(drawX, drawY, drawW, drawH));
                shade.subtract(new Area(selectionView));
                g2.setColor(new Color(0, 0, 0, 110));
                g2.fill(shade);

                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(selectionView);

                g2.setColor(ModernTheme.PRIMARY);
                g2.setStroke(new BasicStroke(2.0f));
                g2.draw(selectionView);

                // Guide lines (rule of thirds)
                g2.setColor(new Color(255, 255, 255, 90));
                double thirdWidth = selectionView.getWidth() / 3.0;
                double thirdHeight = selectionView.getHeight() / 3.0;
                g2.draw(new Line2D.Double(selectionView.getX() + thirdWidth, selectionView.getY(),
                    selectionView.getX() + thirdWidth, selectionView.getY() + selectionView.getHeight()));
                g2.draw(new Line2D.Double(selectionView.getX() + 2 * thirdWidth, selectionView.getY(),
                    selectionView.getX() + 2 * thirdWidth, selectionView.getY() + selectionView.getHeight()));
                g2.draw(new Line2D.Double(selectionView.getX(), selectionView.getY() + thirdHeight,
                    selectionView.getX() + selectionView.getWidth(), selectionView.getY() + thirdHeight));
                g2.draw(new Line2D.Double(selectionView.getX(), selectionView.getY() + 2 * thirdHeight,
                    selectionView.getX() + selectionView.getWidth(), selectionView.getY() + 2 * thirdHeight));
            }

            g2.dispose();
        }

        private Rectangle toIntegerRectangle(Rectangle2D rect) {
            int x = (int) Math.max(0, Math.round(rect.getX()));
            int y = (int) Math.max(0, Math.round(rect.getY()));
            int w = (int) Math.min(image.getWidth() - x, Math.round(rect.getWidth()))
                ;
            int h = (int) Math.min(image.getHeight() - y, Math.round(rect.getHeight()));
            return new Rectangle(x, y, Math.max(1, w), Math.max(1, h));
        }

        private boolean isInsideSelection(Point viewPoint) {
            if (selection == null || !hasImage()) {
                return false;
            }
            Point2D.Double imagePoint = viewToImage(viewPoint);
            return imagePoint.x >= selection.x && imagePoint.x <= selection.x + selection.width
                && imagePoint.y >= selection.y && imagePoint.y <= selection.y + selection.height;
        }

        private void translateSelection(Point from, Point to) {
            Point2D.Double start = viewToImage(from);
            Point2D.Double end = viewToImage(to);
            double dx = end.x - start.x;
            double dy = end.y - start.y;
            if (!Double.isFinite(dx) || !Double.isFinite(dy) || selection == null) {
                return;
            }
            selection.x += dx;
            selection.y += dy;
            clampSelection();
            repaint();
        }

        private void clampSelection() {
            if (selection == null || !hasImage()) {
                return;
            }
            double iw = image.getWidth();
            double ih = image.getHeight();
            selection.x = clamp(selection.x, 0, Math.max(0, iw - selection.width));
            selection.y = clamp(selection.y, 0, Math.max(0, ih - selection.height));
        }

        private void applyAspectRatio(boolean forceCenter) {
            if (!hasImage()) {
                return;
            }
            double iw = image.getWidth();
            double ih = image.getHeight();
            double base = Math.min(iw, ih) * clamp(sizeFactor, MIN_FACTOR, MAX_FACTOR);

            double width = base;
            double height = base / aspectRatio;

            if (height > ih) {
                height = ih;
                width = height * aspectRatio;
            }
            if (width > iw) {
                width = iw;
                height = width / aspectRatio;
            }

            width = Math.max(32, width);
            height = Math.max(32, height);

            if (selection == null || forceCenter) {
                selection = new Rectangle2D.Double(
                    (iw - width) / 2.0,
                    (ih - height) / 2.0,
                    width,
                    height);
            } else {
                double centerX = selection.getCenterX();
                double centerY = selection.getCenterY();
                selection.width = width;
                selection.height = height;
                selection.x = centerX - width / 2.0;
                selection.y = centerY - height / 2.0;
            }
            clampSelection();
        }

        private double computeScale() {
            if (!hasImage()) {
                return 1.0d;
            }
            double availableWidth = Math.max(1, getWidth() - PADDING * 2.0);
            double availableHeight = Math.max(1, getHeight() - PADDING * 2.0);
            return Math.min(availableWidth / image.getWidth(), availableHeight / image.getHeight());
        }

        private double computeOffsetX(double scale) {
            return (getWidth() - image.getWidth() * scale) / 2.0;
        }

        private double computeOffsetY(double scale) {
            return (getHeight() - image.getHeight() * scale) / 2.0;
        }

        private Point2D.Double viewToImage(Point p) {
            if (!hasImage()) {
                return new Point2D.Double(0, 0);
            }
            double scale = computeScale();
            double offsetX = computeOffsetX(scale);
            double offsetY = computeOffsetY(scale);
            double imgX = (p.x - offsetX) / scale;
            double imgY = (p.y - offsetY) / scale;
            return new Point2D.Double(imgX, imgY);
        }

        private double clamp(double value, double min, double max) {
            return Math.max(min, Math.min(max, value));
        }
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    private void buildUI() {
        setLayout(new BorderLayout(0, 0));
        
        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(true);
        headerPanel.setBackground(ModernTheme.SURFACE);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, ModernTheme.BORDER),
            BorderFactory.createEmptyBorder(24, 32, 24, 32)
        ));
        
        JLabel titleLabel = new JLabel("Edit Profile");
        titleLabel.setFont(new Font(ModernTheme.FONT_BODY.getFamily(), Font.BOLD, 24));
        titleLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Update your account information");
        subtitleLabel.setFont(new Font(ModernTheme.FONT_BODY.getFamily(), Font.PLAIN, 13));
        subtitleLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(LEFT_ALIGNMENT);
        
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(8));
        headerPanel.add(subtitleLabel);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Content Panel with Scroll
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(ModernTheme.BACKGROUND);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(16, 32, 24, 32));
        
        // Profile Picture Section
        JPanel pictureCard = createModernCard();
        pictureCard.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
        
        profilePictureLabel = new JLabel();
        profilePictureLabel.setPreferredSize(new Dimension(120, 120));
        profilePictureLabel.setHorizontalAlignment(SwingConstants.CENTER);
        profilePictureLabel.setVerticalAlignment(SwingConstants.CENTER);
        loadProfilePicture();
        
        JPanel picturePanel = new JPanel(new BorderLayout(10, 10));
        picturePanel.setOpaque(false);
        picturePanel.add(profilePictureLabel, BorderLayout.CENTER);
        
        uploadPictureButton = ModernTheme.createSecondaryButton("Upload Picture");
        uploadPictureButton.setPreferredSize(new Dimension(140, 36));
        uploadPictureButton.addActionListener(e -> uploadProfilePicture());
        picturePanel.add(uploadPictureButton, BorderLayout.SOUTH);
        
        pictureCard.add(picturePanel);
        contentPanel.add(pictureCard);
        contentPanel.add(Box.createVerticalStrut(16));
        
    // Profile Information Card with PAN Card
        JPanel profileCard = createModernCard();
        profileCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Account Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        JLabel nameLabel = new JLabel("Account Name");
        nameLabel.setFont(new Font(ModernTheme.FONT_BODY.getFamily(), Font.BOLD, 13));
        nameLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        profileCard.add(nameLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        nameField = new JTextField(currentAccount.getAccountName());
        nameField.setPreferredSize(new Dimension(300, 38));
        ModernTheme.styleTextField(nameField);
        profileCard.add(nameField, gbc);
        
        // Account Type
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        JLabel typeLabel = new JLabel("Account Type");
        typeLabel.setFont(new Font(ModernTheme.FONT_BODY.getFamily(), Font.BOLD, 13));
        typeLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        profileCard.add(typeLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        accountTypeField = new JComboBox<>(Account.AccountType.values());
        accountTypeField.setSelectedItem(currentAccount.getAccountType());
        accountTypeField.setPreferredSize(new Dimension(300, 38));
        ModernTheme.styleComboBox(accountTypeField);
        profileCard.add(accountTypeField, gbc);
        
        // Email
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        JLabel emailLabel = new JLabel("Email");
        emailLabel.setFont(new Font(ModernTheme.FONT_BODY.getFamily(), Font.BOLD, 13));
        emailLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        profileCard.add(emailLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JPanel emailPanel = new JPanel();
        emailPanel.setLayout(new BoxLayout(emailPanel, BoxLayout.Y_AXIS));
        emailPanel.setOpaque(false);
        
        emailField = new JTextField(currentAccount.getEmail() != null ? currentAccount.getEmail() : "");
        emailField.setPreferredSize(new Dimension(300, 38));
        emailField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        ModernTheme.styleTextField(emailField);
        
        emailValidLabel = new JLabel();
        emailValidLabel.setFont(new Font(ModernTheme.FONT_BODY.getFamily(), Font.PLAIN, 11));
        
        emailField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { validateEmail(); }
            @Override
            public void removeUpdate(DocumentEvent e) { validateEmail(); }
            @Override
            public void changedUpdate(DocumentEvent e) { validateEmail(); }
        });
        
        emailPanel.add(emailField);
        emailPanel.add(Box.createVerticalStrut(4));
        emailPanel.add(emailValidLabel);
        
        profileCard.add(emailPanel, gbc);
        
        // Phone
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        JLabel phoneLabel = new JLabel("Phone");
        phoneLabel.setFont(new Font(ModernTheme.FONT_BODY.getFamily(), Font.BOLD, 13));
        phoneLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        profileCard.add(phoneLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        phoneField = new JTextField(currentAccount.getPhone() != null ? currentAccount.getPhone() : "");
        phoneField.setPreferredSize(new Dimension(300, 38));
        ModernTheme.styleTextField(phoneField);
        profileCard.add(phoneField, gbc);
        
        // PAN Card
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.0;
        JLabel panLabel = new JLabel("PAN Card");
        panLabel.setFont(new Font(ModernTheme.FONT_BODY.getFamily(), Font.BOLD, 13));
        panLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        profileCard.add(panLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JPanel panPanel = new JPanel();
        panPanel.setLayout(new BoxLayout(panPanel, BoxLayout.Y_AXIS));
        panPanel.setOpaque(false);
        
        panCardField = new JTextField(currentAccount.getPanCard() != null ? currentAccount.getPanCard() : "");
        panCardField.setPreferredSize(new Dimension(300, 38));
        panCardField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        ModernTheme.styleTextField(panCardField);
        
        panValidLabel = new JLabel();
        panValidLabel.setFont(new Font(ModernTheme.FONT_BODY.getFamily(), Font.PLAIN, 11));
        
        panCardField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { validatePAN(); }
            @Override
            public void removeUpdate(DocumentEvent e) { validatePAN(); }
            @Override
            public void changedUpdate(DocumentEvent e) { validatePAN(); }
        });
        
        panPanel.add(panCardField);
        panPanel.add(Box.createVerticalStrut(4));
        panPanel.add(panValidLabel);
        
        profileCard.add(panPanel, gbc);
        
        contentPanel.add(profileCard);
        contentPanel.add(Box.createVerticalStrut(16));
        
        // Password Change Section
        JPanel passwordCard = createModernCard();
        passwordCard.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 20));
        
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font(ModernTheme.FONT_BODY.getFamily(), Font.BOLD, 13));
        passwordLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        
        changePasswordButton = ModernTheme.createSecondaryButton("Change Password");
        changePasswordButton.setPreferredSize(new Dimension(160, 38));
        changePasswordButton.addActionListener(e -> changePassword());
        
        passwordCard.add(passwordLabel);
        passwordCard.add(changePasswordButton);
        
    contentPanel.add(passwordCard);
    contentPanel.add(Box.createVerticalStrut(16));
    contentPanel.add(Box.createVerticalGlue());

    JScrollPane scrollPane = new JScrollPane(contentPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setBorder(null);
    scrollPane.setOpaque(false);
    scrollPane.getViewport().setOpaque(false);
    scrollPane.getViewport().setBackground(ModernTheme.BACKGROUND);
    styleScrollPane(scrollPane);

    add(scrollPane, BorderLayout.CENTER);
        
        // Footer Panel
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(ModernTheme.SURFACE);
        footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, ModernTheme.BORDER));
        
        // Left side - Delete Account button
        JPanel leftFooterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 16));
        leftFooterPanel.setBackground(ModernTheme.SURFACE);
        
        JButton deleteAccountButton = ModernTheme.createDangerButton("Delete Account");
        deleteAccountButton.setPreferredSize(new Dimension(140, 38));
        deleteAccountButton.setIcon(ModernIcons.create(ModernIcons.IconType.DELETE, ModernTheme.TEXT_WHITE, 16));
        deleteAccountButton.addActionListener(e -> confirmDeleteAccount());
        leftFooterPanel.add(deleteAccountButton);
        
        // Right side - Cancel and Save buttons
        JPanel rightFooterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 16));
        rightFooterPanel.setBackground(ModernTheme.SURFACE);
        
        JButton cancelButton = ModernTheme.createSecondaryButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(100, 38));
        cancelButton.addActionListener(e -> dispose());
        
        saveButton = ModernTheme.createPrimaryButton("Save Changes");
        saveButton.setPreferredSize(new Dimension(140, 38));
        saveButton.addActionListener(e -> saveProfile());
        
        rightFooterPanel.add(cancelButton);
        rightFooterPanel.add(saveButton);
        
        footerPanel.add(leftFooterPanel, BorderLayout.WEST);
        footerPanel.add(rightFooterPanel, BorderLayout.EAST);
        
        add(footerPanel, BorderLayout.SOUTH);
        
        // Initial validation
        validateEmail();
        validatePAN();
    }
    
    private JPanel createModernCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBackground(ModernTheme.SURFACE);
        card.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        return card;
    }
    
    private void loadProfilePicture() {
        String picturePath = currentAccount.getProfilePicturePath();
        if (picturePath != null && !picturePath.isEmpty() && new File(picturePath).exists()) {
            try {
                BufferedImage img = ImageIO.read(new File(picturePath));
                if (img != null) {
                    setProfilePictureImage(img);
                    return;
                }
            } catch (IOException e) {
                setDefaultProfilePicture();
                return;
            }
        }
        setDefaultProfilePicture();
    }

    private void setProfilePictureImage(BufferedImage image) {
        if (image == null) {
            setDefaultProfilePicture();
            return;
        }
        Image scaled = image.getScaledInstance(120, 120, Image.SCALE_SMOOTH);
        profilePictureLabel.setIcon(new ImageIcon(scaled));
        profilePictureLabel.setText(null);
        profilePictureLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        profilePictureLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
        profilePictureLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    }
    
    private void setDefaultProfilePicture() {
        profilePictureLabel.setIcon(ModernIcons.create(ModernIcons.IconType.USER, ModernTheme.TEXT_SECONDARY, 80));
        profilePictureLabel.setText("No Photo");
        profilePictureLabel.setFont(new Font(ModernTheme.FONT_BODY.getFamily(), Font.PLAIN, 11));
        profilePictureLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        profilePictureLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
        profilePictureLabel.setHorizontalTextPosition(SwingConstants.CENTER);
    }
    
    private void uploadProfilePicture() {
        ProfilePictureDialog dialog = new ProfilePictureDialog(SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        
        if (!dialog.isApproved()) {
            return;
        }
        BufferedImage croppedImage = dialog.getCroppedImage();
        if (croppedImage == null) {
            return;
        }
        
        try {
            File profilePicDir = new File("profile_pictures");
            if (!profilePicDir.exists()) {
                profilePicDir.mkdirs();
            }
            String fileName = "user_" + currentAccount.getId() + "_" + System.currentTimeMillis() + ".png";
            File destFile = new File(profilePicDir, fileName);
            ImageIO.write(croppedImage, "png", destFile);
            newProfilePicturePath = destFile.getAbsolutePath();
            currentAccount.setProfilePicturePath(newProfilePicturePath);
            setProfilePictureImage(croppedImage);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                "Unable to save profile picture: " + ex.getMessage(),
                "Image Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void validateEmail() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            emailValidLabel.setText("");
            return;
        }
        
        if (EMAIL_PATTERN.matcher(email).matches()) {
            emailValidLabel.setText("✓ Valid email");
            emailValidLabel.setForeground(ModernTheme.SUCCESS);
        } else {
            emailValidLabel.setText("✗ Invalid email format");
            emailValidLabel.setForeground(ModernTheme.DANGER);
        }
    }
    
    private void validatePAN() {
    String pan = panCardField.getText().trim();
        if (pan.isEmpty()) {
            panValidLabel.setText("");
            return;
        }
        
        if (PAN_PATTERN.matcher(pan).matches()) {
            panValidLabel.setText("✓ Valid PAN format");
            panValidLabel.setForeground(ModernTheme.SUCCESS);
        } else {
            panValidLabel.setText("✗ Invalid PAN (Format: ABCDE1234F)");
            panValidLabel.setForeground(ModernTheme.DANGER);
        }
    }
    
    private void changePassword() {
        // Create password change dialog
        JDialog passwordDialog = new JDialog(this, "Change Password", true);
        passwordDialog.setLayout(new BorderLayout(10, 10));
        passwordDialog.setSize(450, 350);
        passwordDialog.setLocationRelativeTo(this);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Current Password
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Current Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField currentPasswordField = new JPasswordField(20);
        ModernTheme.styleTextField(currentPasswordField);
        panel.add(currentPasswordField, gbc);
        
        // New Password
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField newPasswordField = new JPasswordField(20);
        ModernTheme.styleTextField(newPasswordField);
        panel.add(newPasswordField, gbc);
        
        // Confirm Password
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        JPasswordField confirmPasswordField = new JPasswordField(20);
        ModernTheme.styleTextField(confirmPasswordField);
        panel.add(confirmPasswordField, gbc);
        
        passwordDialog.add(panel, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton cancelBtn = ModernTheme.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> passwordDialog.dispose());
        
        JButton changeBtn = ModernTheme.createPrimaryButton("Change Password");
        changeBtn.addActionListener(e -> {
            String currentPassword = new String(currentPasswordField.getPassword());
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            
            if (currentPassword.isEmpty() || newPassword.isEmpty()) {
                JOptionPane.showMessageDialog(passwordDialog, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(passwordDialog, "New passwords do not match!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (newPassword.length() < 6) {
                JOptionPane.showMessageDialog(passwordDialog, "Password must be at least 6 characters!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                // Verify current password
                String hashedCurrent = PasswordHasher.hashPassword(currentPassword.toCharArray(), currentAccount.getPasswordSalt());
                if (!hashedCurrent.equals(currentAccount.getPasswordHash())) {
                    JOptionPane.showMessageDialog(passwordDialog, "Current password is incorrect!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Generate new hash
                String newSalt = PasswordHasher.generateSalt();
                String newHash = PasswordHasher.hashPassword(newPassword.toCharArray(), newSalt);
                
                // Update in database
                DBHelper dbHelper = new DBHelper();
                Connection conn = dbHelper.getConnection();
                String sql = "UPDATE accounts SET password_hash = ?, password_salt = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, newHash);
                    pstmt.setString(2, newSalt);
                    pstmt.setInt(3, currentAccount.getId());
                    
                    int rowsAffected = pstmt.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        currentAccount.setPasswordHash(newHash);
                        currentAccount.setPasswordSalt(newSalt);
                        
                        JOptionPane.showMessageDialog(passwordDialog, 
                            "Password changed successfully!", 
                            "Success", 
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        passwordDialog.dispose();
                    } else {
                        JOptionPane.showMessageDialog(passwordDialog, 
                            "Failed to change password.", 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(passwordDialog, 
                    "Error changing password: " + ex.getMessage(), 
                    "Database Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        buttonPanel.add(cancelBtn);
        buttonPanel.add(changeBtn);
        passwordDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        passwordDialog.setVisible(true);
    }
    
    private void saveProfile() {
        // Validate inputs
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
    String pan = panCardField.getText().trim();
        
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Account name is required", 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            nameField.requestFocus();
            return;
        }
        
        if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a valid email address", 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            emailField.requestFocus();
            return;
        }
        
        if (!pan.isEmpty() && !PAN_PATTERN.matcher(pan).matches()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a valid PAN card number (Format: ABCDE1234F)", 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            panCardField.requestFocus();
            return;
        }
        
        // Update account in database
        try {
            DBHelper dbHelper = new DBHelper();
            Connection conn = dbHelper.getConnection();
            
            String profilePicPath = newProfilePicturePath != null ? newProfilePicturePath : currentAccount.getProfilePicturePath();
            
            String sql = "UPDATE accounts SET account_name = ?, account_type = ?, email = ?, phone = ?, pan_card = ?, profile_picture_path = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setString(2, ((Account.AccountType) accountTypeField.getSelectedItem()).name());
                pstmt.setString(3, email.isEmpty() ? null : email);
                pstmt.setString(4, phone.isEmpty() ? null : phone);
                pstmt.setString(5, pan.isEmpty() ? null : pan);
                pstmt.setString(6, profilePicPath);
                pstmt.setInt(7, currentAccount.getId());
                
                int rowsAffected = pstmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    // Update session context
                    currentAccount.setAccountName(name);
                    currentAccount.setAccountType((Account.AccountType) accountTypeField.getSelectedItem());
                    currentAccount.setEmail(email.isEmpty() ? null : email);
                    currentAccount.setPhone(phone.isEmpty() ? null : phone);
                    currentAccount.setPanCard(pan.isEmpty() ? null : pan);
                    currentAccount.setProfilePicturePath(profilePicPath);
                    
                    SessionContext.setCurrentAccount(currentAccount);
                    
                    succeeded = true;
                    
                    JOptionPane.showMessageDialog(this, 
                        "Profile updated successfully!", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                    
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Failed to update profile. Please try again.", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error updating profile: " + ex.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Confirms and executes account deletion with password verification
     */
    private void confirmDeleteAccount() {
        // First confirmation
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "⚠️ WARNING: This will permanently delete your account and ALL associated data!\n\n" +
            "This includes:\n" +
            "• All transactions\n" +
            "• Bank accounts\n" +
            "• Deposits and investments\n" +
            "• Loans and lendings\n" +
            "• Credit cards\n" +
            "• Tax profiles\n\n" +
            "This action CANNOT be undone!\n\n" +
            "Are you sure you want to continue?",
            "Confirm Account Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Password verification dialog
        JDialog passwordDialog = new JDialog(this, "Verify Password", true);
        passwordDialog.setSize(450, 250);
        passwordDialog.setLocationRelativeTo(this);
        passwordDialog.setUndecorated(true);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(ModernTheme.SURFACE);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            new ModernTheme.RoundedBorder(16, ModernTheme.BORDER),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Verify Your Password");
        titleLabel.setFont(ModernTheme.FONT_HEADER.deriveFont(Font.BOLD, 18f));
        titleLabel.setForeground(ModernTheme.TEXT_PRIMARY);
        
        JLabel warningLabel = new JLabel("⚠️");
        warningLabel.setFont(new Font("Arial", Font.PLAIN, 32));
        
        headerPanel.add(warningLabel, BorderLayout.WEST);
        headerPanel.add(Box.createHorizontalStrut(12), BorderLayout.CENTER);
        headerPanel.add(titleLabel, BorderLayout.EAST);
        
        // Content
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel instructionLabel = new JLabel("<html><center>Enter your password to confirm<br>account deletion</center></html>");
        instructionLabel.setFont(ModernTheme.FONT_BODY);
        instructionLabel.setForeground(ModernTheme.TEXT_SECONDARY);
        instructionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPasswordField passwordField = new JPasswordField(20);
        ModernTheme.styleTextField(passwordField);
        passwordField.setPreferredSize(new Dimension(300, 38));
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        contentPanel.add(instructionLabel, gbc);
        
        gbc.gridy = 1;
        contentPanel.add(passwordField, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        
        JButton cancelBtn = ModernTheme.createSecondaryButton("Cancel");
        cancelBtn.setPreferredSize(new Dimension(100, 38));
        cancelBtn.addActionListener(e -> passwordDialog.dispose());
        
        JButton deleteBtn = ModernTheme.createDangerButton("Delete Account");
        deleteBtn.setPreferredSize(new Dimension(150, 38));
        deleteBtn.addActionListener(e -> {
            String password = new String(passwordField.getPassword());
            
            if (password.isEmpty()) {
                JOptionPane.showMessageDialog(passwordDialog,
                    "Please enter your password",
                    "Password Required",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Verify password
            try {
                char[] passwordChars = password.toCharArray();
                if (!PasswordHasher.verifyPassword(passwordChars, 
                        currentAccount.getPasswordSalt(), 
                        currentAccount.getPasswordHash())) {
                    JOptionPane.showMessageDialog(passwordDialog,
                        "Incorrect password. Account deletion cancelled.",
                        "Authentication Failed",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Password verified - proceed with deletion
                passwordDialog.dispose();
                
                // Final confirmation
                int finalConfirm = JOptionPane.showConfirmDialog(
                    this,
                    "This is your LAST CHANCE to cancel!\n\n" +
                    "Delete account for: " + currentAccount.getAccountName() + "?",
                    "Final Confirmation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE
                );
                
                if (finalConfirm == JOptionPane.YES_OPTION) {
                    performAccountDeletion();
                }
                
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(passwordDialog,
                    "Error verifying password: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // Enter key to submit
        passwordField.addActionListener(e -> deleteBtn.doClick());
        
        buttonPanel.add(cancelBtn);
        buttonPanel.add(deleteBtn);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        passwordDialog.add(mainPanel);
        passwordDialog.setVisible(true);
    }
    
    /**
     * Performs the actual account deletion
     */
    private void performAccountDeletion() {
        try {
            int accountId = currentAccount.getId();
            String accountName = currentAccount.getAccountName();
            
            // Create FinanceManager instance to call deleteAccount
            FinanceManager manager = new FinanceManager();
            manager.deleteAccount(accountId);
            
            // Clear session
            SessionContext.clear();
            
            // Show success message
            JOptionPane.showMessageDialog(this,
                "Account '" + accountName + "' has been permanently deleted.\n" +
                "The application will now close.",
                "Account Deleted",
                JOptionPane.INFORMATION_MESSAGE);
            
            // Close this dialog
            dispose();
            
            // Close the parent window and exit to login
            Window[] windows = Window.getWindows();
            for (Window window : windows) {
                if (window.isVisible() && window != this) {
                    window.dispose();
                }
            }
            
            // Relaunch login screen
            SwingUtilities.invokeLater(() -> {
                try {
                    DBHelper dbHelper = new DBHelper();
                    AuthManager authManager = new AuthManager(dbHelper.getConnection());
                    new LoginDialog(null, authManager).setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(0);
                }
            });
            
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error deleting account: " + ex.getMessage(),
                "Deletion Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}