import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String user = userField.getText();
                String pass = new String(passField.getPassword());

                if (user.isEmpty() || pass.isEmpty()) {
                    JOptionPane.showMessageDialog(LoginPanel.this, "Please fill in all fields!", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    if (DatabaseManager.register(user, pass)) {
                        JOptionPane.showMessageDialog(LoginPanel.this, "Registration Successful! You can now login.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        userField.setText("");
                        passField.setText("");
                    } else {
                        JOptionPane.showMessageDialog(LoginPanel.this, "Username already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String user = userField.getText();
                String pass = new String(passField.getPassword());

                if (user.isEmpty() || pass.isEmpty()) {
                    JOptionPane.showMessageDialog(LoginPanel.this, "Please fill in all fields!", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    if (DatabaseManager.login(user, pass)) {
                        JOptionPane.showMessageDialog(LoginPanel.this, "Login Successful! Welcome " + user, "Success", JOptionPane.INFORMATION_MESSAGE);
                        CardLayout cl = (CardLayout) getParent().getLayout();
                        cl.show(getParent(), "MainMenuScreen");
                    } else {
                        JOptionPane.showMessageDialog(LoginPanel.this, "Invalid Username or Password!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
    }
}