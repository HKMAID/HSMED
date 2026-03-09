package hsmed;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class InterfaceGestionRDV extends JFrame {

    private final DatabaseManager db;
    private final int matricule;
    private final JFrame parentFrame;
    private JTable table;
    private DefaultTableModel tableModel;
    private JToggleButton historyToggleBtn; 

    public InterfaceGestionRDV(int matricule, DatabaseManager db, JFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.matricule = matricule;
        this.db = db;
        initComponents();
        actualiser(); 
    }

    private void initComponents() {
        setTitle("Gestion des Rendez-vous - HSMED");
        setSize(1200, 750);
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

        JLabel titleLabel = new JLabel("Gestion des Rendez-vous");
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
        searchField.setToolTipText("Rechercher par nom, prénom ou date (AAAA-MM-JJ)");

        JButton searchBtn = creerBouton("Rechercher", new Color(52, 152, 219), Color.WHITE);
        JButton refreshBtn = creerBouton("Actualiser", new Color(46, 204, 113), Color.WHITE);
        
        historyToggleBtn = new JToggleButton("Voir Historique");
        historyToggleBtn.setFont(new Font("Arial", Font.BOLD, 12));
        historyToggleBtn.setBackground(new Color(155, 89, 182));
        historyToggleBtn.setForeground(Color.WHITE);
        historyToggleBtn.setFocusPainted(false);
        historyToggleBtn.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        historyToggleBtn.addActionListener(e -> {
            if (historyToggleBtn.isSelected()) {
                historyToggleBtn.setText("Voir RDV à venir");
                historyToggleBtn.setBackground(new Color(142, 68, 173)); 
            } else {
                historyToggleBtn.setText("Voir Historique");
                historyToggleBtn.setBackground(new Color(155, 89, 182));
            }
            actualiser(); 
        });

        searchBtn.addActionListener(e -> {
            String query = searchField.getText();
            if (!query.isEmpty()) {
                searchRDV(query);
            } else {
                actualiser();
            }
        });

        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            actualiser();
        });

        searchPanel.add(new JLabel("Rechercher:"));
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);
        searchPanel.add(refreshBtn);
        searchPanel.add(Box.createHorizontalStrut(20));
        searchPanel.add(historyToggleBtn); 

        return searchPanel;
    }

    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);

        tableModel = new DefaultTableModel(
            new Object[]{"Nom", "Prénom", "Date", "Heure Début", "Heure Fin"}, 
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
        JButton cancelBtn = creerBouton("Annuler", new Color(231, 76, 60), Color.WHITE);

        addBtn.setPreferredSize(new Dimension(150, 35));
        cancelBtn.setPreferredSize(new Dimension(150, 35));

        addBtn.addActionListener(e -> ajouterRDV());
        cancelBtn.addActionListener(e -> annulerRDV());

        buttonPanel.add(addBtn);
        buttonPanel.add(cancelBtn);

        return buttonPanel;
    }

    private void actualiser() {
        boolean showHistory = historyToggleBtn.isSelected();
        chargerRDV(showHistory);
    }

    private void chargerRDV(boolean isHistory) {
        tableModel.setRowCount(0);

        String sql;
        if (isHistory) {
            sql = "SELECT p.nomPat, p.prenomPat, DATE(c.debut), c.debut, c.fin " +
                  "FROM rdv r " + "JOIN patient p ON r.matPat = p.matPat " +
                  "JOIN creneau c ON r.matCren = c.matCren " +
                  "WHERE c.debut < NOW() " +"ORDER BY c.debut DESC";  
        } else {
            sql = "SELECT p.nomPat, p.prenomPat, DATE(c.debut), c.debut, c.fin " +
                  "FROM rdv r " +
                  "JOIN patient p ON r.matPat = p.matPat " +
                  "JOIN creneau c ON r.matCren = c.matCren " +
                  "WHERE c.debut >= NOW() " + "ORDER BY c.debut ASC";     
        }

        try (PreparedStatement pstmt = db.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            while (rs.next()) {
                String nomPat = rs.getString("nomPat");
                String prenomPat = rs.getString("prenomPat");
                Timestamp debut = rs.getTimestamp("debut");
                Timestamp fin = rs.getTimestamp("fin");

                String dateFormatted = "";
                String heureDebutFormatted = "";
                String heureFinFormatted = "";

                if (debut != null && fin != null) {
                    LocalDateTime dateTimeDebut = debut.toLocalDateTime();
                    LocalDateTime dateTimeFin = fin.toLocalDateTime();

                    dateFormatted = dateTimeDebut.toLocalDate().format(dateFormatter);
                    heureDebutFormatted = dateTimeDebut.toLocalTime().format(timeFormatter);
                    heureFinFormatted = dateTimeFin.toLocalTime().format(timeFormatter);
                }

                Object[] row = {
                    nomPat,
                    prenomPat,
                    dateFormatted,
                    heureDebutFormatted,
                    heureFinFormatted
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void searchRDV(String query) {
        tableModel.setRowCount(0);
        boolean isHistory = historyToggleBtn.isSelected();
        
        String timeCondition = isHistory ? "AND c.debut < NOW() " : "AND c.debut >= NOW() ";

        String sql = "SELECT p.nomPat, p.prenomPat, DATE(c.debut), c.debut, c.fin " +
                     "FROM rdv r " + "JOIN patient p ON r.matPat = p.matPat " +
                     "JOIN creneau c ON r.matCren = c.matCren " +
                     "WHERE (p.nomPat LIKE ? OR p.prenomPat LIKE ? OR DATE(c.debut) = ?) " +
                     timeCondition + "ORDER BY c.debut DESC";

        try (PreparedStatement pstmt = db.prepareStatement(sql)) {
            String searchTerm = "%" + query + "%";
            pstmt.setString(1, searchTerm);
            pstmt.setString(2, searchTerm);

            try {
                LocalDate.parse(query); 
                pstmt.setString(3, query);
            } catch (Exception e) {
                pstmt.setString(3, ""); 
            }

            ResultSet rs = pstmt.executeQuery();
            remplirTAB(rs);

            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Aucun RDV trouvé", "Recherche", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erreur lors de la recherche: " + e.getMessage(), 
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void remplirTAB(ResultSet rs) throws SQLException {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        while (rs.next()) {
            String nomPat = rs.getString("nomPat");
            String prenomPat = rs.getString("prenomPat");
            Timestamp debut = rs.getTimestamp("debut");
            Timestamp fin = rs.getTimestamp("fin");

            String dateFormatted = "";
            String heureDebutFormatted = "";
            String heureFinFormatted = "";

            if (debut != null && fin != null) {
                LocalDateTime dateTimeDebut = debut.toLocalDateTime();
                LocalDateTime dateTimeFin = fin.toLocalDateTime();
                
                dateFormatted = dateTimeDebut.toLocalDate().format(dateFormatter);
                heureDebutFormatted = dateTimeDebut.toLocalTime().format(timeFormatter);
                heureFinFormatted = dateTimeFin.toLocalTime().format(timeFormatter);
            }

            Object[] row = {
                nomPat, prenomPat, dateFormatted, heureDebutFormatted, heureFinFormatted
            };
            tableModel.addRow(row);
        }
    }

    private void ajouterRDV() {
        JComboBox<String> patientCombo = new JComboBox<>();
        chargerPatientsCombo(patientCombo);
        
        JTextField dateField = new JTextField(15);
        LocalDateTime now = LocalDateTime.now().plusHours(1).withMinute(0).withSecond(0);
        dateField.setText(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        dateField.setToolTipText("Format: AAAA-MM-JJ HH:MM (ex: 2025-11-21 14:30)");
        
        Object[] fields = {
            "Patient :", patientCombo,
            "Date du RDV (AAAA-MM-JJ HH:MM) :", dateField };
        
        int result = JOptionPane.showConfirmDialog(this, fields, "Ajouter un RDV", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                String patientInfo = (String) patientCombo.getSelectedItem();
                if (patientInfo == null || patientInfo.equals("Aucun patient")) {
                    JOptionPane.showMessageDialog(this, "Veuillez sélectionner un patient.", 
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
               
                String dateStr = dateField.getText().trim();
                if (dateStr.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Veuillez saisir une date.", "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                LocalDateTime dateDemandee;
                try {
                    dateDemandee = LocalDateTime.parse(dateStr, formatter);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Format de date invalide.\nUtilisez: AAAA-MM-JJ HH:MM (ex: 2025-05-20 14:00)", 
                        "Erreur de format", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (dateDemandee.isBefore(LocalDateTime.now())) {
                    JOptionPane.showMessageDialog(this, "Impossible de créer un RDV dans le passé.", "Date invalide", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int matPat = Integer.parseInt(patientInfo.split(" - ")[0]);
                String sqlDate = dateDemandee.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                String sqlCheck = "SELECT matCren FROM creneau WHERE debut = ? AND etatCren = 'disponible'";
                
                int matCrenTrouve = -1;

                try (PreparedStatement pstmtCheck = db.prepareStatement(sqlCheck)) {
                    pstmtCheck.setString(1, sqlDate);
                    ResultSet rs = pstmtCheck.executeQuery();
                    
                    if (rs.next()) {
                        matCrenTrouve = rs.getInt("matCren");
                    }
                }

                if (matCrenTrouve != -1) {
                    insererRDV(matPat, matCrenTrouve);
                } else {
                    String sqlVerifPris = "SELECT matCren FROM creneau WHERE debut = ?";
                    boolean estPris = false;
                    try (PreparedStatement pstmtPris = db.prepareStatement(sqlVerifPris)) {
                        pstmtPris.setString(1, sqlDate);
                        if (pstmtPris.executeQuery().next()) estPris = true;
                    }

                    if (estPris) {
                         JOptionPane.showMessageDialog(this, 
                            "Ce créneau existe mais est déjà RÉSERVÉ.", 
                            "Indisponible", JOptionPane.WARNING_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, 
                            "Aucun créneau n'est configuré pour cette date.\nVeuillez saisir une date correspondant à un créneau existant.", 
                            "Créneau introuvable", JOptionPane.ERROR_MESSAGE);
                    }
                }

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erreur SQL : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

    private void insererRDV(int matPat, int matCren) throws SQLException {
        String sqlInsert = "INSERT INTO rdv (matPat, matCren) VALUES (?, ?)";
        try (PreparedStatement pstmt = db.prepareStatement(sqlInsert)) {
            pstmt.setInt(1, matPat);
            pstmt.setInt(2, matCren);
            
            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                String updateCreneau = "UPDATE creneau SET etatCren = 'réservé' WHERE matCren = ?";
                try (PreparedStatement pstmtUpd = db.prepareStatement(updateCreneau)) {
                    pstmtUpd.setInt(1, matCren);
                    pstmtUpd.executeUpdate();
                }
                
                JOptionPane.showMessageDialog(this, "RDV ajouté avec succès!", "Succès", JOptionPane.INFORMATION_MESSAGE);
                actualiser(); 
            }
        }
    }
    
    private void annulerRDV() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un RDV dans le tableau", 
                "Information", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nom = (String) tableModel.getValueAt(row, 0);
        String prenom = (String) tableModel.getValueAt(row, 1);
        String date = (String) tableModel.getValueAt(row, 2);
        String heureDebut = (String) tableModel.getValueAt(row, 3); 

        try {
            DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter sqlFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate localDate = LocalDate.parse(date, displayFormatter);
            String dateSQL = localDate.format(sqlFormatter);

            String sqlFindRDV = "SELECT c.debut, r.matCren FROM rdv r " +
                               "JOIN patient p ON r.matPat = p.matPat " +
                               "JOIN creneau c ON r.matCren = c.matCren " +
                               "WHERE p.nomPat = ? AND p.prenomPat = ? AND DATE(c.debut) = ? " +
                               "AND TIME_FORMAT(c.debut, '%H:%i') = ?"; 

            try (PreparedStatement pstmt = db.prepareStatement(sqlFindRDV)) {
                pstmt.setString(1, nom);
                pstmt.setString(2, prenom);
                pstmt.setString(3, dateSQL);
                pstmt.setString(4, heureDebut);

                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    int matCren = rs.getInt("matCren");
                    Timestamp debut = rs.getTimestamp("debut");

                    LocalDateTime dateTimeRDV = debut.toLocalDateTime();
                    if (dateTimeRDV.isAfter(LocalDateTime.now())) {
                         LocalDateTime limitAnnulation = dateTimeRDV.minusHours(48);
                         if (LocalDateTime.now().isAfter(limitAnnulation)) {
                             JOptionPane.showMessageDialog(this, 
                                 "Impossible d'annuler ce RDV.\nL'annulation doit se faire au moins 48h avant.",
                                 "Délai dépassé", JOptionPane.WARNING_MESSAGE);
                             return;
                         }
                    } else {}

                    int confirm = JOptionPane.showConfirmDialog(this, 
                        "Voulez-vous supprimer ce RDV pour " + prenom + " " + nom + "?","Confirmation", JOptionPane.YES_NO_OPTION);

                    if (confirm == JOptionPane.YES_OPTION) {
                        String sqlDelete = "DELETE FROM rdv WHERE matCren = ?";
                        try (PreparedStatement pstmtDel = db.prepareStatement(sqlDelete)) {
                            pstmtDel.setInt(1, matCren);
                            int rows = pstmtDel.executeUpdate();

                            if (rows > 0) {
                                String updateCreneau = "UPDATE creneau SET etatCren = 'disponible' WHERE matCren = ?";
                                try (PreparedStatement pstmtUpd = db.prepareStatement(updateCreneau)) {
                                    pstmtUpd.setInt(1, matCren);
                                    pstmtUpd.executeUpdate();
                                }
                                JOptionPane.showMessageDialog(this, "RDV supprimé.", "Succès", JOptionPane.INFORMATION_MESSAGE);
                                actualiser();
                            }
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Impossible de retrouver l'ID du RDV.", "Erreur technique", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void chargerPatientsCombo(JComboBox<String> combo) {
        combo.removeAllItems();
        String sql = "SELECT matPat, nomPat, prenomPat FROM patient ORDER BY nomPat ASC";
        try (PreparedStatement pstmt = db.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                combo.addItem(rs.getInt("matPat") + " - " + rs.getString("nomPat") + " " + rs.getString("prenomPat"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        if (combo.getItemCount() == 0) combo.addItem("Aucun patient");
    }

    private void chargerCreneauxCombo(JComboBox<String> combo) {
        combo.removeAllItems();
        String sql = "SELECT matCren, debut, fin FROM creneau " +
                     "WHERE etatCren = 'disponible' AND debut > NOW() " +"ORDER BY debut ASC";
        
        try (PreparedStatement pstmt = db.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            while (rs.next()) {
                LocalDateTime start = rs.getTimestamp("debut").toLocalDateTime();
                LocalDateTime end = rs.getTimestamp("fin").toLocalDateTime();
                combo.addItem(rs.getInt("matCren") + " - " + start.format(fmt) + " à " + end.toLocalTime());
            }
        } catch (SQLException e) { e.printStackTrace(); }
        if (combo.getItemCount() == 0) combo.addItem("Aucun créneau");
    }

    private void retourner() {
        if (parentFrame != null) parentFrame.setVisible(true);
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