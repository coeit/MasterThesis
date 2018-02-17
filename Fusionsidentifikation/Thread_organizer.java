import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Constantin on 07.06.17.
 * args[0] = qcov ; args[1] = archiveList ; args[3] = Threadcount
 */
public class Thread_organizer {
    static HashMap<String, Double> acc2len;
    static int archiveSize = 0;
    public static void main(String[] args) {
        acc2len = buildLenHash(); /**CREATE HASH FOR LENGTH **/
        System.err.println("Sequence Length Hashing ... DONE!");

        ArrayList<ArrayList<String>> archiveCut = readArchiveList2(args[1].trim(), Integer.parseInt(args[2]));
        /*for (int i = 0; i < archiveCut.size(); i++){
            //System.out.print(i + ":: ");
            for (String line : archiveCut.get(i)){
                System.out.print(line + "\t");
            }
            System.out.println();
        }*/
        int t = 0;
        for (ArrayList<String> archive : archiveCut) {
            Thread thread = new Thread(new FuDet(args[0], archive, acc2len, t));
            thread.start();
            System.err.println("Thread " + t + " started!");
            t++;
        }
    }

    static private String getArcGCF(String archive){
        String[] split = archive.split("GCF");
        String[] split2 = split[1].split("_");
        String GCF = "GCF_" + split2[1];
        return GCF;
    }

    /**Seperates BLAST Archive list according to Thread count and Organisms**/
    static private ArrayList<ArrayList<String>> readArchiveList2(String filename, int Threadcnt) {
        int partsize = 0;
        int lines = 0;
        try {
            lines = countLines(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
        archiveSize = lines;
        partsize = lines/Threadcnt;

        int linecnt = 1;
        FileReader fr = null;
        BufferedReader br = null;
        ArrayList<ArrayList<String>> ret = new ArrayList<ArrayList<String>>();
        ArrayList<String> tmp = new ArrayList<String>();
        try {
            fr = new FileReader(filename);
            br = new BufferedReader(fr);
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                tmp.add(sCurrentLine);
                if(linecnt%partsize == 0){
                    ret.add(tmp);
                    tmp = new ArrayList<String>();
                }
                if(linecnt == lines){
                    for (String s : tmp){
                        ret.get(Threadcnt-1).add(s);
                    }
                }
                linecnt++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)
                    br.close();
                if (fr != null)
                    fr.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        for(int i = ret.size()-1; i > 0; i--){
            while(ret.get(i).size() != 0){
                if(getArcGCF(ret.get(i).get(0)).equals(getArcGCF(ret.get(i-1).get(ret.get(i-1).size()-1)))){
                    ret.get(i-1).add(ret.get(i).get(0));
                    ret.get(i).remove(0);
                } else {
                    break;
                }
            }
        }
        return ret;

    }

    /**Accesion (GCF_xx_WP_xx ==> Sequence Length)**/
    static private HashMap<String, Double> buildLenHash(){
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

    public static int countLines(String filename) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(filename));
        try {
            byte[] c = new byte[1024];
            int count = 0;
            int readChars = 0;
            boolean empty = true;
            while ((readChars = is.read(c)) != -1) {
                empty = false;
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
            }
            return (count == 0 && !empty) ? 1 : count;
        } finally {
            is.close();
        }
    }
}
