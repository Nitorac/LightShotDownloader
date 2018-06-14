import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.regex.Pattern;

public class Main {

    public static MainForm mF;
    public static JFrame root;

    public static void main(String[] args) {
        root = new JFrame("LightShot Downloader");
        mF = new MainForm();
        root.setContentPane(mF.rootPanel);
        root.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        root.pack();
        root.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width) / 2 - root.getWidth() / 2, (Toolkit.getDefaultToolkit().getScreenSize().height) / 2 - root.getHeight() / 2);
        root.setVisible(true);

        mF.findBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File("."));
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int response = chooser.showOpenDialog(null);
            if (response == JFileChooser.APPROVE_OPTION) {
                mF.pathEditText.setText(chooser.getSelectedFile().getAbsolutePath());
            } else {
                System.out.println("Choix de dossier annulé !");
            }
        });

        mF.motifEditText.addCaretListener(e -> motifVerif(e.getDot()));
        motifVerif(1);

        mF.executeBtn.addActionListener(e -> {
            File outputDir = new File(mF.pathEditText.getText().trim());
            if(!outputDir.exists()){
                if(!outputDir.mkdirs()){
                    JOptionPane.showMessageDialog(null, "Impossible de créer le dossier de sortie", "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            new ExecutionDialog(root, "Processus", true, mF.motifEditText.getText().trim(), outputDir);
        });
    }

    public static void motifVerif(int caretPosition){
        String input = mF.motifEditText.getText();
        if(input.length() > 6){
            JOptionPane.showMessageDialog(null, "Vous ne pouvez pas dépasser 6 caractères", "Erreur", JOptionPane.ERROR_MESSAGE);
            SwingUtilities.invokeLater(() -> mF.motifEditText.setText(input.substring(0, 6)));
            return;
        }
        if(Pattern.compile("[^a-zA-Z0-9]").matcher(input).find()){
            JOptionPane.showMessageDialog(null, "Il ne faut entrer que des caractères alpha-numériques !", "Erreur", JOptionPane.ERROR_MESSAGE);
            StringBuilder sb = new StringBuilder(input);
            sb.deleteCharAt(caretPosition-1);
            SwingUtilities.invokeLater(() -> mF.motifEditText.setText(sb.toString()));
            return;
        }

        int nb = (int)Math.pow(36, (6 - input.length()));
        mF.executeBtn.setText("Exécuter (" + NumberFormat.getInstance(Locale.FRENCH).format(nb) + " images à télécharger)");
    }
}
