package net.nitorac;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;

public class QueuerTask implements Runnable {

    private ArrayList<String> combis;

    public QueuerTask(ArrayList<String> combis){
        this.combis = combis;
    }

    @Override
    public void run() {
        System.out.println("QueuerTask démarre !");
        for(String s : combis){
            try{
                Document doc = Jsoup.connect(ResultDialog.BASE_URL + s)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36")
                    .followRedirects(true)
                    .get();
                String url = doc.getElementById("screenshot-image").attr("src").trim();
                if(url.isEmpty() || url.startsWith("//")){
                    Main.resDiag.errors.incrementAndGet();
                    continue;
                }

                Main.resDiag.requests.offer(new ImageRequest(url, s));
                System.out.println("Élément queué : " + s);
            }catch (IOException e){
                e.printStackTrace();
                Main.resDiag.errors.incrementAndGet();
            }
        }
    }
}
