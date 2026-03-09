package hsmed;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class InterfaceGestionPatients extends JFrame {
    private final DatabaseManager db;
    private final int matricule;
    private final JFrame parentFrame;
    private JTable table;
    private DefaultTableModel tableModel;
    
    public InterfaceGestionPatients(int matricule, DatabaseManager db, JFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.matricule = matricule;
        this.db = db;
        initComponents();
        chargerPatients();
    }
    
    private void initComponents() {
        setTitle("Gestion des patients - HSMED");
        setSize(1000, 700);
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
    
    JLabel titleLabel = new JLabel("Gestion des patients");
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
        
        JButton searchBtn = creerBouton("Rechercher", new Color(52, 152, 219), Color.WHITE);
        JButton refreshBtn = creerBouton("Actualiser", new Color(46, 204, 113), Color.WHITE);
        
        searchBtn.addActionListener(e -> {
            String query = searchField.getText();
            if (!query.isEmpty()) {
                chercherPatients(query);
            } else {
                chargerPatients();
            }
        });
        
        refreshBtn.addActionListener(e -> chargerPatients());
        
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
            new Object[]{"Matricule", "Nom", "Prénom", "Sexe", "Date de naissance", "Num. Téléphone"}, 
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
        
        JButton addBtn = creerBouton("Ajouter", new Color(46, 204, 113), Color.WHITE);
        JButton editBtn = creerBouton("Modifier", new Color(241, 196, 15), Color.WHITE);
        
        addBtn.setPreferredSize(new Dimension(150, 35));
        editBtn.setPreferredSize(new Dimension(150, 35));
        
        addBtn.addActionListener(e -> ajouterPatient());
        editBtn.addActionListener(e -> modifierPatient());
        
        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        
        return buttonPanel;
    }
    
    private void chargerPatients() {
    tableModel.setRowCount(0);
    
    String sql = "SELECT matPat, nomPat, prenomPat, sexe, dateNaiss, numTelephone " +"FROM patient ORDER BY matPat ASC";
    
    try (PreparedStatement pstmt = db.prepareStatement(sql)) {
        ResultSet rs = pstmt.executeQuery();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        while (rs.next()) {
            Date dateNaiss = rs.getDate("dateNaiss");
            String dateFormatted = "";
            if (dateNaiss != null) {
                dateFormatted = dateNaiss.toLocalDate().format(formatter);
            }
            
            Object[] row = {
                rs.getInt("matPat"),
                rs.getString("nomPat"),
                rs.getString("prenomPat"),
                rs.getString("sexe").equals("M") ? "Masculin" : "Féminin",
                dateFormatted,
                rs.getString("numTelephone")
            };
            tableModel.addRow(row);
        }
    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Erreur lors du chargement: " + e.getMessage(), 
            "Erreur", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }
}
    
    private void chercherPatients(String query) {
        tableModel.setRowCount(0);
        
        String sql = "SELECT matPat, nomPat, prenomPat, sexe, dateNaiss, numTelephone " +
                    "FROM patient WHERE nomPat LIKE ? OR prenomPat LIKE ? OR numTelephone LIKE ? OR matPat LIKE ?";
        
        try (PreparedStatement pstmt = db.prepareStatement(sql)) {
            String searchTerm = "%" + query + "%";
            pstmt.setString(1, searchTerm);
            pstmt.setString(2, searchTerm);
            pstmt.setString(3, searchTerm);
            pstmt.setString(4, searchTerm);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Object[] row = {
                    rs.getInt("matPat"),
                    rs.getString("nomPat"),
                    rs.getString("prenomPat"),
                    rs.getString("sexe"),
                    rs.getDate("dateNaiss"),
                    rs.getString("numTelephone")
                };
                tableModel.addRow(row);
            }
            
            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Aucun patient trouvé", 
                    "Recherche", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors de la recherche: " + e.getMessage(), 
                "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void ajouterPatient() {
        JTextField nomField = new JTextField();
        JTextField prenomField = new JTextField();
        JComboBox<String> sexeCombo = new JComboBox<>(new String[]{"Masculin", "Féminin"});
        JTextField dateField = new JTextField();
        dateField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        JTextField phoneField = new JTextField();
        
        Object[] fields = {
            "Nom:", nomField,
            "Prénom:", prenomField,
            "Sexe:", sexeCombo,
            "Date de Naissance (AAAA-MM-JJ):", dateField,
            "Numéro de Téléphone:", phoneField
        };
        
        int result = JOptionPane.showConfirmDialog(this, fields, "Ajouter un Patient", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                String nom = nomField.getText().trim();
                String prenom = prenomField.getText().trim();
                String sexe = (String) sexeCombo.getSelectedItem();
                sexe = "Masculin".equals(sexe) ? "M" : "F";
                String dateStr = dateField.getText().trim();
                String phone = phoneField.getText().trim();
                
                if (nom.isEmpty() || prenom.isEmpty() || dateStr.isEmpty() || phone.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs", 
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                try {
                    LocalDate.parse(dateStr);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Format de date invalide. Utilisez AAAA-MM-JJ", 
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (phone.length() < 10) {
                    JOptionPane.showMessageDialog(this, 
                        "Le numéro de téléphone doit contenir au moins 10 chiffres",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                String sql = "INSERT INTO patient (nomPat, prenomPat, sexe, dateNaiss, numTelephone) " +
                           "VALUES (?, ?, ?, ?, ?)";
                
                try (PreparedStatement pstmt = db.prepareStatement(sql)) {
                    pstmt.setString(1, nom);
                    pstmt.setString(2, prenom);
                    pstmt.setString(3, sexe);
                    pstmt.setString(4, dateStr);
                    pstmt.setString(5, phone);
                    
                    int rows = pstmt.executeUpdate();
                    if (rows > 0) {
                        JOptionPane.showMessageDialog(this, 
                            "Patient ajouté avec succès!", "Succès", JOptionPane.INFORMATION_MESSAGE);
                        chargerPatients();
                    }
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, 
                    "Erreur lors de l'ajout du patient: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    private void modifierPatient() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, 
                "Veuillez sélectionner un patient dans le tableau", "Information", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int matPat = (int) tableModel.getValueAt(row, 0);
        String nomActuel = (String) tableModel.getValueAt(row, 1);
        String prenomActuel = (String) tableModel.getValueAt(row, 2);
        String sexeActuel = (String) tableModel.getValueAt(row, 3);
        String dateActuelle = (String) tableModel.getValueAt(row, 4);
        String phoneActuel = (String) tableModel.getValueAt(row, 5);
        
        String dateForEditing = dateActuelle;
        try {
            if (!dateActuelle.isEmpty()) {
                LocalDate date = LocalDate.parse(dateActuelle, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                dateForEditing = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
        } catch (Exception e) {}
        
        JTextField nomField = new JTextField(nomActuel);
        JTextField prenomField = new JTextField(prenomActuel);
        JComboBox<String> sexeCombo = new JComboBox<>(new String[]{"Masculin", "Féminin"});
        sexeCombo.setSelectedItem(sexeActuel);
        JTextField dateField = new JTextField(dateForEditing);
        JTextField phoneField = new JTextField(phoneActuel);
        
        Object[] fields = {
            "Nom:", nomField,
            "Prénom:", prenomField,
            "Sexe:", sexeCombo,
            "Date de Naissance (YYYY-MM-DD):", dateField,
            "Numéro de Téléphone:", phoneField
        };
        
        int result = JOptionPane.showConfirmDialog(this, fields, "Modifier Patient N°" + matPat, 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                String nom = nomField.getText().trim();
                String prenom = prenomField.getText().trim();
                String sexe = (String) sexeCombo.getSelectedItem();
                sexe = "Masculin".equals(sexe) ? "M" : "F";
                String dateStr = dateField.getText().trim();
                String phone = phoneField.getText().trim();
                
                if (nom.isEmpty() || prenom.isEmpty() || dateStr.isEmpty() || phone.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs", "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                try {
                    LocalDate.parse(dateStr);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Format de date invalide. Utilisez AAAA-MM-JJ","Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (phone.length() < 10) {
                    JOptionPane.showMessageDialog(this, 
                        "Le numéro de téléphone doit contenir au moins 10 chiffres","Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                String sql = "UPDATE patient SET nomPat = ?, prenomPat = ?, sexe = ?, " +
                           "dateNaiss = ?, numTelephone = ? WHERE matPat = ?";
                
                try (PreparedStatement pstmt = db.prepareStatement(sql)) {
                    pstmt.setString(1, nom);
                    pstmt.setString(2, prenom);
                    pstmt.setString(3, sexe);
                    pstmt.setString(4, dateStr);
                    pstmt.setString(5, phone);
                    pstmt.setInt(6, matPat);
                    
                    int rows = pstmt.executeUpdate();
                    if (rows > 0) {
                        JOptionPane.showMessageDialog(this, "Patient modifié avec succès!", "Succès", JOptionPane.INFORMATION_MESSAGE);
                        chargerPatients();
                    }
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, 
                    "Erreur lors de la modification du patient: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
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