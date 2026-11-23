import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TrabalhoPratico05 extends JFrame implements ActionListener {

    private final JTextField txtMaximo;
    private final JTextField txtPrioridade1;
    private final JTextField txtPrioridade2;
    private final JButton btnIniciar;
    private final JButton btnLimpar;

    private final PainelContadorRunnable painelThread1;
    private final PainelContadorRunnable painelThread2;

    private Thread thread1;
    private Thread thread2;

    public TrabalhoPratico05() {
        super("Trabalho Prático 5");
        setSize(680, 480);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Painel superior: entradas e botão Iniciar
        JPanel painelTopo = new JPanel(new GridBagLayout());
        painelTopo.setBorder(BorderFactory.createEmptyBorder(12, 12, 5, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;

        txtMaximo = new JTextField("1000", 12);
        txtPrioridade1 = new JTextField("1", 12);
        txtPrioridade2 = new JTextField("10", 12);
        btnIniciar = new JButton("Iniciar");
        btnIniciar.addActionListener(this);

        // Linha 0: Máximo
        gbc.gridx = 0;
        gbc.gridy = 0;
        painelTopo.add(new JLabel("Máximo:"), gbc);

        gbc.gridx = 1;
        painelTopo.add(txtMaximo, gbc);

        // Linha 1: Prioridade 1 + observação ao lado
        gbc.gridx = 0;
        gbc.gridy = 1;
        painelTopo.add(new JLabel("Prioridade 1:"), gbc);

        gbc.gridx = 1;
        painelTopo.add(txtPrioridade1, gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridheight = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        painelTopo.add(new JLabel("}   Número entre 1 e 10"), gbc);
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.WEST;

        // Linha 2: Prioridade 2
        gbc.gridx = 0;
        gbc.gridy = 2;
        painelTopo.add(new JLabel("Prioridade 2:"), gbc);

        gbc.gridx = 1;
        painelTopo.add(txtPrioridade2, gbc);

        // Linha 3: Botão Iniciar
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 6, 4, 6);
        painelTopo.add(btnIniciar, gbc);

        add(painelTopo, BorderLayout.NORTH);

        // Painel central: dois painéis Runnable com lista e rótulo Final
        painelThread1 = new PainelContadorRunnable();
        painelThread2 = new PainelContadorRunnable();

        JPanel painelCentro = new JPanel(new GridLayout(1, 2, 40, 10));
        painelCentro.setBorder(BorderFactory.createEmptyBorder(5, 50, 5, 50));
        painelCentro.add(painelThread1);
        painelCentro.add(painelThread2);

        add(painelCentro, BorderLayout.CENTER);

        // Painel inferior: botão Limpar
        btnLimpar = new JButton("Limpar");
        btnLimpar.addActionListener(this);

        JPanel painelRodape = new JPanel(new FlowLayout(FlowLayout.CENTER));
        painelRodape.setBorder(BorderFactory.createEmptyBorder(5, 10, 15, 10));
        painelRodape.add(btnLimpar);

        add(painelRodape, BorderLayout.SOUTH);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnIniciar) {
            iniciarContagem();
        } else if (e.getSource() == btnLimpar) {
            limparTela();
        }
    }

    private void iniciarContagem() {
        try {
            int maximo = Integer.parseInt(txtMaximo.getText().trim());
            int prio1 = Integer.parseInt(txtPrioridade1.getText().trim());
            int prio2 = Integer.parseInt(txtPrioridade2.getText().trim());

            if (maximo <= 0) {
                JOptionPane.showMessageDialog(this, "Informe um valor máximo maior que zero.");
                return;
            }
            if (prio1 < Thread.MIN_PRIORITY || prio1 > Thread.MAX_PRIORITY ||
                    prio2 < Thread.MIN_PRIORITY || prio2 > Thread.MAX_PRIORITY) {
                JOptionPane.showMessageDialog(this, "As prioridades devem estar entre 1 e 10.");
                return;
            }

            painelThread1.prepararContagem(maximo);
            painelThread2.prepararContagem(maximo);
            btnIniciar.setEnabled(false);

            thread1 = new Thread(painelThread1, "Thread-1");
            thread2 = new Thread(painelThread2, "Thread-2");

            thread1.setPriority(prio1);
            thread2.setPriority(prio2);

            Thread monitor = new Thread(() -> {
                try {
                    thread1.join();
                    thread2.join();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                SwingUtilities.invokeLater(() -> btnIniciar.setEnabled(true));
            });

            thread1.start();
            thread2.start();
            monitor.start();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Digite apenas números inteiros válidos.");
        }
    }

    private void limparTela() {
        if (thread1 != null && thread1.isAlive()) {
            thread1.interrupt();
        }
        if (thread2 != null && thread2.isAlive()) {
            thread2.interrupt();
        }

        painelThread1.limpar();
        painelThread2.limpar();
        btnIniciar.setEnabled(true);
    }

    // Painel que implementa Runnable conforme pedido no enunciado
    private static class PainelContadorRunnable extends JPanel implements Runnable {
        private final DefaultListModel<String> modelo;
        private final JList<String> lista;
        private final JLabel lblFinal;
        private int limiteMaximo;

        public PainelContadorRunnable() {
            setLayout(new BorderLayout(0, 6));

            modelo = new DefaultListModel<>();
            lista = new JList<>(modelo);
            lista.setFont(new Font("SansSerif", Font.PLAIN, 13));

            JScrollPane scrollPane = new JScrollPane(lista);
            scrollPane.setPreferredSize(new Dimension(140, 150));

            lblFinal = new JLabel("Final", SwingConstants.CENTER);
            lblFinal.setFont(lblFinal.getFont().deriveFont(Font.BOLD));
            lblFinal.setVisible(false);

            JPanel painelRodapeLista = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 2));
            painelRodapeLista.add(lblFinal);

            add(scrollPane, BorderLayout.CENTER);
            add(painelRodapeLista, BorderLayout.SOUTH);
        }

        public void prepararContagem(int limiteMaximo) {
            this.limiteMaximo = limiteMaximo;
            modelo.clear();
            lblFinal.setVisible(false);
        }

        public void limpar() {
            modelo.clear();
            lblFinal.setVisible(false);
        }

        @Override
        public void run() {
            for (int i = 0; i < limiteMaximo; i++) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }

                final String textoNumero = String.valueOf(i);
                SwingUtilities.invokeLater(() -> {
                    modelo.addElement(textoNumero);
                    int ultimo = modelo.getSize() - 1;
                    if (ultimo >= 0) {
                        lista.ensureIndexIsVisible(ultimo);
                    }
                });

                try {
                    Thread.sleep(3);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            SwingUtilities.invokeLater(() -> lblFinal.setVisible(true));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TrabalhoPratico05().setVisible(true));
    }
}