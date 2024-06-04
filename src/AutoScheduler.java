import javax.swing.*;
import java.awt.*;

public class AutoScheduler extends JFrame {
    private CardLayout mainLayout;
    private JPanel mainPanel;
    public static Timetable timetable = new Timetable();

    public AutoScheduler() {
        setTitle("AUTO Scheduler");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        mainLayout = new CardLayout();
        mainPanel = new JPanel(mainLayout);

        mainPanel.add(PanelFactory.createStartPanel(mainLayout, mainPanel), "StartPanel");
        mainPanel.add(PanelFactory.createImportPresetPanel(mainLayout, mainPanel), "ImportPresetPanel");
        mainPanel.add(PanelFactory.createSetPeriodPanel(mainLayout, mainPanel), "SetPeriodPanel");
        mainPanel.add(PanelFactory.createManageMemberPanel(mainLayout, mainPanel), "ManageMemberPanel");
        mainPanel.add(PanelFactory.createWorkTimetablePanel(mainLayout, mainPanel), "WorkTimetablePanel");

        getContentPane().add(mainPanel);

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AutoScheduler::new);
    }
}
