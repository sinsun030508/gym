package project.gym;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;

public class SignUpFrame extends JFrame {
    private JTextField nameField, phoneField;
    private JCheckBox cabinetBox;
    private JLabel priceLabel;
    private JRadioButton threeMonth, sixMonth, twelveMonth;
    private ButtonGroup membershipGroup;

    public SignUpFrame() {
        setTitle("회원가입");
        setSize(400, 300);
        setLayout(new GridLayout(7, 2));

        nameField = new JTextField();
        phoneField = new JTextField();
        cabinetBox = new JCheckBox("캐비닛 사용 (1만원/월)");
        priceLabel = new JLabel("총 요금: 0원");

        threeMonth = new JRadioButton("3개월 (13만원)");
        sixMonth = new JRadioButton("6개월 (24만원)");
        twelveMonth = new JRadioButton("12개월 (40만원)");
        membershipGroup = new ButtonGroup();
        membershipGroup.add(threeMonth);
        membershipGroup.add(sixMonth);
        membershipGroup.add(twelveMonth);

        JButton submitBtn = new JButton("가입 완료");

        cabinetBox.addActionListener(e -> updatePrice());
        threeMonth.addActionListener(e -> updatePrice());
        sixMonth.addActionListener(e -> updatePrice());
        twelveMonth.addActionListener(e -> updatePrice());

        submitBtn.addActionListener(e -> registerMember());

        add(new JLabel("이름")); add(nameField);
        add(new JLabel("전화번호")); add(phoneField);
        add(threeMonth); add(sixMonth);
        add(twelveMonth); add(cabinetBox);
        add(priceLabel); add(new JLabel());
        add(submitBtn);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void updatePrice() {
        int price = 0;
        if (threeMonth.isSelected()) price = 130000;
        else if (sixMonth.isSelected()) price = 240000;
        else if (twelveMonth.isSelected()) price = 400000;

        if (cabinetBox.isSelected()) {
            if (threeMonth.isSelected()) price += 10000 * 3;
            else if (sixMonth.isSelected()) price += 10000 * 6;
            else if (twelveMonth.isSelected()) price += 10000 * 12;
        }

        priceLabel.setText("총 요금: " + price + "원");
    }

    private void registerMember() {
        String name = nameField.getText();
        String phone = phoneField.getText();
        boolean cabinet = cabinetBox.isSelected();
        int months = threeMonth.isSelected() ? 3 : sixMonth.isSelected() ? 6 : 12;
        LocalDate now = LocalDate.now();

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "INSERT INTO members (name, phone, membership_months, cabinet, start_date) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, phone);
            pstmt.setInt(3, months);
            pstmt.setBoolean(4, cabinet);
            pstmt.setDate(5, Date.valueOf(now));
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "회원가입 완료!");
            dispose();
            new LoginFrame();  // 로그인 창으로 이동
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB 저장 오류");
        }
    }
}
