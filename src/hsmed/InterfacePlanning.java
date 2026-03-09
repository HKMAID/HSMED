
package hsmed;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.time.*;
import java.time.format.TextStyle;
import java.util.Locale;

public class InterfacePlanning extends JFrame {
    private final DatabaseManager db;
    private final JFrame parentFrame;
    private final int matricule;
    private JTable tableCreneaux;
    private DefaultTableModel tableModel;
    private JComboBox<String> monthComboBox;
    private JComboBox<Integer> dayComboBox;
    private JComboBox<Integer> yearComboBox;
    private LocalDate dateSelectionnee;
    private boolean userSearch = false;
    
    public InterfacePlanning(int matricule, DatabaseManager db, JFrame parentFrame) {
        this.matricule = matricule;
        this.db = db;
        this.parentFrame = parentFrame;
        initComponents();
    }
    
    private void initComponents() {
        setTitle("Plannings - HSMED");
        setSize(900, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBackground(new Color(245, 247, 250));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        JPanel tablePanel = createTablePanel();
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        
        JPanel searchPanel = createSearchPanel();
        mainPanel.add(searchPanel, BorderLayout.WEST);
        
        add(mainPanel);
        initializeWithCurrentDate();
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(245, 247, 250));
        
        JLabel titleLabel = new JLabel("Liste des créneaux", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(41, 128, 185));
        
        JButton retourBtn = creerBouton("← Retour", new Color(149, 165, 166), Color.WHITE);
        retourBtn.setPreferredSize(new Dimension(120, 40));
        retourBtn.addActionListener(e -> retourner());
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        JPanel retourPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        retourPanel.setBackground(new Color(245, 247, 250));
        retourPanel.add(retourBtn);
        headerPanel.add(retourPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
        searchPanel.setBackground(new Color(245, 247, 250));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Rechercher par date"));
        searchPanel.setMaximumSize(new Dimension(220, 300));
        
        JLabel yearLabel = new JLabel("Année :");
        yearLabel.setFont(new Font("Arial", Font.BOLD, 12));
        yearLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchPanel.add(yearLabel);
        
        yearComboBox = new JComboBox<>(getYears());
        yearComboBox.setMaximumSize(new Dimension(200, 30));
        yearComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        yearComboBox.addActionListener(e -> {
            userSearch = false;
            updateMonthComboBox();
        });
        searchPanel.add(yearComboBox);     
        searchPanel.add(Box.createVerticalStrut(10));
        
        JLabel monthLabel = new JLabel("Mois :");
        monthLabel.setFont(new Font("Arial", Font.BOLD, 12));
        monthLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchPanel.add(monthLabel);
        
        monthComboBox = new JComboBox<>();
        monthComboBox.setMaximumSize(new Dimension(200, 30));
        monthComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        monthComboBox.addActionListener(e -> {
            userSearch = false;
            updateDayComboBox();
        });
        searchPanel.add(monthComboBox);     
        searchPanel.add(Box.createVerticalStrut(10));
        
        JLabel dayLabel = new JLabel("Jour :");
        dayLabel.setFont(new Font("Arial", Font.BOLD, 12));
        dayLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchPanel.add(dayLabel);
        
        dayComboBox = new JComboBox<>();
        dayComboBox.setMaximumSize(new Dimension(200, 30));
        dayComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        dayComboBox.addActionListener(e -> {
            userSearch = true;
            chargerCreneaux();
        });
        searchPanel.add(dayComboBox);
        searchPanel.add(Box.createVerticalStrut(15));
        
        JButton recherchBtn = creerBouton("Rechercher", new Color(52, 152, 219), Color.WHITE);
        recherchBtn.setMaximumSize(new Dimension(180, 35));
        recherchBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        recherchBtn.addActionListener(e -> {
            userSearch = true;
            chargerCreneaux();
        });
        searchPanel.add(recherchBtn);
        searchPanel.add(Box.createVerticalGlue());
        
        updateMonthComboBox();
        return searchPanel;
    }
    
    private Integer[] getYears() {
        Integer[] years = new Integer[11];
        for (int i = 0; i < 11; i++) {
            years[i] = 2025 + i;
        }
        return years;
    }
    
    private void updateMonthComboBox() {
    monthComboBox.removeAllItems();
    Integer selectedYear = (Integer) yearComboBox.getSelectedItem();
    
    if (selectedYear != null) {
        for (int month = 1; month <= 12; month++) {
            String monthName = Month.of(month)
                .getDisplayName(TextStyle.FULL, Locale.FRENCH).toUpperCase();
            monthComboBox.addItem(monthName);
        }
    }
    updateDayComboBox();
}

private void updateDayComboBox() {
    dayComboBox.removeAllItems();
    String selectedMonth = (String) monthComboBox.getSelectedItem();
    Integer selectedYear = (Integer) yearComboBox.getSelectedItem();
    
    if (selectedMonth != null && selectedYear != null) {
        try {
            String monthName = selectedMonth.split(" ")[0];
            
            int month = getMonthNumberFromFrenchName(monthName);
            
            YearMonth yearMonth = YearMonth.of(selectedYear, month);
            int daysInMonth = yearMonth.lengthOfMonth();
            
            for (int i = 1; i <= daysInMonth; i++) {
                dayComboBox.addItem(i);
            }
            
            if (dayComboBox.getItemCount() > 0) {
                dayComboBox.setSelectedIndex(0);
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la mise à jour des jours: " + e.getMessage());
        }
    }
}

private void initializeWithCurrentDate() {
    LocalDate aujourdhui = LocalDate.now();  
    yearComboBox.setSelectedItem(aujourdhui.getYear());
  
    updateMonthComboBox();
    String currentMonthName = aujourdhui.getMonth()
        .getDisplayName(TextStyle.FULL, Locale.FRENCH).toUpperCase();
    monthComboBox.setSelectedItem(currentMonthName);
    
    updateDayComboBox();
    dayComboBox.setSelectedItem(aujourdhui.getDayOfMonth());
    
    chargerCreneaux();
    }
    
    private int getMonthNumberFromFrenchName(String frenchMonthName) {
        String[] frenchMonths = {
            "JANVIER", "FÉVRIER", "MARS", "AVRIL", "MAI", "JUIN",
            "JUILLET", "AOÛT", "SEPTEMBRE", "OCTOBRE", "NOVEMBRE", "DÉCEMBRE"
        };
        
        for (int i = 0; i < frenchMonths.length; i++) {
            if (frenchMonths[i].equals(frenchMonthName.toUpperCase())) {
                return i + 1;
            }
        }
        throw new IllegalArgumentException("Mois français non reconnu: " + frenchMonthName);
    }
    
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(new Color(245, 247, 250));
        
        String[] columnNames = {"Début créneau", "Fin créneau", "État créneau"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tableCreneaux = new JTable(tableModel);
        tableCreneaux.setFont(new Font("Arial", Font.PLAIN, 12));
        tableCreneaux.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tableCreneaux.getTableHeader().setBackground(new Color(41, 128, 185));
        tableCreneaux.getTableHeader().setForeground(Color.WHITE);
        tableCreneaux.setRowHeight(30);
        tableCreneaux.setSelectionBackground(new Color(52, 152, 219));
        
        JScrollPane scrollPane = new JScrollPane(tableCreneaux);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }
    
   private boolean initialisation = true;
   
   private void chargerCreneaux() {
    tableModel.setRowCount(0);
    
    String selectedMonth = (String) monthComboBox.getSelectedItem();
    Integer selectedDay = (Integer) dayComboBox.getSelectedItem();
    Integer selectedYear = (Integer) yearComboBox.getSelectedItem();
    
    if (selectedMonth == null || selectedDay == null || selectedYear == null) {
        return;
    }
    
    try {
        String monthName = selectedMonth.split(" ")[0];
        int month = getMonthNumberFromFrenchName(monthName);
        
        dateSelectionnee = LocalDate.of(selectedYear, month, selectedDay);
        
        String sql = "SELECT debut, fin, etatCren FROM creneau " + "WHERE DATE(debut) = ? " +
                     "ORDER BY DATE(debut) ASC, debut ASC";
        
        try (PreparedStatement pstmt = db.prepareStatement(sql)) {
            pstmt.setString(1, dateSelectionnee.toString());
            
            ResultSet rs = pstmt.executeQuery();
            
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                Timestamp debutTimestamp = rs.getTimestamp("debut");
                Timestamp finTimestamp = rs.getTimestamp("fin");
                
                LocalTime heureDebut = debutTimestamp.toLocalDateTime().toLocalTime();
                LocalTime heureFin = finTimestamp.toLocalDateTime().toLocalTime();
                
                String etatCreneau = rs.getString("etatCren");
                
                tableModel.addRow(new Object[]{
                    heureDebut.toString(), 
                    heureFin.toString(), 
                    etatCreneau
                });
            }
            
            if (!hasData && InterfaceMedecin.affichée == true) {
        JOptionPane.showMessageDialog(this, "Aucun créneau défini pour le " + selectedDay + " " + monthName.toLowerCase() + " " + selectedYear + "!", 
            "Information", JOptionPane.INFORMATION_MESSAGE);
    }
        }
    } catch (SQLException e) {
        System.out.println("Erreur lors du chargement des créneaux: " + e.getMessage());
        e.printStackTrace();
        JOptionPane.showMessageDialog(this, "Erreur lors du chargement des créneaux!", "Erreur", JOptionPane.ERROR_MESSAGE);
    } catch (Exception e) {
        System.out.println("Erreur de date: " + e.getMessage());
        JOptionPane.showMessageDialog(this, "Erreur de sélection de date!", "Erreur", JOptionPane.ERROR_MESSAGE);
    }
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
    
    private void retourner() {
        parentFrame.setVisible(true);
        this.dispose();
        InterfaceMedecin.affichée = false;
    }
}