import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

public class ClientGUI extends JFrame {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    private JTextField txtNumberOfStudents;
    private JPanel marksPanel;
    private JTextField txtNumberOfSeats;
    private JButton btnSubmit;
    private JLabel lblCutoffMark;
    private JTable tableResults;
    private DefaultTableModel tableModel;
    private List<StudentMarksPanel> studentMarksPanels;

    public ClientGUI() {
        setTitle("Cutoff Mark Calculator");
        setSize(800, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        studentMarksPanels = new ArrayList<>();

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        add(mainPanel);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblNumStudents = new JLabel("Number of Students:");
        txtNumberOfStudents = new JTextField(10);
        JButton btnSetStudents = new JButton("Set");
        topPanel.add(lblNumStudents);
        topPanel.add(txtNumberOfStudents);
        topPanel.add(btnSetStudents);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        // Marks Panel
        marksPanel = new JPanel();
        marksPanel.setLayout(new BoxLayout(marksPanel, BoxLayout.Y_AXIS));
        marksPanel.setBorder(BorderFactory.createTitledBorder("Enter Marks (Out of 100 each)"));
        JScrollPane scrollMarks = new JScrollPane(marksPanel);
        scrollMarks.setPreferredSize(new Dimension(750, 300));
        centerPanel.add(scrollMarks);

        // Number of Seats
        JPanel panelSeats = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblSeats = new JLabel("Number of Seats:");
        txtNumberOfSeats = new JTextField(10);
        panelSeats.add(lblSeats);
        panelSeats.add(txtNumberOfSeats);
        centerPanel.add(panelSeats);

        // Submit Button
        btnSubmit = new JButton("Calculate Cutoff Mark");
        centerPanel.add(btnSubmit);

        lblCutoffMark = new JLabel("Cutoff Mark: ");
        lblCutoffMark.setFont(new Font("Arial", Font.BOLD, 16));
        centerPanel.add(lblCutoffMark);

        String[] columnNames = { "Rank", "Student No.", "Mathematics", "Physics", "Chemistry", "Total Marks",
                "Status" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableResults = new JTable(tableModel);
        JScrollPane scrollTable = new JScrollPane(tableResults);
        scrollTable.setPreferredSize(new Dimension(750, 300));
        centerPanel.add(scrollTable);

        btnSetStudents.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setNumberOfStudents();
            }
        });

        btnSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calculateCutoffMark();
            }
        });
    }

    private void setNumberOfStudents() {
        String input = txtNumberOfStudents.getText().trim();
        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the number of students.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int numStudents;
        try {
            numStudents = Integer.parseInt(input);
            if (numStudents <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number of students. Please enter a positive integer.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        marksPanel.removeAll();
        studentMarksPanels.clear();
        tableModel.setRowCount(0);
        lblCutoffMark.setText("Cutoff Mark: ");

        for (int i = 1; i <= numStudents; i++) {
            StudentMarksPanel smp = new StudentMarksPanel(i);
            studentMarksPanels.add(smp);
            marksPanel.add(smp);
        }

        marksPanel.revalidate();
        marksPanel.repaint();
    }

    private void calculateCutoffMark() {
        String numStudentsStr = txtNumberOfStudents.getText().trim();
        String numSeatsStr = txtNumberOfSeats.getText().trim();

        if (numStudentsStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the number of students.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (numSeatsStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the number of seats.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int numStudents;
        int numSeats;
        try {
            numStudents = Integer.parseInt(numStudentsStr);
            if (numStudents <= 0)
                throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number of students. Please enter a positive integer.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            numSeats = Integer.parseInt(numSeatsStr);
            if (numSeats <= 0)
                throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid number of seats. Please enter a positive integer.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (studentMarksPanels.size() != numStudents) {
            JOptionPane.showMessageDialog(this, "Number of marks panels does not match number of students.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<StudentData> studentsData = new ArrayList<>();
        List<Float> totalMarks = new ArrayList<>();

        for (int i = 0; i < studentMarksPanels.size(); i++) {
            StudentMarksPanel smp = studentMarksPanels.get(i);
            try {
                int maths = smp.getMathsMarks();
                int physics = smp.getPhysicsMarks();
                int chemistry = smp.getChemistryMarks();

                if (maths < 0 || maths > 100 || physics < 0 || physics > 100 || chemistry < 0 || chemistry > 100) {
                    JOptionPane.showMessageDialog(this,
                            "Invalid marks for Student " + (i + 1) + ". Each mark should be between 0 and 100.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                float total = maths + (physics / 2.0f) + (chemistry / 2.0f);
                if (total > 200.0f) {
                    JOptionPane.showMessageDialog(this, "Total marks for Student " + (i + 1) + " exceed 200.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                StudentData sd = new StudentData(i + 1, maths, physics, chemistry, total);
                studentsData.add(sd);
                totalMarks.add(total);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Invalid marks for Student " + (i + 1) + ". Please enter integers between 0 and 100.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        if (numSeats > numStudents) {
            JOptionPane.showMessageDialog(this, "Number of seats cannot exceed number of students.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                DataInputStream dis = new DataInputStream(socket.getInputStream())) {

            dos.writeInt(numStudents);

            for (StudentData sd : studentsData) {
                dos.writeInt(sd.getMathsMarks());
                dos.writeInt(sd.getPhysicsMarks());
                dos.writeInt(sd.getChemistryMarks());
            }

            dos.writeInt(numSeats);
            dos.flush();

            float cutoffMark = dis.readFloat();
            lblCutoffMark.setText(String.format("Cutoff Mark: %.2f", cutoffMark));

            // Sorting students based on total marks (descending order)
            Collections.sort(studentsData, new Comparator<StudentData>() {
                @Override
                public int compare(StudentData sd1, StudentData sd2) {
                    return Float.compare(sd2.getTotalMarks(), sd1.getTotalMarks());
                }
            });

            // Clear the table and add sorted data with ranks
            tableModel.setRowCount(0);
            for (int rank = 0; rank < studentsData.size(); rank++) {
                StudentData sd = studentsData.get(rank);
                String status = sd.getTotalMarks() >= cutoffMark ? "Passed" : "Not Passed";
                Object[] row = { rank + 1, "22CSR" + String.format("%03d", sd.getStudentNumber()), sd.getMathsMarks(),
                        sd.getPhysicsMarks(), sd.getChemistryMarks(), sd.getTotalMarks(), status };
                tableModel.addRow(row);
            }

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error communicating with the server: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientGUI().setVisible(true);
            }
        });
    }

    class StudentMarksPanel extends JPanel {
        private JTextField txtMaths;
        private JTextField txtPhysics;
        private JTextField txtChemistry;

        public StudentMarksPanel(int studentNumber) {
            setLayout(new FlowLayout(FlowLayout.LEFT));

            JLabel lblStudentNo = new JLabel("Student " + studentNumber + ": ");
            txtMaths = new JTextField(5);
            txtPhysics = new JTextField(5);
            txtChemistry = new JTextField(5);

            add(lblStudentNo);
            add(new JLabel("Maths:"));
            add(txtMaths);
            add(new JLabel("Physics:"));
            add(txtPhysics);
            add(new JLabel("Chemistry:"));
            add(txtChemistry);
        }

        public int getMathsMarks() {
            return Integer.parseInt(txtMaths.getText().trim());
        }

        public int getPhysicsMarks() {
            return Integer.parseInt(txtPhysics.getText().trim());
        }

        public int getChemistryMarks() {
            return Integer.parseInt(txtChemistry.getText().trim());
        }
    }

    class StudentData {
        private int studentNumber;
        private int mathsMarks;
        private int physicsMarks;
        private int chemistryMarks;
        private float totalMarks;

        public StudentData(int studentNumber, int mathsMarks, int physicsMarks, int chemistryMarks, float totalMarks) {
            this.studentNumber = studentNumber;
            this.mathsMarks = mathsMarks;
            this.physicsMarks = physicsMarks;
            this.chemistryMarks = chemistryMarks;
            this.totalMarks = totalMarks;
        }

        public int getStudentNumber() {
            return studentNumber;
        }

        public int getMathsMarks() {
            return mathsMarks;
        }

        public int getPhysicsMarks() {
            return physicsMarks;
        }

        public int getChemistryMarks() {
            return chemistryMarks;
        }

        public float getTotalMarks() {
            return totalMarks;
        }
    }
}
