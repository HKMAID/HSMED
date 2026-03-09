package hsmed;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class InterfaceGestionMedecin extends JFrame {
    private final DatabaseManager db;
    private final int matricule;
    private final JFrame parentFrame;
    private JTable table;
    private DefaultTableModel tableModel;
    
    public InterfaceGestionMedecin(int matricule, DatabaseManager db, JFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.matricule = matricule;
        this.db = db;
        initComponents();
        chargerCreneaux();
    }
    
    private void initComponents() {
        setTitle("Gestion des Créneaux - HSMED");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(245, 247, 250));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JPanel headerPanel = createHeaderPanel();
        JPanel searchPanel = createSearchPanel();
        
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setBackground(new Color(245, 247, 250));
        topContainer.add(headerPanel, BorderLayout.NORTH);
        topContainer.add(searchPanel, BorderLayout.SOUTH);
        
        mainPanel.add(topContainer, BorderLayout.NORTH);
        
        JPanel tablePanel = createTablePanel();
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(new Color(245, 247, 250));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JLabel titleLabel = new JLabel("Gestion des Créneaux");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(41, 128, 185));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JButton backBtn = creerBouton("← Retour", new Color(149, 165, 166), Color.WHITE);
        backBtn.setPreferredSize(new Dimension(120, 40));
        backBtn.addActionListener(e -> retourner());
        
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(backBtn, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBackground(new Color(245, 247, 250));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JTextField searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 12));
        searchField.setToolTipText("Rechercher par date (YYYY-MM-DD) ou état");
        
        JButton searchBtn = creerBouton("Rechercher", new Color(52, 152, 219), Color.WHITE);
        JButton refreshBtn = creerBouton("Actualiser", new Color(46, 204, 113), Color.WHITE);
        JButton updateBtn = creerBouton("Mettre à jour", new Color(230, 126, 34), Color.WHITE);
        
        searchBtn.addActionListener(e -> {
            String query = searchField.getText();
            if (!query.isEmpty()) {
                chercherCreneaux(query);
            } else {
                chargerCreneaux();
            }
        });
        
        refreshBtn.addActionListener(e -> {
            chargerCreneaux();
            JOptionPane.showMessageDialog(this,"Affichage actualisé","Actualisation", JOptionPane.INFORMATION_MESSAGE);
        });
        
        updateBtn.addActionListener(e -> {
            actualiserCreneauxPasses();
        });
        
        searchPanel.add(new JLabel("Rechercher:"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(refreshBtn);
        searchPanel.add(updateBtn);
        
        return searchPanel;
    }
    
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        
        tableModel = new DefaultTableModel(
            new Object[]{"Date", "Début", "Fin", "État"}, 
            0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        table = new JTable(tableModel);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(41, 128, 185));
        table.getTableHeader().setForeground(Color.WHITE);
        
        table.getColumnModel().getColumn(0).setPreferredWidth(120); 
        table.getColumnModel().getColumn(1).setPreferredWidth(80);  
        table.getColumnModel().getColumn(2).setPreferredWidth(80);  
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        
        JScrollPane scrollPane = new JScrollPane(table);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }
    
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(new Color(245, 247, 250));
        
        JButton addBtn = creerBouton("Ajouter Créneau", new Color(46, 204, 113), Color.WHITE);
        JButton indispoBtn = creerBouton("Marquer Indisponible", new Color(241, 196, 15), Color.WHITE);
        JButton deleteBtn = creerBouton("Supprimer", new Color(231, 76, 60), Color.WHITE);
        
        addBtn.setPreferredSize(new Dimension(180, 35));
        indispoBtn.setPreferredSize(new Dimension(180, 35));
        deleteBtn.setPreferredSize(new Dimension(180, 35));
        
        addBtn.addActionListener(e -> ajouterCreneau());
        indispoBtn.addActionListener(e -> marquerIndisponible());
        deleteBtn.addActionListener(e -> supprimerCreneau());
        
        buttonPanel.add(addBtn);
        buttonPanel.add(indispoBtn);
        buttonPanel.add(deleteBtn);
        
        return buttonPanel;
    }
    
    private void chargerCreneaux() {
        tableModel.setRowCount(0);
        
        String sql = "SELECT debut, fin, etatCren " + "FROM creneau " + "WHERE DATE(debut) >= CURDATE()" + "ORDER BY DATE(debut) ASC, debut ASC";
        try (PreparedStatement pstmt = db.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            
            while (rs.next()) {
                Timestamp debut = rs.getTimestamp("debut");
                Timestamp fin = rs.getTimestamp("fin");
                
                String dateFormatted = "";
                String debutFormatted = "";
                String finFormatted = "";
                
                if (debut != null) {
                    dateFormatted = debut.toLocalDateTime().format(dateFormatter);
                    debutFormatted = debut.toLocalDateTime().format(timeFormatter);
                }
                if (fin != null) {
                    finFormatted = fin.toLocalDateTime().format(timeFormatter);
                }
                
                Object[] row = {
                    dateFormatted,
                    debutFormatted,
                    finFormatted,
                    rs.getString("etatCren")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void chercherCreneaux(String query) {
        tableModel.setRowCount(0);
        
        String sql = "SELECT debut, fin, etatCren "+ "FROM creneau " +"WHERE DATE(debut) = ? OR etatCren LIKE ? "+ "ORDER BY debut DESC";
        
        try (PreparedStatement pstmt = db.prepareStatement(sql)) {
            try {
                pstmt.setString(1, query);
            } catch (Exception e) {
                pstmt.setString(1, "");
            }
            pstmt.setString(2, "%" + query + "%");
            
            ResultSet rs = pstmt.executeQuery();
            
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            
            while (rs.next()) {
                Timestamp debut = rs.getTimestamp("debut");
                Timestamp fin = rs.getTimestamp("fin");
                
                String dateFormatted = "";
                String debutFormatted = "";
                String finFormatted = "";
                
                if (debut != null) {
                    dateFormatted = debut.toLocalDateTime().format(dateFormatter);
                    debutFormatted = debut.toLocalDateTime().format(timeFormatter);
                }
                if (fin != null) {
                    finFormatted = fin.toLocalDateTime().format(timeFormatter);
                }
                
                Object[] row = {
                    dateFormatted,
                    debutFormatted,
                    finFormatted,
                    rs.getString("etatCren")
                };
                tableModel.addRow(row);
            }
            
            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Aucun créneau trouvé", "Recherche", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors de la recherche: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void ajouterCreneau() {
    JTextField dateDebut = new JTextField("YYYY-MM-DD HH:mm");
    JTextField dateFin = new JTextField("YYYY-MM-DD HH:mm");
    
    Object[] fields = {
        "Début du créneau (YYYY-MM-DD HH:mm):", dateDebut,
        "Fin du créneau (YYYY-MM-DD HH:mm):", dateFin
    };
    
    int result = JOptionPane.showConfirmDialog(this, fields, "Ajouter un Créneau", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    
    if (result == JOptionPane.OK_OPTION) {
        try {
            String debutStr = dateDebut.getText().trim();
            String finStr = dateFin.getText().trim();
            
            if (debutStr.isEmpty() || finStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            LocalDateTime debutDateTime = null;
            LocalDateTime finDateTime = null;
            try {
                debutDateTime = LocalDateTime.parse(debutStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                finDateTime = LocalDateTime.parse(finStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Format de date/heure invalide. Utilisez YYYY-MM-DD HH:mm", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            LocalDateTime maintenant = LocalDateTime.now();
            if (debutDateTime.isBefore(maintenant)) {
                JOptionPane.showMessageDialog(this, "La date de début ne peut pas être dans le passé", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (finDateTime.isBefore(maintenant)) {
                JOptionPane.showMessageDialog(this, "La date de fin ne peut pas être dans le passé", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (finDateTime.isBefore(debutDateTime) || finDateTime.isEqual(debutDateTime)) {
                JOptionPane.showMessageDialog(this, "La fin du créneau doit être après le début", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            String[] etats = {"disponible", "indisponible"};
            String etatChoisi = (String) JOptionPane.showInputDialog(this, "Sélectionner l'état initial du créneau:",
                "État du créneau",
                JOptionPane.QUESTION_MESSAGE,
                null,
                etats,
                etats[0]);
            
            if (etatChoisi == null) {
                return;
            }
            
            String sql = "INSERT INTO creneau (debut, fin, etatCren) " +"VALUES (?, ?, ?)";
            
            try (PreparedStatement pstmt = db.prepareStatement(sql)) {
                pstmt.setTimestamp(1, Timestamp.valueOf(debutDateTime));
                pstmt.setTimestamp(2, Timestamp.valueOf(finDateTime));
                pstmt.setString(3, etatChoisi);
                
                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    JOptionPane.showMessageDialog(this, "Créneau ajouté avec succès!\nÉtat: " + etatChoisi, "Succès", JOptionPane.INFORMATION_MESSAGE);
                    chargerCreneaux();
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors de l'ajout du créneau: " + e.getMessage(),"Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
    
    private void marquerIndisponible() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, 
                "Veuillez sélectionner un créneau dans le tableau","Information", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String etatActuel = (String) tableModel.getValueAt(row, 3);
        
        if ("Indisponible".equals(etatActuel)) {
            JOptionPane.showMessageDialog(this, 
                "Ce créneau est déjà marqué comme indisponible", 
                "Information", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Êtes-vous sûr de vouloir marquer ce créneau comme indisponible?",
            "Confirmation", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String debutStr = (String) tableModel.getValueAt(row, 1);
                String finStr = (String) tableModel.getValueAt(row, 2);
                String dateStr = (String) tableModel.getValueAt(row, 0);
                
                LocalDateTime debut = LocalDateTime.parse(dateStr + " " + debutStr, 
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                LocalDateTime fin = LocalDateTime.parse(dateStr + " " + finStr, 
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                
                String sql = "UPDATE creneau SET etatCren = 'Indisponible' " +"WHERE debut = ? AND fin = ?";
                
                try (PreparedStatement pstmt = db.prepareStatement(sql)) {
                    pstmt.setTimestamp(1, Timestamp.valueOf(debut));
                    pstmt.setTimestamp(2, Timestamp.valueOf(fin));
                    
                    int rows = pstmt.executeUpdate();
                    
                    if (rows > 0) {
                        JOptionPane.showMessageDialog(this, "Créneau marqué comme indisponible", "Succès", JOptionPane.INFORMATION_MESSAGE);
                        chargerCreneaux();
                    } else {
                        JOptionPane.showMessageDialog(this, "Aucun créneau n'a été modifié", "Information", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erreur lors de la mise à jour: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    private void supprimerCreneau() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un créneau dans le tableau", "Information", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, "Êtes-vous sûr de vouloir supprimer ce créneau?\nCette action est irréversible.",
                "Confirmation de suppression", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String debutStr = (String) tableModel.getValueAt(row, 1);
                String finStr = (String) tableModel.getValueAt(row, 2);
                String dateStr = (String) tableModel.getValueAt(row, 0);
                
                LocalDateTime debut = LocalDateTime.parse(dateStr + " " + debutStr, 
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                LocalDateTime fin = LocalDateTime.parse(dateStr + " " + finStr, 
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                
                String sql = "DELETE FROM creneau WHERE debut = ? AND fin = ?";
                
                try (PreparedStatement pstmt = db.prepareStatement(sql)) {
                    pstmt.setTimestamp(1, Timestamp.valueOf(debut));
                    pstmt.setTimestamp(2, Timestamp.valueOf(fin));
                    
                    int rows = pstmt.executeUpdate();
                    
                    if (rows > 0) {
                        JOptionPane.showMessageDialog(this, "Créneau supprimé avec succès", "Succès", JOptionPane.INFORMATION_MESSAGE);
                        chargerCreneaux();
                    } else {
                        JOptionPane.showMessageDialog(this, "Aucun créneau n'a été supprimé", "Information", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erreur lors de la suppression: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    private void actualiserCreneauxPasses() {
        try {
            String sql = "UPDATE creneau SET etatCren = 'indisponible' " +"WHERE fin < NOW() AND etatCren != 'indisponible'";
            
            try (PreparedStatement pstmt = db.prepareStatement(sql)) {
                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    JOptionPane.showMessageDialog(this, rows + " créneau(x) passé(s) marqué(s) comme indisponible(s)", "Mise à jour", JOptionPane.INFORMATION_MESSAGE);
                    chargerCreneaux();
                } else {
                    JOptionPane.showMessageDialog(this, "Aucun créneau passé à mettre à jour", "Mise à jour", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors de la mise à jour: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void retourner() {
        if (parentFrame != null) {
            parentFrame.setVisible(true);
        }
        this.dispose();
    }
    
    private JButton creerBouton(String texte, Color bg, Color fg) {
        JButton btn = new JButton(texte);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        return btn;
    }
}