import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {

    public LoginPanel() {
        setLayout(new GridBagLayout());
        setBackground(Color.BLACK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);


        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(Color.GREEN);
        userLabel.setFont(new Font("Arial", Font.BOLD, 16));
        JTextField userField = new JTextField(15);


        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(Color.GREEN);
        passLabel.setFont(new Font("Arial", Font.BOLD, 16));
        JPasswordField passField = new JPasswordField(15);


        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");


        gbc.gridx = 0; gbc.gridy = 0; add(userLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 0; add(userField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; add(passLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 1; add(passField, gbc);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        gbc.gridx = 1; gbc.gridy = 2; add(buttonPanel, gbc);


    }
}