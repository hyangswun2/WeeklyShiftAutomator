import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.text.SimpleDateFormat;
import net.sourceforge.jdatepicker.impl.JDatePanelImpl;
import net.sourceforge.jdatepicker.impl.JDatePickerImpl;
import net.sourceforge.jdatepicker.impl.UtilDateModel;

public class Calendar extends JFrame implements ActionListener {
    private JDatePickerImpl datePicker;
    public String sDate;
    public String eDate;
    public Calendar() {
        super("WeeklyShiftAutomator");
        setLayout(new FlowLayout());
        add(new JLabel("Select Day : "));

        UtilDateModel model = new UtilDateModel();
        JDatePanelImpl datePanel = new JDatePanelImpl(model);

        datePicker = new JDatePickerImpl(datePanel);
        add(datePicker);

        JButton button = new JButton("Confirm");
        button.addActionListener(this);
        add(button);
        setSize(400, 300);
        setVisible(true);

    }

    public static void main(String[] args) {
        new Calendar();
    }
    @Override
    public void actionPerformed(ActionEvent event) {
        Date selectedDate = (Date) datePicker.getModel().getValue();
        SimpleDateFormat Date = new SimpleDateFormat("MM-dd");
        long oneWeekInMillis = 6 * 24 * 60 * 60 * 1000L;
        Date oneWeekLaterDate = new Date(selectedDate.getTime() + oneWeekInMillis);

        sDate = Date.format(selectedDate);
        eDate = Date.format(oneWeekLaterDate);
        JOptionPane.showMessageDialog(this, "selected : " + sDate + " to " + eDate);
    }

}