import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by students on 31.05.17.
 */
public class QQ_filter_v2_2 {
    static Map<String, String> q2q = new HashMap<String, String>(30000000);
    //static ArrayList<Thread> threads = new ArrayList<Thread>();
    public static void main(String[] args) {
        int threadcnt = Integer.parseInt(args[1]);
        fillQQHash();
        System.err.println("QQ HASH FILLED");
        Map<String,Double> acc2len = buildLenHash(); /**CREATE HASH FOR LENGTH **/
        System.err.println("SEQUENCE LENGTH HASH FILLED");
        ArrayList<String> fusionIn = readNames(args[0]);
        ArrayList<Thread> threads = new ArrayList<Thread>(threadcnt);
        int i = 0;
        while(true){
            if(actThreadCnt(threads) < threadcnt){
                Thread thread = new Thread(new QQfilter(fusionIn.get(i),q2q, acc2len));
                threads.add(thread);
                thread.start();
                System.err.println(fusionIn.get(i) + " | " +actThreadCnt(threads));
                i++;
            }
            if(i >= fusionIn.size()){
                break;
            }
            else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void fillQQHash(){
        BufferedReader br;
        try {
            String sCurrentLine;
            br = new BufferedReader(new FileReader("/home/constantin/Fusions/QQBlast/BLAST_OUT/all_QQ_sort.blast"));
            while ((sCurrentLine = br.readLine()) != null) {
                String[] fields = sCurrentLine.split("\t");
				if (Double.parseDouble(fields[10]) <= 1e-10) {
                	if (q2q.containsKey(fields[0])) {
                    	String existing = q2q.get(fields[0]);
                    	String extraContent = fields[1];
                    	q2q.put(fields[0], existing + "\t" + extraContent);
                	} else {
                    	q2q.put(fields[0], fields[1]);
                	}
				}
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<String> readNames(String filename){
        ArrayList<String> ret = new ArrayList<String>();
        BufferedReader br;
        try {
            String sCurrentLine;
            br = new BufferedReader(new FileReader("/home/constantin/Fusions/ALL_BLAST_ALL_ORG_v2/FuDet_OUT/QQBLAST_filtered/" + filename));
            while ((sCurrentLine = br.readLine()) != null) {
                ret.add(sCurrentLine.trim());
            }
        } catch (IOException e){
            e.printStackTrace();
        }

        return ret;
    }

    public static int actThreadCnt(ArrayList<Thread> threads){
        int ret = 0;
        for (Thread thread : threads) {
            if(thread.isAlive()){
                ret++;
            }
        }
        return ret;
    }

    public static HashMap<String, Double> buildLenHash(){
        FileReader fr = null;
        BufferedReader br = null;
        HashMap<String, Double> ret = new HashMap<String, Double>((int) (Math.ceil(19050992/0.75)+1));
        try {
            fr = new FileReader("/home/constantin/Fusions/seq_length.list");
            br = new BufferedReader(fr);
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                String[] fields = sCurrentLine.split("\t");
                ret.put(fields[0], Double.parseDouble(fields[1]));
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try {
                if (br != null)
                    br.close();
                if (fr != null)
                    fr.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return ret;
    }

}

class QQfilter implements Runnable{
    String filename;
    Map<String, String> q2q;
    Map<String,Double> acc2len;
    File out;
    FileWriter writer;
    public QQfilter(String Filename, Map<String,String> Q2q, Map<String, Double> Acc2len){
        this.filename = Filename;
        this.q2q = Q2q;
        this.acc2len = Acc2len;
        this.out = new File("QQfilter_OUT_v2.2/" + Filename.split("fu")[0] + "QQ.fusion");
        try {
            this.writer = new FileWriter(out);
            //out.delete();
            //out.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void run(){
        try {
            String sCurrentLine;
            ArrayList<String> new_prot = new ArrayList<String>();
            BufferedReader br = new BufferedReader(new FileReader("/home/constantin/Fusions/ALL_BLAST_ALL_ORG_v2/FuDet_OUT/" + filename));
            int iend, iend2, DashIndex, Gstart, clParIndex, Gend, DashIndex2, Gstart2, clParIndex2, Gend2;
            Pattern findscov = Pattern.compile("(GCF[^\\t:]+)\\((\\d+-\\d+)\\)");
            while ((sCurrentLine = br.readLine()) != null) {
                String[] fields = sCurrentLine.split("::");
                String[] fields2 = fields[1].split("\t");
                String[] split = sCurrentLine.split("\t");
                String acc, acc2;
                boolean sw = false;
                for (int i = 0; i < fields2.length; i++){
                    DashIndex = fields2[i].indexOf("-");
                    clParIndex = fields2[i].indexOf(")");
                    iend = fields2[i].indexOf("(");
                    Gstart = Integer.parseInt(fields2[i].substring(iend+1, DashIndex));
                    Gend = Integer.parseInt(fields2[i].substring(DashIndex+1, clParIndex));
                    acc = fields2[i].substring(0,iend);
                    Gene Gene1 = new Gene(acc,Gstart,Gend);
                    for (int j = i+1; j < fields2.length; j++) {
                        DashIndex2 = fields2[j].indexOf("-");
                        clParIndex2 = fields2[j].indexOf(")");
                        iend2 = fields2[j].indexOf("(");
                        Gstart2 = Integer.parseInt(fields2[j].substring(iend2+1, DashIndex2));
                        Gend2 = Integer.parseInt(fields2[j].substring(DashIndex2+1, clParIndex2));
                        acc2 = fields2[j].substring(0,iend2);
                        Gene Gene2 = new Gene(acc2,Gstart2,Gend2);
                        if (!q2q.get(acc).contains(acc2) && !q2q.get(acc2).contains(acc) && Genecmp(Gene1,Gene2,10.0)){
                            //System.err.println(Gene1.getGenename() + "::" + Gene1.getStart() + "-" + Gene1.getEnd() + " -- " + Gene2.getGenename() + "::" + Gene2.getStart() + "-" + Gene2.getEnd());
                            sw = true;
                            break;
                        }
                    }
                    if(sw){
                        break;
                    }
                }
                if(sw){
                    writer.write(sCurrentLine + "\n");
                    //System.out.println(sCurrentLine);
                }
            }
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean Genecmp(Gene gene1, Gene gene2, Double lim){
        Double gene1Len = acc2len.get(gene1.getGenename());
        Double gene2Len = acc2len.get(gene2.getGenename());
        if(gene2.getStart() < gene1.getStart()){
            Gene tmp = gene1;
            gene1 = gene2;
            gene2 = tmp;
        }
        else if(gene2.getStart() == gene1.getStart()){
            if(gene1.getEnd() > gene2.getEnd()){
                Gene tmp = gene1;
                gene1 = gene2;
                gene2 = tmp;
            }
        }
        if (gene1.getEnd() - gene2.getStart() + 1 < (gene1Len+gene2Len) * (lim/100)) {
            return true;
        }
        else { return false;}
    }
}
