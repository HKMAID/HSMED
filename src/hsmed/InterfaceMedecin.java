package hsmed;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;

public class InterfaceMedecin extends JFrame {

    static boolean affichée;
    private final DatabaseManager db;
    private final JFrame parentFrame;
    private final int matricule;
    
    public InterfaceMedecin(int matricule, DatabaseManager db, JFrame parentFrame) {
        this.matricule = matricule;
        this.db = db;
        this.parentFrame = parentFrame;
        initComponents();
        affichée = false;
    }
    
    private void initComponents() {
        setTitle("Espace Médecin - HSMED");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(245, 247, 250));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        JPanel headerPanel = createHeaderWithUserInfo();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        JPanel buttonsContainer = new JPanel(new BorderLayout());
        buttonsContainer.setBackground(new Color(245, 247, 250));
        buttonsContainer.setBorder(BorderFactory.createEmptyBorder(100, 0, 0, 0));
        
        JPanel buttonsFlowPanel = new JPanel();
        buttonsFlowPanel.setBackground(new Color(245, 247, 250));
        buttonsFlowPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 0));
        
        JPanel patientsButton = creerBoutonMenu("📅", "Plannings", "Consulter vos plannings", "Plan");
        patientsButton.setPreferredSize(new Dimension(200, 150));
        buttonsFlowPanel.add(patientsButton);
        
        JPanel medecinButton = creerBoutonMenu("👥️", "Dossiers médicaux", "Gestion des dossiers médicaux", "Doss");
        medecinButton.setPreferredSize(new Dimension(200, 150));
        buttonsFlowPanel.add(medecinButton);
        
        buttonsContainer.add(buttonsFlowPanel, BorderLayout.CENTER);
        mainPanel.add(buttonsContainer, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private JPanel createHeaderWithUserInfo() {
    JPanel headerPanel = new JPanel(new BorderLayout());
    headerPanel.setBackground(new Color(245, 247, 250));
        
    JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    userInfoPanel.setBackground(new Color(245, 247, 250));
        
    String[] userInfo = getNomPrenom();
    String nomPrenom = "Dr. "+userInfo[0] + " " + userInfo[1];
        
    JLabel userLabel = new JLabel(nomPrenom);
    userLabel.setFont(new Font("Arial", Font.BOLD, 14));
    userLabel.setForeground(Color.BLACK);
    userInfoPanel.add(userLabel);
    
    JPanel centerPanel = new JPanel(new BorderLayout());
    centerPanel.setBackground(new Color(245, 247, 250));
    centerPanel.setBorder(BorderFactory.createEmptyBorder(70, 0, 0, 16)); // tableau de bord : -70y, -16x 
    
    JLabel titleLabel = new JLabel("Tableau de bord", SwingConstants.CENTER);
    titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
    titleLabel.setForeground(new Color(41, 128, 185));
    centerPanel.add(titleLabel, BorderLayout.CENTER);
    
    JPanel disconnectPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    disconnectPanel.setBackground(new Color(245, 247, 250));
    
    JButton deconnectBtn = creerBouton("Déconnexion", new Color(231, 76, 60), Color.WHITE);
    deconnectBtn.setPreferredSize(new Dimension(130, 35));
    deconnectBtn.addActionListener(e -> deconnecter());
    disconnectPanel.add(deconnectBtn);
    
    headerPanel.add(userInfoPanel, BorderLayout.WEST);
    headerPanel.add(centerPanel, BorderLayout.CENTER);
    headerPanel.add(disconnectPanel, BorderLayout.EAST);
    
    return headerPanel;
}
    
    private String[] getNomPrenom() {
        String[] userInfo = {"Utilisateur", "Inconnu"};
        
        String sql = "SELECT nomUtili, prenomUtili FROM utilisateur WHERE matUtili = ?";
        
        try (PreparedStatement pstmt = db.prepareStatement(sql)) {
            pstmt.setInt(1, matricule);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                userInfo[0] = rs.getString("nomUtili");
                userInfo[1] = rs.getString("prenomUtili");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des infos utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
        
        return userInfo;
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
    
    private JPanel creerBoutonMenu(String emoji, String title, String desc, String type) {
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
                if (type.equals("Plan")) {
                    new InterfacePlanning(matricule, db, InterfaceMedecin.this).setVisible(true);
                    InterfaceMedecin.this.setVisible(false);
                    Timer timer = new Timer(1000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    affichée = true;}
            });
            timer.setRepeats(false);
            timer.start();
                } else if (type.equals("Doss")) {
                    new InterfaceDossiersMedicaux(matricule, db, InterfaceMedecin.this).setVisible(true);
                    InterfaceMedecin.this.setVisible(false);
                }
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
    
    private void deconnecter() {
    int confirmation = JOptionPane.showConfirmDialog(this, "Êtes-vous sûr de vouloir vous déconnecter ?", "Confirmation de déconnexion", JOptionPane.YES_NO_OPTION,
        JOptionPane.WARNING_MESSAGE);
    if (confirmation == JOptionPane.YES_OPTION) {
        parentFrame.setVisible(true);
        this.dispose();
        JOptionPane.showMessageDialog(parentFrame,"Vous avez été déconnecté avec succès.","Déconnexion",JOptionPane.INFORMATION_MESSAGE);
    }
}
}