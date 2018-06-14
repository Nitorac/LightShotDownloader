package net.nitorac;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ResultDialog extends JDialog {

    public ConcurrentLinkedQueue<ImageRequest> requests;
    public AtomicInteger errors;
    public ConcurrentHashMap<String, BufferedImage> images;

    public ResultsForm form;
    private String pattern;
    private File saveDir;

    public static final char[] ALL_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
    public static final String BASE_URL = "https://prnt.sc/";

    public ResultDialog(JFrame parent, String title, boolean modal, String pattern, File saveDir){
        super(parent, title, false);
        this.pattern = pattern;
        this.saveDir = saveDir;
        form = new ResultsForm();
        this.setContentPane(form.rootPanel);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.pack();
        this.setVisible(true);

        requests = new ConcurrentLinkedQueue<>();
        images = new ConcurrentHashMap<>();
        errors = new AtomicInteger(0);

        form.imgList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String prefix = (String)form.imgList.getSelectedValue();
                form.imgLbl.setIcon(new ImageIcon(getFittedImage(images.get(prefix))));
            }
        });

        form.imgLbl.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                String prefix = (String)form.imgList.getSelectedValue();
                form.imgLbl.setIcon(new ImageIcon(getFittedImage(images.get(prefix))));
            }
        });

        start();
    }

    public Image getFittedImage(BufferedImage img){
        int panelW = form.imgPanel.getWidth();
        int panelH = form.imgPanel.getHeight();
        int imgW = img.getWidth(null);
        int imgH = img.getHeight(null);

        double widthRatio = (double)panelW / (double)imgW;
        double heightRatio = (double)panelH / (double)imgH;
        double ratio = Math.min(widthRatio, heightRatio);

        return img.getScaledInstance((int)Math.max(1,(imgW * ratio)), (int)Math.max(1,(imgH * ratio)), Image.SCALE_SMOOTH);
    }

    public void start(){
        Thread queuer = new Thread(new QueuerTask(getAllMotifs(pattern)));
        Thread downloader = new Thread(new DownloaderTask(saveDir));
        SwingUtilities.invokeLater(() -> {
            queuer.start();
            downloader.start();
        });
    }

    /*public void updateDisplay(int i, int err, int total, String pattern, String ext){
        form.imgDL.setText("Images téléchargées : " + String.valueOf(i - err));
        form.img2download.setText("Images à télécharger : " + String.valueOf(total - i));
        form.currentIndex.setText("Indice actuel : " + pattern);
        form.errors.setText("Erreurs : " + String.valueOf(err));
        form.currentPath.setText("Fichier actuel : " + (new File(saveDir, pattern + ext)));
    }*/

    public static ArrayList<String> getAllMotifs(String pattern){
        ArrayList<String> results = new ArrayList<>();
        if(pattern.length() == 6){
            results.add(pattern);
            return results;
        }
        for(char c : ALL_CHARS){
            results.addAll(getAllMotifs(pattern + String.valueOf(c)));
        }
        return results;
    }
}
