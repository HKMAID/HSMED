package hsmed;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class InterfaceDossiersMedicaux extends JFrame {
    private final DatabaseManager db;
    private final int matricule;
    private final JFrame parentFrame;
    private JTable table;
    private DefaultTableModel tableModel;
    
    public InterfaceDossiersMedicaux(int matricule, DatabaseManager db, JFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.matricule = matricule;
        this.db = db;
        initComponents();
        loadDossiers();
    }
    
    private void initComponents() {
        setTitle("Dossiers Médicaux - HSMED");
        setSize(900,700);
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
    
    JLabel titleLabel = new JLabel("Gestion des Dossiers Médicaux");
    titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
    titleLabel.setForeground(new Color(41, 128, 185));
    titleLabel.setHorizontalAlignment(JLabel.CENTER);
    
    JButton backBtn = creerBouton("← Retour", new Color(149, 165, 166), Color.WHITE);
    backBtn.setPreferredSize(new Dimension(120, 40));
    backBtn.addActionListener(e -> retourner());

    JPanel centerPanel = new JPanel(new BorderLayout());
    centerPanel.setOpaque(false);
    centerPanel.add(titleLabel, BorderLayout.CENTER);
    
    headerPanel.add(backBtn, BorderLayout.EAST);
    headerPanel.add(centerPanel, BorderLayout.CENTER);
    
    return headerPanel;
}
    
    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.setBackground(new Color(245, 247, 250));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JTextField searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JButton searchBtn = creerBouton("Rechercher", new Color(52, 152, 219), Color.WHITE);
        JButton refreshBtn = creerBouton("Actualiser", new Color(46, 204, 113), Color.WHITE);
        
        searchBtn.addActionListener(e -> {
            String query = searchField.getText();
            if (!query.isEmpty()) {
                rechercherDossiers(query);
            }
        });
        
        refreshBtn.addActionListener(e -> loadDossiers());
        
        searchPanel.add(new JLabel("Rechercher:"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(refreshBtn);
        
        return searchPanel;
    }
    
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        
        tableModel = new DefaultTableModel(
            new Object[]{"N° Dossier", "Nom", "Prénom", "Date de création", "Groupe Sanguin"}, 
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
        
        JScrollPane scrollPane = new JScrollPane(table);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }
    
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(new Color(245, 247, 250));
        
        JButton viewBtn = creerBouton("Consulter", new Color(41, 128, 185), Color.WHITE);
        JButton addBtn = creerBouton("Nouveau", new Color(46, 204, 113), Color.WHITE);
        JButton editBtn = creerBouton("Modifier", new Color(241, 196, 15), Color.WHITE);
        
        viewBtn.setPreferredSize(new Dimension(150, 35));
        addBtn.setPreferredSize(new Dimension(150, 35));
        editBtn.setPreferredSize(new Dimension(150, 35));
        
        viewBtn.addActionListener(e -> consulterDossier());
        addBtn.addActionListener(e -> ajouterDossier());
        editBtn.addActionListener(e -> modifierDossier());
        
        buttonPanel.add(viewBtn);
        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        
        return buttonPanel;
    }
    
    private void loadDossiers() {
        tableModel.setRowCount(0);
        
        String sql = "SELECT d.matDoss, p.nomPat, p.prenomPat, d.dateCreation, d.grpSanguin " +
                    "FROM dossiermedical d JOIN patient p ON d.matPat = p.matPat";
        
        try (PreparedStatement pstmt = db.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("d.matDoss"),
                    rs.getString("p.nomPat"),
                    rs.getString("p.prenomPat"),
                    rs.getDate("d.dateCreation"),
                    rs.getString("d.grpSanguin")
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement: " + e.getMessage(), 
                "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void rechercherDossiers(String query) {
        tableModel.setRowCount(0);
        
        String sql = "SELECT d.matDoss, p.nomPat, p.prenomPat, d.dateCreation, d.grpSanguin " +
                    "FROM dossiermedical d JOIN patient p ON d.matPat = p.matPat " +
                    "WHERE d.matDoss LIKE ? OR p.nomPat LIKE ? OR p.prenomPat LIKE ?";
        
        try (PreparedStatement pstmt = db.prepareStatement(sql)) {
            String searchTerm = "%" + query + "%";
            pstmt.setString(1, searchTerm);
            pstmt.setString(2, searchTerm);
            pstmt.setString(3, searchTerm);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("d.matDoss"),
                    rs.getString("p.nomPat"),
                    rs.getString("p.prenomPat"),
                    rs.getDate("d.dateCreation"),
                    rs.getString("d.grpSanguin")
                };
                tableModel.addRow(row);
            }
            
            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Aucun résultat trouvé", 
                    "Recherche", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors de la recherche: " + e.getMessage(), 
                "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void consulterDossier() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un dossier", 
                "Erreur", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int matDoss = (int) tableModel.getValueAt(row, 0);
        String nom = (String) tableModel.getValueAt(row, 1);
        String prenom = (String) tableModel.getValueAt(row, 2);
        String dateCreation = tableModel.getValueAt(row, 3).toString();
        String grpSanguin = (String) tableModel.getValueAt(row, 4);
      
        String sql = "SELECT remarques, traitements FROM dossiermedical WHERE matDoss = ?";
        String remarques = "Aucune remarque";
        String traitements = "Aucun traitement";
        
        try (PreparedStatement pstmt = db.prepareStatement(sql)) {
            pstmt.setInt(1, matDoss);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String rem = rs.getString("remarques");
                String trait = rs.getString("traitements");
                
                remarques = (rem != null && !rem.isEmpty()) ? rem : "Aucune remarque";
                traitements = (trait != null && !trait.isEmpty()) ? trait : "Aucun traitement";
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des remarques/traitements: " + e.getMessage());
            e.printStackTrace();
        }
        
        String message = String.format(
            "Dossier Médical N° %d\n\n" +
            "Patient: %s %s\n" +
            "Date création: %s\n" +
            "Groupe sanguin: %s\n\n" +
            "━━━━━━━━━━━━━━━━━━━━━━\n" +
            "REMARQUES:\n%s\n\n" +
            "TRAITEMENTS:\n%s",
            matDoss, nom, prenom, dateCreation, grpSanguin, remarques, traitements
        );
        
        JOptionPane.showMessageDialog(this, message, "Dossier Médical - N°" + matDoss, 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void ajouterDossier() {
        JTextField nomField = new JTextField();
        JTextField prenomField = new JTextField();
        JComboBox<String> grpSanguinCombo = new JComboBox<>(new String[]{
            "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
        });
        JTextArea remarquesArea = new JTextArea(3, 20);
        JTextArea traitementsArea = new JTextArea(3, 20);
        
        JScrollPane remarquesScroll = new JScrollPane(remarquesArea);
        JScrollPane traitementsScroll = new JScrollPane(traitementsArea);
        
        Object[] fields = {
            "Nom du patient:", nomField,
            "Prénom du patient:", prenomField,
            "Groupe Sanguin:", grpSanguinCombo,
            "Remarques:", remarquesScroll,
            "Traitements:", traitementsScroll
        };
        
        int result = JOptionPane.showConfirmDialog(this, fields, "Nouveau Dossier Médical", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                String nom = nomField.getText().trim();
                String prenom = prenomField.getText().trim();
                
                if (nom.isEmpty() || prenom.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Veuillez entrer le nom et le prénom du patient","Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                String sqlVerif = "SELECT matPat FROM patient WHERE nomPat = ? AND prenomPat = ?";
                int matPat = -1;
                
                try (PreparedStatement pstmtVerif = db.prepareStatement(sqlVerif)) {
                    pstmtVerif.setString(1, nom);
                    pstmtVerif.setString(2, prenom);
                    ResultSet rsVerif = pstmtVerif.executeQuery();
                    
                    if (rsVerif.next()) {
                        matPat = rsVerif.getInt("matPat");
                    } else {
                        JOptionPane.showMessageDialog(this, 
                            "Patient non trouvé: " + nom + " " + prenom + "\n\n" +
                            "Veuillez vérifier l'orthographe ou ajouter le patient en premier",
                            "Patient introuvable", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
                
                String sqlCheckDossier = "SELECT matDoss FROM dossiermedical WHERE matPat = ?";
                
                try (PreparedStatement pstmtCheck = db.prepareStatement(sqlCheckDossier)) {
                    pstmtCheck.setInt(1, matPat);
                    ResultSet rsCheck = pstmtCheck.executeQuery();
                    
                    if (rsCheck.next()) {
                        JOptionPane.showMessageDialog(this, 
                            "Ce patient possède déjà un dossier médical (N°" + rsCheck.getInt("matDoss") + ")",
                            "Dossier existant", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
                
                String grpSanguin = (String) grpSanguinCombo.getSelectedItem();
                String remarques = remarquesArea.getText();
                String traitements = traitementsArea.getText();
                
                String sql = "INSERT INTO dossiermedical (matPat, dateCreation, grpSanguin, remarques, traitements) " +
                           "VALUES (?, CURDATE(), ?, ?, ?)";
                
                try (PreparedStatement pstmt = db.prepareStatement(sql)) {
                    pstmt.setInt(1, matPat);
                    pstmt.setString(2, grpSanguin);
                    pstmt.setString(3, remarques.isEmpty() ? null : remarques);
                    pstmt.setString(4, traitements.isEmpty() ? null : traitements);
                    
                    int rows = pstmt.executeUpdate();
                    if (rows> 0) {
                        JOptionPane.showMessageDialog(this, 
                            "Dossier créé avec succès pour " + nom + " " + prenom,"Succès", JOptionPane.INFORMATION_MESSAGE);
                        loadDossiers();
                    }
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erreur SQL: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    private void modifierDossier() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un dossier","Erreur", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int matDoss = (int) tableModel.getValueAt(row, 0);
        
        String sqlSelect = "SELECT remarques, traitements FROM dossiermedical WHERE matDoss = ?";
        String remarquesActuelles = "";
        String traitementsActuels = "";
        
        try (PreparedStatement pstmtSelect = db.prepareStatement(sqlSelect)) {
            pstmtSelect.setInt(1, matDoss);
            ResultSet rsSelect = pstmtSelect.executeQuery();
            
            if (rsSelect.next()) {
                remarquesActuelles = rsSelect.getString("remarques") != null ? rsSelect.getString("remarques") : "";
                traitementsActuels = rsSelect.getString("traitements") != null ? rsSelect.getString("traitements") : "";
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors du chargement des données: " + e.getMessage());
        }
        
        JTextArea remarquesArea = new JTextArea(3, 20);
        JTextArea traitementsArea = new JTextArea(3, 20);
        
        remarquesArea.setText(remarquesActuelles);
        traitementsArea.setText(traitementsActuels);
        
        JScrollPane remarquesScroll = new JScrollPane(remarquesArea);
        JScrollPane traitementsScroll = new JScrollPane(traitementsArea);
        
        Object[] fields = {
            "Remarques:", remarquesScroll,
            "Traitements:", traitementsScroll
        };
        
        int result = JOptionPane.showConfirmDialog(this, fields, "Modifier Dossier num° " + matDoss, 
            JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                String sql = "UPDATE dossiermedical SET remarques = ?, traitements = ? WHERE matDoss = ?";
                
                try (PreparedStatement pstmt = db.prepareStatement(sql)) {
                    pstmt.setString(1, remarquesArea.getText().isEmpty() ? null : remarquesArea.getText());
                    pstmt.setString(2, traitementsArea.getText().isEmpty() ? null : traitementsArea.getText());
                    pstmt.setInt(3, matDoss);
                    
                    int rows = pstmt.executeUpdate();
                    if (rows > 0) {
                        JOptionPane.showMessageDialog(this, "Dossier modifié avec succès","Succès", JOptionPane.INFORMATION_MESSAGE);
                        loadDossiers();
                    }
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erreur SQL: " + e.getMessage(), 
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            }
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
        btn.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        return btn;
    }
}