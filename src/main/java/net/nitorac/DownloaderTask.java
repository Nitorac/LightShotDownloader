package net.nitorac;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloaderTask implements Runnable {

    File saveDir;

    public DownloaderTask(File saveDir){
        this.saveDir = saveDir;
    }

    @Override
    public void run() {
        System.out.println("DownloaderTask démarre !");
        while(true){
            if(Main.resDiag.requests.isEmpty()){
                try {
                    System.out.println("DownloaderTask attent !");
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }

            try {
                ImageRequest img = Main.resDiag.requests.poll();
                String url = img.url;
                String ext = getExt(url);
                System.out.println("Élément en téléchargement : " + img.prefix);

                /*System.setProperty("http.proxyHost", "172.20.0.1");
                System.setProperty("http.proxyPort", "3128");
                System.setProperty("https.proxyHost", "172.20.0.1");
                System.setProperty("https.proxyPort", "3128");*/

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
                File fileSave = new File(saveDir, img.prefix + ext.toString());
                OutputStream os = new FileOutputStream(fileSave);

                byte[] buffer = new byte[1024];
                int byteReaded = is.read(buffer);
                while(byteReaded != -1) {
                    os.write(buffer,0,byteReaded);
                    byteReaded = is.read(buffer);
                }

                os.close();
                System.out.println("Élément téléchargé : " + img.prefix);
                Main.resDiag.images.put(img.prefix, ImageIO.read(fileSave));
                ((DefaultListModel)Main.resDiag.form.imgList.getModel()).addElement(img.prefix);

            } catch (IOException e) {
                e.printStackTrace();
            }
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
}
