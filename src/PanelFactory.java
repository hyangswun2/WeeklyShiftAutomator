import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;
import java.time.format.DateTimeFormatter;
import java.util.List;


import net.sourceforge.jdatepicker.impl.JDatePanelImpl;
import net.sourceforge.jdatepicker.impl.JDatePickerImpl;
import net.sourceforge.jdatepicker.impl.UtilDateModel;


public class PanelFactory {
    private static JDatePickerImpl datePicker;
    public static LocalDateTime sDate;
    private static Member currentMember;
    private static List<Member> members = new ArrayList<>();
    private static JPanel membersPanel;
    private static ButtonGroup group;
    private static boolean isFileParsedSuccessfully = false;
    public static JPanel createStartPanel(CardLayout layout, JPanel panel) {
        JPanel startPanel = new JPanel();
        startPanel.setLayout(null);
        startPanel.setBackground(Color.WHITE);

        JButton importPresetButton = new JButton("Import Preset");
        importPresetButton.setBounds(200, 150, 400, 100);
        importPresetButton.setFont(new Font("Arial", Font.BOLD, 30));
        importPresetButton.setBackground(Color.WHITE);
        importPresetButton.setBorder(BorderFactory.createLineBorder(Color.decode("#000080"), 2));
        importPresetButton.addActionListener(e -> layout.show(panel, "ImportPresetPanel"));

        JButton startNewButton = new JButton("Start New");
        startNewButton.setBounds(200, 300, 400, 100);
        startNewButton.setFont(new Font("Arial", Font.BOLD, 30));
        startNewButton.setBackground(Color.WHITE);
        startNewButton.setBorder(BorderFactory.createLineBorder(Color.decode("#000080"), 2));
        startNewButton.addActionListener(e -> layout.show(panel, "SetPeriodPanel"));

        startPanel.add(importPresetButton);
        startPanel.add(startNewButton);

        return startPanel;
    }

