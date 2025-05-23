package project.gym;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.*;
import java.time.temporal.ChronoUnit;

public class MemberInfoFrame extends JFrame {
    private JLabel nameLabel, daysLeftLabel, goalWeightLabel, attendanceLabel, diffLabel;
    private JLabel durationLabel, nowWeightLabel;
    private int memberId;
    private int attendanceDays;
    private int membershipMonths;
    private LocalDate startDate;
    private LocalDate lastAttendanceDate;
    private Float goalWeight;
    private LocalDate todayDate;

    public MemberInfoFrame(ResultSet rs) throws SQLException {
        setTitle("회원 정보");
        setSize(500, 450);
        getContentPane().setLayout(null);

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

        todayDate = LocalDate.now();

        nameLabel = new JLabel("회원 이름: " + name);
        nameLabel.setFont(new Font("굴림", Font.BOLD, 15));
        nameLabel.setBounds(10, 13, 200, 18);
        getContentPane().add(nameLabel);

        daysLeftLabel = new JLabel("남은 날짜: " + calculateDaysLeft() + "일");
        daysLeftLabel.setFont(new Font("굴림", Font.BOLD, 15));
        daysLeftLabel.setBounds(10, 34, 200, 24);
        getContentPane().add(daysLeftLabel);

        attendanceLabel = new JLabel("출석 일수: " + attendanceDays + "일");
        attendanceLabel.setBounds(348, 130, 120, 30);
        getContentPane().add(attendanceLabel);

        goalWeightLabel = new JLabel("목표 체중: " + (goalWeight != null ? goalWeight + "kg" : "미설정"));
        goalWeightLabel.setFont(new Font("굴림", Font.BOLD, 12));
        goalWeightLabel.setBounds(10, 56, 200, 25);
        getContentPane().add(goalWeightLabel);

        diffLabel = new JLabel(goalWeight != null ? getGoalDiffText(null) : " ");
        diffLabel.setBounds(10, 115, 250, 25);
        getContentPane().add(diffLabel);

        durationLabel = new JLabel("오늘 이용 시간: -");
        durationLabel.setBounds(348, 157, 134, 25);
        getContentPane().add(durationLabel);

        nowWeightLabel = new JLabel("현재 체중: -");
        nowWeightLabel.setBounds(10, 87, 180, 18);
        getContentPane().add(nowWeightLabel);

        JButton attendBtn = new JButton("출석");
        attendBtn.setBounds(338, 15, 128, 63);
        getContentPane().add(attendBtn);

        JButton endBtn = new JButton("이용 종료");
        endBtn.setBounds(338, 90, 128, 30);
        getContentPane().add(endBtn);

        JTextField weightField = new JTextField();
        weightField.setBounds(72, 330, 83, 23);
        getContentPane().add(weightField);

        JButton goalBtn = new JButton("목표 체중");
        goalBtn.setBounds(167, 330, 92, 23);
        getContentPane().add(goalBtn);

        JButton nowBtn = new JButton("현재 체중");
        nowBtn.setBounds(269, 330, 92, 23);
        getContentPane().add(nowBtn);

        JLabel weightLabel = new JLabel("체중 입력:");
        weightLabel.setBounds(10, 330, 64, 23);
        getContentPane().add(weightLabel);

        attendBtn.addActionListener(e -> markAttendance());
        endBtn.addActionListener(e -> markEnd());
        goalBtn.addActionListener(e -> setGoalWeight(weightField.getText()));
        nowBtn.addActionListener(e -> compareWeight(weightField.getText()));

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
        LocalDateTime startTime = LocalDateTime.now();

        try (Connection conn = DBUtil.getConnection()) {
            String sql = "UPDATE members SET attendance_days = ?, last_attendance_date = ?, start_time = ?, end_time = NULL WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, attendanceDays);
            pstmt.setDate(2, Date.valueOf(today));
            pstmt.setTimestamp(3, Timestamp.valueOf(startTime));
            pstmt.setInt(4, memberId);
            pstmt.executeUpdate();

            attendanceLabel.setText("출석 일수: " + attendanceDays + "일");
            durationLabel.setText("오늘 이용 시간: -");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void markEnd() {
        LocalDate today = LocalDate.now();

        if (lastAttendanceDate == null || !lastAttendanceDate.equals(today)) {
            JOptionPane.showMessageDialog(this, "출석하지 않았습니다.");
            return;
        }

        try (Connection conn = DBUtil.getConnection()) {
            LocalDateTime endTime = LocalDateTime.now();

            String updateSql = "UPDATE members SET end_time = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(updateSql);
            pstmt.setTimestamp(1, Timestamp.valueOf(endTime));
            pstmt.setInt(2, memberId);
            pstmt.executeUpdate();

            // 시작 시간 불러오기
            String selectSql = "SELECT start_time FROM members WHERE id = ?";
            pstmt = conn.prepareStatement(selectSql);
            pstmt.setInt(1, memberId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Timestamp startTs = rs.getTimestamp("start_time");
                if (startTs != null) {
                    LocalDateTime startTime = startTs.toLocalDateTime();
                    long minutes = ChronoUnit.MINUTES.between(startTime, endTime);
                    durationLabel.setText("오늘 이용 시간: " + minutes + "분");
                } else {
                    JOptionPane.showMessageDialog(this, "시작 시간이 존재하지 않습니다.");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "DB 오류 발생");
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
                diffLabel.setText(getGoalDiffText(null));
                JOptionPane.showMessageDialog(this, "목표 체중 설정 완료");
            }
        } catch (NumberFormatException | SQLException ex) {
            JOptionPane.showMessageDialog(this, "유효한 숫자를 입력하세요");
        }
    }

    private void compareWeight(String weightStr) {
        try {
            float nowWeight = Float.parseFloat(weightStr);
            nowWeightLabel.setText("현재 체중: " + nowWeight + "kg");

            if (goalWeight == null) {
                JOptionPane.showMessageDialog(this, "목표 체중이 설정되지 않았습니다.");
                return;
            }

            diffLabel.setText(getGoalDiffText(nowWeight));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "체중 입력 오류");
        }
    }

    private String getGoalDiffText(Float nowWeight) {
        if (goalWeight == null) return "목표 체중 미설정";

        if (nowWeight == null) return "";

        float diff = nowWeight - goalWeight;
        if (diff == 0f) return "🎉 목표 체중 달성!";
        return "목표까지 " + String.format("%.1f", Math.abs(diff)) + "kg " + (diff > 0 ? "감량 필요" : "증가 필요");
    }
}
