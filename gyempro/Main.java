package project.gym;

import java.awt.EventQueue;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;

    /**
     * 애플리케이션 시작
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                DBUtil.init(); // DB 연결 초기화
                Main frame = new Main();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 메인 프레임 생성
     */
    public Main() {
        setTitle("헬스장 회원 관리 메인");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 300, 250);
        contentPane = new JPanel();
        contentPane.setLayout(null);
        setContentPane(contentPane);

        JButton btnSignUp = new JButton("회원가입");
        btnSignUp.setBounds(60, 30, 160, 60);
        contentPane.add(btnSignUp);

        JButton btnLogin = new JButton("로그인");
        btnLogin.setBounds(60, 110, 160, 60);
        contentPane.add(btnLogin);

        // 회원가입 창 열기
        btnSignUp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new SignUpFrame();
            }
        });

        // 로그인 창 열기
        btnLogin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new LoginFrame();
            }
        });
    }
}