    public static JPanel createImportPresetPanel(CardLayout layout, JPanel panel) {
        JPanel importPresetPanel = new JPanel(new BorderLayout());
        importPresetPanel.setBackground(Color.WHITE);

        Color navyColor = Color.decode("#000080");

        JLabel titleLabel = new JLabel("Import Preset", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.BLACK);
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.add(titleLabel);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        importPresetPanel.add(titlePanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 10, 20, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField filePathField = new JTextField();
        filePathField.setEditable(false);
        filePathField.setBackground(Color.WHITE);
        filePathField.setOpaque(true);
        filePathField.setBorder(BorderFactory.createLineBorder(navyColor, 2));
        filePathField.setFont(new Font("Malgun Gothic", Font.PLAIN, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        centerPanel.add(filePathField, gbc);

        JButton browseButton = new JButton("Browse");
        browseButton.setFont(new Font("Arial", Font.PLAIN, 18));
        int buttonFontSize = browseButton.getFont().getSize();
        int buttonHeight = buttonFontSize * 2;
        browseButton.setPreferredSize(new Dimension(100, buttonHeight));
        browseButton.setBackground(Color.WHITE);
        browseButton.setBorder(BorderFactory.createLineBorder(navyColor, 2));
        gbc.gridx = 1;
        gbc.weightx = 0;
        centerPanel.add(browseButton, gbc);

        importPresetPanel.add(centerPanel, BorderLayout.CENTER);

        JButton setPeriodButton = new JButton("Set Period");
        setPeriodButton.setFont(new Font("Arial", Font.PLAIN, 18));
        setPeriodButton.setPreferredSize(new Dimension(120, 50));
        setPeriodButton.setBackground(Color.WHITE);
        setPeriodButton.setBorder(BorderFactory.createLineBorder(navyColor, 2));
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(setPeriodButton);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        importPresetPanel.add(bottomPanel, BorderLayout.SOUTH);

        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                    if (!filePath.endsWith(".txt")) {
                        JOptionPane.showMessageDialog(null, "Please select a valid .txt file.", "Invalid File", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    filePathField.setText(filePath);
                    isFileParsedSuccessfully = parsePresetFile(filePath);
                    if (!isFileParsedSuccessfully) {
                        JOptionPane.showMessageDialog(null, "File format is incorrect.", "Error", JOptionPane.ERROR_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, "Preset imported successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });

        setPeriodButton.addActionListener(e -> {
            if (!isFileParsedSuccessfully) {
                JOptionPane.showMessageDialog(null, "Please import a valid preset file first.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                layout.show(panel, "SetPeriodPanel");
            }
        });

        filePathField.setPreferredSize(new Dimension(0, buttonHeight));
        return importPresetPanel;
    }

    private static boolean parsePresetFile(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            members.clear();
            AutoScheduler.timetable = new Timetable();

            Member member = null;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("#") || line.isEmpty()) {
                    continue;
                }

                if (line.startsWith("name:")) {
                    if (member != null) {
                        members.add(member);
                    }
                    member = new Member("", "");
                    String[] parts = line.split(":", 2);
                    if (parts.length < 2) return false;
                    member.setName(parts[1].trim());
                } else if (line.startsWith("number:")) {
                    String[] parts = line.split(":", 2);
                    if (parts.length < 2) return false;
                    if (member != null) {
                        member.setNumber(parts[1].trim());
                    }
                } else if (line.startsWith("must_work_hours:")) {
                    String[] parts = line.split(":", 2);
                    if (parts.length < 2) return false;
                    if (member != null) {
                        member.setMWH(Double.parseDouble(parts[1].trim()));
                    }
                } else if (line.startsWith("must_include_times:")) {
                    if (member != null) {
                        String[] entries = line.split(":", 2);
                        if (entries.length < 2) return false;
                        String[] times = entries[1].trim().split(">, <");
                        for (String entry : times) {
                            entry = entry.replace("<", "").replace(">", "").trim();
                            String[] keyValue = entry.split(" : ");
                            if (keyValue.length < 2) return false;
                            LocalDateTime date = LocalDate.parse(keyValue[0].trim(), dateFormatter).atStartOfDay();
                            String[] timeValues = keyValue[1].trim().split(", ");
                            for (String time : timeValues) {
                                member.getMIT().computeIfAbsent(date, k -> new HashSet<>()).add(time);
                            }
                        }
                    }
                } else if (line.startsWith("must_exclude_times:")) {
                    if (member != null) {
                        String[] entries = line.split(":", 2);
                        if (entries.length < 2) return false;
                        String[] times = entries[1].trim().split(">, <");
                        for (String entry : times) {
                            entry = entry.replace("<", "").replace(">", "").trim();
                            String[] keyValue = entry.split(" : ");
                            if (keyValue.length < 2) return false;
                            LocalDateTime date = LocalDate.parse(keyValue[0].trim(), dateFormatter).atStartOfDay();
                            String[] timeValues = keyValue[1].trim().split(", ");
                            for (String time : timeValues) {
                                member.getMET().computeIfAbsent(date, k -> new HashSet<>()).add(time);
                            }
                        }
                    }
                } else if (line.startsWith("<")) {
                    line = line.replace("<", "").replace(">", "").trim();
                    String[] keyValue = line.split(" : ");
                    if (keyValue.length < 2) return false;
                    LocalDateTime date = LocalDate.parse(keyValue[0].trim(), dateFormatter).atStartOfDay();
                    String[] times = keyValue[1].trim().split(", ");
                    for (String time : times) {
                        AutoScheduler.timetable.days[date.getDayOfWeek().getValue() - 1]
                                .computeIfAbsent(date, k -> new HashSet<>()).add(time);
                    }
                }
            }

            if (member != null) {
                members.add(member);
            }

            return true;
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void updateMembersPanel() {
        membersPanel.removeAll();
        group.clearSelection();

        for (int i = 0; i < members.size(); i++) {
            Member member = members.get(i);
            JCheckBox checkBox = new JCheckBox(member.getName());
            checkBox.setActionCommand(String.valueOf(i));
            checkBox.setBackground(Color.WHITE);
            checkBox.setFont(new Font("Malgun Gothic", Font.PLAIN, 16));
            group.add(checkBox);
            membersPanel.add(checkBox);
        }

        membersPanel.revalidate();
        membersPanel.repaint();
    }

    public static JPanel createSetPeriodPanel(CardLayout layout, JPanel panel) {
        JPanel setPeriodPanel = new JPanel();
        setPeriodPanel.setLayout(new BorderLayout());
        setPeriodPanel.setBackground(Color.WHITE);

        Color navyColor = Color.decode("#000080");

        JLabel titleLabel = new JLabel("Select Day", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.BLACK);
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBackground(Color.WHITE);
        titlePanel.add(titleLabel);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setPeriodPanel.add(titlePanel, BorderLayout.NORTH);

        UtilDateModel model = new UtilDateModel();
        JDatePanelImpl datePanel = new JDatePanelImpl(model);

        datePicker = new JDatePickerImpl(datePanel);
        datePicker.setPreferredSize(new Dimension(300, 40));

        JTextField dayField = (JTextField) datePicker.getComponent(0);
        dayField.setBackground(Color.WHITE);
        dayField.setForeground(Color.BLACK);
        dayField.setFont(new Font("Arial", Font.PLAIN, 16));
        dayField.setBorder(BorderFactory.createLineBorder(navyColor, 2));
        dayField.setPreferredSize(new Dimension(270, 40));

        JButton pickerButton = (JButton) datePicker.getComponent(1);
        pickerButton.setFont(new Font("Arial", Font.PLAIN, 18));
        pickerButton.setBackground(Color.WHITE);
        pickerButton.setForeground(Color.BLACK);
        pickerButton.setBorder(BorderFactory.createLineBorder(navyColor, 2));
        pickerButton.setPreferredSize(new Dimension(30, 40));

        JPanel datePanelWrapper = new JPanel(new GridBagLayout());
        datePanelWrapper.setBackground(Color.WHITE);
        datePanelWrapper.add(datePicker);
        setPeriodPanel.add(datePanelWrapper, BorderLayout.CENTER);

        JButton confirmButton = new JButton("Set Timetable");
        confirmButton.setFont(new Font("Arial", Font.PLAIN, 18));
        confirmButton.setPreferredSize(new Dimension(150, 50));
        confirmButton.setBackground(Color.WHITE);
        confirmButton.setBorder(BorderFactory.createLineBorder(navyColor, 2));
        confirmButton.addActionListener(e -> {
            Date selectedDate = (Date) datePicker.getModel().getValue();
            if (selectedDate != null) {
                sDate = selectedDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy/MM/dd");

                String formattedSDate = sDate.format(formatter);
                String formattedEDate = sDate.plusWeeks(1).format(formatter);

                JOptionPane.showMessageDialog(panel, "Selected: " + formattedSDate + " to " + formattedEDate);

                for (int i = 0; i < 7; i++) {
                    AutoScheduler.timetable.days[i].put(sDate.plusDays(i), new HashSet<>());
                }

                panel.add(createSetTimetablePanel(layout, panel), "SetTimetablePanel");
                layout.show(panel, "SetTimetablePanel");
            } else {
                JOptionPane.showMessageDialog(panel, "No date selected.");
            }
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(confirmButton);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setPeriodPanel.add(bottomPanel, BorderLayout.SOUTH);

        return setPeriodPanel;
    }

    public static JPanel createSetTimetablePanel(CardLayout layout, JPanel panel) {
        JPanel setTimetablePanel = new JPanel(new BorderLayout());
        setTimetablePanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Set Timetable", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setTimetablePanel.add(titleLabel, BorderLayout.NORTH);

        JPanel timetablePanel = new JPanel(new GridBagLayout());
        timetablePanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel dateLabel = new JLabel("Date");
        dateLabel.setFont(new Font("Arial", Font.BOLD, 16));
        timetablePanel.add(dateLabel, gbc);

        gbc.gridx = 1;
        JLabel workingHoursLabel = new JLabel("Working hours");
        workingHoursLabel.setFont(new Font("Arial", Font.BOLD, 16));
        timetablePanel.add(workingHoursLabel, gbc);

        gbc.gridx = 2;
        JLabel startTimeEndTimeLabel = new JLabel("Start Time-End Time");
        startTimeEndTimeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        timetablePanel.add(startTimeEndTimeLabel, gbc);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy/MM/dd");

        Color navyColor = Color.decode("#000080");
        Color textFieldBackground = Color.decode("#FFFFFF");
        Color buttonBackground = Color.decode("#FFFFFF");

        Dimension textFieldDimension = new Dimension(150, 40);

        Pattern timePattern = Pattern.compile("^\\d{2}-\\d{2}$");

        for (int i = 0; i < 7; i++) {
            gbc.gridx = 0;
            gbc.gridy = i + 1;
            LocalDateTime date = sDate.plusDays(i);
            String formattedDate = date.format(formatter);
            JTextField dateField = new JTextField(formattedDate);
            dateField.setEditable(false);
            dateField.setFont(new Font("Arial", Font.PLAIN, 16));
            dateField.setHorizontalAlignment(JTextField.CENTER);
            dateField.setBackground(textFieldBackground);
            dateField.setBorder(BorderFactory.createLineBorder(navyColor, 2));
            dateField.setPreferredSize(textFieldDimension);
            timetablePanel.add(dateField, gbc);

            gbc.gridx = 1;
            JTextField workingHoursField = new JTextField(" // Add Working Hour to click Add button.");
            workingHoursField.setFont(new Font("Arial", Font.PLAIN, 16));
            workingHoursField.setBackground(textFieldBackground);
            workingHoursField.setBorder(BorderFactory.createLineBorder(navyColor, 2));
            workingHoursField.setPreferredSize(new Dimension(300, 40));
            timetablePanel.add(workingHoursField, gbc);

            gbc.gridx = 2;
            JTextField startEndTimeField = new JTextField(" // Write here");
            startEndTimeField.setFont(new Font("Arial", Font.PLAIN, 16));
            startEndTimeField.setBackground(textFieldBackground);
            startEndTimeField.setBorder(BorderFactory.createLineBorder(navyColor, 2));
            startEndTimeField.setPreferredSize(textFieldDimension);
            timetablePanel.add(startEndTimeField, gbc);

            gbc.gridx = 3;
            JButton addButton = new JButton("Add");
            addButton.setFont(new Font("Arial", Font.BOLD, 16));
            addButton.setBackground(buttonBackground);
            addButton.setBorder(BorderFactory.createLineBorder(navyColor, 2));
            addButton.setPreferredSize(new Dimension(80, 40));
            timetablePanel.add(addButton, gbc);

            // Add action listener to the button
            final int index = i;
            addButton.addActionListener(e -> {
                String startEndTime = startEndTimeField.getText();
                Matcher matcher = timePattern.matcher(startEndTime);
                if (matcher.matches()) {
                    AutoScheduler.timetable.days[index].get(sDate.plusDays(index)).add(startEndTime);
                    String currentText = workingHoursField.getText();
                    if (currentText.equals(" // Add Working Hour to click Add button.")) {
                        workingHoursField.setText(startEndTime);
                    } else {
                        workingHoursField.setText(String.join(", ", AutoScheduler.timetable.days[index].get(sDate.plusDays(index))));
                    }
                    startEndTimeField.setText("");
                } else {
                    JOptionPane.showMessageDialog(panel, "Invalid time format. Please enter time in HH-HH format.");
                }
            });
        }

        setTimetablePanel.add(timetablePanel, BorderLayout.CENTER);

        JButton manageMemberButton = new JButton("Manage Member");
        manageMemberButton.setFont(new Font("Arial", Font.PLAIN, 18));
        manageMemberButton.setPreferredSize(new Dimension(200, 50));
        manageMemberButton.setBackground(Color.WHITE);
        manageMemberButton.setBorder(BorderFactory.createLineBorder(Color.decode("#000080"), 2));
        manageMemberButton.addActionListener(e -> layout.show(panel, "ManageMemberPanel"));

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(manageMemberButton);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        setTimetablePanel.add(bottomPanel, BorderLayout.SOUTH);

        return setTimetablePanel;
    }

    public static JPanel createManageMemberPanel(CardLayout layout, JPanel panel) {
        JPanel manageMemberPanel = new JPanel(new BorderLayout());
        manageMemberPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Manage Member", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        manageMemberPanel.add(titleLabel, BorderLayout.NORTH);

        membersPanel = new JPanel();
        membersPanel.setLayout(new BoxLayout(membersPanel, BoxLayout.Y_AXIS));
        membersPanel.setBackground(Color.WHITE);
        membersPanel.setBorder(BorderFactory.createLineBorder(Color.decode("#000080"), 2));
        membersPanel.setPreferredSize(new Dimension(300, 400));

        // ButtonGroup for single selection of checkboxes
        group = new ButtonGroup();

        // Add members to the membersPanel
        updateMembersPanel();

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setBackground(Color.WHITE);

        JButton addMemberButton = new JButton("Add Member");
        addMemberButton.setFont(new Font("Arial", Font.PLAIN, 18));
        addMemberButton.setBackground(Color.WHITE);
        addMemberButton.setBorder(BorderFactory.createLineBorder(Color.decode("#000080"), 2));
        addMemberButton.setMaximumSize(new Dimension(200, 50));
        addMemberButton.addActionListener(e -> {
            currentMember = new Member("", "");
            panel.add(createManageMemberInfoPanel(layout, panel, true), "ManageMemberInfoPanel");
            layout.show(panel, "ManageMemberInfoPanel");
        });

        buttonsPanel.add(addMemberButton);
        buttonsPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JButton modifyMemberButton = new JButton("Modify Member");
        modifyMemberButton.setFont(new Font("Arial", Font.PLAIN, 18));
        modifyMemberButton.setBackground(Color.WHITE);
        modifyMemberButton.setBorder(BorderFactory.createLineBorder(Color.decode("#000080"), 2));
        modifyMemberButton.setMaximumSize(new Dimension(200, 50));
        modifyMemberButton.addActionListener(e -> {
            for (Component component : membersPanel.getComponents()) {
                if (component instanceof JCheckBox) {
                    JCheckBox checkBox = (JCheckBox) component;
                    if (checkBox.isSelected()) {
                        currentMember = members.get(Integer.parseInt(checkBox.getActionCommand()));
                        panel.add(createManageMemberInfoPanel(layout, panel, false), "ManageMemberInfoPanel");
                        layout.show(panel, "ManageMemberInfoPanel");
                        break;
                    }
                }
            }
        });

        buttonsPanel.add(modifyMemberButton);
        buttonsPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JButton deleteMemberButton = new JButton("Delete Member");
        deleteMemberButton.setFont(new Font("Arial", Font.PLAIN, 18));
        deleteMemberButton.setBackground(Color.WHITE);
        deleteMemberButton.setBorder(BorderFactory.createLineBorder(Color.decode("#000080"), 2));
        deleteMemberButton.setMaximumSize(new Dimension(200, 50));
        deleteMemberButton.addActionListener(e -> {
            for (Component component : membersPanel.getComponents()) {
                if (component instanceof JCheckBox) {
                    JCheckBox checkBox = (JCheckBox) component;
                    if (checkBox.isSelected()) {
                        members.remove(Integer.parseInt(checkBox.getActionCommand()));
                        updateMembersPanel();
                        break;
                    }
                }
            }
        });

        buttonsPanel.add(deleteMemberButton);

        manageMemberPanel.add(buttonsPanel, BorderLayout.CENTER);

        JButton createWorkTimetableButton = new JButton("Create Work Timetable");
        createWorkTimetableButton.setFont(new Font("Arial", Font.PLAIN, 18));
        createWorkTimetableButton.setBackground(Color.WHITE);
        createWorkTimetableButton.setBorder(BorderFactory.createLineBorder(Color.decode("#000080"), 2));
        createWorkTimetableButton.setPreferredSize(new Dimension(200, 50));
        createWorkTimetableButton.addActionListener(e -> layout.show(panel, "WorkTimetablePanel"));

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(createWorkTimetableButton);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        manageMemberPanel.add(bottomPanel, BorderLayout.SOUTH);
        manageMemberPanel.add(membersPanel, BorderLayout.WEST);

        return manageMemberPanel;
    }

    public static JPanel createManageMemberInfoPanel(CardLayout layout, JPanel panel, boolean isNewMember) {
        JPanel manageMemberInfoPanel = new JPanel(new BorderLayout());
        manageMemberInfoPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Manage Member Info", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        manageMemberInfoPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel memberPanel = new JPanel(new GridBagLayout());
        memberPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 멤버 이름 라벨 및 텍스트 필드 추가
        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        memberPanel.add(nameLabel, gbc);

        JTextField nameField = new JTextField(currentMember.getName());
        nameField.setFont(new Font("Malgun Gothic", Font.PLAIN, 16));
        nameField.setBackground(Color.WHITE);
        nameField.setBorder(BorderFactory.createLineBorder(Color.decode("#000080"), 2));
        nameField.setPreferredSize(new Dimension(300, 40));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        memberPanel.add(nameField, gbc);

        // 전화번호 라벨 및 텍스트 필드 추가
        JLabel numberLabel = new JLabel("Phone Number:");
        numberLabel.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        memberPanel.add(numberLabel, gbc);

        JTextField numberField = new JTextField(currentMember.getNumber());
        numberField.setFont(new Font("Arial", Font.PLAIN, 16));
        numberField.setBackground(Color.WHITE);
        numberField.setBorder(BorderFactory.createLineBorder(Color.decode("#000080"), 2));
        numberField.setPreferredSize(new Dimension(300, 40));
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        memberPanel.add(numberField, gbc);

        // 날짜, 포함 시간, 제외 시간 라벨 및 텍스트 필드 추가
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        JLabel dateLabel = new JLabel("Date");
        dateLabel.setFont(new Font("Arial", Font.BOLD, 16));
        memberPanel.add(dateLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JLabel MIT = new JLabel("Must Include Time");
        MIT.setFont(new Font("Arial", Font.BOLD, 16));
        memberPanel.add(MIT, gbc);

        gbc.gridx = 2;
        JLabel MET = new JLabel("Must Exclude Time");
        MET.setFont(new Font("Arial", Font.BOLD, 16));
        memberPanel.add(MET, gbc);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy/MM/dd");

        Color navyColor = Color.decode("#000080");
        Color textFieldBackground = Color.decode("#FFFFFF");

        Dimension textFieldDimension = new Dimension(300, 40);

        Pattern timePattern = Pattern.compile("^\\d{2}-\\d{2}$");

        Map<LocalDateTime, JTextField> mitFields = new HashMap<>();
        Map<LocalDateTime, JTextField> metFields = new HashMap<>();

        for (int i = 0; i < 7; i++) {
            gbc.gridx = 0;
            gbc.gridy = i + 3;
            gbc.weightx = 0.0;
            LocalDateTime date = sDate.plusDays(i);
            String formattedDate = date.format(formatter);
            JTextField dateField = new JTextField(formattedDate);
            dateField.setEditable(false);
            dateField.setFont(new Font("Arial", Font.PLAIN, 16));
            dateField.setHorizontalAlignment(JTextField.CENTER);
            dateField.setBackground(textFieldBackground);
            dateField.setBorder(BorderFactory.createLineBorder(navyColor, 2));
            dateField.setPreferredSize(new Dimension(100, 40));
            memberPanel.add(dateField, gbc);

            gbc.gridx = 1;
            gbc.weightx = 1.0;
            JTextField MITField = new JTextField();
            MITField.setFont(new Font("Arial", Font.PLAIN, 16));
            MITField.setBackground(textFieldBackground);
            MITField.setBorder(BorderFactory.createLineBorder(navyColor, 2));
            MITField.setPreferredSize(textFieldDimension);
            memberPanel.add(MITField, gbc);
            mitFields.put(date, MITField);

            gbc.gridx = 2;
            JTextField METField = new JTextField();
            METField.setFont(new Font("Arial", Font.PLAIN, 16));
            METField.setBackground(textFieldBackground);
            METField.setBorder(BorderFactory.createLineBorder(navyColor, 2));
            METField.setPreferredSize(textFieldDimension);
            memberPanel.add(METField, gbc);
            metFields.put(date, METField);
        }

        // Must Work Hours 라벨 및 텍스트 필드 추가
        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.weightx = 0.0;
        JLabel mwhLabel = new JLabel("Must Work Hours:");
        mwhLabel.setFont(new Font("Arial", Font.BOLD, 16));
        memberPanel.add(mwhLabel, gbc);

        gbc.gridx = 1;
        JTextField mwhField = new JTextField(String.valueOf(currentMember.getMWH()));
        mwhField.setFont(new Font("Arial", Font.PLAIN, 16));
        mwhField.setBackground(Color.WHITE);
        mwhField.setBorder(BorderFactory.createLineBorder(navyColor, 2));
        mwhField.setPreferredSize(new Dimension(100, 40));
        memberPanel.add(mwhField, gbc);

        manageMemberInfoPanel.add(memberPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);

        // Done 버튼
        JButton doneButton = new JButton("Done");
        doneButton.setFont(new Font("Arial", Font.PLAIN, 18));
        doneButton.setPreferredSize(new Dimension(150, 50));
        doneButton.setBackground(Color.WHITE);
        doneButton.setBorder(BorderFactory.createLineBorder(Color.decode("#000080"), 2));
        doneButton.addActionListener(e -> {
            currentMember.setName(nameField.getText());
            currentMember.setNumber(numberField.getText());
            currentMember.setMWH(Double.parseDouble(mwhField.getText()));

            // 필드 내용 저장
            for (Map.Entry<LocalDateTime, JTextField> entry : mitFields.entrySet()) {
                LocalDateTime date = entry.getKey();
                String mitValue = entry.getValue().getText();
                String metValue = metFields.get(date).getText();

                Set<String> mitSet = currentMember.getMIT().getOrDefault(date, new HashSet<>());
                Set<String> metSet = currentMember.getMET().getOrDefault(date, new HashSet<>());

                if (!mitValue.isEmpty()) {
                    String[] mitValues = mitValue.split(", ");
                    Collections.addAll(mitSet, mitValues);
                }
                if (!metValue.isEmpty()) {
                    String[] metValues = metValue.split(", ");
                    Collections.addAll(metSet, metValues);
                }

                currentMember.getMIT().put(date, mitSet);
                currentMember.getMET().put(date, metSet);
            }

            if (isNewMember) {
                members.add(currentMember);
            }

            updateMembersPanel();
            layout.show(panel, "ManageMemberPanel");
        });
        buttonPanel.add(doneButton);

        // Exit 버튼
        JButton exitButton = new JButton("Exit");
        exitButton.setFont(new Font("Arial", Font.PLAIN, 18));
        exitButton.setPreferredSize(new Dimension(150, 50));
        exitButton.setBackground(Color.WHITE);
        exitButton.setBorder(BorderFactory.createLineBorder(Color.decode("#000080"), 2));
        exitButton.addActionListener(e -> layout.show(panel, "ManageMemberPanel"));
        buttonPanel.add(exitButton);

        manageMemberInfoPanel.add(buttonPanel, BorderLayout.SOUTH);

        return manageMemberInfoPanel;
    }

    public static JPanel createWorkTimetablePanel(CardLayout layout, JPanel panel) {
        JPanel workTimetablePanel = new JPanel(new BorderLayout());
        workTimetablePanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Work Timetable", SwingConstants.LEFT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        workTimetablePanel.add(titleLabel, BorderLayout.NORTH);

        JPanel timetablePanel = new JPanel(new GridBagLayout());
        timetablePanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel dateLabel = new JLabel("Date");
        dateLabel.setFont(new Font("Arial", Font.BOLD, 16));
        timetablePanel.add(dateLabel, gbc);

        gbc.gridx = 1;
        JLabel memberLabel = new JLabel("Member");
        memberLabel.setFont(new Font("Arial", Font.BOLD, 16));
        timetablePanel.add(memberLabel, gbc);

        gbc.gridx = 2;
        JLabel hoursLabel = new JLabel("Working Hours");
        hoursLabel.setFont(new Font("Arial", Font.BOLD, 16));
        timetablePanel.add(hoursLabel, gbc);

        workTimetablePanel.add(timetablePanel, BorderLayout.CENTER);

        JButton generateButton = new JButton("Generate Timetable");
        generateButton.setFont(new Font("Arial", Font.PLAIN, 18));
        generateButton.setPreferredSize(new Dimension(200, 50));
        generateButton.setBackground(Color.WHITE);
        generateButton.setBorder(BorderFactory.createLineBorder(Color.decode("#000080"), 2));
        generateButton.addActionListener(e -> generateWorkTimetable(timetablePanel));

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(generateButton);

        JButton exportPresetButton = new JButton("Export Preset");
        exportPresetButton.setFont(new Font("Arial", Font.PLAIN, 18));
        exportPresetButton.setPreferredSize(new Dimension(150, 50));
        exportPresetButton.setBackground(Color.WHITE);
        exportPresetButton.setBorder(BorderFactory.createLineBorder(Color.decode("#000080"), 2));
        exportPresetButton.addActionListener(e -> exportPreset());
        bottomPanel.add(exportPresetButton);

        JButton exportTimetableButton = new JButton("Export Timetable");
        exportTimetableButton.setFont(new Font("Arial", Font.PLAIN, 18));
        exportTimetableButton.setPreferredSize(new Dimension(150, 50));
        exportTimetableButton.setBackground(Color.WHITE);
        exportTimetableButton.setBorder(BorderFactory.createLineBorder(Color.decode("#000080"), 2));
        exportTimetableButton.addActionListener(e -> exportTimetable());
        bottomPanel.add(exportTimetableButton);

        JButton exitButton = new JButton("Exit");
        exitButton.setFont(new Font("Arial", Font.PLAIN, 18));
        exitButton.setPreferredSize(new Dimension(150, 50));
        exitButton.setBackground(Color.WHITE);
        exitButton.setBorder(BorderFactory.createLineBorder(Color.decode("#000080"), 2));
        exitButton.addActionListener(e -> System.exit(0));
        bottomPanel.add(exitButton);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        workTimetablePanel.add(bottomPanel, BorderLayout.SOUTH);

        return workTimetablePanel;
    }

    private static void generateWorkTimetable(JPanel timetablePanel) {
        timetablePanel.removeAll();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yy/MM/dd");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel dateLabel = new JLabel("Date");
        dateLabel.setFont(new Font("Arial", Font.BOLD, 16));
        timetablePanel.add(dateLabel, gbc);

        gbc.gridx = 1;
        JLabel memberLabel = new JLabel("Member");
        memberLabel.setFont(new Font("Arial", Font.BOLD, 16));
        timetablePanel.add(memberLabel, gbc);

        gbc.gridx = 2;
        JLabel hoursLabel = new JLabel("Working Hours");
        hoursLabel.setFont(new Font("Arial", Font.BOLD, 16));
        timetablePanel.add(hoursLabel, gbc);

        Random rand = new Random();
        int maxHoursPerDay = 8;

        for (int i = 0; i < 7; i++) {
            LocalDateTime date = sDate.plusDays(i);
            String formattedDate = date.format(dateFormatter);

            for (Member member : members) {
                Set<String> workHours = new HashSet<>(member.getMIT().getOrDefault(date, new HashSet<>()));
                workHours.removeAll(member.getMET().getOrDefault(date, new HashSet<>()));

                while (workHours.size() < member.getMWH() / 7) {
                    int startHour = rand.nextInt(24);
                    int endHour = Math.min(startHour + rand.nextInt(maxHoursPerDay) + 1, 24);
                    String workHour = String.format("%02d-%02d", startHour, endHour);

                    if (workHours.stream().noneMatch(wh -> overlaps(wh, workHour))) {
                        workHours.add(workHour);
                    }
                }

                gbc.gridx = 0;
                gbc.gridy++;
                timetablePanel.add(new JLabel(formattedDate), gbc);

                gbc.gridx = 1;
                timetablePanel.add(new JLabel(member.getName()), gbc);

                gbc.gridx = 2;
                timetablePanel.add(new JLabel(String.join(", ", workHours)), gbc);
            }
        }

        timetablePanel.revalidate();
        timetablePanel.repaint();
    }

    private static boolean overlaps(String existing, String candidate) {
        String[] existingTimes = existing.split("-");
        int existingStart = Integer.parseInt(existingTimes[0]);
        int existingEnd = Integer.parseInt(existingTimes[1]);

        String[] candidateTimes = candidate.split("-");
        int candidateStart = Integer.parseInt(candidateTimes[0]);
        int candidateEnd = Integer.parseInt(candidateTimes[1]);

        return !(candidateEnd <= existingStart || candidateStart >= existingEnd);
    }
    private static void exportPreset() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showSaveDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            if (!filePath.endsWith(".txt")) {
                filePath += ".txt";
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                writer.write("# Members\n");
                for (Member member : members) {
                    writer.write("name: " + member.getName() + "\n");
                    writer.write("number: " + member.getNumber() + "\n");
                    writer.write("must_work_hours: " + member.getMWH() + "\n");

                    writer.write("must_include_times: ");
                    for (Map.Entry<LocalDateTime, Set<String>> entry : member.getMIT().entrySet()) {
                        writer.write("<" + entry.getKey().toLocalDate() + " : " + String.join(", ", entry.getValue()) + ">, ");
                    }
                    writer.write("\n");

                    writer.write("must_exclude_times: ");
                    for (Map.Entry<LocalDateTime, Set<String>> entry : member.getMET().entrySet()) {
                        writer.write("<" + entry.getKey().toLocalDate() + " : " + String.join(", ", entry.getValue()) + ">, ");
                    }
                    writer.write("\n\n");
                }

                writer.write("# Timetable\n");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                for (int i = 0; i < 7; i++) {
                    for (Map.Entry<LocalDateTime, Set<String>> entry : AutoScheduler.timetable.days[i].entrySet()) {
                        writer.write("<" + entry.getKey().format(formatter) + " : " + String.join(", ", entry.getValue()) + ">\n");
                    }
                }

                JOptionPane.showMessageDialog(null, "Preset exported successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error exporting preset.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void exportTimetable() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showSaveDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            if (!filePath.endsWith(".txt")) {
                filePath += ".txt";
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                writer.write("# Timetable\n");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                for (int i = 0; i < 7; i++) {
                    for (Map.Entry<LocalDateTime, Set<String>> entry : AutoScheduler.timetable.days[i].entrySet()) {
                        writer.write("<" + entry.getKey().format(formatter) + " : " + String.join(", ", entry.getValue()) + ">\n");
                    }
                }

                JOptionPane.showMessageDialog(null, "Timetable exported successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error exporting timetable.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
