package project.gym;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class MemberInfoFrame extends JFrame {
    private JLabel nameLabel, daysLeftLabel, goalWeightLabel, attendanceLabel, diffLabel;
    private int memberId;
    private int attendanceDays;
    private int membershipMonths;
    private LocalDate startDate;
    private LocalDate lastAttendanceDate;
    private Float goalWeight;

    public MemberInfoFrame(ResultSet rs) throws SQLException {
        setTitle("회원 정보");
        setSize(500, 400);

        // DB 정보 추출
        memberId = rs.getInt("id");
        String name = rs.getString("name");
        attendanceDays = rs.getInt("attendance_days");
        membershipMonths = rs.getInt("membership_months");
        startDate = rs.getDate("start_date").toLocalDate();
        Date lastDate = rs.getDate("last_attendance_date");
        lastAttendanceDate = (lastDate != null) ? lastDate.toLocalDate() : null;
        goalWeight = rs.getFloat("goal_weight");
        if (rs.wasNull()) goalWeight = null;
        getContentPane().setLayout(null);
        goalWeightLabel = new JLabel("목표 체중: " + (goalWeight != null ? goalWeight + "kg" : "미설정"));
        goalWeightLabel.setFont(new Font("굴림", Font.BOLD, 12));
        goalWeightLabel.setBounds(10, 300, 128, 25);
        getContentPane().add(goalWeightLabel);
        daysLeftLabel = new JLabel("남은 날짜: " + calculateDaysLeft() + "일");
        daysLeftLabel.setBounds(10, 34, 168, 24);
        getContentPane().add(daysLeftLabel);
        daysLeftLabel.setFont(new Font("굴림", Font.BOLD, 15));
        nameLabel = new JLabel("회원 이름: " + name);
        nameLabel.setBounds(10, 13, 168, 18);
        getContentPane().add(nameLabel);
        nameLabel.setFont(new Font("굴림", Font.BOLD, 15));
        attendanceLabel = new JLabel("출석 일수: " + attendanceDays + "일");
        attendanceLabel.setBounds(360, 81, 92, 30);
        getContentPane().add(attendanceLabel);
        JButton attendBtn = new JButton("출석");
        attendBtn.setBounds(338, 15, 128, 63);
        getContentPane().add(attendBtn);
        JTextField weightField = new JTextField();
        weightField.setBounds(72, 330, 83, 23);
        getContentPane().add(weightField);
        JButton goalBtn = new JButton("목표 체중");
        goalBtn.setBounds(167, 330, 92, 23);
        getContentPane().add(goalBtn);
        JButton nowBtn = new JButton("현재 체중");
        nowBtn.setBounds(269, 330, 92, 23);
        getContentPane().add(nowBtn);
        
                JLabel label_1 = new JLabel("체중 입력:");
                label_1.setBounds(10, 330, 64, 23);
                getContentPane().add(label_1);
                diffLabel = new JLabel(" ");
                diffLabel.setBounds(136, 301, 213, 23);
                getContentPane().add(diffLabel);
        nowBtn.addActionListener(e -> compareWeight(weightField.getText()));
        goalBtn.addActionListener(e -> setGoalWeight(weightField.getText()));
        attendBtn.addActionListener(e -> markAttendance());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private long calculateDaysLeft() {
        LocalDate endDate = startDate.plusMonths(membershipMonths);
        return ChronoUnit.DAYS.between(LocalDate.now(), endDate);
    }

    private void markAttendance() {
        LocalDate today = LocalDate.now();

        if (lastAttendanceDate != null && lastAttendanceDate.equals(today)) {
            JOptionPane.showMessageDialog(this, "오늘은 이미 출석했습니다!");
            return;
        }

        attendanceDays++;
        lastAttendanceDate = today;

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "UPDATE members SET attendance_days = ?, last_attendance_date = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, attendanceDays);
            pstmt.setDate(2, Date.valueOf(today));
            pstmt.setInt(3, memberId);
            pstmt.executeUpdate();

            attendanceLabel.setText("출석 일수: " + attendanceDays + "일");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void setGoalWeight(String weightStr) {
        try {
            float weight = Float.parseFloat(weightStr);
            goalWeight = weight;
            try (Connection conn = DBUtil.getConnection()) {
                String sql = "UPDATE members SET goal_weight = ? WHERE id = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setFloat(1, weight);
                pstmt.setInt(2, memberId);
                pstmt.executeUpdate();
                goalWeightLabel.setText("목표 체중: " + weight + "kg");
                JOptionPane.showMessageDialog(this, "목표 체중 설정 완료");
            }
        } catch (NumberFormatException | SQLException ex) {
            JOptionPane.showMessageDialog(this, "유효한 숫자를 입력하세요");
        }
    }

    private void compareWeight(String weightStr) {
        try {
            float nowWeight = Float.parseFloat(weightStr);
            if (goalWeight == null) {
                JOptionPane.showMessageDialog(this, "목표 체중이 설정되지 않았습니다.");
                return;
            }

            if (nowWeight == goalWeight) {
                diffLabel.setText("🎉 목표 체중 달성!");
            } else {
                float diff = nowWeight - goalWeight;
                diffLabel.setText("목표까지 " + String.format("%.1f", Math.abs(diff)) + "kg " + (diff > 0 ? "감량 필요" : "증가 필요"));
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "체중 입력 오류");
        }
    }
}

