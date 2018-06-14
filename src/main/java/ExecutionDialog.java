import com.sun.deploy.Environment;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExecutionDialog extends JDialog {

    private ExecutionForm form;
    private String pattern;
    private File saveDir;

    public static final char[] ALL_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
    public static final String BASE_URL = "https://prnt.sc/";

    public ExecutionDialog(JFrame parent, String title, boolean modal, String pattern, File saveDir){
        super(parent, title, false);
        this.pattern = pattern;
        this.saveDir = saveDir;
        form = new ExecutionForm();
        this.setContentPane(form.rootPanel);
        this.pack();
        this.setVisible(true);
        start();
    }

    public void start(){
        ArrayList<String> combinaisons = getAllMotifs(pattern);
        int errors = 0;
        for(int i = 0; i < combinaisons.size(); i++){
            String prefix = combinaisons.get(i);
            StringBuilder ext = new StringBuilder();
            errors += (download(saveDir, prefix, ext)) ? 0 : 1;
            final int err = errors;
            final int it = i;
            SwingUtilities.invokeLater(() -> updateDisplay(it, err, combinaisons.size(), prefix, ext.toString()));
        }
        dispose();
    }

    public void updateDisplay(int i, int err, int total, String pattern, String ext){
        form.imgDL.setText("Images téléchargées : " + String.valueOf(i - err));
        form.img2download.setText("Images à télécharger : " + String.valueOf(total - i));
        form.currentIndex.setText("Indice actuel : " + pattern);
        form.errors.setText("Erreurs : " + String.valueOf(err));
        form.currentPath.setText("Fichier actuel : " + (new File(saveDir, pattern + ext)));
    }

    public static boolean download(File saveDir, String prefix, StringBuilder ext){
        try {
            System.out.println(BASE_URL + prefix);
            Document doc = Jsoup.connect(BASE_URL + prefix)
                .userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36")
                .followRedirects(true)
                .get();
            String url = doc.getElementById("screenshot-image").attr("src").trim();
            if(url.isEmpty() || url.startsWith("//")){
                return false;
            }

            ext.append(getExt(url));

            HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(true);
            connection.addRequestProperty("Accept-Language", "en-us,fr-fr,zh-cn;q=0.5");
            connection.addRequestProperty("Connection", "keep-alive");
            connection.addRequestProperty("Accept-Encoding", "gzip, deflate");
            connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:60.0) Gecko/20100101 Firefox/60.0");
            connection.connect();
            InputStream is = connection.getInputStream();
            OutputStream os = new FileOutputStream(new File(saveDir, prefix + ext.toString()));

            byte[] buffer = new byte[1024];
            int byteReaded = is.read(buffer);
            while(byteReaded != -1)
            {
                os.write(buffer,0,byteReaded);
                byteReaded = is.read(buffer);
            }

            os.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }

    public static String getExt(String url){
        String ext = "";
        Matcher m = Pattern.compile("[^/.]*\\.(\\w+)[^/]*$").matcher(url);
        if(m.find()) {
            ext = "." + m.group(1);
        }
        return ext;
    }

    public static void main(String[] args){

    }

    public static ArrayList<String> getAllMotifs(String prefix){
        ArrayList<String> results = new ArrayList<>();
        if(prefix.length() == 6){
            results.add(prefix);
            return results;
        }
        for(char c : ALL_CHARS){
            results.addAll(getAllMotifs(prefix + String.valueOf(c)));
        }
        return results;
    }
}
