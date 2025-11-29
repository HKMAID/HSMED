package hsmed;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;

public class Connexion extends JFrame {
    private final DatabaseManager db;
    
    public Connexion(DatabaseManager db) {
        this.db = db;
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Connexion - HSMED");
        setSize(680, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(245, 247, 250));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        JPanel headerPanel = createHeaderPanel("HSMED", "Sélectionnez votre profil");
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        JPanel buttonsContainer = new JPanel(new BorderLayout());
        buttonsContainer.setBackground(new Color(245, 247, 250));
        buttonsContainer.setBorder(BorderFactory.createEmptyBorder(100, 0, 0, 0));
        
        JPanel buttonsFlowPanel = new JPanel();
        buttonsFlowPanel.setBackground(new Color(245, 247, 250));
        buttonsFlowPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 0));
        
        JPanel medecinButton = createUserTypeButton("👨‍⚕️", "MÉDECIN", "Accès aux dossiers médicaux", "Medecin", 10001);
        medecinButton.setPreferredSize(new Dimension(200, 150));
        buttonsFlowPanel.add(medecinButton);
        
        JPanel secretaireButton = createUserTypeButton("👩‍💼", "SECRÉTAIRE", "Gestion administrative", "Secretaire", 20001);
        secretaireButton.setPreferredSize(new Dimension(200, 150));
        buttonsFlowPanel.add(secretaireButton);
        
        buttonsContainer.add(buttonsFlowPanel, BorderLayout.CENTER);
        mainPanel.add(buttonsContainer, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private JPanel createHeaderPanel(String title, String subtitle) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 247, 250));
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(new Color(41, 128, 185));
        
        JLabel subtitleLabel = new JLabel(subtitle, SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(100, 100, 100));
        
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        return headerPanel;
    }
    
    private JPanel createUserTypeButton(String emoji, String title, String desc, String type, int matricule) {
        JPanel buttonPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(220, 230, 240));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(new Color(41, 128, 185));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
            }
        };
        
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));
        buttonPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JLabel emojiLabel = new JLabel(emoji, SwingConstants.CENTER);
        emojiLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        emojiLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(new Color(41, 128, 185));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel descLabel = new JLabel(desc, SwingConstants.CENTER);
        descLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        descLabel.setForeground(new Color(100, 100, 100));
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        buttonPanel.add(emojiLabel);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(titleLabel);
        buttonPanel.add(Box.createVerticalStrut(5));
        buttonPanel.add(descLabel);
        
        buttonPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showPasswordDialog(type, matricule);
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                buttonPanel.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                buttonPanel.repaint();
            }
        });
        
        return buttonPanel;
    }
    
    private void showPasswordDialog(String type, int matricule) {
        JDialog dialog = new JDialog(this, "Authentification - " + type, true);
        dialog.setSize(460, 280);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(245, 247, 250));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 247, 250));
        
        JLabel titleLabel = new JLabel("Authentification", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(41, 128, 185));
        
        JLabel typeLabel = new JLabel("Profil: " + type, SwingConstants.CENTER);
        typeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        typeLabel.setForeground(new Color(100, 100, 100));
        
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(typeLabel, BorderLayout.SOUTH);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        JPanel formPanel = new JPanel(new GridLayout(2, 1, 10, 15));
        formPanel.setBackground(new Color(245, 247, 250));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        JLabel passwordLabel = new JLabel("Mot de passe:");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        
        JPasswordField passwordField = new JPasswordField();
        passwordField.setFont(new Font("Arial", Font.PLAIN, 13));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)));
        
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);
        
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        buttonsPanel.setBackground(new Color(245, 247, 250));
        
        JButton annulerBtn = creerBouton("Annuler", new Color(149, 165, 166), Color.WHITE);
        annulerBtn.addActionListener(e -> dialog.dispose());
        
        JButton connexionBtn = creerBouton("Connexion", new Color(46, 204, 113), Color.WHITE);
        connexionBtn.addActionListener(e -> {
            String motDePasse = new String(passwordField.getPassword());
            
            if (motDePasse.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,"Veuillez entrer votre mot de passe!","Erreur",JOptionPane.ERROR_MESSAGE);
                passwordField.requestFocus();
                return;
            }
            
            if (authentifier(type, matricule, motDePasse)) {
                dialog.dispose();
                ouvrirInterface(type, matricule);
            } else {
                JOptionPane.showMessageDialog(dialog,"Mot de passe incorrect!","Erreur",JOptionPane.ERROR_MESSAGE);
                passwordField.setText("");
                passwordField.requestFocus();
            }
        });
        
        passwordField.addActionListener(e -> connexionBtn.doClick());
        
        buttonsPanel.add(annulerBtn);
        buttonsPanel.add(connexionBtn);
        
        mainPanel.add(buttonsPanel, BorderLayout.SOUTH);
        
        dialog.add(mainPanel);
        dialog.setVisible(true);
        
        passwordField.requestFocus();
    }
    
    private JButton creerBouton(String texte, Color bg, Color fg) {
        JButton btn = new JButton(texte);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        return btn;
    }
    
    public boolean authentifier(String type, int matricule, String motDePasse) {
    String sqlSelect = "";
    String sqlUpdate = "UPDATE utilisateur SET derniereCnx = NOW() WHERE matUtili = ?";
    
    if (type.equals("Medecin")) {
        sqlSelect = "SELECT mdpUtili FROM utilisateur WHERE matUtili = ?";
    } else if (type.equals("Secretaire")) {
        sqlSelect = "SELECT mdpUtili FROM utilisateur WHERE matUtili = ?";
    }
         
    try (PreparedStatement pstmtSelect = db.prepareStatement(sqlSelect);
         PreparedStatement pstmtUpdate = db.prepareStatement(sqlUpdate)) {
       
        pstmtSelect.setInt(1, matricule);
        ResultSet rs = pstmtSelect.executeQuery();
        
        if (rs.next()) {
            String hashStocke = rs.getString("mdpUtili");
            boolean result = Hashage.verifyPassword(motDePasse, hashStocke);
            
            if (result) {
                pstmtUpdate.setInt(1, matricule);
                pstmtUpdate.executeUpdate();
            }
            
            return result;
        } else {
            return false;
        }
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    } catch (IllegalArgumentException e) {
        e.printStackTrace();
        return false;
    }
}
    
    private void ouvrirInterface(String type, int matricule) {
        this.setVisible(false);
        switch (type) {
            case "Medecin":
                new InterfaceMedecin(matricule, db, this).setVisible(true);
                break;
            case "Secretaire":
                new InterfaceSecretaire(matricule, db, this).setVisible(true);
                break;
            default:
                JOptionPane.showMessageDialog(this,"Type d'utilisateur non reconnu!","Erreur",JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static void main(String[] args) {
        DatabaseManager db = new DatabaseManager();
        java.awt.EventQueue.invokeLater(() -> {
            new Connexion(db).setVisible(true);
        });
    }
}